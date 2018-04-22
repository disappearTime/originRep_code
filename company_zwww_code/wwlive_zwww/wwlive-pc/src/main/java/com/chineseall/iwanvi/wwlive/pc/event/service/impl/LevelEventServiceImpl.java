package com.chineseall.iwanvi.wwlive.pc.event.service.impl;

import java.math.BigDecimal;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.chineseall.iwanvi.wwlive.common.cache.RedisClientAdapter;
import com.chineseall.iwanvi.wwlive.common.cache.RedisExpireTime;
import com.chineseall.iwanvi.wwlive.common.cache.RedisKey;
import com.chineseall.iwanvi.wwlive.common.constants.Constants;
import com.chineseall.iwanvi.wwlive.dao.wwlive.UserLevelMapper;
import com.chineseall.iwanvi.wwlive.pc.event.service.LevelEventService;

@Service
public class LevelEventServiceImpl implements LevelEventService{

    @Autowired
    private UserLevelMapper userLevelMapper;
    
    @Autowired
    private RedisClientAdapter redisAdapter;
    
    private static final int DIAMONDS_PER_LEVEL = 20;// 每关需要的钻石数
    
    private Logger logger = Logger.getLogger(this.getClass());
    
    private boolean isInEvent() {
        return redisAdapter.existsKey(RedisKey.GODDESS_EVENT_START) ? true : false;
    }

    @Override
    public Double getCurDiamonds(Long anchorId) {
        
        // 活动未开始返回-1
        if(!isInEvent()){
            return -1.0;
        }
        
        try {
            Integer score = 0;
            String scoreKey = RedisKey.ANCHOR_GIFT_SCORE_ + anchorId;
            if(redisAdapter.existsKey(scoreKey)){
                String scoreStr = redisAdapter.strGet(scoreKey);
                score = Integer.valueOf(scoreStr);
            } else{
                score = userLevelMapper.getScoreById(anchorId, Constants._0);//类型 0-主播; 1-用户   
                if(score == null){
                    score = 0;
                }
                redisAdapter.strSet(scoreKey, score);
                redisAdapter.expireKey(scoreKey, RedisExpireTime.EXPIRE_DAY_30);
            }
            
            BigDecimal accurateDiamonds = new BigDecimal(score.toString()).divide(new BigDecimal("100.0"));// 数据表中score以分为单位
            double diamonds = accurateDiamonds.doubleValue();
            
            logger.info("关卡活动获取钻石--> anchorId = " + anchorId + ", 钻石数 = " + diamonds);        
            return diamonds;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1.0;
    }

    @Override
    public Integer getCurLevels(Long anchorId) {
        
        Double diamonds = getCurDiamonds(anchorId);
        if(diamonds != -1){
            int levels = (int) (diamonds / DIAMONDS_PER_LEVEL);
            logger.info("关卡活动获取关卡--> anchorId = " + anchorId + ", 关卡数 = " + levels);
            return levels;
        } else{
            return -1;
        }
    }

}
