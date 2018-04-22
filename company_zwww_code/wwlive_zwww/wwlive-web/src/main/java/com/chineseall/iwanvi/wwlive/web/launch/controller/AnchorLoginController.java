package com.chineseall.iwanvi.wwlive.web.launch.controller;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSONObject;
import com.chineseall.iwanvi.wwlive.common.check.ResponseResult;
import com.chineseall.iwanvi.wwlive.common.check.ResultMsg;
import com.chineseall.iwanvi.wwlive.common.exception.IWanviException;
import com.chineseall.iwanvi.wwlive.common.exception.LoginException;
import com.chineseall.iwanvi.wwlive.domain.wwlive.Anchor;
import com.chineseall.iwanvi.wwlive.web.common.util.JsonUtils;
import com.chineseall.iwanvi.wwlive.web.common.util.RegexUtils;
import com.chineseall.iwanvi.wwlive.web.launch.service.LoginService;

@Controller
@RequestMapping("/launch/anchor")
public class AnchorLoginController {

    private final Logger LOGGER = Logger
            .getLogger(this.getClass());
	
    @Autowired
    private LoginService launchLoginService;

	@RequestMapping(value = "/login", method = RequestMethod.POST)
    @ResponseBody
	public ResponseResult<JSONObject> login(HttpServletRequest request) {
	    
		ResponseResult<JSONObject> result = new ResponseResult<JSONObject>();
		String requestId = request.getParameter("requestId");
		if (StringUtils.isNotBlank(requestId)) {
			result.setRequestId(requestId);
		}

		//1.校验 2.注册或更新用户信息  3.返回用户id
		String passport = request.getParameter("passport");
		if (!RegexUtils.isMatche(passport, RegexUtils.EMAIL) 
				&& !RegexUtils.isMatche(passport, RegexUtils.MOBILE_NUMBER)) {
			result.setResponseByResultMsg(ResultMsg.INVALID_ACCOUNT);
			return result;
		}
		String passwd = request.getParameter("passwd");
		
		
		try {
			Anchor anchor = launchLoginService.doLogin(passport, passwd);
			result.setData(JsonUtils.toValueOfJsonString(anchor));
			result.setResponseByResultMsg(ResultMsg.SUCCESS_);
		}catch (Exception e) {
			if (e instanceof IWanviException) {
				ResultMsg msg = ResultMsg.SYSTEM_ERR;
				msg.setInfo(e.getMessage());
				result.setResponseByResultMsg(msg);
			} else if (e instanceof LoginException) {
				ResultMsg msg = ResultMsg.LOGIN_INFO;
				result.setResponseByResultMsg(msg);
			} else {
				ResultMsg msg = ResultMsg.FAIL_;
				msg.setInfo("未知错误");
				result.setResponseByResultMsg(msg);
			}
			result.setData(new JSONObject());
			LOGGER.error("登录失败：", e);
		}
		return result;
	}
    
    
	
}
