package com.chineseall.iwanvi.wwlive.web.common.charge.type;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSONObject;
import com.chineseall.iwanvi.wwlive.common.cache.RedisClientAdapter;
import com.chineseall.iwanvi.wwlive.dao.wwlive.RechargeInfoMapper;
import com.chineseall.iwanvi.wwlive.domain.wwlive.RechargeInfo;
import com.chineseall.iwanvi.wwlive.web.common.charge.DefaultUserCharge;
import com.chineseall.iwanvi.wwlive.web.common.pay.RSAConfig;
import com.chineseall.iwanvi.wwlive.web.common.pay.alipay.UserAliPay;
import com.chineseall.iwanvi.wwlive.web.common.security.PaymentSecurity;

@Component("aliCharge")
public class AliCharge extends DefaultUserCharge {

	/**
	 * 支付宝通知接口
	 */
	@Value("${alipay.recharge.notify.url}")
	private String notifyUrl;

	@Autowired
	private UserAliPay userAliPay;

	@Autowired
	private RechargeInfoMapper chargeMapper;

	@Autowired
	private RedisClientAdapter redisAdapter;
	
	@Override
	public Map<String, Object> resultMap(RechargeInfo info) {
		JSONObject json = userAliPay.crateBizContent("万维直播充值",
				info.getOutTradeNo(), new BigDecimal(info.getAmt())
						.divide(new BigDecimal(100)).setScale(2, //支付宝以元为单位
						BigDecimal.ROUND_HALF_UP).toString());
		
		Map<String, Object> map = new HashMap<String, Object>();// 结果Map
		Map<String, String> keyValues = userAliPay.buildOrderParamMap(json,
				info.getOutTradeNo(), notifyUrl);
		String orderParam = userAliPay.buildOrderParam(keyValues);
		String sign = userAliPay.getSign(keyValues,
				RSAConfig.IWANVI_PRIVATE_RSA);
		map.put("orderParam", orderParam);
		map.put("sign", sign);
		int cnt = chargeMapper.insertRechargeInfo(info);
		map.put("result", cnt);

		String outTradeNo = info.getOutTradeNo();
		outTradeNo = PaymentSecurity.makeSecurity(outTradeNo, redisAdapter);
		PaymentSecurity.securityCache(map, outTradeNo, redisAdapter);//微信加密机制
		return map;
	}

}
