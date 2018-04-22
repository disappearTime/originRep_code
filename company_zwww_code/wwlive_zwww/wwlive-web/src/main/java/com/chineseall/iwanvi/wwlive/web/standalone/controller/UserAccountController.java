package com.chineseall.iwanvi.wwlive.web.standalone.controller;

import com.alibaba.fastjson.JSONObject;
import com.chineseall.iwanvi.wwlive.common.cache.RedisClientAdapter;
import com.chineseall.iwanvi.wwlive.common.cache.RedisExpireTime;
import com.chineseall.iwanvi.wwlive.common.cache.RedisKey;
import com.chineseall.iwanvi.wwlive.common.check.ResponseResult;
import com.chineseall.iwanvi.wwlive.common.check.ResultMsg;
import com.chineseall.iwanvi.wwlive.common.tools.StrMD5;
import com.chineseall.iwanvi.wwlive.domain.wwlive.OauthInfo;
import com.chineseall.iwanvi.wwlive.domain.wwlive.UserInfo;
import com.chineseall.iwanvi.wwlive.web.common.constants.WebConstants;
import com.chineseall.iwanvi.wwlive.web.common.util.JsonUtils;
import com.chineseall.iwanvi.wwlive.web.common.util.ValidationUtils;
import com.chineseall.iwanvi.wwlive.web.standalone.service.UserAccountService;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;

import java.util.HashMap;
import java.util.Map;

/**
 * 独立版APP用户账号相关操作controller
 * Created by Niu Qianghong on 2017-06-27 0027.
 */
@Controller
public class UserAccountController {

    @Autowired
    private RedisClientAdapter redisAdapter;

    @Autowired
    private UserAccountService userAccountService;

    private static final Integer MAX_ACCESS_TIMES = 100; // 同一ip某段时间内访问注册接口的最大次数

    private Logger logger = Logger.getLogger(this.getClass());

    private final String IOS_PLATFORM = "iOS";

    /**
     * 获取用户全部信息, iOS使用
     * @param request
     * @return
     */
    @RequestMapping(value = "/app/account/allinfo", method = RequestMethod.POST)
    @ResponseBody
    public ResponseResult<JSONObject> getAllInfo(HttpServletRequest request){
        ResponseResult<JSONObject> rr = new ResponseResult<>();
        try {
            String userIdStr = request.getParameter("userId");
            if (StringUtils.isBlank(userIdStr)) {
                rr.setResponseByResultMsg(ResultMsg.FAIL);
                return rr;
            }

            Long userId = Long.valueOf(userIdStr);
            // 获取用户所有信息
            UserInfo user = userAccountService.getAllInfoById(userId);
            JSONObject userJSON = JsonUtils.toValueOfJsonString(user);
            rr.setResponseByResultMsg(ResultMsg.SUCCESS);
            rr.setData(userJSON);
            return rr;
        } catch (NumberFormatException e) {
            logger.error("获取用户所有信息异常", e);
            rr.setResponseByResultMsg(ResultMsg.FAIL);
            return rr;
        }
    }

    /**
     * 游客用户注册
     * @param request
     * @return
     */
    @RequestMapping(value = "/external/account/register", method = RequestMethod.POST)
    @ResponseBody
    public ResponseResult<JSONObject> register(HttpServletRequest request){
        logger.error("开始注册");
        ResponseResult<JSONObject> rr = new ResponseResult<>();
        try {
            // 校验注册接口参数
            String ip = request.getParameter("ip");
            String credential = request.getParameter("credential");
            String platformStr = request.getParameter("platform");
            int platform = 0;
            if(IOS_PLATFORM.equals(platformStr)){
                platform = 1;
            }
            if(!checkRegParams(request) // coverKey校验
                    || credential.length() < 26 // credential校验
                    || !ValidationUtils.checkIP(ip) // IP校验
                    || isTooFrequent(ip)){ // IP访问频率校验
                logger.error("校验失败");
                rr.setResponseByResultMsg(ResultMsg.FAIL);
                return rr;
            }

            String version = request.getParameter("version");
            Integer pushType = Integer.valueOf(request.getParameter("pushType"));
            String deviceToken = request.getParameter("deviceToken");
            String cnid = request.getParameter("cnid");
            Map<String, Object> userInfo = userAccountService.register(credential, ip, platform, version,
                    pushType, deviceToken, cnid);
            JSONObject userJSON = JsonUtils.toValueOfJsonString(userInfo);
            rr.setResponseByResultMsg(ResultMsg.SUCCESS);
            // rr.setData(userInfo);
            rr.setData(userJSON);

        } catch (Exception e) {
            e.printStackTrace();
            rr.setResponseByResultMsg(ResultMsg.FAIL);
        }
        return rr;
    }

