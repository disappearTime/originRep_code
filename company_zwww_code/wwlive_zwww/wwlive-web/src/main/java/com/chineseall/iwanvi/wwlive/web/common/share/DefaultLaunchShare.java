package com.chineseall.iwanvi.wwlive.web.common.share;

import java.util.Map;

public abstract class DefaultLaunchShare {

	public abstract Map<String, String> getShareInfo(Long userId, Long id , Integer shareKind);
	
}
