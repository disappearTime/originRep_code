package com.chineseall.iwanvi.wwlive.web.common.helper;

import java.util.List;
import java.util.Map;

import com.chineseall.iwanvi.wwlive.common.cache.RedisClientAdapter;
import com.chineseall.iwanvi.wwlive.common.cache.RedisExpireTime;
import com.chineseall.iwanvi.wwlive.common.cache.RedisKey;
import com.chineseall.iwanvi.wwlive.dao.wwlive.BlackListMapper;

/**
 * 黑名单辅助工具
 * @author DIKEPU
 *
 */
public class BlackListHelper {
	
	public static boolean isOnBlackList(BlackListMapper blackListMapper, RedisClientAdapter redisAdapter, Long userId) {

		if ("0".equals(redisAdapter.strGet(RedisKey.BLACK_LIST_FOREVER_ + userId))) {
			return true;
		}else {
			List<Map<String, Object>> list = blackListMapper.findBlackStatusByUserId(userId);
			Object status = null;
			if (list != null && !list.isEmpty()) {
				status = list.get(0).get("status");
			}
			if (status != null) {
				Integer s =(Integer) status;
				if (s.intValue() == 0) {
        			redisAdapter.strSetexByNormal(RedisKey.BLACK_LIST_FOREVER_ + userId, RedisExpireTime.EXPIRE_DAY_30, "0");
        			return true;
				} else {
        			redisAdapter.strSetexByNormal(RedisKey.BLACK_LIST_FOREVER_ + userId, RedisExpireTime.EXPIRE_DAY_30, "1");
				}
			}
		}
		return false;
	}
	
	
}
