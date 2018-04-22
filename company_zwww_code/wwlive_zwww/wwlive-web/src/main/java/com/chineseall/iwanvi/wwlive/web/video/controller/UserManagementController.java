package com.chineseall.iwanvi.wwlive.web.video.controller;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.chineseall.iwanvi.wwlive.common.check.ResponseResult;
import com.chineseall.iwanvi.wwlive.common.check.ResultMsg;
import com.chineseall.iwanvi.wwlive.common.constants.Constants;
import com.chineseall.iwanvi.wwlive.web.common.util.RegexUtils;
import com.chineseall.iwanvi.wwlive.web.common.util.ValidationUtils;
import com.chineseall.iwanvi.wwlive.web.video.service.UserInfo2Service;

@Controller
public class UserManagementController {

	@Autowired
	UserInfo2Service userInfo2Service;

	@RequestMapping(value = "/app/user/isblack", method = RequestMethod.POST)
	@ResponseBody
	public ResponseResult<Integer> isInBlackList(HttpServletRequest request) {
		int videoId = Integer.valueOf(request.getParameter("videoId"));
		int userId = Integer.valueOf(request.getParameter("userId"));

		ResponseResult<Integer> result = new ResponseResult<Integer>();

        if(!ValidationUtils.isValid(request)){
        	result.setResponseByResultMsg(ResultMsg.COVER_KEY_CHECK_FAILED);
            return result;
        }
        
    	result.setResponseByResultMsg(ResultMsg.SUCCESS);
		if (userInfo2Service.isInBlackList(videoId, userId)) {
			result.setData(Constants._1);
		} else {
			result.setData(Constants._0);
		}
		return result;
	}
	
	
	/**
	 * 获得昵称完成看视频的任务
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/external/user/get/name", method = RequestMethod.POST)
	@ResponseBody
	public Map<String, String> getNameAndCompleteTask(HttpServletRequest request) {
		String uid = request.getParameter("userId");
		if (StringUtils.isBlank(uid)) {
			return new HashMap<String, String>();
		}
		if (!RegexUtils.isNum(uid)) {
			return new HashMap<String, String>();
		}
		Long userId = Long.parseLong(uid);
		return userInfo2Service.getNameAndCompleteTask(userId);
	}

	/**
	 * 修改昵称完成看视频的任务
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/external/user/modify/name", method = RequestMethod.POST)
	@ResponseBody
	public Map<String, Object> modifyName(HttpServletRequest request) {
	    
		String userName = request.getParameter("userName");
		String uid = request.getParameter("userId");
		Map<String, Object> result = null;
		if (StringUtils.isBlank(userName) 
				|| StringUtils.isBlank(uid)) {
			result = new HashMap<String, Object>();
			result.put("code", 3);
		}
		if (!RegexUtils.isNum(uid)) {
			result =  new HashMap<String, Object>();
			result.put("code", 3);
		}
		if (!RegexUtils.isMatche(userName, "^[a-zA-Z0-9_\u4E00-\uFA29]{1,8}$")) {
			result = new HashMap<String, Object>();
			result.put("code", 3);
		}
		if (result != null) {
			return result;
		}
		Long userId = Long.parseLong(uid);
		Map<String, Object> resultMap = 
		        userInfo2Service.modifyName(userId, userName);
        return resultMap;
	}
}
