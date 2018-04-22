package com.chineseall.iwanvi.wwlive.pc.login.service.impl;

import java.io.IOException;
import java.util.Date;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.chineseall.iwanvi.wwlive.common.cache.ExecCaller;
import com.chineseall.iwanvi.wwlive.common.cache.RedisClient;
import com.chineseall.iwanvi.wwlive.common.cache.RedisClientAdapter;
import com.chineseall.iwanvi.wwlive.common.cache.RedisExpireTime;
import com.chineseall.iwanvi.wwlive.common.cache.RedisKey;
import com.chineseall.iwanvi.wwlive.common.tools.StrMD5;
import com.chineseall.iwanvi.wwlive.dao.wwlive.AnchorMapper;
import com.chineseall.iwanvi.wwlive.domain.wwlive.Anchor;
import com.chineseall.iwanvi.wwlive.pc.common.RequestUtils;
import com.chineseall.iwanvi.wwlive.pc.common.WebConstants;
import com.chineseall.iwanvi.wwlive.pc.common.exception.LoginException;
import com.chineseall.iwanvi.wwlive.pc.common.helper.AnchorInfoHelper;
import com.chineseall.iwanvi.wwlive.pc.common.loginContext.LoginUser;
import com.chineseall.iwanvi.wwlive.pc.login.service.LoginService;
import com.fasterxml.jackson.databind.ObjectMapper;

import redis.clients.jedis.Jedis;

/**
 * Created by kai on 16/8/25.
 */
@Service("loginService")
public class LoginServiceImpl implements LoginService {

    private static final String PASS_SALT = "iwanvi_salt";

    private static final String REDIS_LOGIN_KEY_PREFIX = "login_";

    ObjectMapper objectMapper = new ObjectMapper();

    private static final Logger LOGGER = Logger.getLogger(LoginService.class);

    @Autowired
    private RedisClient redisClient;

    @Autowired
    private AnchorMapper anchorMapper;

    @Autowired
    private RedisClientAdapter redisAdapter;

    @Override
    public LoginUser validLogin(HttpServletRequest request, HttpServletResponse response) throws LoginException {

        final String loginCookie = RequestUtils.getCookieValue(request, WebConstants.Login.LOGIN_COOKIE_KEY);

        if (StringUtils.isBlank(loginCookie)) {
            return null;
        }

        LoginUser loginUser = getLoginUser(loginCookie);

        if (loginUser == null) {
            return null;
        }
        
        /*if(loginUser.getAcctStatus() != 0){
            RequestUtils.deleteCookie(response, request, WebConstants.Login.LOGIN_COOKIE_KEY);
            return null;
        }*/

        Anchor anchor = AnchorInfoHelper.getAndCacheCurrentAnchorInfo(redisAdapter, anchorMapper, loginUser.getUserId());
        if (anchor != null && anchor.getAcctStatus() != 0) {
            RequestUtils.deleteCookie(response, request, WebConstants.Login.LOGIN_COOKIE_KEY);
            return null;
        }

        // 判断登录用户等有效期如果低于30分钟,则更新redis里有效时间
        long curr = System.currentTimeMillis();
        if (loginUser.getExpireTime() < curr + 1000 * 60 * 60) {

            loginUser.setExpireTime(curr + 1000 * 3600 * 12);
            redisClient.setEx(REDIS_LOGIN_KEY_PREFIX + loginCookie, loginUser, 3600 * 12);

            LOGGER.info("登录延长失效时间,登录名:" + loginUser.getUserName());
        }

        return loginUser;
    }

