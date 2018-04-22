package com.chineseall.iwanvi.wwlive.web.video.controller;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.chineseall.iwanvi.wwlive.common.check.ResponseResult;
import com.chineseall.iwanvi.wwlive.common.check.ResultMsg;
import com.chineseall.iwanvi.wwlive.web.common.util.ControllerRequestUtils;
import com.chineseall.iwanvi.wwlive.web.common.util.ValidationUtils;
import com.chineseall.iwanvi.wwlive.web.video.service.GoodsInfoService;

@Controller
@RequestMapping("/app")
public class GoodsInfoController {
	
	private final static Logger LOGGER = Logger.getLogger(GoodsInfoController.class);
	
    @Autowired
    private GoodsInfoService goodsInfoService;
    
    /**
     * 查询架上的礼品列表
     * @param request
     * @return
     */
    @RequestMapping(value = "/goods/list", method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    public ResponseResult<Map<String,Object>> findShelfGoods(HttpServletRequest request){
        ResponseResult<Map<String, Object>> rr = new ResponseResult<>();
        
        if(!ValidationUtils.isValid(request)){
            rr.setResponseByResultMsg(ResultMsg.COVER_KEY_CHECK_FAILED);
            return rr;
        }
        try {
            String userId = request.getParameter("userId");
            rr.setData(goodsInfoService.getShelfGoodsList(userId));
            rr.setResponseByResultMsg(ResultMsg.SUCCESS);
        } catch (Exception e) {
        	LOGGER.error("获得礼物列表失败：", e);
            rr.setResponseByResultMsg(ResultMsg.FAIL);
        }
        return rr;
    }
    
    /**
     * 贵族列表，贵族展示
     * @param request
     * @return
     */
    @RequestMapping(value = "/goods/nobles", method = RequestMethod.POST)
    @ResponseBody
    public ResponseResult<Map<String,Object>> findNobles(HttpServletRequest request){
        ResponseResult<Map<String, Object>> rr = new ResponseResult<>();
        
        if(!ValidationUtils.isValid(request)){
            rr.setResponseByResultMsg(ResultMsg.COVER_KEY_CHECK_FAILED);
            return rr;
        }
        try {
        	Long userId = ControllerRequestUtils.parseLongFromRquest(request, "userId");
            rr.setData(goodsInfoService.findNobles(userId));
            rr.setResponseByResultMsg(ResultMsg.SUCCESS);
        } catch (Exception e) {
            rr.setResponseByResultMsg(ResultMsg.FAIL);
        	LOGGER.error("获得贵族列表异常：", e);
        	e.printStackTrace();
        }
        return rr;
    }

    /**
     * 贵族礼品特效下载接口
     * @param request
     * @return
     */
    @RequestMapping(value = "/noble/specialeffects", method = {RequestMethod.POST,RequestMethod.GET})
    @ResponseBody
    public ResponseResult<Map<String,Object>> findShelfNobleGoods(HttpServletRequest request){
        ResponseResult<Map<String, Object>> rr = new ResponseResult<>();

        if(!ValidationUtils.isValid(request)){
            rr.setResponseByResultMsg(ResultMsg.COVER_KEY_CHECK_FAILED);
            return rr;
        }

        Map<String, Object> shelfNobleList = goodsInfoService.getShelfNobleList();
        rr.setData(shelfNobleList);
        rr.setResponseByResultMsg(ResultMsg.SUCCESS);

        return rr;
    }

    @RequestMapping(value = "/noble/oneSpecial", method = {RequestMethod.POST,RequestMethod.GET})
    @ResponseBody
    public ResponseResult<Map<String,Object>> findShelfNobleGoodsById(HttpServletRequest request){
        ResponseResult<Map<String, Object>> rr = new ResponseResult<>();

        if(!ValidationUtils.isValid(request)){
            rr.setResponseByResultMsg(ResultMsg.COVER_KEY_CHECK_FAILED);
            return rr;
        }
        String goodsId = request.getParameter("goodsId");
        Map<String, Object> shelfNoble= goodsInfoService.getShelfNobleById(goodsId);
        rr.setData(shelfNoble);
        rr.setResponseByResultMsg(ResultMsg.SUCCESS);

        return rr;
    }

    /**
     * 弹幕信息
     */
    @RequestMapping(value = "/goods/barrage", method = {RequestMethod.POST,RequestMethod.GET})
    @ResponseBody
    public ResponseResult<Map<String,String>> getShelfBarrage (HttpServletRequest request) {
        ResponseResult<Map<String, String>> rr = new ResponseResult<>();

        if(!ValidationUtils.isValid(request)){
            rr.setResponseByResultMsg(ResultMsg.COVER_KEY_CHECK_FAILED);
            return rr;
        }

        rr.setData(goodsInfoService.getShelfBarrage());
        rr.setResponseByResultMsg(ResultMsg.SUCCESS);

        return rr;
    }

}
