package com.chineseall.iwanvi.wwlive.web.video.service.impl;

import com.alibaba.dubbo.registry.redis.RedisRegistry;
import com.chineseall.iwanvi.wwlive.common.cache.RedisClientAdapter;
import com.chineseall.iwanvi.wwlive.common.cache.RedisExpireTime;
import com.chineseall.iwanvi.wwlive.common.cache.RedisKey;
import com.chineseall.iwanvi.wwlive.dao.wwlive.AdvertMapper;
import com.chineseall.iwanvi.wwlive.dao.wwlive.UserPushMapper;
import com.chineseall.iwanvi.wwlive.domain.wwlive.Advertisement;
import com.chineseall.iwanvi.wwlive.domain.wwlive.UserPush;
import com.chineseall.iwanvi.wwlive.web.video.service.AdvertisementService;
import org.apache.commons.collections.map.HashedMap;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Created by 云瑞 on 2017/6/28.
 */
@Service
public class AdvertisementServiceImpl implements AdvertisementService {

    private static final Logger LOGGER = Logger.getLogger(AdvertisementServiceImpl.class);

    @Autowired
    private RedisClientAdapter redisAdapter;

    @Autowired
    private AdvertMapper advertMapper;

    @Autowired
    private UserPushMapper userPushMapper;

    public Map<String, Object> getAdvertBannerList(String cnid,String version) {
        String tmpKey = RedisKey.ADVERT_LIST_BANNER;
        return getBanners(tmpKey, false,2,cnid,version);
    }

    public Map<String, Object> getGrayAdvertBannerList(String cnid,String version) {
        String tmpKey = RedisKey.ADVERT_LIST_BANNER_TEXT;
        return getBanners(tmpKey, false,2,cnid,version);
    }

    public Map<String, Object> getAdvertBootimgList(String cnid,String version){
        String tmpKey = RedisKey.ADVERT_LIST_BOOTIMG;
        return getBanners(tmpKey, false,1,cnid,version);
    }

    public Map<String, Object> getGrayAdvertBootimgList(String cnid,String version){
        String tmpKey = RedisKey.ADVERT_LIST_BOOTIMG_TEXT;
        return getBanners(tmpKey, false,1,cnid,version);
    }

    public Map<String, Object> getBanners(String tmpKey, boolean isGray,int place,String cnid,String version) {
        Map<String, Object> resultJson = new HashMap<>();
        List<Map<String, String>> advertList = null;
        List<String> advIds = null;
        try {
//            List<String> keys = redisAdapter.findKeys(tmpKey + "*");
            advIds = advertMapper.findadvertByCnidVersion(place, cnid, version);
            if (advIds != null && advIds.size() > 0) {
                advertList = getBannerjson(advIds,tmpKey);
            }else {
                resultJson.put("advertList", new String[]{});
                return resultJson;
            }

            if(advertList.size() == 0){
                setAdvertInfo(tmpKey, place);
                advertList = getBannerjson(advIds,tmpKey);
            }

        } catch (Exception e) {
            LOGGER.error("",e);
        }
        if(advertList.size() == 0){
            resultJson.put("advertList", new String[]{});
        }else  {
            resultJson.put("advertList", advertList);
        }
        return resultJson;
    }

    public List<String> advIdAddRedis(int place,String cnid,String version) {
        List<String> advIds = advertMapper.findadvertByCnidVersion(place, cnid, version);
        if(advIds!=null && advIds.size()>0){
            for (String id:advIds){
                redisAdapter.listRpush(cnid+"_"+version,id);
                redisAdapter.expireKey(cnid+"_"+version, RedisExpireTime.EXPIRE_DAY_1);
            }
        }
        return advIds;
    }

    /**
     * redis 中获取广告
     *
     */
    public  List<Map<String, String>> getBannerjson(List<String> advIds,String tmpKey) {
        List<Map<String, String>> advertList = new ArrayList<>();
        for (String id : advIds) {
            Map<String, String> advMap = null;

            if (redisAdapter.existsKey(tmpKey+id)) {
                advMap = redisAdapter.hashMGet(tmpKey+id, "advId",
                        "advUrl", "type", "url");
            }
            if (advMap != null && !advMap.isEmpty()) {
                advertList.add(advMap);
            }
        }
        return advertList;
    }

    /**
     * 数据中获取广告并添加到广告
     */
    public List<Map<String, String>> setAdvertInfo(String tmpKey, int place) {
        List<String> Downshelf = advertMapper.findAdvstateDownshelf();
        List<String> Upshelf = advertMapper.findAdvstateUpshelf();

        if(Downshelf != null && Downshelf.size() > 0) {
            for (String advId:Downshelf) {
                advertMapper.updateAdvstateById(Long.valueOf(advId),2);
            }
        }
        if(Upshelf != null && Upshelf.size() > 0) {
            for (String advId2:Upshelf) {
                advertMapper.updateAdvstateById(Long.valueOf(advId2),1);
            }
        }
        List<Map<String, String>> list = new ArrayList<>();
        List<Advertisement> advsertList = null;
        if(place == 1) {
            advsertList = advertMapper.findBootImgsUpshelf(place);
        }else {
            advsertList = advertMapper.findAdvsertIsUpshelfAll(place);
        }

        if(advsertList != null && advsertList.size() > 0) {
            for (Advertisement adv : advsertList) {
                Map<String,String> map = new HashedMap();
                map.put("advId",adv.getAdvId()+"");
                map.put("advUrl",adv.getAdvUrl());
                map.put("type",adv.getType()+"");
                map.put("url",adv.getUrl());
                list.add(map);
                redisAdapter.hashMSet(tmpKey + adv.getAdvId(),map);
                redisAdapter.expireKey(tmpKey+adv.getAdvId(), RedisExpireTime.EXPIRE_MIN_10);
            }
        }
        return list;
    }

    public int addUserPush(UserPush userPush) {
       return  userPushMapper.addUserPush(userPush);
    }
}