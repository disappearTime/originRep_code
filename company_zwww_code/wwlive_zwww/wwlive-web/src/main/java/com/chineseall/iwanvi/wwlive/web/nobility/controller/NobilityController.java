package com.chineseall.iwanvi.wwlive.web.nobility.controller;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import com.chineseall.iwanvi.wwlive.dao.wwlive.CouponInfoMapper;
import com.chineseall.iwanvi.wwlive.dao.wwlive.RoleInfoMapper;
import com.chineseall.iwanvi.wwlive.domain.wwlive.*;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.alibaba.fastjson.JSONObject;
import com.chineseall.iwanvi.wwlive.common.cache.RedisClientAdapter;
import com.chineseall.iwanvi.wwlive.common.cache.RedisExpireTime;
import com.chineseall.iwanvi.wwlive.common.cache.RedisKey;
import com.chineseall.iwanvi.wwlive.common.check.ResponseResult;
import com.chineseall.iwanvi.wwlive.common.check.ResultMsg;
import com.chineseall.iwanvi.wwlive.dao.wwlive.OrderInfoMapper;
import com.chineseall.iwanvi.wwlive.web.common.util.ValidationUtils;
import com.chineseall.iwanvi.wwlive.web.nobility.common.NobilityCommon;
import com.chineseall.iwanvi.wwlive.web.nobility.service.Nobility2Service;
import com.chineseall.iwanvi.wwlive.web.nobility.service.NobilityService;
import com.chineseall.iwanvi.wwlive.web.video.service.PayService;

@Controller
public class NobilityController {

    static final Logger logger = Logger.getLogger(NobilityController.class);

    @Autowired
    private NobilityService nobilityService;

    @Autowired
    private Nobility2Service nobility2Service;
    @Autowired
    private PayService payService;
    @Autowired
    private NobilityCommon nobilityCommon;
    @Autowired
    private OrderInfoMapper orderInfoMapper;
    @Autowired
    private RedisClientAdapter redisClientAdapter;

    @Autowired
    private RoleInfoMapper roleInfoMapper;

    @Autowired
    private CouponInfoMapper couponInfoMapper;

    static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");


    @RequestMapping(value="/app/nobility/my/nobilitypage",method = RequestMethod.GET)

    public String toNobilityPage(HttpServletRequest request, Model model) {
        ModelAndView modelAndView = new ModelAndView("noblecenter/nobleCenter");

        long userId = Long.parseLong(request.getParameter("userId"));

        //获骑士取相关信息
//        List<CavallierInfo> cavallierInfoList = nobilityService.getCavalierInfo(userId);

        //获取用户开通贵族有效时间
        List nobleEndTime = nobilityService.getUserNobleTimeEnd(userId);

//        modelAndView.addObject(cavallierInfoList);
        model.addAttribute("nobleEndTime",JSONObject.toJSON(nobleEndTime));
        return "noblecenter/nobleCenter";
    }

    /**
     * 获取骑士对应的价格及优惠
     * @param request
     * @return
     */
    @RequestMapping(value = "/app/nobility/my/getMyNobilityPrice", method = RequestMethod.POST)
    @ResponseBody
    public ResponseResult<DiscountPriceInfo> getMyNobilityInfo(HttpServletRequest request) {
        ResponseResult<DiscountPriceInfo> rr = new ResponseResult<DiscountPriceInfo>();
        String userId = request.getParameter("userId");
        String goodsId = request.getParameter("goodsId");

        if(StringUtils.isBlank(userId)||StringUtils.isBlank(goodsId)){
            rr.setResponseByResultMsg(ResultMsg.LOST_PARAMS);
            return rr;
        }
        try {
            DiscountPriceInfo dis =  nobilityService.getMyNobilityPrice(userId,goodsId);
            rr.setResponseByResultMsg(ResultMsg.SUCCESS);
            rr.setData(dis);
        } catch (Exception e) {
            logger.error("贵族购买优惠异常： ", e);
            rr.setResponseByResultMsg(ResultMsg.FAIL);
        }
        return rr;
    }

    /**
     * 立即开通骑士，生成订单
     * @param request
     * @return
     */
    @RequestMapping(value = "/app/nobility/my/insertMyNobilityOrder", method = RequestMethod.POST)
    @ResponseBody
    public ResponseResult<JSONObject> insertMyNobilityOrder(HttpServletRequest request) {
        ResponseResult<JSONObject> rr = new ResponseResult<JSONObject>();
        String userId = request.getParameter("userId");
        String goodsId = request.getParameter("goodsId");
        String payType = request.getParameter("payType");//支付方式 2微信 3支付宝

        if(StringUtils.isBlank(userId)||StringUtils.isBlank(goodsId)
                ||StringUtils.isBlank(payType)){
            rr.setResponseByResultMsg(ResultMsg.LOST_PARAMS);
            return rr;
        }
        try {
        	JSONObject data = 
        			nobility2Service.noblePurchase(Long.valueOf(userId), 0L, 0L, 0, Long.valueOf(goodsId), Integer.valueOf(payType));

            rr.setResponseByResultMsg(ResultMsg.SUCCESS);
            rr.setData(data);
        } catch (Exception e) {
            logger.error("贵族购买异常： ", e);
            rr.setResponseByResultMsg(ResultMsg.FAIL);
        }
        return rr;
    }

