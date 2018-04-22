package com.chineseall.iwanvi.wwlive.web.my.service;

import java.util.Map;

import com.chineseall.iwanvi.wwlive.domain.wwlive.RechargeInfo;

import javax.servlet.http.HttpServletRequest;

public interface ReChargeService {

	Map<String, Object> reCharge(long goodsId, RechargeInfo info, int amt);

	Map<String, Object> getRechageList();

	/**
	 * 积分兑换钻石
	 * @param userId
	 * @return
	 */
	Map<String, String> exchangePage(Long userId);

    boolean isPaid(String outTradeNo);

    /*void syncDataWithDZ(Map<String, Object> map);*/
}
