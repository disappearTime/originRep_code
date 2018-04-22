package com.chineseall.iwanvi.wwlive.web.common.pay.weixin;

import java.net.HttpURLConnection;
import java.net.URLEncoder;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSONObject;
import com.chineseall.iwanvi.wwlive.common.constants.Constants;
import com.chineseall.iwanvi.wwlive.common.exception.IWanviException;
import com.chineseall.iwanvi.wwlive.common.tools.HttpUtils;


/**
 * 微信支付，使用方式，1.先获得orderid，2.然后组成url请求地址，3.通过url请求中文书城服务器返回json数据，4.通过返回的json调起微信支付。
 * <p/>
 * 通过中文书城微信支付接口实现
 * @author DIKEPU
 * @since 2016-10-09
 * @version 1.0
 */
@Component
public class UserWXPay {
	private static final Logger LOGGER = Logger.getLogger(UserWXPay.class);

	/**
	 * 微信支付请求地址
	 */
	@Value("${wxpay.request.url}")
	private String wxPayUrl;
	
	/**
	 * 生成微信的wap_url信息，通过wap_url调起微信支付
	 * @param amount 支付金额
	 * @param orderId
	 * @return { "errcode":{"code":0,"msg":""}, "order_id":"123xxx", "code_url":"", "prepay_id":"", "trade_type":"", "app_param":"", "wap_url":"weixin://wap/pay?appid%3d" }
	 */
	public JSONObject getWXWapURL(long amount, String orderId) {
		if (amount <= 0) {
			LOGGER.error("amount不合法，金额小于等于0");
			throw new IWanviException("amount不合法，金额小于等于0");
		}
		if (StringUtils.isBlank(orderId)) {
			LOGGER.error("orderId错误");
			throw new IWanviException("orderId错误");
		}
		//生成订单
		try {
			String uri =  wxPayUrl + "&amount=" + amount + "&orderid=" 
					+ orderId + "&title=" + URLEncoder.encode("万维直播支付", Constants.UTF8);
			HttpURLConnection conn = HttpUtils.createGetHttpConnection(uri, Constants.UTF8);
			String result = HttpUtils.returnString(conn);//{ "errcode":{"code":0,"msg":""}, "order_id":"123xxx", "code_url":"", "prepay_id":"", "trade_type":"", "app_param":"", "wap_url":"weixin://wap/pay?appid%3dwxcc0e0a063cc8d985%26noncestr%3d6dd64e42656b4196b96cbb5550882cbc%26package%3dWAP%26prepayid%3dwx201509241936268b236509f90956498287%26timestamp%3d1443094590%26sign%3d9596194536C59221F9437500D5129ABD" }
			if (StringUtils.isNotBlank(result)) {
				return JSONObject.parseObject(result);
			}
		} catch (Exception e) {
			LOGGER.error("生成微信支付订单错误");
			throw new IWanviException("生成微信支付订单错误");
		}
		
		return null;
	}
	
}
