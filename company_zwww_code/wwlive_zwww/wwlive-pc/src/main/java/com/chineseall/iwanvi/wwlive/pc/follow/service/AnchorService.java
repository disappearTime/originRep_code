package com.chineseall.iwanvi.wwlive.pc.follow.service;

import com.chineseall.iwanvi.wwlive.pc.common.Page;

public interface AnchorService {

    Page getFollowPage(Long anchorId, Page page, Long timestamp);

}
