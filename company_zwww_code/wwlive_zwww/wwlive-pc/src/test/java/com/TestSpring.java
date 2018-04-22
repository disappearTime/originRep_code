package com;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.alibaba.fastjson.JSONObject;
import com.chineseall.iwanvi.wwlive.common.cache.RedisClientAdapter;
import com.chineseall.iwanvi.wwlive.common.cache.RedisKey;
import com.chineseall.iwanvi.wwlive.common.constants.Constants;
import com.chineseall.iwanvi.wwlive.common.exception.IWanviDataBaseException;
import com.chineseall.iwanvi.wwlive.common.exception.IWanviException;
//import com.chineseall.iwanvi.wwlive.common.external.ks3.Ks3ClientWrapper;
import com.chineseall.iwanvi.wwlive.common.external.rongcloud.RongCloudFacade;
import com.chineseall.iwanvi.wwlive.common.external.rongcloud.domain.ChatroomInfo;
import com.chineseall.iwanvi.wwlive.common.external.rongcloud.domain.SdkHttpResult;
import com.chineseall.iwanvi.wwlive.common.external.rongcloud.domain.TxtMessage;
import com.chineseall.iwanvi.wwlive.dao.wwlive.LiveVideoInfoMapper;
import com.chineseall.iwanvi.wwlive.pc.video.service.KsCloudService;
//import com.ksyun.ks3.dto.Bucket;

//import com.chineseall.iwanvi.wwlive.domain.database1.Test1;
//import com.github.pagehelper.PageHelper;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:spring/application.xml"})
public class TestSpring {

    @Autowired
	private LiveVideoInfoMapper videoMapper;

    @Autowired
	private KsCloudService ksCloudService;
    
    @Autowired
    RedisClientAdapter redisAdapter;
    
	private static final String Live_Name = "LIVE0000077";
	private static final String User_Name = "fk00002";

	public void test0() throws Exception {
		// 加入聊天室
		SdkHttpResult result = null;
		List<String> userIds = new ArrayList<String>();
		userIds.add(User_Name);
		result = RongCloudFacade.joinChatroom(userIds, Live_Name,
		Constants.FORMAT_JSON);
		System.out.println("joinChatroom=" + result);
		
	}

	public void test1() throws Exception {
		// 发送聊天室
		SdkHttpResult result = null;

		List<String> chatIds = new ArrayList<String>();
		chatIds.add(Live_Name);
		TxtMessage tx = new TxtMessage("hello my friend 1");
		result = RongCloudFacade.publishChatroomMessage(User_Name, chatIds, tx, Constants.FORMAT_JSON);
		System.out.println("publishChatroomMessage=" + result);
		
		Thread.sleep(5000);
		RoomData room = new RoomData("7", "", "");
		String extra = JSONObject.toJSONString(room);
		tx = new TxtMessage("hello my friend 2", extra);
		result = RongCloudFacade.publishChatroomMessage(User_Name, chatIds, tx, Constants.FORMAT_JSON);
		System.out.println("publishChatroomMessage=" + result);

		Thread.sleep(5000);
		RoomData room1 = new RoomData("8", "送了个JavaScript", "http://www.jb51.net/images/logo.gif");
		String extra1 = JSONObject.toJSONString(room1);
		TxtMessage tx1 = new TxtMessage("hello my friend 3", extra1);
		result = RongCloudFacade.publishChatroomMessage(User_Name, chatIds, tx1, Constants.FORMAT_JSON);
		System.out.println("publishChatroomMessage=" + result);

//		RoomData room = new RoomData("1", "系统消息：这是条测试消息", "");
//		String extra = JSONObject.toJSONString(room);
//		TxtMessage tx = new TxtMessage("", extra);
//		tx.setObjectName("1111111111111111111111");
//		result = RongCloudFacade.publishChatroomMessage(User_Name, chatIds, tx, Constants.FORMAT_JSON);
//		System.out.println("publishChatroomMessage=" + result);
//		for (int i = 0; i < 1000; i++) {
////			RoomData room = new RoomData("2", "系统消息：xxx被管理员禁言", "");
////			String extra = JSONObject.toJSONString(room);
//			Thread.sleep(10);
//			TxtMessage tx = new TxtMessage("123", "");
//			result = RongCloudFacade.publishChatroomMessage(User_Name, chatIds, tx, Constants.FORMAT_JSON);
//			System.out.println("publishChatroomMessage=" + result);
//		}
		
	}

