package com.chineseall.iwanvi.wwlive.pc.video.service;

import java.text.ParseException;
import java.util.Map;

import com.chineseall.iwanvi.wwlive.domain.wwlive.Anchor;
import com.chineseall.iwanvi.wwlive.pc.common.Page;

public interface AnchorService {

    Map<String, Object> modifyInfo(Anchor anchor, String loginCookie, String newPasswd);

    Map<String, Object> getAnchorInfo(long anchorId) throws ParseException;

    Page getContribList(Page page, long anchorId);

    Map<String, Object> getAllGoodsAndIncome(long anchorId);

    Page getMonthDetail(Page page, long anchorId);
    
    Anchor getById(long anchorId);

    Map<String, Object> getVideoCntAndIncome(long anchorId);

    Page getAdmins(long anchorId, Page page);

    int setCardFace(long anchorId, Long videoId, String imgUrl);
}
