package com.chineseall.iwanvi.wwlive.web.otherapp.controller;

import com.chineseall.iwanvi.wwlive.domain.wwlive.UserInfo;
import com.chineseall.iwanvi.wwlive.web.otherapp.service.DZUserInfoService;
import com.mongodb.util.JSON;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Niu Qianghong on 2017-08-23 0023.
 */
@Controller
public class DZUserController {

    private Logger logger = Logger.getLogger(this.getClass());

    @Autowired
    private DZUserInfoService userInfoService;

    /**
     * 返回 {"result", 0}格式json, 0-无重复昵称, >0 表示有重复昵称, =-1 表示发生异常
     * @param request
     * @return
     */
    @RequestMapping("/external/dz/query/nickname/cnt")
    @ResponseBody
    public Map<String, Object> queryRepeatedNickname(HttpServletRequest request) {
        Map<String, Object> result = new HashMap<>();
        try {
            String nickname = request.getParameter("nickname");
            nickname = URLDecoder.decode(nickname, "utf-8");
            logger.info("修改昵称: nickname = " + nickname);
            Integer userCnt = userInfoService.getNameCnt(nickname);
            result.put("cnt", userCnt);
        } catch (Exception e) {
            result.put("cnt", -1);
        }
        return result;
    }

    @RequestMapping("/external/dz/user/login")
    @ResponseBody
    public UserInfo addUserFromDz(HttpServletRequest request) throws UnsupportedEncodingException {
        String userInfo = request.getParameter("userInfo");
        logger.info("dz: userInfo = " + userInfo);
        Map dzUser = (Map)JSON.parse(userInfo);
        UserInfo user = userInfoService.addUser(dzUser);
        return user;
    }

    @RequestMapping("/external/dz/user/modify")
    @ResponseBody
    public Map<String, Object> syncUserInfo(HttpServletRequest request) throws UnsupportedEncodingException {
        String newInfo = request.getParameter("userInfo");
        logger.info("修改的用户信息: userInfo = " + newInfo);
        //newInfo = new String(newInfo.getBytes("iso8859-1"), "utf8");
        Map dzUser = (Map)JSON.parse(newInfo);
        int cnt = userInfoService.modifyInfo(dzUser);
        Map<String, Object> result = new HashMap<>();
        result.put("cnt", cnt);
        return result;
    }
}
