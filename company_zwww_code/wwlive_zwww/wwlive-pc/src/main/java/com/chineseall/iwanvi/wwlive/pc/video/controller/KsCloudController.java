package com.chineseall.iwanvi.wwlive.pc.video.controller;

import com.alibaba.fastjson.JSON;
import com.chineseall.iwanvi.wwlive.common.cache.RedisClientAdapter;
import com.chineseall.iwanvi.wwlive.common.cache.RedisKey;
import com.chineseall.iwanvi.wwlive.common.constants.Constants;
import com.chineseall.iwanvi.wwlive.common.tools.FileMD5Tools;
import com.chineseall.iwanvi.wwlive.pc.video.service.KsCloudService;
import com.chineseall.iwanvi.wwlive.pc.video.util.DateUtil;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.File;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 接收金山通知
 * 
 * @author DIKEPU
 *
 */
@Controller
@RequestMapping("/kscloud/live")
public class KsCloudController {

	private static final Logger LOGGER = Logger
			.getLogger(KsCloudController.class);
	@Autowired
	private RedisClientAdapter redisAdapter;
	@Autowired
	KsCloudService ksCloudService;

	@RequestMapping(value = "/start", method = RequestMethod.POST)
	@ResponseBody
	public void noticeLiveStart(HttpServletRequest request) {
		String streamName = getLiveName("金山云推流开始start", request);
		if (StringUtils.isEmpty(streamName)) {
			LOGGER.error("streamName为空或null：" + streamName);
			return;
		}
		if (streamName.contains("_")) {// 直播其他视频格式
			streamName = streamName.split("_")[0];
		}
		ksCloudService.noticeLiveStart(streamName);

	}

	@RequestMapping(value = "/stop", method = RequestMethod.POST)
	@ResponseBody
	public void noticeLiveStop(HttpServletRequest request) {

		String streamName = getLiveName("金山云推流结束stop", request);
		if (StringUtils.isEmpty(streamName)) {
			LOGGER.error("streamName为空或null：" + streamName);
			return;
		}
		if (streamName.contains("_")) {// 直播其他视频格式
			streamName = streamName.split("_")[0];
		}
		ksCloudService.noticeLiveStop(streamName);

	}

	private String getLiveName(String typeDes, HttpServletRequest request) {
		// app=live&name=LIVE0000081&swfurl=nil&flashver=nil&tcurl=rtmp://iwanvi1.uplive.ks-cdn.com/live&call=user_publish&vdoid=
		// app live 频道
		// name asd 流名
		// flashver MAC 10,2,153,2 用于播放SWF文件的Flash播放器版本
		// swfurl swl文件播放url，无默认值
		// tcurl rtmp://test.uplive.ks-cdn.com/live/stream 推流url
		// call user_publish 请求类型（推流开始：user_publish;推流结束：user_publish_done）
		// vdoid 123 用于标识文件是否拼接
		try (BufferedReader br = request.getReader()) {
			String str, wholeStr = "";
			while ((str = br.readLine()) != null) {
				wholeStr += str;
			}
//			com.chineseall.iwanvi.wwlive.web.common.util.
 			redisAdapter.listLpush(Constants.TRACE_LIVE_STREAM_STATUS_KEY,(DateUtil.formatDate(new Date(),"yyyy-MM-dd hh:mm:ss")+"--"+typeDes+"----"+wholeStr));
 			if (wholeStr.contains("name")) {
				String[] params = wholeStr.split("&");
				LOGGER.info("typeDes:" + typeDes + ",getLiveName金山云通知参数："
						+ wholeStr);
				if (ArrayUtils.isNotEmpty(params) && params.length > 2) {
					return params[1].split("=")[1];
				}
			}
		} catch (Exception e) {
			LOGGER.error("金山云获得直播名称时异常", e);
		}
		return null;
	}

	@RequestMapping("/setNewApkMd5VersionUrl")
	public Map<String, Object> setNewApkMd5VersionUrl(HttpServletRequest request) {
		String miyao = request.getParameter("miyao");// 密钥
		System.out.println("--A--setNewApkMd5VersionUrl----"
				+ JSON.toJSONString(request.getParameterMap()));
		LOGGER.debug("-----setNewApkMd5VersionUrl----"
				+ JSON.toJSONString(request.getParameterMap()));
		if (StringUtils.isNotBlank(miyao) && miyao.equals("0*Ac3$FgK1")) {
			String filePath = request.getParameter("filePath");
			String version = request.getParameter("version");
			String downUrl = request.getParameter("downUrl");
			String apkMd5 = request.getParameter("apkMd5");
			if (StringUtils.isNotBlank(filePath)) {
				String md5 = FileMD5Tools.getMd5ByFile(new File(filePath));
				redisAdapter.strSetByNormal(RedisKey.MD5_KEY, md5);
			}
			if (StringUtils.isNotBlank(apkMd5)) {
				redisAdapter.strSetByNormal(RedisKey.MD5_KEY, apkMd5);
			}
			if (StringUtils.isNotBlank(version)) {
				redisAdapter.strSetByNormal(RedisKey.VERSION_KEY, version);
			}
			if (StringUtils.isNotBlank(downUrl)) {
				redisAdapter.strSetByNormal(RedisKey.DOWNLOAD_KEY, downUrl);
			}
		}
		Map<String, Object> result = new HashMap<String, Object>();
		result.put("md5值", redisAdapter.strGet(RedisKey.MD5_KEY));
		result.put("version", redisAdapter.strGet(RedisKey.VERSION_KEY));
		result.put("downUrl", redisAdapter.strGet(RedisKey.DOWNLOAD_KEY));
		return result;
	}

}