    /**
     * 校验接口参数的coverKey
     * 加密规则: 按照IMEI + mac + requestId + version拼接, 获取md5串进行比对
     * @return
     */
    private boolean checkRegParams(HttpServletRequest request){
        String credential = request.getParameter("credential");
        String requestId = request.getParameter("requestId");
        String version = request.getParameter("version");
        String coverKey = request.getParameter("coverKey");

        if(StringUtils.isBlank(coverKey) || StringUtils.isBlank(credential)
                || StringUtils.isBlank(requestId) || StringUtils.isBlank(version)){
            return false;
        }

        StringBuilder sb = new StringBuilder();
        sb.append(credential);
        sb.append(requestId);
        sb.append(version);
        String stringMD5 = StrMD5.getInstance().getStringMD5(sb.toString());

        return coverKey.equals(stringMD5);
    }

    /**
     * 检查该ip是否访问太频繁
     * 太频繁标准: 24h中访问次数 > 100
     * @param ip
     * @return
     */
    private boolean isTooFrequent(String ip){
        String ipKey = RedisKey.REGISTER_IP_KEY + ip;
        if (redisAdapter.existsKey(ipKey)){
            String timesStr = redisAdapter.strGet(ipKey);
            if(StringUtils.isBlank(timesStr)){
                redisAdapter.strSetByNormal(ipKey, "50");// 当value为空时, 将value置为50, 此时允许该ip访问50次
                redisAdapter.expireKey(ipKey, RedisExpireTime.EXPIRE_DAY_1);
                return false;
            } 
            Integer times = Integer.valueOf(timesStr);
            if (MAX_ACCESS_TIMES > times){
                redisAdapter.strIncr(ipKey); // ip访问次数+1
                return false;
            }
            logger.info("IP: " + ip + "一天内访问次数到达100");
            return true;
        } else {
            redisAdapter.strSetByNormal(ipKey, "1");
            redisAdapter.expireKey(ipKey, RedisExpireTime.EXPIRE_DAY_1);
            return false;
        }
    }

    /**
     * 用户绑定微信
     * @param request
     */
    @RequestMapping(value = "/app/user/bind/wechat", method = RequestMethod.POST)
    @ResponseBody
    public ResponseResult<Map<String, Object>> bindToWeChat(HttpServletRequest request){
        logger.info("wx: 开始绑定");
        ResponseResult<Map<String, Object>> rr = new ResponseResult<>();

        String code = request.getParameter("code");
        String userIdStr = request.getParameter("userId");
        String token = request.getParameter("token");
        String platform = request.getParameter("platform");

        if(StringUtils.isBlank(code) || StringUtils.isBlank(userIdStr)){
            logger.info("wx: code或userId为空");
            rr.setResponseByResultMsg(ResultMsg.FAIL);
            return rr;
        }

        try {
            Long userId = Long.valueOf(userIdStr);
            logger.info("wx: userId = " + userIdStr);
            Map<String, Object> bindResult = userAccountService.bindToWeChat(userId, token, code, platform);
            rr.setResponseByResultMsg(ResultMsg.SUCCESS);
            rr.setData(bindResult);
            return rr;
        } catch (Exception e) {
            logger.error("独立APP: 用户绑定微信异常uid = " + userIdStr + "--> ", e);
        }

        rr.setResponseByResultMsg(ResultMsg.FAIL);
        return rr;
    }


    /**
     * 用户绑定QQ
     * @param request
     */
    @RequestMapping(value = "/app/user/bind/qq", method = RequestMethod.POST)
    @ResponseBody
    public ResponseResult<Map<String, Object>> bindToQQ(HttpServletRequest request){
        ResponseResult<Map<String, Object>> rr = new ResponseResult<>();

        String openId = request.getParameter("openId");
        String userIdStr = request.getParameter("userId");
        String token = request.getParameter("token");
        String accessToken = request.getParameter("accessToken");

        String sex = request.getParameter("sex");
        String avatar = request.getParameter("avatar");
        String nickname = request.getParameter("nickname");

        if(StringUtils.isBlank(openId) || StringUtils.isBlank(userIdStr)){
            rr.setResponseByResultMsg(ResultMsg.FAIL);
            return rr;
        }

        try {
            Long userId = Long.valueOf(userIdStr);
            Map<String, Object> bindResult = userAccountService.bindToQQ(userId, token, openId, accessToken, sex, avatar, nickname);
            rr.setResponseByResultMsg(ResultMsg.SUCCESS);
            rr.setData(bindResult);
            return rr;
        } catch (Exception e) {
            logger.error("独立APP: 用户绑定QQ异常uid = " + userIdStr + "--> ", e);
        }

        rr.setResponseByResultMsg(ResultMsg.FAIL);
        return rr;
    }

