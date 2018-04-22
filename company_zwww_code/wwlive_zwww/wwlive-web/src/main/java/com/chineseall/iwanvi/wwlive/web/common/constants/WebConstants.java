package com.chineseall.iwanvi.wwlive.web.common.constants;

public final class WebConstants {
	/**
	 * 操作成功常量1
	 */
	public static final int SUCCESS = 1;

	/**
	 * 操作失败常量0
	 */
	public static final int FAIL = 0;

	/**
	 * ehcache中礼品列表的key
	 */
	public static final String EC_GOODS_LIST = "goods_list";

	/**
	 * ehcache礼品列表中架上礼品的key
	 */
	public static final String ON_OFFER_GOODS = "on_offer_goods_list";

	/**
	 * 生成支付宝外部展示订单号
	 */
	public static final String SEQ_OUT_TRADE_NO = "out_trade_no";

	/**
	 * 生成微信外部展示订单号
	 */
	public static final String SEQ_WX_ORDER_ID = "wx_order_id";

	/**
	 * 生成积分外部展示订单号
	 */
	public static final String SEQ_CX_ORDER_ID = "cx_order_id";

	/**
	 * 充值信息
	 */
	public static final String SEQ_CZ_CHARGE_ID = "cz_charge_id";

	/**
	 * 背包礼物外部订单号
	 */
	public static final String SEQ_BPGIFT_GIVE = "bpgift_no";

	/**
	 * 抽奖外部订单号
	 */
	public static final String GAME_GIVE = "game_no";


	/**
	 * 贵族订单信息
	 */
	public static final String SEQ_NOBLE_ID = "noble_id";
	/**
	 * 钻石支付信息
	 */
	public static final String SEQ_ZS_CHARGE_ID = "zs_charge_id";
	/**
	 * 支付宝订单号前缀
	 */
	public static final String ZFB = "zfb";
	/**
	 * 微信订单号前缀
	 */
	public static final String WX = "wx";

	/**
	 * 创新版订单号前缀
	 */
	public static final String CX = "cx";

	/**
	 * 充值前缀
	 */
	public static final String CZ = "cz";

	/**
	 * 钻石支付前缀
	 */
	public static final String ZS = "zs";

	/**
	 * 贵族
	 */
	public static final String NB = "nb";

	/**
	 * 背包礼物前缀
	 */
	public static final String BPGIFT = "bp";

	/**
	 * 抽奖前缀
	 */
	public static final String GAME = "game";

	/**
	 * 1钻石 = 100贡献值
	 */
	public static final int DIAMOND_CONTRIB_RATE = 100;

	/**
	 * 历史直播观看人数每10人次更新到数据库一次
	 */
	public static final int HISTORY_VIEWERS_UPDATE_BUFFER = 10;

	/**
	 * 设备信息认证类型 = 0
	 */
	public static final int OAUTH_DEVICE = 0;

	/**
	 * 微信认证类型 = 1
	 */
	public static final int OAUTH_WECHAT = 1;

	/**
	 * QQ认证类型 = 2
	 */
	public static final int OAUTH_QQ = 2;

	/**
	 * 账号密码认证[免电]类型 = 3
	 */
	public static final int OAUTH_USER_PASSWORD = 3;

	/**
	 * push类型, 友盟 = 0
	 */
	public static final int PUSH_UMENG = 0;

	public static final String SEQ_STREAM_ID = "stream_id";

	public static final String STREAMP_REFIX = "LIVE";

	public static final String SEQ_VDOID = "vdoid_id";

	private WebConstants() {
	}

}