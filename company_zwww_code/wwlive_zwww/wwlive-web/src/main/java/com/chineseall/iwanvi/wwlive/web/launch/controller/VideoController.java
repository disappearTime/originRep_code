package com.chineseall.iwanvi.wwlive.web.launch.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.chineseall.iwanvi.wwlive.common.cache.RedisClientAdapter;
import com.chineseall.iwanvi.wwlive.common.check.ResponseResult;
import com.chineseall.iwanvi.wwlive.common.check.ResultMsg;
import com.chineseall.iwanvi.wwlive.web.common.helper.PageUrlHelper;
import com.chineseall.iwanvi.wwlive.web.common.util.*;
import com.chineseall.iwanvi.wwlive.web.launch.service.VideoService;
import com.chineseall.iwanvi.wwlive.web.video.service.LiveVideoInfo2Service;
import com.chineseall.iwanvi.wwlive.web.video.service.LiveVideoInfoService;
import com.service.FollowNoticeService;
import com.service.impl.FollowNoticeServiceImpl;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;

import java.io.BufferedReader;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 直播controller
 * @author Niu Qianghong
 *
 */
@Controller("launchVideoController")
@RequestMapping("/launch")
public class VideoController {
    
    @Autowired
    private VideoService videoService;

    @Autowired
    private LiveVideoInfoService liveVideoInfoService;
    
    private LogUtils logUtil = new LogUtils(this.getClass());

    private final Logger log = Logger.getLogger(this.getClass());
    
    private static FollowNoticeService followNoticeService = new FollowNoticeServiceImpl();

    @Autowired
    private RedisClientAdapter redisAdapter;

	@Autowired
	LiveVideoInfo2Service liveVideoInfo2Service;
	
    @ResponseBody
    @RequestMapping("/video/create")
    public ResponseResult<JSONObject> createVideo(HttpServletRequest request){
        
        //接口校验, 无额外加密字段, 只校验通用参数anchorId, nonce, requestId
        if(!ValidationUtils.isValidForLaunch(request)){
            return new ResponseResult<>(ResultMsg.COVER_KEY_CHECK_FAILED);
        }
        
        ResponseResult<JSONObject> rr = new ResponseResult<>();
        //参数包括封面图片地址, 直播主题, anchorId
        String coverImgUrl = request.getParameter("coverImg");
        String videoName = request.getParameter("videoName");        
        String anchorIdStr = request.getParameter("anchorId");
        try {
            Long anchorId = Long.valueOf(anchorIdStr);
            int formatType = 1;//主播端视频格式类型为竖屏 = 1
            boolean record = true;//主播端视频默认录制
            int videoType = 0;//主播端视频默认为娱乐直播
            rr.setData(JsonUtils.toValueOfJsonString(videoService.addLaunchedVideo(coverImgUrl, videoName, anchorId, formatType, record, videoType)));
            rr.setResponseByResultMsg(ResultMsg.SUCCESS_);
            
            // 通知开播
            boolean flag = followNoticeService.noticeAnchorOpenLive(anchorId, 1, (new Date()).getTime());

            try (BufferedReader br = request.getReader()) {
                String str, wholeStr = "";
                while ((str = br.readLine()) != null) {
                    wholeStr += str;
                }
                redisAdapter.listLpush(com.chineseall.iwanvi.wwlive.common.constants.Constants.TRACE_LIVE_STREAM_STATUS_KEY,(DateTools.formatDate(new Date(),"yyyy-MM-dd hh:mm:ss")+"--创建直播(App端)--"+ wholeStr+"---"+ JSON.toJSONString(rr)));
            } catch (Exception e) {
                e.printStackTrace();
            }

            log.info("关注通知--通知开播, anchorId = " + anchorId + "结果: " + flag);
        } catch (Exception e) {
        	rr.setResponseByResultMsg(ResultMsg.FAIL_);
            rr.setData(new JSONObject());
            if (e instanceof NumberFormatException) {
            	log.error("无法解析数字：", e);
            } else {
                rr.setToolTip(e.getMessage());
                log.error("创建直播失败", e);
            }
        }
        logUtil.getLogger().info("创建直播接口返回信息 = " + JSON.toJSONString(rr));
        return rr;
    }
    
