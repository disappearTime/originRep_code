package com.chineseall.iwanvi.wwlive.web.common.helper;

import java.util.HashMap;
import java.util.Map;

import com.chineseall.iwanvi.wwlive.common.constants.Constants;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.chineseall.iwanvi.wwlive.common.cache.RedisClientAdapter;
import com.chineseall.iwanvi.wwlive.common.cache.RedisExpireTime;
import com.chineseall.iwanvi.wwlive.common.cache.RedisKey;
import com.chineseall.iwanvi.wwlive.dao.wwlive.ContributionListMapper;
import com.chineseall.iwanvi.wwlive.web.common.util.DateTools;

public class UserRankHelper {

	static final Logger LOGGER = Logger.getLogger(UserRankHelper.class);

    /**
     * 获得用户排名和积分，查询数据库获得并缓存
     * @param redisAdapter
     * @param contribMapper
     * @param userId
     * @return
     */
	public static Map<String, Object> getAndCacheUserRank(RedisClientAdapter redisAdapter, 
			ContributionListMapper contribMapper, Long userId) {
		if (redisAdapter == null || contribMapper == null || userId == null
				|| userId.longValue() < 0) {
			Map<String, Object> userInfo = new HashMap<String, Object>();
			userInfo.put("contrib", new Long(0));
			userInfo.put("rank", new Long(0));
			return userInfo;
		}
		long contrib = contribMapper.getContribAll(userId);
		long rank = 0L;
		if(contrib!=0L){
			rank = contribMapper.getRankAll(contrib);
		}
		Map<String, Object> userInfo = new HashMap<String, Object>();
		userInfo.put("contrib", contrib);
		userInfo.put("rank", rank);
		// 添加用户信息到redis中
		redisAdapter.hashMSet(RedisKey.USER_RANK_CONTRIB_ + userId, userInfo);
		redisAdapter.expireKey(RedisKey.USER_RANK_CONTRIB_ + userId,
				RedisExpireTime.EXPIRE_DAY_3);
		redisAdapter.strSetexByNormal(RedisKey.USER_RANK_SCORE_ + rank, RedisExpireTime.EXPIRE_DAY_10, contrib + "");
		return userInfo;

	}

	public static Map<String, String> getAndCacheStrUserRank(
			RedisClientAdapter redisAdapter,
			ContributionListMapper contribMapper, Long userId) {
		Map<String, String> userInfo = new HashMap<String, String>();
		if (redisAdapter == null || contribMapper == null || userId == null
				|| userId.longValue() < 0) {
			userInfo.put("contrib", "0");
			userInfo.put("rank", "0");
			return userInfo;
		}
		long contrib = contribMapper.getContribAll(userId);
		long rank = 0L;
		if(contrib!=0L){
			rank = contribMapper.getRankAll(contrib);
		}
		userInfo.put("contrib", contrib + "");
		userInfo.put("rank", rank + "");
		// 添加用户信息到redis中
		redisAdapter.hashMSet(RedisKey.USER_RANK_CONTRIB_ + userId, userInfo);
		redisAdapter.expireKey(RedisKey.USER_RANK_CONTRIB_ + userId, RedisExpireTime.EXPIRE_DAY_3);
		redisAdapter.strSetexByNormal(RedisKey.USER_RANK_SCORE_ + rank, RedisExpireTime.EXPIRE_DAY_10, contrib + "");
		return userInfo;

	}

	public static Map<String, Object> getUserRankCache(RedisClientAdapter redisAdapter, 
    		ContributionListMapper contribMapper, Long userId) {
		if (redisAdapter == null || userId == null || userId.longValue() < 0) {
			Map<String, Object> userInfo = new HashMap<String, Object>();
			userInfo.put("contrib", new Long(0));
			userInfo.put("rank", new Long(0));
			return userInfo;
		}
		Map<String, String> userInfo = redisAdapter
				.hashGetAll(RedisKey.USER_RANK_CONTRIB_ + userId);
        chageUserRankInfo(userInfo, redisAdapter, contribMapper, userId);
		Map<String, Object> result = new HashMap<String, Object>();
		result.putAll(userInfo);
		return result;

	}