	public void test3() throws Exception {
		System.out.println("-------------------------------------");
		/*
		 * fk00001
			fk00007
			fk00006
			fk00005
		*/
		String result = RongCloudFacade
				.getToken(
						"fk00001",
						"fk00001",
						"",
						1);
		
		System.out.println("fk00001 gettoken=" + result);
		System.out.println("-------------------------------------");
		/*result = RongCloudFacade
				.getToken(
						"fk00007",
						"fk00007",
						"",
						1, Constants.FORMAT_JSON);
		
		System.out.println("fk00007 gettoken=" + result);
		System.out.println("-------------------------------------");
		result = RongCloudFacade
				.getToken(
						"fk00006",
						"fk00006",
						"",
						1, Constants.FORMAT_JSON);
		
		System.out.println("fk00006 gettoken=" + result);
		System.out.println("-------------------------------------");
		result = RongCloudFacade
				.getToken(
						"fk00005",
						"fk00005",
						"",
						1, Constants.FORMAT_JSON);
		
		System.out.println("fk00005 gettoken=" + result);
		System.out.println("-------------------------------------");*/
	}

	public void test4() {
//		LiveVideoInfo info = videoMapper.findVideoInfoByStreamName("LIVE0000033");
//		System.out.println(info.getStreamName() + " " + info.getVideoStatus());
		ksCloudService.noticeLiveStop("LIVE0000033");
	}

	public void test5() throws Exception {
		List<String> list = redisAdapter.findKeys("anchor_info_*");
		System.out.println(Arrays.toString(list.toArray()));
	}

	public void test6() throws Exception {
		String key = RedisKey.VideoGrayKeys.LIVING_VIDEOS_;// + DateFormatUtils.format(new Date(), Constants.YY_MM_DD)
		long cnt = redisAdapter.zsetAdd(key, 
				 0  //为null则为0
						, 776 + "");
		System.out.println(cnt);
	}
	
	public void test7(){
		try {
			ChatroomInfo chatroom = new ChatroomInfo("000000000456", "test123");
			SdkHttpResult result = RongCloudFacade.createChatroom(chatroom,
					Constants.FORMAT_JSON);
			System.out.println(result);
			if (result.getHttpCode() != 200) {
				throw new IWanviDataBaseException("生成聊天室失败。");
			}
			SdkHttpResult rongResult = RongCloudFacade.joinChatroom("41", "000000000456", Constants.FORMAT_JSON);
			System.out.println(rongResult);
			if (rongResult.getHttpCode() != 200) {
				throw new IWanviException("创建视频流失败：聊天室失败。");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	public void test8(){
		try {
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/*
	public void test8(){
		Ks3ClientWrapper ks3 = new Ks3ClientWrapper();
		ks3.deleteObject("test-iwanvi", "skdtest");
	}

	public void test9(){
		Ks3ClientWrapper ks3 = new Ks3ClientWrapper();
		List<Bucket> list = ks3.listBuckets();
		for (Bucket b : list) {
			System.out.println(b.getName() + "[" + b.toString() + "]");
		}
	}*/
	
	public static void main(String[] args) {
		RoomData room1 = new RoomData("8", "送了个JavaScript", "http://www.jb51.net/images/logo.gif");
		System.out.println(JSONObject.toJSON(room1).toString());
	}
	
}
class RoomData {
	String dataType, dataValue, dataExtra;

	public String getDataType() {
		return dataType;
	}

	public void setDataType(String dataType) {
		this.dataType = dataType;
	}

	public String getDataValue() {
		return dataValue;
	}

	public void setDataValue(String dataValue) {
		this.dataValue = dataValue;
	}

	public String getDataExtra() {
		return dataExtra;
	}

	public void setDataExtra(String dataExtra) {
		this.dataExtra = dataExtra;
	}

	public RoomData(String dataType, String dataValue, String dataExtra) {
		super();
		this.dataType = dataType;
		this.dataValue = dataValue;
		this.dataExtra = dataExtra;
	}

	public RoomData() {
		super();
	}
	
}
