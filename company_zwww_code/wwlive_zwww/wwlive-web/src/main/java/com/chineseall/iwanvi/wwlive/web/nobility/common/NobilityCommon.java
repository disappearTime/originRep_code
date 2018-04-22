package com.chineseall.iwanvi.wwlive.web.nobility.common;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.chineseall.iwanvi.wwlive.common.cache.RedisClientAdapter;
import com.chineseall.iwanvi.wwlive.common.cache.RedisExpireTime;
import com.chineseall.iwanvi.wwlive.common.cache.RedisKey;
import com.chineseall.iwanvi.wwlive.common.constants.Constants;
import com.chineseall.iwanvi.wwlive.common.enums.DicCouponEnum;
import com.chineseall.iwanvi.wwlive.common.enums.DictInfoEnum;
import com.chineseall.iwanvi.wwlive.common.tools.RongMsgUtils;
import com.chineseall.iwanvi.wwlive.dao.wwlive.*;
import com.chineseall.iwanvi.wwlive.domain.util.DateUtil;
import com.chineseall.iwanvi.wwlive.domain.wwlive.*;
import com.chineseall.iwanvi.wwlive.web.common.charge.aftercharge.AfterChargeTask;
import com.chineseall.iwanvi.wwlive.web.common.enums.PayType;
import com.chineseall.iwanvi.wwlive.web.common.helper.AllChatroomsNoticeHelper;
import com.chineseall.iwanvi.wwlive.web.common.helper.AnchorInfoHelper;
import com.chineseall.iwanvi.wwlive.web.common.helper.GoodsInfoHelper;
import com.chineseall.iwanvi.wwlive.web.common.helper.LiveAdminHelper;
import com.chineseall.iwanvi.wwlive.web.common.helper.LiveVideoInfoHelper;
import com.chineseall.iwanvi.wwlive.web.common.helper.RoleNobleHelper;
import com.chineseall.iwanvi.wwlive.web.common.helper.UserInfoHelper;
import com.chineseall.iwanvi.wwlive.web.common.helper.UserRankHelper;
import com.chineseall.iwanvi.wwlive.web.common.pay.OutTradeNoUtil;
import com.chineseall.iwanvi.wwlive.web.common.pay.type.ZsVirturalPay;
import com.chineseall.iwanvi.wwlive.web.event.service.MedalHonorService;
import com.chineseall.iwanvi.wwlive.web.video.service.DaoFactoryService;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.commons.lang.time.DateUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by lvliang on 2017/7/11.
 */
@Component
public class NobilityCommon {

	private static final Logger logger = Logger.getLogger(NobilityCommon.class);
	
	@Autowired
	private GoodsInfoMapper goodsInfoMapper;
	@Autowired
	private OutTradeNoUtil outTradeNoUtil;
	@Autowired
	private OrderInfoMapper orderInfoMapper;
	@Autowired
	private CouponInfoMapper couponInfoMapper;
	@Autowired
	private TransInfoMapper transInfoMapper;
	@Autowired
	private RoleInfoMapper roleInfoMapper;
	@Autowired
	private AcctInfoMapper acctInfoMapper;
	@Autowired
	private RechargeInfoMapper rechargeInfoMapper;
	@Autowired
	private DaoFactoryService daoFactoryService;
	@Autowired
	private RedisClientAdapter redisAdapter;
	@Autowired
	private LiveAdminMapper adminMapper;
	
	@Autowired
	private ContributionListMapper contributionListMapper;

    @Autowired
    private LiveVideoInfoMapper liveVideoInfoMapper;

    @Autowired
    private UserInfoMapper userInfoMapper;

    @Autowired
    private DiscountInfoMapper discountInfoMapper;

    @Autowired
    private AfterChargeTask afterChargeTask;

    @Autowired
    private AnchorMapper anchorMapper;

