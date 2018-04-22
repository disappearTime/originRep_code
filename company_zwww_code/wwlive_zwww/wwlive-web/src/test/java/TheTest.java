import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.junit.Test;
import org.springframework.util.ReflectionUtils.FieldFilter;

import redis.clients.jedis.Jedis;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.alibaba.fastjson.serializer.ValueFilter;
import com.chineseall.iwanvi.wwlive.common.check.ResponseResult;
import com.chineseall.iwanvi.wwlive.common.check.ResultMsg;
import com.chineseall.iwanvi.wwlive.common.constants.Constants;
import com.chineseall.iwanvi.wwlive.common.constants.ExternalSDKConfig;
import com.chineseall.iwanvi.wwlive.common.enums.GoodsEnum;
import com.chineseall.iwanvi.wwlive.common.external.kscloud.KSCloudFacade;
import com.chineseall.iwanvi.wwlive.common.external.rongcloud.RongCloudFacade;
import com.chineseall.iwanvi.wwlive.common.external.rongcloud.domain.SdkHttpResult;
import com.chineseall.iwanvi.wwlive.common.external.rongcloud.domain.TxtMessage;
import com.chineseall.iwanvi.wwlive.common.tools.Base64Tools;
import com.chineseall.iwanvi.wwlive.common.tools.HttpUtils;
import com.chineseall.iwanvi.wwlive.common.tools.StrMD5;
import com.chineseall.iwanvi.wwlive.domain.util.DateUtil;
import com.chineseall.iwanvi.wwlive.domain.wwlive.CouponInfo;
import com.chineseall.iwanvi.wwlive.domain.wwlive.RechargeInfo;
import com.chineseall.iwanvi.wwlive.domain.wwlive.UserInfo;
import com.chineseall.iwanvi.wwlive.web.common.enums.Origin;
import com.chineseall.iwanvi.wwlive.web.common.util.DateTools;
import com.chineseall.iwanvi.wwlive.web.common.util.RegexUtils;
import com.chineseall.iwanvi.wwlive.web.video.controller.DownLoadController;

public class TheTest {

	public void test1() {
		System.out.println(ExternalSDKConfig.getString("kscloud.accesskey"));
	}

	public void test2() {
		String presetUrl = KSCloudFacade.getPresetList("live");
		System.out.println(presetUrl);
		System.out.println(KSCloudFacade.getPresets(presetUrl));

	}

	public void test3() {
		System.out.println(KSCloudFacade.buildRtmp("live00001",
				"preset=iwanvi_live_demo&public=0&vdoid=12345"));
	}

	public void test4() {
		long expire = System.currentTimeMillis() / 1000 + 12000;// 200分钟
		System.out.println(KSCloudFacade.buildRtmp("LIVE" + expire, expire,
				"preset=iwanvi_live_demo&public=0&vdoid=12345"));
		String resource = "public=0&vdoid=12345";
		expire = System.currentTimeMillis() / 1000 + 12000;// 200分钟
		System.out.println(KSCloudFacade.buildRtmp("LIVE" + expire, expire,
				resource));

	}

	public void test5() {
		String str = KSCloudFacade.addBlack("live", "LIVE1471587052");
		System.out.println(str);

		SdkHttpResult result = null;
		try {
			HttpURLConnection conn = HttpUtils.createPostHttpConnection(str);
			result = HttpUtils.returnResult(conn);
			System.out.println(result);
		} catch (Exception e) {
		}
	}

	public void test6() {
		String str = KSCloudFacade.delBlack("live", "LIVE1471587052");
		System.out.println(str);

		SdkHttpResult result = null;
		try {
			HttpURLConnection conn = HttpUtils.createPostHttpConnection(str);
			result = HttpUtils.returnResult(conn);
			System.out.println(result);
		} catch (Exception e) {
		}
	}

	public void test7() {
		Jedis jedis = new Jedis("192.168.1.239", 7384);
		System.out.println(jedis.ping());
		jedis.select(1);
		jedis.close();
	}

