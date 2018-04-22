package com.chineseall.iwanvi.wwlive.web.common.helper;

import java.text.ParseException;
import java.util.Map;

import org.apache.log4j.Logger;

import com.chineseall.iwanvi.wwlive.common.cache.RedisClientAdapter;
import com.chineseall.iwanvi.wwlive.common.cache.RedisExpireTime;
import com.chineseall.iwanvi.wwlive.common.cache.RedisKey;
import com.chineseall.iwanvi.wwlive.dao.wwlive.LiveVideoInfoMapper;
import com.chineseall.iwanvi.wwlive.domain.wwlive.LiveVideoInfo;

public class LiveVideoInfoHelper {

    private static final Logger LOGGER = Logger.getLogger(LiveVideoInfoHelper.class);
	
	public static LiveVideoInfo getVideoInfoCache(RedisClientAdapter redisAdapter, 
			LiveVideoInfoMapper liveVideoInfoMapper, long videoId, String... fields) {

		String key = RedisKey.LIVE_VIDEO_INFO_ + videoId;
		
		Map<String, String> videoMap = redisAdapter.hashMGet(key, fields);
		if (videoMap == null || videoMap.isEmpty()) {
			return null;
		}
		LiveVideoInfo info = new LiveVideoInfo();
		try {
			info.doStringMapToValue(videoMap);
			redisAdapter.expireKey(key, RedisExpireTime.EXPIRE_DAY_7);
		} catch (ParseException e) {
			e.printStackTrace();
        	LOGGER.error("视频直播转换异常(ParseException)：" + e.toString());
		}
		
		return info;
	}

	public static LiveVideoInfo getAndCacheVideoInfo(RedisClientAdapter redisAdapter, 
			LiveVideoInfoMapper liveVideoInfoMapper, long videoId) {
		if (redisAdapter == null 
				|| liveVideoInfoMapper == null || videoId == 0) {
			return null;
		}
		LiveVideoInfo info = liveVideoInfoMapper.findVideoInfoById(videoId);
		String key = RedisKey.LIVE_VIDEO_INFO_ + videoId;
		if (info != null) {
            Map<String, Object> map = info.putFieldValueToMap();
            map.put("anchorName",liveVideoInfoMapper.getAnchorNameById(info.getAnchorId()));
            redisAdapter.hashMSet(key, map);
			redisAdapter.expireKey(key, RedisExpireTime.EXPIRE_DAY_7);
		}
		return info;
	}
	
	public static Map<String, String> getAndCacheVideoInfoStringMap(RedisClientAdapter redisAdapter, 
			LiveVideoInfoMapper liveVideoInfoMapper, long videoId) {
		if (redisAdapter == null 
				|| liveVideoInfoMapper == null || videoId == 0) {
			return null;
		}
		LiveVideoInfo info = liveVideoInfoMapper.findVideoInfoById(videoId);
		if (info != null) {
			String key = RedisKey.LIVE_VIDEO_INFO_ + videoId;
			Map<String, String> map_ = info.putFieldValueToStringMap();
            String anchorName = liveVideoInfoMapper.getAnchorNameById(info.getAnchorId());
            map_.put("anchorName",anchorName);
            redisAdapter.hashMSet(key, map_);
			redisAdapter.expireKey(key, RedisExpireTime.EXPIRE_DAY_7);
			return map_;
		}
		return null;
	}
	
}
