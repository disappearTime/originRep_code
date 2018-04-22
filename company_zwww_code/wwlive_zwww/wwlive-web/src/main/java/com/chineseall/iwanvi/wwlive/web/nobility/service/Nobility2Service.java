package com.chineseall.iwanvi.wwlive.web.nobility.service;

import java.util.Map;

import com.alibaba.fastjson.JSONObject;

/**
 * Created by Niu Qianghong on 2017-07-13 0013.
 */
public interface Nobility2Service {
    Map<String,Object> checkExpire(Long userId);
    
    /**
     * 购买贵族
     * @param userId 用户id 必传
     * @param videoId 视频id 非必传
     * @param anchorId 主播id 非
     * @param way	        来源 0-H5 1-直播间 非
     * @param goodsId  贵族ID 必传
     * @param payType  支付类型：2微信 3支付宝 必传
     * @return
     */
    JSONObject noblePurchase(Long userId, Long videoId, Long anchorId, 
    		Integer way, Long goodsId, Integer payType);
}
