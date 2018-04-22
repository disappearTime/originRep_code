package com.chineseall.iwanvi.wwlive.web.game.utils;

import com.chineseall.iwanvi.wwlive.domain.wwlive.GameGift;
import org.apache.log4j.Logger;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * Created by lvliang on 2017/10/11.
 */
public class SortToListUtil {
    private static Logger logger = Logger.getLogger(SortToListUtil.class);
    /**
     * map 排序共用方法
     * @param gameGiftList 对象列表
     * @param order 升序：asc 降序 desc
     * @return
     */
    public static List<GameGift> getSortToList(List<GameGift> gameGiftList, final String order) {
        Collections.sort(gameGiftList, new Comparator<GameGift>() {
            public int compare(GameGift g1,GameGift g2) {
                String gift1value = String.valueOf(g1.getGameGiftId());
                String gift2value = String.valueOf(g2.getGameGiftId());
                if ("ASC".equals(order.toUpperCase())) {
                    return gift1value.compareTo(gift2value);
                } else {
                    return gift2value.compareTo(gift1value);
                }
            }
        });
        return gameGiftList;
    }

}
