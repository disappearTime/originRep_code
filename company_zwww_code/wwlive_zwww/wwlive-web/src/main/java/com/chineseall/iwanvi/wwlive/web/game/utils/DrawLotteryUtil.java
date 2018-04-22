package com.chineseall.iwanvi.wwlive.web.game.utils;

import com.chineseall.iwanvi.wwlive.domain.wwlive.GameGift;

import java.util.*;

/**
 * 抽奖工具类<br/>
 * 整体思想：
 * 奖品集合 + 概率比例集合
 * 将奖品按集合中顺序概率计算成所占比例区间，放入比例集合。并产生一个随机数加入其中，排序。</br>
 * 排序后，随机数落在哪个区间，就表示那个区间的奖品被抽中。</br>
 * 返回的随机数在集合中的索引，该索引就是奖品集合中的索引。</br>
 * 比例区间的计算通过概率相加获得。
 *  Created by lvliang on 2017/10/13.
 */
public class DrawLotteryUtil {

    /**
     * 按顺序将概率添加到集合中
     * @param mapList
     * @return
     */
    public static int drawGift(List<GameGift> mapList){
        if(null != mapList && mapList.size()>0){
            List<Double> probList = new ArrayList<Double>(mapList.size());
            for(GameGift gift:mapList){
                //按顺序将概率添加到集合中
                probList.add(gift.getGameGiftProbability());
            }
            return draw(probList);
        }
        return -1;
    }

    /**
     * 抽奖
     * @param probList
     * @return
     */
    public static int draw(List<Double> probList){
        List<Double> sortRateList = new ArrayList<Double>();

        // 计算概率总和
        double sumRate = 0;
        for(Double prob : probList){
            sumRate += prob;
        }
        if(sumRate != 0){
            double rate = 0D;   //概率所占比例
            for(Double prob : probList){
                rate += prob;
                // 构建一个比例区段组成的集合(避免概率和不为1)
                sortRateList.add(rate / sumRate);
            }

            // 随机生成一个随机数，并排序
            double random = Math.random();
            sortRateList.add(random);
            Collections.sort(sortRateList);

            // 返回该随机数在比例集合中的索引
            return sortRateList.indexOf(random);
        }
        return -1;
    }

    /**
     * 根据用户的概率进行抽奖
     * @param pro
     * @return
     */
    public static int drawGiftByUserPro(double pro){
        // 计算概率总和
        double sumRate = 100;
        List<Double> sortRateList = new ArrayList<Double>();
        double rate = 0;   //概率所占比例
        double random = Math.random();
        sortRateList.add(rate/sumRate);
        sortRateList.add(pro/sumRate);
        sortRateList.add(1.0);
        sortRateList.add(random);
        Collections.sort(sortRateList);
        return sortRateList.indexOf(random);
    }

}
