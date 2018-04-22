package com.chineseall.iwanvi.wwlive.web.common.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;

/**
 * regex工具类
 * @author DIKEPU
 *
 */
public final class RegexUtils {

	public static final String MOBILE_NUMBER = "^(((13[0-9])|(15([0-3]|[5-9]))|(18[0-9]))\\d{8})|(0\\d{2}-\\d{8})|(0\\d{3}-\\d{7})$";

	public static final String EMAIL = "^([a-z0-9A-Z]+[-|_|\\.]?)+[a-z0-9A-Z]@([a-z0-9A-Z]+(-[a-z0-9A-Z]+)?\\.)+[a-zA-Z]{2,}$";
	
	public static boolean isNum(String str) {
		if (StringUtils.isEmpty(str)) {
			return false;
		}

		Pattern pattern = Pattern.compile("^\\d+$");
		Matcher digital = pattern.matcher(str);
		return digital.matches();
	}

	/**
	 * 是否符合正则表达式
	 * @param str
	 * @param regex
	 * @return
	 */
	public static boolean isMatche(String str, String regex) {
		if (StringUtils.isEmpty(str)) {
			return false;
		}
		if (StringUtils.isBlank(regex)) {
			return false;
		}
		Pattern pattern = Pattern.compile(regex);
		Matcher digital = pattern.matcher(str);
		return digital.matches();
	}
}
