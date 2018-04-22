package com.chineseall.iwanvi.wwlive.pc.video.service.impl;

import com.chineseall.iwanvi.wwlive.pc.common.ExtendProperties;
import com.chineseall.iwanvi.wwlive.pc.video.service.DaoFactoryService;
import com.zw.zcf.dao.redis.IRedisDao;
import com.zw.zcf.dao.redis.factory.RedisDaoFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("daoFactoryService")
public class DaoFactoryServiceImpl implements DaoFactoryService {
    @Autowired
    private ExtendProperties extendProperties;
    /**
     * 获取push-redis的对象
     * @return IRedisDao
     */
    public   IRedisDao getPushRedisDao() {
        String host=extendProperties.getPushRedisHost();
        int port=extendProperties.getPushRedisPort();
        String pwd=extendProperties.getPushReidsPwd();
//        System.out.print(host+"---getPushRedisDao---"+port);
        return RedisDaoFactory.getRedisDao(host,port,pwd);
    }
}
