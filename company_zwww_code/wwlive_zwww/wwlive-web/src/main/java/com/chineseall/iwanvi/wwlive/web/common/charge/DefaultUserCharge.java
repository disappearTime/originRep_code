package com.chineseall.iwanvi.wwlive.web.common.charge;

import java.math.BigDecimal;
import java.util.Map;

import com.chineseall.iwanvi.wwlive.common.exception.IWanviException;
import com.chineseall.iwanvi.wwlive.domain.wwlive.GoodsInfo;
import com.chineseall.iwanvi.wwlive.domain.wwlive.RechargeInfo;

/**
 * 用户充值
 * @author DIKEPU
 * @since 2017-01-16 二期
 */
public abstract class DefaultUserCharge {
	
	/**
	 * 创建充值信息
	 * @param userId
	 * @param goods
	 * @return
	 * @throws IWanviException 如果物品为空 ({@code goods == null})
	 */
	public RechargeInfo reBuildRechargeInfo(RechargeInfo info, GoodsInfo goods) {
		if (goods == null) {
			throw new IWanviException("物品为空，goods：" + goods);
		}
		long totalAmount = new BigDecimal(goods.getDiscount()).multiply(new BigDecimal(goods.getGoodsPrice()))
				.divide(new BigDecimal(100)).setScale(2, BigDecimal.ROUND_HALF_UP).longValue();
		
		info.setRechargeAmount(new Long(goods.getGoodsPrice().intValue()));
		info.setAmt(totalAmount);
		info.setGoodsId(goods.getGoodsId());
		info.setGoodsName(goods.getGoodsName());
		info.setDiscount(goods.getDiscount());
		
		return info;
	}
	
	public abstract Map<String, Object> resultMap(RechargeInfo info); 
	
}
