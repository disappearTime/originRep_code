package com.chineseall.iwanvi.wwlive.web.video.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.chineseall.iwanvi.wwlive.common.cache.RedisClientAdapter;
import com.chineseall.iwanvi.wwlive.common.cache.RedisKey;
import com.chineseall.iwanvi.wwlive.common.tools.RongMsgUtils;
import com.chineseall.iwanvi.wwlive.dao.wwlive.ContributionListMapper;
import com.chineseall.iwanvi.wwlive.dao.wwlive.GoodsInfoMapper;
import com.chineseall.iwanvi.wwlive.dao.wwlive.LiveVideoInfoMapper;
import com.chineseall.iwanvi.wwlive.domain.wwlive.GoodsInfo;
import com.chineseall.iwanvi.wwlive.domain.wwlive.LiveVideoInfo;
import com.chineseall.iwanvi.wwlive.domain.wwlive.OrderInfo;
import com.chineseall.iwanvi.wwlive.web.common.enums.PayType;
import com.chineseall.iwanvi.wwlive.web.common.helper.GoodsInfoHelper;
import com.chineseall.iwanvi.wwlive.web.common.helper.LiveVideoInfoHelper;
import com.chineseall.iwanvi.wwlive.web.common.pay.DefaultUserPay;
import com.chineseall.iwanvi.wwlive.web.common.pay.OutTradeNoUtil;
import com.chineseall.iwanvi.wwlive.web.common.util.SpringContextUtils;
import com.chineseall.iwanvi.wwlive.web.video.service.NoticeService;
import com.chineseall.iwanvi.wwlive.web.video.service.PayService;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;

import java.util.HashMap;
import java.util.Map;

/**
 * 支付功能
 * <p/>
 * 增加通知机器人功能及通知有人送礼通知
 * @author DIKEPU
 *
 */
@Service
public class PayServiceImpl implements PayService {

	public static final Logger LOGGER = Logger.getLogger(PayServiceImpl.class);
	/**
	 * 支付宝通知接口
	 */
	@Value("${alipay.notify.url}")
	private String notifyUrl;

	@Autowired
	private GoodsInfoMapper goodsInfoMapper;

	@Autowired
	private OutTradeNoUtil outTradeNoUtil;
	
	@Autowired
	private RedisClientAdapter redisAdapter;

	@Autowired
    private NoticeService noticeService;

	@Autowired
	private LiveVideoInfoMapper liveVideoInfoMapper;

	@Autowired
	private ContributionListMapper contribMapper;

	@Autowired
	private ThreadPoolTaskExecutor taskExecutor;

	/**
	 * 通过支付宝、微信、或者是积分添加订单信息
	 */
	@Transactional
	public Map<String, Object> toPay(HttpServletRequest request) {
		// 1. 获得商品名称 2.生成订单信息 3.返回客户端结果


		Long goodsId = Long.valueOf(request.getParameter("goodsId"));
		Long userId = Long.valueOf(request.getParameter("userId"));
		Long videoId = Long.valueOf(request.getParameter("videoId"));
		Long anchorId = Long.valueOf(request.getParameter("anchorId"));
		Integer goodsNum = Integer.parseInt(request.getParameter("goodsNum"));
		Integer payType = Integer.parseInt(request.getParameter("payType"));
		String app = request.getParameter("app");

		GoodsInfo goods = null;
		// 1. 获得商品名称
		if(redisAdapter.existsKey(RedisKey.GOODS_INFO_ + goodsId)) {
			goods = GoodsInfoHelper.getGoodsInfo(redisAdapter, 
					goodsId, "goodsId", "goodsName", "goodsImg", "goodsPrice", "discount", "goodsType");
		} else {
			goods = GoodsInfoHelper.getAndCacheGoodsInfo(redisAdapter, goodsInfoMapper, goodsId);
		}
		
		if (goods != null) {
			if (goodsNum < 0) {
				Map<String, Object> result = new HashMap<String, Object>();
				result.put("result", 0);
				return result;
			}
			
			if (goods.getDiscount() == null 
					|| goods.getDiscount().intValue() <= 0) {
				goods.setDiscount(100);
			}

			PayType pay = PayType.getPayType(payType);//支付类型  0-铜币, 1-积分, 2-微信, 3-支付宝, 4-钻石
			if (pay == null) {
				Map<String, Object> result = new HashMap<String, Object>();
				result.put("result", 0);
				return result;
			}
			pay.prePayEmbed(request);
			
			DefaultUserPay userPay = (DefaultUserPay) SpringContextUtils.getBean(pay.getPayBeanName());
			final OrderInfo order = userPay.buildOrderInfo(userId, goods, goodsNum, videoId, anchorId, payType);
			setReceiveNo(order, videoId, app);
			order.setOutTradeNo(outTradeNoUtil.getTradeNo(pay));
			//GoodsType 商品类型 0-充值 1-临时 2-连送 3-贵族 4-贵族连送 5-世界通知 6弹幕
			if(goods.getGoodsType() > 2 && goods.getGoodsType() < 6) {
				order.setOrderType(1);//orderType  商品类型 0礼物 1贵族 2使用抵用券 3续费  4弹幕
			}else if (goods.getGoodsType() == 6){
				order.setOrderType(4);
			}else if(goods.getGoodsType() == 1 || goods.getGoodsType() == 2){
				order.setOrderType(0);
			}
			Map<String, Object> payResultMap = userPay.pay(order, app);
			return payResultMap;// 2.生成订单信息 3.返回客户端结果
		}
		return null;

	}

