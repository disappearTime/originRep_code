package com.chineseall.iwanvi.wwlive.web.common.util;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.lang.StringUtils;

public class ControllerRequestUtils {

    /**
     * 获得参数
     * @param request
     * @return Map 参数名称为key，值为value
     */
    public static Map<String, String> getParam(HttpServletRequest request) {
    	if (request == null) {
    		return new HashMap<String, String>();
    	}
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

    /**
     * 获得用户的信息map
     * @param request
     * @return
     */
    public static Map<String, String> getCommonParam(HttpServletRequest request) {
    	if (request == null) {
    		return new HashMap<String, String>();
    	}
		Enumeration<?> enumeration = request.getParameterNames();
		Map<String, String> paramMap = new HashMap<String, String>();
		while (enumeration.hasMoreElements()) {
			Object name = enumeration.nextElement();
			if (name instanceof String) {
				if ("cnid".equals(name)) {
					paramMap.put(name.toString(),
							request.getParameter(name.toString()));
				} else if ("version".equals(name)) {
					paramMap.put(name.toString(),
							request.getParameter(name.toString()));
				} else if ("model".equals(name)) {
					paramMap.put(name.toString(),
							request.getParameter(name.toString()));
				} else if ("IMEI".equals(name)) {
					paramMap.put(name.toString(),
							request.getParameter(name.toString()));
				} else if ("platform".equals(name)) {
					paramMap.put(name.toString(),
							request.getParameter(name.toString()));
				} else if ("userId".equals(name)) {
					paramMap.put(name.toString(),
							request.getParameter(name.toString()));
				} else if ("anchorId".equals(name)) {
					paramMap.put(name.toString(),
							request.getParameter(name.toString()));
				}
			}
		}

		return paramMap;
	}

    /**
     * 获得用户的信息map
     * @param request
     * @return
     */
    public static Map<String, String> getLaunchCommonParam(HttpServletRequest request) {
    	if (request == null) {
    		return new HashMap<String, String>();
    	}
		Enumeration<?> enumeration = request.getParameterNames();
		Map<String, String> paramMap = new HashMap<String, String>();
		while (enumeration.hasMoreElements()) {
			Object name = enumeration.nextElement();
			if (name instanceof String) {
				if ("cnid".equals(name)) {
					paramMap.put(name.toString(),
							request.getParameter(name.toString()));
				} else if ("version".equals(name)) {
					paramMap.put(name.toString(),
							request.getParameter(name.toString()));
				} else if ("model".equals(name)) {
					paramMap.put(name.toString(),
							request.getParameter(name.toString()));
				} else if ("IMEI".equals(name)) {
					paramMap.put(name.toString(),
							request.getParameter(name.toString()));
				} else if ("platform".equals(name)) {
					paramMap.put(name.toString(),
							request.getParameter(name.toString()));
				} else if ("anchorId".equals(name)) {
					paramMap.put(name.toString(),
							request.getParameter(name.toString()));
				}
			}
		}
		paramMap.put("userId", "0");
		return paramMap;
	}
    /**
     * 解析request请求名称为paramName的Integer型参数的值
     * @param request
     * @param paramName
     * @return
     */
    public static Integer parseIntFromRquest(HttpServletRequest request, String paramName) {
    	if(StringUtils.isBlank(paramName)) {
    		return null;
    	}
    	if (request == null) {
    		throw new IllegalArgumentException("HttpServletRequest请求不能为空！");
    	}
    	String strInt = request.getParameter(paramName);
    	if (StringUtils.isNotBlank(strInt)) {
    		return Integer.parseInt(strInt);
    	}
    	return 0;
    }

    /**
     * 解析request请求名称为paramName的Integer型参数的值
     * @param request
     * @param paramName
     * @return
     */
    public static Long parseLongFromRquest(HttpServletRequest request, String paramName) {
    	if(StringUtils.isBlank(paramName)) {
    		return null;
    	}
    	if (request == null) {
    		throw new IllegalArgumentException("HttpServletRequest请求不能为空！");
    	}
    	String strLong = request.getParameter(paramName);
    	if (StringUtils.isNotBlank(strLong)) {
    		return  Long.parseLong(strLong);
    	}
    	return 0L;
    }
    
    /**
     * 
     * @param request
     * @param paramName
     * @param type
     * @return
     * @throws IllegalAccessException 
     * @throws InstantiationException 
     */
    public static Object parseValFromRquest(HttpServletRequest request, String paramName, Class<?> type) throws InstantiationException, IllegalAccessException {
    	if(StringUtils.isBlank(paramName)) {
    		return null;
    	}
    	if (request == null || type == null) {
    		throw new IllegalArgumentException("HttpServletRequest请求或type为空！");
    	}
    	String str = request.getParameter(paramName);
    	if (StringUtils.isNotBlank(str)) {
    		return ConvertUtils.convert(request.getParameter(paramName), type);
    	}
    	return type.newInstance();
    }
    
}
