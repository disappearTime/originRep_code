package com.chineseall.iwanvi.wwlive.web.game.utils;

import com.chineseall.iwanvi.wwlive.domain.wwlive.GameGift;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 *
 *  Created by lvliang on 2017/10/13.
 */
public class DrawLotteryUtilBak {

    public static int Drawlottery(List<GameGift> gameGiftList){
    //总的概率区间
    double totalPro = 0d;
    //存储每个奖品新的概率区间
    List<Double> proSection = new ArrayList<Double>();
    proSection.add(0d);
    //遍历每个奖品，设置概率区间，总的概率区间为每个概率区间的总和
    for (GameGift gameGift : gameGiftList) {
        //每个概率区间为奖品概率*10的和
        totalPro += gameGift.getGameGiftProbability()*10;
        proSection.add(totalPro);
    }
    //获取总的概率区间中的随机数
    Random random = new Random();
    double randomPro = (double)random.nextInt((int)totalPro);
    //判断取到的随机数在哪个奖品的概率区间中
    for (int i = 0; i < proSection.size(); i++) {
        if(randomPro >= proSection.get(i)
                && randomPro < proSection.get(i + 1)){
            return i;
        }
    }
    return -1;
}

}
