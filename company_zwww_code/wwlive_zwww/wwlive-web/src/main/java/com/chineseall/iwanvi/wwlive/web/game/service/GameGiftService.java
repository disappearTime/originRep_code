package com.chineseall.iwanvi.wwlive.web.game.service;

import com.chineseall.iwanvi.wwlive.domain.wwlive.GameGift;

import java.util.List;

/**
 * 游戏奖品列表
 * Created by lvliang on 2017/10/18.
 */
public interface GameGiftService {
    //获取全部奖项
    List<GameGift> getGameGifts(int giftType);
    //获取最小奖项
    GameGift getMinGameGift(int giftType);
}
