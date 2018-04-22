package com.chineseall.iwanvi.wwlive.web.event.service;

import java.util.List;

public interface MedalHonorService {
    
    /**
     * 通过id获得某用户的勋章名称
     * @param winnerId
     * @return
     */
    List<String> getGoddessMedal(Long winnerId);

    List<String> getUserMedalsById(Long userId);
    
    List<String> getIndexAnchorMedal(Long anchorId);
}