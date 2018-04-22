package test;

import java.math.BigDecimal;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import redis.clients.jedis.Tuple;

import com.alibaba.dubbo.common.json.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.chineseall.iwanvi.wwlive.common.cache.RedisClientAdapter;
import com.chineseall.iwanvi.wwlive.common.cache.RedisExpireTime;
import com.chineseall.iwanvi.wwlive.common.cache.RedisKey;
import com.chineseall.iwanvi.wwlive.common.enums.DictInfoEnum;
import com.chineseall.iwanvi.wwlive.dao.wwlive.AnchorMapper;
import com.chineseall.iwanvi.wwlive.dao.wwlive.BaseDictInfoMapper;
import com.chineseall.iwanvi.wwlive.dao.wwlive.CouponInfoMapper;
import com.chineseall.iwanvi.wwlive.dao.wwlive.DiscountInfoMapper;
import com.chineseall.iwanvi.wwlive.dao.wwlive.GoodsInfoMapper;
import com.chineseall.iwanvi.wwlive.dao.wwlive.PrivilegeInfoMapper;
import com.chineseall.iwanvi.wwlive.dao.wwlive.RoleInfoMapper;
import com.chineseall.iwanvi.wwlive.domain.wwlive.CouponInfo;
import com.chineseall.iwanvi.wwlive.domain.wwlive.DiscountInfo;
import com.chineseall.iwanvi.wwlive.domain.wwlive.DiscountPriceInfo;
import com.chineseall.iwanvi.wwlive.domain.wwlive.RoleInfo;
import com.chineseall.iwanvi.wwlive.web.common.helper.RoleNobleHelper;
import com.chineseall.iwanvi.wwlive.web.launch.service.AnchorService;
import com.chineseall.iwanvi.wwlive.web.nobility.service.NobilityService;
import com.chineseall.iwanvi.wwlive.web.video.service.DaoFactoryService;
import com.chineseall.iwanvi.wwlive.web.video.service.LiveVideoInfo2Service;
import com.chineseall.iwanvi.wwlive.web.video.service.UserInfoService;
import com.chineseall.iwanvi.wwlive.web.video.service.impl.GoodsInfoServiceImpl;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:spring/application.xml" })
public class TestSpring240 {

	@Autowired
	private RoleInfoMapper roleInfoMapper;

	@Autowired
	private BaseDictInfoMapper baseDictInfoMapper;

	@Autowired
	private LiveVideoInfo2Service liveVideoInfo2Service;

    @Autowired
    private AnchorMapper anchorMapper;
    
	@Autowired
	private RedisClientAdapter redisAdapter;

	@Autowired
	private GoodsInfoServiceImpl goodsInfoService;

	@Autowired
	private CouponInfoMapper couponInfoMapper;

    @Autowired
    private AnchorService anchorService;

    @Autowired
    private UserInfoService userService;
    
    @Autowired
    private GoodsInfoMapper goodsInfoMapper;

    @Autowired
    PrivilegeInfoMapper privilegeInfoMapper;

    @Autowired
    private NobilityService nobilityService;

    @Autowired
    private GoodsInfoMapper goodsMapper;
    
	public void test1() {
		RoleInfo level = roleInfoMapper.findLevelByUserId(new Long(1));
		System.out.println(level.getRoleLevel() + ", "
				+ level.getEffectiveEndTime());
	}

	public void test2() {
		RoleInfo level = roleInfoMapper.findLevelByUserId(new Long(2));
		System.out.println(level);
	}

	public void test3() {
		String content = baseDictInfoMapper.getDictContentByCode(DictInfoEnum
				.getDictInfoEnum(1).getCode());
		System.out.println(content);
	}

	public void test4() {
		System.out.println((int) 1.0);
	}

	public void test5() {
		System.out.println(JSONObject.toJSONString(liveVideoInfo2Service
				.getWatchingVideoNobles(12)));
	}

	public void test6() {
		Set<Tuple> set = redisAdapter.zsetRevrangeWithScores(
				"test_zset_123_dikepu", 0, 12);
		for (Tuple t : set) {
			System.out.println(t.getElement() + ", " + t.getScore());
		}
	}

	public void test7() {
		List<RoleInfo> list = roleInfoMapper.findLevelsByUserId(1L);
		for (RoleInfo l : list) {
			System.out.println(l);
		}
		System.out.println(Arrays.toString(list.toArray(new RoleInfo[] {})));
	}