	public void test8() {//
		System.out.println(TestEnum.Expire_10.getVal());
	}

	public void test9() {
		System.out.println(3600L * 24L * 30L);
		System.out.println(new Integer(0x76a700));
	}

	public void test10() throws Exception {
		File oldFile = new File("C:/Users/Thinkpad/Desktop/tx.txt");
		System.out.println(oldFile.getName());
		FileInputStream fis = new FileInputStream(oldFile);
		FileOutputStream fos = new FileOutputStream(new File(
				"C:/Users/Thinkpad/Desktop/tt.txt"));
		byte[] inByte = new byte[512];
		while (fis.read(inByte) != -1) {
			fos.write(inByte);
		}// \001
		fis.close();
		fos.close();
	}

	public void test11() {
		System.out.println('\001');

	}

	public void test12() {
		System.out
				.println("MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCjSGHJJMAWjeY0d6IEWYu/tujPOOZ8pWfBlCRKBF8V2cVBK6Xx8R8dYtMdAL7itb+Z9VJJlY3EChCJFsFNH+uuW0vFTCS+1ujkltZ4MmaUlcSVhSkF6pTo6Al+hzvN1b2UX5CjKjkqsCr53UuHaEUQPNxZ1K0opq/xTz9xY3n2LQIDAQAB"
						.length());
	}

	public void test13() {
		System.out.println(Origin.FROMCX.toString());
	}

	public void test14() {
		int a = 13;
		test(a);
		System.out.println(a);
	}

	public void test15() {
		System.out.println("1254338301201610241127098090491".length());
	}

	// @org.junit.Test
	public void test16() {
		Date date = new Date(1478343035953L);
		System.out.println(new SimpleDateFormat("yyyy-MM-dd hh:mm:ss")
				.format(date));
	}

	public void test17() {
		System.out.println(Boolean.TRUE.toString().equals("true"));
	}

	public void test18() {
		ResponseResult<Long> result = new ResponseResult<>(ResultMsg.SUCCESS);
		result.setData(321L);
		System.out.println(JSONObject.toJSONString(result,
				SerializerFeature.WriteMapNullValue));
	}

	public void test19() {
		int i = 3;
		System.out.println(i &= 1);
	}

	public void test20() {
		System.out.println(Integer.MAX_VALUE);
	}

	public void test21() {
		System.out.println(12.9D % 1);
	}

	public void test22() {
		// System.out.println(DateFormatUtils.format(new Date(),
		// "yyyy-mm-dd HH:mm:ss.ff"));
		System.out.println(new SimpleDateFormat("yyyy-mm-dd HH:mm:ss.S")
				.format(new Date()));
	}

	public void test23() {
		System.out.println(StrMD5.getInstance().getStringMD5(
				"16334891kzUwKwMz357889df99624e9cb39bfec7c6055985"));
	}

	public void test24() {
		System.out.println(StrMD5.getInstance().getStringMD5(
				"148CuZyKhrpe5e2b6b4d0b04040b83e49223d8011aa"));
	}

	public void test25() {
		System.out
				.println(RegexUtils
						.isMatche(
								"as",
								"^[a-zA-Z]{1}([a-zA-Z0-9_]){2,7}|[\u4E00-\uFA29]{1}([a-zA-Z0-9_\u4E00-\uFA29]){2,7}$"));
	}

	public void test26() {
		System.out.println(new BigDecimal(10).divide(new BigDecimal(3), // 支付宝以元为单位
				BigDecimal.ROUND_HALF_UP).toString());

	}

	public void test27() {
		// System.out.println((Date) null);
		UserInfo user = new UserInfo();
		System.out.println(user);
	}

	public void test28() {
		System.out.println(new BigDecimal(20.0).divide(new BigDecimal(100))
				.setScale(2, BigDecimal.ROUND_HALF_UP));
	}

	public void test29() {
		// JSONObject json = new JSONObject();
		UserInfo user = new UserInfo();
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("ser", new Integer(1));
		user.setUserId(123l);
		user.setBirthday(new Date());
		System.out.println(JSONObject.toJSONString(map));
		System.out
				.println(JSONObject.toJSONString(user, new TestValueFilter()));
	}

