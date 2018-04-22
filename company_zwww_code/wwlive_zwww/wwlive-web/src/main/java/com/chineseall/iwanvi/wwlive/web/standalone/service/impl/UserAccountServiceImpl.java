package com.chineseall.iwanvi.wwlive.web.standalone.service.impl;

import com.alibaba.fastjson.JSON;
import com.chineseall.iwanvi.wwlive.common.cache.RedisClientAdapter;
import com.chineseall.iwanvi.wwlive.common.cache.RedisExpireTime;
import com.chineseall.iwanvi.wwlive.common.cache.RedisKey;
import com.chineseall.iwanvi.wwlive.common.constants.Constants;
import com.chineseall.iwanvi.wwlive.common.external.rongcloud.RongCloudFacade;
import com.chineseall.iwanvi.wwlive.common.tools.HttpUtils;
import com.chineseall.iwanvi.wwlive.common.tools.StrMD5;
import com.chineseall.iwanvi.wwlive.dao.wwlive.OauthInfoMapper;
import com.chineseall.iwanvi.wwlive.dao.wwlive.UserInfoMapper;
import com.chineseall.iwanvi.wwlive.dao.wwlive.UserPushMapper;
import com.chineseall.iwanvi.wwlive.domain.wwlive.OauthInfo;
import com.chineseall.iwanvi.wwlive.domain.wwlive.UserInfo;
import com.chineseall.iwanvi.wwlive.domain.wwlive.UserPush;
import com.chineseall.iwanvi.wwlive.web.common.constants.WebConstants;
import com.chineseall.iwanvi.wwlive.web.common.helper.UserInfoHelper;
import com.chineseall.iwanvi.wwlive.web.common.util.DateTools;
import com.chineseall.iwanvi.wwlive.web.common.util.UserAccountUtils;
import com.chineseall.iwanvi.wwlive.web.standalone.service.UserAccountService;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.HttpURLConnection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Niu Qianghong on 2017-06-27 0027.
 */
@Service
public class UserAccountServiceImpl implements UserAccountService {

    private Logger logger = Logger.getLogger(this.getClass());

    @Value("${cx.uri.userinfo.bydevice}")
    private String getUserFromCxUrl; // 通过mac + imei获取创新版用户接口地址

    private final String getWxTokenUrl = "https://api.weixin.qq.com/sns/oauth2/access_token";

    @Value("${wechat.login.appid}")
    private String wxAppid;

    @Value("${wechat.login.secret}")
    private String wxAppSecret;

    @Autowired
    private RedisClientAdapter redisAdapter;

    @Autowired
    private UserInfoMapper userInfoMapper;

    @Autowired
    private OauthInfoMapper oauthInfoMapper;

    @Autowired
    private UserPushMapper userPushMapper;

    @Value("${cx.uri.userid.bylogin}")
    private String getCxUserIdUrl; // 通过账号密码获取创新版用户id

    @Value("${wechat.login.appid}")
    private String iOSWXAppid;

    @Value("${wechat.login.secret}")
    private String iOSWXSecret;

    /**
     * 直播用户注册
     *
     * @return
     */
    @Override
    public Map<String, Object> register(String credential, String ip, Integer platform,
                                        String version, Integer pushType, String deviceToken, String cnid) {
        Map<String, Object> resultJson = new HashMap<>();

        UserInfo user = null;
        // 先根据设备信息从oauth表中查询用户信息
        user = userInfoMapper.getUserByOpenId(0, credential);// 0-使用设备信息唯一确定某用户
        logger.error("从本地根据设备信息查询用户: use = " + user);
        if (user != null && user.getUserId() != null) {
            resultJson.put("result", 1);
            resultJson.put("userInfo", user);
            logger.error("认证表中根据设备信息可以查到用户: user = " + user);
            return resultJson;
        }

        // 直接分配用户
        user = creatNewUser(credential, ip, platform, version);
        logger.error("直接注册用户, user = " + user);
        resultJson.put("result", user == null ? 0 : 1);
        resultJson.put("userInfo", user);

        String pushKey = RedisKey.ZBPUSH_ + pushType + "_" + user.getUserId();
        if (user != null && !redisAdapter.existsKey(pushKey)) {
            // 添加user-push映射关系记录, 当前为友盟
            UserPush userPush = new UserPush();
            userPush.setAppVersion(version);
            userPush.setDeviceToken(deviceToken);
            userPush.setUserId(user.getUserId());
            userPush.setPushType(pushType);
            userPush.setPlatform(platform);
            userPush.setAppCnid(cnid);
            int result = userPushMapper.addUserPush(userPush);
            logger.error("创建user-push对应关系记录, result = " + result);
            String userPushKey = RedisKey.ZBPUSH_ + WebConstants.PUSH_UMENG + "_" + user.getUserId();
            redisAdapter.strSetByNormal(userPushKey, deviceToken);
            redisAdapter.expireKey(userPushKey, RedisExpireTime.EXPIRE_DAY_1);
        }

        return resultJson;
    }

