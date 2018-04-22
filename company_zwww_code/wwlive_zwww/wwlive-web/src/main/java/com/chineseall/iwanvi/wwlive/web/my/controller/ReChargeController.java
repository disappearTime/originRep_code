package com.chineseall.iwanvi.wwlive.web.my.controller;

import com.chineseall.iwanvi.wwlive.common.check.ResponseResult;
import com.chineseall.iwanvi.wwlive.common.check.ResultMsg;
import com.chineseall.iwanvi.wwlive.common.constants.Constants;
import com.chineseall.iwanvi.wwlive.domain.wwlive.RechargeInfo;
import com.chineseall.iwanvi.wwlive.web.common.embedding.DataEmbeddingTools;
import com.chineseall.iwanvi.wwlive.web.common.helper.PageUrlHelper;
import com.chineseall.iwanvi.wwlive.web.common.util.ControllerRequestUtils;
import com.chineseall.iwanvi.wwlive.web.common.util.RegexUtils;
import com.chineseall.iwanvi.wwlive.web.common.util.ValidationUtils;
import com.chineseall.iwanvi.wwlive.web.my.service.ReChargeService;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@Controller
public class ReChargeController {

    private final Logger LOGGER = Logger
            .getLogger(this.getClass());
    
    @Autowired
    private ReChargeService reChargeService;

    /**
     * 微信、支付宝充值页面
     * @param request
     * @return
     */
    @RequestMapping(value = "/app/my/rechage/page", method = RequestMethod.GET)
    public ModelAndView rechageList(HttpServletRequest request) {
    	ModelAndView model = new ModelAndView("my/recharge");
    	if(!ValidationUtils.isValid(request)){
            return model;
        }
    	Map<String, String> params = ControllerRequestUtils.getParam(request);
        model.addObject("params", params);
        if (params != null && params.containsKey("target")) {
        	params.remove("target");
        }
        try {
        	model.addObject("exchargeUrl", PageUrlHelper.buildCommonUrl(params));
        	model.addObject("userId", request.getParameter("userId"));
            model.addAllObjects(reChargeService.getRechageList());
        } catch (Exception e) {
        	e.printStackTrace();
        	LOGGER.error(e.toString());
        }
        return model;
    }

    /**
     * 积分兑换页面
     * @param request
     * @return
     */
    @RequestMapping(value = "/app/my/exchange/page", method = RequestMethod.GET)
    public ModelAndView exchangePage(HttpServletRequest request) {
    	ModelAndView model = new ModelAndView("my/excharge");
    	if(!ValidationUtils.isValid(request)){
            return model;
        }
    	Long userId = Long.parseLong(request.getParameter("userId"));
        try {//当用户为其他用户时是否还能打开此页面？
        	Map<String, String> params = ControllerRequestUtils.getParam(request);
        	if (params != null && params.containsKey("target")) {
            	params.remove("target");
            }
        	model.addObject("chargeUrl", PageUrlHelper.buildCommonUrl(params));
        	model.addObject("userId", request.getParameter("userId"));
        	params.putAll(reChargeService.exchangePage(userId));
            model.addObject("params", params);
        } catch (Exception e) {
        	LOGGER.error(e.getCause());
        }
        return model;
    }
    
    /**
     * 充值、兑换
     * @param request
     * @return
     */
	@RequestMapping(value = "/app/my/rechage", method = RequestMethod.POST)
    @ResponseBody
	public ResponseResult<Map<String, Object>> rechage(HttpServletRequest request) {
	    if(!ValidationUtils.isValid(request)){
            return new ResponseResult<>(ResultMsg.COVER_KEY_CHECK_FAILED);
        }
		String requestId = request.getParameter("requestId");
		ResponseResult<Map<String, Object>> result = checkRechage(request); //接口校验
        if (result != null) {
        	result.setRequestId(requestId);
        	return result;
        }
		
		Map<String, Object> map = null;
        Integer way = Integer.valueOf(request.getParameter("way"));
		
		try {
			long goodsId = Long.parseLong(request.getParameter("goodsId"));
			int amt = Integer.parseInt(request.getParameter("amt"));
			
			RechargeInfo info = buildRechargeInfo(request);
			map = reChargeService.reCharge(goodsId, info, amt);
			
			pollingUrl(info, map, request);
		} catch (Exception e) {
			e.printStackTrace();
			LOGGER.error("创建支充值信息失败：" + e.toString());
			result = new ResponseResult<Map<String, Object>>(ResultMsg.FAIL);
        	result.setRequestId(requestId);
			return result;
		}

		if (map == null || map.isEmpty()) {
			result = new ResponseResult<Map<String, Object>>(ResultMsg.FAIL);
        	result.setRequestId(requestId);
			return result;
		}
		int cnt = (int) map.get("result");
		if (cnt <= 0) {
			result = new ResponseResult<Map<String, Object>>(ResultMsg.FAIL);
			result.setData(map);
        	result.setRequestId(requestId);
			return result;
		}

		/*// 带有dz标识的链接才同步数据
		String app = request.getParameter("app");
		LOGGER.info("定制版调试: 处理后的充值map = " + map);
		if ("dz".equals(app)) {
			// 同步充值信息
			reChargeService.syncDataWithDZ(map);
		}*/

		result = new ResponseResult<Map<String, Object>>(ResultMsg.SUCCESS);
		result.setData(map);
    	result.setRequestId(requestId);
		return result;
		
	}

