package com.chineseall.iwanvi.wwlive.pc.video.service;

import java.util.List;
import java.util.Map;

import com.chineseall.iwanvi.wwlive.domain.wwlive.ADPublishDetail;

public interface ADPublishService {

//    Map<String, Object> recordAdPublish(Long videoId, String channelNum, String versionNum, Integer adId,
//            Integer adType, Long anchorId, Long roomNum);
    public Map<String, Object> recordAdPublish(List<ADPublishDetail> list, Long anchorId);

}
