package com.chineseall.iwanvi.wwlive.web.launch.service;

import java.util.Map;

public interface VideoService {

    Map<String, Object> getGoodsList(Integer pageNo, Integer pageSize, Long videoId);

    Map<String, Object> getEndInfo(Long videoId, Long anchorId, String streamName);

    Map<String, Object> addLaunchedVideo(String coverImgUrl, String videoName, Long anchorId, int formatType,
            boolean record, int videoType);
    
    Map<String, Object> videoConsInfo(Long videoId);

    Map<String, Object> getHistoryVideoDetail(long anchorId, long videoId);

    int getAnchorIncome(Long anchorId);

    /**
     * 通知服务器停止还是开始
     * @param msgType 0停止 1开始
     * @param videoId
     * @return
     */
    Integer sendLiveMsg(long msgType, long videoId);
}
