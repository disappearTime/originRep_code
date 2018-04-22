package com.chineseall.iwanvi.wwlive.web.common.helper;

import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.chineseall.iwanvi.wwlive.common.cache.RedisClientAdapter;
import com.chineseall.iwanvi.wwlive.common.cache.RedisExpireTime;
import com.chineseall.iwanvi.wwlive.common.cache.RedisKey;
import com.chineseall.iwanvi.wwlive.dao.wwlive.AnchorMapper;
import com.chineseall.iwanvi.wwlive.domain.wwlive.Anchor;

public class AnchorInfoHelper {
	
    static final Logger LOGGER = Logger.getLogger(AnchorInfoHelper.class);
    
	public static Anchor getAndCacheAnchorInfo(RedisClientAdapter redisAdapter, 
			AnchorMapper anchorMapper, long anchorId) {
		if (redisAdapter == null 
				|| anchorMapper == null || anchorId <= 0) {
			return null;
		}
        Anchor anchor = anchorMapper.findAnchorById(anchorId);        
        if (anchor != null) {
            // 添加用户信息到redis中
            redisAdapter.hashMSet(RedisKey.ANCHOR_INFO_ + anchorId, anchor.putFieldValueToStringMap());
    		redisAdapter.expireKey(RedisKey.ANCHOR_INFO_ + anchorId, RedisExpireTime.EXPIRE_DAY_30);
        } else {
        	LOGGER.error("主播：" + anchorId + "不存在。");
        }
        return anchor;
	}

	public static Anchor getAndCacheAnchorInfo(RedisClientAdapter redisAdapter, 
			AnchorMapper anchorMapper, long anchorId, String... fields) throws ParseException {
		if (redisAdapter == null 
				|| anchorMapper == null || anchorId <= 0) {
			return null;
		}
        Anchor anchor = anchorMapper.findAnchorById(anchorId);
        if (anchor != null) {
        	Map<String, String> anchorMap = anchor.putFieldValueToStringMap();
            // 添加用户信息到redis中
            redisAdapter.hashMSet(RedisKey.ANCHOR_INFO_ + anchorId, anchorMap);
    		redisAdapter.expireKey(RedisKey.ANCHOR_INFO_ + anchorId, RedisExpireTime.EXPIRE_DAY_30);
    		anchor = covertMap2Domain(anchorMap, fields);
        } else {
        	LOGGER.error("主播：" + anchorId + "不存在。");
        }
        return anchor;
	}
	
	private static Anchor covertMap2Domain(Map<String, String> anchorMap, String... fields) throws ParseException {
		if (fields == null || fields.length <= 0) {
			return new Anchor();
		}
		Anchor anchor = new Anchor();
		Map<String, String> map_ = new HashMap<String, String>();
		for (String field : fields) {
			map_.put(field, anchorMap.get(field));
		}
		anchor.doStringMapToValue(map_);
		return anchor;
	}

	public static Map<String, Object> getAndCacheAnchorMapInfo(RedisClientAdapter redisAdapter, 
			AnchorMapper anchorMapper, long anchorId, String... fields) {
		if (redisAdapter == null 
				|| anchorMapper == null || anchorId <= 0) {
			return null;
		}
        Anchor anchor = anchorMapper.findAnchorById(anchorId);
        Map<String, Object> result = null;
        if (anchor != null) {
            redisAdapter.hashMSet(RedisKey.ANCHOR_INFO_ + anchorId, anchor.putFieldValueToStringMap()); // 添加用户信息到redis中，putFieldValueToStringMap此方法解决了解析日期的问题
    		redisAdapter.expireKey(RedisKey.ANCHOR_INFO_ + anchorId, RedisExpireTime.EXPIRE_DAY_30);
    		result = covertMap2ResultMap(anchor.putFieldValueToMap(), fields);
        } else {
        	LOGGER.error("主播：" + anchorId + "不存在。");
        }
        return result;
	}
	
	private static Map<String, Object>  covertMap2ResultMap(Map<String, Object> anchorMap, String... fields) {
		if (fields == null || fields.length <= 0) {
			return new HashMap<String, Object>();
		}
		Map<String, Object> map_ = new HashMap<String, Object>();
		for (String field : fields) {
			map_.put(field, anchorMap.get(field));
		}
		return map_;
	}
}
