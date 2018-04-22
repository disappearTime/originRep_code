package com.chineseall.iwanvi.wwlive.web.video.service;

import java.text.ParseException;
import java.util.Map;


public interface GoodsInfoService {

    Map<String, Object> getShelfGoodsList(String userId) throws ParseException;

    Map<String, String> getShelfBarrage();
    
    Map<String, Object> findNobles(Long userId);

//    List<JSONObject> getNobleGoods(String userId);
    
    Map<String, Object> getShelfNobleList();

    Map<String, Object> getShelfNobleById(String goodsId);
}
