package com.chineseall.iwanvi.wwlive.web.common.util;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;

/**
 * 字符串替换占位符
 * @author DIKEPU
 *
 */
public class StringTools {

    private static String regex = "\\#\\([^\\)]+\\)"; 

    /**
     * targetStr字符串占位符#()替换为paramsMap的value
     * @param paramsMap
     * @param targetStr
     * @return
     */
	public static String replace(Map<String, String> paramsMap, String targetStr) {
		if (paramsMap == null || paramsMap.isEmpty()) {
			return "";
		}
		if (StringUtils.isBlank(targetStr)) {
			return "";
		}
	    
	    Pattern p = Pattern.compile(regex);  
	    Matcher m = p.matcher(targetStr);  
	    
	    String g;  
	    while (m.find()) {  
	        g = m.group();  
	        g = g.substring(2, g.length() - 1);  
	        targetStr = m.replaceFirst(paramsMap.get(g));  
	        m = p.matcher(targetStr);  
	    }
	    return targetStr;
	}
}
