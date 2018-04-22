package com.chineseall.iwanvi.wwlive.web.game.service;

import java.util.Map;

/**
 * Created by Niu Qianghong on 2017-10-18 0018.
 */
public interface CardGameService {
    Map<String, Object> getBpGiftList(Long userId);

    Map<String,Object> getGameCover(Long anchorId);

    Map<String,Object> findLuckyList(Long userId);

    Map<String,Object> getDayCardFace();

    Map<String, Object> processingLuckDraw(int type, long userId, String userName, long anchorId,
                                           long videoId, int count, String roomNum, String chatroomId,String mode) throws Exception;

    Map<String,Object> giveGift(Long userId, Long anchorId, Long videoId, Integer goodsId, Integer giftCnt, String app);
}