    /**
     * 利用设备信息直接创建直播新用户
     *
     * @return
     */
    private UserInfo creatNewUser(String credential, String ip, Integer platform, String version) {
        UserInfo user = new UserInfo();
        user.setOrigin(2); // 2-直接注册
        String loginId = getNextId();
        String defaultName = UserInfoHelper.getDefaultName(loginId);
        loginId += "_zb";
        user.setLoginId(loginId);
        user.setUserName(defaultName); // 游客 + 八位纯数字
        user.setSex(1); // 默认性别为男
        Date now = new Date();
        user.setBirthday(now);
        user.setZodiac(DateTools.getZodiacByDate(now));

        user.setAcctStatus(0); // 0-正常
        user.setAcctType(0); // 0-普通用户
        user.setVirtualCurrency(0L); // 积分默认为0
        try {
            String userName = user.getUserName();
            String token = RongCloudFacade.getToken(loginId, StringUtils.isBlank(userName) ? "" : userName,
                    "", 1);
            user.setRongToken(token);
        } catch (Exception e) {
            logger.error("独立APP: 获取融云token时异常", e);
        }
        user.setUserIp(ip);
        user.setAppVersion(version);
        user.setPlatform(platform);
        user.setCreateTime(now);
        user.setUpdateTime(now);
        user.setLoginOn(now);
        user.setVersionOptimizedLock(0);
        user.setToken(Constants.getUUID());

        // 默认账号规则: 默认昵称去掉游客之后的8位数字
        String account = defaultName.replace("游客", "");
        user.setAccount(account);

        String date = DateTools.formatDate(now, "MMdd");
        String password = account + new StringBuilder(date).reverse().toString();
        password = StrMD5.getInstance().getStringMD5(password);
        user.setPassword(password);// 默认密码为账号 + 创建日期倒序拼接, 例如07-12日创建用户, loginId为Y123_zb, 则密码为Y123_zb2170
        int result = userInfoMapper.insertUserInfo(user);
        if (result > 0) {
            Long userId = user.getUserId();
            String token = user.getToken();
            // 缓存用户信息并添加设备信息认证记录
            cacheAndAuthUser(user, credential);
            // 缓存uid和token对应key
            String tokenKey = RedisKey.UID_TO_TOKEN_ + userId;
            redisAdapter.strSetByNormal(tokenKey, token);
            String idKey = RedisKey.TOKEN_TO_UID_ + token;
            redisAdapter.strSetByNormal(idKey, userId.toString());
        }
        return user;
    }

    /**
     * 通过创新版信息注册直播用户
     * 创建用户之后要添加设备信息认证和账号密码信息认证两种
     *
     * @param loginId
     * @param cxUser
     * @return
     */
    /*private UserInfo createNewUserByCx(String credential, String loginId, Map<String, Object> cxUser) {
        UserInfo user = new UserInfo();
        user.setOrigin(0); // 0-创新版, 1-中文书城
        user.setLoginId(loginId);
        String userName = (String) cxUser.get("userName");
        user.setUserName(userName);
        String headImg = (String) cxUser.get("headImg");
        user.setHeadImg(headImg);
        user.setAcctStatus(Constants._0);
        user.setAcctType(Constants._0);
        Object points = cxUser.get("userPoints");
        user.setVirtualCurrency(points == null ? 0 : Long.valueOf(points.toString()));
        Date birthday = new Date();
        try {
            DateUtils.parseDate(cxUser.get("birthday").toString(), new String[]{"yyyy-MM-dd"});
            user.setBirthday(birthday);
        } catch (Exception e) {
            logger.error("独立APP: 注册时转换创新版用户生日异常, birthday = " + cxUser.get("birthday").toString(), e);
        }
        user.setZodiac(DateTools.getZodiacByDate(birthday));

        int sex = MapUtils.getIntValue(cxUser, "sex", 0);
        if (sex == 0) {
            sex = 2;
        } else if (sex == 2) {
            sex = 0;
        } // 创新版性别定义0-未知, 1-男, 2-女; 直播性别定义0-女, 1-男, 2-未知
        user.setSex(sex);

        Date now = new Date();
        user.setCreateTime(now);
        user.setUpdateTime(now);
        user.setLoginOn(now);
        user.setVersionOptimizedLock(0);

        user.setUserIp(cxUser.get("ip").toString());
        user.setPlatform((Integer) cxUser.get("platform"));
        user.setAppVersion(cxUser.get("version").toString());
        user.setToken(Constants.getUUID());
        String account = "Y" + loginId;
        user.setAccount(account);

        String date = DateTools.formatDate(now, "MMdd");
        String password = account + new StringBuilder(date).reverse().toString();
        password = StrMD5.getInstance().getStringMD5(password);
        user.setPassword(password);

        try {
            String token = RongCloudFacade.getToken(loginId, StringUtils.isBlank(userName) ? "" : userName,
                    StringUtils.isBlank(headImg) ? "" : headImg, 1);
            user.setRongToken(token);
        } catch (Exception e) {
            logger.error("独立APP: 获取融云token时异常", e);
        }

        int result = userInfoMapper.insertUserInfo(user);
        if (result > 0) {
            Long userId = user.getUserId();

            // 缓存用户信息并添加设备信息认证记录
            cacheAndAuthUser(user, credential);

            // 添加账号密码认证
            String oauthKey = RedisKey.OAUTH_ + WebConstants.OAUTH_USER_PASSWORD + "_" + userId;
            if (!redisAdapter.existsKey(oauthKey)) {
                OauthInfo oauthInfo = new OauthInfo();
                String openId = (String) cxUser.get("account");
                oauthInfo.setOpenId(openId);
                oauthInfo.setAccessToken((String) cxUser.get("password"));
                oauthInfo.setUserId(userId);
                oauthInfo.setOauthType(WebConstants.OAUTH_USER_PASSWORD);
                addOauthInfo(oauthInfo);
                redisAdapter.strSetByNormal(oauthKey, openId);
            }

            // 缓存uid和token对应key
            String token = user.getToken();
            String tokenKey = RedisKey.UID_TO_TOKEN_ + userId;
            redisAdapter.strSetByNormal(tokenKey, token);
            String idKey = RedisKey.TOKEN_TO_UID_ + token;
            redisAdapter.strSetByNormal(idKey, userId.toString());
            return user;
        } else {
            return null;
        }
    }*/

