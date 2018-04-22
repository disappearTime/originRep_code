package com.chineseall.iwanvi.wwlive.web.video.service.impl;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.chineseall.iwanvi.wwlive.common.cache.RedisClientAdapter;
import com.chineseall.iwanvi.wwlive.common.cache.RedisExpireTime;
import com.chineseall.iwanvi.wwlive.common.cache.RedisKey;
import com.chineseall.iwanvi.wwlive.common.constants.Constants;
import com.chineseall.iwanvi.wwlive.common.external.rongcloud.domain.SdkHttpResult;
import com.chineseall.iwanvi.wwlive.common.tools.HttpUtils;
import com.chineseall.iwanvi.wwlive.dao.wwlive.UserInfoMapper;
import com.chineseall.iwanvi.wwlive.domain.wwlive.UserInfo;
import com.chineseall.iwanvi.wwlive.web.common.helper.UserInfoHelper;
import com.chineseall.iwanvi.wwlive.web.common.util.RegexUtils;
import com.chineseall.iwanvi.wwlive.web.common.util.StringTools;
import com.chineseall.iwanvi.wwlive.web.video.service.UserInfo2Service;

@Service
public class UserInfoService2Impl implements UserInfo2Service {

	static final Logger LOGGER = Logger.getLogger(UserInfoService2Impl.class);

	@Autowired
	RedisClientAdapter redisAdapter;

	@Autowired
	private UserInfoMapper userInfoMapper;

	@Autowired
	private ThreadPoolTaskExecutor taskExecutor;

	@Value("${user.cx.task}")
	String taskUrl;

	@Value("${user.cx.modify.name}")
	String modifyName;

	@Override
	public boolean isInBlackList(int videoId, int userId) {
		String key = RedisKey.BLACKLIST_ + videoId + Constants.UNDERLINE
				+ userId;
		return redisAdapter.existsKey(key);
	}

	/**
	 * 获得用户昵称完成创新版任务
	 */
	@Override
	public Map<String, String> getNameAndCompleteTask(Long userId) {

		// 获得用户信息
		Map<String, String> userInfo = getUserInfo(userId);

		// 去完成任务
		String loginId = userInfo.get("loginId");
		String origin = userInfo.get("origin");
		toCompleteTask(loginId, origin, userId);

		// 用户每天只有一次修改机会
		if (!isNeedModifyName(userId)) {
			return new HashMap<String, String>();
		}
		return userInfo;
	}

	/**
	 * 是否需要修改昵称
	 * 
	 * @param userId
	 * @return
	 */
	private boolean isNeedModifyName(Long userId) {
		String modifyNameKey = RedisKey.CX_MODIFY_NAME_
				+ DateFormatUtils.format(new Date(), Constants.YY_MM_DD)
				+ Constants.UNDERLINE + userId;
		if (redisAdapter.existsKey(modifyNameKey)) {
			return false;
		} else {
			// 修改标志
			redisAdapter.strSet(modifyNameKey, userId);
			redisAdapter.expireKey(modifyNameKey, RedisExpireTime.EXPIRE_DAY_1);
			return true;
		}
	}

	/**
	 * 用户昵称，用户的登录id，来源
	 * 
	 * @param userId
	 * @return
	 */
	private Map<String, String> getUserInfo(Long userId) {
		Map<String, String> userInfo = redisAdapter.hashMGet(
				RedisKey.USER_INFO_ + userId, "userName", "loginId", "origin");

		if (userInfo == null || userInfo.isEmpty()
				|| StringUtils.isBlank(userInfo.get("userName"))
				|| StringUtils.isBlank(userInfo.get("loginId"))) {

			UserInfo user = UserInfoHelper.getAndCacheUserInfo(redisAdapter,
					userInfoMapper, userId);
			if (user == null) {
				return new HashMap<String, String>();
			}
			userInfo = new HashMap<String, String>();
			userInfo.put("userName", user.getUserName());
			userInfo.put("loginId", user.getLoginId());
			userInfo.put("origin", user.getOrigin() + "");
		}
		return userInfo;
	}

