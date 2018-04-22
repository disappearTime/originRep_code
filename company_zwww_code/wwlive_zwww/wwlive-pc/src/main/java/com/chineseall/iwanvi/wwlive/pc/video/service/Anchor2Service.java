package com.chineseall.iwanvi.wwlive.pc.video.service;

import java.util.Map;

import com.chineseall.iwanvi.wwlive.pc.common.Page;

public interface Anchor2Service {

	/**
	 * PC端登录获得主播信息，包含主播视频、收入
	 * @param passport
	 * @param passwd
	 * @return
	 */
	public Map<String, Object> anchorLogin(String passport, String passwd);
	

	public Map<String, Object> getAnchorIndexInfo(long anchorId);

	public Page getUserList(long anchorId, long videoId, Page page);

	public int addUserToBlack(long anchorId, long userId, int time);
	
	public int delUserToBlack(long anchorId, long userId);
	
}
