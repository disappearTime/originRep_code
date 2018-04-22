package com.chineseall.iwanvi.wwlive.web.launch.service;

import java.util.Map;

public interface UserService {

    Map<String, Object> mute(Long anchorId, Long videoId, Long userId, Integer duration,String log);

    Map<String, Object> getInfo(String viewId, Long anchorId);

}