	public void test8() {
		List<Integer> lvs = RoleNobleHelper.userRoleNobleLevels(redisAdapter,
				roleInfoMapper, 1L);
		System.out.println(Arrays.toString(lvs.toArray(new Integer[] {})));
	}

	public void test9() {
		redisAdapter
				.strSetexByNormal("test", RedisExpireTime.EXPIRE_MIN_10, "");
	}

	public void test10() {
		if (redisAdapter.strGet("test").equals("")) {
			System.out.println("keyi");
		}
		System.out.println(redisAdapter.strGet("test"));
	}

	public void test11() {
		System.out.println("-----------------------------------------------");
		System.out.println(JSONObject.toJSONString(goodsInfoService
				.findNobles(1L)));
		System.out.println("-----------------------------------------------");
//		RoleNobleHelper.isNoble(redisAdapter, roleInfoMapper, userId)
	}

//	@Value("${wechat.pay.api.unifiedorder}")
//	private String unifiedOrderUrl;// 统一下单API访问地址
//
//	@Value("${wechat.pay.appid}")
//	private String appId;
//
//	@Value("${wechat.pay.mchid}")
//	private String mchId;
//
//	@Value("${wechat.pay.notifyurl}")
//	private String notifyUrl;
//
//	@Value("${wechat.pay.apikey}")
//	private String apiKey;
//
//	private static final String GOODS_DESC = "爱阅直播-用户充值";
//
//	private static final String TRADE_TYPE = "APP";
//	@Test
//	public void test12() throws Exception {
//
//		Map<String, Object> params = new HashMap<>();
//		params.put("appid", appId);// 应用号
//		params.put("body", GOODS_DESC);// 商品描述
//		params.put("mch_id", mchId);// 商户号
//		params.put("nonce_str", WCPayUtils.getRandomNumber(16));// 16随机字符串(大小写字母加数字)
//
//		String outTradeNo = "001";
//
//		params.put("out_trade_no", outTradeNo);// 商户订单号
//		params.put("total_fee", 1);// 银行币种支付的钱钱啦
//		params.put("spbill_create_ip", "127.0.0.1");// IP地址
//		params.put("notify_url", notifyUrl); // 微信回调地址
//		params.put("trade_type", TRADE_TYPE);// 支付类型 APP
//		params.put("key", apiKey);
//		String paramXml = WCPayUtils.getXmlFromParamsMap(params);
//
//		HttpClientBuilder httpClientBuilder = HttpClientBuilder.create();
//		CloseableHttpClient closeableHttpClient = httpClientBuilder.build();
//		HttpPost httpPost = new HttpPost(unifiedOrderUrl);
//		HttpClientContext context = HttpClientContext.create();
//		StringEntity se = new StringEntity(new String(
//				paramXml.getBytes("utf-8"), "iso-8859-1"));
//		se.setContentType("text/xml");
//		se.setContentEncoding(new BasicHeader(HTTP.CONTENT_TYPE,
//				"application/xml"));
//		httpPost.setEntity(se);
//		httpPost.setConfig(RequestConfig.DEFAULT);
//		HttpResponse httpResponse = closeableHttpClient.execute(httpPost,
//				context);
//		HttpEntity httpEntity = httpResponse.getEntity();
//		InputStream content = httpEntity.getContent();
//		Map<String, String> ps = WCPayUtils.getParamsMapFromXml(content);
//		System.out.println(ps);
//	}
	
	public void test12() throws Exception {
		RoleInfo role = new RoleInfo();
		role.setGoodsId(184L);
		role.setUserId(1L);
		role.setEffectiveEndTime(DateUtils.addDays(new Date(), 30));
		roleInfoMapper.updateRoleInfoEffectiveEndTime(role);
		
	}

	public void test13() throws Exception {
		CouponInfo coupon = new CouponInfo();
		coupon.setGoodsId(182L);
		coupon.setUserId(716L);
		coupon.setRenewId(1L);
		System.out.println(couponInfoMapper.updateCouponByRenew(coupon));
	}

