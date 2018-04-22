package com.chineseall.iwanvi.wwlive.web.video.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.chineseall.iwanvi.wwlive.common.cache.RedisClientAdapter;
import com.chineseall.iwanvi.wwlive.common.cache.RedisExpireTime;
import com.chineseall.iwanvi.wwlive.common.cache.RedisKey;
import com.chineseall.iwanvi.wwlive.common.constants.Constants;
import com.chineseall.iwanvi.wwlive.common.exception.IWanviException;
import com.chineseall.iwanvi.wwlive.common.external.rongcloud.RongCloudFacade;
import com.chineseall.iwanvi.wwlive.common.external.rongcloud.domain.SdkHttpResult;
import com.chineseall.iwanvi.wwlive.common.tools.HttpUtils;
import com.chineseall.iwanvi.wwlive.common.tools.PageInfo;
import com.chineseall.iwanvi.wwlive.dao.wwlive.BlackListMapper;
import com.chineseall.iwanvi.wwlive.dao.wwlive.ContributionListMapper;
import com.chineseall.iwanvi.wwlive.dao.wwlive.LiveAdminMapper;
import com.chineseall.iwanvi.wwlive.dao.wwlive.OrderInfoMapper;
import com.chineseall.iwanvi.wwlive.dao.wwlive.RoleInfoMapper;
import com.chineseall.iwanvi.wwlive.dao.wwlive.UserInfoMapper;
import com.chineseall.iwanvi.wwlive.common.tools.StrMD5;
import com.chineseall.iwanvi.wwlive.dao.wwlive.*;
import com.chineseall.iwanvi.wwlive.domain.util.DateUtil;
import com.chineseall.iwanvi.wwlive.domain.wwlive.BlackList;
import com.chineseall.iwanvi.wwlive.domain.wwlive.OauthInfo;
import com.chineseall.iwanvi.wwlive.domain.wwlive.UserInfo;
import com.chineseall.iwanvi.wwlive.web.common.constants.WebConstants;
import com.chineseall.iwanvi.wwlive.web.common.enums.Origin;
import com.chineseall.iwanvi.wwlive.web.common.helper.*;
import com.chineseall.iwanvi.wwlive.web.common.util.DateTools;
import com.chineseall.iwanvi.wwlive.web.common.util.JsonUtils;
import com.chineseall.iwanvi.wwlive.web.common.util.RegexUtils;
import com.chineseall.iwanvi.wwlive.web.common.util.StringTools;
import com.chineseall.iwanvi.wwlive.web.common.util.UserAccountUtils;
import com.chineseall.iwanvi.wwlive.web.event.service.MedalHonorService;
import com.chineseall.iwanvi.wwlive.web.launch.service.FileUploadService;
import com.chineseall.iwanvi.wwlive.web.my.service.MyAcctInfoService;
import com.chineseall.iwanvi.wwlive.web.video.service.UserInfoService;
import com.service.FollowAnchorService;
import com.service.impl.FollowAnchorServiceImpl;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.commons.lang.time.DateUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.text.ParseException;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class UserInfoServiceImpl implements UserInfoService {

    static final Logger LOGGER = Logger.getLogger(UserInfoServiceImpl.class);

    @Autowired
    private UserInfoMapper userInfoMapper;
    @Autowired
    private ContributionListMapper contribMapper;
    @Autowired
    private OrderInfoMapper orderMapper;

    @Autowired
    private LiveAdminMapper adminMapper;

    @Autowired
    private WinningRecordsMapper winningRecordsMapper;

	@Value("${userinfo.cx.uri}")
	private String userInfoCxURL;
	
	@Autowired
	private MedalHonorService medalHonorService;

	@Autowired
	private RoleInfoMapper roleInfoMapper;
	
    @Autowired
    private RedisClientAdapter redisAdapter;

    @Autowired
    private BlackListMapper blackListMapper;

    @Value("${cx.ratio}")
    private String cxRatio;

	@Value("${user.cx.modify.name}")
	String modifyName;

    @Autowired
    FileUploadService fileUploadService;

    //获取关注数
    private static FollowAnchorService followService = new FollowAnchorServiceImpl();

    @Autowired
    private OauthInfoMapper oauthInfoMapper;

    @Autowired
    private MyAcctInfoService myAcctInfoService;

    @Autowired
    private AcctInfoMapper acctInfoMapper;

    @Override
    public Map<String, Object> getUserInfoByLoinId(String loginId, String anchorIdStr, String userIdStr) {
    	 Map<String, String> userInfo = null;
    	if ("-1".equals(loginId) || StringUtils.isBlank(loginId)) {//如果loginId为-1时
    		String userKey = RedisKey.USER_INFO_ + userIdStr;
            if (redisAdapter.existsKey(userKey)) {
            	userInfo = UserInfoHelper.getUserInfoMapFromCache(redisAdapter, Long.valueOf(userIdStr), "userId", "headImg", "userName",
                        "sex", "birthday", "zodiac", "acctType", "loginId");
            } else {
            	userInfo = UserInfoHelper.getAndCacheUserMap(redisAdapter, userInfoMapper, Long.valueOf(userIdStr));
            }
    	} else {
    		userInfo = getUserInfoFromRedis(loginId);//获得用户的缓存信息
    	}
        if (userInfo == null || userInfo.isEmpty()) {
            return null;
        }
		UserRankHelper.setUserInfo(redisAdapter, 
        		contribMapper, userInfo);//获得用户的年龄和全站排行
		
        try {
            String headImg = userInfo.get("headImg");
            if (StringUtils.isBlank(headImg)) {
                userInfo.put("headImg", "");
            } 
        } catch (Exception e) {
        	
        }

        UserRankHelper.setUserInfo(redisAdapter,
                contribMapper, userInfo);//获得用户的年龄和全站排行

        Long userId = Long.valueOf(userInfo.get("userId"));

        int followNum = 0;
        try {
            //获取用户的关注数
            followNum = followService.getFollowNumber(userId);
        } catch (Exception e) {
            LOGGER.error("获取用户关注数失败--->>>" + e);
        }
        userInfo.put("followNum", String.valueOf(followNum));

        Long anchorId = 0L;
        if (anchorIdStr != null) {
            anchorId = Long.valueOf(anchorIdStr);
            //用户类型信息acctType, 0-普通用户, 1-超管, 2-房管
            //此处的acctType不等同与用户表中的acctType
            String acctType = userInfo.get("acctType");
            userInfo.put("userType", acctType);//兼容2.0.0以前的用户资料接口
            LOGGER.info("loginId = " + loginId + ", acctType = " + acctType);
            if ("0".equals(acctType)) {
                //普通用户是否为房管
                if (LiveAdminHelper.isAdmin(redisAdapter, adminMapper, anchorId, userId)) {//普通用户是否为房管
                    userInfo.put("userType", "2");
                }
            }
        }

        Map<String, Object> resultJson = new HashMap<>();

        Map<String, Object> dataMap = new HashMap<>();
        dataMap.putAll(userInfo);
        // 用户勋章信息
        dataMap.put("medals", medalHonorService.getUserMedalsById(userId));

		//获得贵族信息
		List<Integer> nobles = RoleNobleHelper.userRoleNobleLevels(redisAdapter, roleInfoMapper, userId);
		if (nobles != null && nobles.size() > 0) {
			Collections.sort(nobles);
			Collections.reverse(nobles);
		}
		dataMap.put("nobles", nobles);
		// 添加用户余额信息 2017-8-30 11:25:59
        Map<String, Object> acctInfoCoin = UserAcctInfoHelper.getAcctInfoCoin(acctInfoMapper, redisAdapter, userId);
        dataMap.put("balance", (double)MapUtils.getLong(acctInfoCoin, "coin", 0L)/100);

        // 用户对主播的送礼个数
        int giftCnt = getGiftCntToAnchor(anchorId, userId);
        dataMap.put("isCustomer", giftCnt + "");

        // 添加土豪勋章
        String richestUserId = redisAdapter.strGet(RedisKey.RICHEST_MEDAL_OWNER);
        dataMap.put("isRichest", userInfo.get("userId").toString().equals(richestUserId) ? 1 : 0);

        resultJson.put("userInfo", dataMap);
        return resultJson;
    }

    /**
     * 获取某用户送给某主播的礼品个数
     * @param anchorId
     * @param userId
     * @return
     */
    private int getGiftCntToAnchor(Long anchorId, Long userId) {
        String key = RedisKey.USER_CONTRIBUTION_CNT_ + anchorId + Constants.UNDERLINE + userId;
        if (redisAdapter.existsKey(key)) {
            String cnt = redisAdapter.strGet(key);
            return Integer.valueOf(cnt);
        } else {
            int cnt = contribMapper.countByAnchorAndUser(anchorId, userId, 1);
            redisAdapter.strSet(key, cnt);
            redisAdapter.expireKey(key, RedisExpireTime.EXPIRE_DAY_1);
            return cnt;
        }
    }

    private Map<String, String> getUserInfoFromRedis(String loginId) {
        Map<String, String> userInfo = new HashMap<>();
        String userLogin = RedisKey.USER_LOGIN_ID_ + loginId;
        Long userId = 0L;
        if (redisAdapter.existsKey(userLogin)) {
            userId = Long.parseLong(redisAdapter.strGet(userLogin).replaceAll("\"", ""));
            // 先从redis中查询用户信息
            String userKey = RedisKey.USER_INFO_ + userId;
            if (redisAdapter.existsKey(RedisKey.USER_INFO_ + userId)) {
                userInfo = redisAdapter.hashMGet(userKey, "userId", "headImg", "userName",
                        "sex", "birthday", "zodiac", "acctType", "loginId");
            }
        }

        if (userInfo == null || userInfo.isEmpty()) {
            UserInfo user = UserInfoHelper.getAndCacheUserInfoByLoginId(redisAdapter, userInfoMapper, loginId);
            if (user == null) {
                return null;
            }
            userInfo = user.putFieldValueToStringMap();
        }
        return userInfo;
    }

    /**
     * 获得用户的年龄和全站排行
     *
     * @param
     */

    @Override
    public Map<String, Object> getExpenseList(String loginId, int pageNum,
                                              int pageSize) {
        int cnt = orderMapper.countOrderByLoginId(loginId);
        Map<String, Object> resultJson = new HashMap<>();
        if (cnt <= 0) {
            return resultJson;
        }
        List<Map<String, Object>> expenseList = orderMapper.getListByLoginId(
                loginId, (pageNum - 1) * pageSize, pageSize);
        PageInfo pageInfo = new PageInfo(pageNum, pageSize, cnt);
        int payType;
        for (Map<String, Object> map : expenseList) {
            payType = (int) map.get("payType");
            if (payType == Constants._1) {// 1-积分 2-微信 3-支付宝 cxRatio
                Integer virtualCurrency = (Integer) map.get("amt");
                BigDecimal diamondCnt = new BigDecimal(virtualCurrency).divide(
                        new BigDecimal(cxRatio), 2, BigDecimal.ROUND_HALF_UP);
                map.put("diamondCnt", diamondCnt);
            }
        }
        resultJson.put("expenseList", expenseList);
        resultJson.put("pageInfo", pageInfo);
        return resultJson;
    }

    public Map<String, Object> getConsume(String userId, int pageNum,
                                          int pageSize) {
        String userKey = RedisKey.USER_INFO_ + userId;
        String loginId = "";
        Map<String, String> userMap = redisAdapter.hashMGet(userKey, "loginId");
        if (userMap == null || userMap.isEmpty()) {
            if (redisAdapter.existsKey(userKey)) {
                redisAdapter.delKeys(userKey);
            }
            UserInfo user = UserInfoHelper.getAndCacheUserInfo(redisAdapter, userInfoMapper, Long.parseLong(userId));
            loginId = user.getLoginId();
        } else {
            loginId = userMap.get("loginId");
        }

        int cnt = orderMapper.countOrderByLoginId(loginId);
        Map<String, Object> resultJson = new HashMap<>();
        if (cnt <= 0) {
            return resultJson;
        }
        List<Map<String, Object>> expenseList = orderMapper.getListByLoginId(
                loginId, (pageNum - 1) * pageSize, pageSize);

        // 翻牌游戏奖品查询
        for (Map<String, Object> expense:expenseList) {
            int orderType = MapUtils.getIntValue(expense, "orderType", 0);
            if (orderType == 6) {
                List<Map<String, Object>> giftList = winningRecordsMapper.getGiftListByOrder(userId, MapUtils.getLongValue(expense, "orderId"));
                expense.put("gameGiftList", giftList);
            }
        }

        PageInfo pageInfo = new PageInfo(pageNum, pageSize, cnt);
        resultJson.put("expenseList", expenseList);
        resultJson.put("pageInfo", pageInfo);
        return resultJson;
    }

    /**
     * 获得用户融云信息
     */
    public Map<String, Object> getRongInf(long userId) {
        Map<String, Object> resultJson = new HashMap<>();
        UserInfo user = new UserInfo();
        Map<String, String> userInfo = getUserInfoFromRedis(userId, user);
        if (StringUtils.isEmpty(userInfo.get("rongToken"))) {
            registerRong(userInfo, user, userId);
        }
        resultJson.putAll(userInfo);
        return resultJson;
    }

    /**
     * 获得用户信息
     *
     * @param userId
     * @param user
     * @return
     */
    private Map<String, String> getUserInfoFromRedis(long userId, UserInfo user) {
        Map<String, String> userInfo = redisAdapter.hashMGet(RedisKey.USER_INFO_
                + userId, "loginId", "userId", "userName", "headImg", "rongToken");
        if (userInfo == null || userInfo.isEmpty()
                || StringUtils.isEmpty(userInfo.get("loginId"))) {
            user = UserInfoHelper.getAndCacheUserInfo(redisAdapter, userInfoMapper, userId);
            if (user == null) {
                return new HashMap<String, String>();
            }
            userInfo = new HashMap<>();
            userInfo.put("rongId", user.getLoginId());
            userInfo.put("rongToken", user.getRongToken());
            userInfo.put("userName", user.getUserName());
            userInfo.put("headImg", user.getHeadImg());
            userInfo.put("userId", userId + "");
            userInfo.put("loginId", user.getLoginId());
        } else {
            userInfo.put("rongId", userInfo.get("loginId"));
        }
        return userInfo;
    }

    /**
     * 注册融云
     *
     * @param userInfo
     * @param user
     * @param userId
     */
    private void registerRong(Map<String, String> userInfo, UserInfo user, long userId) {

        try {
            //注册融云
            String token = RongCloudFacade.getToken(userInfo.get("loginId"), userInfo.get("userName"),
                    userInfo.get("headImg"), 1);
            user.setRongToken(token);
            if (StringUtils.isNotEmpty(token)) {
                user.setUserId(userId);
                int cnt = userInfoMapper.updateUserInfoById(user);
                if (cnt > 0) {
                    userInfo.put("rongToken", token);
                    redisAdapter.hashMSet(RedisKey.USER_INFO_ + userId, userInfo);
                }
            }

        } catch (Exception e) {
            LOGGER.error("注册融云token异常" + e.toString());
        }
    }

    /**
     * 返回传递过来的用户ID
     */
    @Override
    public String getUserLoginId(long userId) {
        Map<String, String> userInfo = redisAdapter.hashMGet(RedisKey.USER_INFO_
                + userId, "origin");
        String loginId = userInfo.get("loginId");
        if (StringUtils.isBlank(loginId)) {
            UserInfo user = UserInfoHelper.getAndCacheUserInfo(redisAdapter, userInfoMapper, userId);
            if (user == null) {
                return "";
            }
            loginId = user.getLoginId();
        }

        //uid格式， uid + _ + cx创新：1_cx 书城：1_s
        return loginId;
    }

    /**
     * {@inheritDoc}
     */
    public UserInfo checkIsExist(String uid) throws IWanviException {
        //1. 校验uid是否合规 2.获得该用户信息

        //1. 校验uid是否合规
        String[] uids = checkUid(uid);
        if (Origin.FROMCX.equals(Origin.getOrigin(uids[1]))) {
            try {
                //2.获得该用户信息
                UserInfo userInfo = getCxUser(uids[0], uid);//通过创新版获得用户详情，目前只有创新版用户
                return setUserIdFromRedis(userInfo, uid);//只有用户存在才走此方法
            } catch (Exception e) {
                LOGGER.info("此用户不存在！uid：" + uid, e);
                throw new IWanviException("此用户不存在！loginId：" + uid);
            }
        }
        return null;
    }

    /**
     * 从redis中获得用户的userId
     *
     * @param userInfo
     * @param loginId
     */
    private UserInfo setUserIdFromRedis(UserInfo userInfo, String loginId) {
        if (userInfo == null) {
            String userLogin = RedisKey.USER_LOGIN_ID_ + loginId;
            if (redisAdapter.existsKey(userLogin)) {
                String userId = redisAdapter.strGet(userLogin);
                userId = userId.replaceAll("\"", "");
                if (StringUtils.isNotBlank(userId)) {
                    userInfo = new UserInfo();
                    userInfo.setUserId(Long.parseLong(userId));
                }
            }
        }
        return userInfo;
    }

    /**
     * 检查uid的合法性
     *
     * @param uid
     * @return
     */
    private String[] checkUid(String uid) {
        if (StringUtils.isBlank(uid)) {
            throw new IWanviException("此用户不存在，uid为空！loginId：" + uid);
        }
        if (uid.contains("_")) {
            String[] uids = uid.split("_");
            uid = uid.split("_")[0];
            if (uids.length < 1) {
                throw new IWanviException("此用户不存在，uid格式不正确！loginId：" + uid);
            }
            if (uid.equals("-1")) {//-1_cx
                throw new IWanviException("此用户为模拟登陆，uid格式不正确！loginId：" + uid);
            }
            return uids;
        } else {
            throw new IWanviException("此用户不存在，uid不包含_！loginId：" + uid);
        }
    }

    /**
     * 从创新版获得用户信息的json字符串
     *
     * @param uid
     * @param loginId
     * @return
     */
    private UserInfo getCxUser(String uid, String loginId) {
        try {
            HttpURLConnection conn = HttpUtils.createGetHttpConnection(userInfoCxURL + uid, Constants.UTF8);
            SdkHttpResult result = HttpUtils.returnResult(conn);
            if (result.getHttpCode() == 200) {
                String strData = result.getResult();
                JSONObject userJson = JSONObject.parseObject(strData);
                if (userJson != null) {
                    String data = userJson.getString("data");
                    JSONObject json = JSONObject.parseObject(data);
                    UserInfo userInfo = jsonUserToLiveUser(json, loginId);
                    return userInfo;
                }
            }
        } catch (Exception e) {
            LOGGER.error("获取创新版用户信息失败." + e.getMessage());
            throw new IWanviException("获取创新版用户信息失败.");
        }
        return null;
    }

    /**
     * 解析创新版用户json信息
     *
     * @param json
     * @param
     * @return
     */
    private UserInfo jsonUserToLiveUser(JSONObject json, String loginId) {
        UserInfo tmpUser = getUserInfo(loginId);
        if (tmpUser != null) {
            return synchroExistUser(json, tmpUser);
        } else {
            return synchroNotExistUser(json, loginId);
        }

    }

    /**
     * 同步用户信息，如果存在返回该用户信息，不能存在返回null
     *
     * @param json
     * @param user
     * @return
     */
    private UserInfo synchroExistUser(JSONObject json, UserInfo user) {
        UserInfo userInfo = new UserInfo();
        String vir = json.getString("userPoints");
        if (!user.getVirtualCurrency().toString().equals(vir) && RegexUtils.isNum(vir)) {
            userInfo.setVirtualCurrency(Long.parseLong(vir));
        }
        String userName = json.getString("userName");
        if (!user.getUserName().equals(userName)) {
            userInfo.setUserName(userName);
        }
        String headImg = json.getString("headImg");
        if (!user.getHeadImg().equals(headImg)) {
            userInfo.setHeadImg(headImg);
        }
        String sexStr = json.getString("sex");
        if (RegexUtils.isNum(sexStr)) {//创新版定义 0-未填写, 1-男, 2-女 //直播端定义按统一定义方式  性别 0女 1男 2未知
            int sex = Integer.parseInt(sexStr);
            if (user.getSex() != null) {
                setZBSex(user, userInfo, sex);
            } else {
                userInfo.setSex(getZBSex(sex));
            }

        }
        String strDate = json.getString("birthday");
        try {
            if (StringUtils.isNotBlank(strDate)) {
                Date birth = DateUtils.parseDate(strDate, new String[]{"yyyy-MM-dd"});
                if (user.getBirthday() == null) {
                    userInfo.setBirthday(birth);
                    userInfo.setZodiac(DateTools.getZodiacByDate(birth));
                } else if (birth.compareTo(user.getBirthday()) != 0) {
                    userInfo.setBirthday(birth);
                    userInfo.setZodiac(DateTools.getZodiacByDate(birth));
                }
            }
        } catch (ParseException e) {
            e.printStackTrace();
            LOGGER.error("获取创新版用户生日失败." + e.getMessage());
        }
        if (StringUtils.isBlank(user.getRongToken())) {//注册融云
            try {
                String token = RongCloudFacade.getToken(user.getLoginId(), user.getUserName(),
                        user.getHeadImg(), 1);
                userInfo.setRongToken(token);
            } catch (Exception e) {
                LOGGER.error("获得融云token异常：", e);
            }
        }
        userInfo.setUserId(user.getUserId());
        userInfo.setLoginId(user.getLoginId());
        userInfo.setOrigin(user.getOrigin());//0创新版 1中文书城 2直接注册  当origin为2时此用户用创新版账户登录过，所以不同步用户信息
        userInfo.setVersionOptimizedLock(user.getVersionOptimizedLock());
        return userInfo;
    }

    /**
     * 同步不存的用户，如果使用此方法说明用户不在
     *
     * @param json
     * @param loginId
     * @return
     */
    private UserInfo synchroNotExistUser(JSONObject json, String loginId) {
        UserInfo userInfo = new UserInfo();
        userInfo.setLoginId(loginId);
        String userPoints = json.getString("userPoints");
        if (StringUtils.isNotEmpty(userPoints)) {
            userInfo.setVirtualCurrency(Long.valueOf(userPoints));
        } else {
            userInfo.setVirtualCurrency(0L);
        }
        userInfo.setUserName(json.getString("userName"));
        userInfo.setHeadImg(json.getString("headImg"));
        if (StringUtils.isNotEmpty(json.getString("sex"))) {
            int zbSex = getZBSex(Integer.parseInt(json.getString("sex")));
            userInfo.setSex(zbSex);
        } else {
            userInfo.setSex(2);
        }
        String strDate = json.getString("birthday");
        try {
            if (StringUtils.isNotEmpty(strDate)) {
                Date birth = DateUtils.parseDate(strDate, new String[]{"yyyy-MM-dd"});
                userInfo.setBirthday(birth);
            }
        } catch (ParseException e) {
            e.printStackTrace();
            LOGGER.error("获取创新版用户生日失败." + e.getMessage());
        }
        return userInfo;
    }

    /**
     * 设置直播用户性别
     *
     * @param user
     * @param userInfo
     * @param sex
     */
    private void setZBSex(UserInfo user, UserInfo userInfo, int sex) {
        int zbSex = user.getSex();
        if (sex == 0 && zbSex != 2) {
            userInfo.setSex(2);
        } else if (sex == 2 && zbSex != 0) {
            userInfo.setSex(0);
        } else if (sex == 1 && zbSex != 1) {
            userInfo.setSex(1);
        }
    }

    private int getZBSex(int sex) {
        if (sex == 0) {
            sex = 2;
        } else if (sex == 2) {
            sex = 0;
        }
        return sex;
    }

    public long synchroVirtualCurrency(long userId, String loginId) throws IWanviException {
        try {
            //1. 校验uid是否合规
            String[] uids = checkUid(loginId);
            if (Origin.FROMCX.equals(Origin.getOrigin(uids[1]))) {
                return updateFromCx(userId, uids[0], loginId);
            }
        } catch (IWanviException e) {
            LOGGER.error("同步用户虚拟货币失败，loginId：" + loginId);
        }
        return 0L;
    }

    /**
     * 如果该用户的信息存在就返回该用户的全部信息，不能存返回null。
     *
     * @param loginId
     * @return
     */
    private UserInfo getUserInfo(String loginId) {
        UserInfo userInfo = null;
        String userLogin = RedisKey.USER_LOGIN_ID_ + loginId;
        if (redisAdapter.existsKey(userLogin)) {
            String userId = redisAdapter.strGet(userLogin);
            userId = userId.replaceAll("\"", "");
            String userKey = RedisKey.USER_INFO_ + userId;
            if (redisAdapter.existsKey(userKey)) {
                Map<String, String> userMap = redisAdapter.hashGetAll(userKey);
                userInfo = new UserInfo();
                try {
                    userInfo.doStringMapToValue(userMap);
                    return userInfo;
                } catch (ParseException e) {
                    LOGGER.error("解析用户信息异常userKey：" + userKey);
                }
            } else {
                userInfo = userInfoMapper.findAllInfoByLoginId(loginId);
            }
        } else {
            userInfo = userInfoMapper.findAllInfoByLoginId(loginId);
        }

        if (userInfo != null && userInfo.getUserId() != null && userInfo.getOrigin() != null) {
            if (userInfo.getOrigin() == 0) { // 创新版用户才同步昵称
                String userInfoKey = RedisKey.USER_INFO_ + userInfo.getUserId();
                redisAdapter.hashMSet(userInfoKey, userInfo.putFieldValueToMap());
                redisAdapter.expireKey(userInfoKey, RedisExpireTime.EXPIRE_DAY_7);
                redisAdapter.strSetByNormal(userLogin, userInfo.getUserId().toString());
                redisAdapter.expireKey(userLogin, RedisExpireTime.EXPIRE_DAY_7);
            }
            userInfo.setLoginId(loginId);
        }
        return userInfo;
    }

    /**
     * 同步创新版的积分信息
     *
     * @param uid
     */
    private long updateFromCx(long userId, String uid, String loginId) {
        try {
            //2.获得该用户信息
            UserInfo userInfo = getCxUser(uid, loginId);//通过创新版获得用户详情，目前只有创新版用户
            return getUserVirtualCurrency(userId, userInfo, loginId);
        } catch (Exception e) {
            LOGGER.info("此用户不存在！uid：" + uid);
            throw new IWanviException("此用户不存在！loginId：" + uid);
        }
    }

    /**
     * 获得用户的积分信息
     *
     * @param userInfo
     * @param loginId
     * @return
     */
    private long getUserVirtualCurrency(long userId, UserInfo userInfo, String loginId) {
        String userInfoKey = RedisKey.USER_INFO_ + userId;
        int cnt = 0;
        if (userInfo != null) {
            cnt = userInfoMapper.updateUserInfoByLoginId(userInfo);
        } else {
            userInfo = new UserInfo();
        }

        if (cnt > 0) {//同步redis
            if (redisAdapter.existsKey(userInfoKey)) {
                if (!redisAdapter.hashMSet(userInfoKey, userInfo.putFieldValueToMap()).equals("OK")) {
                    redisAdapter.delKeys(userInfoKey);
                }
            }
        }
        try {
            userInfo.doStringMapToValue(redisAdapter.hashMGet(userInfoKey, "virtualCurrency"));
        } catch (ParseException e) {
            LOGGER.error("同步用户积分错误：" + loginId + "， 错误信息为：" + e.getMessage());
        }

        if (userInfo.getVirtualCurrency() == null) {
            userInfo = UserInfoHelper.getAndCacheUserInfo(redisAdapter, userInfoMapper, userId);
            if (userInfo == null) {
                return 0L;
            }
        }
        return userInfo.getVirtualCurrency();
    }

    @Override
    public Map<String, Object> mute(Long videoId, Long userId, Long anchorId, String loginId, int duration) {
        //1.超管权利最大 2.超管禁言某用户后为永久禁言 3.房管禁言为临时禁言 4.神殿骑士不能被禁言
    	UserInfo member = getUserInfo(loginId);

        Map<String, String> userInfo = UserInfoHelper.getAndCacheUserMap(redisAdapter, userInfoMapper, userId);
        int acctType = 0;
        if(userInfo != null) {
            acctType = MapUtils.getIntValue(userInfo, "acctType");
        }
        // 神殿骑士不能被禁言
		Integer lv = RoleNobleHelper.userRoleNobleLevel(redisAdapter, roleInfoMapper, member.getUserId());
        if(acctType != 1) {
            if(lv == 6){
                Map<String, Object> result = new HashMap<>();
                result.put("result", 2);
                return result;
            }
        }

        Map<String, Object> data = userIsInBlackList(member, userId);//是否是在禁言列表里，如果是就是超管禁言
        if (data != null && !data.isEmpty()) {
            return data;
        }
        if (!LiveAdminHelper.isAdmin(redisAdapter, adminMapper, anchorId, userId)) {//普通用户是否为房管
            throw new IWanviException("房管账号不存在!");
        }
        String key = RedisKey.BLACKLIST_ + anchorId + Constants.UNDERLINE + member.getUserId();
        if (redisAdapter.existsKey(key)) {
            redisAdapter.expireKey(key, duration);
            data.put("result", 1);
            return data;
        } else {
            redisAdapter.strSetEx(key, key, duration);
            data.put("result", 1);
            return data;
        }
    }

    /**
     * @param member 要被禁言着
     * @param userId 禁言者
     * @return
     */
    private Map<String, Object> userIsInBlackList(UserInfo member, Long userId) {
        Map<String, Object> data = new HashMap<>();

        if (member == null) {
            data.put("result", 1);
            return data;
        }
        if (member.getAcctType() == Constants._1) {//超管不能被禁言
            throw new IWanviException("超管不能被禁言!");
        }

        UserInfo user = null;//发起禁言者
    	String userKey = RedisKey.USER_INFO_ + userId;
    	if (redisAdapter.existsKey(userKey)) {
    		user = UserInfoHelper.getUserInfoFromCache(redisAdapter, userId, "acctType");
    	} else {
    		user = UserInfoHelper.getAndCacheUserInfo(redisAdapter, userInfoMapper, userId);
    	}
    	if (user.getAcctType() == Constants._1) {//永久禁言 acctType == 1 是超管
    		if (redisAdapter.existsKey(RedisKey.BLACK_LIST_FOREVER_ + member.getUserId().toString())) {
                data.put("result", 1);
                return data;
            } else {
                List<Map<String, Object>> list = blackListMapper.findBlackStatusByUserIdAndSuperId(member.getUserId(), userId);
                Object status = null;
                if (list != null && !list.isEmpty()) {
                    status = list.get(0).get("status");
                }
                if (status != null) {
                    Integer s = (Integer) status;
                    if (s.intValue() == 0) {
                        redisAdapter.strSetexByNormal(RedisKey.BLACK_LIST_FOREVER_ + member.getUserId().toString(), RedisExpireTime.EXPIRE_DAY_30, "0");
                        data.put("result", 1);
                        return data;
                    } else {
                        //update
                    }
                }
                BlackList black = new BlackList();
                black.setSuperAdminId(userId);
                black.setUserId(member.getUserId());
                black.setCreateTime(new Date());
                int cnt = blackListMapper.insertBlack(black);
                if (cnt > 0) {
                    redisAdapter.strSetexByNormal(RedisKey.BLACK_LIST_FOREVER_ + member.getUserId().toString(), RedisExpireTime.EXPIRE_DAY_30, "0");
                    data.put("result", 1);
                    return data;
                }
                data.put("result", cnt);
                return data;
            }
        }
        return data;
    }

    /**
     * 根据用户userId获取用户信息
     *
     * @param userId
     * @return
     */
    @Override
    public Map<String, Object> getUserInfoByUserId(String userId,String version) {
        Map<String, String> userInfo = new HashMap<String, String>();
        //获得用户的缓存信息
        String userKey = RedisKey.USER_INFO_ + userId;
        if (redisAdapter.existsKey(userKey)) {
            userInfo = redisAdapter.hashMGet(userKey, "userId", "headImg", "userName",
                    "sex", "birthday", "zodiac","account");
            try{
                String birthday = userInfo.get("birthday");
                Date date = DateUtil.parseStringDate(birthday,"yyyy-MM-dd");
                userInfo.put("zodiac", DateTools.getZodiacByDate(date));
                userInfo.put("birthday", DateFormatUtils.format(date, "yyyy-MM-dd"));
            }catch (Exception e){
                LOGGER.debug("时间转化失败");
                userInfo.put("zodiac", "双鱼座");
                userInfo.put("birthday", "");
            }

        }
        if (userInfo == null || userInfo.isEmpty()) {
            UserInfo user = UserInfoHelper.getAndCacheUserInfo(redisAdapter, userInfoMapper, Long.parseLong(userId));
            if (user == null) {
                return null;
            }
            user.setZodiac(DateTools.getZodiacByDate(user.getBirthday()));
            userInfo = user.putFieldValueToStringMap();
        }

        //获得用户的年龄和全站排行
        UserRankHelper.setUserInfo(redisAdapter,
                contribMapper, userInfo);

        Map<String, Object> resultJson = new HashMap<>();

        Map<String, Object> dataMap = new HashMap<>();
        dataMap.putAll(userInfo);

        // 获取绑定信息
        OauthInfo qqOauth = UserAccountUtils.getOauthByTypeAndId(Long.valueOf(userId),
                WebConstants.OAUTH_QQ, redisAdapter, oauthInfoMapper);
        OauthInfo WeChatOauth = UserAccountUtils.getOauthByTypeAndId(Long.valueOf(userId),
                WebConstants.OAUTH_WECHAT, redisAdapter, oauthInfoMapper);
        Map<String, Object> oauth = new HashMap<>();
        oauth.put("QQ", qqOauth == null ? 0 : 1);
        oauth.put("WeChat", WeChatOauth == null ? 0 : 1);
        dataMap.put("oauth", oauth);

        //获取关注数
        try {
            int followNum = followService.getFollowNumber(Long.parseLong(userId));
            dataMap.put("followNum",followNum);

            //获取版本，判断是否为新版本isNewVersion
            String versionKey = RedisKey.independentUpdownKeys.INDEPENDENT_CURRENT_VERSION;
            if(redisAdapter.existsKey(versionKey)){
                String versionReids = redisAdapter.hashGet(versionKey, "version");

                if (!versionReids.equals(version)) {
                    dataMap.put("isNewVersion",1);
                } else {
                    dataMap.put("isNewVersion",0);
                }
            }else{
                dataMap.put("isNewVersion",0);
            }

            dataMap.put("diamond",myAcctInfoService.getUserAcctInfo(Long.parseLong(userId), null).get("diamond"));
        } catch (Exception e) {
            e.printStackTrace();
        }

        resultJson.put("userInfo", dataMap);


        return resultJson;
    }

    /**
     * 更新用户信息
     *
     * @param request
     * @return
     */
    @Override
    public Map<String, Object> updateUserInfoByUserId(HttpServletRequest request) {
        Map<String, Object> resultJson = new HashMap<String, Object>();
        //根据用户userId判断是否存在该用户
        String userId = request.getParameter("userId");
        String userName = request.getParameter("userName");
        String uploadType = request.getParameter("uploadType");
        String sex = request.getParameter("sex");
        String birthday = request.getParameter("birthday");
        String pwd = request.getParameter("password");
        String token = request.getParameter("token");
        String oldPasswd = request.getParameter("oldPasswd");

        UserInfo userInfo = userInfoMapper.findById(Long.parseLong(userId));

        if (userInfo != null) {
            //判断头像
            if (StringUtils.isNotBlank(uploadType)) {
                Map<String, Object> upload = fileUploadService.uploadAndSave(request);
                if (upload == null || upload.isEmpty()) {
                    resultJson.put("result", "1006");
                    return resultJson;
                } else {
                    JSONObject imgUpload = JsonUtils.toValueOfJsonString(upload);
                    userInfo.setHeadImg(imgUpload.get("imgUrl") == null ? null : imgUpload.get("imgUrl").toString());
                }
            }

            //判断用户昵称是否相同
            if (StringUtils.isNotBlank(userName)) {
                //判断昵称是否已存在
                int num = userInfoMapper.findUserByNickname(userName);
                if (num > 0) {
                    resultJson.put("result", "2002");
                    return resultJson;
                } else {
            		String loginId = userInfo.getLoginId();
            		Integer origin = userInfo.getOrigin();
                    if ((Constants.USER_INFO_ORIGIN_0 + "").equals(origin.toString())) {//独立版用户不需要同步昵称了
            			loginId = loginId.replace("_cx", "");
            			String uri = this.modifyName;
            			Map<String, String> paramsMap = new HashMap<String, String>();
            			paramsMap.put("uid", loginId);
            			paramsMap.put("nickname", userName);
            			uri = StringTools.replace(paramsMap, uri);
            			Integer code = modifyNameByCx(uri, userInfo.getUserId());
            			if (code <= 0) {
                            resultJson.put("result", "2002");
                            return resultJson;
            			}
            			userInfo.setUserName(userName);
            		} else if ((Constants.USER_INFO_ORIGIN_2 + "").equals(origin.toString())) {
                        userInfo.setUserName(userName);
            		}
                }
            }
            //判断性别
            if (StringUtils.isNotBlank(sex)) {
                userInfo.setSex(Integer.parseInt(sex));
            }

            //判断生日
            if (StringUtils.isNotBlank(birthday)) {
                //转化为日期
                Date date = null;
                try {
                    date = DateUtil.parseStringDate(birthday, "yyyy-MM-dd");
                } catch (ParseException e) {
                    LOGGER.error("时间转化错误--->>>" + e);
                }
                userInfo.setZodiac(DateTools.getZodiacByDate(date));
                userInfo.setBirthday(date);
            }

            //判断密码
            if (StringUtils.isNotBlank(pwd) && StringUtils.isNotBlank(token)) {
                //判断token是否有效，有效证明是本人可以修改
                String ticket = UserAccountUtils.getTokenByUserId(Long.parseLong(userId), redisAdapter, userInfoMapper);
                if (ticket.equals(token)) {
                    oldPasswd = StrMD5.getInstance().getStringMD5(oldPasswd);
                    if (!userInfo.getPassword().equals(oldPasswd)){
                        resultJson.put("result", "2004");
                        return resultJson;
                    }
                    String password = StrMD5.getInstance().getStringMD5(pwd);
                    userInfo.setPassword(password);
                }else {
                    resultJson.put("result", "2003");
                    return resultJson;
                }
            }
            userInfoMapper.updateUserInfoById(userInfo);
            //删除缓存
            redisAdapter.delKeys(RedisKey.USER_INFO_ + userId);

            resultJson.put("result", "0");
        } else {
            resultJson.put("result", "1002");
        }
        return resultJson;
    }

	private Integer modifyNameByCx(String uri, Long userId) {

		try {
			HttpURLConnection conn = HttpUtils.createGetHttpConnection(uri,
					Constants.UTF8);
			SdkHttpResult shr = HttpUtils.returnResult(conn);
			if (shr.getHttpCode() == 200) {
				JSONObject json = JSONObject.parseObject(shr.getResult());
				Object o = json.get("code");
				if (o instanceof Integer) {
					Integer code = (Integer) o;
					if (code == 0) {// 1 失败 0成功
						return code >= 0 ? 1 : 0;
					}
				}
			}

		} catch (IOException e) {
			LOGGER.error("生成修改昵称URL失败", e);
		} catch (Exception e) {
			LOGGER.error("修改昵称失败", e);
		}
		return 0;
	}

}
