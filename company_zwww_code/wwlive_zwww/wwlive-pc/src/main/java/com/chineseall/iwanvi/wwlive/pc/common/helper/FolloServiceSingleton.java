package com.chineseall.iwanvi.wwlive.pc.common.helper;

import com.service.impl.FollowNoticeServiceImpl;

/**
 * 请求唐勇的程序发送视频开始及结束通知
 * @author DIKEPU
 *
 */
public class FolloServiceSingleton {

	private static FollowNoticeServiceImpl followNoticeService = new FollowNoticeServiceImpl();
	
	public static FollowNoticeServiceImpl getInstance() {
		return followNoticeService;
	}
	
}
