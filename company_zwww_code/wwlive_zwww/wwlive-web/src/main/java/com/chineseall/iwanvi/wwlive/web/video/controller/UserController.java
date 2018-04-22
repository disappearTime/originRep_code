package com.chineseall.iwanvi.wwlive.web.video.controller;

import com.alibaba.fastjson.JSONObject;
import com.chineseall.iwanvi.wwlive.common.check.ResponseResult;
import com.chineseall.iwanvi.wwlive.common.check.ResultMsg;
import com.chineseall.iwanvi.wwlive.common.constants.Constants;
import com.chineseall.iwanvi.wwlive.common.exception.IWanviException;
import com.chineseall.iwanvi.wwlive.common.tools.HttpUtils;
import com.chineseall.iwanvi.wwlive.web.common.util.ValidationUtils;
import com.chineseall.iwanvi.wwlive.web.video.service.ReportService;
import com.chineseall.iwanvi.wwlive.web.video.service.UserInfoService;
import com.zw.zcf.util.HttpClientUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

import static com.chineseall.iwanvi.wwlive.web.common.util.RegexUtils.isNum;

//import com.chineseall.iwanvi.wwlive.common.cache.RedisClientAdapter;

@Controller
public class UserController {

    static final Logger LOGGER = Logger
            .getLogger(UserController.class);

    @Autowired
    private UserInfoService userService;

    @Autowired
    private ReportService reportService;

    /**
     * 外部直接跳转到直播间的地址
     */
    @Value("${external.points.detail.url}")
    private String pointsDetailUrl;

    /**
     * app端点击用户昵称要展示的用户资料
     *
     * @param request
     * @return
     */
    @RequestMapping(value = "/app/user/detail", method = RequestMethod.POST)
    @ResponseBody
    public ResponseResult<Map<String, Object>> getDetailForApp(HttpServletRequest request) {
        ResponseResult<Map<String, Object>> rr = checkGetDetailForApp(request);
        if (rr != null) {
            return rr;
        }

        String loginId = request.getParameter("viewId");
        String userIdStr = request.getParameter("viewUserId");
        String anchorIdStr = request.getParameter("anchorId");
        rr = new ResponseResult<>();
        rr.setData(userService.getUserInfoByLoinId(loginId, anchorIdStr, userIdStr));
        rr.setResponseByResultMsg(ResultMsg.SUCCESS);

        return rr;
    }

    /**
     * @param request
     * @return
     */
    private ResponseResult<Map<String, Object>> checkGetDetailForApp(HttpServletRequest request) {
        ResponseResult<Map<String, Object>> rr = null;

        if (!ValidationUtils.isValid(request)) {
            rr = new ResponseResult<>();
            rr.setResponseByResultMsg(ResultMsg.COVER_KEY_CHECK_FAILED);
            return rr;
        }
        if (StringUtils.isBlank(request.getParameter("viewId"))) {
            rr = new ResponseResult<>();
            rr.setResponseByResultMsg(ResultMsg.LOST_PARAMS);
            return rr;
        }
        return rr;
    }

    /**
     * app端用户举报直播视频
     *
     * @param request
     * @return
     */
    @RequestMapping(value = "/app/user/reportvideo", method = RequestMethod.POST)
    @ResponseBody
    public ResponseResult<Map<String, Object>> reportVideo(HttpServletRequest request) {
        ResponseResult<Map<String, Object>> rr = checkReportVideo(request);
        if (rr != null) {
            return rr;
        }

        Long reportedKey = Long.valueOf(request.getParameter("reportedKey"));
        int reportKind = Integer.valueOf(request.getParameter("reportKind"));
        Long reportedUserId = Long.valueOf(request.getParameter("reportedUserId"));
        Long userId = Long.valueOf(request.getParameter("userId"));
        int reportType = Integer.valueOf(request.getParameter("reportType"));

        rr = new ResponseResult<>();
        rr.setData(reportService.report(reportedKey, reportKind, reportType, reportedUserId, userId));
        rr.setResponseByResultMsg(ResultMsg.SUCCESS);
        return rr;
    }