    /**
     * 获取当前直播的礼品列表
     * @param request
     * @return
     */
    @ResponseBody
    @RequestMapping("/video/goods")
    public ResponseResult<JSONObject> getGoodsList(HttpServletRequest request){

        //接口校验, 无额外加密字段, 只校验通用参数anchorId, nonce, requestId
        if(!ValidationUtils.isValidForLaunch(request)){
            return new ResponseResult<>(ResultMsg.COVER_KEY_CHECK_FAILED);
        }
        
        ResponseResult<JSONObject> rr = new ResponseResult<>();
        String videoIdStr = request.getParameter("videoId");
        String pageNoStr = request.getParameter("pageNo");
        String pageSizeStr = request.getParameter("pageSize");
        try {
            Long videoId = Long.valueOf(videoIdStr);
            Integer pageNo = Integer.valueOf(pageNoStr);
            Integer pageSize = Integer.valueOf(pageSizeStr);
          //分页参数判断
            if(pageNo < 1 || pageSize < 0){
                return new ResponseResult<>(ResultMsg.FAIL_);
            }
            rr.setData(JsonUtils.toValueOfJsonString(videoService.getGoodsList(pageNo, pageSize, videoId)));
            rr.setResponseByResultMsg(ResultMsg.SUCCESS_);
        } catch (Exception e) {
            rr.setResponseByResultMsg(ResultMsg.FAIL_);
            rr.setData(new JSONObject());
            log.error("获取当前直播的礼品列表失败:", e);
        }
        return rr;
    }
    
    /**
     * 直播结束之后获取总览信息, 包括观看过的用户数, 礼品数, 时长, 公告
     * @param request
     * @return
     */
    @ResponseBody
    @RequestMapping("/video/endinfo")
    public ResponseResult<JSONObject> getEndInfo(HttpServletRequest request){
        
        logUtil.logParam("直播结束", request, "videoId");
        
        //接口校验, 无额外加密字段, 只校验通用参数anchorId, nonce, requestId
        if(!ValidationUtils.isValidForLaunch(request)){
            return new ResponseResult<>(ResultMsg.COVER_KEY_CHECK_FAILED);
        }
        
        ResponseResult<JSONObject> rr = new ResponseResult<>();
        String videoIdStr = request.getParameter("videoId");
        String anchorIdStr = request.getParameter("anchorId");
        String streamName = request.getParameter("streamName");
        try {
            Long videoId = Long.valueOf(videoIdStr);
            Long anchorId = Long.valueOf(anchorIdStr);
            rr.setData(JsonUtils.toValueOfJsonString(videoService.getEndInfo(videoId, anchorId, streamName)));
            rr.setResponseByResultMsg(ResultMsg.SUCCESS_);
            
            long timestamp = (new Date()).getTime();
            int income = videoService.getAnchorIncome(anchorId);
            //通知收入
            boolean flag1 = followNoticeService.noticeAnchorMoney(anchorId, income, timestamp);
            log.info("关注通知--通知收入, anchorId = " + anchorId + ", videoId = " + videoId + "结果: " + flag1);
            //通知直播结束
            boolean flag2 = followNoticeService.noticeAnchorOpenLive(anchorId, 0, timestamp);
            log.info("关注通知--通知结束, anchorId = " + anchorId + ", videoId = " + videoId + "结果: " + flag2);

            try (BufferedReader br = request.getReader()) {
                String str, wholeStr = "";
                while ((str = br.readLine()) != null) {
                    wholeStr += str;
                }
                redisAdapter.listLpush(com.chineseall.iwanvi.wwlive.common.constants.Constants.TRACE_LIVE_STREAM_STATUS_KEY,(DateTools.formatDate(new Date(),"yyyy-MM-dd hh:mm:ss")+"--关闭直播(App端)--"+ wholeStr+"---"+JSON.toJSONString(rr)));
            } catch (Exception e) {
                e.printStackTrace();
             }

        } catch (Exception e) {
            rr.setResponseByResultMsg(ResultMsg.FAIL_);
            rr.setData(new JSONObject());
            log.error("获得直播结束信息失败:", e);
        }
        logUtil.getLogger().info("结束直播接口返回信息 = " + JSON.toJSONString(rr));
        return rr;
    }

    /**
     * 主播端用户贡献值页面
     * @param request
     * @return
     */
    @RequestMapping("/video/consInfo")
    public ModelAndView videoConsInfo(HttpServletRequest request){
    	ModelAndView mv = new ModelAndView("/launch/video_rank_info");
    	
        //接口校验, 无额外加密字段, 只校验通用参数anchorId, nonce, requestId
        if(!ValidationUtils.isValidForLaunch(request)){
            return mv;
        }
    	try {
            String videoIdStr = request.getParameter("videoId");
            Long videoId = Long.valueOf(videoIdStr);
        	Map<String, String> params = ControllerRequestUtils.getCommonParam(request);
            mv.addObject("consPage", PageUrlHelper.buildVideoContributionListUrl(params));
            Map<String, String> requestParams = RequestParamsUtils.defaultRequetParams(new Long(0));
            params.putAll(requestParams);
            mv.addObject("params", params);
        	mv.addAllObjects(videoService.videoConsInfo(videoId));
    	} catch (Exception e) {
            log.error("获得直播结束信息失败:", e);
    	}
    	return mv;
    }
    