	public void test30() {// cz0000000000719
		System.out.println("2.0.0".compareTo("1.0.0"));
		System.out.println("2.0.0".compareTo("1.1.0"));
		System.out.println("2.0.0".compareTo("2.0.0"));
		System.out.println("2.0.0".compareTo("2.1.0"));
	}

	public void test31() {// Y3owkDAwMDAwMDAwNzE5
		String tmp = Base64Tools.encode("cz0000000000719".getBytes());
		if (tmp.length() > 5) {
			String secret = tmp.substring(0, 4);
			secret += "k";
			secret += tmp.substring(5);
			System.out.println(secret);
		}

	}

	public void test32() {
		try {
			System.out.println(new DecimalFormat("###0.00")
					.format((double) new Date().getTime()));
		} catch (Exception e) {
			e.printStackTrace();
		}
		//
	}

	public void test33() {
		try {
			long now = new Date().getTime();
			long stay = now - (new Date(1422720000705L)).getTime();
			System.out.println(now);
			System.out.println(stay);
			System.out.println(new Date(stay));
			Calendar cal = Calendar.getInstance();
			cal.set(Calendar.YEAR, 2000);
			cal.set(Calendar.MONTH, 1);
			cal.set(2015, 1, 1, 0, 0, 0);
			System.out.println(cal.getTime());
			System.out.println(cal.getTime().getTime());
			System.out.println(new Date(1492000000000L));
			// 1492048150060
			System.out.println(new Date(1492048000000L));
			System.out.println(536457617998D);
		} catch (Exception e) {
			e.printStackTrace();
		}
		//
	}

	public void test34() {
		try {
			List<String> chatIds = new ArrayList<String>();
			chatIds.add("LIVE0004602");
			TxtMessage tx = new TxtMessage("");
			JSONObject json = new JSONObject();
			json.put("dataType", new Integer(16));
			json.put("dataValue", "");
			JSONObject dataExtra = new JSONObject();
			List<JSONObject> robotList = new ArrayList<JSONObject>();
			JSONObject nickName = new JSONObject();
			nickName.put("rongId", "64_cx");
			nickName.put("nickName", "我是大王");
			nickName.put("acctType", "1");
			nickName.put("userType", "1");
			nickName.put("isCustomer", "1");
			nickName.put("userId", "121521");
			nickName.put("msg", "你送的礼物好棒！");
			JSONObject acctType = new JSONObject();
			acctType.put("rongId", "64_cx");
			acctType.put("acctType", "我是大王");
			acctType.put("acctType", "1");
			acctType.put("userType", "1");
			acctType.put("isCustomer", "1");
			acctType.put("userId", "121521");
			robotList.add(nickName);
			robotList.add(acctType);
			dataExtra.put("robotList", robotList);
			json.put("dataExtra", dataExtra);
			tx.setExtra(json.toJSONString());
			SdkHttpResult sdk = RongCloudFacade.publishChatroomMessage(
					"1633433", chatIds, tx, Constants.FORMAT_JSON);
			System.out.println(sdk);
		} catch (Exception e) {
			e.printStackTrace();
		}
		//
	}

	public void test35() {
		try {
			System.out
					.println(DateTools.getAWeekAgoDate(Calendar.getInstance()));
		} catch (Exception e) {
			e.printStackTrace();
		}
		//
	}

	public void test36() {
		try {
			noticeUserMuted("LIVE0005821", 1633433L, 112L, "咯哈我一下", 180, 5691L);
		} catch (Exception e) {
			e.printStackTrace();
		}
		//
	}

	public void test37() {
		System.out.println((int) 1.0);

	}

	public void test38() {
		System.out.println(DateUtil.getNextDay(new Date(), 3));
	}

	public void test39() {
		long expire = 16256785499L;
		if (expire > Integer.MAX_VALUE) {
			expire = Integer.MAX_VALUE;
		}
		System.out.println((int) expire);
	}

