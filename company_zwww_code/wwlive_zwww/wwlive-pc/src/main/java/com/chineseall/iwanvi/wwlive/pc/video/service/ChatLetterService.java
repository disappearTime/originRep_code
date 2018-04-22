package com.chineseall.iwanvi.wwlive.pc.video.service;

import java.util.Map;

import com.chineseall.iwanvi.wwlive.pc.common.Page;

/**
 * 私信通知
 * @author DIKEPU
 *
 */
public interface ChatLetterService {

	/**
	 * 获得私信列表
	 * @param page
	 * @param anchorId
	 * @return
	 */
	public Map<String, Object> getLetters(Page page, Long anchorId);
	
	/**
	 * 获得未读私信数量
	 * @param anchorId
	 * @return
	 */
	public String getNoReadLetterNum(Long anchorId);
	
}
