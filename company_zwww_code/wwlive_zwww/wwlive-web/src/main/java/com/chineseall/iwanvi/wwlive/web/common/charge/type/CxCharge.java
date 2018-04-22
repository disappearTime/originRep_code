package com.chineseall.iwanvi.wwlive.web.common.charge.type;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.chineseall.iwanvi.wwlive.common.constants.Constants;
import com.chineseall.iwanvi.wwlive.dao.wwlive.AcctInfoMapper;
import com.chineseall.iwanvi.wwlive.dao.wwlive.RechargeInfoMapper;
import com.chineseall.iwanvi.wwlive.dao.wwlive.TransInfoMapper;
import com.chineseall.iwanvi.wwlive.domain.wwlive.RechargeInfo;
import com.chineseall.iwanvi.wwlive.web.common.charge.DefaultUserCharge;
import com.chineseall.iwanvi.wwlive.web.common.charge.aftercharge.AfterChargeTask;
import com.chineseall.iwanvi.wwlive.web.common.pay.virtual.UserCxVirturalPay;
import com.chineseall.iwanvi.wwlive.web.common.util.RequestParamsUtils;

/**
 * 积分充值钻石
 * @author DIKEPU
 * @since 2017-01-06 二期
 */
@Component("cxCharge")
public class CxCharge extends DefaultUserCharge {

	@Autowired
	private UserCxVirturalPay userCxVirturalPay;

	@Autowired
	private RechargeInfoMapper chargeMapper;
	
	@Autowired
	private TransInfoMapper transInfoMapper;
	
	@Autowired
	private AcctInfoMapper acctInfoMapper;

	@Autowired
	private AfterChargeTask afterChargeTask;
	
	@Value("${cx.ratio}")
	private String cxRatio;
	
	@Override
	public Map<String, Object> resultMap(RechargeInfo info) {
		// 虚拟货币
		long virtualCurrency = new BigDecimal(info.getAmt()).multiply(new BigDecimal(cxRatio))
				.divide(new BigDecimal(100))
				.setScale(0, BigDecimal.ROUND_HALF_UP).longValue();

		int result = userCxVirturalPay.updateUserVirtural(info.getUserId(),
				virtualCurrency);// 扣除该用户积分
		info.setRechargeStatus(Constants._0);// //0未支付，1成功，2失败
		info.setAmt(virtualCurrency);//虚拟货币
		int cnt = chargeMapper.insertRechargeInfo(info);
		Map<String, Object> map = new HashMap<String, Object>();// 结果Map
		if (result == 1) {// 0成功 1失败
			map.put("result", 0);
		} else {
			afterChargeTask.afterCharge(info);//充值后完成的逻辑
			map.put("result", cnt);
		}
		//生成跳转参数
		map.putAll(RequestParamsUtils.defaultRequetParams(info.getUserId()));
		return map;
	}
	
}
