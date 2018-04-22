package com.chineseall.iwanvi.wwlive.web.common.pay.afterpay;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.commons.lang.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSONObject;
import com.chineseall.iwanvi.wwlive.common.cache.RedisClientAdapter;
import com.chineseall.iwanvi.wwlive.common.cache.RedisExpireTime;
import com.chineseall.iwanvi.wwlive.common.cache.RedisKey;
import com.chineseall.iwanvi.wwlive.common.constants.Constants;
import com.chineseall.iwanvi.wwlive.common.enums.GoodsEnum;
import com.chineseall.iwanvi.wwlive.dao.wwlive.ContributionListMapper;
import com.chineseall.iwanvi.wwlive.dao.wwlive.GoodsInfoMapper;
import com.chineseall.iwanvi.wwlive.dao.wwlive.RoleInfoMapper;
import com.chineseall.iwanvi.wwlive.dao.wwlive.TransInfoMapper;
import com.chineseall.iwanvi.wwlive.dao.wwlive.UserInfoMapper;
import com.chineseall.iwanvi.wwlive.domain.wwlive.ContributionList;
import com.chineseall.iwanvi.wwlive.domain.wwlive.GoodsInfo;
import com.chineseall.iwanvi.wwlive.domain.wwlive.OrderInfo;
import com.chineseall.iwanvi.wwlive.domain.wwlive.TransInfo;
import com.chineseall.iwanvi.wwlive.web.common.helper.GoodsInfoHelper;
import com.chineseall.iwanvi.wwlive.web.common.helper.RoleNobleHelper;
import com.chineseall.iwanvi.wwlive.web.common.helper.UserInfoHelper;

/**
 * 用来完成用户之后的一些功能逻辑，该用户的贡献值、交易、主播收入缓存信息
 * <br/>
 * 四期增加直播收入排序功能
 * <br/>
 * 七期增加火箭排序功能
 * @author DIKEPU
 * @since 2017-01-23 二期
 */
@Component
public class AfterPayTask {
	
	@Autowired
	private ContributionListMapper contributionListMapper;

	@Autowired
	private RedisClientAdapter redisAdapter;

	@Autowired
	private TransInfoMapper transInfoMapper;

	@Autowired
	private GoodsInfoMapper goodsInfoMapper;
	
    @Autowired
    private RoleInfoMapper roleInfoMapper;

    @Autowired
    private UserInfoMapper userInfoMapper;
    
	/**
	 * 支付后完成的逻辑
	 * @param order
	 */
	public void afterPay(OrderInfo order) {
		if (order != null){
			buildTransInfo(order);
			upsertContributionAndUpdateRank(order);
			delCache(order.getReceiverKey(), order.getUserId());
		}
	}
	
	/**
	 * 生成或更新该用户的贡献，修改该视频的位置
	 * @param order
	 */
	private void upsertContributionAndUpdateRank(OrderInfo order) {
		String key = RedisKey.USER_CONTRIBUTION_CNT_ + order.getReceiverKey() + Constants.UNDERLINE + order.getUserId();
		int cnt = 0;
		if (redisAdapter.existsKey(key)) {
			String strCnt = redisAdapter.strGet(key);
			if (!"0".equals(strCnt)) {
				cnt = 1;
			}
		}  else {
			cnt = contributionListMapper.countByAnchorAndUser(order.getReceiverKey(), order.getUserId(), 1);
		}
		dealContributionList(order, cnt, key);//处理用户的贡献值信息
        if (!"01".equals(order.getReceiveNo())
				&& !"03".equals(order.getReceiveNo())) {//回放不排序
            rankVideoInfo4HomeTab(order);//主播首页排序
        }
	}
	
	/**
	 * 处理用户的贡献值信息
	 * @param order
	 * @param cnt
	 * @param key
	 */
	private void dealContributionList(OrderInfo order, int cnt, String key) {
		double totalAmt = order.getAmt();//礼物价格
		if (cnt > 0) {
			cnt = contributionListMapper.updateTotalAmt(order.getAmt(), order.getTotalGoodsPrice(),
					order.getGoodsNum(), order.getReceiverKey(), order.getUserId(), 1);
		} else {
			ContributionList con = new ContributionList();
			con.setAnchorId(order.getReceiverKey());
			con.setUserId(order.getUserId());
			con.setGoodsNum(order.getGoodsNum());
			con.setOriginalAmt(order.getTotalGoodsPrice());
			con.setTotalAmt(totalAmt);
			con.setType(1);
			cnt = contributionListMapper.insertContribution(con);
		}
		if (cnt > 0) {
			cntbMarkKey(key, cnt);//贡献标志
		} else {
			delbMarkKey(key);
		}
	}
	
