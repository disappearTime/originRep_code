package com.chineseall.iwanvi.wwlive.web.video.service;

import java.util.List;
import java.util.Map;

import com.chineseall.iwanvi.wwlive.web.common.util.Page;

public interface HomeIndexService {

    /**
     * 直播视频列表
     * @param page
     * @return
     */
    public Map<String, Object> getLivingVideos(Page page);
    
	/**
     * 回放
     * @param page
     * @return
     */
    public Map<String, Object> getHistotyVideoListFromCache(Page page);
    
    /**
     * 直播视频列表，灰度代码
     * @param page
     * @return
     */
    public Map<String, Object> getGrayLivingVideos(Page page);
    
	/**
     * 回放，灰度代码
     * @param page
     * @return
     */
    public Map<String, Object> getGrayHistotyVideoListFromCache(Page page);

    List<Map<String,Object>> getBabeAnchorInfo();
}
