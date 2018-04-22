package com.chineseall.iwanvi.wwlive.web.my.controller;


import com.alibaba.fastjson.JSONObject;
import com.chineseall.iwanvi.wwlive.common.cache.RedisClientAdapter;
import com.chineseall.iwanvi.wwlive.common.cache.RedisKey;
import com.chineseall.iwanvi.wwlive.common.check.ResponseResult;
import com.chineseall.iwanvi.wwlive.common.check.ResultMsg;
import com.chineseall.iwanvi.wwlive.common.constants.Constants;
import com.chineseall.iwanvi.wwlive.dao.wwlive.ContributionListMapper;
import com.chineseall.iwanvi.wwlive.dao.wwlive.UserInfoMapper;
import com.chineseall.iwanvi.wwlive.domain.wwlive.UserInfo;
import com.chineseall.iwanvi.wwlive.web.common.helper.PageUrlHelper;
import com.chineseall.iwanvi.wwlive.web.common.helper.UserRankHelper;
import com.chineseall.iwanvi.wwlive.web.common.util.ControllerRequestUtils;
import com.chineseall.iwanvi.wwlive.web.common.util.ValidationUtils;
import com.chineseall.iwanvi.wwlive.web.my.service.MyAcctInfoService;
import com.chineseall.iwanvi.wwlive.web.video.controller.DownLoadController;
import com.service.FollowAnchorService;
import com.service.impl.FollowAnchorServiceImpl;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

import static com.chineseall.iwanvi.wwlive.web.common.util.RegexUtils.isNum;

@Controller
public class MyAcctInfoController {

    private final Logger LOGGER = Logger
            .getLogger(this.getClass());

    @Autowired
    MyAcctInfoService myAcctInfoService;

    @Autowired
    private RedisClientAdapter redisAdapter;

    @Autowired
    private ContributionListMapper contribMapper;

    @Autowired
    private UserInfoMapper userInfoMapper;

    //获取关注数
    private static FollowAnchorService followService = new FollowAnchorServiceImpl();

    @RequestMapping(value = "/app/my/get", method = RequestMethod.GET)
    public ModelAndView getInfo(HttpServletRequest request) {
        ModelAndView model = new ModelAndView("my/home");
        if (!ValidationUtils.isValid(request)) {
            return model;
        }
        // 通用参数设定, 在页面上发送ajax请求时使用
        Map<String, String> params = ControllerRequestUtils.getCommonParam(request);
        if (params != null && !params.isEmpty()) {
            String commonParams = PageUrlHelper.buildCommonUrl(params);
            model.addObject("commonParams", commonParams);
        }

        model.addObject("params", ControllerRequestUtils.getParam(request));

        try {
            long userId = Long.parseLong(request.getParameter("userId"));
            model.addObject("userId", userId);

            //获取全站排行
            Map<String, String> userInfo = new HashMap<>();
            String rankKey = RedisKey.USER_RANK_CONTRIB_ + userId;
            if (redisAdapter.existsKey(rankKey)) {
                userInfo = UserRankHelper.getUserStrRankCache(redisAdapter, contribMapper, userId);
            } else {
                userInfo = UserRankHelper.getAndCacheStrUserRank(redisAdapter, contribMapper, userId);
            }
            model.addObject("rank", userInfo.get("rank"));

            //来源(cj插件  dl独立版)
            model.addObject("origin", request.getParameter("app"));
            //获取用户相关信息
            UserInfo u = userInfoMapper.findById(userId);
            if (u != null) {
                model.addObject("headImg", u.getHeadImg());
                model.addObject("userName", u.getUserName());
                model.addObject("sex", u.getSex());
            }

            //获取关注数
            int followNum = followService.getFollowNumber(userId);
            model.addObject("followNum", followNum);



            //获取版本，判断是否为新版本isNewVersion
            String versionKey = RedisKey.independentUpdownKeys.INDEPENDENT_CURRENT_VERSION;
            String objQudao=redisAdapter.strGet("dlqudao");
            if(StringUtils.isNotBlank(objQudao)) {//"1062"
                if(objQudao.contains(request.getParameter("cnid"))||objQudao.equals("0")){//0代表所有用户都下载
                    if(redisAdapter.existsKey(versionKey)){
                        String version = redisAdapter.hashGet(versionKey, "version");
                        if(DownLoadController.compareVersion(request.getParameter("version"), version) < 0) {
                            model.addObject("isNewVersion", 1);
                        } else {
                            model.addObject("isNewVersion", 0);
                        }
                    }else{  model.addObject("isNewVersion", 0); }
                }else{  model.addObject("isNewVersion", 0); }
            }else{  model.addObject("isNewVersion", 0); }
            model.addAllObjects(myAcctInfoService.getUserAcctInfo(userId, null));
        } catch (Exception e) {
            LOGGER.error(e.getCause());
        }
        return model;
    }

