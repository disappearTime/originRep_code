package com.chineseall.iwanvi.wwlive.web.video.service;

import java.util.Map;

import com.chineseall.iwanvi.wwlive.web.video.vo.LiveVideoInfoVo;

public interface LiveVideoInfo2Service {

	public LiveVideoInfoVo getLiveVideoInfo(long userId, long videoId, String loginId ,String cnid,String version);
	
	public int leaveLiveVideo(long userId, long videoId);

	public Map<String, Object> getWatchingVideoNobles(long videoId);
	
	LiveVideoInfoVo liveAdvert(LiveVideoInfoVo vo, String cnid, String version);
	
}
