package com.chineseall.iwanvi.wwlive.web.common.helper;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.chineseall.iwanvi.wwlive.web.common.util.RequestParamsUtils;

/**
 * 各种记录的辅助
 * 
 * @author DIKEPU
 * @since 二期
 */
public class PageUrlHelper {

	/**
	 * 跳转页的参数
	 * @param params
	 * @return
	 */
	public static String buildCommonUrl(Map<String, String> params) {
		if (params == null || params.isEmpty()) {
			return "";
		}
		String strUid = params.get("userId");
		Long userId = Long.parseLong(strUid);

		Map<String, String> urlParams = RequestParamsUtils
				.defaultRequetParams(userId);
		
		Map<String, String> tmp = new HashMap<String, String>();
		tmp.putAll(params);
		tmp.putAll(urlParams);
		return RequestParamsUtils.buildParam(tmp);
	}
	
	/**
	 * 跳转页的参数
	 * @param params
	 * @return
	 */
	public static String buildCommonUrlWithoutUser(Map<String, String> params) {
	    if (params == null || params.isEmpty()) {
	        return "";
	    }
	    
	    Map<String, String> tmp = new HashMap<String, String>();
	    tmp.putAll(params);
	    return RequestParamsUtils.buildParam(tmp);
	}
	
	/**
	 * 跳转页的参数，参数按字母顺序排序加密
	 * <p/>
	 * 主播端
	 * @param params
	 * @return
	 */
	public static String buildSortCommonUrl(Map<String, String> params) {
		if (params == null || params.isEmpty()) {
			return "";
		}
		String strUid = params.get("anchorId");
		Long anchorId = Long.parseLong(strUid);

		Map<String, String> urlParams = RequestParamsUtils
				.defaultSortRequetParams(anchorId);
		
		Map<String, String> tmp = new HashMap<String, String>();
		tmp.putAll(params);
		tmp.putAll(urlParams);
		return RequestParamsUtils.buildParam(tmp);
	}
	
	/**
	 * 动态生成的请求参数，按parameters顺序添加
	 * @param params
	 * @param parameters
	 * @return
	 */
	public static String buildDynamicUrl(Map<String, String> params, String... parameters) {
		if (params == null || params.isEmpty()) {
			return "";
		}
		String strUid = params.get("userId");
		if (StringUtils.isEmpty(strUid)) {
			return "";
		}
		Long userId = Long.parseLong(strUid);

		Map<String, String> urlParams = RequestParamsUtils
				.requetParams(userId, parameters);
		
		Map<String, String> tmp = new HashMap<String, String>();
		tmp.putAll(params);
		tmp.putAll(urlParams);
		return RequestParamsUtils.buildParam(tmp);
	}
	
	
	/**
	 * 记录详情页（兑换记录、充值记录）
	 * 
	 * @param userId
	 * @param origin
	 *            1积分，积分兑换记录 2微信 3支付宝
	 * @return
	 */
	public static String buildChargePageUrl(Long userId, String origin) {
		Map<String, String> params = RequestParamsUtils
				.defaultRequetParams(userId);
		params.put("origin", origin);
		return RequestParamsUtils.buildParam(params);
	}

	/**
	 * 跳转页的参数
	 * @param params
	 * @return
	 */
	public static String buildVideoContributionListUrl(Map<String, String> params) {
		if (params == null || params.isEmpty()) {
			return "";
		}
		String anchorIdStr = params.get("anchorId");
		if (StringUtils.isBlank(anchorIdStr)) {
			return "";
		}
		Long anchorId = Long.parseLong(anchorIdStr);
		
		Map<String, String> urlParams = RequestParamsUtils
				.requetParams(new Long(0), anchorId);
		
		Map<String, String> tmp = new HashMap<String, String>();
		tmp.putAll(params);
		tmp.putAll(urlParams);
		return RequestParamsUtils.buildParam(tmp);
	}

}
