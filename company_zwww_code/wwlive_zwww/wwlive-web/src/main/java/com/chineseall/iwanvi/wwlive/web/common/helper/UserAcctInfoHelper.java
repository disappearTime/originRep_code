package com.chineseall.iwanvi.wwlive.web.common.helper;

import java.util.HashMap;
import java.util.Map;

import com.chineseall.iwanvi.wwlive.common.cache.RedisClientAdapter;
import com.chineseall.iwanvi.wwlive.common.cache.RedisExpireTime;
import com.chineseall.iwanvi.wwlive.common.cache.RedisKey;
import com.chineseall.iwanvi.wwlive.dao.wwlive.AcctInfoMapper;

public class UserAcctInfoHelper {
	
	/**
	 * 获得用户钻石数（元作单位）和贡献值
	 * @param acctInfoMapper
	 * @param redisAdapter
	 * @param userId
	 * @return
	 */
	public static Map<String, Object> getUserAcctInfo(AcctInfoMapper acctInfoMapper, 
			RedisClientAdapter redisAdapter, long userId) {
		Map<String, Object> result = new HashMap<String, Object>();
		String key = RedisKey.USER_CTB_DO_ + userId;
		if (redisAdapter.existsKey(key)) {
			Map<String, String> info = redisAdapter.hashGetAll(key);
			result.putAll(info);
			return result;
		} else {
			result = acctInfoMapper.getAcctInfo(userId);
			if (result == null || result.isEmpty()) {
				result.put("diamond", 0.0D);
				result.put("contrib", 0L);
			}
			redisAdapter.hashMSet(key, result);
			redisAdapter.expireKey(key, RedisExpireTime.EXPIRE_DAY_1);
			return result;
		}
	}

	/**
	 * 获得用户的coin值
	 * @param acctInfoMapper
	 * @param redisAdapter
	 * @param userId
	 * @return
	 */
	public static Map<String, Object> getAcctInfoCoin(AcctInfoMapper acctInfoMapper, 
			RedisClientAdapter redisAdapter, long userId) {
		Map<String, Object> result = new HashMap<String, Object>();
		String key = RedisKey.USER_COIN_ + userId;
		if (redisAdapter.existsKey(key)) {
			Map<String, String> info = redisAdapter.hashGetAll(key);
			result.putAll(info);
			return result;
		} else {
			result = acctInfoMapper.getAcctInfoCoin(userId);
			if (result == null || result.isEmpty()) {
				result = new HashMap<String, Object>();
				result.put("coin", 0L);
			}
			redisAdapter.hashMSet(key, result);
			redisAdapter.expireKey(key, RedisExpireTime.EXPIRE_HOUR_5);
			return result;
		}
	}
	
}
