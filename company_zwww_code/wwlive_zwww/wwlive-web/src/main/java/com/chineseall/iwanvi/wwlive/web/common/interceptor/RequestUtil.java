package com.chineseall.iwanvi.wwlive.web.common.interceptor;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;

public final class RequestUtil {
	/**
	 * 获取request Header参数值，如果找不到或者值为空，返回def
	 *
	 * @param request
	 * @param headName
	 * @param def
	 * @return
	 */
	public static String getStringHeaderDef(HttpServletRequest request,
			String headName, String def) {

		String val = request.getHeader(headName);

		if (StringUtils.isNotEmpty(val)) {
			return val;
		}
		return def;

	}

	/**
	 * 获取ip地址
	 *
	 * @param request
	 * @return
	 */
	public static String getIpAddr(HttpServletRequest request) {
		String ip = request.getHeader("x-forwarded-for");
		if (ip == null || ip.length() == 0
				|| "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("Proxy-Client-IP");
		}
		if (ip == null || ip.length() == 0
				|| "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("WL-Proxy-Client-IP");
		}
		if (ip == null || ip.length() == 0
				|| "unknown".equalsIgnoreCase(ip)) {
			ip = request.getRemoteAddr();
		}

		// 经过代理多IP模式，取第一个
		if (StringUtils.isNotBlank(ip) && ip.contains(",")) {
			ip = ip.split(",")[0];
		}

		return ip;
	}

	/**
	 * 获取cookie信息
	 *
	 * @param servletRequest
	 * @param name
	 * @return
	 */
	public static String getCookieValue(HttpServletRequest servletRequest,
			String name) {
		Cookie[] cookies = servletRequest.getCookies();
		if (cookies != null && cookies.length > 0) {
			for (Cookie cookie : cookies) {
				String cookieName = cookie.getName();
				if (cookieName.equals(name)) {
					return cookie.getValue();
				}
			}
		}
		return null;
	}

	/**
	 * 获取cookie信息,如果没有，返回def
	 *
	 * @param servletRequest
	 * @param name
	 * @param def
	 * @return
	 */
	public static String getCookieValue(HttpServletRequest servletRequest,
			String name, String def) {
		String val = getCookieValue(servletRequest, name);
		if (StringUtils.isNotEmpty(val)) {
			return val;
		}
		return def;
	}

}
