import java.net.HttpURLConnection;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import com.chineseall.iwanvi.wwlive.common.check.ResponseResult;
import com.chineseall.iwanvi.wwlive.common.external.rongcloud.domain.SdkHttpResult;
import com.chineseall.iwanvi.wwlive.common.tools.HttpUtils;
import com.chineseall.iwanvi.wwlive.common.tools.StrMD5;
import com.chineseall.iwanvi.wwlive.domain.wwlive.Anchor;
import com.chineseall.iwanvi.wwlive.web.common.spring.LaunchObjectMapper;
import com.chineseall.iwanvi.wwlive.web.common.util.JsonUtils;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

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

	public void test1() throws JsonProcessingException {
		ObjectMapper mapper = new LaunchObjectMapper();
		mapper.setSerializationInclusion(Include.NON_NULL);
		ResponseResult<Map<String, Object>> rr = new ResponseResult<>();
		Map<String, Object> data = new HashMap<String, Object>();
		data.put("duration", new Integer(0));
		data.put("viewers", new Integer(0));
		data.put("notice", null);
		data.put("giftCnt", new Integer(0));
		rr.setData(data);
		System.out.println(mapper.writeValueAsString(rr));
	}

	public void test2() throws JsonProcessingException {
		String[] t = new String[]{"a", "d", "c", "k"};
		Arrays.sort(t);
		System.out.println(Arrays.toString(t));
		Arrays.sort(t, new Comparator<String>() {

			@Override
			public int compare(String o1, String o2) {
				return -(o1.hashCode() - o2.hashCode());
			}
			
		});
		System.out.println(Arrays.toString(t));
	}

	public void test3() throws JsonProcessingException {
		Anchor anchor = new Anchor();
		anchor.setAnchorId(100L);
		anchor.setRongToken("sksksk");
		anchor.setUserName("slslslsll");
		anchor.setHeadImg("slslsll");
		anchor.setRoomNum(1000L);
		System.out.println(JsonUtils.toValueOfJsonString(anchor));
	}

	public void test4() throws Exception {
		HttpURLConnection conn = HttpUtils.createPostHttpConnection("http://zb.cread.com/app/video/detail.json");
    	SdkHttpResult result = HttpUtils.returnResult(conn, "coverKey=8c73012485b38a7c0129afa732078552&platform=android"
    			+ "&IMEI=35460207972937&cnid=1062&requestId=48f13d52a6dc49989cd9c38847ca5e94&userId=704"
    			+ "&version=2.2.0&videoId=5802&nonce=A8TUR2PY&model=SM-J3109");
    	System.out.println(result.toString());
	}

	public void test5() throws Exception {
		System.out.println(StrMD5.getInstance().getStringMD5("704xulvhijpF703D2699E794EA5AB7E835F57BD588C163343311"));
	}

	@org.junit.Test
	public void test6() {
        System.out.println(3 * 0.01);
        System.out.println(3 * 0.01F);
        System.out.println(3 * 0.01F == 0.03);
	}
	
}
