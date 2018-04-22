package com;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.chineseall.iwanvi.wwlive.common.cache.RedisClientAdapter;
import com.chineseall.iwanvi.wwlive.dao.wwlive.BlackListMapper;
import com.chineseall.iwanvi.wwlive.dao.wwlive.ContributionListMapper;
import com.chineseall.iwanvi.wwlive.dao.wwlive.UserInfoMapper;
import com.chineseall.iwanvi.wwlive.pc.common.helper.UserRankHelper;
import com.chineseall.iwanvi.wwlive.pc.video.service.KsCloudService;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:spring/application.xml" })
public class TestSpring230 {

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

	@Autowired
	private ContributionListMapper contribMapper;
	
	@Test
	public void test1() {
		System.out.println(UserRankHelper.getUserStrRankCache(redisAdapter, contribMapper, 692L));
		
	}
	
}
