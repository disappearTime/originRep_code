package com.chineseall.iwanvi.wwlive.pc.common.helper;

import com.chineseall.iwanvi.wwlive.common.cache.RedisClientAdapter;
import com.chineseall.iwanvi.wwlive.common.cache.RedisExpireTime;
import com.chineseall.iwanvi.wwlive.common.cache.RedisKey;
import com.chineseall.iwanvi.wwlive.dao.wwlive.ContributionListMapper;

/**
 * Created by Niu Qianghong on 2017-11-09 0009.
 * 贡献值工具类
 */
public class ContribHelper {

    /**
     * 获得并直接缓存礼品 + 贵族贡献值
     * @param anchorId
     * @param contribMapper
     * @param redisAdapter
     * @return
     */
    public static Long cacheNormal(Long anchorId, ContributionListMapper contribMapper, RedisClientAdapter redisAdapter){
        Long contrib = 0L;
        if (anchorId == null || contribMapper == null || redisAdapter == null) {
            return contrib;
        }
        contrib = contribMapper.getContribByAnchorId(anchorId, 1);
        String contribKey = RedisKey.ANCHOR_CONTRIB_ + anchorId;
        redisAdapter.strSetByNormal(contribKey, contrib.toString());
        redisAdapter.expireKey(contribKey, RedisExpireTime.EXPIRE_HOUR_3);
        return contrib;
    }

    /**
     * 根据anchorId获取送礼+贵族贡献值; 缓存有走缓存, 缓存无走数据库
     * @param anchorId
     * @param contribMapper
     * @param redisAdapter
     * @return
     */
    public static Long getNormalByAnchor(Long anchorId, ContributionListMapper contribMapper, RedisClientAdapter redisAdapter) {
        Long contrib = 0L;
        if (anchorId == null || contribMapper == null || redisAdapter == null) {
            return contrib;
        }

        String contribKey = RedisKey.ANCHOR_CONTRIB_ + anchorId;
        if (redisAdapter.existsKey(contribKey)) {
            contrib = Long.valueOf(redisAdapter.strGet(contribKey));
        } else {
            contrib = contribMapper.getContribByAnchorId(anchorId, 1);
            redisAdapter.strSetByNormal(contribKey, contrib.toString());
            redisAdapter.expireKey(contribKey, RedisExpireTime.EXPIRE_HOUR_3);
        }
        return contrib;
    }
}
