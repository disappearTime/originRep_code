package com.chineseall.iwanvi.wwlive.web.video.service;

import java.util.Date;

import com.chineseall.iwanvi.wwlive.domain.wwlive.UserInfo;

public interface LoginService {

	@Deprecated
	public Long saveUserInfo(String loginId, String userName, String headImg, int sex,
			int origin, Date birthday);
	
	/**
	 * 根据用户信息更新用户信息
	 * @param userInfo
	 * @return
	 */
	public Long upsertUserInfo(UserInfo userInfo);
	

}
