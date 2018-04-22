package com.chineseall.iwanvi.wwlive.web.launch.service;

import java.util.Map;

public interface LaunchShareService {
	public Map<String, String> getShareInfo(Integer type, Long anchorId,
			Long id, Integer shareKind);
}
