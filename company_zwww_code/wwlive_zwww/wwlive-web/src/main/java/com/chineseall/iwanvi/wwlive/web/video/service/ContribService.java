package com.chineseall.iwanvi.wwlive.web.video.service;

public interface ContribService {

    /**
     * 根据主播id和当前用户的贡献值计算此用户对此主播的贡献值排名
     * @param anchorId
     * @param contribution
     * @return
     */
    int getRank(int anchorId, int contribution);

    /**
     * 根据用户id和主播id获得该用户对该主播的贡献值
     * @param userId
     * @param anchorId
     * @return
     */
    int getContrib(int userId, int anchorId);

}
