package com;

import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.alibaba.fastjson.JSONObject;
import com.chineseall.iwanvi.wwlive.pc.common.Page;
import com.chineseall.iwanvi.wwlive.pc.video.service.impl.ChatLetterServiceImpl;
import com.chineseall.iwanvi.wwlive.pc.video.service.impl.UserInfoServiceImpl;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:spring/application.xml" })
public class TestSpring240 {
	
	@Autowired
	ChatLetterServiceImpl chatLetterServiceImpl;

	@Autowired
	UserInfoServiceImpl userInfoServiceImpl;
	
	public void test1() {
		Map<String, Object>  result = chatLetterServiceImpl.getLetters(new Page(), 1633518L);
		System.out.println(JSONObject.toJSONString(result, true));
	}

	@Test
	public void test2() {
		System.out.println(userInfoServiceImpl.getUserInfo(1633526L, 118L));
	}
	
}
