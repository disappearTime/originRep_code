package com.chineseall.iwanvi.wwlive.web.video.service;

import com.chineseall.iwanvi.wwlive.domain.wwlive.OrderInfo;

public interface PayResultNoticeService {

	public OrderInfo updateOrderFromZFB(String tradeStatus,
			String outTradeNo, String tradeNo);
	
   	public OrderInfo updateOrderFromWX(String amount,
   			String orderId, String transactionId);
   	
   	/**
   	 * 查询微信订单是否支付成功
   	 * @param fog
   	 * @return
   	 */
   	public int wxNotifyQuery(String fog);

}
