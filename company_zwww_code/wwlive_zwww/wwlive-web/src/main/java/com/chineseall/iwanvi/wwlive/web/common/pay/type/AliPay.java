package com.chineseall.iwanvi.wwlive.web.common.pay.type;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSONObject;
import com.chineseall.iwanvi.wwlive.dao.wwlive.OrderInfoMapper;
import com.chineseall.iwanvi.wwlive.domain.wwlive.OrderInfo;
import com.chineseall.iwanvi.wwlive.web.common.pay.DefaultUserPay;
import com.chineseall.iwanvi.wwlive.web.common.pay.RSAConfig;
import com.chineseall.iwanvi.wwlive.web.common.pay.alipay.UserAliPay;

/**
 * 支付宝支付
 * @author DIKEPU
 * @since 2017-01-23 二期
 */
@Component("aliPay")
public class AliPay extends DefaultUserPay {

	/**
	 * 支付宝通知接口
	 */
	@Value("${alipay.notify.url}")
	private String notifyUrl;

	@Autowired
	private UserAliPay userAliPay;

	@Autowired
	private OrderInfoMapper orderInfoMapper;

	@Override
	public Map<String, Object> pay(OrderInfo order, String app) {
		JSONObject json = userAliPay.crateBizContent(order.getGoodsName(),
				order.getOutTradeNo(), new BigDecimal(order.getAmt()).divide(new BigDecimal(100)).setScale(2, //支付宝以元为单位
														BigDecimal.ROUND_HALF_UP).toString());
		
		Map<String, Object> map = new HashMap<String, Object>();// 结果Map
		Map<String, String> keyValues = userAliPay.buildOrderParamMap(json,
				order.getOutTradeNo(), notifyUrl);
		String orderParam = userAliPay.buildOrderParam(keyValues);
		String sign = userAliPay.getSign(keyValues,
				RSAConfig.IWANVI_PRIVATE_RSA);
		map.put("orderParam", orderParam);
		map.put("sign", sign);
		int cnt = orderInfoMapper.insertOrderInfo(order);
		map.put("result", cnt);
		return map;
	}
	
	
}
