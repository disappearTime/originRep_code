package com.chineseall.iwanvi.wwlive.pc.common.helper;

import java.util.Date;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateFormatUtils;

import com.chineseall.iwanvi.wwlive.common.cache.RedisClientAdapter;
import com.chineseall.iwanvi.wwlive.common.cache.RedisExpireTime;
import com.chineseall.iwanvi.wwlive.common.cache.RedisKey;
import com.chineseall.iwanvi.wwlive.common.constants.Constants;
import com.chineseall.iwanvi.wwlive.dao.wwlive.LiveVideoInfoMapper;
import com.chineseall.iwanvi.wwlive.domain.wwlive.LiveVideoInfo;

public class AnchorLastVideoForHistoryCacheHelper {

	/**
	 * 取主播最后一个视频放入到回放列，请保持与manage的AnchorLastVideoForHistoryCacheHelper cacheAnchorLastVideo方法一致
	 * @param liveVideoInfoMapper
	 * @param redisAdapter
	 * @param videoId
	 * @param anchorId
	 */
	public static void cacheAnchorLastVideo4Normal(
			LiveVideoInfoMapper liveVideoInfoMapper,
			RedisClientAdapter redisAdapter, long videoId, long anchorId) {
		String today = DateFormatUtils.format(new Date(),
				Constants.YY_MM_DD);
		String tmpKey = RedisKey.VideoKeys.LIVED_VIDEOS_ + today;
		cacheAnchorLastVideo(liveVideoInfoMapper, redisAdapter, videoId, anchorId, tmpKey);
	}
	
	/**
	 * 与manage的AnchorLastVideoForHistoryCacheHelper cacheAnchorLastVideo方法一致
	 * @param liveVideoInfoMapper
	 * @param redisAdapter
	 * @param videoId
	 * @param anchorId
	 */
	public static void cacheAnchorLastVideo4Gray(
			LiveVideoInfoMapper liveVideoInfoMapper,
			RedisClientAdapter redisAdapter, long videoId, long anchorId) {

		String today = DateFormatUtils.format(new Date(),
				Constants.YY_MM_DD);
		String tmpKey = RedisKey.VideoGrayKeys.LIVED_VIDEOS_ + today;
		cacheAnchorLastVideo(liveVideoInfoMapper, redisAdapter, videoId, anchorId, tmpKey);
	}
	
	private static void cacheAnchorLastVideo(LiveVideoInfoMapper liveVideoInfoMapper,
			RedisClientAdapter redisAdapter, long videoId, long anchorId, String tmpKey) {
		String videoKey = RedisKey.LIVE_VIDEO_INFO_ + videoId;

		/*
		 * 视频回放列表，如果删除的是近7天的回放，则触发下面的逻辑
		 */
		String key = RedisKey.ANCHOR_LIVED_VIDEO_ + anchorId;
		String cache = redisAdapter.strGet(key);
		if (StringUtils.isNotBlank(cache) && cache.equals(videoId + "")) {
			LiveVideoInfo video = liveVideoInfoMapper
					.getLastLivedVideoByAnchorId(anchorId);
			if (video != null) {
				videoKey = RedisKey.LIVE_VIDEO_INFO_ + video.getVideoId();
				if (redisAdapter.existsKey(videoKey)) {
                    Map<String, Object> map = video.putFieldValueToMap();
                    map.put("anchorName",liveVideoInfoMapper.getAnchorNameById(video.getAnchorId()));
                    redisAdapter.hashMSet(videoKey, map);
					redisAdapter.expireKey(videoKey,
							RedisExpireTime.EXPIRE_DAY_7);
				}
				// set
				redisAdapter.strSetByNormal(key, video.getVideoId().toString());// 删除后回放时间暂时不动
				if (redisAdapter.existsKey(tmpKey)) {
					double s = (double) (video.getCreateTime().getTime() - Constants.HISTORY_VIDEO_REDUCE_TIME);
					redisAdapter.zsetAdd(tmpKey, s, video.getAnchorId()
							.toString());

				}
			} else {
				redisAdapter.delKeys(key);
				// del
				if (redisAdapter.existsKey(tmpKey)) {
					redisAdapter.zsetRem(tmpKey, anchorId + "");// 存的主播id
				}
			}

		}
	}
	