    /**
     * 校验参数
     *
     * @param request
     * @return
     */
    private ResponseResult<Map<String, Object>> checkReportVideo(HttpServletRequest request) {
        ResponseResult<Map<String, Object>> rr = null;
        if (!ValidationUtils.isValid(request, "reportedKey")) {
            rr = new ResponseResult<>();
            rr.setResponseByResultMsg(ResultMsg.COVER_KEY_CHECK_FAILED);
            return rr;
        }
        if (StringUtils.isBlank(request.getParameter("reportedKey"))
                || StringUtils.isBlank(request.getParameter("reportKind"))
                || StringUtils.isBlank(request.getParameter("reportedUserId"))
                || StringUtils.isBlank(request.getParameter("userId"))
                || StringUtils.isBlank(request.getParameter("reportType"))) {
            rr = new ResponseResult<>();
            rr.setResponseByResultMsg(ResultMsg.LOST_PARAMS);
            return rr;
        }
        return rr;
    }

    /**
     * 用户跳出直播app获得积分等之前须获得返回直播app地址。
     *
     * @param request
     * @return
     */
    @RequestMapping(value = "/app/user/pointsurl", method = RequestMethod.POST)
    @ResponseBody
    public ResponseResult<String> getUserPoints(HttpServletRequest request) {
        ResponseResult<String> result = checkGetUserPoints(request);
        if (result != null) {
            return result;
        }

        String vid = request.getParameter("videoId");
        String uid = request.getParameter("userId");
        int userId = Integer.valueOf(uid);
        String loginId = userService.getUserLoginId(userId);
        if (StringUtils.isNotBlank(loginId)) {
            result = new ResponseResult<String>(ResultMsg.SUCCESS);
            result.setData(pointsDetailUrl + "?videoId=" + vid
                    + "&userId=" + uid + "&uid=" + loginId);
        } else {
            ResultMsg fail = ResultMsg.FAIL;
            fail.setInfo("无此用户");
            return new ResponseResult<String>(fail);
        }
        return result;
    }

    /**
     * 校验进入直播间信息
     *
     * @param request
     * @return
     * @throws IWanviException
     */
    private ResponseResult<String> checkGetUserPoints(HttpServletRequest request) throws IWanviException {
        ResponseResult<String> rr = null;
        if (StringUtils.isBlank(request.getParameter("videoId"))
                || StringUtils.isBlank(request.getParameter("userId"))) {
            rr = new ResponseResult<>();
            ResultMsg fail = ResultMsg.LOST_PARAMS;
            fail.setInfo("视频id或用户id缺失");
            fail.setToolTip("无法跳转");
            rr.setResponseByResultMsg(fail);
        }
        return rr;
    }