	@Autowired
	private MedalHonorService medalHonorService;
	/**
	 * 获取骑士的相关信息(图片/价格/)
	 * 
	 * @return
	 */
	public OrderInfo getOrderInfo(String goodsId) {
		OrderInfo orderInfo = new OrderInfo();

		// 根据goosId获取对应的商品名称图片信息
		String str = redisAdapter.strGet(RedisKey.NobleKey.NOBILITY_ALLCAVALIER);
		if (StringUtils.isNotBlank(str)) {
			JSONObject jsonStr = new JSONObject();
			JSONArray jsonArray = JSONArray.parseArray(str);
			for (Object obj : jsonArray) {
				jsonStr = JSONObject.parseObject(obj.toString());
				String gid = jsonStr.getString("goodsId");
				if (gid.equals(goodsId)) {
					orderInfo.setGoodsId(Long.parseLong(goodsId));
					orderInfo.setGoodsImg(jsonStr.getString("goodsImg"));
					orderInfo.setGoodsName(jsonStr.getString("goodsName"));
					orderInfo.setTotalGoodsPrice(jsonStr
							.getInteger("goodsPrice"));
					orderInfo.setAmt(jsonStr.getLong("goodsPrice"));
				}
			}
		} else {
			GoodsInfo goodsInfo = goodsInfoMapper.getById(Integer
					.parseInt(goodsId));
			if (goodsInfo != null) {
				orderInfo.setGoodsId(Long.parseLong(goodsId));
				orderInfo.setGoodsImg(goodsInfo.getGoodsImg());
				orderInfo.setGoodsName(goodsInfo.getGoodsName());
				orderInfo.setTotalGoodsPrice(goodsInfo.getGoodsPrice());
				orderInfo.setAmt((long) goodsInfo.getGoodsPrice());
			}
		}
		return orderInfo;
	}

	/**
	 * 封装order对象
	 * 
	 * @param payType
	 * @param userId
	 */
	public void insertOrder(OrderInfo orderInfo, String payType, String userId) {
		orderInfo.setOutTradeNo(outTradeNoUtil.getTradeNo(PayType.NBWXPAY));
		orderInfo.setUserId(Long.parseLong(userId));
		orderInfo.setOriginKey(0L);
		orderInfo.setReceiverKey(0L);
		orderInfo.setGoodsNum(1);
		orderInfo.setDiscount(100);
		orderInfo.setPayType(Integer.parseInt(payType));
		orderInfo.setOrderType(5);//5购买贵族
		orderInfo.setOrderStatus(Constants._0);// 未支付
		orderInfo.setOrderYearMonth(DateFormatUtils.format(new Date(),
				"yyyy-MM"));
		orderInfo.setIncome(0.0);

	}

	/**
	 * 根据goodsId获取个对应的打折信息
	 * 
	 * @param goodsId
	 * @return
	 */
	public JSONArray getDiscountInfoByGoodsId(String goodsId) {
		JSONArray jsonArray = new JSONArray();
		String str = redisAdapter.strGet(RedisKey.NobleKey.NOBILITY_DISCOUNT_
				+ goodsId);
		if (StringUtils.isNotBlank(str)) {
			jsonArray = JSONArray.parseArray(str);
		} else {
			List<DiscountInfo> discountInfoList = discountInfoMapper
					.getAllDiscount(Long.parseLong(goodsId));
			// 加入缓存
			if (discountInfoList != null) {
				redisAdapter.strSetexByNormal(RedisKey.NobleKey.NOBILITY_DISCOUNT_
						+ goodsId, RedisExpireTime.EXPIRE_DAY_30,
						JSONObject.toJSONString(discountInfoList));
				String s = redisAdapter
						.strGet(RedisKey.NobleKey.NOBILITY_DISCOUNT_ + goodsId);
				jsonArray = JSONArray.parseArray(s);
			}
		}
		return jsonArray;
	}

	/**
	 * 
	 * @param goodsId
	 * @param isFirst
	 * @return
	 */
	private List<DiscountInfo> getDiscountCouponAndDiamondByGoodsId(Long goodsId, boolean isFirst) {
		String key = RedisKey.NobleKey.NOBLE_DISCOUNT_COUPON_DIAMOND_NEXT_ + goodsId;
		String str = "";
		if (isFirst) {
			key = RedisKey.NobleKey.NOBLE_DISCOUNT_COUPON_DIAMOND_FIRST_ + goodsId;
		}
		str = redisAdapter.strGet(key);
		if (StringUtils.isNotBlank(str)) {
			 List<DiscountInfo> discountInfoList = JSONArray.parseArray(str, DiscountInfo.class);
			 return discountInfoList;
		} else {
			List<DiscountInfo> discountInfoList = discountInfoMapper
					.getDiscountCouponAndDiamond(goodsId, isFirst);
			// 加入缓存
			if (discountInfoList != null) {
				redisAdapter.strSetexByNormal(key, RedisExpireTime.EXPIRE_DAY_30,
						JSONObject.toJSONString(discountInfoList));
			}
			return discountInfoList;
		}
	}
	
