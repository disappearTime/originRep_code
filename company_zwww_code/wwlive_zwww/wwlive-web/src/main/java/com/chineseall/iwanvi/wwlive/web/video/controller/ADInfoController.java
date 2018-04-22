package com.chineseall.iwanvi.wwlive.web.video.controller;

import java.net.HttpURLConnection;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSONObject;
import com.chineseall.iwanvi.wwlive.common.constants.Constants;
import com.chineseall.iwanvi.wwlive.common.tools.HttpUtils;
import com.chineseall.iwanvi.wwlive.web.common.embedding.DataEmbeddingTools;
import com.chineseall.iwanvi.wwlive.web.common.util.RegexUtils;
import com.chineseall.iwanvi.wwlive.web.video.service.ADInfoService;

@Controller
public class ADInfoController {

    private static final Logger LOGGER = Logger
            .getLogger(ADInfoController.class);

    @Value("${adinfo.cx.url}")
    private String addInfoUrl;
    
    @Autowired
    private ADInfoService adInfoService;
    
    @RequestMapping(value = "/external/banner/get", method = RequestMethod.POST)
    @ResponseBody
    public JSONObject getInfo(HttpServletRequest request) {
        try {
            String cnid = request.getParameter("cnid");
            String version = request.getParameter("version");
            HttpURLConnection conn = HttpUtils.createGetHttpConnection(
                    addInfoUrl + "&cnid=" + cnid + "&version=" + version, Constants.UTF8);
            String result = HttpUtils.returnString(conn);
            if (StringUtils.isNotEmpty(result)) {
                JSONObject json = JSONObject.parseObject(result);
                Object obj = json.get("data");
                if (obj == null) {
                    return new JSONObject();
                }
                //埋点
                DataEmbeddingTools.insertLog("8002", "2-1", "", "", request);
                return json;
            }
        } catch (Exception e) {
            LOGGER.error("获取广告异常" + e.getMessage());
        }
        return new JSONObject();
    }

    @RequestMapping(value = "/external/banner/click", method = RequestMethod.POST)
    @ResponseBody
    public void clickInfo(HttpServletRequest request) {
        try {
            String id = request.getParameter("id");
            if (id == null) {
            	id = "";
            }
            DataEmbeddingTools.insertLog("8002", "1-1", "", id, request);
        } catch (Exception e) {
            LOGGER.error("用户点击广告异常" + e.getMessage());
        }
        return;
    }
    
    @RequestMapping(value = "/external/banner/video", method = RequestMethod.POST)
    @ResponseBody
    public JSONObject getVideoInfo(HttpServletRequest request) {
        try {
        	
            String anchorId = request.getParameter("anchorId");
            if (RegexUtils.isNum(anchorId)) {
            	JSONObject json = new JSONObject();
            	if (anchorId.contains("\"")) {
            		anchorId = anchorId.replace("\"", "");
            	}
            	json.putAll(adInfoService.getVideoInfoByAnchorId(Long.parseLong(anchorId)));
            	return json;
            }
        } catch (Exception e) {
            LOGGER.error("获取主播视频异常", e);
        }
        return new JSONObject();
    }

    /**
     * 曝光
     * @param request
     */
    @RequestMapping(value = "/external/banner/record", method = RequestMethod.POST)
    @ResponseBody
    public void inserAdInfoLog(HttpServletRequest request) {
        String id = request.getParameter("id");
        if (id == null) {
        	id = "";
        }
        DataEmbeddingTools.insertLog("8002", "2-1", "", id, request);
    }
    
}
