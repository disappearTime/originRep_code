package com.chineseall.iwanvi.wwlive.web.follow.service.impl;

import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URLEncoder;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.chineseall.iwanvi.wwlive.web.follow.service.UserService;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.chineseall.iwanvi.wwlive.common.cache.RedisClientAdapter;
import com.chineseall.iwanvi.wwlive.common.cache.RedisExpireTime;
import com.chineseall.iwanvi.wwlive.common.cache.RedisKey;
import com.chineseall.iwanvi.wwlive.common.constants.Constants;
import com.chineseall.iwanvi.wwlive.common.exception.IWanviException;
import com.chineseall.iwanvi.wwlive.common.external.kscloud.KSCloudFacade;
import com.chineseall.iwanvi.wwlive.common.external.rongcloud.RongCloudFacade;
import com.chineseall.iwanvi.wwlive.common.external.rongcloud.domain.SdkHttpResult;
import com.chineseall.iwanvi.wwlive.common.tools.HttpUtils;
import com.chineseall.iwanvi.wwlive.dao.wwlive.AnchorMapper;
import com.chineseall.iwanvi.wwlive.dao.wwlive.BlackListMapper;
import com.chineseall.iwanvi.wwlive.dao.wwlive.LiveVideoInfoMapper;
import com.chineseall.iwanvi.wwlive.dao.wwlive.UserInfoMapper;
import com.chineseall.iwanvi.wwlive.domain.wwlive.Anchor;
import com.chineseall.iwanvi.wwlive.domain.wwlive.UserInfo;
import com.chineseall.iwanvi.wwlive.web.common.enums.Origin;
import com.chineseall.iwanvi.wwlive.web.common.helper.AnchorInfoHelper;
import com.chineseall.iwanvi.wwlive.web.common.helper.BlackListHelper;
import com.chineseall.iwanvi.wwlive.web.common.helper.UserInfoHelper;
import com.chineseall.iwanvi.wwlive.web.common.util.DateTools;
import com.chineseall.iwanvi.wwlive.web.common.util.RegexUtils;
import com.chineseall.iwanvi.wwlive.web.event.service.MedalHonorService;
import com.chineseall.iwanvi.wwlive.web.video.vo.VideoJsVo;
import com.service.FollowAnchorService;
import com.service.impl.FollowAnchorServiceImpl;
import com.zw.zcf.util.MapUtils;

@Service("followUserService")
public class UserServiceImpl implements UserService {

    private static FollowAnchorService followService = new FollowAnchorServiceImpl();

    private static final int RESULT_SUCCESS = 1;
    private static final int RESULT_FAIL = 0;

    private Logger logger = Logger.getLogger(this.getClass());

    @Autowired
    private RedisClientAdapter redisAdapter;

    @Autowired
    private LiveVideoInfoMapper videoMapper;

    @Autowired
    private AnchorMapper anchorMapper;

    @Autowired
    private UserInfoMapper userMapper;

    @Autowired
    private BlackListMapper blackListMapper;

    @Value("${userinfo.cx.uri}")
    private String userInfoCxURL;
    
    @Autowired
    private MedalHonorService medalHonorService;