	public void schedule() {
		String receiverKey = RedisKey.ROCKET_RECEIVER;
		if (redisAdapter.existsKey(receiverKey)) {
			Set<String> ids = redisAdapter.setMembers(receiverKey);
			for (String id : ids) {
//				if (!redisAdapter.existsKey(RedisKey.ROCKET_GIVER_ + id)) {
//					String scoreKey = RedisKey.ROCKET_RECEIVER_SCORE_ + id;
//					redisAdapter.setRem(receiverKey, id);
//					redisAdapter.delKeys(scoreKey);
//				}
			}
		}

	}

	public void test14() throws Exception {
		for (long i = 1; i <= 10L; i++) {
			Map<String, Object> result = goodsInfoService.findNobles(i);
			System.out.println(result);
		}
	}

	public void test15() throws Exception {
		System.out.println(anchorService.getMsgList(2, 10, 1633365L));
	}

	public void test16() throws Exception {
		redisAdapter.strSetexByNormal("user_role_noble_1",
				(int) 16256785499L, "3");
	}

	public void test17() throws Exception {
		System.out.println(userService.getUserInfoByLoinId("2808072_cx", "1", "111"));
	}

	public void test18() throws Exception {
		System.out.println(liveVideoInfo2Service.getWatchingVideoNobles(1L));
	}

	//======================fileds==================================
	
    @Autowired
    private DaoFactoryService daoFactoryService;
    
//    GoodsInfoService goodsInfoService;
    
    //==========================================================
	public void test19() throws Exception {
		String chatrooms = "LIVE0007478";
		Map<String, Object> handlerMap = new HashMap<>();
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("m", "buyNobleMsg");
        paramMap.put("chatrooms", chatrooms);
        paramMap.put("userName", "小三");
        paramMap.put("nobleName", "小四");
        paramMap.put("anchorName", "小五");

        handlerMap.put("action", "noticeHandler");
        handlerMap.put("params", paramMap);

        String msgJSON = JSON.json(handlerMap);
        System.out.println(msgJSON);
        daoFactoryService.getPushRedisDao().rpush("pushMsg", msgJSON);
	}

	@Autowired
	private DiscountInfoMapper discountInfoMapper;
	
	public void test20() {
		System.out.println(discountInfoMapper.findDiscountInfosByGoodsId(182));
	}

	public void test21() {
//		System.out.println(couponInfoMapper.getAllDiscount(182L));
	}

	public void test22() {
		System.out.println(liveVideoInfo2Service.getWatchingVideoNobles(7384L));
	}

	public void test23() {
		System.out.println(userService.getUserInfoByLoinId("74_cx", "-1", "718"));
	}

	public void test24() throws ParseException {
		System.out.println(goodsInfoService.getShelfGoodsList("718"));
	}

	public void test25() throws ParseException {
		System.out.println(goodsInfoService.getShelfGoodsList("816").get("nobleList"));//1
		System.out.println(goodsInfoService.getShelfGoodsList("719").get("nobleList"));//2
		System.out.println(goodsInfoService.getShelfGoodsList("716").get("nobleList"));//3
		System.out.println(goodsInfoService.getShelfGoodsList("718").get("nobleList"));//4
		System.out.println(goodsInfoService.getShelfGoodsList("723").get("nobleList"));//5
		System.out.println(goodsInfoService.getShelfGoodsList("721").get("nobleList"));//6
		System.out.println(goodsInfoService.getShelfGoodsList("888").get("nobleList"));//0
	}

	public void test26() {
		System.out.println(redisAdapter.ttl("notexist"));
		System.out.println(redisAdapter.ttl("goods_info_1"));
	}
	

	public void test27() {
		System.out.println(goodsInfoMapper.getGoodsListByType(7));
	}
	
	public void test28() {
		getDiscountInfoByGoodsId(182L);
		// DiscountInfoMapper#getDiscountCouponAndDiamond
	}
	
	private List<DiscountInfo> getDiscountInfoByGoodsId(Long goodsId) {
		String str = redisAdapter.strGet(RedisKey.NobleKey.NOBILITY_DISCOUNT_
				+ goodsId);
		if (StringUtils.isNotBlank(str)) {
			 List<DiscountInfo> discountInfoList = JSONArray.parseArray(str, DiscountInfo.class);
			 System.out.println(discountInfoList + "----------------------------------");
			 return discountInfoList;
		} else {
			List<DiscountInfo> discountInfoList = discountInfoMapper
					.getAllDiscount(goodsId);
			// 加入缓存
			if (discountInfoList != null) {
				redisAdapter.strSetexByNormal(RedisKey.NobleKey.NOBILITY_DISCOUNT_
						+ goodsId, RedisExpireTime.EXPIRE_DAY_30,
						JSONObject.toJSONString(discountInfoList));
			}
			 System.out.println(discountInfoList + "----------------------------------");
			return discountInfoList;
		}
	}

