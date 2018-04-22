package com.chineseall.iwanvi.wwlive.web.common.charge.aftercharge;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.chineseall.iwanvi.wwlive.common.cache.RedisClientAdapter;
import com.chineseall.iwanvi.wwlive.common.cache.RedisKey;
import com.chineseall.iwanvi.wwlive.common.constants.Constants;
import com.chineseall.iwanvi.wwlive.dao.wwlive.AcctInfoMapper;
import com.chineseall.iwanvi.wwlive.dao.wwlive.RechargeInfoMapper;
import com.chineseall.iwanvi.wwlive.dao.wwlive.TransInfoMapper;
import com.chineseall.iwanvi.wwlive.domain.wwlive.AcctInfo;
import com.chineseall.iwanvi.wwlive.domain.wwlive.RechargeInfo;
import com.chineseall.iwanvi.wwlive.domain.wwlive.TransInfo;

/**
 * 完成用户充值后的逻辑，生成一条交易信息，更新用户账户信息
 * @author DIKEPU
 * @since 2017-02-04 二期
 */
@Component
public class AfterChargeTask {

    private static final Logger LOGGER = Logger.getLogger(AfterChargeTask.class);
	@Autowired
	private RedisClientAdapter redisAdapter;

	@Autowired
	private TransInfoMapper transInfoMapper;

	@Autowired
	private AcctInfoMapper acctInfoMapper;
	
	@Autowired
	private RechargeInfoMapper chargeMapper;
	
	/**
	 * 支付后完成的逻辑
	 * @param order
	 */
	public void afterCharge(RechargeInfo recharge) {
		LOGGER.info(recharge);
		if (recharge != null 
				&& Constants._0 == 
				(recharge.getRechargeStatus() == null ? 0 : recharge.getRechargeStatus())){
			buildTransInfo(recharge);
			upsertAcctInfo(recharge);
			updateRechargeInfo(recharge);
			delCache(recharge.getUserId());
		}
	}

	private void delCache(Long userId) {
		String key = RedisKey.USER_CTB_DO_ + userId;;
		String coinKey = RedisKey.USER_COIN_ + userId;
		redisAdapter.delKeys(key, coinKey);
	}
	/**
	 * 构建交易信息
	 * @param info
	 * @return
	 */
	private void buildTransInfo(RechargeInfo info) {
		TransInfo trans = new TransInfo();
		trans.setUserId(info.getUserId());
		trans.setOutId(info.getRechargeId());
		if (info.getOrigin() != null && 5 == info.getOrigin().intValue()) {
			trans.setTransType(Constants._2);//交易类型 1-积分, 2-微信, 3-支付宝, 4-微信SDK, 5-红包, 6贵族返钻
		} else if (info.getOrigin() != null && 6 == info.getOrigin().intValue()) {
			trans.setTransType(Constants._3);//0-消费 1充值 2红包 3贵族返钻
		} else {
			trans.setTransType(Constants._1);//交易类型 0-消费 1充值 2红包 3贵族返钻
		}
		trans.setTransStatus(Constants._0);
		trans.setAmt(info.getAmt());
		trans.setPayType(info.getOrigin());//支付类型  0-铜币, 1-积分, 2-微信, 3-支付宝
		transInfoMapper.insertTransInfo(trans);
	}
	
	/**
	 * 更新或创建用户账户表
	 * @param info
	 */
	private void upsertAcctInfo(RechargeInfo info) {
		AcctInfo acct = acctInfoMapper.findAcctByUserId(info.getUserId());
		if (acct != null) {
			acct.setVideoCoin(acct.getVideoCoin() + info.getRechargeAmount());
			int cnt = acctInfoMapper.updateAcctCoinById(acct);
			if (cnt <= 0) {
				LOGGER.error("充值失败：" + acct);
			}
		} else {
			acct = new AcctInfo();
			acct.setUserId(info.getUserId());
			acct.setVideoCoin(info.getRechargeAmount().longValue());//100coin为1钻
			acct.setAcctType(Constants._0);//0 普通
			acct.setAcctStatus(Constants._0);//0 正常 1冻结
			acctInfoMapper.insertAcctInfo(acct);
		}
	}
	
	public void updateRechargeInfo(RechargeInfo info) {
		RechargeInfo re =  new RechargeInfo();
		re.setRechargeId(info.getRechargeId());
		re.setReceiveNo(info.getReceiveNo());
		re.setRechargeStatus(Constants._1);// orderStatus 支付状态 0未支付， 1成功， 2失败， 3关闭
		re.setUserId(info.getUserId());
		chargeMapper.updateStatus(re);
	}
	
}
