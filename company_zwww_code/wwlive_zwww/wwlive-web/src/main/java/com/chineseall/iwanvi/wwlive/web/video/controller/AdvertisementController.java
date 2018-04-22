package com.chineseall.iwanvi.wwlive.web.video.controller;

import com.chineseall.iwanvi.wwlive.common.cache.RedisClientAdapter;
import com.chineseall.iwanvi.wwlive.common.cache.RedisExpireTime;
import com.chineseall.iwanvi.wwlive.common.cache.RedisKey;
import com.chineseall.iwanvi.wwlive.common.check.ResponseResult;
import com.chineseall.iwanvi.wwlive.common.check.ResultMsg;
import com.chineseall.iwanvi.wwlive.domain.wwlive.UserPush;
import com.chineseall.iwanvi.wwlive.web.common.constants.WebConstants;
import com.chineseall.iwanvi.wwlive.web.standalone.service.UserAccountService;
import com.chineseall.iwanvi.wwlive.web.video.service.AdvertisementService;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

/**
 * Created by 云瑞 on 2017/6/28.
 */
@Controller
@RequestMapping("/external")
public class AdvertisementController {

    private static final Logger LOGGER = Logger.getLogger(AdvertisementController.class);

    @Autowired
    private RedisClientAdapter redisAdapter;

    @Autowired
    private AdvertisementService advertisementService;

    @Autowired
    private UserAccountService userAccountService;

    private final String IOS_PLATFORM = "iOS";

    /**
     * banner 获取
     * @param request
     * @param model
     * @return
     */
    @RequestMapping(value = "/tab/advert/banner", method = {
            RequestMethod.POST, RequestMethod.GET })
    @ResponseBody
    public ResponseResult<Map<String, Object>> advBannerList (HttpServletRequest request, ModelAndView model) {
        ResponseResult<Map<String, Object>> rr = new ResponseResult<>();
        try {
            String cnid = request.getParameter("cnid");
            String version = request.getParameter("version");
            //灰度
//            String objQudao = redisAdapter.strGet("dlqudao");
            String platformStr = request.getParameter("platform");
            int platform = 0;
            if(IOS_PLATFORM.equals(platformStr)){
                platform = 1;
            }

            Map<String, Object> result = null;
//            if(StringUtils.isNotBlank(objQudao)) {//"1062"
//                if(objQudao.contains(cnid)){//0代表所有用户都下载
//                    result = advertisementService.getGrayAdvertBannerList(cnid,version);
//                } else {
//                    result = advertisementService.getAdvertBannerList(cnid,version);
//                }
//            } else  {
//                result = advertisementService.getAdvertBannerList(cnid,version);
//            }
            result = advertisementService.getAdvertBannerList(cnid,version);
            String userIdStr = request.getParameter("userId");
            Long userId = Long.valueOf(userIdStr);
            String type = "0";
            if(StringUtils.isNotEmpty(request.getParameter("pushType")) && !"null".equals(request.getParameter("pushType"))) {
                type = request.getParameter("pushType");
            }
            Integer pushType = Integer.valueOf(type);
            String deviceToken = request.getParameter("deviceToken");
            if(StringUtils.isEmpty(deviceToken)) {
                deviceToken = "";
            }
            String pushKey = RedisKey.ZBPUSH_ + pushType + "_" + userId;
            if (!redisAdapter.existsKey(pushKey)) {
                // 添加user-push映射关系记录, 当前为友盟
                UserPush userPush = new UserPush();
                userPush.setAppVersion(version);
                userPush.setDeviceToken(deviceToken);
                userPush.setUserId(userId);
                userPush.setPushType(pushType);
                userPush.setPlatform(platform);
                userPush.setAppCnid(cnid);
                advertisementService.addUserPush(userPush);
                String userPushKey = RedisKey.ZBPUSH_ + WebConstants.PUSH_UMENG + "_" + userId;
                redisAdapter.strSetByNormal(userPushKey, deviceToken);
                redisAdapter.expireKey(userPushKey, RedisExpireTime.EXPIRE_DAY_1);
            }else {
                if(StringUtils.isNotEmpty(deviceToken)) {
                    userAccountService.updateDeviceToken(pushType, userId, deviceToken, platform, version, cnid);
                }
            }

            rr.setData(result);
            rr.setResponseByResultMsg(ResultMsg.SUCCESS);
        } catch(Exception e) {
            LOGGER.error("banner 获取异常", e);
            rr.setResponseByResultMsg(ResultMsg.FAIL);
        }
        return rr;
    }

    /**
     * 启动图获取
     * @param request
     * @param model
     * @return
     */
    @RequestMapping(value = "/tab/advert/bootimg", method = {
            RequestMethod.POST, RequestMethod.GET })
    @ResponseBody
    public ResponseResult<Map<String, Object>> advBootImgList (HttpServletRequest request, ModelAndView model) {
        ResponseResult<Map<String, Object>> rr = new ResponseResult<>();
        try {
            String cnid = request.getParameter("cnid");
            String version = request.getParameter("version");
            //灰度
            String objQudao = redisAdapter.strGet("qudao");
            Map<String, Object> result = null;
            if(StringUtils.isNotBlank(objQudao)) {//"1062"
                if(objQudao.equals(cnid)){//0代表所有用户都下载
                    result = advertisementService.getGrayAdvertBootimgList(cnid,version);
                } else {
                    result = advertisementService.getAdvertBootimgList(cnid,version);
                }
            } else  {
                result = advertisementService.getAdvertBannerList(cnid,version);
            }
            rr.setData(result);
            rr.setResponseByResultMsg(ResultMsg.SUCCESS);
        } catch(Exception e) {
            LOGGER.error("banner 获取异常", e);
            rr.setResponseByResultMsg(ResultMsg.FAIL);
        }
        return rr;
    }
}
