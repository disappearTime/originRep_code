package com.chineseall.iwanvi.wwlive.web.video.service;

import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSONObject;



public interface LiveVideoInfoService {

    /**
     * 查询全部正在直播的视频列表及主播相关信息
     * @return
     */
    Map<String, Object> getLivingVideos();

    /**
     * 获取某个主播下的用户贡献榜单
     * @param anchorId
     * @return
     */
    Map<String, Object> getRankList(int pageNo, int pageSize, int anchorId);

    /**
     * 分页获取历史直播信息
     * @param pageSize
     * @param pageNo
     * @param cnid 
     * @return
     */
    Map<String, Object> getHistotyVideoList(int pageSize, int pageNo, String cnid);

    /**
     * 获取历史直播详情, 并将用户正在观看视频的记录存到biz_user_video表中
     * @param userId
     * @param videoId
     * @param loginId
     * @return
     */
    Map<String, Object> getHistoryVideoDetail(long userId, long anchorId, long videoId, String loginId,String cnid,String version);

    Map<String, Object> exitHistoryVideo(long userId, long videoId);

    Map<String, Object> getShareModel(Long anchorId, Long videoId);

    /**
     * app_store 测试视频
     * @return
     */
    Map<String, Object> appStroreTest();
    
    Map<String, Object> getAnchorInfoForHst(long anchorId);

    /**
     * 获得主播正在直播的信息
     * @param anchorId
     * @return
     */
    Map<String, Object> getLivingByAnchorId(long anchorId);

    Map<String, Object> getGrayLivingVideos();
    
    /**
     * 获得直播间主播获得火箭列表详情
     * @param anchorId
     * @return
     */
    List<JSONObject> getRocketInfoList(long anchorId);
    
}
