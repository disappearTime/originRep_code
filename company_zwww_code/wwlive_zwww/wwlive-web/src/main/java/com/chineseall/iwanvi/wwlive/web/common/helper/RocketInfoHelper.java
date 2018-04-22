package com.chineseall.iwanvi.wwlive.web.common.helper;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.time.DateFormatUtils;

import com.alibaba.dubbo.common.utils.StringUtils;
import com.alibaba.fastjson.JSONObject;
import com.chineseall.iwanvi.wwlive.common.cache.RedisClientAdapter;
import com.chineseall.iwanvi.wwlive.common.cache.RedisKey;
import com.chineseall.iwanvi.wwlive.common.constants.Constants;

/**
 * 火箭礼物信息
 * @author DIKEPU
 * @since 2017-08-30 version 7th 2.4.0
 */
public class RocketInfoHelper {
	
	public static Map<String, Object> homeIndexNobleRecommend(String anchorId, RedisClientAdapter redisAdapter) {
		String rocketKey = RedisKey.ROCKET_GIVER_LIST_ + anchorId + Constants.UNDERLINE 
				+ DateFormatUtils.format(new Date(), Constants.YY_MM_DD);//当日主播所有视频有效
		Map<String, Object> map = new HashMap<String, Object>();
		if(redisAdapter.existsKey(rocketKey)) {
			List<String> list = redisAdapter.listRange(rocketKey, 0, -1);
			if (CollectionUtils.isEmpty(list)) {
				return map;
			}
			long currentTime = new Date().getTime();
			for (String str : list) {
				RocketGiver giver = JSONObject.parseObject(str, RocketGiver.class);
				if (giver.getExpireTime() > currentTime) {
					map.put("nobleCode", giver.getNobleCode());
					String userKey = RedisKey.USER_INFO_ + giver.getUserId();
					String userName = redisAdapter.hashGet(userKey, "userName");
					if (!StringUtils.isBlank(userName)) {
						map.put("userName", userName);
					} else {
						map.put("userName", giver.getUserName());
					}
					return map;
				} else {//移除
					redisAdapter.listRem(rocketKey, str);
				}
			}
		} 
		return map;
	}
	
}

/**
 * 火箭赠送者
 * @author DIKEPU
 *
 */
class RocketGiver {
	private long userId;
	
	private long expireTime;

	private int nobleCode;
	
	private String userName;
	
	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public long getUserId() {
		return userId;
	}

	public void setUserId(long userId) {
		this.userId = userId;
	}

	public long getExpireTime() {
		return expireTime;
	}

	public void setExpireTime(long expireTime) {
		this.expireTime = expireTime;
	}

	public int getNobleCode() {
		return nobleCode;
	}

	public void setNobleCode(int nobleCode) {
		this.nobleCode = nobleCode;
	}
	
}