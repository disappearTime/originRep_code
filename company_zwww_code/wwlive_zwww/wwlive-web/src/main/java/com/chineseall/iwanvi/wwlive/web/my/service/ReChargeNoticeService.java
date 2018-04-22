package com.chineseall.iwanvi.wwlive.web.my.service;

/**
 * 充值通知
 * @author DIKEPU
 *
 */
public interface ReChargeNoticeService {
	
	int updateReChargeFromZFB(String tradeStatus, String outTradeNo,
			String tradeNo);

    boolean isDealt(String outTradeNo);

    int updateRechargeFromWX(boolean success, String outTradeNo, String receiverId);
	
}
