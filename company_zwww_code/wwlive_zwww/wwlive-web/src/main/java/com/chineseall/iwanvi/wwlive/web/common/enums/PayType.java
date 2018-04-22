package com.chineseall.iwanvi.wwlive.web.common.enums;

import javax.servlet.http.HttpServletRequest;

import com.chineseall.iwanvi.wwlive.common.cache.RedisKey;
import com.chineseall.iwanvi.wwlive.web.common.embedding.DataEmbeddingTools;

import static com.chineseall.iwanvi.wwlive.web.common.constants.WebConstants.*;

/**
 * 支付方式 0-铜币, 1-积分, 2-微信, 3-支付宝, 4-钻石 20-贵族微信购买 21-贵族阿里购买 5-背包礼物支付
 * @author DIKEPU
 *
 */
public enum PayType implements TradeNoOperation {

	/** 书城 */
	SCPAY(0) {

		public String getSequenceName() {
			return null;
		}

		public String getPrefix() {
			return null;
		}

		public String getRedisKey() {
			return null;
		}

		@Override
		public String getPayBeanName() {
			return "";
		}

		@Override
		public void prePayEmbed(HttpServletRequest request) {
		}

//		@Override
//		public void payEmbed(HttpServletRequest request) {
//		}
	},

	/** 创新 */
	CXPAY(1) {

		public String getSequenceName() {
			return SEQ_CX_ORDER_ID;
		}

		public String getPrefix() {
			return CX;
		}

		public String getRedisKey() {
			return RedisKey.CX_OUTTRADENO;
		}

		@Override
		public String getPayBeanName() {
			return "cxVirturalPay";
		}

		@Override
		public void prePayEmbed(HttpServletRequest request) {
			DataEmbeddingTools.insertLog("7005", "1-3",
					request.getParameter("anchorId"), request.getParameter("videoId"), request);//埋点
		}

//		@Override
//		public void payEmbed(HttpServletRequest request) {
//	        DataEmbeddingTools.insertLog("7004", "1-1",
//	                request.getParameter("anchorId"), request.getParameter("videoId"), request);//埋点
//
//		}
	},

	/** 微信 */
	WXPAY(2) {

		public String getSequenceName() {
			return SEQ_WX_ORDER_ID;
		}

		public String getPrefix() {
			return WX;
		}

		public String getRedisKey() {
			return RedisKey.WX_OUTTRADENO;
		}

		@Override
		public String getPayBeanName() {
			return "wxPay";
		}

		@Override
		public void prePayEmbed(HttpServletRequest request) {
			DataEmbeddingTools.insertLog("7005", "1-2",
					request.getParameter("anchorId"), request.getParameter("videoId"), request);//埋点

		}

//		@Override
//		public void payEmbed(HttpServletRequest request) {
//
//		}
	},

	/** 支付宝 */
	ALIPAY(3) {

		public String getSequenceName() {
			return SEQ_OUT_TRADE_NO;
		}

		public String getPrefix() {
			return ZFB;
		}

		public String getRedisKey() {
			return RedisKey.ZFB_OUTTRADENO;
		}

		@Override
		public String getPayBeanName() {
			return "aliPay";
		}

		@Override
		public void prePayEmbed(HttpServletRequest request) {
			DataEmbeddingTools.insertLog("7005", "1-1",
					request.getParameter("anchorId"), request.getParameter("videoId"), request);//埋点

		}

//		@Override
//		public void payEmbed(HttpServletRequest request) {
//
//		}
	},

	/**钻石 */
	ZSPAY(4) {

		public String getSequenceName() {
			return SEQ_ZS_CHARGE_ID;
		}

		public String getPrefix() {
			return ZS;
		}

		public String getRedisKey() {
			return RedisKey.ZS_OUTTRADENO;
		}

		@Override
		public String getPayBeanName() {
			return "zsVirturalPay";
		}

		@Override
		public void prePayEmbed(HttpServletRequest request) {
			DataEmbeddingTools.insertLog("7005", "1-4",
					request.getParameter("anchorId"), request.getParameter("videoId"), request);//埋点

		}

//		@Override
//		public void payEmbed(HttpServletRequest request) {
//			// Auto-generated method stub
//
//		}
	},