    /**
     * 新建用户之后, 缓存用户信息并添加认证信息
     *
     * @param user
     */
    private void cacheAndAuthUser(UserInfo user, String credential) {
        UserInfoHelper.cacheUserInfo(user, redisAdapter);

        Long userId = user.getUserId();
        // 判断是否已经有认证信息
        String oauthKey = RedisKey.OAUTH_ + WebConstants.OAUTH_DEVICE + "_" + userId;
        if (!redisAdapter.existsKey(oauthKey)) {
            // 添加设备信息与用户信息绑定记录[oauth表中]
            OauthInfo oauthInfo = new OauthInfo();
            oauthInfo.setOauthType(0); // 0-创新版/直接注册, 1-微信, 2-QQ, 3-直接注册
            oauthInfo.setUserId(userId);
            oauthInfo.setOpenId(credential);
            oauthInfoMapper.addOauthInfo(oauthInfo);
            // 把认证信息缓存到redis中[userId<-->token]
            redisAdapter.strSetByNormal(oauthKey, credential);
            redisAdapter.expireKey(oauthKey, RedisExpireTime.EXPIRE_DAY_1);
        }
    }

    /**
     * 此方法暂时不用
     * @param cxUser
     * @param credential
     * @return
     */
    /*@Deprecated
    private UserInfo getOrCreateUserByCx(Map<String, Object> cxUser, String credential) {
        String loginId = cxUser.get("uid").toString() + "_cx";

        // 如果该loginId有对应的直播用户, 直接返回
        UserInfo userInfo = userInfoMapper.findUserAllInfoByLoginId(loginId);
        if (userInfo != null) {
            Long userId = userInfo.getUserId();
            // 添加ip, token, account, password, platform, version字段
            if (StringUtils.isBlank(userInfo.getToken())) {
                UserInfo newUser = new UserInfo();
                newUser.setUserId(userId);
                String token = Constants.getUUID();
                newUser.setToken(token);
                String account = "Y" + userInfo.getLoginId();
                newUser.setAccount(account);
                Integer platform = (Integer) cxUser.get("platform");
                newUser.setPlatform(platform);
                String ip = (String) cxUser.get("ip");
                newUser.setUserIp(ip);
                String version = (String) cxUser.get("version");
                newUser.setAppVersion(version);

                String date = DateTools.formatDate(userInfo.getCreateTime(), "MMdd");
                String password = account + new StringBuilder(date).reverse().toString();
                password = StrMD5.getInstance().getStringMD5(password);
                newUser.setPassword(password);
                userInfoMapper.updateUserInfoById(newUser);
                // 清除旧的缓存
                redisAdapter.delKeys(RedisKey.USER_INFO_ + userId);

                userInfo.setToken(token);
                userInfo.setAppVersion(version);
                userInfo.setPassword(password);
                userInfo.setPlatform(platform);
                userInfo.setUserIp(ip);
                userInfo.setAccount(account);
            }
            // 判断是否已经有认证信息
            String oauthKey = RedisKey.OAUTH_ + WebConstants.OAUTH_DEVICE + "_" + userId;
            if (!redisAdapter.existsKey(oauthKey)) {
                // 添加设备信息与用户信息绑定记录[oauth表中]
                OauthInfo oauthInfo = new OauthInfo();
                oauthInfo.setOauthType(WebConstants.OAUTH_DEVICE); // 0-设备信息认证, 1-微信, 2-QQ
                oauthInfo.setUserId(userId);
                oauthInfo.setOpenId(credential);
                oauthInfoMapper.addOauthInfo(oauthInfo);
                // 缓存认证信息
                redisAdapter.strSetByNormal(oauthKey, credential);

            }
            // 添加账号密码认证
            String passOauthKey = RedisKey.OAUTH_ + WebConstants.OAUTH_USER_PASSWORD + "_" + userId;
            if (!redisAdapter.existsKey(passOauthKey)) {
                OauthInfo oauthInfo = new OauthInfo();
                String openId = (String) cxUser.get("account");
                oauthInfo.setOpenId(openId);
                oauthInfo.setAccessToken((String) cxUser.get("password"));
                oauthInfo.setUserId(userId);
                oauthInfo.setOauthType(WebConstants.OAUTH_USER_PASSWORD);
                addOauthInfo(oauthInfo);
                redisAdapter.strSetByNormal(passOauthKey, openId);
            }
            return userInfo;
        }

        // 创建直播用户
        return createNewUserByCx(credential, loginId, cxUser);
    }*/

