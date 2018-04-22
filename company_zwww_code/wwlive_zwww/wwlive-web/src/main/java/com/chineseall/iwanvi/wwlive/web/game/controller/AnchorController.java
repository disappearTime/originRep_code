package com.chineseall.iwanvi.wwlive.web.game.controller;

import com.chineseall.iwanvi.wwlive.common.check.ResponseResult;
import com.chineseall.iwanvi.wwlive.common.check.ResultMsg;
import com.chineseall.iwanvi.wwlive.web.game.service.AnchorService;
import com.chineseall.iwanvi.wwlive.web.game.service.CardGameService;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * Created by Niu Qianghong on 2017-10-18 0018.
 */
@Controller("CardGameAnchorController")
public class AnchorController {

    @Autowired
    private AnchorService anchorService;

    @Autowired
    private CardGameService cardGameService;

    private Logger logger = Logger.getLogger(this.getClass());

    /**
     * 上传主播牌面
     * @param request
     * @return
     */
    @RequestMapping(value = "/launch/anchor/cardface/upload", method = RequestMethod.POST)
    @ResponseBody
    public ResponseResult<Map<String, Object>> uploadCardFace(HttpServletRequest request, HttpServletResponse response){
        ResponseResult<Map<String, Object>> rr = new ResponseResult<>();
        try {
            String anchorIdStr = request.getParameter("anchorId");
            Long videoId = Long.valueOf(request.getParameter("videoId"));
            Long anchorId = Long.valueOf(anchorIdStr);
            rr.setResponseByResultMsg(ResultMsg.SUCCESS);
            Map<String, Object> data = anchorService.uploadCardFace(anchorId, videoId, request);
            rr.setData(data);
        } catch (NumberFormatException e) {
            rr.setResponseByResultMsg(ResultMsg.FAIL);
            logger.error("---uploadCardFace---", e);
        }
        String url = request.getHeader("Origin");
        response.setHeader("Access-Control-Allow-Credentials", "true");
        response.setHeader("Access-Control-Allow-Origin", url);
        return rr;
    }

    /**
     * 幸运榜
     * @return
     */
    @RequestMapping(value = "/launch/game/card/lucklist", method = RequestMethod.POST)
    @ResponseBody
    public ResponseResult<Map<String, Object>> getLuckList(HttpServletRequest request, HttpServletResponse response){
        ResponseResult<Map<String, Object>> rr = new ResponseResult<>();
        try {
            Map<String, Object> luckyList = cardGameService.findLuckyList(null);
            rr.setResponseByResultMsg(ResultMsg.SUCCESS);
            rr.setData(luckyList);
        } catch (NumberFormatException e) {
            rr.setResponseByResultMsg(ResultMsg.FAIL);
            logger.error("---getLuckList---", e);
        }
        String url = request.getHeader("Origin");
        response.setHeader("Access-Control-Allow-Credentials", "true");
        response.setHeader("Access-Control-Allow-Origin", url);
        return rr;
    }

    /**
     * 今日牌面
     * @return
     */
    @RequestMapping(value = "/launch/game/card/todayface", method = RequestMethod.POST)
    @ResponseBody
    public ResponseResult<Map<String, Object>> getTodayCardFace(HttpServletRequest request, HttpServletResponse response){
        ResponseResult<Map<String, Object>> rr = new ResponseResult<>();
        try {
            Map<String, Object> dayface = cardGameService.getDayCardFace();
            rr.setResponseByResultMsg(ResultMsg.SUCCESS);
            rr.setData(dayface);
        } catch (NumberFormatException e) {
            rr.setResponseByResultMsg(ResultMsg.FAIL);
            logger.error("---getTodayCardFace---", e);
        }
        String url = request.getHeader("Origin");
        response.setHeader("Access-Control-Allow-Credentials", "true");
        response.setHeader("Access-Control-Allow-Origin", url);
        return rr;
    }
}