	public void test40() {
		Character ch = new Character((char) 233);
		System.out.println(ch);
	}

	public void test41() {
		// System.out.println(0L == 0);
		System.out.println("1".compareTo("2"));
		System.out.println("9".compareTo("10"));
		System.out.println("9".compareTo("11"));
	}

	public void test42() {
		System.out.println(10000 % 1000);
	}

	public void test43() {
	}

	public void test44() throws Exception {

		String token = RongCloudFacade.getToken("910_zb",
				StringUtils.isBlank("") ? "" : "狮子座", "", 1);
		System.out.println(token);
	}

	public void test45() {
		String str = "";
		for (int i = 0; i < 3001; i++) {
			str += (i + ",");
		}
		System.out.println(str);
	}

	public void test46() throws Exception {

	}

	public void test47() throws Exception {
		System.out.println("-------------------------------------");
		String login_id = "1057_zb";
		String result = RongCloudFacade.getToken(login_id, "", "", 1);

		System.out.println(login_id);
		System.out.println(result);
	}

	public void test48() throws Exception {
		String osCode = "24";
		int oscode = 0;
		System.out.println("-----------------------------------" + osCode);
		System.out.println((StringUtils.isNotBlank(osCode) && (oscode = Integer
				.valueOf(osCode)) >= 24));
		System.out.println(oscode);
	}

	public void test49() throws Exception {
		// StrMD5.getInstance().getMD5Str(key, charset)
		System.out.println(StrMD5.getInstance().getStringMD5(
				"1080lrkchxunA5C33A7574284BF2A7153FA15CBFEF131633445"));
	}

	public void test50() throws Exception {
		System.out.println(GoodsEnum.ROCKET.getSpecial().equals("rocket"));
	}

	public void test51() {
		// int index = "abc/cc?dk".indexOf("/");
		// System.out.println("abc/cc?dk".substring(index));

		System.out.println(new BigDecimal(11).multiply(new BigDecimal(8))
				.divide(new BigDecimal(100), 2, BigDecimal.ROUND_HALF_UP)
				.doubleValue());

	}

	public void test52() throws ParseException {
		// DateUtils.parseDate
		System.out.println(org.apache.commons.lang3.time.DateUtils.parseDate(
				"2017-01-01 12:00:01", Locale.US, new String[] {
						"EEE MMM dd HH:mm:ss zzz yyyy", "yyyy-MM-dd HH:mm:ss",
						"yyyy-MM-dd" }));
	}

	public static String format(Date date, String pattern, Locale locale) {
		if (date == null || pattern == null) {
			return null;
		}
		return new SimpleDateFormat(pattern, locale).format(date);
	}

