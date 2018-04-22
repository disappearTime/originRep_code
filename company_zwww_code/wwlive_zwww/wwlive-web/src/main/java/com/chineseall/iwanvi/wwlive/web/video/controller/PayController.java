package com.chineseall.iwanvi.wwlive.web.video.controller;

import com.chineseall.iwanvi.wwlive.common.check.ResponseResult;
import com.chineseall.iwanvi.wwlive.common.check.ResultMsg;
import com.chineseall.iwanvi.wwlive.web.common.util.ValidationUtils;
import com.chineseall.iwanvi.wwlive.web.event.service.LevelEventService;
import com.chineseall.iwanvi.wwlive.web.video.service.PayService;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.text.ParseException;
import java.util.Map;

@Controller
public class PayController {
	
    static final Logger LOGGER = Logger.getLogger(PayController.class);

	@Autowired
	private PayService payService;
	
	@Autowired
	private LevelEventService levelEventService; // 关卡活动service

	@RequestMapping(value = "/app/user/pay", method = RequestMethod.POST)
	@ResponseBody
	public ResponseResult<Map<String, Object>> pay(HttpServletRequest request)
			throws ParseException {
		
        ResponseResult<Map<String, Object>> result = checkPay(request); //接口校验
        if (result != null) {
        	return result;
        }
		
		Map<String, Object> map = null;
		try {
			map = payService.toPay(request);
		} catch (Exception e) {
			e.printStackTrace();
			LOGGER.error("创建支付订单失败：", e);
			result = new ResponseResult<Map<String, Object>>(ResultMsg.FAIL);
			result.setData(map);
			return result;
		}

		if (map == null || map.isEmpty()) {
			result = new ResponseResult<Map<String, Object>>(ResultMsg.FAIL);
			result.setData(map);
			return result;
		}
		int cnt = (int) map.get("result");
		if (cnt <= 0) {
			result = new ResponseResult<Map<String, Object>>(ResultMsg.FAIL);
			result.setData(map);
			return result;
		}
		result = new ResponseResult<Map<String, Object>>(ResultMsg.SUCCESS);

		// 发送送礼成功后数据变动消息
//		payService.afterPayMsg(request);

//		Long anchorId = Long.valueOf(request.getParameter("anchorId"));// 获取主播当前的关卡数和钻石数
		map.put("diamonds", 0.0D);//levelEventService.getCurDiamonds(anchorId)
		map.put("levels", 0);//levelEventService.getCurLevels(anchorId)
		result.setData(map);
		return result;
	}
	
	/**
	 * 校验支付参数信息
	 * @param request
	 * @return
	 */
    private ResponseResult<Map<String, Object>> checkPay(HttpServletRequest request) {
        ResponseResult<Map<String, Object>> rr = null;
	    //接口校验
        if(!ValidationUtils.isValid(request, "anchorId", "goodsId", "goodsNum")){
        	rr = new ResponseResult<>();
        	rr.setResponseByResultMsg(ResultMsg.COVER_KEY_CHECK_FAILED);
            return rr;
        }
        if (StringUtils.isBlank(request.getParameter("goodsId")) 
        		|| StringUtils.isBlank(request.getParameter("goodsNum")) 
        				|| StringUtils.isBlank(request.getParameter("payType"))
                				|| StringUtils.isBlank(request.getParameter("userId"))
                        				|| StringUtils.isBlank(request.getParameter("videoId"))
                                				|| StringUtils.isBlank(request.getParameter("anchorId"))) {
        	rr = new ResponseResult<>();
            rr.setResponseByResultMsg(ResultMsg.LOST_PARAMS);
            return rr;
        }
        return rr;
    }
	
}
