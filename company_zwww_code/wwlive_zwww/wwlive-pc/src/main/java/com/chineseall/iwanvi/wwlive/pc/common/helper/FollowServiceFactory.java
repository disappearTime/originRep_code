package com.chineseall.iwanvi.wwlive.pc.common.helper;

import com.service.impl.FollowAnchorServiceImpl;

public class FollowServiceFactory {

	private static FollowAnchorServiceImpl followAnchorService = new FollowAnchorServiceImpl();
	
	public static FollowAnchorServiceImpl getFollowAnchorServiceInstance() {
		return followAnchorService;
	}
}
