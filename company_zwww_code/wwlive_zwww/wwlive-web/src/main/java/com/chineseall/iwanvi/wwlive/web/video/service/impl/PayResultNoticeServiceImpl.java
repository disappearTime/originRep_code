package com.chineseall.iwanvi.wwlive.web.video.service.impl;

import com.chineseall.iwanvi.wwlive.common.cache.RedisClientAdapter;
import com.chineseall.iwanvi.wwlive.common.cache.RedisExpireTime;
import com.chineseall.iwanvi.wwlive.common.cache.RedisKey;
import com.chineseall.iwanvi.wwlive.common.constants.Constants;
import com.chineseall.iwanvi.wwlive.dao.wwlive.*;
import com.chineseall.iwanvi.wwlive.domain.wwlive.OrderInfo;
import com.chineseall.iwanvi.wwlive.domain.wwlive.RechargeInfo;
import com.chineseall.iwanvi.wwlive.domain.wwlive.UserInfo;
import com.chineseall.iwanvi.wwlive.web.common.charge.aftercharge.AfterChargeTask;
import com.chineseall.iwanvi.wwlive.web.common.constants.WebConstants;
import com.chineseall.iwanvi.wwlive.web.common.helper.UserInfoHelper;
import com.chineseall.iwanvi.wwlive.web.common.pay.afterpay.AfterPayTask;
import com.chineseall.iwanvi.wwlive.web.common.security.AESCryptedCoder;
import com.chineseall.iwanvi.wwlive.web.common.security.PaymentSecurity;
import com.chineseall.iwanvi.wwlive.web.nobility.common.NobilityCommon;
import com.chineseall.iwanvi.wwlive.web.otherapp.service.DZDataSyncService;
import com.chineseall.iwanvi.wwlive.web.video.service.PayResultNoticeService;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PayResultNoticeServiceImpl implements PayResultNoticeService {

    private static final Logger LOGGER = Logger.getLogger(PayResultNoticeServiceImpl.class);

	@Autowired
	private OrderInfoMapper orderInfoMapper;
	
	@Autowired
	private RedisClientAdapter redisAdapter;

	@Autowired
	private AfterPayTask afterPayTask;

	@Autowired
	private AfterChargeTask afterChargeTask;

	@Autowired
	private RechargeInfoMapper chargeMapper;
	@Autowired
	private NobilityCommon nobilityCommon;

	@Autowired
	private LiveVideoInfoMapper liveVideoInfoMapper;

    @Autowired
    private RoleInfoMapper roleInfoMapper;

    @Autowired
    private UserInfoMapper userInfoMapper;

    @Autowired
    private DZDataSyncService dzDataSyncService;
	/**
	 * 支付宝通知信息
	 */
	@Transactional
	public OrderInfo updateOrderFromZFB(String tradeStatus, String outTradeNo,
			String tradeNo) {
		LOGGER.info("支付宝触发异步通知, tradeStatus：" + tradeStatus + ", tradeStatus："
		+ tradeStatus + ", tradeNo：" + tradeNo);//防止纠纷，打印支付宝信息
		
		OrderInfo orderInfo = new OrderInfo();
		
		OrderInfo order = orderInfoMapper.getOrderInfoByOutNo(outTradeNo);
		if (order == null) {
			return orderInfo;
		}

		String dealPayKey = RedisKey.PAY_NOTICE_DEAL_ + tradeNo;
		if(redisAdapter.existsKey(dealPayKey)) {
			return orderInfo;
		} else {
			redisAdapter.strSetexByNormal(dealPayKey, RedisExpireTime.EXPIRE_MIN_10, "0");
		}
		//贵族订单逻辑处理
		if(outTradeNo.startsWith(WebConstants.NB)){
			if ("TRADE_FINISHED".equals(tradeStatus)) {
				orderInfo.setOrderStatus(Constants._3);// orderStatus 支付状态 0未支付， 1成功， 2失败， 3关闭， 4异常
				orderInfoMapper.updateStatus(orderInfo);
			} else if ("TRADE_SUCCESS".equals(tradeStatus)) {
				if (order == null || order.getOrderStatus() == 1) {
					LOGGER.info("直播支付宝支付触发异步通知，orderId：" + outTradeNo + "，不存在");
					return orderInfo;
				}
				orderInfo.setOrderId(order.getOrderId());
				orderInfo.setOrderStatus(Constants._1);// orderStatus 支付状态 0未支付， 1成功，
				// 2失败， 3关闭
				orderInfo.setReceiveNo(tradeNo);
				orderInfoMapper.updateStatus(orderInfo);

				//处理完成biz_order_info后，更新其他相关表的业务逻辑
				OrderInfo oif = orderInfoMapper.getOrderInfoByOutNo(outTradeNo);
				nobilityCommon.updateNobilityOrderInfo(oif);
				redisAdapter.strSetexByNormal(RedisKey.PAY_NOTICE_ + orderInfo.getOutTradeNo(), RedisExpireTime.EXPIRE_DAY_1 , "0");
				return oif;

			} else if ("TRADE_CLOSED".equals(tradeStatus)) {
				orderInfo.setOrderStatus(Constants._2);
				orderInfoMapper.updateStatus(orderInfo);

			}
			redisAdapter.delKeys(dealPayKey);
			return orderInfo;

		}

		if (order == null 
				|| (order.getOrderStatus() != null && order.getOrderStatus().intValue() == 1)) {
			LOGGER.info("支付宝支付通知信息触发异步通知，tradeNo：" + tradeNo + "，不存在或已完成");
			return orderInfo;
		}
		orderInfo.setOrderId(order.getOrderId());
		if ("TRADE_FINISHED".equals(tradeStatus)) {
			orderInfo.setOrderStatus(Constants._3);// orderStatus 支付状态 0未支付， 1成功， 2失败， 3关闭， 4异常
			orderInfoMapper.updateStatus(orderInfo);

		} else if ("TRADE_SUCCESS".equals(tradeStatus)) {
			orderInfo.setOrderStatus(Constants._1);// orderStatus 支付状态 0未支付， 1成功， 2失败， 3关闭
			orderInfo.setReceiveNo(tradeNo);
			
			orderInfoMapper.updateStatus(orderInfo);
			afterPayTask.afterPay(order);//支付完成后的逻辑
			redisAdapter.delKeys(dealPayKey);
		} else if ("TRADE_CLOSED".equals(tradeStatus)) {
			orderInfo.setOrderStatus(Constants._2);
			orderInfoMapper.updateStatus(orderInfo);
		}
		redisAdapter.delKeys(dealPayKey);

		return order;
	}

	/**
	 * 中文书城微信支付通知
	 * 
	 * @param
	 * @param orderId
	 * @param transactionId
	 */
	@Transactional
	public OrderInfo updateOrderFromWX(String amount, String orderId,
			String transactionId) {
		LOGGER.info("中文书城微信支付触发异步通知，amount：" + amount + ", orderId：" + orderId
				+ "，transactionId：" + transactionId);
		OrderInfo orderInfo = new OrderInfo();
		if (StringUtils.isEmpty(amount)) {
			return orderInfo;
		}
		
		orderId = PaymentSecurity.getClearText(orderId, redisAdapter);
		if (StringUtils.isBlank(orderId)) {
			return orderInfo;
		}
		String dealPayKey = RedisKey.PAY_NOTICE_DEAL_ + transactionId;
		if(redisAdapter.existsKey(dealPayKey)) {
			return orderInfo;
		} else {
			redisAdapter.strSetexByNormal(dealPayKey, RedisExpireTime.EXPIRE_MIN_10, "0");
		}
		
		if (orderId.contains("\"")) {
			orderId = orderId.replace("\"", "");
		}
		
		if (orderId.startsWith(WebConstants.CZ)) {// 中文书城微信充值逻辑
			RechargeInfo info = chargeMapper.getRechargeInfoByOutNo(orderId);
			if (info == null 
					|| (info.getRechargeStatus() == null ? 0 : info.getRechargeStatus()) == 1) {//充值状态，0未支付，1成功，2失败
				return orderInfo;
			}
			info.setReceiveNo(transactionId);

			// 和定制版同步消费数据
			Long userId = info.getUserId();
			UserInfo user = UserInfoHelper.getUserInfoFromCache(redisAdapter, userId);
			if (user == null ) {
				user = UserInfoHelper.getAndCacheUserInfo(redisAdapter, userInfoMapper, userId);
			}

			if (user.getOrigin() == 3) { // origin = 3, 用户来自定制版, 需要同步充值信息
				info.setOutTradeNo(orderId);
				LOGGER.info("微信充值成功, 开始和定制版同步数据...");
				dzDataSyncService.syncRechargeData(info);
			}

			afterChargeTask.afterCharge(info);
			afterPayTask.receivePayMsg(orderId);
			redisAdapter.delKeys(dealPayKey);
			redisAdapter.strSetexByNormal(RedisKey.PAY_NOTICE_ + orderId, RedisExpireTime.EXPIRE_DAY_1 , "0");
			return orderInfo;
		}

		
		OrderInfo order = orderInfoMapper.getOrderInfoByOutNo(orderId);
		if (order == null 
				|| (order.getOrderStatus() != null && order.getOrderStatus().intValue() == 1)) {
			LOGGER.info("中文书城微信支付触发异步通知，orderId：" + orderId + "，不存在");
			return orderInfo;
		}

		//贵族订单逻辑处理
		if(orderId.startsWith(WebConstants.NB)){
			if (order == null || (order.getOrderStatus() != null && order.getOrderStatus().intValue() == 1)) {
				LOGGER.info("直播微信支付触发异步通知，orderId：" + orderId + "，不存在或已完成");
				return orderInfo;
			}
			orderInfo.setOrderId(order.getOrderId());
			orderInfo.setOrderStatus(Constants._1);// orderStatus 支付状态 0未支付， 1成功，
			// 2失败， 3关闭
			orderInfo.setReceiveNo(transactionId);
			orderInfoMapper.updateStatus(orderInfo);

			//处理完成biz_order_info后，更新其他相关表的业务逻辑
		    OrderInfo oif = orderInfoMapper.getOrderInfoByOutNo(orderId);
			nobilityCommon.updateNobilityOrderInfo(oif);
			redisAdapter.delKeys(dealPayKey);
			redisAdapter.strSetexByNormal(RedisKey.PAY_NOTICE_ + orderId, RedisExpireTime.EXPIRE_DAY_1 , "0");
		    return oif;
		}
		orderInfo.setOrderId(order.getOrderId());
		orderInfo.setOrderStatus(Constants._1);// orderStatus 支付状态 0未支付， 1成功，
												// 2失败， 3关闭
		orderInfo.setReceiveNo(transactionId);
		orderInfoMapper.updateStatus(orderInfo);
		
		afterPayTask.afterPay(order);//支付完成后的逻辑
		afterPayTask.receivePayMsg(orderId);
		redisAdapter.delKeys(dealPayKey);
		redisAdapter.strSetexByNormal(RedisKey.PAY_NOTICE_ + orderId, RedisExpireTime.EXPIRE_DAY_1 , "0");
		return order;
	}
	
	@Override
	public int wxNotifyQuery(String fog) {
		//0.等待 1.成功2.不存在
		if (redisAdapter.existsKey(fog)) {
			String secret = AESCryptedCoder.decrypt(fog);
			String orderId = PaymentSecurity.getClearText(secret, redisAdapter);
			String orderKey = RedisKey.PAY_NOTICE_ + orderId;
			if (redisAdapter.existsKey(orderKey)) {
				return 1;
			}
			return 0;
		} else {
			return 2;
		}

	}
}
