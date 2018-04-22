
import java.math.BigInteger;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.commons.lang.time.DateUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.alibaba.fastjson.JSONObject;
import com.chineseall.iwanvi.wwlive.common.cache.RedisClient;
import com.chineseall.iwanvi.wwlive.common.cache.RedisClientAdapter;
import com.chineseall.iwanvi.wwlive.common.cache.RedisExpireTime;
import com.chineseall.iwanvi.wwlive.common.cache.RedisKey;
import com.chineseall.iwanvi.wwlive.common.constants.Constants;
import com.chineseall.iwanvi.wwlive.common.external.rongcloud.RongCloudFacade;
import com.chineseall.iwanvi.wwlive.dao.base.mysql.MysqlSequenceGen;
import com.chineseall.iwanvi.wwlive.dao.wwlive.AnchorMapper;
import com.chineseall.iwanvi.wwlive.dao.wwlive.ContributionListMapper;
import com.chineseall.iwanvi.wwlive.dao.wwlive.GoodsInfoMapper;
import com.chineseall.iwanvi.wwlive.dao.wwlive.LiveAdminMapper;
import com.chineseall.iwanvi.wwlive.dao.wwlive.LiveVideoInfoMapper;
import com.chineseall.iwanvi.wwlive.dao.wwlive.ManagerUserInfoMapper;
import com.chineseall.iwanvi.wwlive.dao.wwlive.OrderInfoMapper;
import com.chineseall.iwanvi.wwlive.dao.wwlive.UserInfoMapper;
import com.chineseall.iwanvi.wwlive.domain.wwlive.Anchor;
import com.chineseall.iwanvi.wwlive.domain.wwlive.ContributionList;
import com.chineseall.iwanvi.wwlive.domain.wwlive.LiveVideoInfo;
import com.chineseall.iwanvi.wwlive.domain.wwlive.ManagerUserInfo;
import com.chineseall.iwanvi.wwlive.domain.wwlive.OrderInfo;
import com.chineseall.iwanvi.wwlive.domain.wwlive.UserInfo;
import com.chineseall.iwanvi.wwlive.web.common.enums.PayType;
import com.chineseall.iwanvi.wwlive.web.common.helper.AnchorInfoHelper;
import com.chineseall.iwanvi.wwlive.web.common.helper.LiveAdminHelper;
import com.chineseall.iwanvi.wwlive.web.common.pay.OutTradeNoUtil;
import com.chineseall.iwanvi.wwlive.web.common.pay.RSAConfig;
import com.chineseall.iwanvi.wwlive.web.common.pay.alipay.UserAliPay;
import com.chineseall.iwanvi.wwlive.web.video.service.UserInfoService;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:spring/application.xml" })
public class TestSpring {

	private final Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	MysqlSequenceGen mysqlSequenceGen;

	// @Autowired
	// Test1Mapper test1Mapper;

	@Autowired
	private RedisClient redisClient;

	@Autowired
	private ThreadPoolTaskExecutor threadPoolTaskExecutor;

	@Autowired
	RedisClientAdapter redisAdapter;

	@Autowired
	OrderInfoMapper orderInfoMapper;

	@Autowired
	UserInfoMapper userInfoMapper;

	@Autowired
	private LiveVideoInfoMapper liveVideoInfoMapper;

	@Autowired
	private AnchorMapper anchorMapper;

	@Autowired
	private GoodsInfoMapper goodsInfoMapper;

	@Autowired
	private ContributionListMapper contributionListMapper;

	@Autowired
	private ManagerUserInfoMapper managerMapper;

	@Autowired
	private UserInfoService userInfoService;

	@Autowired
	private UserAliPay userAliPay;

	@Autowired
	private LiveAdminMapper liveAdminMapper;

	/**
	 * 支付宝通知接口
	 */
	@Value("${alipay.recharge.notify.url}")
	private String notifyUrl;

	public void test1() {
		System.out.println(mysqlSequenceGen.getNextval("test"));
	}

	public void test2() {
		System.out.println(Arrays.toString(mysqlSequenceGen.getNextvals("test",
				10)));
	}

	public void test3() {
		// PageHelper.startPage(0, 5);
		// List<Test1> ts = test1Mapper.findTest1List();
		// System.out.println(ts);
		HashMap<String, Object> value = new HashMap<String, Object>();
		value.put("test1", 12);
		redisClient.setEx("test1", value, 12000);

	}

	public void test4() {
		// Test1 test1 = new Test1();
		// test1.setContent("something");
		// test1Mapper.insertTest(test1);
		// System.out.println(test1);

	}

	public void test5() {
		MDC.put("MODEL", "jy");
		logger.info("------------------------------123------------------------------");
		@SuppressWarnings("unchecked")
		Map<String, String> contextMap = (Map<String, String>) MDC
				.getCopyOfContextMap();
		System.out.println(JSONObject.toJSONString(contextMap));
	}