	/**
	 * 购买贵族成功后，回调。
	 * 1.如果购买过更新 ---> 更新用户的优惠券信息
	 * 2.如果没有更新插入 ---> 插入用户优惠券 插入用户身份信息
	 * @param orderInfo
	 * @return
	 */
	@Transactional
	public int updateNobilityOrderInfo(OrderInfo orderInfo) {
		// 根据id 获取贵族 special
		GoodsInfo goods = GoodsInfoHelper.getFromCacheIfNotExistsCacheGoodsInfo(redisAdapter, 
				goodsInfoMapper, orderInfo.getGoodsId());
		if (goods == null) {
			return 0;
		}
		
		Date date = new Date();
		// 插入到biz_role_info表中
		int modify = upsertRoleInfoByOrder(orderInfo, date, goods);

		if (modify <= 0) {
			return modify;
		}
		
		// 生成一条贵族购买交易信息 biz_trans_info
		insertTransByOrder(orderInfo);
		
		if (orderInfo.getOrderType() != null
				&& orderInfo.getOrderType() != Constants._3) {// 0礼物 1贵族 2使用抵用券 3续费(无优惠)
			// 更新账户表和优惠表信息
			updateCouponAndAcctInfo(orderInfo, date, goods);;
		}

		boolean isInVideo = false;
		try {
			//主播收入 贡献值
			if (orderInfo.getReceiverKey() != null && orderInfo.getReceiverKey() != 0) {
//				upsertContribution(orderInfo);
	            isInVideo = true;
			}
			orderInfo.setOrderStatus(1);
			orderInfoMapper.updateStatus(orderInfo);
		} catch (Exception e) {
			logger.error("购买贵族回调异常：", e);
		}
		
		// 删除相关缓存信息
		deleCache(orderInfo);
		try {
			sendNobleMSG(goods, orderInfo, isInVideo);//骑士消息
		} catch (Exception e) {
			logger.error("购买贵族回调异常：", e);
		}
		return modify;
	}
	
	/**
	 * 生成或更新该用户的贡献，修改该视频的位置
	 * @param order
	 */
	private void upsertContribution(OrderInfo order) {
		String key = RedisKey.USER_CONTRIBUTION_CNT_ + order.getReceiverKey() + Constants.UNDERLINE + order.getUserId();
		String total = RedisKey.ANCHOR_USER_AMT_ + order.getReceiverKey() + Constants.UNDERLINE + order.getUserId();
		int cnt = 0;
		if (redisAdapter.existsKey(key)) {
			String strCnt = redisAdapter.strGet(key);
			if (!"0".equals(strCnt)) {
				cnt = 1;
			}
		}  else {
			cnt = contributionListMapper.countByAnchorAndUser(order.getReceiverKey(), order.getUserId(), 1);
		}
		dealContributionList(order, cnt);//处理用户的贡献值信息
		String ctbKey = RedisKey.USER_CTB_DO_ + order.getUserId();;
		String coinKey = RedisKey.USER_COIN_ + order.getUserId();
		redisAdapter.delKeys(total, ctbKey, coinKey);
	}

	/**
	 * 处理用户的贡献值信息，开通贵族和送礼的不同处是贵族收入主播只占一部分
	 * @param order
	 * @param cnt
	 * @param key
	 */
	private void dealContributionList(OrderInfo order, int cnt) {
		if (cnt > 0) {
			cnt = contributionListMapper.updateTotalAmt(order.getIncome(), order.getTotalGoodsPrice(),
					order.getGoodsNum(), order.getReceiverKey(), order.getUserId(), 1);
		} else {
			ContributionList con = new ContributionList();
			con.setAnchorId(order.getReceiverKey());
			con.setUserId(order.getUserId());
			con.setGoodsNum(order.getGoodsNum());
			con.setOriginalAmt(order.getTotalGoodsPrice());
			con.setTotalAmt(order.getIncome());
			con.setType(1);
			cnt = contributionListMapper.insertContribution(con);
		}
		if (cnt > 0) {
			cntbMarkKey(order, cnt);//贡献标志
			ZsVirturalPay.afterPayMsg(order.getAmt(), order.getReceiverKey(),  order.getOriginKey(),  order.getUserId(),
					contributionListMapper,redisAdapter,liveVideoInfoMapper);
		} else {
			delbMarkKey(order);
		}
//		sendContribMSG(order);
	}

