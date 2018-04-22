package com.chineseall.iwanvi.wwlive.web.my.service.impl;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.chineseall.iwanvi.wwlive.common.cache.RedisClientAdapter;
import com.chineseall.iwanvi.wwlive.common.tools.PageInfo;
import com.chineseall.iwanvi.wwlive.dao.wwlive.AcctInfoMapper;
import com.chineseall.iwanvi.wwlive.dao.wwlive.RechargeInfoMapper;
import com.chineseall.iwanvi.wwlive.web.common.helper.UserAcctInfoHelper;
import com.chineseall.iwanvi.wwlive.web.my.service.MyAcctInfoService;

@Service
public class MyAcctInfoServiceImpl implements MyAcctInfoService {

	@Autowired
	AcctInfoMapper acctInfoMapper;

	@Autowired
	private RechargeInfoMapper chargeMapper;

	@Autowired
	private RedisClientAdapter redisAdapter;
	
	public Map<String, Object> getUserAcctInfo(long userId, String app) {
		//获得用户账户信息
		Map<String, Object> result = UserAcctInfoHelper.getUserAcctInfo(acctInfoMapper, 
				redisAdapter, userId);
		Map<String, Object> acctMap = UserAcctInfoHelper.getAcctInfoCoin(acctInfoMapper, redisAdapter, userId);
		Object objCoin = acctMap.get("coin");
		long coin = 0L;
		if (objCoin instanceof String) {
			coin = Long.valueOf((String) objCoin);
		} else if (objCoin instanceof Long){
			coin = (Long) objCoin;
		}
		// 定制版用户余额需要两位小数
		int scale = "dz".equalsIgnoreCase(app) ? 2 : 1;
		result.put("diamond", new BigDecimal(coin).divide(new BigDecimal(100), scale,
				BigDecimal.ROUND_HALF_DOWN));
		return result;
	}
	
	@Override
	public Map<String, Object> getExpenseList(Long userId, int origin, int pageNum,
			int pageSize) {
		int cnt = chargeMapper.countCharge(userId, origin);
		Map<String, Object> resultJson = new HashMap<>();
		if (cnt <= 0) {
			return resultJson;
		}
		List<Map<String, Object>> exchargeList = chargeMapper.getExchageList(
				userId, origin, (pageNum - 1) * pageSize, pageSize);
		PageInfo pageInfo = new PageInfo(pageNum, pageSize, cnt);
		resultJson.put("exchargeList", exchargeList);
		resultJson.put("pageInfo", pageInfo);
		return resultJson;
	}
	
}
