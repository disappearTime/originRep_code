package com.chineseall.iwanvi.wwlive.web.otherapp.controller;

import com.chineseall.iwanvi.wwlive.web.video.service.AnchorService;
import com.chineseall.iwanvi.wwlive.web.video.service.LiveVideoInfoService;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Niu Qianghong on 2017-08-30 0030.
 */
@Controller
public class DZLiveController {

    @Autowired
    private LiveVideoInfoService liveVideoInfoService;

    @Autowired
    private AnchorService anchorService;

    private Logger logger = Logger.getLogger(this.getClass());

    /**
     * 获取主播的直播或者最近n个历史直播
     * @param request
     * @return
     */
    @RequestMapping("/external/dz/anchor/living")
    @ResponseBody
    public Map<String, Object> getLivingOrHistory(HttpServletRequest request) {
        try {
            Long anchorId = Long.valueOf(request.getParameter("anchorId"));
            Integer videoCnt = Integer.valueOf(request.getParameter("videoCnt"));
            Map<String, Object> livingVideo = liveVideoInfoService.getLivingByAnchorId(anchorId);
            if (livingVideo != null && !livingVideo.isEmpty()) {
                Map<String, Object> livingInfo = new HashMap<>();
                livingInfo.put("livingVideo", livingVideo);
                return livingInfo; // 返回正在直播视频
            }
            // 返回最新的videoCnt个直播
            return anchorService.getAnchorVideoList(anchorId, 1, videoCnt);
        } catch (Exception e) {
            logger.error("定制版请求正在直播视频异常", e);
            return null;
        }

    }

    /**
     * 分页获取主播直播列表
     * @param request
     * @return
     */
    @RequestMapping("/external/dz/anchor/videoList")
    @ResponseBody
    public Map<String, Object> getAnchorVideoList(HttpServletRequest request){
        try {
            Long anchorId = Long.valueOf(request.getParameter("anchorId"));
            Integer pageNo = Integer.valueOf(request.getParameter("pageNo"));
            Integer pageSize = Integer.valueOf(request.getParameter("pageSize"));
            return anchorService.getAnchorVideoList(anchorId, pageNo, pageSize);
        } catch (Exception e) {
            logger.error("定制版请求正在主播视频列表异常", e);
            return null;
        }
    }

}
