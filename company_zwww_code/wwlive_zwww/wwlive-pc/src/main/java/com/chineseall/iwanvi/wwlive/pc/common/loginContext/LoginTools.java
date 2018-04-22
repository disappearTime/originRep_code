package com.chineseall.iwanvi.wwlive.pc.common.loginContext;

import com.chineseall.iwanvi.wwlive.common.cache.RedisClientAdapter;
import com.chineseall.iwanvi.wwlive.common.tools.DesUtils;
import com.chineseall.iwanvi.wwlive.pc.common.RequestUtils;
import com.chineseall.iwanvi.wwlive.pc.common.WebConstants;
import com.chineseall.iwanvi.wwlive.pc.common.exception.LoginException;
import com.chineseall.iwanvi.wwlive.pc.common.filter.AccessFilter;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by kai on 15/8/5.
 */
@Component
public class LoginTools {

    private static Logger logger = Logger.getLogger(LoginTools.class);


    @Autowired
    private RedisClientAdapter redisClientAdapter;


    public LoginUser parseLoginUser(String loginCookieStr) throws LoginException {

        try {
            DesUtils desUtils = new DesUtils();
            String loginStr = desUtils.decrypt(loginCookieStr);

            String[] strArr = loginStr.split(WebConstants.Login.LOGIN_COOKIE_SPLIT);

            LoginUser user = new LoginUser();

            user.setUserId(Long.parseLong(strArr[0]));

            user.setUserName(strArr[1]);

            user.setLoginTime(Long.parseLong(strArr[2]));

            return user;

        } catch (Exception ex) {

            logger.error("解析cookie失败:" + ex.getMessage() + ",cookieStr:" + loginCookieStr);
            throw new LoginException("解析cookie失败:" + ex.getMessage(), ex);
        }
    }

    public String toCookieStr(LoginUser loginUser) {

        try {
            DesUtils desUtils = new DesUtils();

            StringBuffer sb = new StringBuffer();
            sb.append(loginUser.getUserId());
            sb.append(WebConstants.Login.LOGIN_COOKIE_SPLIT).append(loginUser.getUserName());
            sb.append(WebConstants.Login.LOGIN_COOKIE_SPLIT).append(loginUser.getLoginTime());

            return desUtils.encrypt(sb.toString());


        } catch (Exception ex) {

            logger.error("加密用户信息失败:" + ex.getMessage() + "," + loginUser);
            throw new LoginException("加密用户信息失败:" + ex.getMessage(), ex);
        }

    }

    public void validVCode(String vcode, HttpServletRequest request) {
        String ssid = RequestUtils.getCookieValue(request, AccessFilter.SSID_KEY);



        String token = redisClientAdapter.strGet("login_session_key_" + ssid);
        if (StringUtils.isEmpty(token) || !vcode.equalsIgnoreCase(token)) {
            logger.info("当前的SessionID="+ssid+",vcode:" + vcode + ",token:" + token);
            throw new LoginException("验证码填写错误");
        }
    }

    public void setVCode(String vcode, HttpServletRequest request) {


        String ssid = RequestUtils.getCookieValue(request, AccessFilter.SSID_KEY);

        redisClientAdapter.strSetEx("login_session_key_"+ ssid, vcode, 60 * 60);

        logger.info("当前的会话ID=" + ssid + ",验证码=" + vcode);
    }

}