	/** 贵族微信购买 */
	NBWXPAY(20) {

		public String getSequenceName() {
			return SEQ_NOBLE_ID;
		}

		public String getPrefix() {
			return NB;
		}

		public String getRedisKey() {
			return RedisKey.NB_OUTTRADENO;
		}

		@Override
		public String getPayBeanName() {
			return "wxPay";
		}

		@Override
		public void prePayEmbed(HttpServletRequest request) {

		}

//		@Override
//		public void payEmbed(HttpServletRequest request) {
//
//		}
	},
	/**
	 * 贵族阿里支付
	 */
	NBALIPAY(21) {

		public String getSequenceName() {
			return SEQ_NOBLE_ID;
		}

		public String getPrefix() {
			return NB;
		}

		public String getRedisKey() {
			return RedisKey.NB_OUTTRADENO;
		}

		@Override
		public String getPayBeanName() {
			return "aliPay";
		}

		@Override
		public void prePayEmbed(HttpServletRequest request) {

		}

//		@Override
//		public void payEmbed(HttpServletRequest request) {
//
//		}
	},

	/**
	 * 贵族钻石支付
	 */
	NBZSPAY(22) {

		public String getSequenceName() {
			return SEQ_NOBLE_ID;
		}

		public String getPrefix() {
			return NB;
		}

		public String getRedisKey() {
			return RedisKey.NB_OUTTRADENO;
		}

		@Override
		public String getPayBeanName() {
			return "zsNoblePay";
		}

		@Override
		public void prePayEmbed(HttpServletRequest request) {

		}

//		@Override
//		public void payEmbed(HttpServletRequest request) {
//
//		}
	},

	/** 充值 */
	CZPAY(100) {

		public String getSequenceName() {
			return SEQ_CZ_CHARGE_ID;
		}

		public String getPrefix() {
			return CZ;
		}

		public String getRedisKey() {
			return RedisKey.CZ_OUTTRADENO;
		}

		@Override
		public String getPayBeanName() {
			return "";
		}

		@Override
		public void prePayEmbed(HttpServletRequest request) {

		}

//		@Override
//		public void payEmbed(HttpServletRequest request) {
//
//		}
	},

	GAME_PAY(6) {
		public String getSequenceName() {
			return GAME_GIVE;
		}

		public String getPrefix() {
			return GAME;
		}

		public String getRedisKey() {
			return RedisKey.GAME_OUTTRADENO;
		}

		@Override
		public String getPayBeanName() {
			return "";
		}

		@Override
		public void prePayEmbed(HttpServletRequest request) {
		}
	},

	/**
	 * 背包礼物送出
	 */
	BPGIFT_PAY(5) {

		public String getSequenceName() {
			return SEQ_BPGIFT_GIVE;
		}

		public String getPrefix() {
			return BPGIFT;
		}

		public String getRedisKey() {
			return RedisKey.BACK_OUTTRADENO;
		}

		@Override
		public String getPayBeanName() {
			return "";
		}

		@Override
		public void prePayEmbed(HttpServletRequest request) {

		}

	};

	private int type;//支付类型  0-铜币, 1-积分, 2-微信, 3-支付宝, 4-钻石

	/**
	 *
	 * @param type 类型
	 */
	private PayType(int type) {
		this.type = type;
	}

	public int getType() {
		return type;
	}

	/**
	 * 获得支付类型
	 * @param
	 * @return
	 */
	public static PayType getPayType(int type) {
		PayType[] types = PayType.values();
		for (PayType pay : types) {
			if (pay.getType() == type) {
				return pay;
			}
		}
		return null;
	}

}