    /**
     * @param request
     * @return
     */
    @RequestMapping(value = "/external/user/points/detail", method = {
            RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    public ResponseResult<Long> userPointsDetail(
            HttpServletRequest request) {
        String userId = request.getParameter("userId");
        String loginId = request.getParameter("uid");

        ResponseResult<Long> result = null;
        long virtualCurrency = 0L;
        try {
            checkAddScore(loginId, userId);
            //同步用户积分
            virtualCurrency = userService.synchroVirtualCurrency(Long.parseLong(userId), loginId);
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
            ResultMsg fail = ResultMsg.FAIL;
            fail.setInfo(e.getMessage());
            result = new ResponseResult<Long>(fail);
            result.setData(0L);
        }
        result = new ResponseResult<Long>(ResultMsg.SUCCESS);
        result.setData(virtualCurrency);
        return result;

    }

    /**
     * 检验传递的参数是否合规
     *
     * @throws IWanviException
     */
    private void checkAddScore(String userId, String loginId) throws IWanviException {
        if (StringUtils.isBlank(loginId)) {
            throw new IWanviException("用户loginId：" + loginId + "为空。");
        }
        if (StringUtils.isBlank(userId)) {
            throw new IWanviException("请求的用户id：" + userId + "，或观看视频标记：");
        }
    }

    /**
     * 用户跳出直播app获得积分等之前须获得返回直播app地址。
     *
     * @param request
     * @return
     */
    @RequestMapping(value = "/external/user/points/url", method = RequestMethod.POST)
    @ResponseBody
    public ResponseResult<String> getUserPointsFromH5(HttpServletRequest request) {
        ResponseResult<String> result;

        String uid = request.getParameter("userId");
        int userId = Integer.valueOf(uid);
        String loginId = userService.getUserLoginId(userId);
        if (StringUtils.isNotBlank(loginId)) {
            result = new ResponseResult<String>(ResultMsg.SUCCESS);
            result.setData(pointsDetailUrl + "?videoId=" + 0
                    + "&userId=" + uid + "&uid=" + loginId);
        } else {
            ResultMsg fail = ResultMsg.FAIL;
            fail.setInfo("无此用户");
            return new ResponseResult<String>(fail);
        }
        return result;
    }

    /**
     * 根据登录id获取消费列表
     *
     * @param request
     * @return
     */
    @RequestMapping(value = "/external/user/expenselist",
            method = RequestMethod.POST, produces = "text/html;charset=UTF-8")
    @ResponseBody
    public String getExpenseList(HttpServletRequest request, HttpServletResponse response) {
        String loginId = request.getParameter("userId");
        String from = request.getParameter("from");
        String pageSizeStr = request.getParameter("pageSize");
        String pageNumStr = request.getParameter("pageNum");

        String result = "";
        if (StringUtils.isEmpty(from)) {
            return result;
        }
        if (StringUtils.isEmpty(loginId)) {
            return result;
        }
        if (!isNum(pageSizeStr) || !isNum(pageNumStr)) {
            return result;
        }
        int pageNum, pageSize;
        if ((pageNum = Integer.parseInt(pageNumStr)) <= 0 || (pageSize = Integer.parseInt(pageSizeStr)) <= 0) {
            return result;
        }
        if (!isNum(from)) {
            return result;
        }
        if (Constants.USER_INFO_ORIGIN_0 == Integer.parseInt(from)) {
            loginId = loginId + Constants.CX_USER_SUFFIX;
        }

        Map<String, Object> resultJson = userService.getExpenseList(loginId,
                pageNum, pageSize);
        JSONObject json = new JSONObject();
        json.putAll(resultJson);
        response.setCharacterEncoding(Constants.UTF8);
        return json.toJSONString();
    }

    /**
     * 根据登录id获取消费列表
     *
     * @param request
     * @return
     */
    @RequestMapping(value = "/external/user/toconsume",
            method = RequestMethod.GET)
    public ModelAndView toConsume(HttpServletRequest request, HttpServletResponse response) {
        ModelAndView model = new ModelAndView("my/consume_list");
        model.addObject("userId", request.getParameter("userId"));
        return model;
    }

    /**
     * 用户跳转到设置页面
     *
     * @param request
     * @return
     */
    @RequestMapping(value = "/external/user/setting",
            method = RequestMethod.GET)
    public ModelAndView settingCenter(HttpServletRequest request, HttpServletResponse response) {
        ModelAndView model = new ModelAndView("my/setting");
        String isNewVersion = request.getParameter("isNewVersion");
        model.addObject("isNewVersion",isNewVersion);
        return model;
    }

    /**
     * 根据登录id获取消费列表
     *
     * @param request
     * @return
     */
    @RequestMapping(value = "/external/user/consume",
            method = RequestMethod.POST)
    @ResponseBody
    public String getConsume(HttpServletRequest request, HttpServletResponse response) {
        String userId = request.getParameter("userId");
        String pageSizeStr = request.getParameter("pageSize");
        String pageNumStr = request.getParameter("pageNum");

        String result = "";
        if (StringUtils.isEmpty(userId)) {
            return result;
        }
        if (!isNum(pageSizeStr) || !isNum(pageNumStr) || !isNum(userId)) {
            return result;
        }
        int pageNum, pageSize;
        if ((pageNum = Integer.parseInt(pageNumStr)) <= 0 || (pageSize = Integer.parseInt(pageSizeStr)) <= 0) {
            return result;
        }

        Map<String, Object> resultJson = userService.getConsume(userId,
                pageNum, pageSize);
        JSONObject json = new JSONObject();
        json.putAll(resultJson);
        response.setCharacterEncoding(Constants.UTF8);
        return json.toJSONString();
    }

    /**
     * 获取融云token
     *
     * @param request
     * @return
     */
    @RequestMapping(value = "/app/rong/gettoken",
            method = RequestMethod.POST)
    @ResponseBody
    public ResponseResult<Map<String, Object>> getRongInf(HttpServletRequest request, HttpServletResponse response) {
        ResponseResult<Map<String, Object>> result = checkRongInf(request);
        if (result != null) {
            return result;
        }

        String strId = request.getParameter("userId");
        Map<String, Object> userInfo = userService.getRongInf(Long.parseLong(strId));

        if (!ObjectUtils.isEmpty(userInfo)) {
            result = new ResponseResult<Map<String, Object>>(ResultMsg.SUCCESS);
            result.setData(userInfo);
        } else {
            result = new ResponseResult<Map<String, Object>>(ResultMsg.FAIL);
        }
        return result;
    }

    private ResponseResult<Map<String, Object>> checkRongInf(HttpServletRequest request) throws IWanviException {
        ResponseResult<Map<String, Object>> rr = null;
        if (!ValidationUtils.isValid(request)) {
            rr = new ResponseResult<>();
            rr.setResponseByResultMsg(ResultMsg.COVER_KEY_CHECK_FAILED);
            return rr;
        }
        if (StringUtils.isBlank(request.getParameter("userId"))) {
            rr = new ResponseResult<>();
            ResultMsg fail = ResultMsg.LOST_PARAMS;
            fail.setInfo("用户id缺失");
            fail.setToolTip("无法跳转");
            rr.setResponseByResultMsg(fail);
        }
        return rr;
    }

    /**
     * 禁言某用户
     * <p/>
     * 房管禁言限时、超管禁言需要持久化且不限时
     *
     * @return
     */
    @RequestMapping(value = "/app/user/mute", method = RequestMethod.POST)
    @ResponseBody
    public ResponseResult<Map<String, Object>> muteUser(HttpServletRequest request) {
        ResponseResult<Map<String, Object>> rr = new ResponseResult<>();
        String userIdStr = request.getParameter("userId");//房管的用户id
        String anchorIdStr = request.getParameter("anchorId");//主播id
        String loginId = request.getParameter("loginId");//被禁言用户的loginid
        String durationStr = request.getParameter("duration");//禁言时长以分钟为单位        
        String videoIdStr = request.getParameter("videoId");
        try {
            Long userId = Long.valueOf(userIdStr);
            Long anchorId = Long.valueOf(anchorIdStr);
            Integer duration = Integer.valueOf(durationStr);
            Long videoId = Long.valueOf(videoIdStr);
            rr.setData(userService.mute(videoId, userId, anchorId, loginId, duration * 60));//禁言时长换算为秒
            return rr;
        } catch (NumberFormatException e) {
            Map<String, Object> data = new HashMap<>();
            data.put("result", 0);
            rr.setInfo("请稍后重试~");
            LOGGER.error("加入黑名单异常：", e);
            return rr;
        } catch (IWanviException ie) {
            Map<String, Object> data = new HashMap<>();
            data.put("result", 0);
            rr.setInfo(ie.getMessage());
            LOGGER.error("加入黑名单异常：", ie);
            return rr;
        }
    }


    /**
     * 跳转到个人资料页面
     *
     * @param request
     * @param response
     * @return
     */
    @RequestMapping(value = "/external/my/getMyInfo",
            method = RequestMethod.GET)
    public ModelAndView toMyInfo(HttpServletRequest request, HttpServletResponse response) {
        ModelAndView model = new ModelAndView("my/userEditProfile");
        model.addObject("userId", request.getParameter("userId"));
        return model;
    }

    /**
     * 根据用户userId获取用户信息
     *
     * @param request
     * @param response
     * @return
     */
    @RequestMapping(value = "/external/my/getMyInfo",
            method = RequestMethod.POST)
    @ResponseBody
    public ResponseResult<Map<String, Object>> getMyInfo(HttpServletRequest request, HttpServletResponse response) {
        ResponseResult<Map<String, Object>> rr = new ResponseResult<Map<String, Object>>();

        String userId = request.getParameter("userId");
        String version = request.getParameter("version");
        rr.setData(userService.getUserInfoByUserId(userId,version));
        rr.setResponseByResultMsg(ResultMsg.SUCCESS);

        return rr;
    }

    /**
     * 根据用户userId更新用户信息
     *
     * @param request
     * @param response
     * @return
     */
    @RequestMapping(value = "/external/my/updateMyInfo",
            method = RequestMethod.POST)
    @ResponseBody
    public ResponseResult<Map<String, Object>> updateMyInfo(HttpServletRequest request, HttpServletResponse response) {
        ResponseResult<Map<String, Object>> rr = new ResponseResult<Map<String, Object>>();
        String userId = request.getParameter("userId");
        String version = request.getParameter("version");

        Map<String, Object> resultMap = userService.updateUserInfoByUserId(request);
        if (resultMap.get("result").equals("2002")) {
            //昵称已存在
            rr.setResponseByResultMsg(ResultMsg.USERNAME_EXIST);
        } else if (resultMap.get("result").equals("1006")) {
            //图片上传错误
            rr.setResponseByResultMsg(ResultMsg.ANCHOR_HEAD_IMAGE);
        } else if(resultMap.get("result").equals("1002")){
            rr.setResponseByResultMsg(ResultMsg.APP_USER_NOT_EXIST);
        }else if(resultMap.get("result").equals("2003")){
            rr.setResponseByResultMsg(ResultMsg.TOKEN_LOSE);
        }else if(resultMap.get("result").equals("2004")){
            rr.setResponseByResultMsg(ResultMsg.OLDPASSWD_ERR);
        }else {
            rr.setResponseByResultMsg(ResultMsg.SUCCESS);
            rr.setData(userService.getUserInfoByUserId(userId,version));
        }
        return rr;
    }

    @RequestMapping("/user/info/modify/nickname")
    public String toModifyUserName(HttpServletRequest request, Model model){
        String userName = request.getParameter("nickname");
        model.addAttribute("nickname", userName);
        return "my/setData/setNickname";
    }

    @RequestMapping(value = "/external/user/headimg/isavailable",method = {RequestMethod.POST,RequestMethod.GET})
    @ResponseBody
    public ResponseResult<Map<String, Object>> getUserHeadImgIsAvailable (HttpServletRequest request) throws Exception {
        ResponseResult<Map<String, Object>> rr = new ResponseResult<Map<String, Object>>();
        String userId = request.getParameter("userId");
        String version = request.getParameter("version");
        try {
            Map<String, Object> dataMap = userService.getUserInfoByUserId(userId, version);
            String start = "";
            if(dataMap != null) {
                Map<String, Object> userInfo = (Map<String, Object>) dataMap.get("userInfo");
                if(StringUtils.isNotEmpty((String) userInfo.get("headImg"))) {
                    start = HttpUtils.getJSON((String) userInfo.get("headImg"), "utf-8");
                }
            }
            if(StringUtils.isEmpty(start)) {
                rr.setResponseByResultMsg(ResultMsg.FAIL);
            }else {
                rr.setResponseByResultMsg(ResultMsg.SUCCESS);
            }
        }catch (Exception e) {
            LOGGER.error("getUserHeadImgIsAvailable error : " + e);
            rr.setResponseByResultMsg(ResultMsg.SYSTEM_ERR);
        }
        return rr;
    }
}
