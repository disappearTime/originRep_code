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
import com.chineseall.iwanvi.wwlive.web.common.util.LogUtils;
import com.chineseall.iwanvi.wwlive.web.common.util.ValidationUtils;
import com.chineseall.iwanvi.wwlive.web.launch.service.AdminService;

@Controller
@RequestMapping("/launch/admin")
public class AdminController {

    @Autowired
    private AdminService adminService;

    private Logger logger = Logger.getLogger(this.getClass());
    
    private LogUtils log = new LogUtils(this.getClass());
    
    /**
     * 房管个数无限制 2017-3-20 10:03:53
     * 分页获取房管列表
     * @param request
     * @return
     */
    @ResponseBody
    @RequestMapping("/list")
    public ResponseResult<JSONObject> getAdminList(HttpServletRequest request) {

        // 接口校验, 无额外加密字段, 只校验通用参数anchorId, nonce, requestId
        if (!ValidationUtils.isValidForLaunch(request)) {
            return new ResponseResult<>(ResultMsg.COVER_KEY_CHECK_FAILED);
        }

        ResponseResult<JSONObject> rr = new ResponseResult<>();
        String anchorIdStr = request.getParameter("anchorId");
        String pageSizeStr = request.getParameter("pageSize");
        String pageNoStr = request.getParameter("pageNo");
        try {
            Long anchorId = Long.valueOf(anchorIdStr);
            Integer pageSize = Integer.valueOf(pageSizeStr);
            Integer pageNo = Integer.valueOf(pageNoStr);
          //分页参数判断
            if(pageNo < 1 || pageSize < 0){
                return new ResponseResult<>(ResultMsg.FAIL_);
            }
            rr.setData(JsonUtils.toValueOfJsonString(adminService.getListByAnchorId(anchorId, pageNo, pageSize)));
            rr.setResponseByResultMsg(ResultMsg.SUCCESS_);
        } catch (NumberFormatException e) {
            rr.setResponseByResultMsg(ResultMsg.FAIL_);
            logger.error("无法解析数字：", e);
            rr.setData(new JSONObject());
        }
        return rr;
    }

    /**
     * 解除房管
     * 
     * @param request
     * @return
     */
    @ResponseBody
    @RequestMapping("/remove")
    public ResponseResult<JSONObject> removeAdmin(HttpServletRequest request) {

        log.logParam("移除房管", request, "anchorId", "userId", "chatRoomId");
        
        // 接口校验, 无额外加密字段, 只校验通用参数anchorId, nonce, requestId
        if (!ValidationUtils.isValidForLaunch(request)) {
            return new ResponseResult<>(ResultMsg.COVER_KEY_CHECK_FAILED);
        }

        ResponseResult<JSONObject> rr = new ResponseResult<>();
        String anchorIdStr = request.getParameter("anchorId");
        String userIdStr = request.getParameter("userId");
        String chatRoomId = request.getParameter("chatRoomId");
        try {
            Long anchorId = Long.valueOf(anchorIdStr);
            Long userId = Long.valueOf(userIdStr);
            rr.setData(JsonUtils.toValueOfJsonString(adminService.remove(chatRoomId, anchorId, userId)));
            rr.setResponseByResultMsg(ResultMsg.SUCCESS_);
            log.logResult("移除房管", rr);
        } catch (NumberFormatException e) {
            rr.setData(new JSONObject());
            rr.setResponseByResultMsg(ResultMsg.FAIL_);
            logger.error("无法解析数字：", e);
        }
        return rr;
    }

    /**
     * 设置房管
     * 
     * @param request
     * @return
     */
    @ResponseBody
    @RequestMapping("/set")
    public ResponseResult<JSONObject> setAdmin(HttpServletRequest request) {

        log.logParam("设置房管", request, "anchorId", "userId", "chatRoomId");
        
        // 接口校验, 无额外加密字段, 只校验通用参数anchorId, nonce, requestId
        if (!ValidationUtils.isValidForLaunch(request)) {
            return new ResponseResult<>(ResultMsg.COVER_KEY_CHECK_FAILED);
        }

        ResponseResult<JSONObject> rr = new ResponseResult<>();
        String anchorIdStr = request.getParameter("anchorId");
        String userIdStr = request.getParameter("userId");
        String chatRoomId = request.getParameter("chatRoomId");
        try {
            Long anchorId = Long.valueOf(anchorIdStr);
            Long userId = Long.valueOf(userIdStr);
            rr.setData(JsonUtils.toValueOfJsonString(adminService.set(chatRoomId, anchorId, userId)));
            rr.setResponseByResultMsg(ResultMsg.SUCCESS_);
            log.logResult("设置房管", rr);
        } catch (Exception e) {
            rr.setResponseByResultMsg(ResultMsg.FAIL_);
            rr.setData(new JSONObject());
            if (e instanceof NumberFormatException) {
                logger.error("无法解析数字：", e);
            } else {
                rr.setToolTip(e.getMessage());
                log.logResult("设置房管", rr);
            }
        }
        return rr;
    }
}
