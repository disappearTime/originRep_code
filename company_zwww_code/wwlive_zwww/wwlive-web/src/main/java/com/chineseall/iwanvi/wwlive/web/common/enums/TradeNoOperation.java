package com.chineseall.iwanvi.wwlive.web.common.enums;

import javax.servlet.http.HttpServletRequest;

public interface TradeNoOperation {
	
	public String getSequenceName();
	
	public String getPrefix();
	
	public String getRedisKey();
	
	public String getPayBeanName();
	
	public void prePayEmbed(HttpServletRequest request);
	
//	public void payEmbed(HttpServletRequest request);
}
