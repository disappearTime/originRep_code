package com.chineseall.iwanvi.wwlive.pc.video.service.impl;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.chineseall.iwanvi.wwlive.common.cache.RedisClientAdapter;
import com.chineseall.iwanvi.wwlive.common.cache.RedisKey;
import com.chineseall.iwanvi.wwlive.common.constants.Constants;
import com.chineseall.iwanvi.wwlive.dao.wwlive.AnchorMapper;
import com.chineseall.iwanvi.wwlive.dao.wwlive.UserVideoMapper;
import com.chineseall.iwanvi.wwlive.pc.common.Page;
import com.chineseall.iwanvi.wwlive.pc.video.service.Anchor2Service;

@Service
public class Anchor2ServiceImpl implements Anchor2Service {

	@Autowired
	private AnchorMapper anchorMapper;

	@Autowired
	RedisClientAdapter redisAdapter;
	
	@Autowired
	UserVideoMapper userVideoMapper;
	
	/**
	 * {@inheritDoc}
	 */
	public Map<String, Object> anchorLogin(String passport, String passwd) {
		//session
//		Map<String, Object> anchorInfo = anchorMapper.getAnchorByLogin(passport, passwd);
//		if (anchorInfo != null && !anchorInfo.isEmpty() && anchorInfo.get("anchorId") != null) {
//			anchorMapper.updateAnchorLogOnTime((long) anchorInfo.get("anchorId"));
//			return anchorInfo;
//		}
//		return anchorInfo;
        return null;
    }
	
	public int addUserToBlack(long anchorId, long userId, int time) {
		String key = RedisKey.BLACKLIST_ + anchorId + Constants.UNDERLINE + userId;
		if (redisAdapter.existsKey(key)) {
			redisAdapter.expireKey(key, time);
			return 1;
		} else {
			redisAdapter.strSetEx(key, key, time);
			return 1;
		}
	}

	public int delUserToBlack(long anchorId, long userId) {
		String key = RedisKey.BLACKLIST_ + anchorId + Constants.UNDERLINE + userId;
		if (redisAdapter.existsKey(key)) {
			return (int) redisAdapter.delKeys(key);
		} else {
			return 0;
		}
	}
	
	/**
	 * 观看用户
	 */
	public Page getUserList(long anchorId, long videoId, Page page) {
		int cnt = userVideoMapper.findUserListCount(videoId);
		if (cnt > 0) {
			List<Map<String, Object>> userList = userVideoMapper.findUserInfoList(anchorId, videoId, 
					page.getStart(), page.getPageSize());
			page.setTotal(cnt);

			
			if (CollectionUtils.isNotEmpty(userList) && userList.get(0) != null) {
				page.setData(userList);
				page.setId(((BigInteger)userList.get(0).get("videoId")).longValue());
				for (Map<String, Object> m : userList) {
					String userId = m.get("userId").toString();
					String key = RedisKey.BLACKLIST_ + anchorId + Constants.UNDERLINE + userId;
					if (redisAdapter.existsKey(key)) {
						m.put("isBlack", 1);
					} else {
						m.put("isBlack", 0);
					}
				}
			}
			
		}
		return page;
	}
	
	public Map<String, Object> getAnchorIndexInfo(long anchorId) {
		Map<String, Object> anchorInfo = anchorMapper.getAnchorIndexInfo(anchorId);
		return anchorInfo;
	}
	
	
}
