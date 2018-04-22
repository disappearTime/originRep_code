package com.chineseall.iwanvi.wwlive.pc.common.helper;

import org.apache.commons.lang.StringUtils;

import com.chineseall.iwanvi.wwlive.common.cache.RedisClientAdapter;
import com.chineseall.iwanvi.wwlive.common.cache.RedisKey;
import com.chineseall.iwanvi.wwlive.domain.wwlive.LiveVideoInfo;

public class VideoSynchroClickCnt {

	public static void setViewers(
		    RedisClientAdapter redisAdapter, LiveVideoInfo info) {
		 //将缓存中直播的点击次数写入到数据表的viewers字段
        String clickCntKey = RedisKey.LIVING_CLICKCNT_ + info.getVideoId();
        String videoKey = RedisKey.LIVE_VIDEO_INFO_ + info.getVideoId();
        String clickStr = redisAdapter.strGet(clickCntKey);
        if (StringUtils.isNotBlank(clickStr)) {
        	Long clickCnt = Long.valueOf(clickStr);
            info.setViewers(clickCnt);
            redisAdapter.delKeys(clickCntKey, videoKey);
        }
	}
}
