package com.chineseall.iwanvi.wwlive.web.common.util;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import com.chineseall.iwanvi.wwlive.common.constants.Constants;
import com.chineseall.iwanvi.wwlive.common.tools.StrMD5;

/**
 * 用来生成必输参数 nonce、coverKey和requestId
 * <p>
 * 默认md5顺序为UserId Nonce requestId
 * </p>
 * @author DIKEPU
 *
 */
public final class RequestParamsUtils {

	private static String getNonce(int len) {
		StringBuilder returnVal = new StringBuilder();
		char[] tempChars = new char[] { 'A', 'a', 'B', 'b', 'C', 'c', 'D', 'd',
				'E', 'e', 'F', 'f', 'G', 'g', 'H', 'h', 'I', 'i', 'J', 'j',
				'K', 'k', 'L', 'l', 'M', 'm', 'N', 'n', 'O', 'o', 'P', 'p',
				'Q', 'q', 'R', 'r', 'S', 's', 'T', 't', 'U', 'u', 'V', 'v',
				'W', 'w', 'X', 'x', 'Y', 'y', 'Z', 'z', '0', '1', '2', '3',
				'4', '5', '6', '7', '8', '9' };
		Random random = new Random();
		int charLen = tempChars.length;
		for (int i = 0; i < len; i++) {
			returnVal.append(tempChars[random.nextInt(charLen)]);
		}

		return returnVal.toString();
	}

	private static String getRequestId() {
		UUID uuid = UUID.randomUUID();
		String tmp = uuid.toString();
		return tmp.replaceAll("-", "");
	}
	
	/**
	 * 生成9为nonce唯一标示requestId，md5加密方式为nonce + requestId + userId
	 * @param userId
	 * @return
	 */
	public static Map<String, String> defaultRequetParams(Long userId) {
		if (userId == null) {
			return new HashMap<String, String>();
		}
		String nonce =  getNonce(9);
		String requestId = getRequestId();
		String waitStr = userId + nonce + requestId ;
		String coverKey = StrMD5.getInstance().getStringMD5(waitStr);
		Map<String, String> params = new HashMap<String, String>();
		params.put("userId", userId.toString());
		params.put("nonce", nonce);
		params.put("requestId", requestId);
		params.put("coverKey", coverKey);
		return params;
	}

	/**
	 * 多参数加密
	 * @param userId
	 * @param parameters 加密参数
	 * @return
	 */
	public static Map<String, String> requetParams(Long userId, String... parameters) {
		if (userId == null) {
			return new HashMap<String, String>();
		}
		String nonce =  getNonce(9);
		String requestId = getRequestId();
		String waitStr = userId + nonce + requestId ;
		if ((parameters != null && parameters.length > 0)) {
			for (String param : parameters) {
				waitStr += param;
			}
		}
		String coverKey = StrMD5.getInstance().getStringMD5(waitStr);
		Map<String, String> params = new HashMap<String, String>();
		params.put("userId", userId.toString());
		params.put("nonce", nonce);
		params.put("requestId", requestId);
		params.put("coverKey", coverKey);
		return params;
	}
	/**
	 * 按字母顺序排序加密，生成9为nonce唯一标示requestId，md5加密方式为nonce + requestId + userId
	 * @param anchorId主播ID
	 * @return
	 */
	public static Map<String, String> defaultSortRequetParams(Long anchorId) {
		if (anchorId == null) {
			return new HashMap<String, String>();
		}
		String nonce =  getNonce(9);
		String requestId = getRequestId();
		String waitStr = anchorId + nonce + requestId;
		String coverKey = StrMD5.getInstance().getStringMD5(waitStr);
		Map<String, String> params = new HashMap<String, String>();
		params.put("userId", anchorId.toString());
		params.put("nonce", nonce);
		params.put("requestId", requestId);
		params.put("coverKey", coverKey);
		return params;
	}

	/**
	 * 
	 * 生成9为nonce唯一标示requestId，md5加密方式为nonce + requestId + userId + ...
	 * @param userId
	 * @param objs
	 * @return
	 */
	public static Map<String, String> requetParams(Long userId, Object... objs) {
		if (userId == null) {
			return new HashMap<String, String>();
		}
		String nonce =  getNonce(9);
		String requestId = getRequestId();
		String waitStr = userId + nonce + requestId ;
		for (Object o : objs) {
			waitStr += o;
		}
		String coverKey = StrMD5.getInstance().getStringMD5(waitStr);
		Map<String, String> params = new HashMap<String, String>();
		params.put("userId", userId.toString());
		params.put("nonce", nonce);
		params.put("requestId", requestId);
		params.put("coverKey", coverKey);
		return params;
	}
	
	/**
	 * 构造参数信息
	 * @param map 参数
	 * @return
	 */
	public static String buildParam(Map<String, String> map) {
		List<String> keys = new ArrayList<String>(map.keySet());

		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < keys.size() - 1; i++) {
			String key = keys.get(i);
			String value = map.get(key);
			sb.append(buildKeyValue(key, value, true));
			sb.append(Constants.AND);
		}

		String tailKey = keys.get(keys.size() - 1);
		String tailValue = map.get(tailKey);
		sb.append(buildKeyValue(tailKey, tailValue, true));

		return sb.toString();
	}

	/**
	 * 拼接键值对
	 * 
	 * @param key
	 * @param value
	 * @param isEncode
	 * @return
	 */
	private static String buildKeyValue(String key, String value,
			boolean isEncode) {
		StringBuilder sb = new StringBuilder();
		sb.append(key);
		sb.append(Constants.EQU);
		if (isEncode) {
			try {
				sb.append(URLEncoder.encode(value, Constants.UTF8));
			} catch (UnsupportedEncodingException e) {
				sb.append(value);
			}
		} else {
			sb.append(value);
		}
		return sb.toString();
	}

	/**
	 * 加密顺序为顺序加密
	 * @param userId
	 * @param map
	 * @return
	 */
	public static Map<String, String> dynamicRequetParams(Long userId, Map<String, String> map) {
		String nonce =  getNonce(9);
		String requestId = getRequestId();
		String waitStr = userId + nonce + requestId;

		Map<String, String> params = new HashMap<String, String>();
		params.put("userId", userId + "");
		params.put("nonce", nonce);
		params.put("requestId", requestId);
		
		if (map != null && !map.isEmpty()) {
			params.putAll(map);
			List<String> keys = new ArrayList<String>(params.keySet());
			// key排序
			Collections.sort(keys);
			for (int i = 0; i < keys.size() - 1; i++) {
				String key = keys.get(i);
				String value = map.get(key);
				waitStr += value;
			}
		}
		
		String coverKey = StrMD5.getInstance().getStringMD5(waitStr);
		params.put("coverKey", coverKey);
		
		return params;
	}
	
	
	private RequestParamsUtils() {}
	
}
