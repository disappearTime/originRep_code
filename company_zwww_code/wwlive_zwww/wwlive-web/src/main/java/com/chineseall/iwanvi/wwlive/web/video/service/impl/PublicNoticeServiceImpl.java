package com.chineseall.iwanvi.wwlive.web.video.service.impl;

import java.util.Date;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.chineseall.iwanvi.wwlive.common.cache.RedisClientAdapter;
import com.chineseall.iwanvi.wwlive.common.cache.RedisKey;
import com.chineseall.iwanvi.wwlive.web.common.util.DateTools;
import com.chineseall.iwanvi.wwlive.web.video.service.PublicNoticeService;

@Service
public class PublicNoticeServiceImpl implements PublicNoticeService {

//    @Autowired
//    private PublicNoticeMapper publicNoticeMapper;
    
    @Autowired
    private RedisClientAdapter redisAdapter;
    
    private static final String SPERATOR = "|";
    
    @Override
    public String getTodayNotice() {
        StringBuilder notice = new StringBuilder();
        
        String now = DateTools.formatDate(new Date(), "yyyy-MM-dd HH");
        
        Set<String> ids = redisAdapter.setMembers(RedisKey.PRENOTICE_IDS);
        
        String beginTimeField = "beginTime";
        String endTimeField = "endTime";
        String contentField = "content";
        for(String id:ids){
            String noticeKey = RedisKey.PRENOTICE_ + id;
            String endTime = redisAdapter.hashGet(noticeKey, endTimeField);
            String beginTime = redisAdapter.hashGet(noticeKey, beginTimeField);
            // 查询出开始时间  < 当前小时 < 结束时间的公告
            if(StringUtils.isEmpty(endTime) && StringUtils.isEmpty(beginTime)) {
                return "";
            }
            if(now.compareTo(endTime) < 0 && now.compareTo(beginTime) >= 0){
                notice.append(redisAdapter.hashGet(noticeKey, contentField));
                notice.append(SPERATOR);
            }
        }        
        
        /*Date now = new Date();
        String today = DateTools.formatDate(now, "yyyy-MM-dd");
        String noticeKey = RedisKey.PRENOTICE_ + today;
        JSON notice = null;
        if(redisAdapter.existsKey(noticeKey)){
            notice = (JSON) JSON.parse(redisAdapter.strGet(noticeKey));
        } else{
            PublicNotice prenotice = publicNoticeMapper.getBetween(today, today + " 23:59:59");
            notice = (JSON) JSON.toJSON(prenotice);
            redisAdapter.strSetByNormal(noticeKey, notice.toJSONString());
            redisAdapter.expireKey(noticeKey, RedisExpireTime.EXPIRE_DAY_1);
        }*/
        return notice.toString();
    }

}