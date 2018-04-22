package com.chineseall.iwanvi.wwlive.web.launch.service;

import java.util.Map;

public interface AdminService {

    Map<String, Object> getListByAnchorId(Long anchorId, Integer pageNo, Integer pageSize);

    Map<String, Object> remove(String chatRoomId, Long anchorId, Long userId);

    Map<String, Object> set(String chatRoomId, Long anchorId, Long userId);

}
