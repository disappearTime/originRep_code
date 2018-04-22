package com.chineseall.iwanvi.wwlive.pc.event.service;

public interface LevelEventService {

    /**
     * 根据主播id获取当前钻石数; 若活动未在进行中, 返回-1
     * @param anchorId
     * @return
     */
    Double getCurDiamonds(Long anchorId);

    /**
     * 根据主播id获取当前关卡数; 若活动未在进行中, 返回-1
     * @param anchorId
     * @return
     */
    Integer getCurLevels(Long anchorId);

}