	/**
	 * 对该主播有过贡献
	 */
	private void cntbMarkKey(OrderInfo order, int cnt) {
		String key = RedisKey.USER_CONTRIBUTION_CNT_ + order.getReceiverKey() + Constants.UNDERLINE + order.getUserId();
		if (cnt > 0) {
			redisAdapter.strIncrBy(key, cnt);
		} else {
			redisAdapter.strIncr(key);
		}
    	redisAdapter.expireKey(key, RedisExpireTime.EXPIRE_DAY_1);
	}
	
	/**
	 * 删除贡献标志
	 * @param key
	 */
	private void delbMarkKey(OrderInfo order) {
		String key = RedisKey.USER_CONTRIBUTION_CNT_ + order.getReceiverKey() + Constants.UNDERLINE + order.getUserId();
		String total = RedisKey.ANCHOR_USER_AMT_ + order.getReceiverKey() + Constants.UNDERLINE + order.getUserId();
		redisAdapter.delKeys(key, total);
	}
	
	/**
	 * 插入到交易流水表
	 * 
	 * @param orderInfo
	 * @return
	 */
	private void insertTransByOrder(OrderInfo orderInfo) {
		TransInfo trans = new TransInfo();
		trans.setUserId(orderInfo.getUserId());
		trans.setOutId(orderInfo.getOrderId());
		trans.setTransType(Constants._3);// 交易类型 3首次购买贵族
		if (orderInfo.getOrderType() != null
				&& orderInfo.getOrderType() == Constants._3) {// 0-消费 1充值 2红包 3贵族返钻
			trans.setTransType(Constants._4);// 4贵族续费
		}
		trans.setTransStatus(Constants._0);// 交易状态 0正常
		trans.setAmt(orderInfo.getAmt());// 实际支付金额
		trans.setPayType(orderInfo.getPayType());// 支付类型 0-铜币, 1-积分, 2-微信,
													// 3-支付宝, 4-钻石
		transInfoMapper.insertTransInfo(trans);
	}

	/**
	 * 1. 如果是购买（续费），则判断是否贵族过期，如果没有过期则更新，否则过期插入
	 * 
	 * @param orderInfo
	 */
	public int upsertRoleInfoByOrder(OrderInfo orderInfo, Date date, GoodsInfo goods) {
		RoleInfo theRole = roleInfoMapper.findLevelsByUserIdAndGoodsId(
				orderInfo.getUserId(), orderInfo.getGoodsId());
		if (isValid(theRole, date)) {//theRole为空说明是初次开通，直接插入
			return updateRoleInfo(theRole);
		}
		return insertRoleInfo(orderInfo, date, goods);
	}

	private boolean isValid(RoleInfo theRole, Date date) {
		if (theRole == null) {
			return false;
		}
		if (theRole.getEffectiveEndTime() != null
				&& theRole.getEffectiveEndTime().getTime() > date.getTime()) {
			return true;
		}
		roleInfoMapper.updateRoleInfoInvalid(theRole.getRoleId());
		return false;
	}
	
	private int updateRoleInfo(RoleInfo theRole) {
		RoleInfo role = new RoleInfo();
		role.setRoleId(theRole.getRoleId());
		role.setEffectiveEndTime(DateUtils.addDays(theRole.getEffectiveEndTime(), 30));
		return roleInfoMapper.updateRoleInfoEffectiveEndTime(role);
	}

	private int insertRoleInfo(OrderInfo orderInfo, Date date, GoodsInfo goods) {
		RoleInfo roleInfo = new RoleInfo();
		roleInfo.setUserId(orderInfo.getUserId());
		roleInfo.setGoodsId(orderInfo.getGoodsId());
		roleInfo.setGoodsName(orderInfo.getGoodsName());
		int lvl = 0;
		DictInfoEnum info = DictInfoEnum.getDictInfoEnum(goods.getSpecial());
		if (info != null) {
			lvl = info.getLevel();
		}
		roleInfo.setRoleLevel(lvl);// 等级
		roleInfo.setRoleStatus(0);
		roleInfo.setEffectiveStartTime(date);
		roleInfo.setEffectiveEndTime(DateUtil.getNextDay(date, 30));// 加30天
		return roleInfoMapper.insertRoleInfo(roleInfo);
	}

