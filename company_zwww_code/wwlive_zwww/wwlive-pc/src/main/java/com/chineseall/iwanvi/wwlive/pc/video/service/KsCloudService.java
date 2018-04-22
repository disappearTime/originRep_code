package com.chineseall.iwanvi.wwlive.pc.video.service;

public interface KsCloudService {
	
	/**
	 * 视频开始通知
	 * @param streamName
	 * @param originStreamName
	 */
	public void noticeLiveStart(String streamName);
	
	public void noticeLiveStop(String streamName);
}
