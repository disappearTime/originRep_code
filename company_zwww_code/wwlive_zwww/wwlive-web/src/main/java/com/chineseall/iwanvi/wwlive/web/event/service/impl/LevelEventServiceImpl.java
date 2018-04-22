package com.chineseall.iwanvi.wwlive.web.event.service.impl;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.MapUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.chineseall.iwanvi.wwlive.common.cache.RedisClientAdapter;
import com.chineseall.iwanvi.wwlive.common.cache.RedisExpireTime;
import com.chineseall.iwanvi.wwlive.common.cache.RedisKey;
import com.chineseall.iwanvi.wwlive.common.constants.Constants;
import com.chineseall.iwanvi.wwlive.common.enums.EventEnum;
import com.chineseall.iwanvi.wwlive.dao.wwlive.LiveVideoInfoMapper;
import com.chineseall.iwanvi.wwlive.dao.wwlive.UserLevelMapper;
import com.chineseall.iwanvi.wwlive.domain.wwlive.LiveVideoInfo;
import com.chineseall.iwanvi.wwlive.web.event.service.LevelEventService;

@Service
public class LevelEventServiceImpl implements LevelEventService{

    @Autowired
    private UserLevelMapper userLevelMapper;
    
    @Autowired
    private RedisClientAdapter redisAdapter;
    
    @Autowired
    private LiveVideoInfoMapper videoMapper;
    
    private static final int DIAMONDS_PER_LEVEL = 20;// 每关需要的钻石数
    
    private Logger logger = Logger.getLogger(this.getClass());
    
    @Override
    public List<Map<String, Object>> getAnchorRank() {

        String livingKey = RedisKey.VideoGrayKeys.LIVING_VIDEOS_;
        Set<String> livingAnchorIds = redisAdapter.zsetRange(livingKey, 0, -1);// 获取正在直播的主播id

    	List<Map<String, Object>> anchorRank = null;
        try {
        	anchorRank = userLevelMapper.getTop10Anchor(EventEnum.GODDESS.getEventCode());
        	if (anchorRank == null || anchorRank.isEmpty()) {
        		return anchorRank;
        	}
            int rank = 1;
            for(Map<String, Object> anchor : anchorRank){
                // 关卡为0的主播不显示在排行榜中
                int level = MapUtils.getIntValue(anchor, "level", 0);
                if(level == 0){
                    continue;
                }

                Long anchorId = MapUtils.getLongValue(anchor, "anchorId", 0);
                if(livingAnchorIds.contains(anchorId.toString())){
                    LiveVideoInfo live = videoMapper.getLatestLivingVideo(anchorId); // 获取该主播正在直播的视频信息
                    if(live == null){ // 若无正在直播视频
                        anchor.put("isLive", 0);
                        anchor.put("rank", rank);
                        rank++;
                        continue;
                    }
                    anchor.put("videoId", live.getVideoId());
                    anchor.put("chatroomId", live.getChatroomId());
                    anchor.put("type", live.getFormatType());
                    anchor.put("coverImg", live.getCoverImg());
                    anchor.put("rtmpUrl", live.getRtmpUrl());
                    anchor.put("isLive", 1);
                    anchor.put("rank", rank);
                    
                    logger.info("关卡活动主播排行榜--> anchorId = " + anchorId + ", 正在直播的视频信息 = " + live);
                    
                    rank++;
                } else{
                    anchor.put("isLive", 0);
                    anchor.put("rank", rank);
                    rank++;
                }
            }        
        } catch (Exception e) {
        	e.printStackTrace();
        	logger.error("获取主播排行异常: ", e);
        }
        
        
        return anchorRank;
    }

    /**
     * 是否在活动中
     */
    @Override
    public boolean isInEvent() {
        return redisAdapter.existsKey(RedisKey.GODDESS_EVENT_START) ? true : false;
    }
    
    @Override
    public int getEventStatus(){
        if(redisAdapter.existsKey(RedisKey.GODDESS_EVENT_READY)){
            return 0;
        }
        
        if(redisAdapter.existsKey(RedisKey.GODDESS_EVENT_START)){
            return 1;
        }

        if(redisAdapter.existsKey(RedisKey.GODDESS_EVENT_STOP)){
            return 2;
        }
        
        return 1; // 3个key都不存在的时候默认准备状态
    }    

    @Override
    public List<Map<String, Object>> getUserRank(Integer pageNo, Integer pageSize) {
        
        int startRow = (pageNo - 1) * pageSize;
        List<Map<String, Object>> userRank = userLevelMapper.getUserRankByPage(startRow, pageSize, EventEnum.GODDESS.getEventCode());
        
        int rank = 0;
        for(Map<String, Object> user:userRank){
            int place = (pageNo - 1) * pageSize + rank + 1;// 设置排名
            user.put("rank", place);
            rank++;
        }
        
        return userRank;
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
