package com.chineseall.iwanvi.wwlive.web.common.pay.type;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.chineseall.iwanvi.wwlive.common.cache.RedisClientAdapter;
import com.chineseall.iwanvi.wwlive.common.cache.RedisExpireTime;
import com.chineseall.iwanvi.wwlive.common.cache.RedisKey;
import com.chineseall.iwanvi.wwlive.common.constants.Constants;
import com.chineseall.iwanvi.wwlive.common.enums.GoodsEnum;
import com.chineseall.iwanvi.wwlive.common.tools.HttpUtils;
import com.chineseall.iwanvi.wwlive.common.tools.RongMsgUtils;
import com.chineseall.iwanvi.wwlive.dao.wwlive.*;
import com.chineseall.iwanvi.wwlive.domain.wwlive.*;
import com.chineseall.iwanvi.wwlive.web.common.helper.*;
import com.chineseall.iwanvi.wwlive.web.common.pay.DefaultUserPay;
import com.chineseall.iwanvi.wwlive.web.common.pay.afterpay.AfterPayTask;
import com.chineseall.iwanvi.wwlive.web.common.util.StringTools;
import com.chineseall.iwanvi.wwlive.web.video.service.NoticeService;
import com.service.impl.FollowNoticeServiceImpl;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

/**
 * 钻石支付
 * @author DIKEPU
 * @since 2017-01-23 二期
 */
@Component("zsVirturalPay")
public class ZsVirturalPay extends DefaultUserPay {

	public static final Logger LOGGER = Logger.getLogger(ZsVirturalPay.class);
	
	@Autowired
    private NoticeService noticeService;
	
	@Autowired
	private ThreadPoolTaskExecutor taskExecutor;
	
	@Autowired
	private LiveVideoInfoMapper liveVideoInfoMapper;

	@Autowired
	private AcctInfoMapper acctInfoMapper;

	@Autowired
	private ContributionListMapper contributionListMapper;

	@Autowired
	private RedisClientAdapter redisAdapter;
	
	@Autowired
	private GoodsInfoMapper goodsInfoMapper;

	@Autowired
	private OrderInfoMapper orderInfoMapper;

	@Autowired
	private AnchorMapper anchorMapper;

	@Autowired
	private UserInfoMapper userInfoMapper;

	@Autowired
	private RoleInfoMapper roleInfoMapper;
	
	@Autowired
	private AfterPayTask afterPayTask;

	@Autowired
	private ContributionListMapper contribMapper;

	@Value("${redenvelope.event.url}")
	private String redEnvelopeURL;

	@Value("${dz.syncdata.url}")
	private String syncDataUrl;

	@Value("${dz.getDmd.url}")
	private String getDzDmdUrl;

	@Value("${dz.deductDmd.url}")
	private String deductDmdUrl;

	@Override
	public Map<String, Object> pay(OrderInfo order, String app) {
		//1.通过订单信息获得用户账户信息 2.判断是否钻石够支付 3.如果够减去redis中的缓存 4.线程生成trans信息 ，生成贡献信息
		Map<String, Object> result = new HashMap<String, Object>();//0失败, 1成功, 2钻石不够 

		Long coin = 0L;
		Long userId = order.getUserId();
		if ("dz".equalsIgnoreCase(app)) { // 定制版获取余额
			coin = getDmdFromDZ(userId);
		} else { // 直播获取余额
			Map<String, Object> acctMap = UserAcctInfoHelper.getAcctInfoCoin(acctInfoMapper, redisAdapter, userId);
			if (acctMap == null || acctMap.isEmpty()) {
				result.put("result", 2);
				result.put("coin", 0.0);
				return result;
			}
			Object objCoin = acctMap.get("coin");
			if (objCoin instanceof String) {
				coin = Long.valueOf((String) objCoin);
			} else if (objCoin instanceof Long){
				coin = (Long) objCoin;
			}
		}

		long reduce = order.getAmt();
		if (coin < reduce) {
			result.put("result", 2);
			result.put("coin", new BigDecimal(coin).divide(new BigDecimal(100), 2, BigDecimal.ROUND_HALF_UP));
			return result;
		}

		//扣除金额
		if ("dz".equalsIgnoreCase(app)) {
			dealOrder(order, app);//处理订单
			result.put("result", 1);
		} else {
			//用户coin
			String coinKey = RedisKey.USER_COIN_ + userId;
			long surplus = redisAdapter.hashIncrBy(coinKey, "coin", -reduce);//剩余
			if (surplus >= 0) {
				redisAdapter.expireKey(coinKey, RedisExpireTime.EXPIRE_DAY_1);//设置过期时间
				dealOrder(order, app);//处理订单
				result.put("result", 1);
			} else {//余额不足要给用户返回最后减的这笔钱
				redisAdapter.hashIncrBy(coinKey, "coin", reduce);//剩余
				result.put("result", 2);
			}
		}
		return result;
	}

