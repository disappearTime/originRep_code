package com.chineseall.iwanvi.wwlive.pc.common.filter;

import java.io.IOException;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.chineseall.iwanvi.wwlive.common.tools.UUIDUtils;
import com.chineseall.iwanvi.wwlive.pc.common.RequestUtils;

/**
 * Created by kai
 * 访问filter，纪录所有访问日志
 * 校验cookie里是否有客户端标识，如果没有会设置一个有效期365*10天的cookie
 * 校验cookie里是否有当前会话标识，如果没有会设置一个会话级别的cookie
 */
public class AccessFilter implements Filter {
    
    private static final Logger LOGGER = Logger.getLogger(AccessFilter.class);

    /**
     * 万维agent cookie key
     * 有效期365*10天
     */
    public static final String IWANVI_AGENT_KEY = "wwa";

    /**
     * 会话级cookie key
     * 当前会话有效，浏览器关闭后消息
     */
    public static final String SSID_KEY = "ssid";

    /**
     * 不需要记录访问日志的url
     * 类似：/static/这种
     */
    private List<String> skipURL = Collections.emptyList();

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

        String skipurls = filterConfig.getInitParameter("skipurl");

        if (StringUtils.isNotBlank(skipurls)) {

            String[] arrs = skipurls.split(",");

            skipURL = new LinkedList<String>();
            for (String str : arrs) {
                if (StringUtils.isNotBlank(str)) {
                    skipURL.add(str);
                }
            }
        }
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {

        HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;
        HttpServletResponse httpServletResponse = (HttpServletResponse) servletResponse;

        String uri = httpServletRequest.getRequestURI();

        if (isSkipURI(uri)) {  //如果配置了跳过地址，匹配上则直接跳走
            filterChain.doFilter(httpServletRequest, httpServletResponse);
            return;
        }

        /*if(!uri.startsWith("/login")){
            Anchor anchor = 
                    AnchorInfoHelper.getAndCacheCurrentAnchorInfo(redisAdapter, anchorMapper);
            if(anchor !=null && anchor.getAcctStatus() != 0){
                httpServletResponse.sendRedirect("/login/in");
            }            
        }*/
                
        Long beginTime = System.currentTimeMillis();

        String wwa = RequestUtils.getCookieValue(httpServletRequest, IWANVI_AGENT_KEY);

        if (StringUtils.isBlank(wwa)) {
            wwa = UUIDUtils.generate24UUID();
            RequestUtils.addCookie(httpServletResponse, IWANVI_AGENT_KEY, wwa, 60*60*24*365*10);
        }

        String ssid = RequestUtils.getCookieValue(httpServletRequest, SSID_KEY);
        if (StringUtils.isBlank(ssid)) {
            ssid = UUIDUtils.generate24UUID();
            RequestUtils.addCookie(httpServletResponse, SSID_KEY, ssid);
        }


        String ua = RequestUtils.getStringHeaderDef(httpServletRequest, "User-Agent", "");

        String referer = RequestUtils.getStringHeaderDef(httpServletRequest, "referer", "");

        String ip = RequestUtils.getIpAddr(httpServletRequest);

        String pin = RequestUtils.getCookieValue(httpServletRequest, "pin", "");

        String url = httpServletRequest.getRequestURL().toString();

        String methodType = httpServletRequest.getMethod();

        //校验是否带有X-Requested-With头部，标准jquery的ajax会带上此头部（仅仅是约定，有可能不对）
        String reqType = RequestUtils.getStringHeaderDef(httpServletRequest, "X-Requested-With", "") == "" ? "form" : "ajax";

        Map<String, String> paramMap = getParam(httpServletRequest);


        Date accessTime = new Date(beginTime);

        //继续执行
        filterChain.doFilter(httpServletRequest, httpServletResponse);

        Long endTime = System.currentTimeMillis();

        Integer procTime = Integer.parseInt(Long.toString(endTime - beginTime));

        AccessLog accessLog = new AccessLog();
        accessLog.setAccessDate(accessTime);
        accessLog.setIp(ip);
        accessLog.setPin(pin);
        accessLog.setMethodType(methodType);
        accessLog.setParamMap(paramMap);
        accessLog.setProcTime(procTime);
        accessLog.setReferer(referer);
        accessLog.setUa(ua);
        accessLog.setUri(uri);
        accessLog.setUrl(url);
        accessLog.setReqType(reqType);
        accessLog.setSsid(ssid);
        accessLog.setWwa(wwa);

        //日志写入文件
        accessLog.writeToFile();

    }


    private Map<String, String> getParam(HttpServletRequest request) {

        @SuppressWarnings("unchecked")
		Enumeration<String> enumeration = request.getParameterNames();

        Map<String, String> paramMap = new HashMap<String, String>();
        while (enumeration.hasMoreElements()) {
            String name = enumeration.nextElement();
            paramMap.put(name, request.getParameter(name));
        }

        return paramMap;
    }


    /**
     * 是否跳过该url
     *
     * @param uri
     * @return
     */
    private boolean isSkipURI(String uri) {

        for (String skip : skipURL) {

            if (uri.startsWith(skip)) {
                return true;
            }
        }
        return false;
    }


    @Override
    public void destroy() {

        LOGGER.info("destroy()...");

    }
}
