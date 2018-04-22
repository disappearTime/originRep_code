package com.chineseall.iwanvi.wwlive.web.common.helper;

import com.service.impl.FollowAnchorServiceImpl;
import com.service.impl.FollowNoticeServiceImpl;

/**
 * 请求唐勇的程序发送收礼通知
 * @author DIKEPU
 *
 */
public class FolloServiceSingleton {

	private static FollowNoticeServiceImpl followNoticeService = new FollowNoticeServiceImpl();
	private static FollowAnchorServiceImpl followAnchorService = new FollowAnchorServiceImpl();
	
	public static FollowNoticeServiceImpl getInstance() {
		return followNoticeService;
	}

	public static FollowAnchorServiceImpl getFollowAnchorServiceInstance() {
		return followAnchorService;
	}
}
