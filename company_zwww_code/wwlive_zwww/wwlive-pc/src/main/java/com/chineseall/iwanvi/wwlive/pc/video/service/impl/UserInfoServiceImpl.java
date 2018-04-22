package com.chineseall.iwanvi.wwlive.pc.video.service.impl;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import com.alibaba.fastjson.JSONObject;
import com.chineseall.iwanvi.wwlive.common.cache.RedisClientAdapter;
import com.chineseall.iwanvi.wwlive.common.cache.RedisExpireTime;
import com.chineseall.iwanvi.wwlive.common.cache.RedisKey;
import com.chineseall.iwanvi.wwlive.common.constants.Constants;
import com.chineseall.iwanvi.wwlive.common.exception.IWanviException;
import com.chineseall.iwanvi.wwlive.common.external.rongcloud.RongCloudFacade;
import com.chineseall.iwanvi.wwlive.common.external.rongcloud.domain.TxtMessage;
import com.chineseall.iwanvi.wwlive.dao.wwlive.ContributionListMapper;
import com.chineseall.iwanvi.wwlive.dao.wwlive.LiveAdminMapper;
import com.chineseall.iwanvi.wwlive.dao.wwlive.LiveVideoInfoMapper;
import com.chineseall.iwanvi.wwlive.dao.wwlive.RoleInfoMapper;
import com.chineseall.iwanvi.wwlive.dao.wwlive.UserInfoMapper;
import com.chineseall.iwanvi.wwlive.domain.wwlive.LiveAdmin;
import com.chineseall.iwanvi.wwlive.domain.wwlive.LiveVideoInfo;
import com.chineseall.iwanvi.wwlive.domain.wwlive.UserInfo;
import com.chineseall.iwanvi.wwlive.pc.common.helper.FollowServiceFactory;
import com.chineseall.iwanvi.wwlive.pc.common.helper.LiveAdminHelper;
import com.chineseall.iwanvi.wwlive.pc.common.helper.UserInfoHelper;
import com.chineseall.iwanvi.wwlive.pc.common.helper.UserRankHelper;
import com.chineseall.iwanvi.wwlive.pc.event.service.MedalHonorService;
import com.chineseall.iwanvi.wwlive.pc.video.service.UserInfoService;
import com.chineseall.iwanvi.wwlive.pc.video.util.DateUtil;
import com.chineseall.iwanvi.wwlive.pc.common.helper.RoleNobleHelper;
import com.service.FollowAnchorService;
import com.service.impl.FollowAnchorServiceImpl;

@Service
public class UserInfoServiceImpl implements UserInfoService {

    static final Logger LOGGER = Logger.getLogger(UserInfoServiceImpl.class);
    
    @Autowired
    private UserInfoMapper userInfoMapper; ;
    
    @Autowired
    private ContributionListMapper contribMapper;

    @Autowired
    private RedisClientAdapter redisAdapter;
    
    @Autowired
    private LiveAdminMapper adminMapper;
    
    @Autowired
    private LiveVideoInfoMapper liveVideoInfoMapper;
    
    @Autowired
    private LiveAdminMapper liveAdminMapper;
    
    @Autowired
    private MedalHonorService medalHonorService;
    
	@Autowired
	private RoleInfoMapper roleInfoMapper;

    //获取关注数
    private static FollowAnchorService followService = FollowServiceFactory.getFollowAnchorServiceInstance();
    
