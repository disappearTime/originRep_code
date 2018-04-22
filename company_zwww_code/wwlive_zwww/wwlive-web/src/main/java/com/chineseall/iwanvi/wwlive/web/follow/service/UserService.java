package com.chineseall.iwanvi.wwlive.web.follow.service;

import java.util.List;
import java.util.Map;

public interface UserService {

    int getFollowedCnt(Long userId);

    List<Map<String, Object>> getRecommend(String cnid);

    int follow(Long userId, Long anchorId) throws Exception;

    int unfollow(Long userId, Long anchorId) throws Exception;

    Map<String, Object> getFollowList(Long userId, int pageNo, long timestamp, String cnid);

    int isFollower(Long anchorId, Long userId);

    Map<String, Object> getFollowTop3(Long userId, String cnid);

    Long getUserIdByLogin(String userIdStr);

    String get1stPageFollows(Long userId);

}
