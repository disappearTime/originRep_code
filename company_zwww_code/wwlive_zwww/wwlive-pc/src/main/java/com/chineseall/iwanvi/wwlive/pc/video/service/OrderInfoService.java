package com.chineseall.iwanvi.wwlive.pc.video.service;


import com.chineseall.iwanvi.wwlive.pc.common.Page;

public interface OrderInfoService {
	
	/**
    * 用户昵称，商品名称、数量、总价值
    * @param originKey
    * @return
    */
	Page getOrderInfoByOrigKey(long originKey, Page page);
}