	/**
	 * 去完成任务
	 * 
	 * @param loginId
	 * @param origin
	 * @param userId
	 */
	private void toCompleteTask(String loginId, String origin, final Long userId) {
		final String completeKey = RedisKey.CX_TASK_COMPLETE_
				+ DateFormatUtils.format(new Date(), Constants.YY_MM_DD)
				+ Constants.UNDERLINE + userId;
		if (!redisAdapter.existsKey(completeKey)) {
			if (StringUtils.isBlank(origin) || !RegexUtils.isNum(origin)) {
				return;
			}
			if (StringUtils.isNotBlank(loginId)
					&& Constants.USER_INFO_ORIGIN_0 == Integer.parseInt(origin)) {
				final String uid = loginId.replace("_cx", "");
				final String uri = taskUrl + uid;
				taskExecutor.execute(new Runnable() {

					@Override
					public void run() {
						completeTask(uri, userId, completeKey);
					}
				});
			}
		}
	}

	private void completeTask(final String uri, final Long userId, final String completeKey) {
		try {

			HttpURLConnection conn = HttpUtils.createGetHttpConnection(uri,
					Constants.UTF8);
			SdkHttpResult shr = HttpUtils.returnResult(conn);
			if (shr.getHttpCode() == 200) {
				JSONObject json = JSONObject.parseObject(shr.getResult());
				Object o = json.get("code");
				if (o instanceof Integer) {
					Integer code = (Integer) o;
					if (code == 0) {// 0完成 1 未完成
						LOGGER.info("完成任务，userId：" + userId);
						redisAdapter.strSet(completeKey, userId);
						redisAdapter.expireKey(completeKey, RedisExpireTime.EXPIRE_DAY_1);
					}
				}
			}

		} catch (IOException e) {
			LOGGER.error("生成任务URL失败", e);
		} catch (Exception e) {
			LOGGER.error("完成任务失败", e);
		}
	}

	@Override
	public Map<String, Object> modifyName(Long userId, String userName) {

		Map<String, String> userMap = getUserInfo(userId);
		String loginId = userMap.get("loginId");
		String origin = userMap.get("origin");

		Map<String, Object> result = new HashMap<String, Object>();// 返回结果
																	// code：0成功
																	// 1昵称重复
																	// 2用户问题
		if (StringUtils.isBlank(origin) 
				|| !RegexUtils.isNum(origin) 
				|| StringUtils.isBlank(loginId)) {
			result.put("code", 2);
			return result;
			
		}
		if ((Constants.USER_INFO_ORIGIN_0 + "").equals(origin)) {//独立版用户不需要同步昵称了
			loginId = loginId.replace("_cx", "");
			String uri = this.modifyName;
			Map<String, String> paramsMap = new HashMap<String, String>();
			paramsMap.put("uid", loginId);
			paramsMap.put("nickname", userName);
			uri = StringTools.replace(paramsMap, uri);
			Integer code = modifyNameByCx(uri, userId, userName);
			result.put("code", code > 0 ? 0 : 1);
		} else if ((Constants.USER_INFO_ORIGIN_2 + "").equals(origin)) {
			updateUserName(userId, userName);
		}
		return result;

	}

	private Integer modifyNameByCx(String uri, Long userId, String userName) {

		try {
			HttpURLConnection conn = HttpUtils.createGetHttpConnection(uri,
					Constants.UTF8);
			SdkHttpResult shr = HttpUtils.returnResult(conn);
			if (shr.getHttpCode() == 200) {
				JSONObject json = JSONObject.parseObject(shr.getResult());
				Object o = json.get("code");
				if (o instanceof Integer) {
					Integer code = (Integer) o;
					if (code == 0) {// 1 失败 0成功
						int cnt = updateUserName(userId, userName);
						return cnt;
					}
				}
			}

		} catch (IOException e) {
			LOGGER.error("生成修改昵称URL失败", e);
		} catch (Exception e) {
			LOGGER.error("修改昵称失败", e);
		}
		return 0;
	}

	private int updateUserName(Long userId, String userName) {
		UserInfo userInfo = new UserInfo();
		userInfo.setUserId(userId);
		userInfo.setUserName(userName);
		int cnt = userInfoMapper.updateUserInfoById(userInfo);
		String userKey = RedisKey.USER_INFO_ + userId;
		if (cnt > 0 && redisAdapter.existsKey(userKey)) {
			Map<String, Object> params = new HashMap<String, Object>();
			params.put("userName", userName);
			redisAdapter.hashMSet(userKey, params);
		}
		return cnt;
	}
	
}
