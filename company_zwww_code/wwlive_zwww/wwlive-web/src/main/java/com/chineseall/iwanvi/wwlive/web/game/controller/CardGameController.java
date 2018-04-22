package com.chineseall.iwanvi.wwlive.web.game.controller;

import com.chineseall.iwanvi.wwlive.common.check.ResponseResult;
import com.chineseall.iwanvi.wwlive.common.check.ResultMsg;
import com.chineseall.iwanvi.wwlive.domain.wwlive.GameGift;
import com.chineseall.iwanvi.wwlive.web.game.service.CardGameService;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.text.ParseException;
import java.util.*;

/**
 * 纸牌游戏controller
 * Created by Niu Qianghong on 2017-10-18 0018.
 */
@Controller
@RequestMapping("/app/game/card")
public class CardGameController {
    @Autowired
    private CardGameService cardGameService;

    private Logger logger = Logger.getLogger(this.getClass());

    /**
     * 获取背包礼物列表
     * @param request
     * @return
     */
    @RequestMapping(value = "/bpgifts", method = RequestMethod.POST)
    @ResponseBody
    public ResponseResult<Map<String, Object>> getBpGiftList(HttpServletRequest request){
        ResponseResult<Map<String, Object>> rr = new ResponseResult<>();
        try {
            String userIdStr = request.getParameter("userId");
            Long userId = Long.valueOf(userIdStr);
            Map<String, Object> bpGifts = cardGameService.getBpGiftList(userId);
            rr.setResponseByResultMsg(ResultMsg.SUCCESS);
            rr.setData(bpGifts);
        } catch (NumberFormatException e) {
            rr.setResponseByResultMsg(ResultMsg.FAIL);
            logger.error("---getBpGiftList---", e);
        }
        return rr;
    }

    /**
     * 获取游戏封面
     * @param request
     * @return
     */
    @RequestMapping(value = "/cover", method = RequestMethod.POST)
    @ResponseBody
    public ResponseResult<Map<String, Object>> getCover(HttpServletRequest request){
        ResponseResult<Map<String, Object>> rr = new ResponseResult<>();
        try {
            String anchorIdStr = request.getParameter("anchorId");
            Long anchorId = Long.valueOf(anchorIdStr);
            Map<String, Object> gameCover = cardGameService.getGameCover(anchorId);
            rr.setResponseByResultMsg(ResultMsg.SUCCESS);
            rr.setData(gameCover);
        } catch (NumberFormatException e) {
            rr.setResponseByResultMsg(ResultMsg.FAIL);
            logger.error("---getCover---", e);
        }
        return rr;
    }

    /**
     * 抽牌
     * @param request
     * @return
     */
    @RequestMapping(value = "/pick", method = RequestMethod.POST)
    @ResponseBody
    public ResponseResult<Map<String, Object>> pickCard(HttpServletRequest request){
        ResponseResult<Map<String, Object>> rr = new ResponseResult<>();
        try {
            long userId = Long.parseLong(request.getParameter("userId"));
            String mode = request.getParameter("mode");
            int type = Integer.parseInt(request.getParameter("cardType"));//1金2银3铜
            int count = Integer.parseInt(request.getParameter("pickTimes"));//抽牌次数
            long anchorId = Long.parseLong(request.getParameter("anchorId"));
            long videoId = Long.parseLong(request.getParameter("videoId"));
            String chatroomId = request.getParameter("chatroomId");
            String roomNum = request.getParameter("roomNum");
            String userName = request.getParameter("userName");
            Date st = new Date();
            Map<String, Object> map = cardGameService.processingLuckDraw(type, userId, userName,
                    anchorId, videoId, count, roomNum, chatroomId, mode);
            Date et = new Date();
            long millis = et.getTime() - st.getTime();
            String val = millis > 1000 ? (millis / 1000) + "秒！" : (long) millis + "毫秒";
            System.out.println("总方法用时--->>>"+val);
            if(map != null){
                rr.setResponseByResultMsg(ResultMsg.SUCCESS);
                rr.setData(map);
            }else {
                rr.setResponseByResultMsg(ResultMsg.SUCCESS);
                rr.setData(map);
            }

            /**/
        } catch (Exception e) {
            rr.setResponseByResultMsg(ResultMsg.FAIL);
            logger.error("---pickCard---", e);
        }
        return rr;
    }

    /**
     * 幸运榜
     * @param request
     * @return
     */
    @RequestMapping(value = "/lucklist", method = RequestMethod.POST)
    @ResponseBody
    public ResponseResult<Map<String, Object>> getLuckList(HttpServletRequest request, HttpServletResponse response){
        ResponseResult<Map<String, Object>> rr = new ResponseResult<>();
        try {
            String userIdStr = request.getParameter("userId");
            Long userId = Long.valueOf(userIdStr);
            Map<String, Object> luckyList = cardGameService.findLuckyList(userId);
            rr.setResponseByResultMsg(ResultMsg.SUCCESS);
            rr.setData(luckyList);
        } catch (NumberFormatException e) {
            rr.setResponseByResultMsg(ResultMsg.FAIL);
            logger.error("---getLuckList---", e);
        }
        return rr;
    }

    /**
     * 今日牌面
     * @param request
     * @return
     */
    @RequestMapping(value = "/todayface", method = RequestMethod.POST)
    @ResponseBody
    public ResponseResult<Map<String, Object>> getTodayCardFace(HttpServletRequest request){
        ResponseResult<Map<String, Object>> rr = new ResponseResult<>();
        try {
            Map<String, Object> dayface = cardGameService.getDayCardFace();
            rr.setResponseByResultMsg(ResultMsg.SUCCESS);
            rr.setData(dayface);
        } catch (NumberFormatException e) {
            rr.setResponseByResultMsg(ResultMsg.FAIL);
            logger.error("---getTodayCardFace---", e);
        }
        return rr;
    }

    /**
     * 游戏说明页
     * @return
     */
    @RequestMapping("/readme")
    public String toGameIntro(){
        return "game/gameInstruct";
    }

    /**
     * 送出背包礼物
     * @param request
     * @return
     */
    @RequestMapping(value = "/bpgift/give", method = RequestMethod.POST)
    @ResponseBody
    public ResponseResult<Map<String, Object>> giveBpGift(HttpServletRequest request){
        ResponseResult<Map<String, Object>> rr = new ResponseResult<>();
        try {
            Long userId = Long.valueOf(request.getParameter("userId"));
            Integer goodsId = Integer.valueOf(request.getParameter("bpGiftId"));
            Integer giftCnt = Integer.valueOf(request.getParameter("bpGiftCnt"));
            Long anchorId = Long.valueOf(request.getParameter("anchorId"));
            Long videoId = Long.valueOf(request.getParameter("videoId"));
            String app = request.getParameter("app");
            rr.setResponseByResultMsg(ResultMsg.SUCCESS);
            Map<String, Object> data = cardGameService.giveGift(userId, anchorId, videoId, goodsId, giftCnt, app);
            rr.setData(data);
        } catch (NumberFormatException e) {
            rr.setResponseByResultMsg(ResultMsg.FAIL);
            logger.error("---giveBpGift---", e);
        }
        return rr;
    }
}
