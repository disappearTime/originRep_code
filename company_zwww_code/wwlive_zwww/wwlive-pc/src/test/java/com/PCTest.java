package com;

import java.beans.IntrospectionException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.commons.lang.time.DateUtils;
import org.junit.Test;

import com.alibaba.fastjson.JSONObject;


public class PCTest {
	
	//TODO doMapToValue
	@Test
	public void test0() throws ClassNotFoundException, IntrospectionException {
		Class<?> clazz = Class.forName("com.chineseall.iwanvi.wwlive.domain.wwlive.LiveVideoInfo");
		Field[] fields = clazz.getDeclaredFields();
		System.out.println("public void doMapToValue(Map<String, Object> map, boolean isDealNull) {");
		for (Field field : fields) {
			if (!"serialVersionUID".equals(field.getName()) && (field.getModifiers() & 2) == 2){
//				PropertyDescriptor pd = new PropertyDescriptor(field.getName(), clazz);
				System.out.println("\tif (null != map.get(\"" + field.getName() + "\")) {");
				System.out.println("\t\tthis." + field.getName() + " = (" + field.getType().getSimpleName() + ") map.get(\"" + field.getName() + "\");");
				System.out.println("\t} else {\n\t\tif (isDealNull && map.containsKey(\"" + field.getName() + "\")) "
					+ "{\n\t\t\tthis." + field.getName() + " = (" + field.getType().getSimpleName() + ") map.get(\"" + field.getName() + "\");\n\t\t}\n\t}");
			}
		}
		System.out.println("}");
		System.out.println();
		
	}

	//TODO putFieldValueToMap
	@Test
	public void test1() throws ClassNotFoundException {
		Class<?> clazz = Class.forName("com.chineseall.iwanvi.wwlive.domain.wwlive.LiveVideoInfo");
		Field[] fields = clazz.getDeclaredFields();
		System.out.println("public Map<String, Object> putFieldValueToMap() {");
		System.out.println("\tMap<String, Object> map_= new HashMap<String, Object>();");
		for (Field field : fields) {
			if ((field.getModifiers() & 26) == 26) {
				continue;
			}
			if ((field.getModifiers() & 2) != 2) {
				continue;
			}
			System.out.println("\tmap_.put(\"" + field.getName() + "\"" + ", " + "this." + field.getName() + ");");
		}
		System.out.println("\treturn map_;");
		System.out.println("}");
		System.out.println();
		
	}
	
	public void test2() throws ParseException {
//		System.out.println(DateUtil.getAgeByDate(DateUtils.parseDate("2015-01-02 08:19:20", new String[]{"yyyy-MM-dd", "yyyy-MM-dd HH:mm:ss"})));
		System.out.println(DateFormatUtils.format(new Date(), "yyyy-MM-dd HH:mm:ss"));
		System.out.println(DateUtils.truncate(new Date(), Calendar.YEAR));
	}
	
	public void test3() {
//		byte[] Null = null;
		System.out.println("http://iwanvi-test.ks3-cn-beijing.ksyun.com/record/live/LIVE0000003/hls/LIVE0000003-0000003.m3u8".length());
	}

	public void test4() {
		String path = "http://iwanvi-test.ks3-cn-beijing.ksyun.com/record/live/LIVE0000003/hls/LIVE0000003-0000003.m3u8";
		System.out.println(path.substring(path.lastIndexOf('/') + 1, path.lastIndexOf('.')));
	}
	
	public void test5() {
		System.out.println(new BigDecimal(12550000).multiply(new BigDecimal(0.35)).setScale(0, BigDecimal.ROUND_HALF_UP));
	}

//	@Test
	public void test6() throws IllegalAccessException, InvocationTargetException {
		/*LoginUser user = new LoginUser();
		user.setExpireTime(System.currentTimeMillis());
		user.setLoginTime(System.currentTimeMillis());
		user.setRealName("1234");
		user.setUserId(123L);
		user.setUserName("123");
		LoginUser user2 = new LoginUser();
		LoginUser user3 = new LoginUser();
		BeanUtils.copyProperties(user3, user);
    	System.out.println(user3);
		org.springframework.beans.BeanUtils.copyProperties(user, user2);
    	System.out.println(user2);*/
	}

	public void test7() {
//		Map<String, String> userInfo = null;
//		if (ObjectUtils.isEmpty(userInfo)) {
//			System.out.println("--------------------------1");
//		}
		System.out.println(DateFormatUtils.ISO_DATETIME_FORMAT.getPattern());
		System.out.println(DateFormatUtils.formatUTC(new Date(), DateFormatUtils.ISO_DATETIME_FORMAT.getPattern()));
		System.out.println(DateFormatUtils.formatUTC(new Date(), "yyyy-MM-dd HH:mm:ss"));
		
	}
	
	public void test8() {
		System.out.println("\"12\"".replaceAll("\"", ""));
		String rtmp = "rtmp://iwanvi1.uplive.ks-cdn.com/live/LIVE0000037?accesskey=J0JC2zsEmrGybXdWGTNB&expire=1473138482&preset=iwanvi_live_preset&public=0&vdoid=0000019&signature=%252FJDSBEk2BX2YTIS%252BE0hchcaN96o%253D";
		System.out.println(rtmp.substring(rtmp.lastIndexOf("/") + 1));
	}

	public void test9() {
		PCTestImpl.hello = " world";
		System.out.println(PCTest.hello + " " + PCTestImpl.hello);
	}

	public void test10() {
		String wholeStr = "app=live&name=LIVE0000081&swfurl=nil&flashver=nil&tcurl=rtmp://iwanvi1.uplive.ks-cdn.com/live&call=user_publish&vdoid=";
		String[] params = wholeStr.split("&");
		Lock lock = new ReentrantLock();
		lock.lock();
		try {
			Thread.sleep(10000);
			if (ArrayUtils.isNotEmpty(params) && params.length > 2) {
				System.out.println(params[1].split("=")[1]);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		lock.unlock();
	}

	public void test11() {
		test10();
	}

	public void test12() {
		test10();
	}

	public void test13() {
		UUID uuid = UUID.randomUUID();
	    String str = uuid.toString(); 
	    String uuidStr = str.replace("-", "");
	    System.out.println(uuidStr);
	}

	public void test14() {
		int b;
		b = 1;
		int a = b;
		a = 3;
		System.out.println(a + " " + b);
	}

	public void test15() {
		System.out.println("http://ks3-cn-beijing.ksyun.com/test-iwanvi/".length());
	}

	public void test16() {
		privateMethod();
	}

	public void test17() {
		ArrayList<?>[] ss = new ArrayList<?>[]{};
		
		System.out.println(ss.toString());
		
	}

	public void test18() {
		JSONObject obj = JSONObject.parseObject("{id:1}");
		System.out.println(obj.toString());
		System.out.println(obj.get("id"));
	}
	
	public void test19() {
		System.out.println(tt());
	}

	public void test20() {
		double scale = new BigDecimal(4).divide(new BigDecimal(3), 3, BigDecimal.ROUND_HALF_UP
		).doubleValue();
		System.out.println(scale);
	}
	
	public String tt() {
		String tt = "";
		try {
			System.out.println(1 / 0);
			tt = "123";
		} catch (Exception e) {
		}
		return tt;
	}
	
	public static String hello = "hello";
	
	private void privateMethod() {
		System.out.println("我是私有方法");
	}

	public static void main(String[] args) {
		PCTest t = new PCTestImpl();
		t.test16();
	}
	
}
class PCTestImpl extends PCTest {
	public static String hello = "hello";

	public void test16() {
		System.out.println("我是PCTestImpl");
	}
	
}