	/**
	 * 1. 如果有更新用户的贵族券，没有则是直接插入信息
	 * <p/>
	 * 2. 如果是用户首开则有贡献值
	 * @param orderInfo
	 * @param date
	 */
	private void updateCouponAndAcctInfo(OrderInfo orderInfo, Date date, GoodsInfo goods) {
		//1.获得优惠信息 2.根据优惠信息更新acct表 3.更新完成此coupon要置为不可用
		Long userId = orderInfo.getUserId();
		Long goodsId = orderInfo.getGoodsId();
		CouponInfo couponInfo = couponInfoMapper.getCouponInfoByUidAndGid(userId, goodsId);
		int diamond = 0;//钻石数
		int coupon = 0;//抵用券
		//DiscountType 1初次抵用券 2未使用抵用券返钻 3使用抵用券返钻 4再次返抵用券
		if (couponInfo == null) {//未有优惠
			List<DiscountInfo> discList = getDiscountCouponAndDiamondByGoodsId(goodsId, true);
			for (int i = 0; discList != null && i < discList.size(); i++) {
				DiscountInfo dis = discList.get(i);
				if (dis.getDiscountType() == 2) {
					diamond = new BigDecimal(dis.getDiscountPrice()).intValue();
				}
				if (dis.getDiscountType() == 1) {
					coupon = new BigDecimal(dis.getDiscountPrice()).intValue();
				}
			}
			upsertContribution(orderInfo);
		} else {
			//1旧的优惠信息删除 2.生成再次优惠信息
			couponInfoMapper.updateCouponInfo2InvalidById(couponInfo.getCouponId());
			List<DiscountInfo> discList = getDiscountCouponAndDiamondByGoodsId(goodsId, false);
			for (int i = 0; discList != null && i < discList.size(); i++) {
				DiscountInfo dis = discList.get(i);
				if (dis.getDiscountType() == 3) {
					diamond = new BigDecimal(dis.getDiscountPrice()).intValue();
				}
				if (dis.getDiscountType() == 4) {
					coupon = new BigDecimal(dis.getDiscountPrice()).intValue();
				}
			}
		}
		RechargeInfo recharge = null;
		if (diamond > 0) {
			recharge = buildRechargeInfo(orderInfo, diamond);
			if (couponInfo != null && couponInfo.getCouponId() != null) {
				recharge.setOrigin(7);// 贵族返钻
			}
			rechargeInfoMapper.insertRechargeInfo(recharge);
		}

		if (recharge != null && recharge.getRechargeId() != null) {
			afterChargeTask.afterCharge(recharge);
		}
		if (coupon > 0) {
			buildCouponInfo(orderInfo, coupon, date);
		}
	}
	
	private RechargeInfo buildRechargeInfo(OrderInfo orderInfo, int amt) {
		RechargeInfo rechargeInfo = new RechargeInfo();
		rechargeInfo.setUserId(orderInfo.getUserId());
		rechargeInfo.setOutTradeNo(orderInfo.getOutTradeNo());
		rechargeInfo.setReceiveNo("01");//01
		rechargeInfo.setRechargeAmount((long) amt);//金额
		rechargeInfo.setRechargeType(0);// 钻石
		rechargeInfo.setRechargeStatus(0);// 未支付
		rechargeInfo.setGoodsId(orderInfo.getGoodsId());
		rechargeInfo.setGoodsName(orderInfo.getGoodsName());
		rechargeInfo.setAmt(orderInfo.getAmt());
		rechargeInfo.setDiscount(orderInfo.getDiscount());
		rechargeInfo.setOrigin(6);// 贵族返钻
		rechargeInfo.setWay(0);// 我的
		return rechargeInfo;
	}
	