    /**
     * 从redis中读取当前分配的loginId, 计算出下一个loginId
     *
     * @return
     */
    private String getNextId() {
        String loginId = null;
        if (redisAdapter.existsKey(RedisKey.NEXT_LOGINID)) {
            loginId = redisAdapter.strGet(RedisKey.NEXT_LOGINID);
            redisAdapter.strIncr(RedisKey.NEXT_LOGINID); // loginId + 1
        } else {
            Long lastLoginId = userInfoMapper.getLastId();
            loginId = (lastLoginId + 1) + "";
            redisAdapter.strSetByNormal(RedisKey.NEXT_LOGINID, (lastLoginId + 2) + "");
        }

        return loginId;
    }

    /**
     * 根据绑定信息查询该用户是否绑定过某第三方应用
     *
     * @param oauthType 1=微信, 2=QQ
     * @param userId
     * @param openId
     * @return
     */
    /*private boolean checkBound(int oauthType, Long userId, String openId) {
        OauthInfo oauthInfo = oauthInfoMapper.getByUserIdAndOpenId(oauthType, userId, openId);
        return oauthInfo != null;
    }*/

    /**
     * 绑定微信
     *
     * @param userId
     * @param token
     * @param code
     * @param platform
     * @return 0-失败, 1-成功, 2-用户已绑定过微信, 3-该微信已被绑定过, 4-用户异常
     */
    @Override
    public Map<String, Object> bindToWeChat(Long userId, String token, String code, String platform) {

        Map<String, Object> resultJson = new HashMap<>();

        if (!token.equals(UserAccountUtils.getTokenByUserId(userId, redisAdapter, userInfoMapper))) {
            logger.info("wx: 用户校验失败");
            resultJson.put("result", 4);
            return resultJson;
        }

        // 检查用户是否已经绑定过某微信
        String oauthKey = RedisKey.OAUTH_ + WebConstants.OAUTH_WECHAT + "_" + userId;
        if (redisAdapter.existsKey(oauthKey)) {
            logger.info("wx: 该用户已绑定微信");
            resultJson.put("result", 2);
            return resultJson;
        }
        // 如果微信已经绑定过，则直接返回信息
        OauthInfo oauth = oauthInfoMapper.getByTypeAndUid(WebConstants.OAUTH_WECHAT, userId);
        if (oauth != null) {
            // 缓存认证信息
            redisAdapter.hashMSet(oauthKey, oauth.toStringMap());
            redisAdapter.expireKey(oauthKey, RedisExpireTime.EXPIRE_DAY_1);
            resultJson.put("result", 2);
            return resultJson;
        }
        
        // 获得token + openId
        Map<String, Object> tokenAndOpenid = getWxTokenAndOpenid(code, platform);
        logger.info("wx: token + openId = " + tokenAndOpenid);
        String openid = (String) tokenAndOpenid.get("openid");
        if (tokenAndOpenid == null
                || tokenAndOpenid.get("access_token") == null
                || tokenAndOpenid.get("openid") == null) {
            resultJson.put("result", 0);
            return resultJson;
        }

        // 获取用户信息
        UserInfo user = UserInfoHelper.getAndCacheUserInfo(redisAdapter, userInfoMapper, userId);
        logger.info("wx: 用户信息 = " + user);
        if (user == null) {
            resultJson.put("result", 0);
            return resultJson;
        }

        // 如果微信已经绑定过某用户, 提示该微信已经被其他用户绑定
        oauth = oauthInfoMapper.getByTypeAndOpenId(WebConstants.OAUTH_WECHAT, openid);
        logger.info("wx: 该微信账号绑定信息 = " + oauth);
        if (oauth != null) {
            resultJson.put("result", 3);
            return resultJson;
        }

        // 查询是否已经绑定过
        /*if (checkBound(1, userId, openid)) {
            resultJson.put("result", 2);
            // resultJson.put("userInfo", user);
            return resultJson;
        }*/

        // 根据token获得用户资料
        Map<String, Object> wxUser = getWxUser(tokenAndOpenid);
        logger.info("wx: 微信用户资料 = " + wxUser);

        // 添加第三方绑定记录[openId, access_token, 用户基本资料]
        OauthInfo oauthInfo = new OauthInfo();
        oauthInfo.setUserId(userId);
        oauthInfo.setAccessToken((String) tokenAndOpenid.get("access_token"));
        oauthInfo.setOpenId(openid);
        oauthInfo.setNickname((String) wxUser.get("nickname"));
        Integer sex = (Integer) wxUser.get("sex");
        if (sex == 2) {// 微信性别 1=男, 2=女; 本地 0=女, 1=男
            sex = 0;
        }
        oauthInfo.setSex(sex);
        oauthInfo.setAvatar((String) wxUser.get("headimgurl"));
        oauthInfo.setOauthType(1); // 0-创新版, 1-微信, 2-QQ
        int cnt = addOauthInfo(oauthInfo);
        logger.info("wx: 添加认证信息结果 = " + cnt);
        if (cnt < 1) {
            resultJson.put("result", 0);
            return resultJson;
        }
        // 缓存认证信息
        redisAdapter.hashMSet(oauthKey, oauthInfo.toStringMap());
        redisAdapter.expireKey(oauthKey, RedisExpireTime.EXPIRE_DAY_1);

        // 根据微信用户资料同步修改用户头像
        UserInfo userInfo = syncUserAvatarAndNickname(user, (String) wxUser.get("headimgurl"), (String) wxUser.get("nickname"));
        logger.info("wx: 同步后的用户资料 = " + userInfo);

        resultJson.put("result", 1);
        resultJson.put("userInfo", userInfo);
        return resultJson;
    }

