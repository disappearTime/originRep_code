package com.chineseall.iwanvi.wwlive.pc.video.service;

import java.util.Map;

public interface UserInfoService {
	/**
	 * 根据用户主键获得用户的头像、昵称、贡献值、性别、生日、年龄、星座
	 * @param userId
	 * @return
	 */
    public Map<String, Object> getUserInfo(Long anchorId, Long userId);
    
    /**
     * 根据用户登录id获得用户的头像、昵称、贡献值、性别、生日、年龄、星座，根据主播id获得是否做过贡献
     * @param loginId
     * @param anchorId
     * @return
     */
    public Map<String, Object> getUserInfo(String loginId, long anchorId);
    
    /**
     * userId用户是否在此videoId为黑名单成员
     * @param videoId
     * @param userId
     * @return
     */
	public Map<String, Object> isBlackOrAdmin(long anchorId, int videoId, int userId);

    public Map<String, Object> setAdmin(String chatRoomId, Long anchorId, Long userId);

    public Map<String, Object> removeAdmin(String chatRoomId, long anchorId, Long userId);
}
