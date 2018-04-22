package com.chineseall.iwanvi.wwlive.web.game.service;

import com.chineseall.iwanvi.wwlive.domain.wwlive.GameGift;

import java.util.List;
import java.util.Set;

/**
 * 抽奖算法
 * Created by lvliang on 2017/10/18.
 */
public interface DrawLotteryService {
    /**
     * 抽一次奖
     * @param giftType
     * @return
     */
    Set<GameGift> drawLottery(int giftType,int userId,int count,int anchorId) throws Exception;

//    Set<GameGift> drawManyLottery(int giftType, int userId, int count);
}