    /**
     * 绑定QQ
     *
     * @param userId
     * @param openId QQ的唯一id
     * @param accessToken
     * @param sex
     * @param avatar
     * @param nickname
     * @return 0-失败, 1-成功, 2-用户已绑定QQ, 3-该QQ已被绑定过, 4-用户异常
     */
    @Override
    public Map<String, Object> bindToQQ(Long userId, String token, String openId, String accessToken, String sex, String avatar, String nickname) {

        Map<String, Object> resultJson = new HashMap<>();

        if (!token.equals(UserAccountUtils.getTokenByUserId(userId, redisAdapter, userInfoMapper))) {
            resultJson.put("result", 4);
            return resultJson;
        }

        // 检查用户是否已绑定某个QQ
        String oauthKey = RedisKey.OAUTH_ + WebConstants.OAUTH_QQ + "_" + userId;
        if (redisAdapter.existsKey(oauthKey)) {
            resultJson.put("result", 2);
            return resultJson;
        }
        
        // 如果QQ已经绑定过，则直接返回信息
        OauthInfo oauth = oauthInfoMapper.getByTypeAndUid(WebConstants.OAUTH_QQ, userId);
        if (oauth != null) {
            // 缓存认证信息
            redisAdapter.hashMSet(oauthKey, oauth.toStringMap());
            redisAdapter.expireKey(oauthKey, RedisExpireTime.EXPIRE_DAY_1);
            resultJson.put("result", 2);
            return resultJson;
        }

        // 如果QQ已经绑定过某用户, 提示该QQ已经被其他用户绑定
        oauth = oauthInfoMapper.getByTypeAndOpenId(WebConstants.OAUTH_QQ, openId);
        if (oauth != null) {
            resultJson.put("result", 3);
            return resultJson;
        }

        UserInfo userInfo = UserInfoHelper.getAndCacheUserInfo(redisAdapter, userInfoMapper, userId);
        if (userInfo == null) {
            resultJson.put("result", 0);
            return resultJson;
        }

        /*// 检查该用户是否已经绑定该QQ
        if (checkBound(2, userId, openId)) {
            resultJson.put("result", 2);
            return resultJson;
        }*/

        // 添加QQ授权记录
        OauthInfo oauthInfo = new OauthInfo();
        oauthInfo.setOauthType(2);
        oauthInfo.setUserId(userId);
        oauthInfo.setOpenId(openId);
        oauthInfo.setAvatar(avatar);
        oauthInfo.setNickname(nickname);
        int sexCode = 0;
        if ("男".equals(sex)) {
            sexCode = 1;
        }
        oauthInfo.setSex(sexCode);
        oauthInfo.setAvatar(avatar);
        oauthInfo.setAccessToken(accessToken);
        int cnt = addOauthInfo(oauthInfo);
        if (cnt < 1) {
            resultJson.put("result", 0);
            return resultJson;
        }
        // 缓存认证信息
        redisAdapter.hashMSet(oauthKey, oauthInfo.toStringMap());
        redisAdapter.expireKey(oauthKey, RedisExpireTime.EXPIRE_DAY_1);

        // 同步用户资料
        syncUserAvatarAndNickname(userInfo, avatar, nickname);

        resultJson.put("result", 1);
        resultJson.put("userInfo", userInfo);
        return resultJson;

    }

