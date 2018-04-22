package com.chineseall.iwanvi.wwlive.pc.login.interceptor;

import java.io.IOException;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import com.chineseall.iwanvi.wwlive.pc.common.WebConstants;
import com.chineseall.iwanvi.wwlive.pc.common.exception.LoginException;
import com.chineseall.iwanvi.wwlive.pc.common.loginContext.LoginUser;
import com.chineseall.iwanvi.wwlive.pc.common.loginContext.UserThreadLocalContext;
import com.chineseall.iwanvi.wwlive.pc.login.service.LoginService;


/**
 * 根据cookie 判断是否登录
 * 
 *
 */
public class LoginInterceptor extends HandlerInterceptorAdapter {
    
	private final static Log logger = LogFactory.getLog(LoginInterceptor.class);

    @Autowired
    private LoginService loginService;

	/**
	 *
	 * @param request
	 * @param response
	 * @param handler
	 * @return
	 * @throws ServletException
	 * @throws IOException
	 */
	@Override
	public final boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws ServletException, IOException {

		//读取cookie
		//获取用户数据
		//用户数据存进loginContext


		try {
            LoginUser loginUser = loginService.validLogin(request, response);

            if (loginUser == null) {
            	String uri = request.getRequestURI();
            	if (uri.equals(WebConstants.KSCLOUD_START) 
            			|| uri.equals(WebConstants.KSCLOUD_STOP)) {
            		return true;
            	}
            	/*if (request.getHeader("x-requested-with") != null
                        && request.getHeader("x-requested-with")
                                .equalsIgnoreCase("XMLHttpRequest")) {
                    // 如果是ajax请求响应头会有，x-requested-with；
                }*/
                response.sendRedirect(WebConstants.Login.LOGIN_URL);
                return false;
 
            }

			UserThreadLocalContext.addCurrentUser(loginUser);
			
			request.setAttribute(WebConstants.Login.LOGIN_USER_ATTR_KEY,loginUser);

			if (logger.isDebugEnabled()) {
				logger.debug(loginUser);
			}
			Map<String, String> income = loginService.getAnchorIncomeVideoCnt(loginUser.getUserId());
			request.setAttribute(WebConstants.Login.LOGIN_USER_INCOME_VIDEOCNT_KEY, income);
			return true;


		} catch (LoginException e) {
			logger.error(e.getMessage());

			response.sendRedirect(WebConstants.Login.LOGIN_URL);

			return false;
		}

	}


	/**
	 * 删除用户状态
	 */
	public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        UserThreadLocalContext.remove();
	}

}
