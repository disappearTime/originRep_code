package com.chineseall.iwanvi.wwlive.pc.video.service.impl;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.chineseall.iwanvi.wwlive.dao.wwlive.OrderInfoMapper;
import com.chineseall.iwanvi.wwlive.pc.common.Page;
import com.chineseall.iwanvi.wwlive.pc.video.service.OrderInfoService;

@Service
public class OrderInfoServiceImpl implements OrderInfoService {

	@Autowired
	OrderInfoMapper orderInfoMapper;

	@Override
	public Page getOrderInfoByOrigKey(long originKey,
			Page page) {
		int cnt = orderInfoMapper.cntOrderInfoByOrigKey(originKey);
		if (cnt > 0) {
			 List<Map<String, Object>> orderList =
					 orderInfoMapper.getOrderInfoByOrigKey(originKey, page.getStart(), page.getPageSize());
			 page.setId(originKey);
			 page.setData(orderList);
			 page.setTotal(cnt);
		}
		
		return page;
	}
	
}