    @RequestMapping(value = "/app/acct/get", method = RequestMethod.POST)
    @ResponseBody
    public ResponseResult<Map<String, Object>> getAcctInfo(HttpServletRequest request) {

        if (!ValidationUtils.isValid(request)) {
            return new ResponseResult<>(ResultMsg.COVER_KEY_CHECK_FAILED);
        }
        ResponseResult<Map<String, Object>> result = null; //接口校验
        try {
            String app = request.getParameter("app");
            long userId = Long.parseLong(request.getParameter("userId"));
            result = new ResponseResult<Map<String, Object>>();
            result.setData(myAcctInfoService.getUserAcctInfo(userId, app));
            result.setResponseByResultMsg(ResultMsg.SUCCESS);
            result.setRequestId(request.getParameter("requestId"));
        } catch (Exception e) {
            LOGGER.error(e.getCause());
        }
        return result;
    }

    /**
     * 根据登录id获取消费列表
     *
     * @param request
     * @return
     */
    @RequestMapping(value = "/external/excharge/page",
            method = RequestMethod.GET)
    public ModelAndView pageInfo(HttpServletRequest request, HttpServletResponse response) {
        ModelAndView model = new ModelAndView("my/excharge_list");
        try {
            model.addObject("userId", request.getParameter("userId"));
            model.addObject("origin", request.getParameter("origin"));
        } catch (Exception e) {
            LOGGER.error(e.getCause());
        }
        return model;
    }

    /**
     * 根据登录id获取消费列表
     *
     * @param request
     * @return
     */
    @RequestMapping(value = "/external/excharge/list",
            method = RequestMethod.POST)
    @ResponseBody
    public String getConsume(HttpServletRequest request, HttpServletResponse response) {
        String userId = request.getParameter("userId");
        String pageSizeStr = request.getParameter("pageSize");
        String pageNumStr = request.getParameter("pageNum");
        String origin = request.getParameter("origin");

        String result = "";
        if (StringUtils.isEmpty(userId) || StringUtils.isEmpty(origin)) {
            return result;
        }
        if (!isNum(userId)) {
            return result;
        }
        if (!isNum(pageSizeStr) || !isNum(pageNumStr) || !isNum(userId)
                || !isNum(origin)) {
            return result;
        }
        int pageNum, pageSize;
        if ((pageNum = Integer.parseInt(pageNumStr)) <= 0
                || (pageSize = Integer.parseInt(pageSizeStr)) <= 0) {
            return result;
        }

        Map<String, Object> resultJson = myAcctInfoService.getExpenseList(Long.parseLong(userId),
                Integer.parseInt(origin),
                pageNum, pageSize);
        JSONObject json = new JSONObject();
        json.putAll(resultJson);
        response.setCharacterEncoding(Constants.UTF8);
        return json.toJSONString();
    }

    @RequestMapping(value = "/app/my/setData/setPassword", method = RequestMethod.GET)
    public String setUserPasswd(HttpServletRequest request, HttpServletResponse response) {
    	
    	return "my/setData/setPassword";
    }
}
