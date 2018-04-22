package com.chineseall.iwanvi.wwlive.web.my.service.impl;

import com.chineseall.iwanvi.wwlive.common.cache.RedisClientAdapter;
import com.chineseall.iwanvi.wwlive.dao.wwlive.UserInfoMapper;
import com.chineseall.iwanvi.wwlive.domain.wwlive.UserInfo;
import com.chineseall.iwanvi.wwlive.web.common.helper.UserInfoHelper;
import com.chineseall.iwanvi.wwlive.web.otherapp.service.DZDataSyncService;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.chineseall.iwanvi.wwlive.common.constants.Constants;
import com.chineseall.iwanvi.wwlive.dao.wwlive.RechargeInfoMapper;
import com.chineseall.iwanvi.wwlive.domain.wwlive.RechargeInfo;
import com.chineseall.iwanvi.wwlive.web.common.charge.aftercharge.AfterChargeTask;
import com.chineseall.iwanvi.wwlive.web.common.pay.afterpay.AfterPayTask;
import com.chineseall.iwanvi.wwlive.web.my.service.ReChargeNoticeService;

/**
 * 充值结果通知
 * @author DIKEPU
 * @since 2017-02-04 二期
 */
@Service
public class ReChargeNoticeServiceImpl implements ReChargeNoticeService{

    private static final Logger LOGGER = Logger.getLogger(ReChargeNoticeServiceImpl.class);

	@Autowired
	private RechargeInfoMapper chargeMapper;

	@Autowired
	private AfterChargeTask afterChargeTask;

	@Autowired
	private AfterPayTask afterPayTask;

	@Autowired
	private RedisClientAdapter redisAdapter;

	@Autowired
	private UserInfoMapper userInfoMapper;

	@Autowired
	private DZDataSyncService dzDataSyncService;

	@Transactional
	public int updateReChargeFromZFB(String tradeStatus, String outTradeNo,
			String tradeNo) {
		LOGGER.info("支付宝充值触发异步通知, tradeStatus：" + tradeStatus + ", tradeStatus："
		+ tradeStatus + ", tradeNo：" + tradeNo);//防止纠纷，打印支付宝信息

		RechargeInfo info = chargeMapper.getRechargeInfoByOutNo(outTradeNo);
		if (info == null) {
			return 0;
		}
		
		RechargeInfo recharge = new RechargeInfo();
		recharge.setRechargeId(info.getRechargeId());
		recharge.setReceiveNo(tradeNo);

		if ("TRADE_FINISHED".equals(tradeStatus)) {
			recharge.setRechargeStatus(Constants._2);// orderStatus 支付状态 0未支付， 1成功， 2失败
			return chargeMapper.updateStatus(recharge);
		} else if ("TRADE_SUCCESS".equals(tradeStatus)) {
			info.setReceiveNo(tradeNo);

			// 和定制版同步消费数据
			Long userId = info.getUserId();
			UserInfo user = UserInfoHelper.getUserInfoFromCache(redisAdapter, userId);
			if (user == null ) {
				user = UserInfoHelper.getAndCacheUserInfo(redisAdapter, userInfoMapper, userId);
			}

			if (user.getOrigin() == 3) { // origin = 3, 用户来自定制版, 需要同步充值信息
				info.setOutTradeNo(outTradeNo);
				LOGGER.info("支付宝充值成功, 开始和定制版同步数据...");
				dzDataSyncService.syncRechargeData(info);
			}

			afterChargeTask.afterCharge(info);
			afterPayTask.receivePayMsg(outTradeNo);
		} else if ("TRADE_CLOSED".equals(tradeStatus)) {
			recharge.setRechargeStatus(Constants._0);
			return chargeMapper.updateStatus(recharge);
		}
		return 1;
	}

    @Override
    public boolean isDealt(String outTradeNo) {
        RechargeInfo rechargeInfo = chargeMapper.getRechargeInfoByOutNo(outTradeNo);
        if(rechargeInfo != null && rechargeInfo.getRechargeStatus() == 1){// 充值状态 = 1表示充值成功
            return true;
        }
        return false;
    }

    @Override
    public int updateRechargeFromWX(boolean success, String outTradeNo, String receiverId) {

        RechargeInfo info = chargeMapper.getRechargeInfoByOutNo(outTradeNo);        
        if (info == null) {
            return 0;
        }
        
        RechargeInfo recharge = new RechargeInfo();
        recharge.setRechargeId(info.getRechargeId()); 
        recharge.setReceiveNo(receiverId);
        if(success){
			info.setReceiveNo(receiverId);
            afterChargeTask.afterCharge(info);
            afterPayTask.receivePayMsg(outTradeNo);
            LOGGER.info("微信充值--回调处理, 数据库订单状态 = " + 1);
        } else{
            recharge.setRechargeStatus(Constants._2);
            LOGGER.info("微信充值--回调处理, 数据库订单状态 = " + 2);
            return chargeMapper.updateStatus(recharge);
        }
        return 1;
    }
    
    
}
