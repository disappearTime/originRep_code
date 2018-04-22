package com.chineseall.iwanvi.wwlive.web.launch.service;

import com.chineseall.iwanvi.wwlive.domain.wwlive.Anchor;

public interface LoginService {

	/**
	 * 主播登录
	 * @param passport
	 * @param passwd
	 * @return
	 */
	public Anchor doLogin(String passport, String passwd);
	
}
