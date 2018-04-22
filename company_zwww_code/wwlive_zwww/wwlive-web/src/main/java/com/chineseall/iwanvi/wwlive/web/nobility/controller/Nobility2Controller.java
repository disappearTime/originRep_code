package com.chineseall.iwanvi.wwlive.web.nobility.controller;

import com.alibaba.fastjson.JSONObject;
import com.chineseall.iwanvi.wwlive.common.check.ResponseResult;
import com.chineseall.iwanvi.wwlive.common.check.ResultMsg;
import com.chineseall.iwanvi.wwlive.web.common.util.ControllerRequestUtils;
import com.chineseall.iwanvi.wwlive.web.common.util.RegexUtils;
import com.chineseall.iwanvi.wwlive.web.follow.service.UserService;
import com.chineseall.iwanvi.wwlive.web.nobility.service.Nobility2Service;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * Created by Niu Qianghong on 2017-07-11 0011.
 */
@Controller
public class Nobility2Controller {

    @Autowired
    private Nobility2Service nobility2Service;
    @Autowired
    private UserService userService;

    static final Logger LOGGER = Logger.getLogger(Nobility2Controller.class);

    @RequestMapping("/external/app/user/check")
    @ResponseBody
    public ResponseResult<Map<String, Object>> checkExpire(HttpServletRequest request){
        ResponseResult<Map<String, Object>> rr = new ResponseResult<>();
        try {
            Long userId = 0l;
            String loginId = request.getParameter("loginId");
            String strUid = request.getParameter("userId");
            if(StringUtils.isNotBlank(loginId)){
                userId = userService.getUserIdByLogin(loginId);
            }
            if (StringUtils.isNotBlank(strUid) && RegexUtils.isNum(strUid)) {
            	userId = Long.valueOf(strUid);
            }
            Map<String, Object> data = nobility2Service.checkExpire(userId);
            rr.setResponseByResultMsg(ResultMsg.SUCCESS);
            rr.setData(data);
        } catch (Exception e) {
            LOGGER.error("检查贵族是否过期异常, userId = " + request.getParameter("userId"), e);
            rr.setResponseByResultMsg(ResultMsg.FAIL);
        }
        return rr;
    }

    /**
     * 贵族购买
     * @param request
     * @return
     */
    @RequestMapping("/external/app/noble/purchase")
    @ResponseBody
    public ResponseResult<Map<String, Object>> noblePurchase(HttpServletRequest request){
        ResponseResult<Map<String, Object>> rr = new ResponseResult<>();
        try {
        	String userIdStr = request.getParameter("userId");
        	if (StringUtils.isBlank(userIdStr)) {
                rr.setResponseByResultMsg(ResultMsg.FAIL);
                rr.setInfo("用户id为空");
        		return rr;
        	}
            Long userId = Long.valueOf(userIdStr);
            Long goodsId = ControllerRequestUtils.parseLongFromRquest(request, "goodsId");
            
            Long videoId = ControllerRequestUtils.parseLongFromRquest(request, "videoId");
            Long anchorId = ControllerRequestUtils.parseLongFromRquest(request, "anchorId");
            Integer way = ControllerRequestUtils.parseIntFromRquest(request, "way");
            Integer payType = ControllerRequestUtils.parseIntFromRquest(request, "payType");
            
            JSONObject data = nobility2Service.noblePurchase(userId, videoId, anchorId, 
            		 way, goodsId, payType);
            rr.setResponseByResultMsg(ResultMsg.SUCCESS);
            rr.setData(data);
        } catch (Exception e) {
            LOGGER.error("贵族购买异常： ", e);
            rr.setResponseByResultMsg(ResultMsg.FAIL);
        }
        return rr;
    }
    
}