    public Map<String, Object> getUserInfo(Long anchorId, Long userId) {

        Map<String, Object> userInfo = getInfo(userId);
    	if (!ObjectUtils.isEmpty(userInfo)) {
            //获得该用户对该主播的贡献值
            //获得全站排行;
    		String rankKey = RedisKey.USER_RANK_CONTRIB_ + userId;
    		if (redisAdapter.existsKey(rankKey)) {
                userInfo.putAll(UserRankHelper.getUserRankCache(redisAdapter, contribMapper, userId));
    		} else {
                userInfo.putAll(UserRankHelper.getAndCacheUserRank(redisAdapter, contribMapper, userId));
    		}
    	}
    	
    	// 是否送过礼
    	if (StringUtils.isNotBlank(userId.toString())) {
            String key = RedisKey.USER_CONTRIBUTION_CNT_ + anchorId + Constants.UNDERLINE + userId;
            if (redisAdapter.existsKey(key)) {
                String cnt = redisAdapter.strGet(key);
                if (!"0".equals(cnt)) {
                    userInfo.put("follower", cnt);
                }
            } else {
                int cnt = contribMapper.countByAnchorAndUser(anchorId, userId, 1);
                userInfo.put("follower", cnt + "");//做过贡献标记
                redisAdapter.strSet(key, cnt);
                redisAdapter.expireKey(key, RedisExpireTime.EXPIRE_DAY_1);
            }
        }
    	
    	// 是否房管
        boolean isAdmin = 
                LiveAdminHelper.isAdmin(redisAdapter, liveAdminMapper, anchorId, Long.valueOf(userId));
        if(isAdmin){
            userInfo.put("isAdmin", "1");
        } else{
            userInfo.put("isAdmin", "0");
        }

        int followNum = 0;
        try {
            //获取用户的关注数
            followNum = followService.getFollowNumber(userId);
        } catch (Exception e) {
            LOGGER.error("获取用户关注数失败--->>>" + e);
        }
        userInfo.put("followNum", String.valueOf(followNum));
        // 用户勋章信息
        List<String> medals = medalHonorService.getUserMedalsById(userId);
        userInfo.put("medals", medals);

		//获得贵族信息
		List<Integer> nobles = RoleNobleHelper.userRoleNobleLvels(redisAdapter, roleInfoMapper, userId);
		if (nobles != null && nobles.size() > 0) {
			Collections.sort(nobles);
			Collections.reverse(nobles);
		}
		userInfo.put("nobles", nobles);
    	return userInfo;
    }
	
    private Map<String, Object> getInfo(long userId) {
        //先从redis中查询用户信息
    	String userInfoKey = RedisKey.USER_INFO_ + userId;
    	Map<String, String> userInfo = null;
    	if (redisAdapter.existsKey(userInfoKey)) {
    		userInfo = redisAdapter.hashMGet(userInfoKey, "userName", "acctType", "headImg", "sex", "birthday", "age", "zodiac");
    	}
      	Map<String, Object> userM = new HashMap<String, Object>();
      	
     	if (ObjectUtils.isEmpty(userInfo) || userInfo.isEmpty()) {
     		UserInfo user = userInfoMapper.findById(userId);
    		redisAdapter.hashMSet(userInfoKey, user.putFieldValueToStringMap());
    		redisAdapter.expireKey(userInfoKey, RedisExpireTime.EXPIRE_DAY_5);
    		userM = new HashMap<>();
    		userM.put("userName", user.getUserName());
    		userM.put("acctType", user.getAcctType());
    		userM.put("headImg", user.getHeadImg());
    		userM.put("sex", user.getSex());
    		userM.put("zodiac", user.getZodiac());
    		userM.put("age", DateUtil.getAgeByDate(user.getBirthday()));
     	} else {
     		try {
     			String strAge = userInfo.get("age");
     			if (StringUtils.isEmpty(strAge)) {
         			int age = 0;
         			String birthDay = userInfo.get("birthday");
     				if (StringUtils.isNotEmpty(birthDay)) {
        				age = DateUtil.getAgeByDate(DateUtils.parseDate(birthDay, new String[]{"yyyy-MM-dd", "yyyy-MM-dd HH:mm:ss"}));
         			}
    	         	userM.putAll(userInfo);
    	         	userM.put("age", age);
     			}
     			
			} catch (ParseException e) {
				LOGGER.error("计算年龄时异常", e);
			}
     	}
     	return userM;
    }
    
