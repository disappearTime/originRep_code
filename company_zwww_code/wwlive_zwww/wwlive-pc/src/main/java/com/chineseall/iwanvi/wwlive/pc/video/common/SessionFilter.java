package com.chineseall.iwanvi.wwlive.pc.video.common;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.WebApplicationContext;

import com.chineseall.iwanvi.wwlive.common.cache.RedisClientAdapter;
import com.chineseall.iwanvi.wwlive.common.cache.RedisExpireTime;
import com.chineseall.iwanvi.wwlive.common.cache.RedisKey;
import com.chineseall.iwanvi.wwlive.common.constants.Constants;
import com.chineseall.iwanvi.wwlive.common.tools.Base64Tools;

/**
 * 用户session检查，登录成功后，默认登录时长为12小时，当过期后收回，如果再默认时间有操作可继续加时间
 * @author DIKEPU
 *
 */
public class SessionFilter implements Filter {

    static final Logger LOGGER = Logger.getLogger(SessionFilter.class);
	
	private RedisClientAdapter redisAdapter;
	
	private String logonHtml = "";
	
	private String logonStrings = "";
	
	private String includeStrings = "";
	
	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		//获得登录界面，获得不检查cookie的url地址
		ServletContext context = filterConfig.getServletContext();
		ApplicationContext ac =(ApplicationContext) context.getAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE);
		redisAdapter = (RedisClientAdapter) ac.getBean("redisAdapter");
		this.logonHtml = filterConfig.getInitParameter("logonHtml");
		this.logonStrings = filterConfig.getInitParameter("loginStrings");
		includeStrings = filterConfig.getInitParameter("includeStrings");
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;  
        HttpServletResponse res = (HttpServletResponse) response; 
        String uri = req.getRequestURI();
        String[] exclude = logonStrings.split(";");
        
        String[] include = includeStrings.split(";");
        

        if (exclude != null) {
    		if (isContains(uri, exclude)) {
    			chain.doFilter(request, response);
            	return;
            }
        }

        if (include != null) {
    		if (isContains(uri, include)) {
    			chain.doFilter(request, response);
            	return;
            }
        }
        
    	checkCookie(req, res);
		chain.doFilter(request, response);
		
	}

	@Override
	public void destroy() {
		
	}

	private void checkCookie(HttpServletRequest req, HttpServletResponse res) throws IOException, ServletException {
		Cookie[] cookies = req.getCookies();
		if (cookies != null) {
			for (int i = 0; i < cookies.length; i++) {
				Cookie cookie = cookies[i];
				if (cookie.getName().equals(Constants.REDIS_SESSION)) {
					String value = null;
					if ((value = cookie.getValue()) != null) {
						value = new String(Base64Tools.decode(value));
						String[] values = value.split("_");
						if (values == null || values.length < 2) {
							//跳转到登录界面
							res.sendRedirect(logonHtml);
						}
						String secret = values[0];
						String anchorKey = RedisKey.PREFIX_ANCHOR_SESSION_
								+ values[1];
						if (StringUtils.isEmpty(secret)
								|| !redisAdapter.existsKey(anchorKey)) {
							//跳转到登录界面
							res.sendRedirect(logonHtml);
						} else {
							redisAdapter.expireKey(value,
									RedisExpireTime.EXPIRE_HOUR_12);
						}
					}
				}
			}
             
        } else {
        	res.sendRedirect(logonHtml);
        }
	}

	public  boolean isContains(String container, String[] regx) {
		
        boolean result = false;

        if(regx == null || regx.length==0){
        	return result;
        }
        if(StringUtils.isEmpty(container)){
        	return result;
        }
        for (int i = 0; i < regx.length; i++) {
            if (container.contains(".") && container.endsWith(regx[i])) {
                return true;
            } else if (container.equals(regx[i])) {
            	return true;
            }
        }
        return result;
    }
	
}
