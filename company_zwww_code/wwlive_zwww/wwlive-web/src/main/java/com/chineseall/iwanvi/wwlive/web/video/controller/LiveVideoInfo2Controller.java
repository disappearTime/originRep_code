package com.chineseall.iwanvi.wwlive.web.video.controller;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.chineseall.iwanvi.wwlive.common.check.ResponseResult;
import com.chineseall.iwanvi.wwlive.common.check.ResultMsg;
import com.chineseall.iwanvi.wwlive.web.common.embedding.DataEmbeddingTools;
import com.chineseall.iwanvi.wwlive.web.common.util.ValidationUtils;
import com.chineseall.iwanvi.wwlive.web.video.service.LiveVideoInfo2Service;
import com.chineseall.iwanvi.wwlive.web.video.vo.LiveVideoInfoVo;

@Controller
public class LiveVideoInfo2Controller {

	static final Logger LOGGER = Logger
			.getLogger(LiveVideoInfo2Controller.class);
	
	@Autowired
	LiveVideoInfo2Service liveVideoInfo2Service;
    
	/**
	 * 获得视频详情
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/app/video/detail", method = { RequestMethod.POST,
			RequestMethod.GET })
	@ResponseBody
	public ResponseResult<LiveVideoInfoVo> getLiveVideoInfo(
			HttpServletRequest request) {
		
		ResponseResult<LiveVideoInfoVo> result = null;
		
        if(!ValidationUtils.isValid(request)){
        	result = new ResponseResult<>();
        	result.setResponseByResultMsg(ResultMsg.COVER_KEY_CHECK_FAILED);
            return result;
        }
        try {
     		long videoId = Long.valueOf(request.getParameter("videoId"));
     		long userId = Long.valueOf(request.getParameter("userId"));
			String loginId = request.getParameter("loginId");
			String cnid = request.getParameter("cnid");
			String version = request.getParameter("version");

			LiveVideoInfoVo vo = liveVideoInfo2Service.getLiveVideoInfo(userId,
					videoId, loginId,cnid,version);
     		if (vo != null) {
     			result = new ResponseResult<LiveVideoInfoVo>(ResultMsg.SUCCESS);
     			result.setData(vo);
     		} else {
     			ResultMsg fail = ResultMsg.FAIL;
     			fail.setInfo("正在直播的视频不存在。");
     			result = new ResponseResult<LiveVideoInfoVo>(fail);
     		}
     		if (vo != null) {
     			//埋点
     	        DataEmbeddingTools.insertLog("7002", "1-1", 
     	                vo.getAnchorId().toString(), videoId + "", request);
     		}
		} catch (Exception e) {
			LOGGER.error("获得直播信息异常：", e);
 			result = new ResponseResult<LiveVideoInfoVo>(ResultMsg.FAIL);
		}
		return result;
	}

	/**
	 * 离开直播间
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/app/video/leave", method = RequestMethod.POST)
	@ResponseBody
	public ResponseResult<Integer> leaveLiveVideo(HttpServletRequest request) {
		long videoId = Long.valueOf(request.getParameter("videoId"));
		long userId = Long.valueOf(request.getParameter("userId"));
		ResponseResult<Integer> result = new ResponseResult<>();

        if(!ValidationUtils.isValid(request)){
        	result.setResponseByResultMsg(ResultMsg.COVER_KEY_CHECK_FAILED);
            return result;
        }
        try {
    		double incr = liveVideoInfo2Service.leaveLiveVideo(userId, videoId);
    		if (incr > 0) {
    			result.setResponseByResultMsg(ResultMsg.SUCCESS);
    			result.setData(1);
    		} else {
    			result.setResponseByResultMsg(ResultMsg.FAIL);
    			result.setData(0);
    		}
        } catch (Exception e) {
        	LOGGER.error("离开异常", e);
			result.setResponseByResultMsg(ResultMsg.FAIL);
			result.setData(0);
        }
		return result;
	}

	/**
	 * 直播间贵族列表
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/app/video/nobles", method = RequestMethod.POST)
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
        	LOGGER.error("获取贵族列表异常：", e);
			result.setResponseByResultMsg(ResultMsg.FAIL);
			result.setData(new HashMap<String, Object>());
        }
        return result;
	}
	
}