    public Map<String, Object> getUserInfo(String loginId, long anchorId) {
    	Map<String, String> userInfo = getUserInfoFromRedis(loginId);
    	if(userInfo == null || userInfo.isEmpty()) {
    		return null;
    	}
    	String userId = userInfo.get("userId");
    	if (StringUtils.isNotBlank(userId)) {
    		String key = RedisKey.USER_CONTRIBUTION_CNT_ + anchorId + Constants.UNDERLINE + userId;
    		if (redisAdapter.existsKey(key)) {
    			String cnt = redisAdapter.strGet(key);
    			if (!"0".equals(cnt)) {
    				userInfo.put("follower", cnt);
    			}
    		} else {
            	int cnt = contribMapper.countByAnchorAndUser(anchorId, Integer.parseInt(userId), 1);
            	userInfo.put("follower", cnt + "");//做过贡献标记
            	redisAdapter.strSet(key, cnt);
            	redisAdapter.expireKey(key, RedisExpireTime.EXPIRE_DAY_1);
    		}
    	}
    	boolean isAdmin = 
    	        LiveAdminHelper.isAdmin(redisAdapter, liveAdminMapper, anchorId, Long.valueOf(userId));
    	if(isAdmin){
    	    userInfo.put("isAdmin", "1");
    	} else{
    	    userInfo.put("isAdmin", "0");
    	}
    	
    	// 用户勋章信息
    	Map<String, Object> tempMap = new HashMap<>();
    	tempMap.putAll(userInfo);
    	List<String> medals = medalHonorService.getUserMedalsById(Long.valueOf(userId));
    	tempMap.put("medals", medals);
        Integer level = RoleNobleHelper.userRoleNobleLevel(redisAdapter, roleInfoMapper, Long.valueOf(userId));
        if(level != null){
            tempMap.put("nobelCode", level);
        }
        Map<String, Object> result = new HashMap<>();
    	result.putAll(tempMap);
    	return result;
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
				userInfo = redisAdapter.hashMGet(userKey, "userId","headImg", "userName", 
						"sex", "birthday", "zodiac", "acctType");
			}
		} 
		if (userInfo == null || userInfo.isEmpty()) {
			UserInfo user = getAndCacheUserInfoByLoginId(redisAdapter, userInfoMapper, loginId);
			if (user == null) {
				return null;
			}
			userInfo = user.putFieldValueToStringMap();
		}
		return userInfo;
	}
    
    private UserInfo getAndCacheUserInfoByLoginId(RedisClientAdapter redisAdapter, 
			UserInfoMapper userInfoMapper, String loginId) {
		if (redisAdapter == null 
				|| userInfoMapper == null || StringUtils.isBlank(loginId)) {
			return null;
		}
		UserInfo user = userInfoMapper.findAllInfoByLoginId(loginId);
        if (user != null) {
            // 添加用户信息到redis中
            redisAdapter.hashMSet(RedisKey.USER_INFO_ + user.getUserId(), user.putFieldValueToStringMap());
    		redisAdapter.expireKey(RedisKey.USER_INFO_ + user.getUserId(), RedisExpireTime.EXPIRE_DAY_5);
    		String userLogin = RedisKey.USER_LOGIN_ID_ + loginId;
			redisAdapter.strSetByNormal(userLogin, user.getUserId().toString());
			redisAdapter.expireKey(userLogin, RedisExpireTime.EXPIRE_DAY_7);
        } else {
        	LOGGER.error("用户：loginId" + loginId + "不存在。");
        }
        return user;
	}

    /**
     * 用户是否是禁言和房管
     */
	@Override
	public Map<String, Object> isBlackOrAdmin(long anchorId, int videoId, int userId) {
	    Map<String, Object> resultMap = new HashMap<>();
		String key = RedisKey.BLACKLIST_ + anchorId + Constants.UNDERLINE + userId;
		boolean isBlack = redisAdapter.existsKey(key);
	    boolean isAdmin = LiveAdminHelper.isAdmin(redisAdapter, adminMapper, anchorId, 
	            Long.valueOf(userId + ""));
	    resultMap.put("isBlack", isBlack ? 1 : 0);//禁言状态
	    resultMap.put("isAdmin", isAdmin ? 1 : 0);//是否房管
		return resultMap;
	}

    @Override
    public Map<String, Object> setAdmin(String chatRoomId, Long anchorId, Long userId) {
        String userName = null;
        String key = RedisKey.USER_INFO_ + userId;
        String loginId = "";
        if (!redisAdapter.existsKey(key)) {
            UserInfo user = UserInfoHelper.getAndCacheUserInfo(redisAdapter, userInfoMapper, userId);
            if (user == null || user.getUserId() == null) {
                throw new IWanviException("此用户不存在！");
            } else if(user.getAcctType() == 1){
                throw new IWanviException("超管用户不能设置为房管!");
            }
            userName = user.getUserName();
            loginId = user.getLoginId().toString();
        } else{
            Map<String, String> userInfo = redisAdapter.hashMGet(key, "acctType", "userName", "loginId");
            userName = userInfo.get("userName");
            loginId = userInfo.get("loginId");
            if("1".equals(userInfo.get("acctType"))){
                throw new IWanviException("超管用户不能设置为房管!");
            }
        }
        
        Map<String, Object> data = new HashMap<>();
        int result = upsertLiveAdmin(anchorId, userId);
        data.put("result", result);
        //发出设置房管通知
        noticeSetAdmin(chatRoomId, anchorId, userId, userName, loginId);
        return data;
    }
	
    /**
     * 增加房管，如果此用户被删除，则重新更新用户的状态为正常
     * <p/>
     * 0正常，1解除
     * @param anchorId
     * @param userId
     * @return
     */
    private int upsertLiveAdmin(Long anchorId, Long userId) {
        String adminKey = RedisKey.LIVE_ADMIN_INFO_ + anchorId + Constants.UNDERLINE + userId;
        int status = 0;
        if (redisAdapter.existsKey(adminKey)){
            String adminStatus = redisAdapter.hashGet(adminKey, "adminStatus");
            String adminId = redisAdapter.hashGet(adminKey, "adminId");
            
            if ("0".equals(adminStatus) && StringUtils.isNotBlank(adminId)) {
                status = adminMapper.updateAdminStatus(anchorId, userId, new Integer(0)); //如果房管记录存在, 说明数据库中是有记录的, 则更新房管状态即可
                return status;
            }
        }
        //redis中无缓存记录, 先缓存; 查看库中是否有记录, 如果有则更新, 没有则插入
        LiveAdmin admin = LiveAdminHelper.getAndCacheAdminInfo(redisAdapter, adminMapper, anchorId, userId);
        if (admin == null || admin.getUserId() <= 0) {
            status = adminMapper.addAdmin(anchorId, userId);
        } else {
            status = adminMapper.updateAdminStatus(anchorId, userId, new Integer(0));
        }
        redisAdapter.delKeys(adminKey);//删除存信息
        return status;
    }
    
    /**
     * 通知设置房管
     * @param info
     */
    private void noticeSetAdmin(String chatRoomId, Long anchorId, Long userId, String userName, String loginId) {
        try {
            List<String> chatIds = new ArrayList<String>();
            chatIds.add(chatRoomId);
            TxtMessage tx = new TxtMessage("");
            JSONObject json = new JSONObject();
            json.put("dataType", "14");
            json.put("dataValue", "");
            json.put("dataExtra", "{\"userId\":\"" + userId + "\", \"userName\":\"" + userName + "\", \"loginId\":\""
                    + loginId +"\"}");
            tx.setExtra(json.toJSONString());
            RongCloudFacade.publishChatroomMessage(anchorId.toString(), chatIds, tx, Constants.FORMAT_JSON);
        } catch (Exception e) {
            LOGGER.error("通知失败，ChatroomId：" + chatRoomId);
        }
    }

    @Override
    public Map<String, Object> removeAdmin(String chatRoomId, long anchorId, Long userId) {
    	Map<String, Object> data = new HashMap<>();
        
        if(!LiveAdminHelper.isAdmin(redisAdapter, adminMapper, anchorId, userId)){
            data.put("result", 1);
            return data;
        }
        
        String userName = null;
        String key = RedisKey.USER_INFO_ + userId;
        String loginId = "";
        if (!redisAdapter.existsKey(key)) {
            UserInfo user = UserInfoHelper.getAndCacheUserInfo(redisAdapter, userInfoMapper, userId);
            if (user == null || user.getUserId() == null) {
                throw new IWanviException("此用户不存在！");
            }
            userName = user.getUserName();
            loginId = user.getLoginId().toString();                    
        } else {
            userName = redisAdapter.hashGet(key, "userName");
            loginId = redisAdapter.hashGet(key, "loginId");
        }
        
        int result = adminMapper.updateAdminStatus(anchorId, userId, new Integer(1));
        //清除缓存信息
        String adminKey = RedisKey.LIVE_ADMIN_INFO_ + anchorId
                + Constants.UNDERLINE + userId;
        redisAdapter.delKeys(adminKey);
        
        data.put("result", result);
        
        if(chatRoomId == null){
            //获取主播正在直播的视频
            LiveVideoInfo video = liveVideoInfoMapper.getLatestLivingVideo(anchorId);
            if(video != null){
                chatRoomId = video.getChatroomId();
            } else{
                return data;
            }
        }
        noticeRemoveAdmin(chatRoomId, anchorId, userId, userName, loginId);
        return data;
    }
    
    /**
     * 通知解除房管
     * @param info
     */
    private void noticeRemoveAdmin(String chatRoomId, Long anchorId, Long userId, String userName, String loginId) {
        try {
            List<String> chatIds = new ArrayList<String>();
            chatIds.add(chatRoomId);
            TxtMessage tx = new TxtMessage("");
            JSONObject json = new JSONObject();
            json.put("dataType", "15");
            json.put("dataValue", "");
            json.put("dataExtra", "{\"userId\":\"" + userId + "\", \"userName\":\"" + userName + "\", \"loginId\":\""
                    + loginId + "\"}");
            tx.setExtra(json.toJSONString());
            RongCloudFacade.publishChatroomMessage(anchorId.toString(), chatIds, tx, Constants.FORMAT_JSON);
        } catch (Exception e) {
            LOGGER.error("通知失败，ChatroomId：" + chatRoomId);
        }
    }
}