    @Override
    public LoginUser login(String name, String pass, HttpServletResponse response) throws LoginException {

        // 数据库校验
        // 生成cookiekey
        // 写入redis
        // 写入cookie

        String md5Pass = StrMD5.getInstance().encrypt(pass, PASS_SALT);

        Anchor anchor = anchorMapper.getAnchorByLogin(name);

        if (anchor == null) {
            LOGGER.info("登录失败,登录名不存在,登录名:" + name);
            throw new LoginException("用户名错误");
        }

        if (anchor.getAcctStatus() != 0) {
            // 账户禁用, 禁止登录
            LOGGER.info("登录失败, 账号已禁用, 登录名:" + name);
            throw new LoginException("账号已禁用");
        }

        if (!md5Pass.equals(anchor.getPasswd())) {

            LOGGER.info("登录失败,登录名和密码不匹配,登录名:" + name);
            throw new LoginException("密码错误");
        }

        Long loginTime = System.currentTimeMillis();

        String cookieKey = StrMD5.getInstance().getStringMD5(anchor.getAnchorId() + "_" + loginTime);

        /*
         * a.anchor_id ,a.passport,a.user_name, a.passwd, a.head_img , a.sex ,
         * a.room_num , a.birthday, a.notice
         * 
         */
        LoginUser loginUser = new LoginUser();
        loginUser.doMapToDtoValue(anchor.putFieldValueToMap(), false);
        loginUser.setLoginTime(loginTime);
        loginUser.setExpireTime(loginTime + 1000 * 3600 * 12);

        redisClient.setEx(REDIS_LOGIN_KEY_PREFIX + cookieKey, loginUser, 3600 * 12);

        RequestUtils.addCookie(response, WebConstants.Login.LOGIN_COOKIE_KEY, cookieKey);

        anchorMapper.updateAnchorLogOnTime(loginUser.getUserId(), new Date(loginTime));

        return loginUser;
    }

    @Override
    public void loginOut(HttpServletRequest request, HttpServletResponse response) {

        // 删除cookie
        // 记录登出日志

        final String loginCookie = RequestUtils.getCookieValue(request, WebConstants.Login.LOGIN_COOKIE_KEY);

        if (StringUtils.isBlank(loginCookie)) {
            LOGGER.info("login out,cookie is empty!!!");
            return;
        }

        LoginUser loginUser = getLoginUser(loginCookie);

        redisClient.exec(new ExecCaller<Object>() {
            @Override
            public Object exec(Jedis jedis) {
                jedis.del(loginCookie);
                return null;
            }
        });

        RequestUtils.deleteCookie(response, request, WebConstants.Login.LOGIN_COOKIE_KEY);

        LOGGER.info("login out," + loginUser);
    }

    public LoginUser getLoginUser(String loginCookie) {

        final String key = REDIS_LOGIN_KEY_PREFIX + loginCookie;

        LoginUser loginUser = redisClient.exec(new ExecCaller<LoginUser>() {
            @Override
            public LoginUser exec(Jedis jedis) {
                String user = jedis.get(key);

                if (StringUtils.isNotEmpty(user)) {

                    try {
                        return objectMapper.readValue(user, LoginUser.class);
                    } catch (IOException e) {
                        ;
                    }

                }
                return null;
            }
        });

        return loginUser;
    }

    @Override
    public Map<String, String> getAnchorIncomeVideoCnt(long anchorId) {
        String key = RedisKey.ANCHOR_INCOME_VIEDOCNT_ + anchorId;
        Map<String, String> income = redisAdapter.hashGetAll(key);
        if (income == null || income.isEmpty()) {// 放入redis中
            Map<String, Object> in = anchorMapper.getAnchorIncomeVideoCnt(anchorId);

            if (in == null || in.isEmpty()) {// 排空检查
                income.put("anchorId", anchorId + "");
                income.put("income", "0");
                income.put("videoCnt", "0");
                return income;
            } else {
                income.put("anchorId", anchorId + "");
                income.put("income", ((Double) in.get("income")).intValue() + "");
                income.put("videoCnt", in.get("videoCnt").toString());
            }

            redisAdapter.hashMSet(key, in);
            redisAdapter.expireKey(key, RedisExpireTime.EXPIRE_HOUR_12);// 12 小时
        }
        return income;
    }

}
