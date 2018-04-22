package com.chineseall.iwanvi.wwlive.web.video.controller;

import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import com.chineseall.iwanvi.wwlive.common.cache.RedisClientAdapter;
import com.chineseall.iwanvi.wwlive.common.cache.RedisKey;
import com.chineseall.iwanvi.wwlive.dao.wwlive.ContributionListMapper;
import com.chineseall.iwanvi.wwlive.web.common.helper.UserRankHelper;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.chineseall.iwanvi.wwlive.common.check.ResponseResult;
import com.chineseall.iwanvi.wwlive.common.check.ResultMsg;
import com.chineseall.iwanvi.wwlive.domain.wwlive.UserInfo;
import com.chineseall.iwanvi.wwlive.web.video.service.LoginService;
import com.chineseall.iwanvi.wwlive.web.video.service.UserInfoService;

/**
 * 直播进入接口
 * @author DIKEPU
 *
 */
@Controller
@RequestMapping("/app/user")
public class LoginController {

    static final Logger LOGGER = Logger.getLogger(LoginController.class);
    
	@Autowired
	private LoginService loginService;

	@Autowired
	private UserInfoService userInfoService;

	@Autowired
	private RedisClientAdapter redisAdapter;

	@Autowired
	private ContributionListMapper contribMapper;

	@RequestMapping(value = "/login", method = RequestMethod.POST)
    @ResponseBody
	public ResponseResult<Map<String, Object>> login(HttpServletRequest request) throws ParseException {
		//1.校验 2.注册或更新用户信息  3.返回用户id
		
		//无法区分来源，故uid传值方式， uid + _ + cx创新：1_cx 书城：1_s
		String uid = request.getParameter("uid");
		String version = request.getParameter("version");
		
		ResponseResult<Map<String, Object>> result = new ResponseResult<Map<String, Object>>();
        
		UserInfo userInfo = null;
		Map<String, Object> rs = null;
		try {
			userInfo = userInfoService.checkIsExist(uid);
			if (userInfo != null && !userInfo.putFieldValueToMap().isEmpty()) {
				userInfo.setLoginId(uid);
				loginService.upsertUserInfo(userInfo);
			}
	    	result.setResponseByResultMsg(ResultMsg.SUCCESS);
			rs = getLoginResult(userInfo);
			if (StringUtils.isNotBlank(version) 
					&& (DownLoadController.compareVersion("3.1.0", version) <= 0)) {
				//贵族信息 20171010
				putRankContri(rs, userInfo);
			}
		}catch (Exception e) {
			LOGGER.error("获得用户信息失败：", e);
			LOGGER.error("获得用户信息失败：" + userInfo);
        	result.setResponseByResultMsg(ResultMsg.APP_USER_NOT_EXIST);
			return result;
		}
		rs.put("rongId", uid);
		result.setData(rs);
		return result;
	}
	
	private Map<String, Object> getLoginResult(UserInfo userInfo) {
		Map<String, Object> result =  new HashMap<String, Object>();
		if (userInfo == null) {
			return result;
		}
		result.put("userId", userInfo.getUserId());
		result.put("userName", userInfo.getUserName());
		result.put("headImg", userInfo.getHeadImg());
		result.put("rongToken", userInfo.getRongToken());
		result.put("loginId", userInfo.getLoginId());
		return result;
	}
	
	private void putRankContri(Map<String, Object> result, UserInfo userInfo) {
		//返回用户id
		// 获取贡献值
		Map<String, String> user;
		String rankKey = RedisKey.USER_RANK_CONTRIB_ + userInfo.getUserId();
		if (redisAdapter.existsKey(rankKey)) {
			user = UserRankHelper.getUserStrRankCache(redisAdapter, contribMapper, userInfo.getUserId());
		} else {
			user = UserRankHelper.getAndCacheStrUserRank(redisAdapter, contribMapper, userInfo.getUserId());
		}
		result.put("totalAmt", user == null ? 0 : user.get("contrib"));
		
	}
	
}
