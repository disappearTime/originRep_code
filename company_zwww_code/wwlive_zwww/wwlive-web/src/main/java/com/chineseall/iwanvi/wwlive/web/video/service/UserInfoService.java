package com.chineseall.iwanvi.wwlive.web.video.service;

import com.chineseall.iwanvi.wwlive.common.exception.IWanviException;
import com.chineseall.iwanvi.wwlive.domain.wwlive.UserInfo;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;


public interface UserInfoService {

    Map<String, Object> getUserInfoByLoinId(String loginId, String anchorIdStr, String userIdStr);

    Map<String, Object> getExpenseList(String loginId, int pageNum, int pageSize);

    Map<String, Object> getConsume(String loginId, int pageNum, int pageSize);

    public String getUserLoginId(long userId);

    /**
     * 如果是创新版用户，通过创新版url获得用户信息
     *
     * @param uid 外部源用户id
     */
    public UserInfo checkIsExist(String uid) throws IWanviException;

    public long synchroVirtualCurrency(long userId, String loginId) throws IWanviException;

    public Map<String, Object> getRongInf(long userId);

    /**
     * 禁言某用户，当超管用户禁言某用户时则为永久禁言
     *
     * @param videoId
     * @param userId
     * @param anchorId
     * @param loginId
     * @param duration
     * @return
     */
    Map<String, Object> mute(Long videoId, Long userId, Long anchorId, String loginId, int duration);

    /**
     * 根据userId获取用户信息
     *
     * @param userId
     * @return
     */
    Map<String, Object> getUserInfoByUserId(String userId,String version);

    /**
     * 更新用户信息
     * @param request
     * @return
     */
    Map<String, Object> updateUserInfoByUserId(HttpServletRequest request);

}