	public void test29() throws ParseException {
		System.out.println("------------------------------------------------------------------------------------");
		try {
			List<JSONObject> list = null;
			//goodsInfoService.getNobleGoods("1");
//			System.out.println(JSONObject.toJSON(list).toString());
//			System.out.println(list.size());
//			list = goodsInfoService.getNobleGoods("2");
//			System.out.println(JSONObject.toJSON(list).toString());
//			System.out.println(list.size());
			list = goodsInfoService.getNobleGoods("1145");
			System.out.println(JSONObject.toJSON(list).toString());
			System.out.println(list.size());
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("------------------------------------------------------------------------------------");
	}

	public void test30() {
		Map<String, Object> map = goodsInfoService.findNobles(33L);
		System.out.println(JSONObject.toJSON(map));
	}

	public void test31() {
        Date updateTime = new Date();
		 //贵族礼物
        int count = goodsInfoMapper.countUpAndDownNobles(updateTime);
        System.out.println(count);
        if(count > 0) {
        	goodsInfoMapper.upAndDownNobles(updateTime);
            List<Map<String, Object>> invalidGoods = privilegeInfoMapper.getInvalidGoodsPrivilege();//不可用的礼物
            List<Map<String, Object>> validGoods = privilegeInfoMapper.getValidGoodsPrivilege();//可用的礼物
            List<Map<String, Object>> changeGoods = new ArrayList<Map<String, Object>>(); 
            if (CollectionUtils.isNotEmpty(invalidGoods)) {
            	privilegeInfoMapper.updateGoodsPrivilegeInvalid();
            	changeGoods.addAll(invalidGoods);
            }
            if (CollectionUtils.isNotEmpty(validGoods)) {
            	privilegeInfoMapper.updateGoodsPrivilegeValid();
            	changeGoods.addAll(validGoods);
            }
            if (CollectionUtils.isNotEmpty(changeGoods)) {
            	for (Map<String, Object> map : changeGoods) {
            		long receiverKey = (long) map.get("receiverKey");
            		int privilegeType = (int) map.get("privilegeType");
            		if (privilegeType == 1) {
            			redisAdapter.delKeys(RedisKey.NobleKey.NOBLE_JSON_ + receiverKey);
            		} else if (privilegeType == 3) {
//            			redisAdapter.delKeys(RedisKey.NobleKey.EXCLUSIVE_NOBLE_JSON_ + receiverKey);
            		}
            	}
            }
        }
	}

	public void test32() {
        Map<String, Object> in = anchorMapper.getAnchorIncomeVideoCnt(1633360L);
        System.out.println(in.get("income").getClass().getName());
	}

	public void test33() {
        DiscountPriceInfo dis =  nobilityService.getMyNobilityPrice("672", "183");
        System.out.println(JSONObject.toJSON(dis));
	}

	public void test34() {
	    List<Long> nobleGoodsIds = goodsMapper.findNobleGoodsId();
	    System.out.println(nobleGoodsIds);
	}

	public void test35() {
		CouponInfo couponInfo = couponInfoMapper.getCouponInfoByUidAndGid(5L, 182L);
	    System.out.println(couponInfo);
	    int diamond = 0;//钻石数
		int coupon = 0;//抵用券
		//DiscountType 1初次抵用券 2未使用抵用券返钻 3使用抵用券返钻 4再次返抵用券
		if (couponInfo == null) {//未有优惠
			List<DiscountInfo> discList = getDiscountCouponAndDiamondByGoodsId(182L, true);
			for (int i = 0; discList != null && i < discList.size(); i++) {
				DiscountInfo dis = discList.get(i);
				if (dis.getDiscountType() == 2) {
					diamond = new BigDecimal(dis.getDiscountPrice()).intValue();
				}
				if (dis.getDiscountType() == 1) {
					coupon = new BigDecimal(dis.getDiscountPrice()).intValue();
				}
			}
		}
		System.out.println(diamond + " " + coupon);
	}

	// TODO
	@Test
	public void test36() {
		System.out.println(JSONObject.toJSON(goodsInfoService.findNobles(781L)));
	}
	
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
}