	public void test6() throws InterruptedException {
		MDC.put("MODEL", "jy");
		logger.info("------------------------------123------------------------------");
		threadPoolTaskExecutor.execute(new Runnable() {

			@Override
			public void run() {
				System.out
						.println("-----------------------------------------------------------");
				System.out.println(Arrays.toString(mysqlSequenceGen
						.getNextvals("test", 10)));
				System.out
						.println("-----------------------------------------------------------");

			}
		});
		Thread.sleep(10000);//
	}

	public void test7() {
		// System.out.println(redisAdapter.existsKey("test1"));
		// System.out.println(redisAdapter.strIncr("testincr"));
		System.out.println(redisAdapter.strIncrBy("testincr", 10));
	}

	public void test8() {
		// System.out.println(redisAdapter.existsKey("test1"));
		// System.out.println(redisAdapter.strIncr("testincr"));
		// System.out.println(redisAdapter.zsetAdd("test1zset", 0, "123"));
		// System.out.println(redisAdapter.zsetIncrBy("test1zset", 2, "123"));
		// System.out.println(redisAdapter.zsetRange("test1zset", 0, -1));
		System.out.println(redisAdapter.zsetIncrBy("test1zset", -1, "123"));
		System.out.println(redisAdapter.zsetScore("test1zset", "123"));
	}

	public void test9() {
		OrderInfo order = orderInfoMapper.getOrderInfoById(1);
		System.out.println(order);
	}

	public void test10() {
		// List<Map<String, Object>> list = liveVideoInfoMapper.getRankList(33);
		// System.out.println(Arrays.toString(list.toArray()));
	}

	// userInfoMapper

	public void test11() {
		UserInfo userInfo = userInfoMapper.findById(1);
		System.out.println(userInfo);
	}

	public void test12() {
		// System.out.println(liveVideoInfoMapper.countVideos(33));
		System.out
				.println("--------------------------------------------------------------");
		System.out.println(liveVideoInfoMapper.findVideoByPC(11, 22));
		System.out
				.println("--------------------------------------------------------------");
	}

	public void test13() {
		System.out.println(redisAdapter.hashGetAll("123"));
	}

	public void test14() {
		System.out.println(Integer.MAX_VALUE);
		// System.out.println(anchorMapper.updateAnchorLogOnTime(1633352));
	}

	public void test15() {
		// System.out.println(anchorMapper.getAnchorByLogin("1234", "000"));
		UserInfo userInfo = userInfoMapper.findById(12);

		redisAdapter.hashMSet(RedisKey.USER_INFO_ + 12,
				userInfo.putFieldValueToStringMap());
		redisAdapter.expireKey(RedisKey.USER_INFO_ + 12,
				RedisExpireTime.EXPIRE_DAY_5);
	}

	public void test16() throws ParseException {
		// System.out.println(anchorMapper.getAnchorByLogin("1234", "000"));
		String key = RedisKey.USER_INFO_ + 12;
		Map<String, String> userInfo = redisAdapter.hashGetAll(key);
		UserInfo user = new UserInfo();
		user.doStringMapToValue(userInfo);
		System.out.println(user.toString());
	}

	public void test17() throws ParseException {
		// System.out.println(goodsInfoMapper.findGoodsInfoById(1));
	}

	public void test18() throws ParseException {
		OrderInfo orderInfo = new OrderInfo();
		orderInfo.setOutTradeNo("out123");
		// orderInfo.setUserId(12);
		// orderInfo.setOriginKey(10);
		// orderInfo.setOrderId(11);
		// orderInfo.setReceiverKey(10);
		// orderInfo.setGoodsId(1);
		orderInfo.setGoodsName("面条");
		orderInfo.setGoodsImg("dldl");
		orderInfo.setGoodsNum(12);
		orderInfo.setTotalGoodsPrice(100);
		orderInfo.setDiscount(100);
		orderInfo.setPayType(3);
		orderInfo.setOrderStatus(0);
		orderInfo.setOrderYearMonth(DateFormatUtils.format(new Date(),
				"yyyy-MM"));
		orderInfo.setAmt(100L);
		orderInfo.setIncome(0d);

		orderInfoMapper.insertOrderInfo(orderInfo);
	}

	public void test19() throws ParseException {

		ContributionList con = new ContributionList();
		// con.setAnchorId(10);
		// con.setUserId(12);
		con.setGoodsNum(12);
		con.setOriginalAmt(100);
		con.setTotalAmt(100d);
		// con.setGoodsId(1);
		contributionListMapper.insertContribution(con);
	}

