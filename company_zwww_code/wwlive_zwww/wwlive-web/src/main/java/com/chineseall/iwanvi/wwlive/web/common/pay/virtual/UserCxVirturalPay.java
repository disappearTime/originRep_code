package com.chineseall.iwanvi.wwlive.web.common.pay.virtual;

import static com.chineseall.iwanvi.wwlive.common.constants.Constants.AND;
import static com.chineseall.iwanvi.wwlive.common.constants.Constants.EQU;

import java.net.HttpURLConnection;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSONObject;
import com.chineseall.iwanvi.wwlive.common.cache.RedisClientAdapter;
import com.chineseall.iwanvi.wwlive.common.cache.RedisKey;
import com.chineseall.iwanvi.wwlive.common.constants.Constants;
import com.chineseall.iwanvi.wwlive.common.external.rongcloud.domain.SdkHttpResult;
import com.chineseall.iwanvi.wwlive.common.tools.Base64Tools;
import com.chineseall.iwanvi.wwlive.common.tools.HttpUtils;
import com.chineseall.iwanvi.wwlive.dao.wwlive.UserInfoMapper;
import com.chineseall.iwanvi.wwlive.domain.wwlive.UserInfo;
import com.chineseall.iwanvi.wwlive.web.common.helper.UserInfoHelper;

/**
 * 创新版用户用虚拟货币消费
 * @author DIKEPU
 * @version 1.0
 * @since 2016-09-21
 */
@Component
public class UserCxVirturalPay {
	private static final Logger LOGGER = Logger.getLogger(UserCxVirturalPay.class);

	/**
	 * 创新版
	 */
	@Value("${user.pay.on.cx.virtual}")
	private String cxPayUrl;

	@Value("${userinfo.cx.uri}")
	private String userInfoCxURL;
	
    @Autowired
    private UserInfoMapper userInfoMapper;
    
    @Autowired
    private RedisClientAdapter redisAdapter;

    @Value("${cx.ratio}")
    private String cxRatio;
    
    /**
     * 0 成功 ,1失败
     * @param userId
     * @param virtualCurrency
     * @return
     */
	public int updateUserVirtural(Long userId, long virtualCurrency) {
		// 1.更新创新版用户积分 2.更新user信息 3.更新redis信息
		String loginId = "";
		long currency = 0L;
		Map<String, Object> userInfo = userInfoMapper.findVirtualCurrencyById(userId);
    	loginId = (String) userInfo.get("loginId");
    	if (userInfo.get("virtualCurrency") != null) {
        	currency = (Long) userInfo.get("virtualCurrency");
    	}
		if (currency < virtualCurrency) {
			return 1;
		}
        if (!beforeCxPayCheck(virtualCurrency, currency, userId)) {
			return 1;
		}
		if (createPayUrl(loginId, -virtualCurrency, userId) < 1) {
			long surplus = currency - (long)virtualCurrency;
			long real = getVirtual(loginId);
			if (real != surplus) {
				surplus = real;
			}
			int update = userInfoMapper.updateVirtualCurrencyById(userId, surplus);
			if (update > 0) {
				Map<String, Object> user = new HashMap<String, Object>();
				user.put("virtualCurrency", surplus);
				if (redisAdapter.existsKey(RedisKey.USER_INFO_ + userId)) {
					redisAdapter.hashMSet(RedisKey.USER_INFO_
							+ userId, user);
				}
			}
		} else {
			return 1;
		}
        return 0;
	}
	
	private int createPayUrl(String loginId, long virtualCurrency, Long userId) {
		 try {
			 	if (loginId.contains(Constants.UNDERLINE)) {
			 		loginId = loginId.split(Constants.UNDERLINE)[0];
			 	}
			 	String inteStr = URLEncoder.encode("直播消费", Constants.UTF8);
		    	String sign = new String(Base64Tools.encode((loginId + virtualCurrency + inteStr).getBytes()));

				StringBuilder sbUrl = new StringBuilder("uid").append(EQU).append(loginId)
						.append(AND).append("consume").append(EQU).append(virtualCurrency)
						.append(AND).append("inteStr").append(EQU).append(inteStr)
						.append(AND).append("sign").append(EQU).append(sign);
	        	HttpURLConnection conn = HttpUtils.createPostHttpConnection(cxPayUrl.toString());
	        	SdkHttpResult result = HttpUtils.returnResult(conn, sbUrl.toString());
	        	String code = null;
	        	if (result.getResult() != null 
	        			&& StringUtils.isNotBlank(result.getResult())) {
	        		JSONObject json = JSONObject.parseObject(result.getResult());
	        		code = json.getString("code");
	        	}
	        	if ("0".equals(code)) {
	        		return 0;
	        	} else if("1".equals(code)) {
	    			LOGGER.error("请求创新版积分更新接口失败userId：" + userId);
	        	}
			} catch (Exception e) {
				LOGGER.error("请求创新版积分更新接口异常：" + e.toString());
			}
	        return 1;
	}
	
	private long getVirtual(String loginId) {
		try {
			if (loginId.contains(Constants.UNDERLINE)) {
		 		loginId = loginId.split(Constants.UNDERLINE)[0];
		 	}
			HttpURLConnection conn = HttpUtils.createGetHttpConnection(userInfoCxURL + loginId, Constants.UTF8);
			SdkHttpResult result = HttpUtils.returnResult(conn);
			if(result.getHttpCode() == 200) {
				String strData = result.getResult();
				JSONObject userJson = JSONObject.parseObject(strData);
				if (userJson != null){
					String data = userJson.getString("data");
					JSONObject json = JSONObject.parseObject(data);
					String userPoints = json.getString("userPoints");
					return (long) (userPoints == null ? 0L : Long.parseLong(userPoints));
				}
			}
		} catch (Exception e) {
			LOGGER.error("获取创新版用户信息失败." + e.toString());
		}
		return 0L;
	}
	
	/**
	 * 创新版支付前检查用户自身的虚拟货币是否充足
	 * 
	 * @param virtualCurrency
	 *            要花费的虚拟货币
	 * @param currency
	 *            自身携带的虚拟货币
	 * @param userId
	 *            用户id
	 * @return true充足 false不充足
	 */
	private boolean beforeCxPayCheck(long virtualCurrency, Long currency,
			Long userId) {
		if (currency == null) {// 获得虚拟货币信息
			UserInfo user = UserInfoHelper.getAndCacheUserInfo(redisAdapter,
					userInfoMapper, userId);
			if (user == null) {
				return false;
			}
			if (user.getVirtualCurrency() == null 
					|| user.getVirtualCurrency() == 0) {
				return false;
			}
			currency = user.getVirtualCurrency();
		} 
		if (currency.intValue() < virtualCurrency) {
			return false;
		}
		return true;
	}
	
}