	public static Map<String, String> getUserStrRankCache(RedisClientAdapter redisAdapter,  
    		ContributionListMapper contribMapper, Long userId) {
		if (redisAdapter == null || userId == null || userId.longValue() < 0) {
			Map<String, String> userInfo = new HashMap<String, String>();
			userInfo.put("contrib", "0");
			userInfo.put("rank", "0");
			return userInfo;
		}
		Map<String, String> userInfo = redisAdapter
				.hashGetAll(RedisKey.USER_RANK_CONTRIB_ + userId);
        chageUserRankInfo(userInfo, redisAdapter, contribMapper, userId);
		return userInfo;

	}
	
	/**
	 * 获得用户的年龄、贡献值和全站排行
	 * @param redisAdapter
	 * @param contribMapper
	 * @param userInfo
	 * @return
	 */
	public static void setUserInfo(
			RedisClientAdapter redisAdapter,
			ContributionListMapper contribMapper, Map<String, String> userInfo) {
		String birthday = userInfo.get("birthday");
		String strUserId = userInfo.get("userId");
		if (StringUtils.isBlank(strUserId)) {
			userInfo.put("age", "0");
			userInfo.put("birthday", "");
			userInfo.put("totalAmt", "0");
			userInfo.put("rank", "0");
			return;
		}

		try {
			userInfo.put("age", DateTools.getAgeByDate(birthday) + ""); // 年龄实时计算
			Long userId = Long.parseLong(strUserId);
			String rankKey = RedisKey.USER_RANK_CONTRIB_ + userId;
			if (redisAdapter.existsKey(rankKey)) {
				userInfo.putAll(getUserStrRankCache(redisAdapter, contribMapper, userId));
			} else {
				userInfo.putAll(getAndCacheStrUserRank(
						redisAdapter, contribMapper, userId));
			}
			userInfo.put("totalAmt", userInfo.get("contrib"));
		} catch (Exception e) {
			userInfo.put("age", "0");
			userInfo.put("birthday", "");
			userInfo.put("totalAmt", "0");
			userInfo.put("rank", "0");
			LOGGER.error("解析用户信息失败：" + strUserId, e);
		}
		return;
	}

	/**
	 * 如果该等级的全站排行分数发生变化则会查询数据库，重新计算用户排名和该名次的分数
	 * @param userInfo
	 * @param redisAdapter
	 * @param contribMapper
	 * @param userId
	 */
	private static void chageUserRankInfo(Map<String, String> userInfo, RedisClientAdapter redisAdapter,
            ContributionListMapper contribMapper, Long userId) {
		if (userInfo == null || userInfo.isEmpty()) {
			return;
		}
        if (userInfo != null) {
        	String rank = userInfo.get("rank");
        	String contrib = userInfo.get("contrib");
        	if ("0".equals(contrib)) {
        		return;
        	}
        	String rankKey = RedisKey.USER_RANK_SCORE_ + rank;
        	String score = redisAdapter.strGet(rankKey);
        	if (StringUtils.isNotBlank(score) && score.equals(contrib)) {
        		return;
        	}
        	userInfo.putAll(getAndCacheStrUserRank(redisAdapter, contribMapper, userId));
        }
	}

	/**
	 * 获取用户在直播间的贡献值
	 */
	public static int getUserInAnchorIdAmt(RedisClientAdapter redisAdapter,ContributionListMapper contribMapper,String userId,long anchorId) {
		if(StringUtils.isEmpty(userId)) {
			return 0;
		}
		String key = RedisKey.ANCHOR_USER_AMT_ + anchorId + Constants.UNDERLINE + userId;
		if(redisAdapter.existsKey(key)) {
			String contrib = redisAdapter.strGet(key);
			return Integer.valueOf(contrib);
		}else {
			int contrib = contribMapper.getContrib(Long.parseLong(userId), anchorId);
			redisAdapter.strSetexByNormal(key,RedisExpireTime.EXPIRE_DAY_1,contrib+"");
			return contrib;
		}
	}
}