	private void setReceiveNo(final OrderInfo order, Long videoId, String app) {
		LiveVideoInfo info = null;
		if (redisAdapter.existsKey(RedisKey.LIVE_VIDEO_INFO_ + videoId)) {
			info = LiveVideoInfoHelper.getVideoInfoCache(redisAdapter, liveVideoInfoMapper, videoId, "videoStatus");
		} else {
			info = LiveVideoInfoHelper.getAndCacheVideoInfo(redisAdapter, liveVideoInfoMapper, videoId);
		}
		if (StringUtils.isNotBlank(app)) {
			order.setReceiveNo("02");//02独立直播表示礼物
		}
		if (info != null && info.getVideoStatus() != null 
				&& info.getVideoStatus().intValue() ==4) {
			order.setReceiveNo("01");//01表示回放礼物
			if (StringUtils.isNotBlank(app)) {
				order.setReceiveNo("03");//独立03表示回放礼物
			}
		}
	}

	/**
	 * 送礼成功之后数据变动消息发送
	 * @param request
	 *//*
	@Override
	public void afterPayMsg(HttpServletRequest request) {
		try {
			Long anchorId = Long.valueOf(request.getParameter("anchorId"));
			Long videoId = Long.valueOf(request.getParameter("videoId"));
			Long userId = Long.valueOf(request.getParameter("userId"));

			LiveVideoInfo videoInfo = LiveVideoInfoHelper.getVideoInfoCache(redisAdapter, liveVideoInfoMapper, videoId, "chatroomId");
			if (videoInfo == null || videoInfo.getChatroomId() == null){
				videoInfo = LiveVideoInfoHelper
						.getAndCacheVideoInfo(redisAdapter, liveVideoInfoMapper, videoId);
			}
			String chatroomId = videoInfo.getChatroomId();
			// 获取主播贡献值
			Long contrib = contribMapper.getContribByAnchorId(anchorId, 1);
			JSONObject dataExtra = new JSONObject();
			dataExtra.put("contribVal", contrib.toString());
			Integer syncMsgType = 27;
			RongMsgUtils.sendChatroomMsg(chatroomId, userId,syncMsgType, "", dataExtra.toJSONString());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}*/

//	/**
//	 * 机器人通知，送礼通知
//	 * @param order
//	 * @param videoInfo
//	 */
//	private void noticeThread(final OrderInfo order, final LiveVideoInfo videoInfo) {
//		taskExecutor.execute(new Runnable() {
//			@Override
//			public void run() {
//				for (int i = 0; i < order.getGoodsNum(); i++) {
//					treadNotice(order, videoInfo);
//				}
//				FollowNoticeServiceImpl followNoticeService = FolloServiceSingleton.getInstance();
//				try {
//					followNoticeService.noticeAnchorMoney(order.getReceiverKey(), 
//							((order.getGoodsPrice().intValue() / 100) * order.getGoodsNum()), new Date().getTime());
//				} catch (Exception e) {
//					LOGGER.error("通知主播收礼异常", e);
//				}
//			}
//		});
//	}
//	
//	private void treadNotice(OrderInfo order, LiveVideoInfo info) {
//		try {
//			if (info != null) {
//				noticeService.manSendGift(order.getOriginKey(), info.getChatroomId(), info.getAnchorId(), 
//					order.getGoodsId(), (order.getGoodsPrice().intValue() / 100.0), order.getUserId());
//			}
//		}catch(Exception e){
//			LOGGER.info("支付调用机器人异常", e);
//		}
//	}
	
}