	private void buildCouponInfo(OrderInfo orderInfo, int amt, Date date) {
		CouponInfo couponInfo = new CouponInfo();
		couponInfo.setCouponName(orderInfo.getGoodsName() + "购买优惠");// 折扣券名称
		couponInfo.setCouponType(DicCouponEnum.getDictInfoEnum(
				orderInfo.getGoodsName()).getCode());// 折扣券类型 0标示折扣
		couponInfo.setCouponValue((long) amt);
		couponInfo.setCouponBalance((long) amt);
//		couponInfo.setRenewId(Long.parseLong(strRenewId));
		//第33天时要继续扣款
		couponInfo.setEffectivenessTime(DateUtil.getNextDay(
				date, 33));
		couponInfo.setUserId(orderInfo.getUserId());
		couponInfo.setGoodsId(orderInfo.getGoodsId());
		couponInfo.setCouponStatus(0L);
		couponInfoMapper.insertCoupon(couponInfo);
	}


	/**
	 * 删除相关缓存信息
	 * 
	 * @param orderInfo
	 */
	private void deleCache(OrderInfo orderInfo) {
		Long userId = orderInfo.getUserId();
		Long goodsId = orderInfo.getGoodsId();
		String user_role_noble = RedisKey.NobleKey.USER_ROLE_NOBLE_ + userId;
		String user_role_noble_titles = RedisKey.NobleKey.USER_ROLE_NOBLE_TITLES_
				+ userId;
		String noble_check = RedisKey.NobleKey.NOBLE_CHECK_ + userId;
		String userRole = RedisKey.NobleKey.USER_ROLE_ + userId;
		String nobleKey = RedisKey.NobleKey.USER_ROLE_NOBLE_GOODSIDS_ + userId;// 贵族key
		String couponKey = RedisKey.NobleKey.USER_COUPONINFO_ + goodsId + Constants.UNDERLINE + userId;//优惠信息
        String anKey = "anchor_rank_" + orderInfo.getReceiverKey();
		redisAdapter.delKeys(user_role_noble,
				user_role_noble_titles, noble_check, userRole, nobleKey, couponKey, anKey);
	}

	/**
	 * 弹幕发送消息
	 * 
	 * @param chatroomId
	 * @param userId
	 * @param goodsId
	 * @param anchorId
	 */
	public void pushBarrageMsg(String chatroomId, String userId,
			String goodsId, String anchorId, String content) {
		Map<String, String> userInfo = new HashMap<String, String>();
		// 根据用户id获取用户信息
		String userKey = RedisKey.USER_INFO_ + userId;
		if (redisAdapter.existsKey(RedisKey.USER_INFO_ + userId)) {
			userInfo = redisAdapter.hashMGet(userKey, "userId", "headImg",
					"userName", "sex", "birthday", "zodiac", "acctType","loginId");
		}
		String acctType = "0";
		if (anchorId != null) {
//			Long aid = Long.valueOf(anchorId);
			// 用户类型信息acctType, 0-普通用户, 1-超管, 2-房管
			// 此处的acctType不等同与用户表中的acctType
			acctType = userInfo.get("acctType");
			if ("0".equals(acctType)) {
				// 普通用户是否为房管
				if (LiveAdminHelper.isAdmin(redisAdapter, adminMapper,
						Long.parseLong(anchorId), Long.parseLong(userId))) {// 普通用户是否为房管
					acctType = "2";
				}
			}
		}
		//根据用户获开通的贵族信息
		int level = RoleNobleHelper.userRoleNobleLevel(redisAdapter, roleInfoMapper, Long.parseLong(userId));

        int medals = medalHonorService.getUserMedalsById(Long.parseLong(userId)).size();

		Map<String, Object> paramMap = new HashMap<>();
		paramMap.put("chatroomId", chatroomId); // 聊天室id
		paramMap.put("userId", Long.parseLong(userId)); // 用户id
		paramMap.put("loginId",userInfo.get("loginId"));
		paramMap.put("headImg", userInfo.get("headImg")); // 头像
		paramMap.put("userName", userInfo.get("userName")); // 昵称
		paramMap.put("acctType", Integer.valueOf(acctType)); // 用户身份
        paramMap.put("medals", medals);
        paramMap.put("sex", userInfo.get("sex")); // 性别
		paramMap.put("content", content); // 弹幕内容
		paramMap.put("nobleCode", level); // 贵族级别

		String msgJSON = JSON.toJSONString(paramMap);
		try {
//			daoFactoryService.getPushRedisDao().rpush("pushMsg", msgJSON);
            RongMsgUtils.sendChatroomMsg(chatroomId, 0L, new Integer(26), "", msgJSON);
		} catch (Exception e) {
			logger.error(e);
		}

	}