    @Override
    public int getFollowedCnt(Long userId) {
        try {
        	if (userId == null) {
        		return 0;
        	}
            return followService.getFollowNumber(userId);
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * 推荐6位主播, 优先显示直播中的, 其次是近7天收入最高的
     */
    @Override
    public List<Map<String, Object>> getRecommend(String cnid) {

        List<Map<String, Object>> recommends = new ArrayList<>();

        String liveKey = null;
        String channel = redisAdapter.strGet("qudao");// 灰度渠道控制
        if(StringUtils.isNotBlank(channel) && channel.equals(cnid)){
            liveKey = RedisKey.VideoGrayKeys.LIVING_VIDEOS_;
        } else{
            liveKey = RedisKey.VideoKeys.LIVING_VIDEOS_;
        }
        
        Set<String> lvingAnchorIds = redisAdapter.zsetRevRange(liveKey, 0, -1);
        
        try {
            List<Map<String, Object>> ranchors = followService.queryRecommendAnchor();
            logger.info("推荐日志--> 推荐的主播: " + ranchors);
            
            int anchorCnt = 0;// 保证推荐页是6位主播
            if(ranchors != null && ranchors.size() > 0){
                for(Map<String, Object> anchor:ranchors){
                    
                    if(anchorCnt == 6){
                        break;
                    }
                    
                    Long anchorId = MapUtils.getLong(anchor, "anchorId", 0L);
                    if (redisAdapter.setIsMember(RedisKey.DINGZHI_ANCHOR, anchorId.toString())){
                        // 排除定制版的主播
                        continue;
                    }
                    List<String> medals = medalHonorService.getGoddessMedal(anchorId); // 获取勋章
                    
                    String anchorKey = RedisKey.ANCHOR_INFO_ + anchorId;
                    if(redisAdapter.existsKey(anchorKey)){
                        String acctType = redisAdapter.hashGet(anchorKey, "acctType");
                        if("1".equals(acctType)){// acctType = 1表示测试用户, 不出现在推荐列表中
                            logger.info("推荐日志--> anchorId = " + anchorId + "为测试账号");
                            continue;
                        }
                    } else{
                        Anchor anchorInfo = AnchorInfoHelper.getAndCacheAnchorInfo(redisAdapter, anchorMapper, anchorId);
                        if(anchorInfo != null && anchorInfo.getAcctType() != 1){
                            logger.info("推荐日志--> anchorId = " + anchorId + "为测试账号");
                            continue;
                        }
                    }
                    
                    if(lvingAnchorIds.contains(anchorId.toString())){
                        String livingKey = RedisKey.ANCHOR_LIVING_VIDEO_ + anchorId;
                        String liveId = redisAdapter.strGet(livingKey);// 主播正在直播的视频id
                        Map<String, Object> rcmdInfo = 
                                videoMapper.getRcmdInfoById(liveId == null ? 0L : Long.valueOf(liveId));
                        String viewers = redisAdapter.hashGet(RedisKey.LIVE_VIDEO_INFO_ + rcmdInfo.get("videoId"), "viewers");
                        rcmdInfo.put("viewers", StringUtils.isBlank(viewers) ? 0 : viewers);
                        rcmdInfo.put("isLive", 1);
                        rcmdInfo.put("medals", medals);
                        recommends.add(rcmdInfo);
                        
                        anchorCnt++;
                    } else{
                        Map<String, Object> rcmdInfo = anchorMapper.getRcmdAnchorById(anchorId);
                        int followerCnt = followService.getFansNumber(anchorId);// 获取粉丝数
                        rcmdInfo.put("followerCnt", followerCnt);
                        rcmdInfo.put("isLive", 0);
                        rcmdInfo.put("medals", medals);                        
                        recommends.add(rcmdInfo);
                        
                        anchorCnt++;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return recommends;     
        
        
        
        /*List<String> liveIds = new ArrayList<>();
        for (String anchorId : lvingAnchorIds) {
            String livingKey = RedisKey.ANCHOR_LIVING_VIDEO_ + anchorId;
            liveIds.add(redisAdapter.strGet(livingKey));
        }
        
        int liveCnt = liveIds.size() - 1;

        if (liveCnt >= RECOMMEND_TOTAL) {// 直播数多于6
            List<Map<String, Object>> lives = getLives(liveIds);
            recommends.put("lives", lives);
            return recommends;
        }

        String deadline = DateTools.getAWeekAgoDate(Calendar.getInstance());// 收入统计近7天的

        if (liveCnt > 0 && liveCnt < 6) {// 有直播, 但少于6
            // 获取正在直播的主播id, 用于在收入排序中排除
            List<Long> anchorIds = videoMapper.getAnchorIdByVideoId(liveIds);

            // 查询正在直播的视频信息
            List<Map<String, Object>> lives = getLives(liveIds);
            recommends.put("lives", lives);

            // 查询收入最高的主播信息
            List<Map<String, Object>> anchors = videoMapper.getMostIncomeAnchorLt6(anchorIds, deadline,
                    RECOMMEND_TOTAL - liveCnt);
            for (Map<String, Object> anchor : anchors) {
                Long anchorId = (Long) anchor.get("anchorId");
                int followerCnt = 0;
                try {
                    followerCnt = followService.getFansNumber(anchorId);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                anchor.put("followerCnt", followerCnt);
            }
            recommends.put("anchors", anchors);
            return recommends;
        }

        // 无直播
        List<Map<String, Object>> anchors = videoMapper.getMostIncomeAnchorEq6(deadline, RECOMMEND_TOTAL);
        for (Map<String, Object> anchor : anchors) {
            Long anchorId = (Long) anchor.get("anchorId");
            int followerCnt = 0;
            try {
                followerCnt = followService.getFansNumber(anchorId);
            } catch (Exception e) {
                e.printStackTrace();
            }
            anchor.put("followerCnt", followerCnt);
        }
        recommends.put("anchors", anchors);
        return recommends;*/
    }

    @Override
    public int follow(Long userId, Long anchorId) throws Exception {
        // 用户不存在时不能关注
        UserInfo user = UserInfoHelper.getUserInfoFromCache(redisAdapter, userId);
        if (user == null){
            user = UserInfoHelper.getAndCacheUserInfo(redisAdapter, userMapper, userId);
            if(user == null) {
                return RESULT_FAIL;
            }
        }

        // 0 失败 1成功 2禁言
        if (BlackListHelper.isOnBlackList(blackListMapper, redisAdapter, userId)) {
            return 2;
        }
        boolean success = followService.addFollow(userId, anchorId);
        return success ? RESULT_SUCCESS : RESULT_FAIL;
    }

    @Override
    public int unfollow(Long userId, Long anchorId) throws Exception {
        boolean success = followService.delFollow(userId, anchorId);
        return success ? RESULT_SUCCESS : RESULT_FAIL;
    }

    @Override
    public Map<String, Object> getFollowList(Long userId, int pageNo, long timestamp, String cnid) {

        Map<String, Object> followMap = new HashMap<>();

        List<Map<String, Object>> followList = new ArrayList<>();

        try {            
            String liveKey = null;
            String channel = redisAdapter.strGet("qudao");// 灰度渠道控制
            if(StringUtils.isNotBlank(channel) && channel.equals(cnid)){
                liveKey = RedisKey.VideoGrayKeys.LIVING_VIDEOS_;
            } else{
                liveKey = RedisKey.VideoKeys.LIVING_VIDEOS_;
            }
            Set<String> lvingAnchorIds = redisAdapter.zsetRevRange(liveKey, 0, -1);// 正在直播的主播id

            // 获取第一页关注列表
            List<Map<String, Object>> follows = followService.queryFollow(userId, pageNo, timestamp);
            
            for (Map<String, Object> follow : follows) {
                Long anchorId = MapUtils.getLongValue(follow, "anchorId", 0);// ((Integer)
                                                                             // follow.get("anchorId")).longValue();
                List<String> medals = medalHonorService.getGoddessMedal(anchorId); // 获取勋章
                
                if (lvingAnchorIds.contains(anchorId.toString())) {
                    // 获取该主播的直播信息
                    Map<String, Object> live = videoMapper.getLiveByAnchorId(anchorId);
                    
                    if(live == null){// 没有查询到直播信息, 说明缓存和数据库数据不一致, 该主播并没有在直播
                        // 获取该主播信息
                        Map<String, Object> anchorInfo = anchorMapper.getFollowInfoById(anchorId);
                        // 获取粉丝数
                        int followerCnt = followService.getFansNumber(anchorId);
                        anchorInfo.put("followerCnt", followerCnt);
                        anchorInfo.put("isLive", 0);// isLive = 1, 正在直播; isLive = 0, 没在直播
                        anchorInfo.put("medals", medals);
                        followList.add(anchorInfo); 
                        continue;
                    }
                    
                    // 获取观众数
                    String viewers = redisAdapter.hashGet(RedisKey.LIVE_VIDEO_INFO_ + live.get("videoId"), "viewers");
                    live.put("viewers", StringUtils.isBlank(viewers) ? 0 : viewers);

                    live.put("isLive", 1);// isLive = 1, 正在直播; isLive = 0, 没在直播
                    live.put("medals", medals);
                    //将信息封装，加快直播的展示
                    VideoJsVo js=new VideoJsVo();
                    String stream_name= org.apache.commons.collections.MapUtils.getString(live,"chatroomId","");
                    String[] liuArgs= KSCloudFacade.getRtmpURLs(stream_name);
                    js.setStandURL(liuArgs!=null?liuArgs[0]:"");
                    js.setHeighURL(liuArgs!=null?liuArgs[1]:"");
                    js.setFullHeighURL(liuArgs!=null?liuArgs[2]:"");
                    try {
                        live.put("ext", URLEncoder.encode(JSON.toJSONString(js),"utf-8"));
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                    followList.add(live);
                } else {
                    // 获取该主播信息
                    Map<String, Object> anchorInfo = anchorMapper.getFollowInfoById(anchorId);
                    // 获取粉丝数
                    int followerCnt = followService.getFansNumber(anchorId);
                    anchorInfo.put("followerCnt", followerCnt);
                    anchorInfo.put("isLive", 0);// isLive = 1, 正在直播; isLive = 0, 没在直播
                    anchorInfo.put("medals", medals);
                    followList.add(anchorInfo);
                }
            }
            followMap.put("follows", followList);
            /*
             * followMap.put("lives", lives); followMap.put("anchors", anchors);
             */
            // 将最后一条关注记录添加到map中, 方便在页面上返回timestamp使用
            followMap.put("lastItem", (follows!=null && follows.size()!=0)?follows.get(follows.size() - 1):0);
            return followMap;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return followMap;
    }

    @Override
    public int isFollower(Long anchorId, Long userId) {
        try {
            return followService.isFollow(userId, anchorId) ? 1 : 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    @Override
    public Map<String, Object> getFollowTop3(Long userId, String cnid) {
        Map<String, Object> followMap = new HashMap<>();

        List<Map<String, Object>> followList = new ArrayList<>();

        try {
            String liveKey = null;
            String channel = redisAdapter.strGet("qudao");// 灰度渠道控制
            if(StringUtils.isNotBlank(channel) && channel.equals(cnid)){
                liveKey = RedisKey.VideoGrayKeys.LIVING_VIDEOS_;
            } else{
                liveKey = RedisKey.VideoKeys.LIVING_VIDEOS_;
            }
            
            Set<String> lvingAnchorIds = redisAdapter.zsetRevRange(liveKey, 0, -1);

            List<Map<String, Object>> follows = followService.queryFollow(userId, 1, 0);
            int i = 0;
            for (Map<String, Object> follow : follows) {
                if (i == 3) {
                    break;// 只取前三位主播
                }

                Long anchorId = MapUtils.getLongValue(follow, "anchorId", 0);
                if (lvingAnchorIds.contains(anchorId.toString())) {
                    // 获取该主播的直播信息
                    Map<String, Object> live = videoMapper.getLiveByAnchorId(anchorId);
                    
                    if(live == null){
                        Map<String, Object> anchorInfo = anchorMapper.getFollowInfoById(anchorId);
                        anchorInfo.put("isLive", 0);
                        followList.add(anchorInfo);
                        continue;
                    }
                    
                    // 获取观众数
                    /*
                     * String viewers =
                     * redisAdapter.hashGet(RedisKey.LIVE_VIDEO_INFO_ +
                     * live.get("videoId"), "viewers"); live.put("viewers",
                     * StringUtils.isBlank(viewers) ? 0 : viewers);
                     */

                    live.put("isLive", 1);
                    followList.add(live);
                } else {
                    // 获取该主播信息
                    Map<String, Object> anchorInfo = anchorMapper.getFollowInfoById(anchorId);
                    // 获取粉丝数
                    /*
                     * int followerCnt = followService.getFansNumber(anchorId);
                     * anchorInfo.put("followerCnt", followerCnt);
                     */

                    anchorInfo.put("isLive", 0);
                    followList.add(anchorInfo);
                }

                i++;
            }

            followMap.put("followTop3", followList);
            followMap.put("result", RESULT_SUCCESS);
            return followMap;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return followMap;
    }

    @Override
    public Long getUserIdByLogin(String loginId) {

        // 判断用户是否存在, 如果存在直接返回userId, 不存在创建用户并返回userId
        UserInfo userInfo = UserInfoHelper.getAndCacheUserInfoByLoginId(redisAdapter, userMapper, loginId);
        if (userInfo != null) {
            return userInfo.getUserId();
        } else {// 注册用户            
            userInfo = checkIsExist(loginId);           
            if(userInfo != null && !userInfo.putFieldValueToMap().isEmpty()){
                fromCxUser(userInfo);
                setUserInfo(userInfo);
                
                userMapper.insertUserInfo(userInfo);
                setUserInfoRedisCache(userInfo);
                return userInfo.getUserId();
            }
            return null;
        }
        
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
            }catch (Exception e) {
                logger.info("此用户不存在！uid：" + uid);
                throw new IWanviException("此用户不存在！loginId：" + uid);
            }
        }
        return null;
    }
    
    /**
     * 从redis中获得用户的userId
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
            logger.error("获取创新版用户信息失败." + e.getMessage());
            throw new IWanviException("获取创新版用户信息失败.");
        }
        return null;
    }
    
    /**
     * 解析创新版用户json信息
     * @param json
     * @param uid
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
     * 如果该用户的信息存在就返回该用户的全部信息，不能存返回null。
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
                    logger.error("解析用户信息异常userKey：" + userKey);
                }
            }else {
                userInfo = userMapper.findAllInfoByLoginId(loginId);
            }
        } else {
            userInfo = userMapper.findAllInfoByLoginId(loginId);
        }
        
        if (userInfo != null && userInfo.getUserId() != null) {
            String userInfoKey = RedisKey.USER_INFO_ + userInfo.getUserId();
            redisAdapter.hashMSet(userInfoKey, userInfo.putFieldValueToMap());
            redisAdapter.expireKey(userInfoKey, RedisExpireTime.EXPIRE_DAY_7);
            redisAdapter.strSetByNormal(userLogin, userInfo.getUserId().toString());
            redisAdapter.expireKey(userLogin, RedisExpireTime.EXPIRE_DAY_7);
            userInfo.setLoginId(loginId);
        }
        return userInfo;
    }
    
    /**
     * 同步用户信息，如果存在返回该用户信息，不能存在返回null
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
            if (user.getSex() != null){
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
            logger.error("获取创新版用户生日失败." + e.getMessage());
        }
        userInfo.setUserId(user.getUserId());
        userInfo.setLoginId(user.getLoginId());
        userInfo.setVersionOptimizedLock(user.getVersionOptimizedLock());
        return userInfo;
    }

    /**
     * 同步不存的用户，如果使用此方法说明用户不在
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
            logger.error("获取创新版用户生日失败." + e.getMessage());
        }
        return userInfo;
    }
    
    /**
     * 设置直播用户性别
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

    private void fromCxUser(UserInfo loginUser) {
        // 注册
        loginUser.setOrigin(Constants.USER_INFO_ORIGIN_0);
        // 0 正常
        loginUser.setAcctStatus(Constants._0);
        // 0 普通用户
        loginUser.setAcctType(Constants._0);
        String token = "";
        try {
            // 注册融云
            token = RongCloudFacade.getToken(loginUser.getLoginId(), loginUser.getUserName(), loginUser.getHeadImg(), 1);
            loginUser.setRongToken(token);
        } catch (Exception e) {
            logger.error("注册融云token异常");
        }

    }

    private void setUserInfo(UserInfo loginUser) {
        Date date = new Date();
        loginUser.setCreateTime(date);
        // 星座
        loginUser.setZodiac(DateTools.getZodiacByDate(loginUser.getBirthday()));
        loginUser.setUpdateTime(date);
        loginUser.setLoginOn(date);
        loginUser.setVersionOptimizedLock(0);
    }

    private void setUserInfoRedisCache(UserInfo loginUser) {
        String loginId = loginUser.getLoginId();
        String userLogin = RedisKey.USER_LOGIN_ID_ + loginId;
        redisAdapter.strSetByNormal(userLogin, loginUser.getUserId().toString());
        redisAdapter.expireKey(userLogin, RedisExpireTime.EXPIRE_DAY_7);

        String userInfoKey = RedisKey.USER_INFO_ + loginUser.getUserId();
        if (redisAdapter.existsKey(userInfoKey)) {// 已注册过，获得该用户最新数据
            redisAdapter.hashMSet(userInfoKey, loginUser.putFieldValueToMap());
            try {
                loginUser.doStringMapToValue(
                        redisAdapter.hashMGet(userInfoKey, "userId", "userName", "headImg", "rongToken"));
            } catch (ParseException e) {
                logger.error("转换用户信息时失败。");
                throw new IWanviException("转换用户信息时失败。");
            }
        } else {// 没注册过插入数据
            redisAdapter.hashMSet(userInfoKey, loginUser.putFieldValueToMap());
        }
        // 保存一周
        redisAdapter.expireKey(userInfoKey, RedisExpireTime.EXPIRE_DAY_7);
    }

    @Override
    public String get1stPageFollows(Long userId) {
        try {
            List<Map<String, Object>> follows = followService.queryFollow(userId, 1, 0);
            StringBuilder sb = new StringBuilder();
            int i = 0;
            for (Map<String, Object> follow : follows) {
                Long anchorId = MapUtils.getLongValue(follow, "anchorId", 0);// ((Integer)
                if(i < follows.size() - 1){
                    sb.append(anchorId.toString() + ",");    
                } else{
                    sb.append(anchorId.toString());
                }
                i++;
            }
            return sb.toString();            
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return "";
    }

}
