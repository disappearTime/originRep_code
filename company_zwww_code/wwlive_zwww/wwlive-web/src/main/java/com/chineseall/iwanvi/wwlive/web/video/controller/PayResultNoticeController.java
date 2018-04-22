package com.chineseall.iwanvi.wwlive.web.video.controller;

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

import com.chineseall.iwanvi.wwlive.common.check.ResponseResult;
import com.chineseall.iwanvi.wwlive.common.check.ResultMsg;
import com.chineseall.iwanvi.wwlive.domain.wwlive.OrderInfo;
import com.chineseall.iwanvi.wwlive.web.common.embedding.DataEmbeddingTools;
import com.chineseall.iwanvi.wwlive.web.common.pay.alipay.UserAliPay;
import com.chineseall.iwanvi.wwlive.web.common.util.ValidationUtils;
import com.chineseall.iwanvi.wwlive.web.video.service.PayResultNoticeService;

@Controller
public class PayResultNoticeController {

	private static final Logger LOGGER = Logger.getLogger(PayController.class);
	
	@Autowired
	private PayResultNoticeService payResultNoticeService;
	
	/**
	 * 支付宝合作身份者ID，签约账号
	 */
	@Value("${alipay.partner}")
	private String alipayPartner;
	
	@RequestMapping(value = "/external/zfb/notify", method = RequestMethod.POST)
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
			
			OrderInfo order = payResultNoticeService.updateOrderFromZFB(tradeStatus, outTradeNo, tradeNo);
			if (1 == order.getOrderStatus()) {
				DataEmbeddingTools.insertLog("7004", "1-1", 
				        order.getReceiverKey().toString(), order.getOriginKey().toString(), request);//埋点
			}
			
			return "success";
		} else {
			return "sign fail";
		}
		
	}
	
	/**
	 * 中文书城微信通知查询
	 * @param request
	 * @throws ParseException
	 */
	@RequestMapping(value = "/app/user/wxquery", method = {RequestMethod.POST, RequestMethod.GET})
	@ResponseBody
	public ResponseResult<Integer> wxNotifyQuery(HttpServletRequest request) throws ParseException {
		String fog = request.getParameter("fog");
		Integer data = new Integer(0);
		ResponseResult<Integer> result = new ResponseResult<Integer>();

        if(!ValidationUtils.isValid(request)){
        	result.setResponseByResultMsg(ResultMsg.COVER_KEY_CHECK_FAILED);
            return result;
        }
        
		if (StringUtils.isBlank(fog)) {
			ResultMsg fail = ResultMsg.FAIL;
			fail.setInfo("fog不能存在");
			result = new ResponseResult<Integer>(fail);
			result.setData(data);
			return result;
		}
		//查询
		data = new Integer(payResultNoticeService.wxNotifyQuery(fog));
		result.setResponseByResultMsg(ResultMsg.SUCCESS);
		result.setData(data);
		return result;
		
	}

	/**
	 * 充值后通知查询
	 * @param request
	 * @throws ParseException
	 */
	@RequestMapping(value = "/app/user/recharge/query", method = {RequestMethod.POST, RequestMethod.GET})
	@ResponseBody
	public ResponseResult<Integer> notifyQuery(HttpServletRequest request) throws ParseException {
		String fog = request.getParameter("fog");
		Integer data = new Integer(0);
		ResponseResult<Integer> result = new ResponseResult<Integer>();

        if(!ValidationUtils.isValid(request)){
        	result.setResponseByResultMsg(ResultMsg.COVER_KEY_CHECK_FAILED);
            return result;
        }
        
		if (StringUtils.isBlank(fog)) {
			ResultMsg fail = ResultMsg.FAIL;
			fail.setInfo("fog不能存在");
			result = new ResponseResult<Integer>(fail);
			result.setData(data);
			return result;
		}
		//查询
		data = new Integer(payResultNoticeService.wxNotifyQuery(fog));
		result.setResponseByResultMsg(ResultMsg.SUCCESS);
		result.setData(data);
		return result;
		
	}
	
	/**
	 * 中文书城微信通知
	 * @param request
	 * @throws ParseException
	 */
	@RequestMapping(value = "/external/wx/notify", method = {RequestMethod.POST, RequestMethod.GET})
	@ResponseBody
	public String wxNotify(HttpServletRequest request) throws ParseException {
	    
		Map<String, String> params = getZfbParams(request);

		String orderId = request.getParameter("orderid");
		if (StringUtils.isEmpty(orderId)) {
			return "fail";
		}
		
		LOGGER.info("微信通知参数" + params.toString());
		String amount = request.getParameter("amount");
		String transactionId = request.getParameter("transactionid");
		OrderInfo order = payResultNoticeService.updateOrderFromWX(amount, orderId, transactionId);
		if (1 == (order.getOrderStatus() == null ? 0 : order.getOrderStatus())) {
	        DataEmbeddingTools.insertLog("7004", "1-1", 
	                order.getReceiverKey().toString(), order.getOriginKey().toString(), request);//埋点
		}
		return "success";
		
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

	
	
}
