package com.chineseall.iwanvi.wwlive.web.event.service;

import java.util.List;
import java.util.Map;

public interface LevelEventService {

    List<Map<String, Object>> getAnchorRank();

    boolean isInEvent();

    List<Map<String, Object>> getUserRank(Integer pageNo, Integer pageSize);

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

    int getEventStatus();

}
