package com.chineseall.iwanvi.wwlive.pc.video.service;

import com.zw.zcf.dao.redis.IRedisDao;

public interface DaoFactoryService {
    /**
     * 获取push-redis的对象
     * @return IRedisDao
     */
    public IRedisDao getPushRedisDao();
}
