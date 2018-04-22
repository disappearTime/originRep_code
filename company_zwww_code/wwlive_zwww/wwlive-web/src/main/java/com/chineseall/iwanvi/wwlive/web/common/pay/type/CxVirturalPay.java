package com.chineseall.iwanvi.wwlive.web.common.pay.type;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.chineseall.iwanvi.wwlive.common.constants.Constants;
import com.chineseall.iwanvi.wwlive.dao.wwlive.OrderInfoMapper;
import com.chineseall.iwanvi.wwlive.domain.wwlive.OrderInfo;
import com.chineseall.iwanvi.wwlive.web.common.pay.DefaultUserPay;
import com.chineseall.iwanvi.wwlive.web.common.pay.afterpay.AfterPayTask;
import com.chineseall.iwanvi.wwlive.web.common.pay.virtual.UserCxVirturalPay;

/**
 * 创新版积分支付
 * @author DIKEPU
 * @since 2017-01-23 二期
 */
@Component("cxVirturalPay")
public class CxVirturalPay extends DefaultUserPay {

	@Autowired
	private UserCxVirturalPay userCxVirturalPay;

	@Value("${cx.ratio}")
	private String cxRatio;//比例

	@Autowired
	private OrderInfoMapper orderInfoMapper;
	
	@Autowired
	private AfterPayTask afterPayTask;

	@Override
	public Map<String, Object> pay(OrderInfo order, String app) {
		
		// 虚拟货币
		long virtualCurrency = new BigDecimal(order.getAmt()).multiply(new BigDecimal(cxRatio))
				.divide(new BigDecimal(100))
				.setScale(0, BigDecimal.ROUND_HALF_UP).longValue();

		int update = userCxVirturalPay.updateUserVirtural(order.getUserId(),
				virtualCurrency);// 扣除该用户积分

		Map<String, Object> result = new HashMap<String, Object>();//0失败, 1成功
		if (update == 1) {
			result.put("result", 0);
		} else {
			order.setOrderStatus(Constants._1);// orderStatus 支付状态 0未支付，
												// 1成功，2失败， 3关闭， 4异常
			order.setAmt(virtualCurrency);
			int cnt = orderInfoMapper.insertOrderInfo(order);
			if (cnt > 0) {
				afterPayTask.afterPay(order);//支付完成后的逻辑
			}
			
			result.put("result", cnt);
		}
		
		return result;
	}
	
}
