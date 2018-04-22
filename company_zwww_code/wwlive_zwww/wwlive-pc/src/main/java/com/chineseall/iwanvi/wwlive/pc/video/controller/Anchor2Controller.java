package com.chineseall.iwanvi.wwlive.pc.video.controller;

import java.util.Map;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.chineseall.iwanvi.wwlive.common.cache.RedisClientAdapter;
import com.chineseall.iwanvi.wwlive.common.cache.RedisExpireTime;
import com.chineseall.iwanvi.wwlive.common.cache.RedisKey;
import com.chineseall.iwanvi.wwlive.common.check.ResponseResult;
import com.chineseall.iwanvi.wwlive.common.check.ResultMsg;
import com.chineseall.iwanvi.wwlive.common.constants.Constants;
import com.chineseall.iwanvi.wwlive.common.tools.Base64Tools;
import com.chineseall.iwanvi.wwlive.common.tools.RandomStrTools;
import com.chineseall.iwanvi.wwlive.pc.common.Page;
import com.chineseall.iwanvi.wwlive.pc.common.loginContext.UserThreadLocalContext;
import com.chineseall.iwanvi.wwlive.pc.video.common.PcConstants;
import com.chineseall.iwanvi.wwlive.pc.video.service.Anchor2Service;

@Controller
@RequestMapping("/pc/anchor")
public class Anchor2Controller {

	@Autowired
	Anchor2Service anchor2Service;
	
	@Value("${iwavi.md5}")
	private String secret;

	@Autowired
	private RedisClientAdapter redisAdapter;
	
	/**
	 * 主播登录
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "login", method = RequestMethod.POST)
	@ResponseBody
	public ResponseResult<Map<String, Object>> anchorLogin(HttpServletRequest request, HttpServletResponse response) {
		String passport = request.getParameter("passport");//通行证
		String passwd = request.getParameter("passwd");//密码

//		StrMD5 md5 = StrMD5.getInstance();
//		passwd = md5.encrypt(passwd, secret);
		
		Map<String, Object> anchorInfo = anchor2Service.anchorLogin(passport, passwd);//根据用户通行证和密码获得登录信息
		ResponseResult<Map<String, Object>> result = new ResponseResult<>();
		if (anchorInfo == null || anchorInfo.isEmpty()) {
			result.setResponseByResultMsg(ResultMsg.LOGIN_INFO);
		} else {
			result.setResponseByResultMsg(ResultMsg.SUCCESS);
			result.setData(anchorInfo);
			toBuildSessionCookie(response, anchorInfo);
			/*long anchorId = PcConstants.getAnchorIdByCookie(request);
			if(anchorId == 0){//说明cookie未失效
				//session
				toBuildSessionCookie(response, anchorInfo);
			}*/

		}
		return result;
	}
	
	private void toBuildSessionCookie(HttpServletResponse response, Map<String, Object> anchorInfo) {
		long anchorId = (long) anchorInfo.get("anchorId");
     	String secret = RandomStrTools.randomStr(8) + Constants.UNDERLINE + anchorId;
     	String cookieVal = Base64Tools.encode(secret.getBytes());
     	
     	//cookie有效期
     	Cookie cookie = new Cookie(Constants.REDIS_SESSION, cookieVal);//保存用户信息到cookie中
     	cookie.setMaxAge(RedisExpireTime.EXPIRE_DAY_1);
     	cookie.setPath("/");
     	response.addCookie(cookie);

     	String anchorKey = RedisKey.PREFIX_ANCHOR_SESSION_ + anchorId;//保存到redis中一天
     	redisAdapter.strSet(anchorKey, "0");
     	redisAdapter.expireKey(anchorKey, RedisExpireTime.EXPIRE_DAY_1);
	}
	
	/**
	 * 退出登录
	 * @param request
	 * @param response
	 * @return
	 */
//	@RequestMapping(value = "logout", method = RequestMethod.POST)
	@ResponseBody
	public ResponseResult<Integer> anchorLogout(HttpServletRequest request, HttpServletResponse response) {
		long anchorId = PcConstants.getAnchorIdByCookie(request);
		//删除 session
		long del = toDestroySessionCookie(response, anchorId);
		
		ResponseResult<Integer> result = new ResponseResult<>();
		if (del > 0) {
			result.setResponseByResultMsg(ResultMsg.SUCCESS);
			result.setData(1);
		} else {
			result.setResponseByResultMsg(ResultMsg.FAIL);
			result.setData(0);
		}
		return result;
	}

	private long toDestroySessionCookie(HttpServletResponse response, long anchorId) {
		//删除cookie
     	Cookie cookie = new Cookie(Constants.REDIS_SESSION, null);
     	cookie.setMaxAge(0);
     	response.addCookie(cookie);
     	//删除redis信息
     	String anchorKey = RedisKey.PREFIX_ANCHOR_SESSION_ + anchorId;
     	return redisAdapter.delKeys(anchorKey);
	}
	
	
	/**
	 * 主播登录
	 * @param request
	 * @param response
	 * @return
	 */
//	@RequestMapping(value = "index", method = RequestMethod.POST)
	@ResponseBody
	public ResponseResult<Map<String, Object>> getAnchorIndexInfo(HttpServletRequest request) {
		long anchorId = PcConstants.getAnchorIdByCookie(request);

		Map<String, Object> anchorInfo = anchor2Service.getAnchorIndexInfo(anchorId);
		ResponseResult<Map<String, Object>> result = new ResponseResult<>();
		if (anchorInfo == null || anchorInfo.isEmpty()) {
			result.setResponseByResultMsg(ResultMsg.LOGIN_INFO);
		} else {
			result.setResponseByResultMsg(ResultMsg.SUCCESS);
			result.setData(anchorInfo);

		}
		return result;
		
	}

	@RequestMapping(value = "watching")
	public String getUserList(Page page, HttpServletRequest request, Model model) {
		long videoId = Long.parseLong(request.getParameter("videoId"));
		String videoStatus = request.getParameter("videoStatus");
		long anchorId = UserThreadLocalContext.getCurrentUser().getUserId();
		
		Page pageResult = anchor2Service.getUserList(anchorId, videoId, page);
        model.addAttribute("page", pageResult);
        model.addAttribute("videoStatus", videoStatus);
        return "live/live_view";
		
	}
	
	@RequestMapping(value = "addBlackList", method = RequestMethod.POST)
	@ResponseBody
	public ResponseResult<Integer> addUserToBlack(HttpServletRequest request) {
		//long videoId = Long.parseLong(request.getParameter("videoId"));
		long userId = Long.parseLong(request.getParameter("userId"));
		int time = Integer.parseInt(request.getParameter("time"));
		long anchorId = UserThreadLocalContext.getCurrentUser().getUserId();

		int add = anchor2Service.addUserToBlack(anchorId, userId, time);
		ResponseResult<Integer> result = new ResponseResult<>();
		result.setResponseByResultMsg(ResultMsg.SUCCESS);
		result.setData(add);
		return result;
		
	}
	
	@RequestMapping(value = "delBlackList", method = RequestMethod.POST)
	@ResponseBody
	public ResponseResult<Integer> delUserToBlack(HttpServletRequest request) {
		//long videoId = Long.parseLong(request.getParameter("videoId"));
		long userId = Long.parseLong(request.getParameter("userId"));
		long anchorId = UserThreadLocalContext.getCurrentUser().getUserId();

		int del = anchor2Service.delUserToBlack(anchorId, userId);
		ResponseResult<Integer> result = new ResponseResult<>();
		result.setResponseByResultMsg(ResultMsg.SUCCESS);
		result.setData(del);
		return result;
		
	}
	
}
