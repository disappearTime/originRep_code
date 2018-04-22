package com.chineseall.iwanvi.wwlive.web.video.service;

import com.chineseall.iwanvi.wwlive.domain.wwlive.UserPush;

import java.util.List;
import java.util.Map;

/**
 * Created by 云瑞 on 2017/6/28.
 */
public interface AdvertisementService {

    /**
     * 获取bannergetGrayAdvertBannerList()
     */
    Map<String, Object> getAdvertBannerList(String cnid,String version);

    /**
     * 灰度，获取banner
     */
    Map<String, Object> getGrayAdvertBannerList(String cnid,String version);

    /**
     * 获取启动图
     */
    Map<String, Object> getAdvertBootimgList(String cnid,String version);

    /**
     * 灰度，获取启动图
     */
    Map<String, Object> getGrayAdvertBootimgList(String cnid,String version);

    int addUserPush(UserPush userPush);
}
