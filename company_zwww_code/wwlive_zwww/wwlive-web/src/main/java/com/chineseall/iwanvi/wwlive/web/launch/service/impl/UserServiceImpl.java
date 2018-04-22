package com.chineseall.iwanvi.wwlive.web.launch.service.impl;

import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.chineseall.iwanvi.wwlive.dao.wwlive.*;
import com.chineseall.iwanvi.wwlive.domain.wwlive.RoleInfo;
import com.chineseall.iwanvi.wwlive.web.common.helper.*;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.chineseall.iwanvi.wwlive.common.cache.RedisClientAdapter;
import com.chineseall.iwanvi.wwlive.common.cache.RedisExpireTime;
import com.chineseall.iwanvi.wwlive.common.cache.RedisKey;
import com.chineseall.iwanvi.wwlive.common.constants.Constants;
import com.chineseall.iwanvi.wwlive.common.exception.IWanviException;
import com.chineseall.iwanvi.wwlive.common.tools.RongMsgUtils;
import com.chineseall.iwanvi.wwlive.domain.wwlive.LiveVideoInfo;
import com.chineseall.iwanvi.wwlive.domain.wwlive.UserInfo;
import com.chineseall.iwanvi.wwlive.web.launch.service.UserService;

@Service("launchUserService")
public class UserServiceImpl implements UserService{

    private final Logger LOGGER = Logger
            .getLogger(this.getClass());
    @Autowired
    private RedisClientAdapter redisAdapter;
    
    @Autowired
    private ContributionListMapper contribMapper;
    
    @Autowired
    private UserInfoMapper userInfoMapper;
    
    @Autowired
    private LiveAdminMapper adminMapper;
    
    @Autowired
    private LiveVideoInfoMapper videoMapper;

    @Autowired
    private RoleInfoMapper roleInfoMapper;

    @Override
    public Map<String, Object> mute(Long anchorId, Long videoId, Long userId, Integer duration,String loginId) {//duration以秒为单位
        
    	Map<String, Object> data = new HashMap<>();
    	try {
            UserInfo user = UserInfoHelper.getAndCacheUserInfoByLoginId(redisAdapter, userInfoMapper, loginId);
//            UserInfo user = getUserInfoFromRedis(userId);
            if (user == null) {
                data.put("result", 1);
                return data;
            }
            if(user.getAcctType() == 1){
                //超管不能被禁言
                throw new IWanviException("超管不能被禁言!");
            }
            /*int level = RoleNobleHelper.userRoleNobleLevel(redisAdapter, roleInfoMapper, user.getUserId());
            if(level >= 6) { // 主播可禁言除超管以外所有用户
                data.put("result", 2);
                return data;
            }*/

            String key = RedisKey.BLACKLIST_ + anchorId + Constants.UNDERLINE + userId;
            if (redisAdapter.existsKey(key)) {
                redisAdapter.expireKey(key, duration);
                data.put("result", 1);
            } else {
                redisAdapter.strSetEx(key, key, duration);
                data.put("result", 1);
            }
            
            //根据videoId获得anchorId和streamName
            LiveVideoInfo videoInfo = 
                    LiveVideoInfoHelper.getAndCacheVideoInfo(redisAdapter, videoMapper, videoId);
            //发送消息通知禁言
            noticeUserMuted(videoInfo.getChatroomId(), videoInfo.getAnchorId(), 
                    userId, user, duration, videoId);
            
    	} catch (Exception e) {
			LOGGER.error("转换userInfo失败", e);
    	}

        return data;
    }
    
    /**
     * 通知用户禁言消息
     * @param chatRoomId
     * @param fromUserId
     * @param userId
     * @param duration 以秒为单位
     * @param videoId
     */
    private void noticeUserMuted(String chatRoomId, Long fromUserId, 
            Long userId, UserInfo user, Integer duration, Long videoId) {
        try {
            RongMsgUtils.sendChatroomMsg(chatRoomId, fromUserId, new Integer(2), 
            		"系统消息：" + user.getUserName() + "已被管理员禁言！", 
            		"{\"userId\":\"" + userId + "\", \"gagTime\":\"" 
                    + duration + "\", \"videoId\":\"" + videoId + "\", \"loginId\":\"" + user.getLoginId() + "\"}");
            LOGGER.info("系统消息：" + user.getUserName() + "已被管理员禁言");
        } catch (Exception e) {
            LOGGER.error("用户禁言通知失败，ChatroomId：" + chatRoomId);
        }
    }

