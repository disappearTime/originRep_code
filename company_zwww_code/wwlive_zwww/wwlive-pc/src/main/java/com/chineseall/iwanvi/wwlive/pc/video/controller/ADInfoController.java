package com.chineseall.iwanvi.wwlive.pc.video.controller;

import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.chineseall.iwanvi.wwlive.common.cache.RedisClientAdapter;
import com.chineseall.iwanvi.wwlive.common.cache.RedisKey;
import com.chineseall.iwanvi.wwlive.common.check.ResponseResult;
import com.chineseall.iwanvi.wwlive.common.check.ResultMsg;
import com.chineseall.iwanvi.wwlive.common.constants.Constants;
import com.chineseall.iwanvi.wwlive.common.tools.HttpUtils;
import com.chineseall.iwanvi.wwlive.domain.wwlive.ADPublishDetail;
import com.chineseall.iwanvi.wwlive.pc.common.loginContext.UserThreadLocalContext;
import com.chineseall.iwanvi.wwlive.pc.video.service.ADPublishService;

@Controller
@RequestMapping("/pc/ad")
public class ADInfoController {

	private static final Logger LOGGER = Logger
			.getLogger(ADInfoController.class);

	@Value("${adinfo.cx.url}")
	private String addInfoUrl;

	@Autowired
	private RedisClientAdapter redisAdapter;
	
	@Autowired
	private ADPublishService adPublishService;

	@RequestMapping(value = "/get", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject getInfo(HttpServletRequest request) {
		try {
			HttpURLConnection conn = HttpUtils.createGetHttpConnection(
					addInfoUrl, Constants.UTF8);
			String result = HttpUtils.returnString(conn);
			if (StringUtils.isNotEmpty(result)) {
				JSONObject json = JSONObject.parseObject(result);
				Object obj = json.get("data");
				if (obj == null) {
					return new JSONObject();
				}
				return json;
			}
		} catch (Exception e) {
			LOGGER.error("获取广告异常" + e.getMessage());
		}
		return new JSONObject();
	}

	@RequestMapping(value = "/hassend", method = RequestMethod.POST)
	@ResponseBody
	public Map<String, Object> hasSend(HttpServletRequest request) {
		String videoId = request.getParameter("videoId");
		String id = request.getParameter("id");
		String adType = request.getParameter("adType");
		Map<String, Object> result = new HashMap<String, Object>();
		result.put("sendInfo", 1);
		if (StringUtils.isEmpty(videoId) || StringUtils.isEmpty(id)) {
			return result;
		}
		String key = RedisKey.ANCHOR_VIDOE_AD_ + videoId
				+ Constants.UNDERLINE  + adType  + Constants.UNDERLINE + id;//发送广告set集合
		if (redisAdapter.existsKey(key)) {
			String strTime = redisAdapter.strGet(key).replace("\"", "");
			result.put("sendInfo", 0);
			result.put("time", System.currentTimeMillis() - Long.parseLong(strTime));
		}
		return result;
	}

	/**
	 * 初始化选择框
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/init", method = RequestMethod.POST)
	@ResponseBody
	public Map<String, Object> initSelect(HttpServletRequest request) {
		String videoId = request.getParameter("videoId");
		Map<String, Object> result = new HashMap<String, Object>();
		result.put("sendInfo", 1);
		if (StringUtils.isEmpty(videoId)) {
			return result;
		}
		String key = "";//发送广告set集合
		
		for (int adType = 0; adType < 3; adType++) {
			key = RedisKey.ANCHOR_VIDOE_AD_ + videoId
					+ Constants.UNDERLINE + adType  + Constants.UNDERLINE + Constants.ASTERISK;
			List<String> list = redisAdapter.findKeys(key);
			if (CollectionUtils.isNotEmpty(list)) {
				result.put("sendInfo", 0);
				Map<String, Object> tmp = new HashMap<String, Object>();
				tmp.put("sendInfo", 0);
				tmp.put("adType", adType);
				String option = list.get(0);
				if (adType == 1) {
					String strTime = redisAdapter.strGet(option).replace("\"", "");
					tmp.put("time", System.currentTimeMillis() - Long.parseLong(strTime));
				}
				option = option.substring(option.lastIndexOf(Constants.UNDERLINE) + 1);
				tmp.put("id", option);
				result.put("ad" + adType, tmp);
			}
			
		}
		
		
		return result;
	}

	@RequestMapping(value = "send", method = RequestMethod.POST)
	@ResponseBody
	public ResponseResult<Map<String, Object>> sendAD(HttpServletRequest request) {
		String data = request.getParameter("data");
		String videoId = request.getParameter("videoId");

		List<ADPublishDetail> list = JSONArray.parseArray(data, ADPublishDetail.class);
		long anchorId = UserThreadLocalContext.getCurrentUser().getUserId();
		
		ResponseResult<Map<String, Object>> result = new ResponseResult<>();
		if (list != null && !list.isEmpty()) {
			if (anchorId != list.get(0).getAnchorId()) {
				ResultMsg fail = ResultMsg.FAIL;
				fail.setToolTip("账号不合法。");
				result.setResponseByResultMsg(fail);
				return result;
			}
			Map<String, Object> resultMap = adPublishService.recordAdPublish(list, anchorId);
			String info = (String) resultMap.get("info");

			ResultMsg success = ResultMsg.SUCCESS;
			success.setToolTip(info);
			result.setResponseByResultMsg(success);
			for (ADPublishDetail ad : list) {
				String key = RedisKey.ANCHOR_VIDOE_AD_ + videoId + Constants.UNDERLINE 
						+ ad.getAdType() + Constants.UNDERLINE + ad.getAdId();//发送广告set集合
				if (!redisAdapter.existsKey(key)) {
					redisAdapter.strSet(key, System.currentTimeMillis() + "");
				}
				redisAdapter.expireKey(key, ad.getTime());
	        }
		}
		
		return result;
	}

	@RequestMapping(value = "stop", method = RequestMethod.POST)
	@ResponseBody
	public Map<String, Object> stopAD(HttpServletRequest request) {
		String videoId = request.getParameter("videoId");
		String id = request.getParameter("id");
		String adType = request.getParameter("adType");
		Map<String, Object> result = new HashMap<String, Object>();
		result.put("stopInfo", 1);
		if (StringUtils.isEmpty(videoId) || StringUtils.isEmpty(id)) {
			return result;
		}
		String key = RedisKey.ANCHOR_VIDOE_AD_ + videoId 
				+ Constants.UNDERLINE  + adType + Constants.UNDERLINE + id;//发送广告set集合
		if (redisAdapter.existsKey(key)) {
			redisAdapter.delKeys(key);
		}
		result.put("stopInfo", 0);
		return result;
	}
	
}
