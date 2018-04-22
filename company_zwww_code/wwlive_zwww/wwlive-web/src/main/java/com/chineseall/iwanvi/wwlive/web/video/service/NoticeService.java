package com.chineseall.iwanvi.wwlive.web.video.service;

public interface NoticeService {
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
    public   void manExitLive(long videoId,String zb_chatRoomId, Long zb_anchorId,long exitUserId) ;
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
     *
     */

    public   void manJoinLive(long videoId,String zb_chatRoomId, Long zb_anchorId,long joinUserId) ;
    /**
     * 每进入一个真实用户调用一次。触发修改直播间人数
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
     *  @param userId
     *            //送礼物的人的用户ID--string
     */
    public   void manSendGift(long videoId,String zb_chatRoomId, Long zb_anchorId, Long goodsId, double unitPrice, long userId);
}