	/**
	 * 定制版扣钻, 返回余额
	 * @param userId
	 * @param reduce
	 * @return
	 */
	private long deductDzDmd(Long userId, long reduce) {
		try {
			String url = deductDmdUrl + "&zbUserId=" + userId + "&amount=" + reduce;
			String response = HttpUtils.getJSON(url, Constants.UTF8);
			Map resultMap = (Map) JSON.parse(response);
			long diamond = MapUtils.getLongValue(resultMap, "data", -1L);
			return diamond;
		} catch (Exception e) {
			LOGGER.error("从定制版扣钻异常", e);
			return -1L;
		}
	}

	/**
	 * 从定制版获取余额
	 * @param userId
	 * @return
	 */
	private Long getDmdFromDZ(Long userId) {
		try {
			String url = getDzDmdUrl + "&zbUserId=" + userId;
			String response = HttpUtils.getJSON(url, Constants.UTF8);
			Map diamondMap = (Map) JSON.parse(response);
			long diamond = new BigDecimal(MapUtils.getDoubleValue(diamondMap, "data") + "").multiply(new BigDecimal("100")).longValue();
			return diamond;
		} catch (Exception e) {
			LOGGER.error("从定制版获取余额异常", e);
			return 0L;
		}
	}

	/**
	 * 处理订单信息
	 * @param order
	 */
	private void dealOrder(OrderInfo order, String app) {
		String str = JSONObject.toJSONString(order);
		String orderKey = RedisKey.USER_ORDER_LIST_ + order.getUserId();
		redisAdapter.listRpush(orderKey, str);
		redisAdapter.expireKey(orderKey, RedisExpireTime.EXPIRE_DAY_30);
		if(!redisAdapter.existsKey(RedisKey.USER_ORDER_DEAL_ + order.getUserId())) {
			dealOrderThread(order, app);
		}
		if (!"01".equals(order.getReceiveNo()) 
				&& !"03".equals(order.getReceiveNo())) {//回放不发消息
			final String url = this.redEnvelopeURL;
			noticeThread(order, url);
		}
	}
	
	private void delAcctInfoCache(Long userId, Long anchorId) {
		String key = RedisKey.USER_CTB_DO_ + userId;
        String anKey = "anchor_rank_" + anchorId;
		redisAdapter.delKeys(key, anKey);
	}
	
