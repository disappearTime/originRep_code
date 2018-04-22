package com.chineseall.iwanvi.wwlive.web.common.charge.type;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSONObject;
import com.chineseall.iwanvi.wwlive.common.cache.RedisClientAdapter;
import com.chineseall.iwanvi.wwlive.dao.wwlive.RechargeInfoMapper;
import com.chineseall.iwanvi.wwlive.domain.wwlive.RechargeInfo;
import com.chineseall.iwanvi.wwlive.web.common.charge.DefaultUserCharge;
import com.chineseall.iwanvi.wwlive.web.common.pay.weixin.UserWXPay;
import com.chineseall.iwanvi.wwlive.web.common.security.PaymentSecurity;

@Component("wxCharge")
public class WxCharge extends DefaultUserCharge {

	@Autowired
	private UserWXPay userWXPay;

	@Autowired
	private RechargeInfoMapper chargeMapper;

	@Autowired
	private RedisClientAdapter redisAdapter;
	
	@Override
	public Map<String, Object> resultMap(RechargeInfo info) {
		String outTradeNo = info.getOutTradeNo();

		//1. 由于书城的微信支付未加验证，故做算法校验
		outTradeNo = PaymentSecurity.makeSecurity(outTradeNo, redisAdapter);

		JSONObject json = userWXPay.getWXWapURL(info.getAmt(),
				outTradeNo);
		Map<String, Object> map = new HashMap<String, Object>();// 结果Map
		if (json != null) {
			map.putAll(json);
		}
		
		PaymentSecurity.securityCache(map, outTradeNo, redisAdapter);//微信加密机制
		int cnt = chargeMapper.insertRechargeInfo(info);
		map.put("result", cnt);
		return map;
	}

}