    /**
     * 微信登录
     *
     * @param userId
     * @param token
     * @param code
     * @return {"result": 0, "userInfo" :{}} 0-失败, 1-成功, 2-未绑定
     */
    @Override
    public Map<String, Object> loginByWeChat(Long userId, String token, String code,
                                             Integer pushType, String deviceToken, Integer platform, String version, String cnid) {

        Map<String, Object> resultJson = new HashMap<>();

        // 验证token和userId是否匹配
        if (!token.equals(UserAccountUtils.getTokenByUserId(userId, redisAdapter, userInfoMapper))) {
            resultJson.put("result", 0);
            return resultJson;
        }

        Map<String, Object> tokenAndOpenid = getWxTokenAndOpenid(code, platform == 1 ? "iOS":"Android");
        if (tokenAndOpenid == null) {
            resultJson.put("result", 0);
            return resultJson;
        }

        String openId = (String) tokenAndOpenid.get("openid");
        UserInfo user = userInfoMapper.getUserByOpenId(1, openId);
        resultJson.put("result", user == null ? 2 : 1);
        resultJson.put("userInfo", user);

        int result = (int) resultJson.get("result");
        if (result == 1) {
            // 更新push用到的deviceToken
            updateDeviceToken(pushType, userId, deviceToken, platform, version, cnid);
        }

        return resultJson;
    }

    /**
     * QQ登录
     *
     * @param userId
     * @param token
     * @param openId
     * @return {"result": 0, "userInfo" :{}} 0-失败, 1-成功, 2-未绑定
     */
    @Override
    public Map<String, Object> loginByQQ(Long userId, String token, String openId,
                                         Integer pushType, String deviceToken, Integer platform, String version, String cnid) {

        Map<String, Object> resultJson = new HashMap<>();

        // 验证token和userId是否匹配
        if (!token.equals(UserAccountUtils.getTokenByUserId(userId, redisAdapter, userInfoMapper))) {
            resultJson.put("result", 0);
            return resultJson;
        }

        UserInfo user = userInfoMapper.getUserByOpenId(2, openId);
        resultJson.put("result", user == null ? 2 : 1);
        resultJson.put("userInfo", user);

        int result = (int) resultJson.get("result");
        if (result == 1) {
            // 更新push用到的deviceToken
            updateDeviceToken(pushType, userId, deviceToken, platform, version, cnid);
        }

        return resultJson;
    }

    /**
     * 用户名密码登录, 若登录失败, 则根据用户名查找该用户绑定的第三方信息[微信, QQ]
     *
     * @param userId
     * @param token
     * @param userName
     * @param password
     * @return result =0, 登录失败且无绑定; =1, 登录成功; =2, 登录失败有绑定; =3, 该用户已登录; =4, 用户异常
     */
    @Override
    public Map<String, Object> loginByNormal(Long userId, String token, String userName, String password,
                                             Integer pushType, String deviceToken, Integer platform, String version, String cnid) {
        Map<String, Object> resultJson = new HashMap<>();

        // 验证token和userId是否匹配
        if (!token.equals(UserAccountUtils.getTokenByUserId(userId, redisAdapter, userInfoMapper))) {
            resultJson.put("result", 4);
            return resultJson;
        }

        StrMD5 strMD5 = StrMD5.getInstance();
        // 使用直播用户账号密码登录
        String passwordMD5 = strMD5.getStringMD5(password);
        UserInfo user = userInfoMapper.getByPassword(userName, passwordMD5);
        logger.warn("查询直播用户------user = " + user);
        if (user == null) {
            // 获得创新版userId
            String cxUserId = getCxUserId(userName, password);
            logger.warn("根据账号密码查创新版-----userId = " + cxUserId);
            if(!"no".equals(cxUserId)){
                // 根据创新版id查询
                user = userInfoMapper.findUserAllInfoByLoginId(cxUserId + "_cx");
                logger.warn("根据创新版loginId查直播用户-----user = " + user);
                if (user == null){
                    // 根据创新版创建直播新用户
                    user = createNewUserByCxId(cxUserId, platform, version);
                    logger.warn("根据创新版id创建用户-----user = " + user);
                } else {
                    // 只要登录过独立版用户的免电用户都将其来源修改为直接注册
                    if (2 != user.getOrigin()) {
                        UserInfo newUser = new UserInfo();
                        Long uid = user.getUserId();
                        newUser.setUserId(uid);
                        newUser.setOrigin(2); // 2-直接注册
                        userInfoMapper.updateUserInfoById(newUser);
                        // 清除缓存信息
                        redisAdapter.delKeys(RedisKey.USER_INFO_ + uid);
                    }
                }
            }
        }

        if (user == null) {
            // 根据用户名查询绑定信息
            List<OauthInfo> oauths = oauthInfoMapper.getByAccount(userName);
            logger.warn("查询绑定信息-----oauths = " + oauths);

            if (oauths != null && oauths.size() > 0) {
                resultJson.put("result", 2);
                resultJson.put("oauths", oauths);
            } else {
                resultJson.put("result", 0);
            }

        } else {
            // 如果用户没有新增字段的信息, 则添加
            UserInfoHelper.addNewInfo(user, userInfoMapper);
            if (user.getUserId().equals(userId)) {
                resultJson.put("result", 3);
                return resultJson;
            }

            resultJson.put("result", 1);
            resultJson.put("userInfo", user);
        }

        int result = (int) resultJson.get("result");
        if (result == 1) {
            // 更新push用到的deviceToken
            updateDeviceToken(pushType, user.getUserId(), deviceToken, platform, version, cnid);
            logger.warn("更新用户push");
        }
        logger.warn("返回结果-----" + resultJson);
        return resultJson;
    }

