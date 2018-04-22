package com.chineseall.iwanvi.wwlive.web.event.controller;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.chineseall.iwanvi.wwlive.common.check.ResponseResult;
import com.chineseall.iwanvi.wwlive.common.check.ResultMsg;
import com.chineseall.iwanvi.wwlive.web.common.util.ControllerRequestUtils;
import com.chineseall.iwanvi.wwlive.web.common.util.ValidationUtils;
import com.chineseall.iwanvi.wwlive.web.event.service.RedEnvelopeService;


@Controller
public class RedEnvelopeController {

	public static final Logger LOGGER = Logger.getLogger(RedEnvelopeController.class);
	
	@Autowired
	RedEnvelopeService redEnvelopeService;
	
    /**
     * 
     * @return
     */
    @RequestMapping("/app/redenvelope/luck")
    @ResponseBody
    public ResponseResult<Map<String, Object>> luckyDiamonds(HttpServletRequest request){
        Long userId = ControllerRequestUtils.parseLongFromRquest(request, "userId");
    	String reKey = request.getParameter("reKey");
    	ResponseResult<Map<String, Object>> rr = new ResponseResult<>();
		String requestId = request.getParameter("requestId");
		rr.setRequestId(requestId);
    	if (!ValidationUtils.isValid(request)) {
			rr.setResponseByResultMsg(ResultMsg.COVER_KEY_CHECK_FAILED);
			return rr;
		}
        try {
            Map<String, Object> data = redEnvelopeService.getLuckyDiamond(reKey, userId);//获得钻石
			rr.setResponseByResultMsg(ResultMsg.SUCCESS);
			rr.setData(data);
        } catch (Exception e) {
        	LOGGER.error("抢红包发生异常：", e);
        	Map<String, Object> data = new HashMap<String, Object>();
        	data.put("luckyResult", new Integer(0));
        	data.put("luckyDiamonds", new Integer(0));
			rr.setResponseByResultMsg(ResultMsg.FAIL);
			rr.setData(data);
        }
		return rr;
        
    }
}