    /**
     * 点击观看历史直播, 获得历史视频详情
     * @param request
     * @return
     */
    @RequestMapping(value = "/video/historydetail")
    @ResponseBody
    public ResponseResult<JSONObject> enterHistoryVideo(HttpServletRequest request){
        
        //接口校验, 无额外加密字段, 只校验通用参数anchorId, nonce, requestId
        if(!ValidationUtils.isValidForLaunch(request)){
            return new ResponseResult<>(ResultMsg.COVER_KEY_CHECK_FAILED);
        }
        
        ResponseResult<JSONObject> rr = new ResponseResult<>();
        try {
            long anchorId = Long.valueOf(request.getParameter("anchorId"));//参数需要用户id, 和videoid
            long videoId = Integer.valueOf(request.getParameter("videoId"));
            rr = new ResponseResult<>();
            rr.setData(JsonUtils.toValueOfJsonString(videoService.getHistoryVideoDetail(anchorId, videoId)));  
            rr.setResponseByResultMsg(ResultMsg.SUCCESS_);
        } catch (Exception e) {
            rr.setResponseByResultMsg(ResultMsg.FAIL_);
            rr.setData(new JSONObject());
            log.error("获得直播结束信息失败:", e);
        }
        return rr;
    }

    /**
     * 主播离开，返回直播间发送消息。
     * @param request
     * @return
     */
    @RequestMapping(value = "/video/msg/send")
    @ResponseBody
    public ResponseResult<Integer> sendLiveMsg(HttpServletRequest request){
        
        //接口校验, 无额外加密字段, 只校验通用参数anchorId, nonce, requestId
        if(!ValidationUtils.isValidForLaunch(request)){
            return new ResponseResult<>(ResultMsg.COVER_KEY_CHECK_FAILED);
        }
        
        ResponseResult<Integer> rr = new ResponseResult<>();
        try {
            long msgType = Long.valueOf(request.getParameter("msgType"));//0离开, 1返回
            long videoId = Integer.valueOf(request.getParameter("videoId"));
            rr = new ResponseResult<>();
            rr.setData(videoService.sendLiveMsg(msgType, videoId));  
            rr.setResponseByResultMsg(ResultMsg.SUCCESS_);
        } catch (Exception e) {
            rr.setResponseByResultMsg(ResultMsg.FAIL_);
            rr.setData(new Integer(0));
            log.error("获得直播结束信息失败:", e);
        }
        return rr;
    }

	/**
	 * 直播间贵族列表
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/video/nobles", method = RequestMethod.POST)
	@ResponseBody
	public ResponseResult<Map<String, Object>> videoNobles(HttpServletRequest request) {
		long videoId = Long.valueOf(request.getParameter("videoId"));
		ResponseResult<Map<String, Object>> result = new ResponseResult<>();

        if(!ValidationUtils.isValid(request)){
        	result.setResponseByResultMsg(ResultMsg.COVER_KEY_CHECK_FAILED);
        }
        try {
        	Map<String, Object> nobles = liveVideoInfo2Service.getWatchingVideoNobles(videoId);
			result.setResponseByResultMsg(ResultMsg.SUCCESS);
			result.setData(nobles);
        } catch (Exception e) {
        	log.error("获取贵族列表异常：", e);
			result.setResponseByResultMsg(ResultMsg.FAIL);
			result.setData(new HashMap<String, Object>());
        }
        return result;
	}

    /**
     * 查询火箭获得集合接口
     * @param request
     * @return
     */
    @RequestMapping(value = "/video/roomDetails", method = {RequestMethod.POST, RequestMethod.GET })
    @ResponseBody
    public ResponseResult<Map<String, Object>> getRocketInfoList(HttpServletRequest request){
        ResponseResult<Map<String, Object>> rr = new ResponseResult<>();
        Map<String, Object> result =  new HashMap<String, Object>();
        try {
            Long anchorId = Long.valueOf(request.getParameter("anchorId"));
            List<JSONObject> data = liveVideoInfoService.getRocketInfoList(anchorId);
            result.put("rocketInfoList", data);
            rr.setData(result);
            rr.setResponseByResultMsg(ResultMsg.SUCCESS);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("获得火箭信息失败：", e);
            rr.setResponseByResultMsg(ResultMsg.FAIL);
        }
        return rr;
    }
}
