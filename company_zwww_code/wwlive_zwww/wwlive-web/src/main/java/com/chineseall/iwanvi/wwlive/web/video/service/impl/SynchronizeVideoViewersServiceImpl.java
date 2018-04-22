package com.chineseall.iwanvi.wwlive.web.video.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.chineseall.iwanvi.wwlive.common.cache.RedisClientAdapter;
import com.chineseall.iwanvi.wwlive.web.video.service.SynchronizeVideoViewersService;

@Service
public class SynchronizeVideoViewersServiceImpl implements SynchronizeVideoViewersService {

	@Autowired
	private RedisClientAdapter redisAdapter;
	
}
