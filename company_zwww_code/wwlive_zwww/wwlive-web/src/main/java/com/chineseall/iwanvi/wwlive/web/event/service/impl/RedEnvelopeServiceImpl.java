package com.chineseall.iwanvi.wwlive.web.event.service.impl;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.chineseall.iwanvi.wwlive.common.cache.RedisClientAdapter;
import com.chineseall.iwanvi.wwlive.dao.wwlive.RechargeInfoMapper;
import com.chineseall.iwanvi.wwlive.domain.wwlive.RechargeInfo;
import com.chineseall.iwanvi.wwlive.web.common.charge.aftercharge.AfterChargeTask;
import com.chineseall.iwanvi.wwlive.web.event.service.RedEnvelopeService;

/**
 * 处理客户端的抢红包逻辑
 * @author DIKEPU
 * @since 2017年10月26日
 */
@Service
public class RedEnvelopeServiceImpl implements RedEnvelopeService {

    @Autowired
    private RedisClientAdapter redisAdapter;
	
	@Autowired
	private RechargeInfoMapper rechargeInfoMapper;

	@Autowired
	private AfterChargeTask afterChargeTask;
	
	@Override
	public Map<String, Object> getLuckyDiamond(String reKey, Long userId) {
		String outTradeNo = redisAdapter.setPop(reKey);//充值的外键
		Map<String, Object> result = new HashMap<String, Object>();
		result.put("luckyResult", new Integer(0));
		result.put("luckyDiamonds", new Integer(0));
		if (StringUtils.isNotBlank(outTradeNo)) {
			RechargeInfo info = rechargeInfoMapper.getRechargeInfoByOutNo(outTradeNo);
			if (info == null || (info.getRechargeStatus() != null && 1 == info.getRechargeStatus())) {
				return result;
			}
			info.setUserId(userId);
			afterChargeTask.afterCharge(info);
			result.put("luckyResult", new Integer(1));
			result.put("luckyDiamonds", getTotalDiamonds(info.getRechargeAmount()));
		}
		return result;
	}
	
	/**
	 * 分转钻石
	 * @param rechargeAmount
	 * @return
	 */
	private double getTotalDiamonds(Long rechargeAmount) {
		return new BigDecimal(rechargeAmount)
		.divide(new BigDecimal(100)).setScale(2, 
		BigDecimal.ROUND_HALF_UP).doubleValue();
	}
	
}
