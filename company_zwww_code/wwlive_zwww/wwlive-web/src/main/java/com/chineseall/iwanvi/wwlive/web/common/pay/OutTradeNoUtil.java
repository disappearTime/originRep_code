package com.chineseall.iwanvi.wwlive.web.common.pay;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.chineseall.iwanvi.wwlive.common.cache.RedisClientAdapter;
import com.chineseall.iwanvi.wwlive.common.exception.IWanviException;
import com.chineseall.iwanvi.wwlive.dao.base.mysql.MysqlSequenceGen;
import com.chineseall.iwanvi.wwlive.web.common.enums.PayType;

/**
 * 支付流水号工具，获得支付流水号
 * @author DIKEPU
 *
 */
@Component
public class OutTradeNoUtil {
	
	private static final Logger LOGGER = Logger.getLogger(OutTradeNoUtil.class);

	@Autowired
	private MysqlSequenceGen mysqlSequenceGen;

    @Autowired
    private RedisClientAdapter redisAdapter;
    
	@Value("${app.system}")
	private String appSystem;
	
	private static final int UPPER = 3000;
	
	private static final int LOWER = 0;

	private void outTradeNoCachePool(PayType payType) {
		//正式环境是 13
		int num = 12;
		if (Boolean.TRUE.toString().equals(appSystem)) {
			num = 13;
		}
		Lock lock = new ReentrantLock();
		lock.lock();
		try {
			if (redisAdapter.setCard(payType.getRedisKey()) > UPPER) {
				return;
			}
		} finally {
			lock.unlock();
		}
		String[] treadNos = 
				mysqlSequenceGen.getNextvals(payType.getSequenceName(), num, payType.getPrefix());
		if (treadNos != null 
				&& treadNos.length > 0) {
			redisAdapter.setAdd(payType.getRedisKey(), treadNos);
		}
	}
	
	public String getTradeNo(PayType payType) {
		String key =  payType.getRedisKey();
		String outTradeNo = "";
		if (redisAdapter.existsKey(key)) {
			long members = redisAdapter.setCard(key);
			if (LOWER < members && members < UPPER) {
				outTradeNoCachePool(payType);
			} 
		} else {
			outTradeNoCachePool(payType);
		}
		outTradeNo = redisAdapter.setPop(key);
		if (StringUtils.isBlank(outTradeNo)) {
			LOGGER.error("生成对外流水号失败。");
			throw new IWanviException("生成对外流水号失败。");
		}
		return outTradeNo;
	}
	
}
