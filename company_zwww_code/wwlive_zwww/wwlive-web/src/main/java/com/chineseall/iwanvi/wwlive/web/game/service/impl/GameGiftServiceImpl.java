package com.chineseall.iwanvi.wwlive.web.game.service.impl;

import com.chineseall.iwanvi.wwlive.dao.wwlive.GameGiftMapper;
import com.chineseall.iwanvi.wwlive.domain.wwlive.GameGift;
import com.chineseall.iwanvi.wwlive.web.game.service.GameGiftService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 游戏奖品列表
 * Created by lvliang on 2017/10/18.
 */
@Service
public class GameGiftServiceImpl implements GameGiftService {
    @Autowired
    private GameGiftMapper gameGiftMapper;
    //获取所有奖项
    @Override
    public List<GameGift> getGameGifts(int giftType) {
        return  gameGiftMapper.getGameGifts(giftType);
    }

    //获取最小奖项
    @Override
    public GameGift getMinGameGift(int giftType) {
        GameGift gameGift = gameGiftMapper.getMinGameGift(giftType);
        return gameGift;
    }

}