	/**
	 * 加入到回放H5缓存中
	 * 
	 * @param info
	 */
	public static void cacheAnchorLastHistoryVideo4TabH5(
			LiveVideoInfoMapper liveVideoInfoMapper,
			RedisClientAdapter redisAdapter, LiveVideoInfo info) {
		String cache = redisAdapter.strGet(RedisKey.ANCHOR_LIVED_VIDEO_
				+ info.getAnchorId());
		Date now = new Date();
		if (StringUtils.isNotBlank(cache) && !"0".equals(cache)) {//0 该主播近7天未播过
			if (!cache.equals(info.getVideoId().toString())) {
				String today = DateFormatUtils.format(now, Constants.YY_MM_DD);
				String tmpKey = RedisKey.VideoKeys.LIVED_VIDEOS_ + today;
				if (!redisAdapter.existsKey(tmpKey)) {
					return;
				}
				Double score = redisAdapter.zsetScore(tmpKey, info
						.getAnchorId().toString());
				double s = (double) (info.getCreateTime().getTime() - Constants.HISTORY_VIDEO_REDUCE_TIME);
				if (score == null || score.doubleValue() < s) {
					redisAdapter.zsetAdd(tmpKey, s, info.getAnchorId()
							.toString());
					//灰度代码
					redisAdapter.zsetAdd(RedisKey.VideoGrayKeys.LIVED_VIDEOS_ + today, s, info.getAnchorId()
							.toString());
					redisAdapter.strSetexByNormal(
							RedisKey.ANCHOR_LIVED_VIDEO_ + info.getAnchorId(), RedisExpireTime.EXPIRE_DAY_7, info
									.getVideoId().toString());
				}
			}
		} else {
			// 最近的视频
			String today = DateFormatUtils.format(now, Constants.YY_MM_DD);
			String tmpKey = RedisKey.VideoKeys.LIVED_VIDEOS_ + today;
			if (redisAdapter.existsKey(tmpKey)) {
				double score = (double) (info.getCreateTime().getTime() - Constants.HISTORY_VIDEO_REDUCE_TIME);
				redisAdapter.zsetAdd(tmpKey, score, info.getAnchorId().toString());
				
				//灰度代码
				redisAdapter.zsetAdd(RedisKey.VideoGrayKeys.LIVED_VIDEOS_ + today, score, info.getAnchorId().toString());
				redisAdapter.strSetexByNormal(
					RedisKey.ANCHOR_LIVED_VIDEO_ + info.getAnchorId(), RedisExpireTime.EXPIRE_DAY_7,info
							.getVideoId().toString());
			}
		}
		String key = RedisKey.ANCHOR_VIDEO_ + info.getAnchorId();// 主播最近视频key
		redisAdapter.delKeys(key);
	}
	

	/**
	 * 加入到回放H5缓存中
	 * 
	 * @param info
	 */
	public static void cacheAnchorLastHistoryVideo4GrayTabH5(
			LiveVideoInfoMapper liveVideoInfoMapper,
			RedisClientAdapter redisAdapter, LiveVideoInfo info) {
		String cache = redisAdapter.strGet(RedisKey.ANCHOR_LIVED_VIDEO_
				+ info.getAnchorId());
		Date now = new Date();
		if (StringUtils.isNotBlank(cache) && !"0".equals(cache)) {//0 该主播近7天未播过
			if (!cache.equals(info.getVideoId().toString())) {
				String today = DateFormatUtils.format(now, Constants.YY_MM_DD);
				String tmpKey = RedisKey.VideoGrayKeys.LIVED_VIDEOS_ + today;
				if (!redisAdapter.existsKey(tmpKey)) {
					return;
				}
				Double score = redisAdapter.zsetScore(tmpKey, info
						.getAnchorId().toString());
				double s = (double) (info.getCreateTime().getTime() - Constants.HISTORY_VIDEO_REDUCE_TIME);
				if (score == null || score.doubleValue() < s) {
					redisAdapter.zsetAdd(tmpKey, s, info.getAnchorId()
							.toString());
					redisAdapter.strSetexByNormal(
							RedisKey.ANCHOR_LIVED_VIDEO_ + info.getAnchorId(), RedisExpireTime.EXPIRE_DAY_7, info
									.getVideoId().toString());
				}
			}
		} else {
			// 最近的视频
			String today = DateFormatUtils.format(now, Constants.YY_MM_DD);
			String tmpKey = RedisKey.VideoGrayKeys.LIVED_VIDEOS_ + today;
			if (redisAdapter.existsKey(tmpKey)) {
				double score = (double) (info.getCreateTime().getTime() - Constants.HISTORY_VIDEO_REDUCE_TIME);
				redisAdapter.zsetAdd(tmpKey, score, info.getAnchorId().toString());
				redisAdapter.strSetexByNormal(
						RedisKey.ANCHOR_LIVED_VIDEO_ + info.getAnchorId(), RedisExpireTime.EXPIRE_DAY_7, info
							.getVideoId().toString());
			}
		}
		String key = RedisKey.ANCHOR_VIDEO_ + info.getAnchorId();// 主播最近视频key
		redisAdapter.delKeys(key);
	}
	
}
