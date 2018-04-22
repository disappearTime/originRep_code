package com.chineseall.iwanvi.wwlive.web.follow.service;

import java.util.List;
import java.util.Map;

public interface AnchorService {

    int getFollowerCnt(Long anchorId);

    Map<String, Object> getInfo(Long anchorId);

    Map<String, Object> getBasicInfo(Long anchorId);

    List<Map<String, Object>> getFollowPage(Long anchorId, Integer pageNo, Long timestamp);

}
