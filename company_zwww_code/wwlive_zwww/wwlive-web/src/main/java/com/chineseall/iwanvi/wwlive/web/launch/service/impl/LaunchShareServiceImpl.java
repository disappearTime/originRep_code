package com.chineseall.iwanvi.wwlive.web.launch.service.impl;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.chineseall.iwanvi.wwlive.web.common.enums.share.ShareType;
import com.chineseall.iwanvi.wwlive.web.common.share.DefaultLaunchShare;
import com.chineseall.iwanvi.wwlive.web.common.util.SpringContextUtils;
import com.chineseall.iwanvi.wwlive.web.launch.service.LaunchShareService;

@Service
public class LaunchShareServiceImpl implements LaunchShareService{

	public Map<String, String> getShareInfo(Integer type, Long anchorId,
			Long id, Integer exId) {
		ShareType shareType = ShareType.getShareType(type);
		if (shareType == null) {
			return new HashMap<String, String>();
		}

		DefaultLaunchShare share = (DefaultLaunchShare) SpringContextUtils.getBean(shareType.getBeanName());
		return share.getShareInfo(anchorId, id, exId);
	}

}