    /**
     * 从缓存获得userInfo信息
     * @param userId
     * @return
     * @throws ParseException
     */
    private UserInfo getUserInfoFromRedis(Long userId) throws ParseException {
    	UserInfo user = new UserInfo();
		String userKey = RedisKey.USER_INFO_ + userId;
		if(redisAdapter.existsKey(userKey)){
		    Map<String, String> userMap = redisAdapter.hashMGet(userKey, "acctType", "userName", "loginId");
		    user.doStringMapToValue(userMap);
		} else{
		    user = UserInfoHelper.getAndCacheUserInfo(redisAdapter, userInfoMapper, userId);
		}
		return user;
    }
    
    @Override
    public Map<String, Object> getInfo(String viewId, Long anchorId) {
        Map<String, String> userInfo = getUserInfoFromRedis(viewId, anchorId);//获得用户的缓存信息
        if (userInfo == null || userInfo.isEmpty()) {
            throw new IWanviException("未查找到该用户信息...");
        }
        
        // 用户对主播的送礼个数
        int giftCnt = 0;
        String idStr = userInfo.get("userId");
        if(StringUtils.isNotBlank(idStr)){
            Long userId = Long.valueOf(idStr);
            giftCnt = getGiftCntToAnchor(anchorId, userId);
        }
        userInfo.put("isCustomer", giftCnt + "");
        
        Map<String, Object> data = new HashMap<>();
		UserRankHelper.setUserInfo(redisAdapter, 
        		contribMapper, userInfo);//获得用户的年龄和全站排行
		
		// 添加土豪勋章
		Map<String, Object> tempMap = new HashMap<>();
		tempMap.putAll(userInfo);
		String richestUserId = redisAdapter.strGet(RedisKey.RICHEST_MEDAL_OWNER);
		tempMap.put("isRichest", userInfo.get("userId").toString().equals(richestUserId) ? 1 : 0);
        data.put("userInfo", tempMap);
        return data;
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

    private Map<String, String> getUserInfoFromRedis(String loginId, Long anchorId) {
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
        if (userInfo == null || userInfo.isEmpty()){
            UserInfo user = UserInfoHelper.getAndCacheUserInfoByLoginId(redisAdapter, userInfoMapper, loginId);
            if (user == null) {
                return null;
            }
            userId = user.getUserId();
            userInfo = user.putFieldValueToStringMap();
        }
        
        //用户类型信息acctType, 0-普通用户, 1-超管, 2-房管
        //此处的acctType不等同与用户表中的acctType
        String acctType = userInfo.get("acctType");
        if("0".equals(acctType)){
        	if (LiveAdminHelper.isAdmin(redisAdapter, adminMapper, anchorId, userId)) {//普通用户是否为房管
        		userInfo.put("acctType", "2");
        	}
        }
        //当星座为空时, 设置默认星座摩羯座
        String zodiac = userInfo.get("zodiac");
        if(StringUtils.isEmpty(zodiac)){
            userInfo.put("zodiac", "双鱼座");
        }
        
        return userInfo;
    }


    /**
     * 获取贵族名称
     */
    public String getNobleName (long userId) {
        String nobleKey = RedisKey.NobleKey.USER_ROLE_ + userId;// 贵族key
        String nobleName = "";
        if ((redisAdapter.hashGet(nobleKey, "goodsName")) != null) {
            nobleName = redisAdapter.hashGet(nobleKey, "goodsName");
        } else {
            long currentTime = new Date().getTime();
            RoleInfo lv = roleInfoMapper.findHighLevelGoodsIdByUserId(userId);
            if (lv != null && lv.getEffectiveEndTime() != null) {
                /**
                 * 缓存时间为贵族有效时间
                 */
                int expire = expireTime(lv.getEffectiveEndTime().getTime(), currentTime);
                if (expire > 0) {
                    redisAdapter.hashMSet(nobleKey, lv.putFieldValueToMapAndDateString());
                    redisAdapter.expireKey(nobleKey,expire);
                    nobleName = lv.getGoodsName();
                }
            }
        }
        return nobleName;
    }

    /**
     * 获得过期时间
     * @param effectiveEndTime
     * @param currentTime
     * @return
     */
    private static int expireTime(long effectiveEndTime, long currentTime) {
        long tmpExpire = (effectiveEndTime - currentTime) / 1000;
        int expire = 0;
        if (tmpExpire > Integer.MAX_VALUE) {
            expire = Integer.MAX_VALUE;
        } else {
            expire = (int) tmpExpire;
        }
        return expire;
    }


}
