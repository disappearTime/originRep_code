package com.chineseall.iwanvi.wwlive.web.common.helper;


import com.chineseall.iwanvi.wwlive.common.cache.RedisClientAdapter;
import com.chineseall.iwanvi.wwlive.common.cache.RedisExpireTime;
import com.chineseall.iwanvi.wwlive.common.cache.RedisKey;
import com.chineseall.iwanvi.wwlive.common.constants.Constants;
import com.chineseall.iwanvi.wwlive.common.exception.IWanviException;
import com.chineseall.iwanvi.wwlive.common.tools.StrMD5;
import com.chineseall.iwanvi.wwlive.dao.wwlive.UserInfoMapper;
import com.chineseall.iwanvi.wwlive.domain.wwlive.OrderInfo;
import com.chineseall.iwanvi.wwlive.domain.wwlive.UserInfo;
import com.chineseall.iwanvi.wwlive.web.common.util.DateTools;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import java.text.ParseException;
import java.util.Date;
import java.util.Map;

/**
 * 更新同步用户redis信息辅助类
 * @author DIKEPU
 *
 */
public class UserInfoHelper {
	
    static final Logger LOGGER = Logger.getLogger(UserInfoHelper.class);

	public static final int DEFAULT_NAME_LEN = 8;
	
	public static UserInfo getAndCacheUserInfo(RedisClientAdapter redisAdapter, 
			UserInfoMapper userInfoMapper, long userId) {
		if (redisAdapter == null 
				|| userInfoMapper == null || userId == 0) {
			return null;
		}
		UserInfo user = userInfoMapper.findById(userId);
        if (user != null) {
        	// 如果用户没有account和token, 则添加
			addNewInfo(user, userInfoMapper);

            // 添加用户信息到redis中
            redisAdapter.hashMSet(RedisKey.USER_INFO_ + userId, user.putFieldValueToStringMap());
    		redisAdapter.expireKey(RedisKey.USER_INFO_ + userId, RedisExpireTime.EXPIRE_DAY_7);
        } else {
        	LOGGER.error("用户：" + userId + "不存在。");
        }
        return user;
	}
	
	/**
	 * 获得用户昵称
	 * @param redisAdapter
	 * @param userInfoMapper
	 * @param userId
	 * @return
	 */
	public static String getUserName(RedisClientAdapter redisAdapter, 
			UserInfoMapper userInfoMapper, long userId) {
		String userName = "";
    	if (redisAdapter.existsKey(RedisKey.USER_INFO_ + userId)) {
    		userName = redisAdapter.hashGet(RedisKey.USER_INFO_ + userId, "userName");
    	} else {
    		UserInfo user = getAndCacheUserInfo(redisAdapter, userInfoMapper, userId);
    		if (user != null) {
    			userName = user.getUserName();
    		}
    	}
    	return userName;
	}
	
