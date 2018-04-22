package com.chineseall.iwanvi.wwlive.web.common.helper;

import java.math.BigInteger;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.commons.lang.time.DateUtils;
import org.apache.log4j.Logger;

import com.chineseall.iwanvi.wwlive.common.cache.RedisClientAdapter;
import com.chineseall.iwanvi.wwlive.common.cache.RedisExpireTime;
import com.chineseall.iwanvi.wwlive.common.cache.RedisKey;
import com.chineseall.iwanvi.wwlive.common.constants.Constants;
import com.chineseall.iwanvi.wwlive.dao.wwlive.LiveVideoInfoMapper;
import com.chineseall.iwanvi.wwlive.domain.wwlive.LiveVideoInfo;

/**
 * livedVideoForCache方法要与manage模块的 {@link VideoHomePageJobsServiceImpl#livedVideoForCache VideoHomePageJobsServiceImpl.livedVideoForCache}保持一致
 * @author DIKEPU
 *
 */
public class LivedVideoForHistoryCacheHelper {

	private static final Logger LOGGER = Logger.getLogger(LivedVideoForHistoryCacheHelper.class);
	
	public static void livedVideosForCache(RedisClientAdapter redisAdapter, 
			LiveVideoInfoMapper liveVideoInfoMapper, String todayLivedKey, Date now) {
		LOGGER.info("-- livedVideoForCache start: ");
		try {
			if (redisAdapter.existsKey(todayLivedKey)) {
				return;
			}
			String deadline = DateFormatUtils.format(
					DateUtils.addDays(now, -7),
					DateFormatUtils.ISO_DATE_FORMAT.getPattern());
			List<Map<String, Object>> historyList = liveVideoInfoMapper
					.findHistoryVideosForCache(0, 100, deadline);
			cacheVideoList(redisAdapter, liveVideoInfoMapper, todayLivedKey, historyList);
		} catch (Exception e) {
			LOGGER.error("回放异常：", e);
		}

	}

	public static void livedVideosForGrayCache(RedisClientAdapter redisAdapter, 
			LiveVideoInfoMapper liveVideoInfoMapper, String todayLivedKey, Date now) {
		LOGGER.info("-- livedVideoForCache start: ");
		try {
			if (redisAdapter.existsKey(todayLivedKey)) {
				return;
			}
			String deadline = DateFormatUtils.format(
					DateUtils.addDays(now, -7),
					DateFormatUtils.ISO_DATE_FORMAT.getPattern());
			List<Map<String, Object>> historyList = liveVideoInfoMapper
					.findHistoryVideosForGrayCache(0, 100, deadline);
			cacheVideoList(redisAdapter, liveVideoInfoMapper, todayLivedKey, historyList);
		} catch (Exception e) {
			LOGGER.error("回放异常：", e);
		}

	}
	
	private static void cacheVideoList(RedisClientAdapter redisAdapter, 
			LiveVideoInfoMapper liveVideoInfoMapper, String tmpKey, List<Map<String, Object>> historyList) {
		if (historyList != null && !historyList.isEmpty()) {
			for (Map<String, Object> history : historyList) {
				BigInteger videoId = (BigInteger) history.get("videoId");
				BigInteger anchorId = (BigInteger) history.get("anchorId");
				Date createTime = (Date) history.get("createTime");
				if (!redisAdapter.existsKey(RedisKey.LIVE_VIDEO_INFO_
						+ videoId)) {
					cacheVideoInfo(redisAdapter, liveVideoInfoMapper,
							videoId.longValue());
				}
				double score = (double) (createTime.getTime() - Constants.HISTORY_VIDEO_REDUCE_TIME);
				redisAdapter.zsetAdd(tmpKey, score, anchorId.toString());
				redisAdapter
						.expireKey(tmpKey, RedisExpireTime.EXPIRE_DAY_1 + RedisExpireTime.EXPIRE_HOUR_1);

				String key = RedisKey.ANCHOR_LIVED_VIDEO_ + anchorId;
				redisAdapter.strSetByNormal(key, videoId.toString());
				redisAdapter.expireKey(key, RedisExpireTime.EXPIRE_DAY_7);
			}

		}
	}

	private static void cacheVideoInfo(RedisClientAdapter redisAdapter,
			LiveVideoInfoMapper liveVideoInfoMapper, long videoId) {
		if (redisAdapter == null || liveVideoInfoMapper == null || videoId == 0) {
			return;
		}
		LiveVideoInfo info = liveVideoInfoMapper.findVideoInfoById(videoId);
		String key = RedisKey.LIVE_VIDEO_INFO_ + videoId;
		if (info != null) {
            Map<String, Object> map = info.putFieldValueToMap();
            map.put("anchorName",liveVideoInfoMapper.getAnchorNameById(info.getAnchorId()));
            redisAdapter.hashMSet(key, map);
			redisAdapter.expireKey(key, RedisExpireTime.EXPIRE_DAY_7);
		}
	}
	
	
}
