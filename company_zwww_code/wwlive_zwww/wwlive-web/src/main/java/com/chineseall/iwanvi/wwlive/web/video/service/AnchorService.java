package com.chineseall.iwanvi.wwlive.web.video.service;

import java.util.Map;


public interface AnchorService {
    
    Map<String, Object> getAnchorInfo(Long anchorId, Long userId);

    /**
     * 获得 主播头像、昵称、性别、年龄、星座、公告
     * @param anchorId
     * @return
     */
    Map<String, Object> getBasicInfo(Long anchorId);

    Map<String, Object> getAnchorVideoList(Long anchorId, Integer startRow, Integer pageSize);
    
    /**
     * 获得直播信息
     * @param anchorId
     * @param loginId
     * @return
     */
    Map<String, Object> getAnchorVideo(Long anchorId, String loginId);

    /**
     * 贡献榜
     */
    Map<String,Object> getContribList(long anchorId);
}
