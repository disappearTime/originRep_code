package com.chineseall.iwanvi.wwlive.pc.video.controller;

import java.util.Map;

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
import com.chineseall.iwanvi.wwlive.pc.common.loginContext.UserThreadLocalContext;
import com.chineseall.iwanvi.wwlive.pc.video.service.UserInfoService;

@Controller
@RequestMapping("/pc/user")
public class UserInfoController {

	@Autowired
	UserInfoService userInfoService;
	
	private Logger logger = Logger.getLogger(this.getClass());
	
	/**
	 * 根据用户主键获得用户的头像、昵称、贡献值、性别、生日、年龄、星座
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "detail", method = RequestMethod.POST)
	@ResponseBody
	public ResponseResult<Map<String, Object>> getLiveVideoInfo(HttpServletRequest request) {
		long userId = Long.parseLong(request.getParameter("userId"));
		long anchorId = UserThreadLocalContext.getCurrentUser().getUserId();
		Map<String, Object> userInfo = userInfoService.getUserInfo(anchorId, userId);
		ResponseResult<Map<String, Object>> result = new ResponseResult<>();
		result.setData(userInfo);
		return result;
	}
	
	/**
	 * 根据用户登录id获得用户的头像、昵称、贡献值、性别、生日、年龄、星座，根据主播id获得是否做过贡献
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "info", method = RequestMethod.POST)
	@ResponseBody
	public ResponseResult<Map<String, Object>> getChatRoomUserInfo(HttpServletRequest request) {
		String loginId = request.getParameter("loginId");
        long anchorId = UserThreadLocalContext.getCurrentUser().getUserId();
		Map<String, Object> userInfo = userInfoService.getUserInfo(loginId, anchorId);
		ResponseResult<Map<String, Object>> result = new ResponseResult<>();
		result.setData(userInfo);
		return result;
	}

	/**
	 * 用户是否被禁言或者房管
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "isblackoradmin", method = RequestMethod.POST)
	@ResponseBody
	public ResponseResult<Map<String, Object>> isInBlackList(HttpServletRequest request) {
		int videoId = Integer.valueOf(request.getParameter("videoId"));
		int userId = Integer.valueOf(request.getParameter("userId"));
		long anchorId = UserThreadLocalContext.getCurrentUser().getUserId();

		ResponseResult<Map<String, Object>> result = new ResponseResult<>();
        
    	result.setResponseByResultMsg(ResultMsg.SUCCESS);
    	Map<String, Object> resultMap = 
    	        userInfoService.isBlackOrAdmin(anchorId, videoId, userId);
    	result.setData(resultMap);
		return result;
	}
	
	/**
	 * 设置房管
	 * @return
	 */
	@RequestMapping(value = "setAdmin", method = RequestMethod.POST)
    @ResponseBody
	public ResponseResult<Map<String, Object>> setAdmin(HttpServletRequest request){
        ResponseResult<Map<String, Object>> rr = new ResponseResult<>();
        long anchorId = UserThreadLocalContext.getCurrentUser().getUserId();
        String userIdStr = request.getParameter("userId");
        String chatRoomId = request.getParameter("chatRoomId");
        try {
            Long userId = Long.valueOf(userIdStr);
            rr.setData(userInfoService.setAdmin(chatRoomId, anchorId, userId));
            rr.setResponseByResultMsg(ResultMsg.SUCCESS_);
        } catch (Exception e) {
            rr.setResponseByResultMsg(ResultMsg.FAIL_);
            rr.setData(new JSONObject());
            if (e instanceof NumberFormatException) {
                logger.error("无法解析数字：", e);
            } else {
                rr.setToolTip(e.getMessage());
            }
        }
        return rr;
	}
	
	@ResponseBody
    @RequestMapping("/removeAdmin")
    public ResponseResult<Map<String, Object>> removeAdmin(HttpServletRequest request) {

        ResponseResult<Map<String, Object>> rr = new ResponseResult<>();
        long anchorId = UserThreadLocalContext.getCurrentUser().getUserId();
        String userIdStr = request.getParameter("userId");
        String chatRoomId = request.getParameter("chatRoomId");
        try {
            Long userId = Long.valueOf(userIdStr);
            rr.setData(userInfoService.removeAdmin(chatRoomId, anchorId, userId));
            rr.setResponseByResultMsg(ResultMsg.SUCCESS_);
        } catch (NumberFormatException e) {
            rr.setData(new JSONObject());
            rr.setResponseByResultMsg(ResultMsg.FAIL_);
            logger.error("无法解析数字：", e);
        }
        return rr;
    }
}
