package com.chineseall.iwanvi.wwlive.web.common.pay.alipay;

import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import com.alibaba.fastjson.JSONObject;
import com.chineseall.iwanvi.wwlive.common.constants.Constants;
import com.chineseall.iwanvi.wwlive.common.tools.HttpUtils;
import com.chineseall.iwanvi.wwlive.web.common.pay.RSAConfig;
import com.chineseall.iwanvi.wwlive.web.common.pay.RSATools;

@Component
public class UserAliPay {
	
	private static final Logger LOGGER = Logger.getLogger(UserAliPay.class);

	//Fileds
	/**
	 * 应用id
	 */
	@Value("${alipay.appid}")
	private String appId;
	
	/**
	 * 过期时间30分钟
	 */
	private String timeExpress = "30m";

    /**
     * 支付宝消息验证地址
     */
    private static final String HTTPS_VERIFY_URL = "https://mapi.alipay.com/gateway.do?service=notify_verify&";
    
    //Methods
    
	/**
	 * 构造支付订单参数列表
	 * 
	 * @param json
	 * @param outTradeNo
	 * @return
	 */
	public Map<String, String> buildOrderParamMap(JSONObject json,
			String outTradeNo, String notifyUrl) {
		Map<String, String> keyValues = new HashMap<String, String>();

		keyValues.put("app_id", appId);
		// "{\"timeout_express\":\"30m\",\"product_code\":\"QUICK_MSECURITY_PAY\",\"total_amount\":\"0.01\",\"subject\":\"1\",\"body\":\"我是测试数据\",\"out_trade_no\":\""
		// + getOutTradeNo() + "\"}";
		keyValues.put("biz_content", json.toJSONString());

		keyValues.put("charset", Constants.UTF8);

		keyValues.put("method", "alipay.trade.app.pay");

		keyValues.put("notify_url", notifyUrl);

		keyValues.put("sign_type", "RSA");

		keyValues.put("timestamp",
				DateFormatUtils.format(new Date(), Constants.STAND_YY_MM_DD));

		keyValues.put("version", "1.0");

		return keyValues;
	}

	public JSONObject crateBizContent(String subject, String outTradeNo,
			String totalAmount) {
		Assert.hasText(subject, "subject: 为空或null。");
		Assert.hasText(outTradeNo, "outTradeNo: 为空或null。");
		Assert.hasText(totalAmount, "totalAmount: 为空或null。");
		JSONObject json = new JSONObject();
		json.put("timeout_express", timeExpress);
		json.put("product_code", "QUICK_MSECURITY_PAY");
		json.put("total_amount", totalAmount);
		json.put("subject", subject);
		json.put("out_trade_no", outTradeNo);
		return json;
	}

	/**
	 * 构造支付订单参数信息
	 * @param map 支付订单参数
	 * @return
	 */
	public String buildOrderParam(Map<String, String> map) {
		List<String> keys = new ArrayList<String>(map.keySet());

		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < keys.size() - 1; i++) {
			String key = keys.get(i);
			String value = map.get(key);
			sb.append(buildKeyValue(key, value, true));
			sb.append(Constants.AND);
		}

		String tailKey = keys.get(keys.size() - 1);
		String tailValue = map.get(tailKey);
		sb.append(buildKeyValue(tailKey, tailValue, true));

