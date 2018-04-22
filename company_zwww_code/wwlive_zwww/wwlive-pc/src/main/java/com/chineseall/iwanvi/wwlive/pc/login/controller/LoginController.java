package com.chineseall.iwanvi.wwlive.pc.login.controller;

import com.chineseall.iwanvi.wwlive.pc.common.AjaxJson;
import com.chineseall.iwanvi.wwlive.pc.common.RequestUtils;
import com.chineseall.iwanvi.wwlive.pc.common.WebConstants;
import com.chineseall.iwanvi.wwlive.pc.common.exception.LoginException;
import com.chineseall.iwanvi.wwlive.pc.common.loginContext.LoginUser;
import com.chineseall.iwanvi.wwlive.pc.login.service.LoginService;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Created by kai on 16/8/24.
 */
@Controller
@RequestMapping("/login")
public class LoginController {

    private static final Logger LOGGER = Logger.getLogger(LoginController.class);

    @Autowired
    private LoginService loginService;

    @RequestMapping("/in")
    public String toLogin(HttpServletRequest request, HttpServletResponse response, Model model) throws IOException {

        //校验是否存在cookie
        //校验cookie里用户的有效性
        //有效跳转到首页，无效跳转到登录页

        String returnURL = RequestUtils.getStringParamDef(request, "returnUrl", WebConstants.Login.INDEX_URL);

        LOGGER.info("toLogin()...returnUrl:" + returnURL);

        try {

            //验证不通过会抛出异常
            LoginUser loginUser = loginService.validLogin(request, response);

            if (loginUser != null) {
                //登录状态校验通过，跳转首页
                response.sendRedirect(returnURL);
            }


        } catch (LoginException e) {
            LOGGER.error("登录失败：" + e.getMessage());
        }
        model.addAttribute("returnUrl", returnURL);

        return "login";
    }

    @RequestMapping("/out")
    public String loginOut(HttpServletRequest request, HttpServletResponse response) throws IOException {

        loginService.loginOut(request, response);

        response.sendRedirect(WebConstants.Login.LOGIN_URL);

        return null;
    }

    @RequestMapping("/noaccess")
    public String noAccess(HttpServletRequest request, HttpServletResponse response) {

        return "noaccess";
    }


    @RequestMapping(value = "/dologin", method = RequestMethod.POST)
    @ResponseBody
    public AjaxJson doLogin(
            String uname, String passwd,
            HttpServletRequest request, HttpServletResponse response) {

        AjaxJson ajaxJson = new AjaxJson();
        try {

            //登录失败抛出异常
            loginService.login(uname, passwd,response);


            ajaxJson.setSuccess(true);
            ajaxJson.setMsg("登录成功" );
        } catch (Exception ex) {

            LOGGER.error("登录失败: uname=" + uname + ex.getMessage(), ex);
            ajaxJson.setSuccess(false);
            ajaxJson.setMsg(ex.getMessage());

        }


        return ajaxJson;
    }

}