    private String getChatroomId(Long videoId) {
        String chatroomId = "";
        String key = RedisKey.LIVE_VIDEO_INFO_ + videoId;
        if (redisAdapter.existsKey(key)) {
            chatroomId = redisAdapter.hashGet(key, "chatroomId");
        } else {
            LiveVideoInfo info = LiveVideoInfoHelper.getAndCacheVideoInfo(redisAdapter,
                    liveVideoInfoMapper,videoId);
            if (info != null) {
                chatroomId = info.getChatroomId();
            }
        }
        return chatroomId;
    }

    /**
     * 开通贵族时的飘屏通知
     * @param order
     * @param goods
     * @param isInVideo
     * @return
     * @throws ParseException
     */
    private JSONObject getObjectJson(final OrderInfo order, GoodsInfo goods, boolean isInVideo) throws ParseException {
        JSONObject json = new JSONObject();
        if (redisAdapter.existsKey(RedisKey.USER_INFO_ + order.getUserId())) {
            Map<String, String> map = redisAdapter.hashMGet(RedisKey.USER_INFO_ + order.getUserId(), "userName", "headImg", "loginId","userId", "sex");
            json.putAll(map);
        } else {
            UserInfo user = UserInfoHelper.getAndCacheUserInfo(redisAdapter, userInfoMapper, order.getUserId());
            if (user != null) {
                json.putAll(user.putFieldValueToMap());
            } else {
                json.put("userName", "");
            }
        }
        json.put("goodsId",order.getGoodsId());
        
        Map<String, Object> userInfo = UserRankHelper.getAndCacheUserRank(redisAdapter, contributionListMapper, order.getUserId());
        if (userInfo == null || userInfo.isEmpty()) {
        	userInfo.put("totalAmt", order.getTotalGoodsPrice());
        } else {
        	userInfo.put("totalAmt", userInfo.get("contrib"));
        }
		DictInfoEnum info = DictInfoEnum.getDictInfoEnum(goods.getSpecial());
		if (info != null) {
			Integer level = RoleNobleHelper.userRoleNobleLevel(redisAdapter, roleInfoMapper, order.getUserId());
			json.put("level", level);
			json.put("nobleCode", info.getLevel());
			json.put("nobleName", info.getNobelName());
		}if (isInVideo) {
			json.put("videoId", order.getOriginKey());
		} else {
			json.put("videoId", -1L);
		}
		if (isInVideo) {
	        String anchorKey = RedisKey.ANCHOR_INFO_ + order.getReceiverKey();
	        if (!redisAdapter.existsKey(anchorKey)) {
	        	Anchor anchor = AnchorInfoHelper.getAndCacheAnchorInfo(redisAdapter, anchorMapper, order.getReceiverKey(), "userName");
	        	if (anchor != null) {
	        		json.put("anchorName", anchor.getUserName());
	        	}
	        } else {
	            Map<String, String> anchorDetail = redisAdapter.hashMGet(anchorKey, "userName");
	            if (anchorDetail != null && !anchorDetail.isEmpty()) {
	        		json.put("anchorName", anchorDetail.get("userName"));
	            }
	        }
		}
        json.putAll(userInfo);
        return json;
    }

    /**
     * 发送开通高档贵族的消息
     * @param goods
     * @param orderInfo
     * @param isInVideo 是否在直播间开通
     * @throws ParseException
     */
    private void sendNobleMSG(GoodsInfo goods, OrderInfo orderInfo, boolean isInVideo) throws ParseException {
		//魔法 紫荆 和神殿开通要发通知
		String special = goods.getSpecial();
		DictInfoEnum dict = DictInfoEnum.getDictInfoEnum(special);
		if (dict != null && dict.getLevel() >= 4) {
			//用户名, 骑士名, 主播名
			JSONObject json = getObjectJson(orderInfo, goods, isInVideo);
            AllChatroomsNoticeHelper.sendMsg4AllChatrooms(liveVideoInfoMapper, new Integer(32), json.toString(), json.toString());
		} else if (isInVideo && dict != null && dict.getLevel() < 4) {
            Long videoId = orderInfo.getOriginKey();
			JSONObject json = getObjectJson(orderInfo, goods, isInVideo);
			String ids = getChatroomId(videoId);
            RongMsgUtils.sendChatroomMsg(ids, 0L, new Integer(32), json.toJSONString(), json.toJSONString());
		}
		
    }
    
}
