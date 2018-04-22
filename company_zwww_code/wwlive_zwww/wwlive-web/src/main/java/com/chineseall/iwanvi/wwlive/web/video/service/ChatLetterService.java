package com.chineseall.iwanvi.wwlive.web.video.service;

public interface ChatLetterService {
	
	/**
	 * 用户发送私信
	 * @param sendId
	 * @param receiveId
	 * @param videoId 用来发直播间私信通知
	 * @param content
	 * @return
	 */
	public int sendLetter(Long sendId, Long receiveId, Long videoId, String content);
	
}