    /**
     * 弹幕消费钻石
     * @param request
     * @return
     */
    @RequestMapping(value = "/app/nobility/my/barrage", method = RequestMethod.POST)
    @ResponseBody
    public ResponseResult<Map<String, Object>> barrage(HttpServletRequest request) {
        ResponseResult<Map<String, Object>> result = checkPay(request); //接口校验
        if (result != null) {
            return result;
        }

        Map<String, Object> map = null;
        try {
            map = payService.toPay(request);
            result = new ResponseResult<Map<String, Object>>(ResultMsg.SUCCESS);
            result.setData(map);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("创建支付订单失败：", e);
            result = new ResponseResult<Map<String, Object>>(ResultMsg.FAIL);
            return result;
        }

        if (map == null || map.isEmpty()) {
            result = new ResponseResult<Map<String, Object>>(ResultMsg.FAIL);
            return result;
        }
        int cnt = (int) map.get("result");
        if (cnt <= 0) {
            result = new ResponseResult<Map<String, Object>>(ResultMsg.FAIL);
            return result;
        }

        if (cnt == 1) {
	        //发送融云推送消息
	        nobilityCommon.pushBarrageMsg(request.getParameter("chatroomId"),request.getParameter("userId"),
	                request.getParameter("goodsId"),request.getParameter("anchorId"),
	                request.getParameter("content"));
        }
        return result;
    }

    /**
     * 校验支付参数信息
     * @param request
     * @return
     */
    private ResponseResult<Map<String, Object>> checkPay(HttpServletRequest request) {
        ResponseResult<Map<String, Object>> rr = null;
        //接口校验
        if(!ValidationUtils.isValid(request, "anchorId", "goodsId", "goodsNum")){
            rr = new ResponseResult<>();
            rr.setResponseByResultMsg(ResultMsg.COVER_KEY_CHECK_FAILED);
            return rr;
        }
        if (StringUtils.isBlank(request.getParameter("goodsId"))
                || StringUtils.isBlank(request.getParameter("goodsNum"))
                || StringUtils.isBlank(request.getParameter("payType"))
                || StringUtils.isBlank(request.getParameter("userId"))
                || StringUtils.isBlank(request.getParameter("videoId"))
                || StringUtils.isBlank(request.getParameter("anchorId"))
                || StringUtils.isBlank(request.getParameter("chatroomId"))) {
            rr = new ResponseResult<>();
            rr.setResponseByResultMsg(ResultMsg.LOST_PARAMS);
            return rr;
        }
        return rr;
    }


    /**
     * 检查订单是否支付成功
     * @param request
     * @return
     */
    @RequestMapping(value = "/external/nobility/my/checkSuccessByNo", method = RequestMethod.POST)
    @ResponseBody
    private ResponseResult<Map<String, Object>> checkSuccessByNo(HttpServletRequest request) {
        ResponseResult<Map<String, Object>> rr = new ResponseResult<Map<String, Object>>();
        String orderNo = request.getParameter("orderNo");
        if(StringUtils.isBlank(orderNo)){
            rr.setResponseByResultMsg(ResultMsg.LOST_PARAMS);
            return rr;
        }

        if(redisClientAdapter.existsKey(RedisKey.NobleKey.ORDER_IS_SUCCESS_+orderNo)){
            rr.setResponseByResultMsg(ResultMsg.SUCCESS);
            return rr;
        }

        OrderInfo orderInfo = orderInfoMapper.getOrderInfoByOutNo(orderNo);
        if(orderInfo!=null){
            if(orderInfo.getOrderStatus()==1){
                redisClientAdapter.strSetexByNormal(RedisKey.NobleKey.ORDER_IS_SUCCESS_+orderNo, RedisExpireTime.EXPIRE_DAY_5,orderNo);
                rr.setResponseByResultMsg(ResultMsg.SUCCESS);
               return rr;
            }
        }else{
            rr.setResponseByResultMsg(ResultMsg.ORDER_ISNOTEXIST);
            return rr;
        }
        rr.setResponseByResultMsg(ResultMsg.FAIL);
        return rr;
    }

    /**
     * 支付成功获取用户贵族到期时间
     */
    @RequestMapping("/external/my/noble")
    @ResponseBody
    public ResponseResult<Map<String, Object>> getUserNobleTimeEnd(HttpServletRequest request) throws ParseException {
        ResponseResult<Map<String, Object>> rr = new ResponseResult<Map<String, Object>>();
        String userId = request.getParameter("userId");
        String goodsId = request.getParameter("goodsId");
        if(StringUtils.isBlank(userId) || StringUtils.isBlank(goodsId)){
            rr.setResponseByResultMsg(ResultMsg.LOST_PARAMS);
            return rr;
        }
        RoleInfo roleInfo = nobilityService.getRoleInfoByUidAndGid(Long.parseLong(userId), Long.parseLong(goodsId));
        if(roleInfo != null) {
            Map<String, Object> roleInfoToMap = getRoleInfoToMap(roleInfo);
            CouponInfo couponInfo = couponInfoMapper.getCouponInfoByUidAndGid(roleInfo.getUserId(), Long.parseLong(goodsId));
            if(couponInfo != null) {
                roleInfoToMap.put("couponEndTime",sdf.format(couponInfo.getEffectivenessTime()));
                roleInfoToMap.put("balance",couponInfo.getCouponBalance());
            }
            rr.setResponseByResultMsg(ResultMsg.SUCCESS);
            rr.setData(roleInfoToMap);
            return rr;
        }
        rr.setResponseByResultMsg(ResultMsg.FAIL);
        return rr;
    }

    private static Map<String, Object> getRoleInfoToMap(RoleInfo lv) {
        Map<String,Object> map = new HashMap<>();
        map.put("roleId", lv.getRoleId());
        map.put("userId", lv.getUserId());
        map.put("goodsId", lv.getGoodsId());
        map.put("goodsName", lv.getGoodsName());
        map.put("roleLevel", lv.getRoleLevel());
        map.put("roleStatus", lv.getRoleStatus());
        map.put("effectiveEndTime", sdf.format(lv.getEffectiveEndTime()));
        return map;
    }
}