	/**
	 * 排序主播在首页的位置
	 * <br/>
	 * 排序规则，主播后台排序、小火箭、收礼
	 * @param order
	 */
	private void rankVideoInfo4HomeTab(OrderInfo order) {
		//修改该视频的位置
		String orderKey = RedisKey.VIDEO_GIFT_ + order.getReceiverKey() + Constants.UNDERLINE 
				+ DateFormatUtils.format(new Date(), Constants.YY_MM_DD);//当日主播所有视频有效
		
		Double rank = redisAdapter.zsetScore(RedisKey.ANCHOR_RANK, 
				order.getReceiverKey().toString());
		
		if (redisAdapter.existsKey(orderKey)) {
			redisAdapter.strIncrBy(orderKey, order.getTotalGoodsPrice());//被取消排序时用到
			if (rank != null && rank.doubleValue() > 0.0) {//非后台排序的才能改变分值
				return;
			}
			//小火箭热度，主播放到首页
			if (dealRocketInfo(order)) {
				return;
			}
			if ((redisAdapter.zsetScore(RedisKey.VideoKeys.LIVING_VIDEOS_, order.getReceiverKey().toString()) != null)) {
				redisAdapter.zsetIncrBy(RedisKey.VideoKeys.LIVING_VIDEOS_, 
						order.getTotalGoodsPrice(), order.getReceiverKey().toString());
			}
			//灰度代码
			if ((redisAdapter.zsetScore(RedisKey.VideoGrayKeys.LIVING_VIDEOS_, order.getReceiverKey().toString()) != null)) {
				redisAdapter.zsetIncrBy(RedisKey.VideoGrayKeys.LIVING_VIDEOS_, 
						order.getTotalGoodsPrice(), order.getReceiverKey().toString());
			}
		} else {//该主播今日首次获得礼物
			long score = getScore(order, (long)(Constants.LIVING_VIDEO_PAY_SCORE + order.getTotalGoodsPrice())); //得分算法
			redisAdapter.strSetexByNormal(orderKey, RedisExpireTime.EXPIRE_DAY_2, score + "");//后台该主播被取消排序时用到
			if (rank != null && rank.doubleValue() > 0.0) {//非后台排序的才能改变分值
				return;
			}
			//小火箭热度，主播放到首页
			if (dealRocketInfo(order)) {
				return;
			}
			if ((redisAdapter.zsetScore(RedisKey.VideoKeys.LIVING_VIDEOS_, order.getReceiverKey().toString()) != null)) {
				redisAdapter.zsetAdd(RedisKey.VideoKeys.LIVING_VIDEOS_, 
					score, order.getReceiverKey().toString());
			}
			//灰度代码
			if ((redisAdapter.zsetScore(RedisKey.VideoGrayKeys.LIVING_VIDEOS_, order.getReceiverKey().toString()) != null)) {
				redisAdapter.zsetAdd(RedisKey.VideoGrayKeys.LIVING_VIDEOS_, 
					score, order.getReceiverKey().toString());
			}
		}
	}
	
	/**
	 * 处理火箭信息
	 * @param order
	 * @return
	 */
	private boolean dealRocketInfo(OrderInfo order) {
		long goodsId = order.getGoodsId();
        String goodsKey = RedisKey.GOODS_INFO_ + goodsId;
		GoodsInfo goods = null;
        if (redisAdapter.existsKey(goodsKey)) {
			goods = GoodsInfoHelper.getGoodsInfo(redisAdapter, 
					goodsId, "special");
        } else {
			goods = GoodsInfoHelper.getAndCacheGoodsInfo(redisAdapter, goodsInfoMapper, goodsId);
        }
        if (goods != null && 
        		GoodsEnum.ROCKET
        		.getSpecial().equals(goods.getSpecial())) {//火箭消息
        	rankByRocket(order.getReceiverKey(), order.getUserId());
        	return true;
        }
        return false;
	}
	
	/**
	 * 送火箭后进行排序，如果是该主播第一次收到小火箭的礼物且是第一次的话就是火箭礼物的基本分数
	 * 如果该主播是第一次收到火箭后，但小火箭不是第一次送出，就在基础的分值上-1。
	 * 
	 * @param anchorId
	 * @param userId
	 */
	private void rankByRocket(long anchorId, long userId) {
		buildRocketGiverInfo(anchorId, userId);
//		redisAdapter.strSetexByNormal(RedisKey.ROCKET_GIVER_,
//				RedisExpireTime.EXPIRE_MIN_10, userId + "");
		String receiverKey = RedisKey.ROCKET_RECEIVER;
		if (redisAdapter.setIsMember(receiverKey, anchorId + "")) {
			return;
		} else {
			redisAdapter.setAdd(receiverKey, anchorId + "");
			String key = RedisKey.BASE_ROCKET_SCORE;
			if (redisAdapter.existsKey(key)) {
				Long score = redisAdapter.strDecrBy(key, 1);
				setTabPageRank(anchorId, score);
				redisAdapter.expireKey(key, RedisExpireTime.EXPIRE_HOUR_1);
			} else {
				Long score = Constants.LIVING_VIDEO_PAY_ROCKET_SCORE;
				setTabPageRank(anchorId, score);
				redisAdapter.strSetexByNormal(key, RedisExpireTime.EXPIRE_DAY_1, score.toString());
			}
		}
		redisAdapter.expireKey(receiverKey, RedisExpireTime.EXPIRE_DAY_2);
	}

