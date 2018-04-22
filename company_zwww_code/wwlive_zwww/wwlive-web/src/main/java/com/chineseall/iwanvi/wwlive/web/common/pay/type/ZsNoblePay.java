package com.chineseall.iwanvi.wwlive.web.common.pay.type;

import java.util.HashMap;
import java.util.Map;

import com.chineseall.iwanvi.wwlive.domain.wwlive.AcctInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.chineseall.iwanvi.wwlive.common.cache.RedisClientAdapter;
import com.chineseall.iwanvi.wwlive.common.cache.RedisKey;
import com.chineseall.iwanvi.wwlive.dao.wwlive.AcctInfoMapper;
import com.chineseall.iwanvi.wwlive.dao.wwlive.OrderInfoMapper;
import com.chineseall.iwanvi.wwlive.domain.wwlive.OrderInfo;
import com.chineseall.iwanvi.wwlive.web.common.helper.UserAcctInfoHelper;
import com.chineseall.iwanvi.wwlive.web.common.pay.DefaultUserPay;
import com.chineseall.iwanvi.wwlive.web.nobility.common.NobilityCommon;

/**
 * 用钻石购买贵族称号
 * @author DIKEPU
 *
 */
@Component("zsNoblePay")
public class ZsNoblePay extends DefaultUserPay {

	@Autowired
	private NobilityCommon nobilityCommon;

	@Autowired
	private AcctInfoMapper acctInfoMapper;

	@Autowired
	private RedisClientAdapter redisAdapter;

	@Autowired
	private OrderInfoMapper orderInfoMapper;
	
	@Override
	public Map<String, Object> pay(OrderInfo order, String app) {
		//1.先检查用户的coin是否可以购买 2.充足购买 3.返回成功标志
		//0 失败 1成功 2coin不足
		Map<String, Object> result = new HashMap<String, Object>();
		
		Map<String, Object> acctMap = UserAcctInfoHelper.getAcctInfoCoin(acctInfoMapper, redisAdapter, order.getUserId());
		if (acctMap == null || acctMap.isEmpty()) {
			result.put("result", 2);
			return result;
		}
		
		Object objCoin = acctMap.get("coin");
		long coin = 0L;
		if (objCoin instanceof String) {
			coin = Long.valueOf((String) objCoin);
		} else if (objCoin instanceof Long){
			coin = (Long) objCoin;
		}

		//扣除金额
		long reduce = order.getAmt();
		if (coin < reduce) {
			result.put("result", 2);
			return result;
		}
		int cnt = orderInfoMapper.insertOrderInfo(order);
		if (cnt <= 0) {
			result.put("result", 0);
			return result;
		}
		//获取用户账户信息
		AcctInfo acct = acctInfoMapper.findAcctByUserId(order.getUserId());
		//减去支付的金额
		if(acct != null) {
			acct.setVideoCoin(acct.getVideoCoin() - reduce);
			cnt = acctInfoMapper.updateAcctCoinById(acct);
		}
		if (cnt > 0) {
			cnt = nobilityCommon.updateNobilityOrderInfo(order);
		}
		if (cnt > 0) {
			//用户coin
			String coinKey = RedisKey.USER_COIN_ + order.getUserId();
			redisAdapter.delKeys(coinKey);
			result.put("result", 1);
		} else {
			result.put("result", 0);
		}
		
		return result;
	}

}
