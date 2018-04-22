package com.chineseall.iwanvi.wwlive.pc.common;

import org.apache.commons.lang.StringUtils;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by kai on 16/5/9.
 */
public final class RequestUtils{

    /**
     * 获取request Param参数值，如果找不到或者值为空，返回def
     *
     * @param request
     * @param paramName
     * @param def
     * @return
     */
    public static String getStringParamDef(HttpServletRequest request,
                                           String paramName,
                                           String def) {

        String val = request.getParameter(paramName);

        if (StringUtils.isNotEmpty(val)) {
            return val;
        }
        return def;

    }

    /**
     * 获取request Param参数值，如果找不到或者值为空，返回def
     *
     * @param request
     * @param paramName
     * @param def
     * @return
     */
    public static Integer getIntParamDef(HttpServletRequest request,
                                         String paramName,
                                         Integer def) {
        String val = request.getParameter(paramName);

        if (StringUtils.isNotEmpty(val)) {
            try {
                Integer returnVal = Integer.valueOf(val);
                return returnVal;
            } catch (NumberFormatException nfe) {
                nfe.printStackTrace();
            }
        }

        return def;
    }

    /**
     * 获取request Header参数值，如果找不到或者值为空，返回def
     *
     * @param request
     * @param headName
     * @param def
     * @return
     */
    public static String getStringHeaderDef(HttpServletRequest request,
                                            String headName,
                                            String def) {

        String val = request.getHeader(headName);

        if (StringUtils.isNotEmpty(val)) {
            return val;
        }
        return def;

    }

    /**
     * 获取request Header参数值，如果找不到或者值为空，返回def
     *
     * @param request
     * @param headName
     * @param def
     * @return
     */
    public static Integer getIntHeaderDef(HttpServletRequest request,
                                          String headName,
                                          Integer def) {
        String val = request.getHeader(headName);

        if (val != null) {
            try {
                return Integer.valueOf(val);
            } catch (NumberFormatException nfe) {
                nfe.printStackTrace();
            }
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
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }

        //经过代理多IP模式，取第一个
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
    public static String getCookieValue(HttpServletRequest servletRequest, String name) {
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
    public static String getCookieValue(HttpServletRequest servletRequest, String name, String def) {
        String val = getCookieValue(servletRequest, name);
        if (StringUtils.isNotEmpty(val)) {
            return val;
        }
        return def;
    }


    /**
     * 域名根目录下添加cookie信息
     *
     * @param response
     * @param key
     * @param value
     * @param expire
     */
    public static void addCookie(HttpServletResponse response, String key, String value, Integer expire) {

        Cookie cookie = new Cookie(key, value);

        cookie.setPath("/");
        if (expire != null) {
            cookie.setMaxAge(expire);
        }

        response.addCookie(cookie);

    }


    /**
     * 域名根目录下添加cookie信息
     *
     * @param response
     * @param key
     * @param value
     */
    public static void addCookie(HttpServletResponse response, String key, String value) {

        addCookie(response, key, value, null);

    }

    /**
     * 删除cookie信息
     *
     * @param servletResponse
     * @param request
     * @param name
     */
    public static void deleteCookie(HttpServletResponse servletResponse, HttpServletRequest request, String name) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null && cookies.length > 0) {
            for (Cookie cookie : cookies) {
                String cookieName = cookie.getName();
                if (cookieName.equals(name)) {
                    cookie = new Cookie(name, null);
                    cookie.setMaxAge(0);
                    cookie.setPath("/");
                    servletResponse.addCookie(cookie);
                    break;
                }
            }
        }
    }
}