	public void test20() throws ParseException {
		OrderInfo orderInfo = new OrderInfo();
		orderInfo.setOrderStatus(Constants._4);
		orderInfo.setReceiveNo("8888");
		// orderInfo.setOrderId(1418);
		orderInfoMapper.updateStatus(orderInfo);
		// contributionListMapper.updateTotalAmt(12, 12, 10, 12);

	}

	public void test21() throws ParseException {
		System.out.println(orderInfoMapper.getOrderInfoByOutNo("out123"));

	}

	public void test22() throws ParseException {
		ManagerUserInfo manager = managerMapper.getMagagerByLogin("admin");
		System.out.println(manager.getPasswd());
	}

	public void test23() throws ParseException {
		String livingKey = RedisKey.VideoGrayKeys.LIVING_VIDEOS_ + "160927";
		redisAdapter.zsetIncrBy(livingKey, -1, 187 + "");
	}

	public void test24() throws ParseException {
		String livingKey = RedisKey.VideoGrayKeys.LIVING_VIDEOS_ + "160928";
		System.out.println(redisAdapter.zsetScore(livingKey, "194"));
		// redisAdapter.zsetIncrBy(livingKey, -1, 187 + "");
	}

	// redisAdapter

	public void test25() throws ParseException {
		String livingKey = RedisKey.VideoGrayKeys.LIVING_VIDEOS_ + "161014";
		System.out.println(redisAdapter.zsetScore(livingKey, "112"));
		// redisAdapter.zsetIncrBy(livingKey, -1, 187 + "");
	}

	public void test26() throws ParseException {
		LiveVideoInfo info = liveVideoInfoMapper.findVideoInfoById(378);
		System.out.println(info.getUserName());
	}

	public void test27() throws Exception {
		System.out.println("---------------------------------------");
		String token = RongCloudFacade.getToken(1633489 + "", "主播", "", 0);
		System.out.println(token);
		token = RongCloudFacade.getToken(1633488 + "", "00000032", "", 0);
		System.out.println(token);
		token = RongCloudFacade.getToken(1633487 + "", "00000031", "", 0);
		System.out.println(token);
		System.out.println("---------------------------------------");
	}

