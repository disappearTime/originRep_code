package com.chineseall.iwanvi.wwlive.pc.common.helper;

import java.text.ParseException;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.chineseall.iwanvi.wwlive.common.cache.RedisClientAdapter;
import com.chineseall.iwanvi.wwlive.common.cache.RedisExpireTime;
import com.chineseall.iwanvi.wwlive.common.cache.RedisKey;
import com.chineseall.iwanvi.wwlive.common.exception.IWanviException;
import com.chineseall.iwanvi.wwlive.dao.wwlive.AnchorMapper;
import com.chineseall.iwanvi.wwlive.domain.wwlive.Anchor;

public class AnchorInfoHelper {
    
    private static Logger LOGGER = Logger.getLogger(AnchorInfoHelper.class);
    
    public static Anchor getAndCacheCurrentAnchorInfo(RedisClientAdapter redisAdapter, 
            AnchorMapper anchorMapper, Long anchorId) {
        
        if (redisAdapter == null 
                || anchorMapper == null || anchorId <= 0) {
            return null;
        }
        String anchorInfoKey = RedisKey.ANCHOR_INFO_ + anchorId;
        Anchor anchor = null;
        if (redisAdapter.existsKey(anchorInfoKey)) {
            Map<String, String> tmpMap = redisAdapter.hashMGet(anchorInfoKey, 
                    "anchorId","rongToken", "acctStatus", "userName", "headImg", "roomNum", "passwd");
            if (tmpMap != null && !tmpMap.isEmpty() 
                    && StringUtils.isNotBlank(tmpMap.get("rongToken"))) {
                anchor = new Anchor();
                try {
                    anchor.doStringMapToValue(tmpMap);
                } catch (ParseException e) {
                    LOGGER.info("获得主播信息异常:" + e.toString());
                    throw new IWanviException("获得主播信息异常");
                }
            } else {
                redisAdapter.delKeys(RedisKey.ANCHOR_INFO_ + anchorId);
                anchor = anchorMapper.findAnchorById(anchorId);
                redisAdapter.hashMSet(anchorInfoKey, anchor.putFieldValueToStringMap());
            }
            
        } else {
            anchor = anchorMapper.findAnchorById(anchorId);
            redisAdapter.hashMSet(anchorInfoKey, anchor.putFieldValueToStringMap());
        }        
        
        return anchor;
    }
    
    public static Anchor getAndCacheAnchorInfo(RedisClientAdapter redisAdapter, 
			AnchorMapper anchorMapper, long anchorId) {
		if (redisAdapter == null 
				|| anchorMapper == null || anchorId <= 0) {
			return null;
		}
        Anchor anchor = anchorMapper.findAnchorById(anchorId);        
        if (anchor != null) {
            // 添加用户信息到redis中
            redisAdapter.hashMSet(RedisKey.ANCHOR_INFO_ + anchorId, anchor.putFieldValueToStringMap());
    		redisAdapter.expireKey(RedisKey.ANCHOR_INFO_ + anchorId, RedisExpireTime.EXPIRE_DAY_30);
        } else {
        	LOGGER.error("主播：" + anchorId + "不存在。");
        }
        return anchor;
	}
    
}
