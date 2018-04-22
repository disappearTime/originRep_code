package com.chineseall.iwanvi.wwlive.web.game.service;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * Created by Niu Qianghong on 2017-10-19 0019.
 */
public interface AnchorService {
    Map<String,Object> uploadCardFace(Long anchorId, Long videoId, HttpServletRequest request);
}
