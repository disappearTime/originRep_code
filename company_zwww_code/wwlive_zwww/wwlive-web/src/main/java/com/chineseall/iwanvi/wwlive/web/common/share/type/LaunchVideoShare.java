package com.chineseall.iwanvi.wwlive.web.common.share.type;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.chineseall.iwanvi.wwlive.common.cache.RedisClientAdapter;
import com.chineseall.iwanvi.wwlive.common.cache.RedisKey;
import com.chineseall.iwanvi.wwlive.dao.wwlive.AnchorMapper;
import com.chineseall.iwanvi.wwlive.domain.wwlive.Anchor;
import com.chineseall.iwanvi.wwlive.web.common.helper.AnchorInfoHelper;
import com.chineseall.iwanvi.wwlive.web.common.share.DefaultLaunchShare;

@Component
public class LaunchVideoShare extends DefaultLaunchShare{

    @Autowired
    private AnchorMapper anchorMapper;
    
    @Autowired
    private RedisClientAdapter redisAdapter;
    
    @Value("${domain.name}")
    private String domainName;
    
	@Override
	public Map<String, String> getShareInfo(Long anchorId, Long videoId, Integer shareKind) {
		String anchorKey = RedisKey.ANCHOR_INFO_ + anchorId;
        //先从redis中获取主播信息
        Map<String, String> anchorInfo = redisAdapter.hashMGet(anchorKey, "headImg", "userName");
        if(anchorInfo == null 
        		|| anchorInfo.isEmpty() 
        		|| anchorInfo.get("headImg") == null){
            //从数据库中查询并保存到redis中
            Anchor anchor = AnchorInfoHelper.getAndCacheAnchorInfo(redisAdapter, anchorMapper, anchorId);
            anchorInfo = new HashMap<String, String>();
            if (anchor == null) {
            	return anchorInfo;
            }
            anchorInfo.put("headImg", anchor.getHeadImg());
            anchorInfo.put("userName", anchor.getUserName());
        }
        String headImg = anchorInfo.get("headImg");
        Map<String, String> result = new HashMap<String, String>();	        
        result.put("title", anchorInfo.get("userName") + "正在直播，快来围观！");
        result.put("content", "一大波妹子等你来撩！");
        result.put("url", domainName + "external/livevideo/share?formatType=1&anchorId=" + anchorId + "&videoId=" + videoId);
        if (StringUtils.isNotBlank(headImg)) {
        	if (headImg.contains("_300")) {
        		headImg = headImg.replace("_300", "_70");
        	}
        	result.put("imgUrl", headImg);
        }
		return result;
	}

}
