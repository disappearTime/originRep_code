package com.chineseall.iwanvi.wwlive.web.launch.controller;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSONObject;
import com.chineseall.iwanvi.wwlive.common.check.ResponseResult;
import com.chineseall.iwanvi.wwlive.common.check.ResultMsg;
import com.chineseall.iwanvi.wwlive.web.common.util.JsonUtils;
import com.chineseall.iwanvi.wwlive.web.common.util.ValidationUtils;
import com.chineseall.iwanvi.wwlive.web.launch.service.LaunchShareService;

/**
 * 分享
 * @author DIKEPU
 * @since 2017-02-28 主播端开发一期
 */
@Controller
public class LaunchShareController {

    private final Logger LOGGER = Logger
            .getLogger(this.getClass());
	
	@Autowired
	LaunchShareService launchShareService;

	@RequestMapping(value = "/launch/common/h5/share", method = RequestMethod.POST)
    @ResponseBody
    public ResponseResult<JSONObject> getShareInfo(HttpServletRequest request){
	    
        //接口校验, 无额外加密字段, 只校验通用参数anchorId, nonce, requestId
        if(!ValidationUtils.isValidForLaunch(request)){
            return new ResponseResult<>(ResultMsg.COVER_KEY_CHECK_FAILED);
        }
	    
		ResponseResult<JSONObject> result = new ResponseResult<JSONObject>();
		try {

			Long anchorId = Long.valueOf(request.getParameter("anchorId"));
			Long id = Long.valueOf(request.getParameter("id"));
			Integer type = Integer.valueOf(request.getParameter("shareType"));
			Integer shareKind = Integer.valueOf(request.getParameter("shareKind"));

			result.setResponseByResultMsg(ResultMsg.SUCCESS_);
			result.setData(JsonUtils.toValueOfJsonString(launchShareService.getShareInfo(type, anchorId, id, shareKind)));
		}catch (Exception e) {
			LOGGER.error("分享失败", e);
			result.setData(new JSONObject());
		}
		return result;
	}
	
}
