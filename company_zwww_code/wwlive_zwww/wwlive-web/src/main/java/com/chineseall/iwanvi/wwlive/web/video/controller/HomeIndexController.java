package com.chineseall.iwanvi.wwlive.web.video.controller;

import com.alibaba.fastjson.JSONObject;
import com.chineseall.iwanvi.wwlive.common.cache.RedisClientAdapter;
import com.chineseall.iwanvi.wwlive.common.cache.RedisKey;
import com.chineseall.iwanvi.wwlive.common.check.ResponseResult;
import com.chineseall.iwanvi.wwlive.common.check.ResultMsg;
import com.chineseall.iwanvi.wwlive.web.common.util.ControllerRequestUtils;
import com.chineseall.iwanvi.wwlive.web.common.util.Page;
import com.chineseall.iwanvi.wwlive.web.video.service.HomeIndexService;
import com.chineseall.iwanvi.wwlive.web.video.service.PublicNoticeService;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class HomeIndexController {
	
	private static final Logger LOGGER = Logger.getLogger(HomeIndexController.class);
	
    @Autowired
    private RedisClientAdapter redisAdapter;
    
    @Autowired
    private HomeIndexService homeIndexService;

	@Autowired
	private PublicNoticeService publicNoticeService;
    
	@RequestMapping(value = "/external/tab/video/living", method = {
			RequestMethod.POST, RequestMethod.GET })
	@ResponseBody
	public ResponseResult<Map<String, Object>> livingList(HttpServletRequest request, ModelAndView model, Page page) {
		ResponseResult<Map<String, Object>> rr = new ResponseResult<>();
		try {

			String cnid = request.getParameter("cnid");
			//灰度
			String objQudao = redisAdapter.strGet("qudao");
            String score = request.getParameter("score");
            String videoKey = request.getParameter("videoKey");
            if(StringUtils.isNotEmpty(videoKey)) {
                JSONObject json = new JSONObject();
                json.put("score",score);
                json.put("videoKey",videoKey);
                page.setExtra(json);
            }
			Map<String, Object> result = null;
			if(StringUtils.isNotBlank(objQudao) //1062
					&& objQudao.equals(cnid)){//0代表所有用户都下载
					result = homeIndexService.getGrayLivingVideos(page);
			} else  {
				result = homeIndexService.getLivingVideos(page);
			}
            rr.setData(result);
			rr.setResponseByResultMsg(ResultMsg.SUCCESS);
		} catch(Exception e) {
			LOGGER.error("直播异常", e);
			rr.setResponseByResultMsg(ResultMsg.FAIL);
		}
		return rr;
	}

	@RequestMapping(value = "/external/tab/video/lived", method = {
			RequestMethod.POST, RequestMethod.GET })
	@ResponseBody
	public ResponseResult<Map<String, Object>> livedList(HttpServletRequest request, ModelAndView model, Page page) {
		ResponseResult<Map<String, Object>> rr = new ResponseResult<>();
		try {
			String cnid = request.getParameter("cnid");
			//灰度
			String objQudao = redisAdapter.strGet("qudao");
			String score = request.getParameter("score");
			String videoKey = request.getParameter("videoKey");
			if(StringUtils.isNotEmpty(videoKey)) {
                JSONObject json = new JSONObject();
                json.put("score",score);
                json.put("videoKey",videoKey);
                page.setExtra(json);
            }
			Map<String, Object> result = null;
			if(StringUtils.isNotBlank(objQudao)) {//"1062"
				if(objQudao.equals(cnid)){//0代表所有用户都下载
					result = homeIndexService.getGrayHistotyVideoListFromCache(page);
				} else {
					result = homeIndexService.getHistotyVideoListFromCache(page);
				}
			} else  {
				result = homeIndexService.getHistotyVideoListFromCache(page);
			}
			rr.setData(result);
			rr.setResponseByResultMsg(ResultMsg.SUCCESS);
		} catch(Exception e) {
			LOGGER.error("回放异常", e);
			rr.setResponseByResultMsg(ResultMsg.FAIL);
		}
		return rr;
	}
	
	@RequestMapping(value = "/external/tab/index", method = {RequestMethod.GET, RequestMethod.POST})
	public ModelAndView tabVideoHome(HttpServletRequest request, ModelAndView model) {
		model.setViewName("tab/video_index");
		String cnid = request.getParameter("cnid");
		String snapShotKey = RedisKey.VideoKeys.LIVING_VIDEOS_SNAPSHOT;
		//灰度
		String objQudao = redisAdapter.strGet("qudao");
		if(StringUtils.isNotBlank(objQudao)) {//"1062"
			if(objQudao.equals(cnid)){//0代表所有用户都下载
				snapShotKey = RedisKey.VideoGrayKeys.LIVING_VIDEOS_SNAPSHOT;
			}
		}
		if (redisAdapter.existsKey(snapShotKey)) {
			String videoKey = redisAdapter.strGet(snapShotKey);
			Long cnt = redisAdapter.zsetCard(videoKey);
	        model.addObject("params", ControllerRequestUtils.getParam(request));
			model.getModel().put("livingVideoCnt", cnt);
		} else {
			model.getModel().put("livingVideoCnt", "0");
		}
		return model;
	}

	@RequestMapping(value = "/external/law/getLaw",
			method = RequestMethod.GET)
	public ModelAndView toMyInfo(HttpServletRequest request, HttpServletResponse response) {
		ModelAndView model = new ModelAndView("my/law");
		return model;
	}

	/**
	 * 跳转到宝贝播数页
	 * @return
	 */
	@RequestMapping("/external/baberead")
	public String toBabeRead(){
		return "babyreading/babybooks";
	}

	@RequestMapping("/external/getbabeinfo")
	@ResponseBody
	public Map<String, Object> getBabeInfo(){
		Map<String, Object> babeMap = new HashMap<>();
		List<Map<String, Object>> babeInfo = homeIndexService.getBabeAnchorInfo();
		babeMap.put("babeInfo", babeInfo);
		return babeMap;
	}

}