    @Override
    public OauthInfo queryBound(int oauthWechat, Long userId) {
        OauthInfo oauthInfo = UserAccountUtils
                .getOauthByTypeAndId(userId, WebConstants.OAUTH_WECHAT, redisAdapter, oauthInfoMapper);
        return oauthInfo;
    }

    private UserInfo createNewUserByCxId(String cxUserId, int platform, String version) {
        UserInfo user = new UserInfo();
        user.setOrigin(2); // 2-直接注册
        String defaultName = UserInfoHelper.getDefaultName(cxUserId);
        String loginId = cxUserId + "_cx";
        user.setLoginId(loginId);
        user.setUserName(defaultName); // 游客 + 八位纯数字
        user.setSex(1); // 默认性别为男
        Date now = new Date();
        user.setBirthday(now);
        user.setZodiac(DateTools.getZodiacByDate(now));

        user.setAcctStatus(0); // 0-正常
        user.setAcctType(0); // 0-普通用户
        user.setVirtualCurrency(0L); // 积分默认为0
        try {
            String userName = user.getUserName();
            String token = RongCloudFacade.getToken(loginId, StringUtils.isBlank(userName) ? "" : userName,
                    "", 1);
            user.setRongToken(token);
        } catch (Exception e) {
            logger.error("独立APP: 获取融云token时异常", e);
        }
        user.setAppVersion(version);
        user.setPlatform(platform);
        user.setCreateTime(now);
        user.setUpdateTime(now);
        user.setLoginOn(now);
        user.setVersionOptimizedLock(0);
        user.setToken(Constants.getUUID());

        // 默认账号规则: 默认昵称去掉游客之后的8位数字
        String account = defaultName.replace("游客", "");
        user.setAccount(account);

        String date = DateTools.formatDate(now, "MMdd");
        String password = account + new StringBuilder(date).reverse().toString();
        password = StrMD5.getInstance().getStringMD5(password);
        user.setPassword(password);// 默认密码为账号 + 创建日期倒序拼接, 例如07-12日创建用户, loginId为Y123_zb, 则密码为Y123_zb2170
        int result = userInfoMapper.insertUserInfo(user);
        if (result > 0) {
            Long userId = user.getUserId();
            String token = user.getToken();
            // 缓存用户信息并添加设备信息认证记录
            UserInfoHelper.cacheUserInfo(user, redisAdapter);
            // 缓存uid和token对应key
            String tokenKey = RedisKey.UID_TO_TOKEN_ + userId;
            redisAdapter.strSetByNormal(tokenKey, token);
            String idKey = RedisKey.TOKEN_TO_UID_ + token;
            redisAdapter.strSetByNormal(idKey, userId.toString());
            return user;
        }
        return null;
    }

    /**
     * 若登录时deviceToken不同, 则更新; 相同则不更新
     *
     * @param pushType
     * @param userId
     * @param deviceToken
     * @param platform
     * @param version
     */
    public void updateDeviceToken(Integer pushType, Long userId, String deviceToken, Integer platform, String version, String cnid) {
        String userPushKey = RedisKey.ZBPUSH_ + pushType + "_" + userId;
        String oldToken = redisAdapter.strGet(userPushKey);
        if (!deviceToken.equals(oldToken)) {
            UserPush userPush = new UserPush();
            userPush.setPushType(pushType);
            userPush.setDeviceToken(deviceToken);
            userPush.setPlatform(platform);
            userPush.setAppVersion(version);
            userPush.setAppCnid(cnid);
            userPushMapper.updateByUidAndType(pushType, userId, userPush);
            // 更新缓存
            redisAdapter.strSet(userPushKey, deviceToken);
        }
    }

