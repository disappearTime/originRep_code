package com.chineseall.iwanvi.wwlive.web.common.helper;

import java.util.Date;
import java.util.Map;

import org.apache.commons.lang.time.DateFormatUtils;

import com.chineseall.iwanvi.wwlive.common.cache.RedisClientAdapter;
import com.chineseall.iwanvi.wwlive.common.cache.RedisExpireTime;
import com.chineseall.iwanvi.wwlive.common.cache.RedisKey;
import com.chineseall.iwanvi.wwlive.common.constants.Constants;
import com.chineseall.iwanvi.wwlive.dao.wwlive.LiveVideoInfoMapper;
import com.chineseall.iwanvi.wwlive.domain.wwlive.LiveVideoInfo;

public class AnchorLastVideoForHistoryCacheHelper {

	public static Map<String, String> cacheAnchorLastVideo(
			LiveVideoInfoMapper liveVideoInfoMapper,
			RedisClientAdapter redisAdapter, long anchorId) {

		String key = RedisKey.ANCHOR_LIVED_VIDEO_ + anchorId;
		LiveVideoInfo video = liveVideoInfoMapper
				.getLastLivedVideoByAnchorId(anchorId);
		String today = DateFormatUtils.format(new Date(),
				Constants.YY_MM_DD);
		String tmpKey = RedisKey.VideoKeys.LIVED_VIDEOS_ + today;
		if (video != null) {
			String videoKey = RedisKey.LIVE_VIDEO_INFO_ + video.getVideoId();
			if (redisAdapter.existsKey(videoKey)) {
                Map<String, Object> map = video.putFieldValueToMap();
                map.put("anchorName",liveVideoInfoMapper.getAnchorNameById(anchorId));
                redisAdapter.hashMSet(videoKey, map);
				redisAdapter.expireKey(videoKey, RedisExpireTime.EXPIRE_DAY_7);
			}
			// set
			redisAdapter.strSetByNormal(key, video.getVideoId().toString());// 删除后回放时间暂时不动
			if (redisAdapter.existsKey(tmpKey)) {
        		double s = (double)(video.getCreateTime().getTime() - Constants.HISTORY_VIDEO_REDUCE_TIME);
        		redisAdapter.zsetAdd(tmpKey, s, video.getAnchorId().toString());
				
			}
			return video.putFieldValueToStringMap();
		} else {
			redisAdapter.delKeys(key);
			// del
			if (redisAdapter.existsKey(tmpKey)) {
				redisAdapter.zsetRem(tmpKey, anchorId + "");// 存的主播id
			}
			return null;
		}

	}
}
