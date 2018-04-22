package com.chineseall.iwanvi.wwlive.web.common.interceptor;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import com.alibaba.fastjson.JSONObject;
import com.chineseall.iwanvi.wwlive.common.check.ResponseResult;
import com.chineseall.iwanvi.wwlive.common.check.ResultMsg;
import com.chineseall.iwanvi.wwlive.common.constants.Constants;

public class LaunchRequestCommonInterceptor implements HandlerInterceptor {

	static final Logger LOGGER = Logger
			.getLogger(LaunchRequestCommonInterceptor.class);

	private static String loginUri = "/launch/anchor/login.json";

	@Override
	public boolean preHandle(HttpServletRequest request,
			HttpServletResponse response, Object handler) throws Exception {
		String uri = request.getRequestURI();
		String anchorId = (String) request.getParameter("anchorId");
		String coverKey = (String) request.getParameter("coverKey");
		String nonce = (String) request.getParameter("nonce");
		String requestId = (String) request.getParameter("requestId");
		if (LOGGER.isDebugEnabled()) {
			writeLog(request);// 打印日志
		}
		if (uri.equals(loginUri)) {
			return true;
		} else {//非登录
			if (StringUtils.isEmpty(anchorId)
					|| isLostParams(coverKey, nonce, requestId)) {
				writeResult(response);
				return false;
			}
		}
		
		return true;
	}

	@Override
	public void postHandle(HttpServletRequest request,
			HttpServletResponse response, Object handler,
			ModelAndView modelAndView) throws Exception {
	}

	@Override
	public void afterCompletion(HttpServletRequest request,
			HttpServletResponse response, Object handler, Exception ex)
			throws Exception {
	}

	private boolean isLostParams(String coverKey, String nonce, String requestId) {
		return StringUtils.isEmpty(requestId) || StringUtils.isEmpty(coverKey)
				|| StringUtils.isEmpty(nonce);
	}

	private void writeResult(HttpServletResponse response) throws IOException {
		ResponseResult<String> result = new ResponseResult<String>();
		result.setResponseByResultMsg(ResultMsg.LOST_PARAMS);
		response.setCharacterEncoding(Constants.UTF8);
		response.setContentType(Constants.HTTP_CONTENTT_TYPE_JSON);
		try (PrintWriter pw = response.getWriter()) {
			pw.append(JSONObject.toJSONString(result));
		}
	}

	private void writeLog(ServletRequest request) {
		HttpServletRequest httpServletRequest = (HttpServletRequest) request;
		Map<String, String> paramMap = getParam(httpServletRequest);

		String ua = RequestUtil.getStringHeaderDef(httpServletRequest,
				"User-Agent", "");

		String referer = RequestUtil.getStringHeaderDef(httpServletRequest,
				"referer", "");

		String ip = RequestUtil.getIpAddr(httpServletRequest);

		String pin = RequestUtil.getCookieValue(httpServletRequest, "pin", "");

		String url = httpServletRequest.getRequestURL().toString();

		String methodType = httpServletRequest.getMethod();

		Long beginTime = System.currentTimeMillis();
		Date accessTime = new Date(beginTime);

		String uri = httpServletRequest.getRequestURI();

		AccessLog accessLog = new AccessLog();
		accessLog.setAccessDate(accessTime);
		accessLog.setIp(ip);
		accessLog.setPin(pin);
		accessLog.setMethodType(methodType);
		accessLog.setParamMap(paramMap);
		accessLog.setReferer(referer);
		accessLog.setUa(ua);
		accessLog.setUri(uri);
		accessLog.setUrl(url);
		accessLog.setWwa(httpServletRequest.getQueryString());
		// 日志写入文件
		accessLog.writeToFile();
	}

	private Map<String, String> getParam(HttpServletRequest request) {

		Enumeration<?> enumeration = request.getParameterNames();

		Map<String, String> paramMap = new HashMap<String, String>();
		while (enumeration.hasMoreElements()) {
			Object name = enumeration.nextElement();
			if (name instanceof String) {
				paramMap.put(name.toString(),
						request.getParameter(name.toString()));
			}
		}

		return paramMap;
	}
}