    @Override
    public UserInfo getAllInfoById(Long userId) {
        UserInfo user = UserInfoHelper.getUserInfoFromCache(redisAdapter, userId);
        if (user == null) {
            user = UserInfoHelper.getAndCacheUserInfo(redisAdapter, userInfoMapper, userId);
        }
        return user;
    }

    /**
     * 添加一条第三方绑定记录
     *
     * @param oauthInfo
     * @return
     */
    private int addOauthInfo(OauthInfo oauthInfo) {
        try {
            int cnt = oauthInfoMapper.addOauthInfo(oauthInfo);
            return cnt;
        } catch (Exception e) {
            logger.error("添加第三方绑定记录失败, oauthInfo = ");
        }
        return 0;
    }

    /**
     * 从第三方应用同步用户的头像和昵称资料
     *
     * @param localUser
     * @param headImg
     * @param nickname
     * @return
     */
    private UserInfo syncUserAvatarAndNickname(UserInfo localUser, String headImg, String nickname) {
        if (StringUtils.isBlank(headImg) && StringUtils.isBlank(nickname)) {
            return localUser;
        }

        UserInfo newUser = new UserInfo();
        newUser.setUserId(localUser.getUserId());
        String localHeadImg = localUser.getHeadImg();
        if (StringUtils.isBlank(localHeadImg) || localHeadImg.endsWith("/cx/")) { // 免电用户头像默认为"http://imgs.ikanshu.cn/cx/"
            newUser.setHeadImg(headImg);
            localUser.setHeadImg(headImg);
            redisAdapter.hashSet(RedisKey.USER_INFO_ + localUser.getUserId(), "headImg", headImg);
        }

        if (localUser.getUserName().startsWith("游客")) {
            newUser.setUserName(nickname);
            localUser.setUserName(nickname);
            redisAdapter.hashSet(RedisKey.USER_INFO_ + localUser.getUserId(), "userName", nickname);
        }

        // 更新数据库
        userInfoMapper.updateUserInfoById(newUser);

        return localUser;
    }

    private Map<String, Object> getWxUser(Map<String, Object> tokenAndOpenid) {
        StringBuilder url = new StringBuilder("https://api.weixin.qq.com/sns/userinfo");
        url.append("?access_token=" + tokenAndOpenid.get("access_token"));
        url.append("&openid=" + tokenAndOpenid.get("openid"));
        try {
            String json = HttpUtils.getJSON(url.toString(), Constants.UTF8);
            Map res = (Map) JSON.parse(json);
            return res;
        } catch (Exception e) {
            logger.error("独立APP: 获取微信用户资料时异常, tokenAndOpenid = " + tokenAndOpenid + "--> ", e);
        }
        return null;
    }

    private Map<String, Object> getWxTokenAndOpenid(String code, String platform) {
        StringBuilder url = new StringBuilder("https://api.weixin.qq.com/sns/oauth2/access_token");
        url.append("?appid=" + ("iOS".equals(platform) ? iOSWXAppid: wxAppid));
        url.append("&secret=" + ("iOS".equals(platform) ? iOSWXSecret : wxAppSecret));
        url.append("&code=" + code);
        url.append("&grant_type=authorization_code");
        logger.info("wx: url = " + url.toString());
        try {
            String json = HttpUtils.getJSON(url.toString(), Constants.UTF8);
            Map res = (Map) JSON.parse(json);
            return res;
        } catch (Exception e) {
            logger.error("独立APP: 用户获取微信token时异常, code = " + code + "--> ", e);
        }
        return null;
    }

    /**
     * 根据mac, imei从创新版获取用户信息, 没有相关信息返回null
     *
     * @param credential
     * @return
     */
    private Map<String, Object> getUserFromCx(String credential) {
        try {
            String url = getUserFromCxUrl + "?credential=" + credential;
            String userJson = HttpUtils.getJSON(url, Constants.UTF8);
            Map userInfo = (Map) JSON.parse(userJson);
            return userInfo;
        } catch (Exception e) {
            logger.error("独立APP: 获取创新版用户失败, credential = " + credential, e);
            return null;
        }
    }

    /**
     * 根据账号密码获取创新版用户id
     * @param username
     * @param password
     * @return
     */
    private String getCxUserId(String username, String password) {
        try {
            String url = getCxUserIdUrl
                    + "?username=" + username + "&password=" + password + "&state=HTTP/1.1";
            HttpURLConnection conn = HttpUtils.createPostHttpConnection(url);
            String userId = HttpUtils.returnString(conn);
            return userId;
        } catch (Exception e) {
            logger.error("独立APP: 获取创新版用户id异常", e);
        }
        return null;
    }
}