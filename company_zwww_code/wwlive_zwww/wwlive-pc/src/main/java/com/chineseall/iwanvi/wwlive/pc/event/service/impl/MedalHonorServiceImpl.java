package com.chineseall.iwanvi.wwlive.pc.event.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.chineseall.iwanvi.wwlive.common.cache.RedisClientAdapter;
import com.chineseall.iwanvi.wwlive.common.cache.RedisExpireTime;
import com.chineseall.iwanvi.wwlive.common.cache.RedisKey;
import com.chineseall.iwanvi.wwlive.common.enums.MedalEnum;
import com.chineseall.iwanvi.wwlive.dao.wwlive.MedalHonorMapper;
import com.chineseall.iwanvi.wwlive.pc.event.service.MedalHonorService;

@Service
public class MedalHonorServiceImpl implements MedalHonorService{
    
    @Autowired
    private MedalHonorMapper medalHonorMapper;
    
    @Autowired
    private RedisClientAdapter redisAdapter; 
    
    /**
     * 判断女神活动是否在进行中
     * @return
     */
    private boolean isInEvent() {
        return redisAdapter.existsKey(RedisKey.GODDESS_EVENT_START) ? true : false;
    }
    
    /**
     * 通过主播id查询女神勋章
     */
    @Override
    public List<String> getGoddessMedal(Long anchorId) {        
        
        List<String> medals = new ArrayList<>();
        
        if(isInEvent() || anchorId == null){
            return medals;
        }
        
        if(redisAdapter.existsKey(RedisKey.GODDESS_MEDAL_OWNER)){
            String goddessId = redisAdapter.strGet(RedisKey.GODDESS_MEDAL_OWNER);
            if(anchorId.toString().equals(goddessId)){
                medals.add("女神勋章");
            }
            redisAdapter.expireKey(RedisKey.GODDESS_MEDAL_OWNER, RedisExpireTime.EXPIRE_DAY_180);
        } else{
            Long goddessId = medalHonorMapper.getWinnerByMedal(MedalEnum.GODDESS.getMedalCode());
            if(goddessId == null){
                return medals;
            }
            redisAdapter.strSet(RedisKey.GODDESS_MEDAL_OWNER, goddessId.toString());
            redisAdapter.expireKey(RedisKey.GODDESS_MEDAL_OWNER, RedisExpireTime.EXPIRE_DAY_180);
            if(anchorId.equals(goddessId)){
                medals.add("女神勋章");
            }
        }
        return medals;
    }
    
    /**
     * 获取用户勋章, 用户的土豪勋章只显示30天
     */
    @Override
    public List<String> getUserMedalsById(Long userId){

        List<String> medals = new ArrayList<>();
        
        if(isInEvent() || userId == null){
            return medals;
        }
        
        // 用户勋章信息从redis中获取
        String richestUserId = redisAdapter.strGet(RedisKey.RICHEST_MEDAL_OWNER);
        if(userId.toString().equals(richestUserId)){
            medals.add("土豪勋章");            
        }
        
        return medals;
    }
    
    /**
     * 首页勋章只展示一周
     * @param anchorId
     * @return
     */
    @Override
    public List<String> getIndexAnchorMedal(Long anchorId){
        List<String> medals = new ArrayList<>();
        
        if(isInEvent() || anchorId == null){
            return medals;
        }
        
        String winnerId = redisAdapter.strGet(RedisKey.HOMEPAGE_GODDESS_HONOR);
        if(anchorId.toString().equals(winnerId)){
            medals.add("女神勋章");
        }
        return medals;
    }

}
