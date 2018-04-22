package com.chineseall.iwanvi.wwlive.pc.login.service;

import java.util.Map;

import com.chineseall.iwanvi.wwlive.pc.common.exception.LoginException;
import com.chineseall.iwanvi.wwlive.pc.common.loginContext.LoginUser;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by kai on 16/8/25.
 */
public interface LoginService {


    /**
     * 校验登录 ,成功返回loginUser
     * cookie里没有登录key返回空
     * @param
     * @return
     */
    LoginUser validLogin(HttpServletRequest request,HttpServletResponse response) throws LoginException;

    LoginUser login(String name, String pass,HttpServletResponse response) throws LoginException;

    void loginOut(HttpServletRequest request, HttpServletResponse response);
    
    LoginUser getLoginUser(String loginCookie);

    Map<String, String> getAnchorIncomeVideoCnt(long anchorId);
}
