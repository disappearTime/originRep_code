package com.chineseall.iwanvi.wwlive.web.video.service.impl;

import com.alibaba.fastjson.JSON;
import com.chineseall.iwanvi.wwlive.web.video.service.DaoFactoryService;
import com.chineseall.iwanvi.wwlive.web.video.service.NoticeService;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service("noticeService")
public class NoticeServiceImpl implements NoticeService {
     static final Logger LOGGER = Logger.getLogger(NoticeServiceImpl.class);

    @Autowired
    private DaoFactoryService daoFactoryService;
    /**
     * 每退出一个真实用户调用一次。触发修改直播间人数
     * @param videoId
     *            //聊天室ID--long
     * @param zb_chatRoomId
     *            //聊天室ID --string
     * @param zb_anchorId
     *            //当前主播ID--long
     * @param exitUserId
     *            //退出的用户的UserID--long
     */

    public   void manExitLive(long videoId,String zb_chatRoomId, Long zb_anchorId,long exitUserId) {
        try {
            Map<String, Object> map = new HashMap<String, Object>();

            Map<String, Object> param = new HashMap<String, Object>();
            param.put("m", "manExitLive");
            param.put("videoId", videoId);
            param.put("chatRoomId", StringUtils.isNotBlank(zb_chatRoomId) ? zb_chatRoomId : "");
            param.put("joinUserId", exitUserId);
            param.put("timestamp", System.currentTimeMillis());
            map.put("params", param);

            map.put("action", "noticeHandler");
            String json = JSON.toJSONString(map);
//            System.out.print("---se---"+daoFactoryService);
            daoFactoryService.getPushRedisDao().rpush("pushMsg", json);
            LOGGER.info("~~manExitLive~~~~"+map);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 每进入一个真实用户调用一次。触发修改直播间人数
     * @param videoId
     *            //聊天室ID--long
     * @param zb_chatRoomId
     *            //聊天室ID --string
     * @param zb_anchorId
     *            //当前主播ID--long
     * @param joinUserId
     *            //加入的用户的UserID--long
     */
    public   void manJoinLive(long videoId,String zb_chatRoomId, Long zb_anchorId,long joinUserId) {
        try {
            Map<String, Object> map = new HashMap<String, Object>();

            Map<String, Object> param = new HashMap<String, Object>();
            param.put("m", "manJoinLive");
            param.put("videoId", videoId);
            param.put("anchorId", zb_anchorId);
            param.put("chatRoomId", StringUtils.isNotBlank(zb_chatRoomId) ? zb_chatRoomId : "");
             param.put("joinUserId", joinUserId);
            param.put("timestamp", System.currentTimeMillis());
            map.put("params", param);

            map.put("action", "noticeHandler");
            String json = JSON.toJSONString(map);
//            System.out.print("---se---"+daoFactoryService);
            daoFactoryService.getPushRedisDao().rpush("pushMsg", json);
            LOGGER.info("~~manJoinLive~~~~"+map);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 每送一次礼物调用一次。
     * @param videoId
     *            //聊天室ID--long
     * @param zb_chatRoomId
     *            //聊天室ID --string
     * @param zb_anchorId
     *            //当前主播ID--long
     *  @param goodsId
     *            //商品id--int
     *  @param unitPrice
     *            //商品单价--double
     *  @param sendGiftOfUserId
     *            //送礼物的人的用户ID--string
     */
    public   void manSendGift(long videoId,String zb_chatRoomId, Long zb_anchorId, Long goodsId, double unitPrice, long sendGiftOfUserId){
        try {
            Map<String, Object> map = new HashMap<String, Object>();

            Map<String, Object> param = new HashMap<String, Object>();
            param.put("m", "manSendGift");
            param.put("videoId", videoId);
            param.put("chatRoomId", StringUtils.isNotBlank(zb_chatRoomId) ? zb_chatRoomId : "");
            param.put("anchorId", zb_anchorId);
//            param.put("zb_userName", StringUtils.isNotBlank(zb_userName) ? zb_userName : "");
            param.put("goodsId", goodsId);
            param.put("unitPrice", unitPrice);
            param.put("sendGiftOfUserId",  sendGiftOfUserId);
            param.put("timestamp", System.currentTimeMillis());
            map.put("params", param);

            map.put("action", "noticeHandler");
            String json = JSON.toJSONString(map);
            daoFactoryService.getPushRedisDao().rpush("pushMsg", json);
            LOGGER.info("~~manSendGift~~~~"+map);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
