package com.chineseall.iwanvi.wwlive.web.my.controller;

import java.io.InputStream;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.chineseall.iwanvi.wwlive.web.common.pay.alipay.UserAliPay;
import com.chineseall.iwanvi.wwlive.web.common.util.WCPayUtils;
import com.chineseall.iwanvi.wwlive.web.my.service.ReChargeNoticeService;

@Controller
public class ReChargeNoticeController {

	private static final Logger LOGGER = Logger.getLogger(ReChargeNoticeController.class);
	
    @Value("${wechat.pay.apikey}")
    private String wxAPIKey;

    private static final String WX_SUCCESS_CODE = "SUCCESS";

    private static final String WX_SUCCESS_XML = "<xml><return_code><![CDATA[SUCCESS]]></return_code><return_msg><![CDATA[OK]]></return_msg></xml>";

    private static final String WX_FAIL_XML = "<xml><return_code><![CDATA[FAIL]]></return_code></xml>";

    // private static final String WX_FAIL_CODE = "FAIL";
	
	/**
	 * 支付宝合作身份者ID，签约账号
	 */
	@Value("${alipay.partner}")
	private String alipayPartner;

	@Autowired
	private ReChargeNoticeService reChargeNoticeService;
	
	@RequestMapping(value = "/external/zfb/recharge/notify", method = RequestMethod.POST)
	@ResponseBody
	public String zfbNotify(HttpServletRequest request) throws ParseException {
	    
		Map<String, String> params = getZfbParams(request);

		//sign
		String notifyId = request.getParameter("notify_id");
		if (StringUtils.isEmpty(notifyId)) {
			return "no notify message";
		}
		//判断成功之后使用getResponse方法判断是否是支付宝发来的异步通知。
		
		String sign=request.getParameter("sign");
		if (!UserAliPay.verifyResponse(notifyId, alipayPartner).equals("true")) {
			return "response fail";
		}
		
		if (UserAliPay.getSignVeryfy(params, sign)) {
			LOGGER.info("支付宝异步通知参数详情，params：" + params);
			String tradeStatus = request.getParameter("trade_status");
			String tradeNo = request.getParameter("trade_no");
			String outTradeNo = request.getParameter("out_trade_no");
			
			int cnt = reChargeNoticeService.updateReChargeFromZFB(tradeStatus, outTradeNo, tradeNo);
			if (1 == cnt) {
				return "success";
			}
			
		} else {
			return "sign fail";
		}
		return "fail";
		
	}

	@SuppressWarnings("rawtypes")
	private Map<String, String> getZfbParams(HttpServletRequest request) {
		// 获取支付宝POST过来反馈信息
		Map<String, String> params = new HashMap<String, String>();
		Map requestParams = request.getParameterMap();
		for (Iterator iter = requestParams.keySet().iterator(); iter.hasNext();) {
			String name = (String) iter.next();
			String[] values = (String[]) requestParams.get(name);
			String valueStr = "";
			for (int i = 0; i < values.length; i++) {
				valueStr = (i == values.length - 1) ? valueStr + values[i]
						: valueStr + values[i] + ",";
			}
			params.put(name, valueStr);
		}

		return params;
	}
	
	/**
     * 微信SDK支付回调
     * 2017-5-24 14:41:44 NiuQianghong
     * @param request
     */
    @RequestMapping("/external/wxsdk/recharge/notify")
    @ResponseBody
    public String wxSDKNotify(HttpServletRequest request){      
        boolean success = false;
        try {
            InputStream in = request.getInputStream();
            Map<String, String> notifyMap = WCPayUtils.getParamsMapFromXml(in);
            LOGGER.info("微信充值--回调通知, 结果:" + notifyMap.toString());
            
            String outTradeNo = notifyMap.get("out_trade_no");// 外部订单号
            String receiverId = notifyMap.get("transaction_id");// 微信订单号
            
            if(WX_SUCCESS_CODE.equals(notifyMap.get("return_code"))){
                notifyMap.put("key", wxAPIKey);
                if(WCPayUtils.checkSign(notifyMap)){// 签名校验 
                    if(reChargeNoticeService.isDealt(outTradeNo)){
                        LOGGER.info("微信充值--回调通知, 该订单已处理过. 外部订单号 = " + outTradeNo);
                        return WX_SUCCESS_XML;// 已处理过直接返回成功
                    }
                    
                    success = true;
                    int cnt = reChargeNoticeService.updateRechargeFromWX(success, outTradeNo, receiverId);// 更新充值信息
                    return cnt== 1 ? WX_SUCCESS_XML : WX_FAIL_XML;
                }                
            }
            
            LOGGER.info("微信充值--回调通知, 签名校验失败");
            reChargeNoticeService.updateRechargeFromWX(success, outTradeNo, receiverId);
            return WX_FAIL_XML;
        } catch (Exception e) {
            LOGGER.error("微信充值--回调通知异常", e);
            return WX_FAIL_XML;
        }
    }
}
