package com.chineseall.iwanvi.wwlive.web.common.pay;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Map;

import org.apache.commons.lang.time.DateFormatUtils;

import com.chineseall.iwanvi.wwlive.common.constants.Constants;
import com.chineseall.iwanvi.wwlive.domain.wwlive.GoodsInfo;
import com.chineseall.iwanvi.wwlive.domain.wwlive.OrderInfo;

/**
 * 用户支付
 * @author DIKEPU
 * @since 2017-01-23 二期
 *
 */
public abstract class DefaultUserPay {

	/**
	 * 生成订单信息
	 * 
	 * @param userId
	 *            支付用户id
	 * @param goods
	 *            商品信息
	 * @param goodsNum
	 *            商品数量
	 * @param videoId
	 *            视频id
	 * @param anchorId
	 *            主播信息
	 * @param payType
	 *            支付类型 0-铜币, 1-积分, 2-微信, 3-支付宝
	 * @return 订单信息
	 */
	public OrderInfo buildOrderInfo(Long userId, GoodsInfo goods,
			int goodsNum, Long videoId, Long anchorId, int payType) {

		BigDecimal totalGoodsPrice = new BigDecimal(goodsNum).multiply(
				new BigDecimal(goods.getGoodsPrice())).setScale(2, BigDecimal.ROUND_HALF_UP);

		BigDecimal totalAmount = totalGoodsPrice
				.multiply(new BigDecimal(goods.getDiscount()))
				.divide(new BigDecimal(100))
				.setScale(2, BigDecimal.ROUND_HALF_UP);

		OrderInfo order = new OrderInfo();
		order.setGoodsPrice(goods.getGoodsPrice());//配合机器人
		order.setUserId(userId);
		order.setOriginKey(videoId);
		order.setReceiverKey(anchorId);
		order.setGoodsId(goods.getGoodsId());
		order.setGoodsName(goods.getGoodsName());
		order.setGoodsImg(goods.getGoodsImg());
		order.setGoodsNum(goodsNum);
		order.setTotalGoodsPrice(totalGoodsPrice.intValue());
		order.setDiscount(goods.getDiscount());
		order.setPayType(payType);
		order.setOrderStatus(Constants._0);// orderStatus 支付状态 0未支付， 1成功， 2失败，
											// 3关闭， 4异常
		order.setOrderYearMonth(DateFormatUtils.format(new Date(), "yyyy-MM"));
		order.setAmt(totalAmount.longValue());
		order.setIncome(totalAmount.doubleValue());

		// order.setIncome((totalGoodsPrice *
		// Constants.ANCHOR_INCOME_RATE));//主播收入 现在是100%
		
		return order;
	}

	public abstract Map<String, Object> pay(OrderInfo order, String app);
	
}
