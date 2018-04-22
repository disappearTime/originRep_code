package com.chineseall.iwanvi.wwlive.web.common.pay.type;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSONObject;
import com.chineseall.iwanvi.wwlive.common.cache.RedisClientAdapter;
import com.chineseall.iwanvi.wwlive.dao.wwlive.OrderInfoMapper;
import com.chineseall.iwanvi.wwlive.domain.wwlive.OrderInfo;
import com.chineseall.iwanvi.wwlive.web.common.pay.DefaultUserPay;
import com.chineseall.iwanvi.wwlive.web.common.pay.weixin.UserWXPay;
import com.chineseall.iwanvi.wwlive.web.common.security.PaymentSecurity;

/**
 * 微信支付
 * @author DIKEPU
 * @since 2017-01-23 二期
 */
@Component("wxPay")
public class WxPay extends DefaultUserPay {

	@Autowired
	private RedisClientAdapter redisAdapter;

	@Autowired
	private UserWXPay userWXPay;

	@Autowired
	private OrderInfoMapper orderInfoMapper;
	
	/**
	 * 发送到中文书城微信前outTradeNo加密处理
	 */
	@Override
	public Map<String, Object> pay(OrderInfo order, String app) {

		String outTradeNo = order.getOutTradeNo();
		
		//1. 由于书城的微信支付未加验证，故做算法校验
		outTradeNo = PaymentSecurity.makeSecurity(outTradeNo, redisAdapter);

		Map<String, Object> map = new HashMap<String, Object>();// 结果Map
		if (outTradeNo == null) {
			map.put("result", 0);
			return map;
		}
		JSONObject json = userWXPay.getWXWapURL(order.getAmt(), outTradeNo);
		if (json != null) {
			map.putAll(json);
		}
		
		PaymentSecurity.securityCache(map, outTradeNo, redisAdapter);//微信加密机制
		
		int cnt = orderInfoMapper.insertOrderInfo(order);
		map.put("result", cnt);
		return map;
	}

}
