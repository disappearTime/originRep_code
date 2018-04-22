package com.chineseall.iwanvi.wwlive.web.common.security;

import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.chineseall.iwanvi.wwlive.common.cache.RedisClientAdapter;
import com.chineseall.iwanvi.wwlive.common.cache.RedisExpireTime;
import com.chineseall.iwanvi.wwlive.common.cache.RedisKey;
import com.chineseall.iwanvi.wwlive.common.tools.Base64Tools;

/**
 * 书城微信支付安全算法
 * @author DIKEPU
 *
 */
public class PaymentSecurity {

	/**
	 * 算法用base64生成参数，替换第5个参数为k
	 * 存储到redis中保存1小时，失效后通知就失败
	 * @param outTradeNo
	 * @param redisAdapter
	 * @return
	 */
	public static String makeSecurity(String outTradeNo, RedisClientAdapter redisAdapter) {
		if (outTradeNo == null || redisAdapter == null) {
			return null;
		}
		String tmp = Base64Tools.encode(outTradeNo.getBytes());
		if (tmp.length() > 5) {
			String secret = tmp.substring(0, 4);
			secret += "k";
			secret += tmp.substring(5);
			redisAdapter.strSet(RedisKey.SC_WX_SECRET_ + secret, outTradeNo);
			redisAdapter.expireKey(RedisKey.SC_WX_SECRET_ + secret, RedisExpireTime.EXPIRE_MIN_10);
			tmp = secret;
		}
		return tmp;
	}
	
	/**
	 * 缓存订单信息
	 * @param map
	 * @param outTradeNo
	 * @param redisAdapter
	 */
	public static void securityCache(Map<String, Object> map, String outTradeNo, RedisClientAdapter redisAdapter) {
		String code = AESCryptedCoder.encrypt(outTradeNo);
		redisAdapter.strSet(code, 0);
		redisAdapter.expireKey(code, RedisExpireTime.EXPIRE_DAY_2);
		map.put("fog", code);
		
	}
	
	public static String getClearText(String outTradeNo, RedisClientAdapter redisAdapter) {
		if (outTradeNo == null || redisAdapter == null) {
			return null;
		}
		if (outTradeNo.length() < 5) {
			return null;
		}
		String k = outTradeNo.substring(4, 5);
		if (!"k".equals(k)) {
			return null;
		}
		String key = RedisKey.SC_WX_SECRET_ + outTradeNo;
		if (!redisAdapter.existsKey(key)) {
			return null;
		}
		String clearText = redisAdapter.strGet(key);
		if (StringUtils.isBlank(clearText)) {
			return null;
		}
		if (clearText.contains("\"")) {
			clearText = clearText.replaceAll("\"", "");
		}
		return clearText;
	}
	
	
}
