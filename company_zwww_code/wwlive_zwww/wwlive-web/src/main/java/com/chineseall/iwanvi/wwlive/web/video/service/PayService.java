package com.chineseall.iwanvi.wwlive.web.video.service;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

public interface PayService {
	
	public Map<String, Object> toPay(HttpServletRequest request);

    //void afterPayMsg(HttpServletRequest request);
}