    private ResponseResult<Map<String, Object>> checkRechage(HttpServletRequest request) {
        ResponseResult<Map<String, Object>> rr = null;
        String goodsId = request.getParameter("goodsId");
        String chargeType = request.getParameter("chargeType");
        String amt = request.getParameter("amt");
        String way = request.getParameter("way");
        if (StringUtils.isBlank(goodsId) 
        		|| StringUtils.isBlank(chargeType)
        		|| StringUtils.isBlank(amt)
        		|| StringUtils.isBlank(way)) {
        	rr = new ResponseResult<>();
            rr.setResponseByResultMsg(ResultMsg.LOST_PARAMS);
            return rr;
        }
        if (!RegexUtils.isNum(goodsId) 
        		|| !RegexUtils.isNum(chargeType) 
        		|| !RegexUtils.isNum(amt)
        		|| !RegexUtils.isNum(way) || Integer.parseInt(amt) <= 0) {
        	rr = new ResponseResult<>();
            rr.setResponseByResultMsg(ResultMsg.INCORRECT_FORMAT);
            return rr;
        	
        }
        return rr;
    }
    
    private RechargeInfo buildRechargeInfo(HttpServletRequest request) {

		long userId = Long.parseLong(request.getParameter("userId"));
		int way = Integer.parseInt(request.getParameter("way"));
		String str = request.getParameter("anchorId");
		int chargeType = Integer.parseInt(request.getParameter("chargeType"));
		Long anchorId = null;
		if (StringUtils.isNotBlank(str) && way == 1) {
			anchorId = Long.parseLong(request.getParameter("anchorId"));
		}
		RechargeInfo info = new RechargeInfo();
		info.setUserId(userId);
		info.setRechargeType(Constants._0);//0-钻石
		info.setRechargeStatus(Constants._0);//0未支付，1成功，2失败
		//outTradeNo receiveNo 

		info.setOrigin(chargeType);//充值来源
		info.setAnchorId(anchorId);
		info.setWay(way);

		if (chargeType == 1) {
			DataEmbeddingTools.insertLog("7006", "1-2", userId + "", "", request);
		} else {
			if (StringUtils.isNotBlank(str) && way == 1) {//直播间
				DataEmbeddingTools.insertLog("7006", "1-1", userId + "", "", request);
			} else {//我的页面
				DataEmbeddingTools.insertLog("7006", "1-3", userId + "", "", request);
			}
			
		}
		
		// 设置ip
		info.setIp(DataEmbeddingTools.getIpAddress(request));
		return info;
	}
    
    /**
     * 轮询机制url
     * @param info
     * @param map
     * @param request
     */
    private void pollingUrl(RechargeInfo info, Map<String, Object> map, HttpServletRequest request) {
    	if (info.getOrigin().intValue() != Constants._1) {
    		if (map != null && !map.isEmpty()) {//开启微信轮询查询地址生成
        		String fog = (String) map.get("fog");
            	Map<String, String> params = ControllerRequestUtils.getCommonParam(request);
            	params.put("fog", fog);
            	String wxQuery = PageUrlHelper.buildCommonUrl(params);
            	String rechagePage = PageUrlHelper.buildCommonUrl(params);
            	map.put("query", wxQuery);
            	map.put("rechagePage", rechagePage);
    		}
    	}
    }
    
    @RequestMapping("/app/my/recharge/wx/query")
    @ResponseBody
    public ResponseResult<Map<String, Object>> wxRechargeQuery(HttpServletRequest request){
        
        ResponseResult<Map<String, Object>> rr = new ResponseResult<>();
        
        String outTradeNo = request.getParameter("outTradeNo");
        if(StringUtils.isBlank(outTradeNo)){
            rr.setResponseByResultMsg(ResultMsg.FAIL);
            return rr;
        }
        
        boolean isPaid = reChargeService.isPaid(outTradeNo);
        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put("result", isPaid ? 1 : 0);
        rr.setResponseByResultMsg(ResultMsg.SUCCESS);
        rr.setData(dataMap);
        LOGGER.info("微信充值--APP查询, outTradeNo = " + outTradeNo + ", success = " + isPaid);
        return rr;
    }
}