	private void noticeUserMuted(String chatRoomId, Long fromUserId,
			Long userId, String userName, Integer duration, Long videoId) {
		try {
			List<String> chatIds = new ArrayList<String>();
			chatIds.add(chatRoomId);
			TxtMessage tx = new TxtMessage("");
			JSONObject json = new JSONObject();
			json.put("dataType", "2");
			json.put("dataValue", "系统消息：" + userName + "已被管理员禁言！");
			json.put("dataExtra", "{'userId':'" + userId + "', 'gagTime':'"
					+ duration + "', 'videoId':'" + videoId + "'}");// userId,
																	// 禁言时长s,
																	// videoId
			System.out.println(json.toJSONString());
			tx.setExtra(json.toJSONString());
			RongCloudFacade.publishChatroomMessage(fromUserId.toString(),
					chatIds, tx, Constants.FORMAT_JSON);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// System.out.println(DateTools.getAWeekAgoDate(Calendar.getInstance()));
	//
	private void test(int a) {
		a = 1;
	}

	public static enum TestEnum {
		Expire_10(10), Expire_20(20), Expire_30(30);
		private int val;

		public int getVal() {
			return val;
		}

		public void setVal(int val) {
			this.val = val;
		}

		private TestEnum(int val) {
			this.val = val;
		}

	}

	public void test53() throws ParseException {
		System.out.println(JSONArray.parse(""));
	}

	public void test54() throws Exception {
		List<String> list1 = null;
		List<String> list2 = new ArrayList<String>();
		list2.addAll(list1);
		System.out.println(list2);
	}

	public void test55() throws Exception {
	}

	public void test56() throws Exception {
		long level = 52;
		long currentLv = 51;
		noticeAllFinishMilepost(level, currentLv);
		System.out.println("---------------1----------");
		level = 55;
		currentLv = 51;

		noticeAllFinishMilepost(level, currentLv);
		System.out.println("----------------2---------");
		level = 53;
		currentLv = 52;

		noticeAllFinishMilepost(level, currentLv);
		System.out.println("-----------------3--------");
		level = 52;
		currentLv = 49;

		noticeAllFinishMilepost(level, currentLv);
		System.out.println("-----------------4--------");
	}

	int[] levels = new int[] { 52, 88, 233, 520, 666, 888, 999, 2500, 4000,
			5000 };

	private void noticeAllFinishMilepost(long level, long currentLv) {
		int tmpL = -1;
		List<Integer> insertLevel = new ArrayList<Integer>();
		for (int lv : levels) {
			if (level > lv) {
				tmpL = lv;
				if (lv >= currentLv) {// 大于当前说明是特殊关卡 需要插入的数据内
					insertLevel.add(new Integer(lv));
				}
				continue;
			} else {
				break;
			}
		}
		if (currentLv > tmpL) {
			return;
		}
		System.out.println(this.getClass().getName()
				+ "-------------355 ---------------" + insertLevel);

	}

	public void test57() throws Exception {
		RechargeInfo recharge = new RechargeInfo();
		recharge.setRechargeId(4348377L);
		CouponInfo couponInfo = new CouponInfo();
		couponInfo.setCouponId(1006L);
		System.out
				.println((couponInfo != null
						&& couponInfo.getCouponId() != null && (recharge != null && recharge
						.getRechargeId() != null)));
	}

	//

	public void test58() throws Exception {
		String s = "[{\"nobleName\":\"圣骑专属\",\"goodsId\":71,\"goodsGif\":\"http://ks3-cn-beijing.ksyun.com/wwlive/imgs/goods/liuxingyu.gif\",\"goodsImg\":\"http://ks3-cn-beijing.ksyun.com/wwlive/imgs/goods/b6495e96474640849d4975027f5ca6b5_144.png\",\"isUse\":0,\"goodsPrice\":222.0,\"special\":\"meteor\",\"type\":1,\"goodsName\":\"流星雨\",\"goodsType\":3,\"orderRow\":6}, {\"nobleName\":\"黑骑专属\",\"goodsId\":78,\"goodsGif\":\"http://ks3-cn-beijing.ksyun.com/wwlive/imgs/goods/huojian.gif\",\"goodsImg\":\"http://ks3-cn-beijing.ksyun.com/wwlive/imgs/goods/5e08b13bf5744b2d9894c6c5cd5d6b70_144.png\",\"isUse\":1,\"goodsPrice\":666.0,\"special\":\"rocket\",\"type\":1,\"goodsName\":\"火箭\",\"goodsType\":3,\"orderRow\":1}, {\"nobleName\":\"龙骑专属\",\"goodsId\":68,\"goodsGif\":\"http://ks3-cn-beijing.ksyun.com/wwlive/imgs/goods/xiannvbang.gif\",\"goodsImg\":\"http://ks3-cn-beijing.ksyun.com/wwlive/imgs/goods/fa28d6bc4dc345c9830869a73cdcf0bd_144.png\",\"isUse\":0,\"goodsPrice\":10.0,\"special\":\"\",\"type\":1,\"goodsName\":\"仙女棒\",\"goodsType\":3,\"orderRow\":7}, {\"nobleName\":\"龙骑专属\",\"goodsId\":75,\"goodsGif\":\"http://ks3-cn-beijing.ksyun.com/wwlive/imgs/goods/yigui.gif\",\"goodsImg\":\"http://ks3-cn-beijing.ksyun.com/wwlive/imgs/goods/465185aebfa94e4cb3b2411dc503a26a_144.png\",\"isUse\":0,\"goodsPrice\":520.0,\"special\":\"wardrobe\",\"type\":1,\"goodsName\":\"梦幻衣柜\",\"goodsType\":3,\"orderRow\":8}, {\"nobleName\":\"圣骑专属\",\"goodsId\":67,\"goodsGif\":\"http://ks3-cn-beijing.ksyun.com/wwlive/imgs/goods/shilitaohua.gif\",\"goodsImg\":\"http://ks3-cn-beijing.ksyun.com/wwlive/imgs/goods/0eebefb7fc27431183442e9f72a04a13_144.png\",\"isUse\":1,\"goodsPrice\":6.0,\"special\":\"\",\"type\":1,\"goodsName\":\"十里桃林\",\"goodsType\":3,\"orderRow\":5}, {\"nobleName\":\"黑骑专属\",\"goodsId\":69,\"goodsGif\":\"http://ks3-cn-beijing.ksyun.com/wwlive/imgs/goods/gaobaiqiqiu.gif\",\"goodsImg\":\"http://ks3-cn-beijing.ksyun.com/wwlive/imgs/goods/2e695aecd29a4c7aaa52fc9a29fe2178_144.png\",\"isUse\":1,\"goodsPrice\":21.0,\"special\":\"\",\"type\":1,\"goodsName\":\"告白气球\",\"goodsType\":3,\"orderRow\":9}, {\"nobleName\":\"黑骑专属\",\"goodsId\":70,\"goodsGif\":\"http://ks3-cn-beijing.ksyun.com/wwlive/imgs/goods/richu.gif\",\"goodsImg\":\"http://ks3-cn-beijing.ksyun.com/wwlive/imgs/goods/70ed4297414045cf85c92acb87870c76_144.png\",\"isUse\":1,\"goodsPrice\":1314.0,\"special\":\"sunrise\",\"type\":1,\"goodsName\":\"陪你看日出\",\"goodsType\":3,\"orderRow\":10}, {\"nobleName\":\"魔法专属\",\"goodsId\":64,\"goodsGif\":\"http://ks3-cn-beijing.ksyun.com/wwlive/imgs/goods/shouhuzhijian.gif\",\"goodsImg\":\"http://ks3-cn-beijing.ksyun.com/wwlive/imgs/goods/858d715b5f7149e189ca3981580ed9ca_144.png\",\"isUse\":1,\"goodsPrice\":52.0,\"special\":\"sword\",\"type\":1,\"goodsName\":\"守护之剑\",\"goodsType\":3,\"orderRow\":11}, {\"nobleName\":\"魔法专属\",\"goodsId\":72,\"goodsGif\":\"http://ks3-cn-beijing.ksyun.com/wwlive/imgs/goods/xinhuanufang.gif\",\"goodsImg\":\"http://ks3-cn-beijing.ksyun.com/wwlive/imgs/goods/09fbe74e390a4f9d8496c8fba8899fb6_144.png\",\"isUse\":1,\"goodsPrice\":2888.0,\"special\":\"beWildWthJoy\",\"type\":1,\"goodsName\":\"心花怒放\",\"goodsType\":3,\"orderRow\":12}, {\"nobleName\":\"魔法专属\",\"goodsId\":66,\"goodsGif\":\"http://ks3-cn-beijing.ksyun.com/wwlive/imgs/goods/caidan.gif\",\"goodsImg\":\"http://ks3-cn-beijing.ksyun.com/wwlive/imgs/goods/ae205d7dd5e0482397e05511cecd8e1e_144.png\",\"isUse\":1,\"goodsPrice\":3344.0,\"special\":\"eggs\",\"type\":1,\"goodsName\":\"兔兔彩蛋\",\"goodsType\":3,\"orderRow\":13}, {\"nobleName\":\"紫荆专属\",\"goodsId\":63,\"goodsGif\":\"http://ks3-cn-beijing.ksyun.com/wwlive/imgs/goods/motianlun.gif\",\"goodsImg\":\"http://ks3-cn-beijing.ksyun.com/wwlive/imgs/goods/9986f0c7d7804e2aa9a4427af0f60ae6_144.png\",\"isUse\":1,\"goodsPrice\":99.0,\"special\":\"ferrisWheel\",\"type\":1,\"goodsName\":\"摩天轮\",\"goodsType\":3,\"orderRow\":14}, {\"nobleName\":\"紫荆专属\",\"goodsId\":74,\"goodsGif\":\"http://ks3-cn-beijing.ksyun.com/wwlive/imgs/goods/queqiaoxianghui.gif\",\"goodsImg\":\"http://ks3-cn-beijing.ksyun.com/wwlive/imgs/goods/d3cf53225a664a059910116f2b62535a_144.png\",\"isUse\":1,\"goodsPrice\":8888.0,\"special\":\"magpieBridge\",\"type\":1,\"goodsName\":\"鹊桥相会\",\"goodsType\":3,\"orderRow\":15}, {\"nobleName\":\"紫荆专属\",\"goodsId\":73,\"goodsGif\":\"http://ks3-cn-beijing.ksyun.com/wwlive/imgs/goods/fenhonghai.gif\",\"goodsImg\":\"http://ks3-cn-beijing.ksyun.com/wwlive/imgs/goods/10ee9ffbbe2746d795c31286df68c012_144.png\",\"isUse\":1,\"goodsPrice\":12014.0,\"special\":\"pinkSea\",\"type\":1,\"goodsName\":\"许你一片粉红海\",\"goodsType\":3,\"orderRow\":16}, {\"nobleName\":\"神殿专属\",\"goodsId\":76,\"goodsGif\":\"http://ks3-cn-beijing.ksyun.com/wwlive/imgs/goods/tianshi.gif\",\"goodsImg\":\"http://ks3-cn-beijing.ksyun.com/wwlive/imgs/goods/a060e3fcd3e2476daf80c2951dd5a4b4_144.png\",\"isUse\":1,\"goodsPrice\":980.0,\"special\":\"angel\",\"type\":1,\"goodsName\":\"天使\",\"goodsType\":3,\"orderRow\":17}, {\"nobleName\":\"神殿专属\",\"goodsId\":65,\"goodsGif\":\"http://ks3-cn-beijing.ksyun.com/wwlive/imgs/goods/shendianfazhang.gif\",\"goodsImg\":\"http://ks3-cn-beijing.ksyun.com/wwlive/imgs/goods/11eb94c26e0c44249c848330f76ace3a_144.png\",\"isUse\":1,\"goodsPrice\":66666.0,\"special\":\"staff\",\"type\":1,\"goodsName\":\"神殿法杖\",\"goodsType\":3,\"orderRow\":18}, {\"nobleName\":\"神殿专属\",\"goodsId\":77,\"goodsGif\":\"http://ks3-cn-beijing.ksyun.com/wwlive/imgs/goods/huanqiu.gif\",\"goodsImg\":\"https://ks3-cn-beijing.ksyun.com/wwlive/imgs/goods/5dbf32cbed934c36bb3480efa2a58a4c_144.png\",\"isUse\":1,\"goodsPrice\":131420.0,\"special\":\"world\",\"type\":1,\"goodsName\":\"环游世界\",\"goodsType\":5,\"orderRow\":28}]";
		List<JSONObject> nobleList = new ArrayList<>();
		nobleList = JSONObject.parseArray(s, JSONObject.class);
		if (nobleList != null && !nobleList.isEmpty()) {
			Collections.sort(nobleList, new Comparator<JSONObject>() {
				@Override
				public int compare(JSONObject o1, JSONObject o2) {
					if (!"rocket".equals(o1.getString("special"))) {
						return 10;
					} else if ("rocket".equals(o1.getString("special"))) {
						return -10;
					}
					int isUse1 = (o1.getInteger("isUse") == null ? 1 : o1
							.getInteger("isUse"));// 0可用
					int orderRow1 = (o1.getInteger("orderRow") == null ? 99999
							: o1.getInteger("orderRow"));// 默认99999 越靠前越小
					int isUse2 = (o2.getInteger("isUse") == null ? 1 : o2
							.getInteger("isUse"));
					int orderRow2 = (o2.getInteger("orderRow") == null ? 99999
							: o2.getInteger("orderRow"));
					if (isUse1 == isUse2) {
						return (orderRow1 - orderRow2);
					}
					return (isUse1 - isUse2);
				}

			});
		}
		System.out.println(this.getClass().getName() + " 336 " + nobleList);
	}

	public void test59() {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		System.out.println(sdf.format(getStartTime()));
	}

	public void test60() {
		System.out.println(StrMD5.getInstance().getStringMD5(
				"801193_cxZDPYBZLS5af1ca22832646239f927ec87a18d556329"));	
	}

	public void test61() throws Exception {
		System.out.println(DownLoadController.compareVersion("3.1.0", "3.0.2"));
	}
	
	public void test62() throws Exception {
        JSONObject fun = new JSONObject();
        fun.put("fun", "nobleDue");
        JSONObject data = new JSONObject();
        data.put("userId", "124");
        data.put("nobleIndex","3");
        data.put("uid","1307");
        fun.put("data", data);
		System.out.println(fun.toString());
	}

	public void test63() {
        String response = "{\"code\":0}";
        JSONObject json = JSONObject.parseObject(response);
        System.out.println(json.toJSONString());
	}
	public void test64() {
		RechargeInfo re = new RechargeInfo();
		re.setRechargeStatus(new Integer(1));
		System.out.println((re.getRechargeStatus() == 0));
	}

	public void test65() throws Exception {//下载聊天室的内容
		// 创建聊天室
		SdkHttpResult result = null;

		// 聊天室发送消息
		List<String> chatIds = new ArrayList<String>();
		chatIds.add("000000009266");//秘宝宝
		chatIds.add("000000009268");//兔子

		// //获得聊天室内容
		result = RongCloudFacade.queryChatroom(chatIds,
		Constants.FORMAT_JSON);
		System.out.println("queryChatroom=" + result);
		System.out.println(DateFormatUtils.format(
				DateUtils.addHours(new Date(), -14), "yyyyMMddHH"));
		result = RongCloudFacade.getMsgHistoryUrl(DateFormatUtils.format(
				DateUtils.addHours(new Date(), -14), "yyyyMMddHH"),
				Constants.FORMAT_JSON);
		System.out.println("getMsgHistoryUrl=" + result);

	}

	// TODO
	// TODO
	@Test
	public void test66() throws Exception {
		double ds = 13.0;
		System.out.println(ds / 10.0);
		BigDecimal d = new BigDecimal(ds).divide(new BigDecimal(10), 1, BigDecimal.ROUND_HALF_UP);
		System.out.println(d.doubleValue());
	}
	
	private static Date getStartTime() {
		Calendar todayStart = Calendar.getInstance();
		todayStart.set(Calendar.HOUR_OF_DAY, 0);
		todayStart.set(Calendar.MINUTE, 0);
		todayStart.set(Calendar.SECOND, 0);
		todayStart.set(Calendar.MILLISECOND, 0);
		return todayStart.getTime();
	}

}

class TestFieldFilter implements FieldFilter {

	@Override
	public boolean matches(Field field) {

		return false;
	}

}

class TestValueFilter implements ValueFilter {

	@Override
	public Object process(Object object, String name, Object value) {
		if (value == null) {
			return null;
		}
		if (value instanceof BigDecimal) {
			// 格式化BigDecimal ，去除小数点后面的0
			value = (BigDecimal) value;
			return String.valueOf(value);
		}
		if (value instanceof Double) {
			return String.valueOf(value);
		}
		if (value instanceof Integer) {
			return String.valueOf(value);
		}
		if (value instanceof Long) {
			try {
				return String.valueOf(value);
			} catch (Exception e) {
				return value;
			}
		}
		return value;
	}

}