	/**
	 * 处理订单信息，修改用户的订单信息，修改交易信息，修改用户贡献值，完成积分统计工作
	 * @param tmp
	 */
	private void dealOrderThread(final OrderInfo tmp, final String app) {
		taskExecutor.execute(new Runnable() {
			@Override
			public void run() {
				//1.先设置处理标志 2.处理订单信息 3.生成相应的数据
				Long userId = tmp.getUserId();
				String dealKey = RedisKey.USER_ORDER_DEAL_ + userId;
				redisAdapter.strSetexByNormal(dealKey, RedisExpireTime.EXPIRE_MIN_3,
						"0");

				String jsonOrder = "";
				String orderKey = RedisKey.USER_ORDER_LIST_ + userId;
				OrderInfo order = null;
				while ((jsonOrder = redisAdapter.listRpop(orderKey)) != null
						&& StringUtils.isNotBlank(jsonOrder)) {
					try {
						AcctInfo acct = acctInfoMapper.findAcctByUserId(userId);
						order = JSONObject.parseObject(jsonOrder,
								OrderInfo.class);

						long reduce = order.getAmt();
						if ("dz".equalsIgnoreCase(app)) { // 定制版扣钻
							Long dmd = getDmdFromDZ(userId);
							if (dmd < reduce) {
								String key = RedisKey.USER_COIN_ + userId;
								redisAdapter.delKeys(key);
								return;
							}

							// 扣钻
							long balance = deductDzDmd(userId, reduce);// 定制版余额
							if (balance < 0) {
								return;
							}
						} else { // 非定制版
							if (acct.getVideoCoin() < reduce) {
								String key = RedisKey.USER_COIN_ + userId;
								redisAdapter.delKeys(key);
								return;
							}
							//减去支付的金额
							acct.setVideoCoin(acct.getVideoCoin() - reduce);
							int cnt = acctInfoMapper.updateAcctCoinById(acct);
							if (cnt <= 0) {
								redisAdapter.listRpush(orderKey, JSONObject.toJSONString(order));
								redisAdapter.expireKey(orderKey, RedisExpireTime.EXPIRE_DAY_30);
								continue;
							}
						}

						order.setOrderStatus(Constants._1);// orderStatus 支付状态 0未支付，
						// 1成功，2失败， 3关闭， 4异常

						orderInfoMapper.insertOrderInfo(order);
						delAcctInfoCache(order.getUserId(), order.getReceiverKey());//删除用户的钻石数量缓存
						afterPayTask.afterPay(order);//支付完成后的逻辑
						// 如果是定制版则同步数据
						if ("dz".equalsIgnoreCase(app)) {
							syncDataWithDZ(order);
						}
					} catch(Exception e) {
						redisAdapter.delKeys(dealKey);
						LOGGER.error("用户支付礼物失败：" + tmp, e);
					}
				}
				if (order != null && order.getReceiverKey() != null) {//兼容旧版
					afterPayMsg(order.getAmt(), order.getReceiverKey(), order.getOriginKey(), userId,
							contribMapper,redisAdapter,liveVideoInfoMapper);
				}
				redisAdapter.delKeys(dealKey);
			}
		});
	}

