package com.chineseall.iwanvi.wwlive.web.launch.controller;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSONObject;
import com.chineseall.iwanvi.wwlive.common.check.ResponseResult;
import com.chineseall.iwanvi.wwlive.common.check.ResultMsg;
import com.chineseall.iwanvi.wwlive.web.common.util.JsonUtils;
import com.chineseall.iwanvi.wwlive.web.common.util.ValidationUtils;
import com.chineseall.iwanvi.wwlive.web.launch.service.UserService;
import com.chineseall.iwanvi.wwlive.web.video.service.UserInfoService;

/**
 * 用户controller, 处理获取资料, 禁言等操作
 * @author Niu Qianghong
 *
 */
@Controller("launchUserController")
@RequestMapping("/launch/user")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private UserInfoService userInfoService;
    
    private final Logger log = Logger.getLogger(this.getClass());
    
    private final String PREVIOUS_VERSION = "1.0.0";// 1.0.0版本禁言时长单位为小时
    
    /**
     * 用户禁言
     * @return
     */
    @ResponseBody
    @RequestMapping("/mute")
    public ResponseResult<JSONObject> mute(HttpServletRequest request){
        
        //接口校验, 无额外加密字段, 只校验通用参数anchorId, nonce, requestId
        if(!ValidationUtils.isValidForLaunch(request)){
            return new ResponseResult<>(ResultMsg.COVER_KEY_CHECK_FAILED);
        }
        
        ResponseResult<JSONObject> rr = new ResponseResult<>();
        String videoIdStr = request.getParameter("videoId");
        String anchorIdStr = request.getParameter("anchorId");
        String userIdStr = request.getParameter("userId");
        String loginId = request.getParameter("loginId");
        String durationStr = request.getParameter("duration");//禁言时长, version为1.0.0, 单位为小时; 否则为分钟
        String version = request.getParameter("version");
        try {
            Long userId = Long.valueOf(userIdStr);
            Long anchorId = Long.valueOf(anchorIdStr);
            Integer duration = 0;
            Integer orginDuration = Integer.valueOf(durationStr);
            if(PREVIOUS_VERSION.equals(version)){
                duration = orginDuration * 60 * 60;//禁言时长换算成秒
            } else{
                duration = orginDuration * 60;//禁言时长换算成秒
            }
            Long videoId = Long.valueOf(videoIdStr);
            rr.setData(JsonUtils.toValueOfJsonString(userService.mute(anchorId, videoId, userId, duration,loginId)));
            rr.setResponseByResultMsg(ResultMsg.SUCCESS_);
        } catch (Exception e) {
            rr.setToolTip("参数转换异常, 请稍后联系技术人员~");
            rr.setResponseByResultMsg(ResultMsg.FAIL_);
            rr.setData(new JSONObject());
            if (e instanceof NumberFormatException) {
            	log.error("无法解析数字：", e);
            } else {
                rr.setToolTip(e.getMessage());
                log.error("设置房管", e);
            }
        }
        return rr;
    }
    
    /**
     * 获取用户资料
     * @return
     */
    @ResponseBody
    @RequestMapping("/getinfo")
    public ResponseResult<JSONObject> getInfo(HttpServletRequest request){
        
        //接口校验, 无额外加密字段, 只校验通用参数anchorId, nonce, requestId
        if(!ValidationUtils.isValidForLaunch(request)){
            return new ResponseResult<>(ResultMsg.COVER_KEY_CHECK_FAILED);
        }        
        
        ResponseResult<JSONObject> rr = new ResponseResult<>();
        String viewId = request.getParameter("viewId");
        String anchorIdStr = request.getParameter("anchorId");
        try {
            rr.setData(JsonUtils.toValueOfJsonString(userInfoService.getUserInfoByLoinId(viewId, anchorIdStr, "-1")));
            rr.setResponseByResultMsg(ResultMsg.SUCCESS_);
        } catch (Exception e) {
            rr.setResponseByResultMsg(ResultMsg.FAIL_);
            rr.setData(new JSONObject());
            log.error("获取用户资料失败:", e);
        }
        return rr;
    }
}
