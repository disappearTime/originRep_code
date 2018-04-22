package com.chineseall.iwanvi.wwlive.pc.common.helper;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.chineseall.iwanvi.wwlive.common.cache.RedisClientAdapter;
import com.chineseall.iwanvi.wwlive.common.cache.RedisExpireTime;
import com.chineseall.iwanvi.wwlive.common.cache.RedisKey;
import com.chineseall.iwanvi.wwlive.dao.wwlive.ContributionListMapper;

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
		if (redisAdapter == null 
				|| contribMapper == null || userId == null || userId.longValue() < 0) {
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
		redisAdapter.expireKey(RedisKey.USER_RANK_CONTRIB_ + userId, RedisExpireTime.EXPIRE_DAY_3);
		redisAdapter.strSetexByNormal(RedisKey.USER_RANK_SCORE_ + rank, RedisExpireTime.EXPIRE_DAY_10, contrib + "");
        return userInfo;
		
	}

	public static Map<String, Object> getUserRankCache(RedisClientAdapter redisAdapter, 
			ContributionListMapper contribMapper, Long userId) {
		if (redisAdapter == null 
				 || userId == null || userId.longValue() < 0) {
			Map<String, Object> userInfo = new HashMap<String, Object>();
	        userInfo.put("contrib", new Long(0));
	        userInfo.put("rank", new Long(0));
			return userInfo;
		}
		Map<String, String> userInfo = redisAdapter.hashGetAll(RedisKey.USER_RANK_CONTRIB_ + userId);
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

    public static Map<String, String> getAndCacheStrUserRank(RedisClientAdapter redisAdapter,
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
        redisAdapter.expireKey(RedisKey.USER_RANK_CONTRIB_ + userId,
                RedisExpireTime.EXPIRE_DAY_3);
		redisAdapter.strSetexByNormal(RedisKey.USER_RANK_SCORE_ + rank, RedisExpireTime.EXPIRE_DAY_10, contrib + "");
        return userInfo;
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
	
}
