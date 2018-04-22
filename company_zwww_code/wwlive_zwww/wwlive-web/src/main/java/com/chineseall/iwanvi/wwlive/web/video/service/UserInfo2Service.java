package com.chineseall.iwanvi.wwlive.web.video.service;

import java.util.Map;

public interface UserInfo2Service {
	public boolean isInBlackList(int videoId, int userId);

	/**
	 * 获得用户昵称完成创新版任务
	 * @param userId
	 * @return
	 */
	public Map<String, String> getNameAndCompleteTask(Long userId);
	
	/**
	 * 修改用户昵称
	 * @param userId
	 * @param userName
	 * @return 返回code：0成功 1昵称重复 2用户问题
	 */
	public Map<String, Object> modifyName(Long userId, String userName);
}
