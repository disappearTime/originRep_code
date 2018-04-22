package com;

import java.util.Date;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.chineseall.iwanvi.wwlive.common.cache.RedisClientAdapter;
import com.chineseall.iwanvi.wwlive.dao.wwlive.BlackListMapper;
import com.chineseall.iwanvi.wwlive.dao.wwlive.UserInfoMapper;
import com.chineseall.iwanvi.wwlive.domain.wwlive.BlackList;
import com.chineseall.iwanvi.wwlive.pc.video.service.KsCloudService;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:spring/application.xml" })
public class TestSpring220 {

	@Autowired
	BlackListMapper blackListMapper;

	@Autowired
	RedisClientAdapter redisAdapter;

	@Autowired
	private ThreadPoolTaskExecutor taskExecutor;

	@Autowired
	private UserInfoMapper userInfoMapper;

	@Autowired
	private KsCloudService ksCloudService;
	
	public void test1() {
		BlackList black = new BlackList();
		black.setSuperAdminId(1L);
		black.setUserId(3L);
		black.setCreateTime(new Date());
		System.out.println(blackListMapper.insertBlack(black));
	}

	public void test2() {
		System.out.println(blackListMapper.findBlackStatusByUserIdAndSuperId(1L, 1L));
		System.out.println(blackListMapper.findBlackInfoByUserIdAndSuperId(1L, 1L));
	}

	public void test3() {
		System.out.println(redisAdapter.zsetScore("canihelpyou", "911"));
		System.out.println(redisAdapter.zsetRem("canihelpyou", "911"));
		System.out.println(redisAdapter.setIsMember("canihelpyou", "911"));
	}

	@Test
	public void test4() throws InterruptedException {
		ksCloudService.noticeLiveStop("LIVE0005875");
		Thread.sleep(100000);
	}
	
}