		return sb.toString();
	}

	/**
	 * 拼接键值对
	 * 
	 * @param key
	 * @param value
	 * @param isEncode
	 * @return
	 */
	private static String buildKeyValue(String key, String value,
			boolean isEncode) {
		StringBuilder sb = new StringBuilder();
		sb.append(key);
		sb.append(Constants.EQU);
		if (isEncode) {
			try {
				sb.append(URLEncoder.encode(value, Constants.UTF8));
			} catch (UnsupportedEncodingException e) {
				sb.append(value);
			}
		} else {
			sb.append(value);
		}
		return sb.toString();
	}

	/**
	 * 对支付参数信息进行签名
	 * 
	 * @param map
	 *            待签名授权信息
	 * 
	 * @return
	 */
	public String getSign(Map<String, String> map, String rsaKey) {
		List<String> keys = new ArrayList<String>(map.keySet());
		// key排序
		Collections.sort(keys);

		StringBuilder authInfo = new StringBuilder();
		for (int i = 0; i < keys.size() - 1; i++) {
			String key = keys.get(i);
			String value = map.get(key);
			authInfo.append(buildKeyValue(key, value, false));
			authInfo.append(Constants.AND);
		}

		String tailKey = keys.get(keys.size() - 1);
		String tailValue = map.get(tailKey);
		authInfo.append(buildKeyValue(tailKey, tailValue, false));

		String oriSign = RSATools.sign(authInfo.toString(), rsaKey,
				Constants.UTF8);
		String encodedSign = "";

		try {
			encodedSign = URLEncoder.encode(oriSign, Constants.UTF8);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return "sign=" + encodedSign;
	}

	/**
	 * 把数组所有元素，并按照“参数=参数值”的模式用“&”字符拼接成字符串
	 * @param params 需要参与字符拼接的参数组
	 * @param sorts 是否需要排序 true 或者 false
	 * @return 拼接后字符串
	 */
	private static String createLinkString(Map<String, String> params) {
		List<String> keys = new ArrayList<String>(params.keySet());
		
		Collections.sort(keys);
		String prestr = "";
		for (int i = 0; i < keys.size(); i++) {
			String key = keys.get(i);
			String value = params.get(key);

			if (i == keys.size() - 1) {// 拼接时，不包括最后一个&字符
				prestr = prestr + key + Constants.EQU + value;
			} else {
				prestr = prestr + key + Constants.EQU + value + Constants.AND;
			}
		}
		return prestr;
	}

    /**
     * 根据反馈回来的信息，生成签名结果
     * @param Params 通知返回来的参数数组
     * @param sign 比对的签名结果
     * @return 生成的签名结果
     */
	public static boolean getSignVeryfy(Map<String, String> params, String sign) {
		String signType = params.get("sign_type");
    	//过滤空值、sign与sign_type参数
    	Map<String, String> sParaNew = paraFilter(params);
        //获取待签名字符串
        String content = createLinkString(sParaNew);
        //获得签名验证结果
        boolean isSign = false;
        if(signType.equals("RSA")){
        	isSign = RSATools.doCheck(content, sign, RSAConfig.ALIPAY_PUBLIC_KEY, Constants.UTF8);
        }
        return isSign;
    }

    /** 
     * 除去数组中的空值和签名参数
     * @param sArray 签名参数组
     * @return 去掉空值与签名参数后的新签名参数组
     */
    private static Map<String, String> paraFilter(Map<String, String> sArray) {

        Map<String, String> result = new HashMap<String, String>();

        if (sArray == null || sArray.size() <= 0) {
            return result;
        }

        for (String key : sArray.keySet()) {
            String value = sArray.get(key);
            if (value == null || value.equals("") || key.equalsIgnoreCase("sign")
                || key.equalsIgnoreCase("sign_type")) {
                continue;
            }
            result.put(key, value);
        }

        return result;
    }
    /**
     * 获取远程服务器ATN结果,验证返回URL
     * @param notify_id 通知校验ID
     * @return 服务器ATN结果
     * 验证结果集：
     * invalid命令参数不对 出现这个错误，请检测返回处理中partner和key是否为空 
     * true 返回正确信息
     * false 请检查防火墙或者是服务器阻止端口问题以及验证时间是否超过一分钟
     */
     public static String verifyResponse(String notifyId, String alipayPartner) {
         //获取远程服务器ATN结果，验证是否是支付宝服务器发来的请求
         String veryfy_url = HTTPS_VERIFY_URL + "partner=" 
        		 + alipayPartner + "&notify_id=" + notifyId;
         return checkUrl(veryfy_url);
     }

     /**
     * 获取远程服务器ATN结果
     * @param urlvalue 指定URL路径地址
     * @return 服务器ATN结果
     * 验证结果集：
     * invalid命令参数不对 出现这个错误，请检测返回处理中partner和key是否为空 
     * true 返回正确信息
     * false 请检查防火墙或者是服务器阻止端口问题以及验证时间是否超过一分钟
     */
     private static String checkUrl(String urlvalue) {
         String inputLine = "";

         try {
             HttpURLConnection conn = 
            		 HttpUtils.createGetHttpConnection(urlvalue, Constants.UTF8);
             inputLine = HttpUtils.returnString(conn);
         } catch (Exception e) {
             inputLine = "";
             LOGGER.error("校验阿里通知失败，请求地址：" + urlvalue + "，" + e.getMessage());
         }

         return inputLine;
     }
    
    
    
}
