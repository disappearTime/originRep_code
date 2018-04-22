package com.chineseall.iwanvi.wwlive.pc.common.helper;


import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.chineseall.iwanvi.wwlive.common.cache.RedisClientAdapter;
import com.chineseall.iwanvi.wwlive.common.cache.RedisExpireTime;
import com.chineseall.iwanvi.wwlive.common.cache.RedisKey;
import com.chineseall.iwanvi.wwlive.dao.wwlive.UserInfoMapper;
import com.chineseall.iwanvi.wwlive.domain.wwlive.UserInfo;

/**
 * 更新同步用户redis信息辅助类
 * @author DIKEPU
 *
 */
public class UserInfoHelper {
	
    static final Logger LOGGER = Logger.getLogger(UserInfoHelper.class);
	
	public static UserInfo getAndCacheUserInfo(RedisClientAdapter redisAdapter, 
			UserInfoMapper userInfoMapper, long userId) {
		if (redisAdapter == null 
				|| userInfoMapper == null || userId == 0) {
			return null;
		}
		UserInfo user = userInfoMapper.findById(userId);
        if (user != null) {
            // 添加用户信息到redis中
            redisAdapter.hashMSet(RedisKey.USER_INFO_ + userId, user.putFieldValueToStringMap());
    		redisAdapter.expireKey(RedisKey.USER_INFO_ + userId, RedisExpireTime.EXPIRE_DAY_7);
        } else {
        	LOGGER.error("用户：" + userId + "不存在。");
        }
        return user;
	}

	public static UserInfo getAndCacheUserInfoByLoginId(RedisClientAdapter redisAdapter, 
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
}