	public void test28() {
		UserInfo userInfo = null;
		try {
			userInfo = userInfoService.checkIsExist("61_cx");
			System.out.println(userInfo.getUserName());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void test29() {
		String key = "aaa";
		redisAdapter.strSet(key, 123);
		String strId = redisAdapter.strGet(key);
		System.out.println(strId);
		redisAdapter.strSet(key, 0);
		strId = redisAdapter.strGet(key);
		System.out.println(strId.equals("0"));
		// RongCloudFacade.getToken(storedAnchor.getAnchorId() + "",
		// storedAnchor.getUserName(), "", 0, Constants.FORMAT_JSON);
	}

	public void test30() {
		try {
			System.out
					.println(RongCloudFacade
							.getToken(
									"1633508",
									"子荨",
									"http://imgstest.ikanshu.cn/images-wwlive/anchor/default_male_head.png",
									0));
		} catch (Exception e) {
			e.printStackTrace();
		}
		//
	}

	public void test31() {
		try {
			System.out.println(redisAdapter
					.setMembers("living_video_viewres_1299"));
		} catch (Exception e) {
			e.printStackTrace();
		}
		//
	}

	public void test32() {

	}

	@Autowired
	private OutTradeNoUtil outTradeNoUtil;

	public void test33() {
		try {

			System.out
					.println("----------------------------------------------------------------------------------------------");
			System.out
					.println("----------------------------------------------------------------------------------------------");
			System.out
					.println("----------------------------------------------------------------------------------------------");
			String ot1 = "dd3";
			JSONObject json = userAliPay.crateBizContent("高洋小肥羊", ot1, "0.01");
			Map<String, String> keyValues = userAliPay.buildOrderParamMap(json,
					ot1, notifyUrl);
			String orderParam = userAliPay.buildOrderParam(keyValues);
			String sign = userAliPay.getSign(keyValues,
					RSAConfig.IWANVI_PRIVATE_RSA);

			System.out.println(orderParam);
			System.out.println(sign);

			System.out
					.println("----------------------------------------------------------------------------------------------");
			String ot = outTradeNoUtil.getTradeNo(PayType.CZPAY);
			json = userAliPay.crateBizContent("万维直播充值", ot, "0.01");
			keyValues = userAliPay.buildOrderParamMap(json, ot, notifyUrl);
			orderParam = userAliPay.buildOrderParam(keyValues);
			sign = userAliPay.getSign(keyValues, RSAConfig.IWANVI_PRIVATE_RSA);

			System.out.println(orderParam);
			System.out.println(sign);
			System.out
					.println("----------------------------------------------------------------------------------------------");
			System.out
					.println("----------------------------------------------------------------------------------------------");
			System.out
					.println("----------------------------------------------------------------------------------------------");
		} catch (Exception e) {
			e.printStackTrace();
		}
		//
	}

	public void test34() {
		try {
			System.out.println(outTradeNoUtil.getTradeNo(PayType.ZSPAY));
		} catch (Exception e) {
			e.printStackTrace();
		}
		//
	}

	public void test35() {
		try {
			System.out
					.println("----------------------------------------------------------------------------------------------------------------------------------");
			System.out.println(RongCloudFacade.joinChatroom("2808028_cx",
					"LIVE0001526", Constants.FORMAT_JSON));
			System.out
					.println("----------------------------------------------------------------------------------------------------------------------------------");
		} catch (Exception e) {
			e.printStackTrace();
		}
		//
	}

	public void test36() {
		try {
			System.out
					.println("----------------------------------------------------------------------------------------------------------------------------------");
			System.out.println(RongCloudFacade.getToken("11154_cx",
					"M10011213", "", 1));
			System.out
					.println("----------------------------------------------------------------------------------------------------------------------------------");
		} catch (Exception e) {
			e.printStackTrace();
		}
		//
	}

	public void test37() {
		try {
			System.out.println(redisAdapter.hashGet("abc", "fk"));
		} catch (Exception e) {
			e.printStackTrace();
		}
		//
	}

	public void test38() {
		try {
			System.out.println(RongCloudFacade.getToken("2810582_cx", "", "",
					0));
			System.out.println(RongCloudFacade.getToken("2810592_cx", "", "",
					0));
		} catch (Exception e) {
			e.printStackTrace();
		}
		//
	}

	public void test39() {
		try {
			System.out.println(LiveAdminHelper.isAdmin(redisAdapter,
					liveAdminMapper, 1633508L, 112L));
		} catch (Exception e) {
			e.printStackTrace();
		}
		//
	}

	public void test41() {
		try {
			System.out.println(RongCloudFacade.getToken("62", "于佳溪", "", 0));
		} catch (Exception e) {
			e.printStackTrace();
		}
		//
	}

	public void test42() {
		try {
			System.out.println(redisAdapter.zsetRevrangeByScore(
					"lived_videos_2017-04-14", "(69330423295", "-1", 0, 2));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// @Test
	public void test43() {
		try {
			LiveVideoInfo video = liveVideoInfoMapper.findVideoInfoById(321);
			redisAdapter.hashMSet(RedisKey.LIVE_VIDEO_INFO_ + 321,
					video.putFieldValueToMap());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Autowired
	com.chineseall.iwanvi.wwlive.web.video.service.impl.ADInfoServiceImpl adInfoServiceImpl;

	public void test44() {
		try {
			System.out.println(adInfoServiceImpl
					.getVideoInfoByAnchorId(1633367L));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void test45() {
		try {
			String key = RedisKey.VideoGrayKeys.LIVING_VIDEOS_;
			System.out.println(redisAdapter.zsetScore(key, "250"));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void test48() {
		try {
			LiveVideoInfo video = new LiveVideoInfo();
			video.setVideoName("伊尔库斯克");
			redisAdapter.hashMSet(RedisKey.LIVE_VIDEO_INFO_ + 321,
					video.putFieldValueToMapNotNull());
			video.setFormatType(0);
			redisAdapter.hashMSet(RedisKey.LIVE_VIDEO_INFO_ + 321,
					video.putFieldValueToStringMap());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void test49() {
		try {
			// String key = RedisKey.ANCHOR_INFO_ + "1633362";
			Anchor anchor = AnchorInfoHelper.getAndCacheAnchorInfo(
					redisAdapter, anchorMapper, 1633362L);
			System.out.println(anchor);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void test50() {
		try {
			Map<String, String> map = redisAdapter
					.hashGetAll("anchor_info_1633362");
			String birthdayStr = map.get("birthday");
			//验证下生日是什么
			System.out.println(DateFormatUtils.format(
					DateUtils.parseDate(birthdayStr, new String[] {
							"yyyy-MM-dd HH:mm:ss", "yyyy-MM-dd" }),
					"yyyy-MM-dd"));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void test51() {
		try {
			LiveVideoInfo video = liveVideoInfoMapper.findVideoInfoById(123L);
			System.out.println(video.getVideoId());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void test52() {
		try {

			String today = DateFormatUtils.format(new Date(),
					Constants.YY_MM_DD);
			String tmpKey = RedisKey.VideoGrayKeys.LIVED_VIDEOS_ + today;
			Double score = redisAdapter.zsetScore(tmpKey, "101");
			System.out.println(score);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// TODO
	// TODO
	@Test
	public void test53() {
		try {
			Map<String, Object> videoInfo = liveVideoInfoMapper
					.getLivingByAnchorId(1633519L);

			BigInteger videoId = (BigInteger) videoInfo.get("videoId");
			System.out.println(videoId);
			System.out.println(videoInfo.get("videoId").getClass().getName());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