	public static Map<String, String> getAndCacheUserMap(RedisClientAdapter redisAdapter, 
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
    		return user.putFieldValueToStringMap();
        } else {
        	LOGGER.error("用户：" + userId + "不存在。");
        }
        return null;
	}
	
	public static UserInfo getAndCacheUserInfoByLoginId(RedisClientAdapter redisAdapter, 
			UserInfoMapper userInfoMapper, String loginId) {
		if (redisAdapter == null 
				|| userInfoMapper == null || StringUtils.isBlank(loginId)) {
			return null;
		}
		UserInfo user = userInfoMapper.findAllInfoByLoginId(loginId);
        if (user != null) {
			// 如果用户没有account和token, 则添加
			addNewInfo(user, userInfoMapper);

            // 添加用户信息到redis中
            redisAdapter.hashMSet(RedisKey.USER_INFO_ + user.getUserId(), user.putFieldValueToStringMap());
    		redisAdapter.expireKey(RedisKey.USER_INFO_ + user.getUserId(), RedisExpireTime.EXPIRE_DAY_5);
    		String userLogin = RedisKey.USER_LOGIN_ID_ + loginId;
			redisAdapter.strSetByNormal(userLogin, user.getUserId().toString());
			redisAdapter.expireKey(userLogin, RedisExpireTime.EXPIRE_DAY_7);
        } else {
        	LOGGER.error("用户：loginId " + loginId + "不存在。");
        }
        return user;
	}
	
	/**
	 * 
	 * @param redisAdapter
	 * @param userId
	 * @param fields 为空返回全部数据
	 * @return
	 */
	public static UserInfo getUserInfoFromCache(RedisClientAdapter redisAdapter, long userId, String... fields) {
		if (redisAdapter == null 
				|| userId <= 0) {
			return null;
		}
		String key = RedisKey.USER_INFO_ + userId;
		Map<String, String> userMap = null;
		if (fields != null && fields.length > 0) {
			userMap = redisAdapter.hashMGet(key, fields);
		} else {
			userMap = redisAdapter.hashGetAll(key);
		}
		if (userMap != null && !userMap.isEmpty()) {
			UserInfo user = new UserInfo();
			try {
				user.doStringMapToValue(userMap);
				return user;
			} catch (ParseException e) {
				LOGGER.error("转换用户信息失败：", e);
			}
		}
		return null;
	}
	

	/**
	 * 
	 * @param redisAdapter
	 * @param userId
	 * @param fields 为空返回全部数据
	 * @return
	 */
	public static Map<String, String> getUserInfoMapFromCache(RedisClientAdapter redisAdapter, long userId, String... fields) {
		if (redisAdapter == null 
				|| userId <= 0) {
			return null;
		}
		String key = RedisKey.USER_INFO_ + userId;
		Map<String, String> userMap = null;
		if (fields != null && fields.length > 0) {
			userMap = redisAdapter.hashMGet(key, fields);
		} else {
			userMap = redisAdapter.hashGetAll(key);
		}
		return userMap;
	}

	/**
	 * 已经存在的用户如果没有account信息, password信息或token信息, 则追加
	 * @param user
	 * @param userInfoMapper
	 */
	public static UserInfo addNewInfo(UserInfo user, UserInfoMapper userInfoMapper){
		if (user == null || user.getUserId() == null){
			return null;
		}
		Long userId = user.getUserId();

		String account = user.getAccount();
		if(StringUtils.isBlank(account)){
			account = getAccount(user.getLoginId());
			UserInfo newUser = new UserInfo();
			newUser.setUserId(userId);
			newUser.setAccount(account);
			user.setAccount(account);
			userInfoMapper.updateUserInfoById(newUser);
		}
		String password = user.getPassword();
		if(StringUtils.isBlank(password)){
			Date createTime = user.getCreateTime();
			String date = DateTools.formatDate(createTime, "MMdd");
			password = account + new StringBuilder(date).reverse().toString();
			password = StrMD5.getInstance().getStringMD5(password);
			UserInfo newUser = new UserInfo();
			newUser.setUserId(userId);
			newUser.setPassword(password);
			user.setPassword(password);
			userInfoMapper.updateUserInfoById(newUser);
		}
		String token = user.getToken();
		if(StringUtils.isBlank(token)){
			token = Constants.getUUID();
			UserInfo newUser = new UserInfo();
			newUser.setUserId(userId);
			newUser.setToken(token);
			user.setToken(token);
			userInfoMapper.updateUserInfoById(newUser);
		}
		return user;
	}

	public static void cacheUserInfo(UserInfo userInfo, RedisClientAdapter redisAdapter){
		Long userId = userInfo.getUserId();
		if (userId == null || redisAdapter == null){
			return;
		}
		String userInfoKey = RedisKey.USER_INFO_ + userId;
		redisAdapter.hashMSet(userInfoKey, userInfo.putFieldValueToMap());
		redisAdapter.expireKey(userInfoKey, RedisExpireTime.EXPIRE_DAY_7);
	}

	/**
	 * 根据loginId生成默认昵称
	 * @param loginId
	 * @return
	 */
	public static String getDefaultName(String loginId) {
		if (StringUtils.isBlank(loginId)) {
			throw new IWanviException("昵称生成失败!");
		}

		String prefix = "游客";
		for (int i = 0; i < DEFAULT_NAME_LEN - loginId.length(); i++) {
			prefix += "0";
		}
		return prefix + loginId;
	}

	/**
	 * 根据loginId生成账号
	 * @param loginId
	 * @return
	 */
	public static String getAccount(String loginId) {
		if (StringUtils.isBlank(loginId)) {
			throw new IWanviException("账号生成失败!");
		}

		if (loginId.contains("_")) {
			loginId = loginId.substring(0, loginId.indexOf("_"));
		}

		String prefix = "";
		for (int i = 0; i < DEFAULT_NAME_LEN - loginId.length(); i++) {
			prefix += "0";
		}
		return prefix + loginId;
	}
}