	/**
	 * 设置首页位置
	 * @param anchorId
	 * @param score
	 */
	private void setTabPageRank(long anchorId, Long score) {
		if ((redisAdapter.zsetScore(RedisKey.VideoKeys.LIVING_VIDEOS_, anchorId
				+ "") != null)) {
			redisAdapter.zsetAdd(RedisKey.VideoKeys.LIVING_VIDEOS_, score,
					anchorId + "");
		}
		if ((redisAdapter.zsetScore(RedisKey.VideoGrayKeys.LIVING_VIDEOS_,
				anchorId + "") != null)) {
			redisAdapter.zsetAdd(RedisKey.VideoGrayKeys.LIVING_VIDEOS_, score,
					anchorId + "");
		}
		redisAdapter.strSetexByNormal(RedisKey.ROCKET_RECEIVER_SCORE_
				+ anchorId, RedisExpireTime.EXPIRE_HOUR_1, score.toString());
	}

	private void delCache(Long anchorId, Long userId) {
		String key = RedisKey.ANCHOR_INCOME_VIEDOCNT_ + anchorId;
		String rankKey = RedisKey.USER_RANK_CONTRIB_ + userId;
		String tatalKey = RedisKey.ANCHOR_USER_AMT_ + anchorId + Constants.UNDERLINE + userId;
		redisAdapter.delKeys(key, rankKey,tatalKey);
	}
	
	/**
	 * 如果首次获得礼物信息，则重新计算该主播的首页排序得分信息 	 
	 * @param order 订单信息
	 * @param totalGoodsPrice 初次收入  + 默认值 {@link Constants#LIVING_VIDEO_PAY_SCORE}
	 * @return 得分
	 */
	private long getScore(OrderInfo order, long totalGoodsPrice) {
		String videoKey = RedisKey.LIVE_VIDEO_INFO_ + order.getOriginKey();
		Map<String, String> videoMap = redisAdapter.hashMGet(videoKey, "viewers", "videoId");
		String viewers = videoMap.get("viewers");
		if (StringUtils.isBlank(viewers)) {
			viewers = "0";
		}
		BigDecimal scoreViewers = new BigDecimal(viewers).divide(new BigDecimal(1000)).setScale(3).add(new BigDecimal(totalGoodsPrice));
		return scoreViewers.longValue();
	}

	/**
	 * 对该主播有过贡献
	 * @param anchorId
	 * @param userId
	 */
	private void cntbMarkKey(String key, int cnt) {
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
	private void delbMarkKey(String key) {
		redisAdapter.delKeys(key);
	}
	
	/**
	 * 生成交易流水信息
	 * @param order
	 * @return
	 */
	private void buildTransInfo(OrderInfo order) {
		TransInfo trans = new TransInfo();
		trans.setUserId(order.getUserId());
		trans.setOutId(order.getOrderId());
		trans.setTransType(Constants._0);//交易类型 0-消费 1充值
		trans.setTransStatus(Constants._0);
		trans.setAmt(order.getAmt());
		trans.setPayType(order.getPayType());//支付类型  0-铜币, 1-积分, 2-微信, 3-支付宝, 4-钻石
		transInfoMapper.insertTransInfo(trans);
	}
	
	/**
	 * 充值完成后加密
	 * @param outTradeNo
	 */
	public void receivePayMsg(String outTradeNo) {
		String secret = outTradeNo;
		redisAdapter.delKeys(RedisKey.SC_WX_SECRET_ + secret);
		redisAdapter.strSetexByNormal(RedisKey.PAY_NOTICE_ + outTradeNo, RedisExpireTime.EXPIRE_DAY_1 , "0");
	}
	
	/**
	 * 创建送火箭礼物的list信息，用户id 和过期时间
	 * @param order
	 */
	private void buildRocketGiverInfo(long anchorId, long userId) {
		String orderKey = RedisKey.ROCKET_GIVER_LIST_ + anchorId + Constants.UNDERLINE 
				+ DateFormatUtils.format(new Date(), Constants.YY_MM_DD);//当日主播所有视频有效
		/*
		 *当用户初次发送火箭礼物时，改list存在10分钟，再送再加10分钟 
		 */
		long time = 0;
		if ((time = redisAdapter.ttl(orderKey)) > 0) {
			time += (RedisExpireTime.EXPIRE_MIN_10 - 3);
		} else {
			time = (RedisExpireTime.EXPIRE_MIN_10 - 2);
		}
		JSONObject json = new JSONObject();
		Integer nobleCode = RoleNobleHelper.userRoleNobleLevel(redisAdapter, roleInfoMapper, userId);
		String userName = UserInfoHelper.getUserName(redisAdapter, userInfoMapper, userId);
		json.put("userId", userId);
		json.put("nobleCode", nobleCode);
		json.put("userName", userName);
		json.put("expireTime", DateUtils.addSeconds(new Date(), (int) time).getTime());
		//修改该视频的位置
		redisAdapter.listRpush(orderKey, json.toJSONString());
		redisAdapter.expireKey(orderKey, (int) time);
	}
	
}
