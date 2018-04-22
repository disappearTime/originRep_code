package test;

import java.util.HashMap;
import java.util.Map;








import org.apache.commons.lang.StringUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.chineseall.iwanvi.wwlive.common.cache.RedisClientAdapter;
import com.chineseall.iwanvi.wwlive.common.cache.RedisExpireTime;
import com.chineseall.iwanvi.wwlive.common.cache.RedisKey;
import com.chineseall.iwanvi.wwlive.dao.wwlive.AcctInfoMapper;
import com.chineseall.iwanvi.wwlive.dao.wwlive.LiveVideoInfoMapper;
import com.chineseall.iwanvi.wwlive.dao.wwlive.RechargeInfoMapper;
import com.chineseall.iwanvi.wwlive.domain.wwlive.RechargeInfo;
import com.chineseall.iwanvi.wwlive.domain.wwlive.UpgradeInfo;
import com.chineseall.iwanvi.wwlive.web.common.util.StringTools;
import com.chineseall.iwanvi.wwlive.web.video.service.UserInfoService;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:spring/application.xml" })
public class TestSpring230 {

	@Autowired
	RedisClientAdapter redisAdapter;

	@Autowired
	LiveVideoInfoMapper liveVideoInfoMapper;

	@Autowired
	private RechargeInfoMapper rechargeInfoMapper;

	@Value("${redenvelope.event.url}")
	private String redEnvelopeURL;

	@Autowired
	private UserInfoService userService;

	public void test1() {
		System.out.println(redisAdapter.listRpop("abc"));
	}

	public void test2() {
		System.out.println(liveVideoInfoMapper.findLivingChatroomIds());
	}

	public void test3() {
		RechargeInfo info = rechargeInfoMapper
				.getRechargeInfoByOutNo("re000000007865");
		System.out.println(info);
	}

	public void test4() {
		Map<String, String> paramsMap = new HashMap<String, String>();
		paramsMap.put("videoId", "123");
		paramsMap.put("anchorId", "122");
		paramsMap.put("anchorName", "sdfsd");
		final String url = this.redEnvelopeURL;
		String tmpURL = StringTools.replace(paramsMap, url);// 请求地址
		paramsMap.put("videoId", "dd");
		paramsMap.put("anchorId", "ss");
		paramsMap.put("anchorName", "dd");
		tmpURL = StringTools.replace(paramsMap, url);// 请求地址
		System.out.println(tmpURL);
	}

	public void test5() {
		System.out.println(userService.getUserInfoByLoinId("974_zb", "1", ""));

	}