	/**
	 * 和定制版同步数据
	 * @param order
	 */
	private void syncDataWithDZ(OrderInfo order) {
		LOGGER.info("开始和定制版同步数据, orderInfo = " + order);
		String pendingKey = RedisKey.PENDING_ORDER_ + order.getOrderId();
		Map<String, Object> orderInfo = new HashMap<>();
		orderInfo.put("order", JSON.toJSONString(order));
		redisAdapter.hashMSet(pendingKey, orderInfo);

		try {
			HttpURLConnection conn = HttpUtils.createPostHttpConnection(syncDataUrl);

			StringBuilder params = new StringBuilder();
			params.append("type=4"); // 和定制版约定type=4为直播送礼
			params.append("&amount=" + order.getAmt()); // 实际消费的钻石数
			params.append("&zbUserId=" + order.getUserId());
			params.append("&goodsId=" + order.getGoodsId());
			params.append("&goodsName=" + order.getGoodsName());
			params.append("&goodsCnt=" + order.getGoodsNum());
			params.append("&anchorId=" + order.getReceiverKey());
			params.append("&outTradeNo=zbxf_" + order.getOutTradeNo()); // 外部订单号为zbxf(直播消费) + outTradeNo, 订单id获取不到

			OutputStream out = conn.getOutputStream();
			out.write(params.toString().getBytes());
			out.close();

			int responseCode = conn.getResponseCode();
			if (200 == responseCode) {
				BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), "utf-8"));
				String response = reader.readLine();
				Map resultMap = (Map) JSON.parse(response);
				int result = MapUtils.getIntValue(resultMap, "result", 0);
				if (result == 1) {
					// 将待同步的充值信息缓存删除
					redisAdapter.delKeys(pendingKey);
					LOGGER.info("和定制版同步数据完成, orderInfo = " + orderInfo);
					LOGGER.info("userId = " + order.getUserId() +  ", goodsName = " + order.getGoodsName() + ", diamonds = " + order.getAmt() );
				} else {
					LOGGER.warn("和定制版同步数据失败, 请求返回 = " + response + ", orderInfo = " + orderInfo);
				}
			} else {
				LOGGER.warn("和定制版同步数据失败, code = " + responseCode + ", orderInfo = " + orderInfo);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 机器人通知，送礼通知，特效动画通知
	 * @param order
	 * @param url
	 */
	private void noticeThread(final OrderInfo order, final String url) {
		taskExecutor.execute(new Runnable() {
			@Override
			public void run() {
				try {
					LiveVideoInfo videoInfo = new LiveVideoInfo();
					LiveVideoInfo info = null;
					Long videoId = order.getOriginKey();
					if (redisAdapter.existsKey(RedisKey.LIVE_VIDEO_INFO_ + videoId)) {
						info = LiveVideoInfoHelper.getVideoInfoCache(redisAdapter, liveVideoInfoMapper, videoId,
                                "chatroomId", "anchorId","coverImg","formatType","videoStatus");
					} else {
						info = LiveVideoInfoHelper.getAndCacheVideoInfo(redisAdapter, liveVideoInfoMapper, videoId);
					}
					videoInfo.setChatroomId(info.getChatroomId());
					videoInfo.setAnchorId(info.getAnchorId());
					for (int i = 0; i < order.getGoodsNum(); i++) {
						treadNotice(order, videoInfo);
					}
					FollowNoticeServiceImpl followNoticeService = FolloServiceSingleton.getInstance();
					try {
						followNoticeService.noticeAnchorMoney(order.getReceiverKey(), 
								((order.getGoodsPrice().intValue() / 100) * order.getGoodsNum()), new Date().getTime());
					} catch (Exception e) {
						LOGGER.error("通知主播收礼异常", e);
					}
					if (redisAdapter.existsKey(RedisKey.GODDESS_EVENT_START)) {//女神活动
						goddessNotice(order, url);
					}
		            if(order.getGoodsId().longValue() == 7 || order.getGoodsId().longValue() == 8){//7跑车 8游艇  未定义类型写死
		            	JSONObject json = getSpecialGiftMsg(order);
		            	json.put("videoId", videoId);
		            	json.put("anchorId",info.getAnchorId());
		            	json.put("chatroomId",getChatroomId(videoId));
		            	json.put("type",info.getFormatType());
						json.put("totalAmt", UserRankHelper.getUserInAnchorIdAmt(redisAdapter,contribMapper,order.getUserId()+"",info.getAnchorId()));
                        if(info.getVideoStatus() != 4){
                            AllChatroomsNoticeHelper.sendMsg4AllChatrooms(liveVideoInfoMapper, new Integer(22), json.toString(), "");
                        }
		            }
                    long goodsId = order.getGoodsId();
		            String goodsKey = RedisKey.GOODS_INFO_ + goodsId;
		            GoodsInfo goods = null;
		            if (redisAdapter.existsKey(goodsKey)) {
		    			goods = GoodsInfoHelper.getGoodsInfo(redisAdapter, 
		    					goodsId, "special", "goodsPrice", "goodsType");
		            } else {
		    			goods = GoodsInfoHelper.getAndCacheGoodsInfo(redisAdapter, goodsInfoMapper, goodsId);
		            }
                    if (goods != null &&
		            		GoodsEnum.ROCKET
		            		.getSpecial().equals(goods.getSpecial())) {//火箭消息
		            	String userName = "";
		            	String userKey = RedisKey.USER_INFO_ + order.getUserId();
		            	if (redisAdapter.existsKey(userKey)) {
		            		UserInfo u = 
		            				UserInfoHelper.getUserInfoFromCache(redisAdapter, order.getUserId(), "userName");
		            		userName = (u == null) ? "" : u.getUserName();
		            	} else {
		            		UserInfo u = 
		            				UserInfoHelper.getAndCacheUserInfo(redisAdapter, userInfoMapper, order.getUserId());
		            		userName = (u == null) ? "" : u.getUserName();
		            	}
						String chatroomId = getChatroomId(videoId);
						sendRocketMsg(chatroomId, order, userName,info.getAnchorId());
		            }

		            int goodsType = goods.getGoodsType()!=null ? goods.getGoodsType():0;

		            if(goods.getGoodsPrice() >= 100000 || goodsType == 5) {//线上用此判断
                        JSONObject msg = getSpecialGiftMsg(order);
                        msg.put("anchorId",info.getAnchorId());
                        msg.put("videoId",videoId);
                        msg.put("chatroomId",getChatroomId(videoId));
                        msg.put("cover",info.getCoverImg());
                        msg.put("type",info.getFormatType());
						msg.put("totalAmt", UserRankHelper.getUserInAnchorIdAmt(redisAdapter, contribMapper, order.getUserId()+"", info.getAnchorId()));
						if(order.getGoodsId().longValue() != 7 
								&& order.getGoodsId().longValue() != 8){
							//除本直播间飘屏，前去围观
							if(info.getVideoStatus() != 4 ){
								AllChatroomsNoticeHelper.sendMsg4NotThisChatrooms(liveVideoInfoMapper, new Integer(31), msg.toString(), msg.toString(), getChatroomId(videoId));
							}
							//本直播间飘屏
							String chatroomId = getChatroomId(videoId);
							JSONObject json = getSpecialGiftMsg(order);
							json.put("totalAmt", UserRankHelper.getUserInAnchorIdAmt(redisAdapter,contribMapper,order.getUserId()+"",info.getAnchorId()));
							RongMsgUtils.sendChatroomMsg(chatroomId, 0L, new Integer(22), json.toJSONString(), json.toJSONString());
						}
                    }
//		            sendContribMSG(order);
				} catch(Exception e) {
                    e.printStackTrace();
                    System.out.println(e);
                    LOGGER.error("用户支付礼物失败：" + order, e);
				}
			}
		});
	}

	private void treadNotice(OrderInfo order, LiveVideoInfo info) {
		try {
			if (info != null) {
				noticeService.manSendGift(order.getOriginKey(), info.getChatroomId(), info.getAnchorId(), 
					order.getGoodsId(), (order.getGoodsPrice().intValue() / 100.0), order.getUserId());
			}
		}catch(Exception e){
			LOGGER.info("支付调用机器人异常", e);
		}
	}
	
	private void goddessNotice(final OrderInfo order, final String url) {//女神活动存在就改写积分发送红包
		
		String str = JSONObject.toJSONString(order);
		String orderKey = RedisKey.ANCHOR_ORDER_LIST_ + order.getReceiverKey();
		redisAdapter.listRpush(orderKey, str);
		redisAdapter.expireKey(orderKey, RedisExpireTime.EXPIRE_DAY_30);
		String anchorName = getAnchorName(order);
		Map<String, String> paramsMap = new HashMap<String, String>();
		paramsMap.put("videoId", order.getOriginKey().toString());
		paramsMap.put("anchorId", order.getReceiverKey().toString());
		paramsMap.put("anchorName", anchorName);
		String tmpURL = StringTools.replace(paramsMap, url);//请求地址
		
		HttpURLConnection conn;
		try {
			conn = HttpUtils.createGetHttpConnection(tmpURL, Constants.UTF8);
	        HttpUtils.returnString(conn);
		} catch (Exception e) {
			LOGGER.error("发送红包通知异常", e);
		}
		
	}
	
	private String getAnchorName(final OrderInfo order) {
		String anchorInfoKey = RedisKey.ANCHOR_INFO_ + order.getReceiverKey();
		String anchorName = "";
		if (redisAdapter.existsKey(anchorInfoKey)) {
			anchorName = redisAdapter.hashGet(anchorInfoKey, "userName");
		} else {
			Anchor anchor = AnchorInfoHelper.getAndCacheAnchorInfo(redisAdapter, anchorMapper, order.getReceiverKey());
			if (anchor != null) {
				anchorName = anchor.getUserName();
			}
		}
		return anchorName;
	}
	
	/**
	 * 获得订单用户的昵称
	 * @param order
	 * @return
	 */
	private String getUserName(final OrderInfo order) {
		String userName = "";
    	if (redisAdapter.existsKey(RedisKey.USER_INFO_ + order.getUserId())) {
    		userName = redisAdapter.hashGet(RedisKey.USER_INFO_ + order.getUserId(), "userName");
    	} else {
    		UserInfo user = UserInfoHelper.getAndCacheUserInfo(redisAdapter, userInfoMapper, order.getUserId());
    		if (user != null) {
    			userName = user.getUserName();
    		}
    	}
    	return userName;
	}
	
	/**
	 * 特殊礼物消息
	 * @param order
	 * @return
	 */
	private JSONObject getSpecialGiftMsg(final OrderInfo order) {
    	String userName = getUserName(order);
    	String anchorName = getAnchorName(order);
    	String giftName = order.getGoodsName();
    	JSONObject json = new JSONObject();
		json.put("userName", userName);
		json.put("anchorName", anchorName);
		json.put("giftName", giftName);
		return json;
	}
	private void sendRocketMsg(String chatroomId, OrderInfo order, String userName,long anchorId) {
		JSONObject json = new JSONObject();
		json.put("userName", userName);
		json.put("expireTime", RedisExpireTime.EXPIRE_MIN_10);
		Long userId = order.getUserId();
		UserInfo userInfo = UserInfoHelper.getUserInfoFromCache(redisAdapter, userId, "loginId");
		if (userInfo == null) {
			userInfo = UserInfoHelper.getAndCacheUserInfo(redisAdapter, userInfoMapper, userId);
		}
		json.put("loginId", userInfo.getLoginId());
		json.put("lv", RoleNobleHelper.userRoleNobleLevel(redisAdapter, roleInfoMapper, userId));
		json.put("totalAmt", UserRankHelper.getUserInAnchorIdAmt(redisAdapter,contribMapper, userId +"",anchorId));
		RongMsgUtils.sendChatroomMsg(chatroomId, 0L, new Integer(28), "",
				json.toJSONString());
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

    /*private void sendContribMSG(OrderInfo orderInfo) {
    	JSONObject json = getContributionValue(orderInfo);
		String ids = getChatroomId(orderInfo.getOriginKey());
        RongMsgUtils.sendChatroomMsg(ids, 0L, new Integer(27), "", json.toJSONString());
    }
    
    private JSONObject getContributionValue(OrderInfo orderInfo) {
        JSONObject json = new JSONObject();
        json.put("contribVal", orderInfo.getTotalGoodsPrice() + "");
    	return json;
    }*/
    
	public static void afterPayMsg(Long amt, Long anchorId, Long videoId, Long userId,ContributionListMapper contribMapper,
								   RedisClientAdapter redisAdapter,LiveVideoInfoMapper liveVideoInfoMapper) {
		try {
			LiveVideoInfo videoInfo = LiveVideoInfoHelper.getVideoInfoCache(redisAdapter, liveVideoInfoMapper, videoId, "chatroomId");
			if (videoInfo == null || videoInfo.getChatroomId() == null){
				videoInfo = LiveVideoInfoHelper
						.getAndCacheVideoInfo(redisAdapter, liveVideoInfoMapper, videoId);
			}
			String chatroomId = videoInfo.getChatroomId();
			// 获取主播贡献值

			Long contrib = 0L;
			String contribKey = RedisKey.ANCHOR_CONTRIB_ + anchorId;
			if (redisAdapter.existsKey(contribKey)) {
				contrib = redisAdapter.strIncrBy(contribKey, amt);
			} else {
				contrib = ContribHelper.getNormalByAnchor(anchorId, contribMapper, redisAdapter);
			}

			//Long contrib = contribMapper.getContribByAnchorId(anchorId, 1);
			JSONObject dataExtra = new JSONObject();
			dataExtra.put("contribVal", contrib.toString());
			Integer syncMsgType = 27;
			RongMsgUtils.sendChatroomMsg(chatroomId, userId,syncMsgType, "", dataExtra.toJSONString());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}
