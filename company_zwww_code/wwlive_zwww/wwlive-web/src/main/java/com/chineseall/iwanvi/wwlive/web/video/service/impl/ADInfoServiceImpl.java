package com.chineseall.iwanvi.wwlive.web.video.service.impl;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.chineseall.iwanvi.wwlive.common.cache.RedisClientAdapter;
import com.chineseall.iwanvi.wwlive.common.cache.RedisExpireTime;
import com.chineseall.iwanvi.wwlive.common.cache.RedisKey;
import com.chineseall.iwanvi.wwlive.dao.wwlive.LiveVideoInfoMapper;
import com.chineseall.iwanvi.wwlive.domain.wwlive.LiveVideoInfo;
import com.chineseall.iwanvi.wwlive.web.common.helper.LiveVideoInfoHelper;
import com.chineseall.iwanvi.wwlive.web.video.service.ADInfoService;

@Service
public class ADInfoServiceImpl implements ADInfoService {

	private static final Logger LOGGER = Logger.getLogger(ADInfoServiceImpl.class);
	
	@Autowired
	private LiveVideoInfoMapper liveVideoInfoMapper;

	@Autowired
	private RedisClientAdapter redisAdapter;

	/**
	 * 获得主播的最近视频信息
	 * @param anchorId
	 * @return
	 */
	public Map<String, Object> getVideoInfoByAnchorId(Long anchorId) {
		String key = RedisKey.ANCHOR_VIDEO_ + anchorId;
		Map<String, Object> videoInfo = new HashMap<String, Object>();
		if (redisAdapter.existsKey(key)) {
			String vidoIdStr = redisAdapter.strGet(key);
			if (StringUtils.isNotBlank(vidoIdStr) && vidoIdStr.contains("\"")) {
				vidoIdStr = vidoIdStr.replace("\"", "");
				redisAdapter.strSetByNormal(key, vidoIdStr);
			}
			String tmpKey = RedisKey.LIVE_VIDEO_INFO_ + vidoIdStr;
			if (redisAdapter.existsKey(tmpKey)) {
				videoInfo.putAll(redisAdapter.hashMGet(tmpKey, "videoId", "chatroomId", "anchorId",
						"formatType", "coverImg", "videoStatus"));
			} else {
				try {
					Long videoId = Long.parseLong(vidoIdStr);
					if(videoId.longValue() != 0) {
						videoInfo.putAll(LiveVideoInfoHelper.getAndCacheVideoInfoStringMap(redisAdapter, liveVideoInfoMapper, videoId));
					}
				} catch (Exception e) {
					LOGGER.error("获取直播信息异常：", e);
				}
			}
		} else {
			Long videoId = new Long(0);
			LiveVideoInfo video = liveVideoInfoMapper
					.findBannerVideoInfoByAnchorId(anchorId);
			if (video != null) {
				Map<String, Object> map_ = video.putFieldValueToMap();
				map_.put("anchorName",liveVideoInfoMapper.getAnchorNameById(anchorId));
				videoInfo.putAll(map_);
				redisAdapter.hashMSet(RedisKey.LIVE_VIDEO_INFO_  + video.getVideoId(), map_);
				redisAdapter.expireKey(RedisKey.LIVE_VIDEO_INFO_  + video.getVideoId(), RedisExpireTime.EXPIRE_HOUR_5);
				videoId = video.getVideoId();
			}
			redisAdapter.strSetByNormal(key, videoId.toString());
			redisAdapter.expireKey(key, RedisExpireTime.EXPIRE_DAY_5);
		}
		return videoInfo;
	}

}
