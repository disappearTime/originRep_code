package com.chineseall.iwanvi.wwlive.pc.common.helper;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.chineseall.iwanvi.wwlive.common.cache.RedisClientAdapter;
import com.chineseall.iwanvi.wwlive.common.cache.RedisExpireTime;
import com.chineseall.iwanvi.wwlive.common.cache.RedisKey;
import com.chineseall.iwanvi.wwlive.common.constants.Constants;
import com.chineseall.iwanvi.wwlive.dao.wwlive.LiveAdminMapper;
import com.chineseall.iwanvi.wwlive.domain.wwlive.LiveAdmin;

public class LiveAdminHelper {

	static final Logger LOGGER = Logger.getLogger(LiveAdminHelper.class);

	public static LiveAdmin getAndCacheAdminInfo(
			RedisClientAdapter redisAdapter, LiveAdminMapper liveAdminMapper,
			Long anchorId, Long userId) {
		if (redisAdapter == null || liveAdminMapper == null || anchorId == 0) {
			return null;
		}
		LiveAdmin admin = liveAdminMapper.getByUserIdAndAnchorId(userId,
				anchorId);
		if (admin != null) {
			// 添加用户信息到redis中
			String adminKey = RedisKey.LIVE_ADMIN_INFO_ + anchorId
					+ Constants.UNDERLINE + userId;
			redisAdapter.hashMSet(adminKey, admin.putFieldValueToMap());
			redisAdapter.expireKey(adminKey, RedisExpireTime.EXPIRE_DAY_30);
		} else {
			LOGGER.error("主播：" + anchorId + ", 房管" + userId + "不存在。");
		}
		return admin;
	}

	public static LiveAdmin getAdminInfoCache (
			RedisClientAdapter redisAdapter, LiveAdminMapper liveAdminMapper,
			Long anchorId, Long userId) {
		if (redisAdapter == null || liveAdminMapper == null || anchorId == 0) {
			return null;
		}
		LiveAdmin admin = liveAdminMapper.getByUserIdAndAnchorId(userId,
				anchorId);
		if (admin != null) {
			// 添加用户信息到redis中
			String adminKey = RedisKey.LIVE_ADMIN_INFO_ + anchorId
					+ Constants.UNDERLINE + userId;
			redisAdapter.hashMSet(adminKey, admin.putFieldValueToMap());
			redisAdapter.expireKey(adminKey, RedisExpireTime.EXPIRE_DAY_30);
		} else {
			LOGGER.error("主播：" + anchorId + ", 房管" + userId + "不存在。");
		}
		return admin;
	}
	
	public static boolean isAdmin(RedisClientAdapter redisAdapter,
			LiveAdminMapper liveAdminMapper, Long anchorId, Long userId) {
		if (redisAdapter == null || liveAdminMapper == null || anchorId == 0) {
			return false;
		}
		String adminKey = RedisKey.LIVE_ADMIN_INFO_ + anchorId
				+ Constants.UNDERLINE + userId;
		if (redisAdapter.existsKey(adminKey)) {
			Map<String, String> statusM = redisAdapter.hashMGet(adminKey,
					"adminStatus");
			String status = statusM.get("adminStatus");
			if ("0".equals(status)) {
				return true;
			}
		} else {
			LiveAdmin admin = getAndCacheAdminInfo(redisAdapter,
					liveAdminMapper, anchorId, userId);
			if (admin != null && admin.getAdminStatus() == 0) {
				return true;
			}
			//当该用户不是房管时也放到相应的hash作为缓存，用来作为非房管标志
			Map<String, String> statusM = new HashMap<String, String>();
			statusM.put("adminStatus", "1");
			redisAdapter.hashMSet(adminKey, statusM);
			redisAdapter.expireKey(adminKey, RedisExpireTime.EXPIRE_DAY_30);
		}
		return false;
	}

}
