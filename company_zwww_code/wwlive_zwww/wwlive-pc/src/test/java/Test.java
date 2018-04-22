import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.chineseall.iwanvi.wwlive.common.external.rongcloud.RongCloudFacade;
import com.chineseall.iwanvi.wwlive.common.tools.StrMD5;
import com.chineseall.iwanvi.wwlive.domain.wwlive.Anchor;
import com.service.impl.FollowAnchorServiceImpl;

public class Test {

    public static void main(String[] args) {
        ScriptEngineManager factory = new ScriptEngineManager();
        for (ScriptEngineFactory available : factory.getEngineFactories()) {
            System.out.println(available.getEngineName());
        }
        ScriptEngine engine = factory.getEngineByName("JavaScript");
        String js;

        js = "var map = Array.prototype.map \n";
        js += "var names = [\"john\", \"jerry\", \"bob\"]\n";
        js += "var a = map.call(names, function(name) { return name.length() })\n";
        js += "print(a)";
        try {
            engine.eval(js);
        } catch (ScriptException e) {
            e.printStackTrace();
        }

    }
    
    public void test1() {
    	String str = "123,321,2,";
    	System.out.println(str.substring(0, str.length() - 1));
    }

    public void test2() {
    	Random random = new Random();
    	for (int i = 0; i < 100; i++) {
        	System.out.println(random.nextInt(10));
    	}
    }
    
    public void test3() throws Exception {
    	FollowAnchorServiceImpl followService = 
                new FollowAnchorServiceImpl();
    	System.out.println(followService.getFansNumber(1633554));
    }

    public void test4() throws Exception {

		System.out.println("-------------------------------------");
		String result = RongCloudFacade
				.getToken(
						"1057_zb",
						"",
						"",
						1);
		
		System.out.println("1633557 gettoken=" + result);
		result = RongCloudFacade
				.getToken(
						"1633556",
						"",
						"",
						1);
		
		System.out.println("1633556 gettoken=" + result);
		result = RongCloudFacade
				.getToken(
						"1633548",
						"",
						"",
						1);
		
		System.out.println("1633548 gettoken=" + result);
		result = RongCloudFacade
				.getToken(
						"1633547",
						"",
						"",
						1);
		
		System.out.println("1633547 gettoken=" + result);
		result = RongCloudFacade
				.getToken(
						"1633545",
						"",
						"",
						1);
		
		System.out.println("1633545 gettoken=" + result);
		result = RongCloudFacade
				.getToken(
						"1633532",
						"",
						"",
						1);
		
		System.out.println("1633532 gettoken=" + result);
		result = RongCloudFacade
				.getToken(
						"1633502",
						"",
						"",
						1);
		
		System.out.println("1633502 gettoken=" + result);
		System.out.println("-------------------------------------");
    }
    
    public void test5() {
    	Anchor anchor = new Anchor();
		anchor.setPassport("zhoumeng@163.com");
		anchor.setUserName("周梦");
		String passwd = "123456";
		anchor.setPasswd(StrMD5.getInstance().encrypt(passwd, "iwanvi_salt"));
//		anchor.setBirthday();
		anchor.setZodiac("摩羯座");
		Integer sex = 0;
		anchor.setSex(sex);
		anchor.setRoomNum(17000151L);
		anchor.setHeadImg("");
		String token = null;
		try {
			token = RongCloudFacade.getToken(152 + "", "周梦", "", 0);
		} catch (Exception e) {
			e.printStackTrace();
		}
		anchor.setRongToken(token);
		System.out.println(anchor);
    }

    @org.junit.Test    
    public void test6() {
    	List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
    	Map<String, Object> s1 = null;
    	for (int i = 0; i < 10; i++) {
    		s1 = new HashMap<String, Object>();
    		s1.put("k", i);
    		list.add(s1);
    	}
    	System.out.println(list);
    	
    	String stupid = JSONObject.toJSONString(list);
    	System.out.println(stupid);
    	
    	List<JSONObject>  sb = JSONArray.parseArray(stupid, JSONObject.class);
    	System.out.println(sb);
    }
    
}