	public void test6() {
		try {
			String version = "3.0.21";
			String cnid = "1062";
			String osCode = "24";// 系统版本号
			String packname = "com.mianfeia.book";
			String app = "";
			System.out
					.println("------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------");
			System.out.println("version: " + version + ",cnid: " + cnid
					+ ",osCode: " + osCode + ",packname: " + packname
					+ ",app: " + app);
			System.out
					.println("------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------");
			int isUpdate = 0;
			// 是否强制更新
			String newestVersion = null;

			if (StringUtils.isNotBlank(osCode)
					&& Integer.valueOf(osCode) >= 24) {
				newestVersion = redisAdapter
						.hashGet(
								RedisKey.independentUpdownKeys.INDEPENDENT_CURRENT_VERSION,
								"version");
           	 	app = "dl";
				System.out.println(getAndroidVersionData(cnid, app, isUpdate,
						version, newestVersion));
			}
			return;
			/*if ("dl".equals(app)) {
				newestVersion = redisAdapter
						.hashGet(
								RedisKey.independentUpdownKeys.INDEPENDENT_CURRENT_VERSION,
								"version");
			} else {
				newestVersion = redisAdapter.hashGet(RedisKey.UPGRADE_INFO,
						"version");
				if (redisAdapter.setIsMember(RedisKey.FORCE_UPGRADE_VERSIONS,
						version)) {
					isUpdate = 2;
				}
			}

			if (StringUtils.isBlank(newestVersion)) {
				newestVersion = redisAdapter.strGet(RedisKey.VERSION_KEY);
			}

			if (newestVersion != null
					&& redisAdapter.existsKey(RedisKey.DOWNLOAD_KEY)
					&& redisAdapter.existsKey(RedisKey.MD5_KEY)) {
				System.out.println(getAndroidVersionData(cnid, app, isUpdate,
						version, newestVersion));
			}*/
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		// return JSONObject.toJSON(getAndroidVersionMsg(0, 1, "")).toString();
		System.out.println(addAnderVersionData(getAndroidVersionMsg(0, 1, "")));

	}

	private Map<String, Object> getAndroidVersionData(String cnid, String app,
			int isUpdate, String version, String newestVersion)
			throws Exception {
		if (StringUtils.isEmpty(version)) {
			return addAnderVersionData(new AndroidVersion());
		}
		if (version.equals("-1")) {
			return addAnderVersionData(getAndroidVersionMsgFromRedis(0,
					isUpdate, "", cnid, app));
		}
		if (compareVersion(version, newestVersion) < 0) {
			return addAnderVersionData(getAndroidVersionMsgFromRedis(0,
					isUpdate, "", cnid, app));
		}
		return addAnderVersionData(getAndroidVersionMsgFromRedis(0, 1, "",
				cnid, app));

	}

	/**
	 * 获得安卓客户端当前版本信息
	 * 
	 * @param code
	 * @param isupdate
	 * @param errorMsg
	 * @return
	 */
	private AndroidVersion getAndroidVersionMsg(int code, int isupdate,
			String errorMsg) {
		// getAndroidVersionMsgFromRedis(0, 0, "");
		AndroidVersion msg = new AndroidVersion();
		msg.setCode(code);
		msg.setIsupdate(isupdate);
		msg.setErrorMsg(errorMsg);
		msg.setUrl("123");
		msg.setVersion("ts");
		msg.setMd5("md5");
		return msg;
	}

	private AndroidVersion getAndroidVersionMsgFromRedis(int code,
			int isupdate, String errorMsg, String cnid, String app) {
		AndroidVersion msg = new AndroidVersion();
		msg.setCode(code);
		msg.setIsupdate(isupdate);
		msg.setErrorMsg(errorMsg);
		if ("dl".equals(app)) {
			if (redisAdapter
					.existsKey(RedisKey.independentUpdownKeys.INDEPENDENT_CURRENT_VERSION)) {
				String objQudao = redisAdapter.strGet("dlqudao");
				if (StringUtils.isNotBlank(objQudao)) {// "1062"
					if (objQudao.contains(cnid) || objQudao.equals("0")) {// 0代表所有用户都下载
						Map<String, String> upgradeInfo = redisAdapter
								.hashGetAll(RedisKey.independentUpdownKeys.INDEPENDENT_CURRENT_VERSION);
						if (upgradeInfo != null && !upgradeInfo.isEmpty()) {
							UpgradeInfo info = UpgradeInfo.fromMap(upgradeInfo);
							msg.setUrl(info.getUrl());
							msg.setVersion(info.getVersion());
							msg.setMd5(info.getMd5());
							msg.setUpdateMsg(info.getUpdateMsg());
							msg.setApkSize(info.getApkSize());
							return msg;
						}
					}
				}
			}
			msg.setIsupdate(1);
		} else {
			if (redisAdapter.existsKey(RedisKey.UPGRADE_INFO)) {
				String objQudao = redisAdapter.strGet("qudao");
				if (StringUtils.isNotBlank(objQudao)) {// "1062"
					if (objQudao.equals(cnid) || objQudao.equals("0")) {// 0代表所有用户都下载
						Map<String, String> upgradeInfo = redisAdapter
								.hashGetAll(RedisKey.UPGRADE_INFO);
						if (upgradeInfo != null && !upgradeInfo.isEmpty()) {
							UpgradeInfo info = UpgradeInfo.fromMap(upgradeInfo);
							msg.setUrl(info.getUrl());
							msg.setVersion(info.getVersion());
							msg.setMd5(info.getMd5());
							msg.setUpdateMsg(info.getUpdateMsg());
							msg.setApkSize(info.getApkSize());
							return msg;
						}
					}
					msg.setIsupdate(1);
				}
			}
			msg.setUrl(StringUtils.isBlank(redisAdapter
					.strGet(RedisKey.DOWNLOAD_KEY)) ? "" : redisAdapter.strGet(
					RedisKey.DOWNLOAD_KEY).replaceAll("\"", ""));

			msg.setVersion(StringUtils.isBlank(redisAdapter
					.strGet(RedisKey.VERSION_KEY)) ? "" : redisAdapter.strGet(
					RedisKey.VERSION_KEY).replaceAll("\"", ""));

			msg.setMd5(StringUtils.isBlank(redisAdapter
					.strGet(RedisKey.MD5_KEY)) ? "" : redisAdapter.strGet(
					RedisKey.MD5_KEY).replaceAll("\"", ""));
			msg.setApkSize(1);

			msg.setUpdateMsg("");
		}
		return msg;
	}


	/**
	 * 安卓校验返回参数
	 * 
	 * @author DIKEPU
	 *
	 */
	private class AndroidVersion {
		private int code = 1;
		private String errorMsg = "";
		private String version = "";
		private int isupdate = 1;
		private String url = "";
		private String md5 = "";
		private String updateMsg = "";
		private long apkSize = 16172701L;

		public int getCode() {
			return code;
		}

		public void setCode(int code) {
			this.code = code;
		}

		public String getErrorMsg() {
			return errorMsg;
		}

		public void setErrorMsg(String errorMsg) {
			this.errorMsg = errorMsg;
		}

		public String getVersion() {
			return version;
		}

		public void setVersion(String version) {
			this.version = version;
		}

		public int getIsupdate() {
			return isupdate;
		}

		public void setIsupdate(int isupdate) {
			this.isupdate = isupdate;
		}

		public String getUrl() {
			return url;
		}

		public void setUrl(String url) {
			this.url = url;
		}

		public String getMd5() {
			return md5;
		}

		public void setMd5(String md5) {
			this.md5 = md5;
		}

		public String getUpdateMsg() {
			return updateMsg;
		}

		public void setUpdateMsg(String updateMsg) {
			this.updateMsg = updateMsg;
		}

		public long getApkSize() {
			return apkSize;
		}

		public void setApkSize(long apkSize) {
			this.apkSize = apkSize;
		}

	}

	public Map<String, Object> addAnderVersionData(AndroidVersion android) {
		Map<String, Object> result = new HashMap<>();
		result.put("updateMsg", android.getUpdateMsg());
		result.put("md5", android.getMd5());
		result.put("errorMsg", android.getErrorMsg());
		result.put("code", android.getCode());
		result.put("url", android.getUrl());
		result.put("apkSize", android.getApkSize());
		result.put("version", android.getVersion());
		result.put("isupdate", android.getIsupdate());
		return result;
	}

	// 比较版本号的大小,前者大则返回一个正数,后者大返回一个负数,相等则返回0
	public static int compareVersion(String version1, String version2)
			throws Exception {
		if (version1 == null || version2 == null) {
			throw new Exception("compareVersion error:illegal params.");
		}
		String[] versionArray1 = version1.split("\\.");// 注意此处为正则匹配，不能用"."；
		String[] versionArray2 = version2.split("\\.");
		int idx = 0;
		int minLength = Math.min(versionArray1.length, versionArray2.length);// 取最小长度值
		int diff = 0;
		while (idx < minLength
				&& (diff = versionArray1[idx].length()
						- versionArray2[idx].length()) == 0// 先比较长度
				&& (diff = versionArray1[idx].compareTo(versionArray2[idx])) == 0) {// 再比较字符
			++idx;
		}
		// 如果已经分出大小，则直接返回，如果未分出大小，则再比较位数，有子版本的为大；
		diff = (diff != 0) ? diff : versionArray1.length - versionArray2.length;
		return diff;
	}

	@Autowired
	private AcctInfoMapper acctInfoMapper;

	public void test7() {
		Map<String, Object> map = acctInfoMapper.getAcctInfoCoin(112L);
		System.out.println(map);
		System.out.println(map.get("coin").getClass());
		System.out.println(map.get("coin").getClass().getName());
		System.out.println(map.get("coin").getClass().getCanonicalName());
		map = acctInfoMapper.getAcctInfoCoin(673L);
		System.out.println(map);
		System.out.println(map.get("coin").getClass());
		System.out.println(map.get("coin").getClass().getName());
		System.out.println(map.get("coin").getClass().getCanonicalName());
		map = acctInfoMapper.getAcctInfoCoin(149L);
		System.out.println(map);
		System.out.println(map.get("coin").getClass());
		System.out.println(map.get("coin").getClass().getName());
		System.out.println(map.get("coin").getClass().getCanonicalName());
	}

	public void test8() {
		String coinKey = "gogo1";
		Map<String, Object> acctMap = new HashMap<String, Object>();
		acctMap.put("coin", 500);
		System.out.println(redisAdapter.hashMSet(coinKey, acctMap));
		redisAdapter.expireKey(coinKey, RedisExpireTime.EXPIRE_DAY_1);
	}

	// TODO
	// TODO
	@Test
	public void test9() {
		String coinKey = "gogo1";
		long surplus = redisAdapter.hashIncrBy(coinKey, "coin", -100);//剩余
		System.out.println(surplus);
	}
	
}
