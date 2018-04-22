package com.chineseall.iwanvi.wwlive.pc.video.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.chineseall.iwanvi.wwlive.common.cache.RedisClientAdapter;
import com.chineseall.iwanvi.wwlive.common.cache.RedisKey;
import com.chineseall.iwanvi.wwlive.dao.wwlive.ADPublishDetailMapper;
import com.chineseall.iwanvi.wwlive.domain.wwlive.ADPublishDetail;
import com.chineseall.iwanvi.wwlive.pc.video.service.ADPublishService;

@Service
public class ADPublishServiceImpl implements ADPublishService{
    
    @Autowired
    private RedisClientAdapter redisAdapter;
    
    @Autowired
    private ADPublishDetailMapper publishMapper;

   /* @Override
    public Map<String, Object> recordAdPublish(Long videoId, String channelNum, String versionNum, Integer adId,
            Integer adType, Long anchorId, Long roomNum) {
        
        Map<String, Object> resultMap = new HashMap<>();
        //判断视频是否直播结束
        String videoKey = RedisKey.VideoKeys.LIVING_VIDEOS_;
        Long index = redisAdapter.zsetRank(videoKey, anchorId.toString());
        if(index != null){
            //没有结束的话, 保存发布记录
            int result = publishMapper.add(channelNum, versionNum, adId, adType, anchorId, roomNum);
            resultMap.put("result", result);
            if(result == 1){
                resultMap.put("info", "广告发布记录已经保存成功~");
            } else{
                resultMap.put("info", "广告发布记录保存失败, 不过不要方, 请联系技术人员~");
            }
        } else{
            resultMap.put("result", 0);
            resultMap.put("info", "直播已经结束, 广告发布记录未保存.");
        }
        return resultMap;
    }*/

    public Map<String, Object> recordAdPublish(List<ADPublishDetail> list, Long anchorId) {
    	
        Map<String, Object> resultMap = new HashMap<>();
        if (list == null || list.isEmpty()) {
            resultMap.put("result", 0);
            resultMap.put("info", "未选择中任何广告信息.");
        }
        
        //判断视频是否直播结束
        String videoKey = RedisKey.VideoKeys.LIVING_VIDEOS_;
        Long index = redisAdapter.zsetRank(videoKey, anchorId + "");
       if(index == null){
            //没有结束的话, 保存发布记录
            int result = publishMapper.insertByBatch(list);
            resultMap.put("result", result);
            if(result >= 1){
                resultMap.put("info", "广告发布记录已经保存成功~");
            } else{
                resultMap.put("info", "广告发布记录保存失败, 不过不要方, 请联系技术人员~");
            }
        } else{
            resultMap.put("result", -1);
            resultMap.put("info", "直播已经结束, 广告发布记录未保存.");
        }
        return resultMap;
    }

}