    /**
     * 微信登录
     * @param request
     * @return
     */
    @RequestMapping(value = "/app/user/login/wechat", method = RequestMethod.POST)
    @ResponseBody
    public ResponseResult<Map<String, Object>> loginByWeChat(HttpServletRequest request){
        ResponseResult<Map<String, Object>> rr = new ResponseResult<>();
        try {
            String token = request.getParameter("token");
            String userIdStr = request.getParameter("userId");
            String code = request.getParameter("code");
            String platformStr = request.getParameter("platform");
            String cnid = request.getParameter("cnid");
            int platform = 0;
            if(IOS_PLATFORM.equals(platformStr)){
                platform = 1;
            }
            Integer pushType = Integer.valueOf(request.getParameter("pushType"));
            String deviceToken = request.getParameter("deviceToken");
            String version = request.getParameter("version");

            Long userId = Long.valueOf(userIdStr);
            Map<String, Object> data =
                    userAccountService.loginByWeChat(userId, token, code, pushType, deviceToken, platform, version, cnid);
            rr.setResponseByResultMsg(ResultMsg.SUCCESS);
            rr.setData(data);
        } catch (Exception e) {
            logger.error("独立APP: 微信登录失败", e);
            rr.setResponseByResultMsg(ResultMsg.FAIL);
        }

        return rr;
    }

    /**
     * QQ登录
     * @param request
     * @return
     */
    @RequestMapping(value = "/app/user/login/qq", method = RequestMethod.POST)
    @ResponseBody
    public ResponseResult<Map<String, Object>> loginByQQ(HttpServletRequest request){
        ResponseResult<Map<String, Object>> rr = new ResponseResult<>();
        try {
            String token = request.getParameter("token");
            String userIdStr = request.getParameter("userId");
            String openId = request.getParameter("openId");
            String platformStr = request.getParameter("platform");
            String cnid = request.getParameter("cnid");
            int platform = 0;
            if(IOS_PLATFORM.equals(platformStr)){
                platform = 1;
            }
            Integer pushType = Integer.valueOf(request.getParameter("pushType"));
            String deviceToken = request.getParameter("deviceToken");
            String version = request.getParameter("version");

            Long userId = Long.valueOf(userIdStr);
            Map<String, Object> data =
                    userAccountService.loginByQQ(userId, token, openId, pushType, deviceToken, platform, version, cnid);
            rr.setResponseByResultMsg(ResultMsg.SUCCESS);
            rr.setData(data);
        } catch (Exception e) {
            logger.error("独立APP: QQ登录失败", e);
            rr.setResponseByResultMsg(ResultMsg.FAIL);
        }
        return rr;
    }

    /**
     * 用户名密码登录
     * @param request
     * @return
     */
    @RequestMapping(value = "/app/user/login/normal", method = RequestMethod.POST)
    @ResponseBody
    public ResponseResult<Map<String, Object>> loginByPassword(HttpServletRequest request){
        ResponseResult<Map<String, Object>> rr = new ResponseResult<>();
        try {
            String userIdStr = request.getParameter("userId");
            Long userId = Long.valueOf(userIdStr);
            String token = request.getParameter("token");
            String userName = request.getParameter("userName");
            String password = request.getParameter("password");
            String platformStr = request.getParameter("platform");
            String cnid = request.getParameter("cnid");
            int platform = 0;
            if(IOS_PLATFORM.equals(platformStr)){
                platform = 1;
            }
            Integer pushType = Integer.valueOf(request.getParameter("pushType"));
            String deviceToken = request.getParameter("deviceToken");
            String version = request.getParameter("version");

            Map<String, Object> data =
                    userAccountService.loginByNormal(userId, token, userName, password, pushType, deviceToken, platform, version, cnid);
            rr.setResponseByResultMsg(ResultMsg.SUCCESS);
            rr.setData(data);
        } catch (Exception e) {
            logger.error("独立APP: 账号密码登录失败", e);
            rr.setResponseByResultMsg(ResultMsg.FAIL);
        }
        return rr;
    }

    @RequestMapping("/app/user/switch")
    public String switchUser(){
        return "my/login";
    }

    @RequestMapping("/app/user/bound/query")
    @ResponseBody
    public ResponseResult<Map<String, Object>> boundWeChatQuery(HttpServletRequest request){
        ResponseResult<Map<String, Object>> rr = new ResponseResult<>();
        try {
            Long userId = Long.valueOf(request.getParameter("userId"));
            Integer type = Integer.valueOf(request.getParameter("type"));
            OauthInfo oauthInfo = userAccountService.queryBound(type, userId);
            rr.setResponseByResultMsg(ResultMsg.SUCCESS);
            Map<String, Object> data = new HashMap<>();
            if (oauthInfo != null && oauthInfo.getOpenId() != null){
                data.put("boundResult", 1);
            } else {
                data.put("boundResult", 0);
            }
            rr.setData(data);
            return rr;
        } catch (NumberFormatException e) {
            logger.error("查询绑定时异常", e);
        }
        rr.setResponseByResultMsg(ResultMsg.FAIL);
        return rr;
    }

}
