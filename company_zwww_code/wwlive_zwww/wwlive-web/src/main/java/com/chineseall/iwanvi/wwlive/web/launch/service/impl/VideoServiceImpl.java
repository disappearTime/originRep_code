package com.chineseall.iwanvi.wwlive.web.launch.service.impl;

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.chineseall.iwanvi.wwlive.dao.wwlive.*;
import com.chineseall.iwanvi.wwlive.web.common.helper.ContribHelper;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.chineseall.iwanvi.wwlive.common.cache.RedisClientAdapter;
import com.chineseall.iwanvi.wwlive.common.cache.RedisExpireTime;
import com.chineseall.iwanvi.wwlive.common.cache.RedisKey;
import com.chineseall.iwanvi.wwlive.common.constants.Constants;
import com.chineseall.iwanvi.wwlive.common.exception.IWanviDataBaseException;
import com.chineseall.iwanvi.wwlive.common.exception.IWanviException;
import com.chineseall.iwanvi.wwlive.common.external.kscloud.KSCloudFacade;
import com.chineseall.iwanvi.wwlive.common.external.rongcloud.RongCloudFacade;
import com.chineseall.iwanvi.wwlive.common.external.rongcloud.domain.ChatroomInfo;
import com.chineseall.iwanvi.wwlive.common.external.rongcloud.domain.SdkHttpResult;
import com.chineseall.iwanvi.wwlive.common.external.rongcloud.domain.TxtMessage;
import com.chineseall.iwanvi.wwlive.common.tools.HttpUtils;
import com.chineseall.iwanvi.wwlive.dao.base.mysql.MysqlSequenceGen;
import com.chineseall.iwanvi.wwlive.domain.wwlive.Anchor;
import com.chineseall.iwanvi.wwlive.domain.wwlive.LiveVideoInfo;
import com.chineseall.iwanvi.wwlive.web.common.constants.WebConstants;
import com.chineseall.iwanvi.wwlive.web.common.helper.AnchorInfoHelper;
import com.chineseall.iwanvi.wwlive.web.common.helper.RoleNobleHelper;
import com.chineseall.iwanvi.wwlive.web.common.util.DateTools;
import com.chineseall.iwanvi.wwlive.web.launch.service.VideoService;
import com.zw.zcf.util.MapUtils;

@Service("launchVideoService")
public class VideoServiceImpl implements VideoService{
    
    static final Logger LOGGER = Logger.getLogger(VideoServiceImpl.class);
    
    @Autowired
    private OrderInfoMapper orderMapper;
    
    @Autowired
    private RedisClientAdapter redisAdapter;
    
    @Autowired
    private LiveVideoInfoMapper videoMapper;
    
    @Autowired
    private AnchorMapper anchorMapper;
    
    @Autowired
    MysqlSequenceGen mysqlSequenceGen;
    
    @Autowired
    UserVideoMapper userVideoMapper;

    @Autowired
    LiveVideoInfoMapper liveVideoInfoMapper;
    
    @Autowired
    private RoleInfoMapper roleInfoMapper;
    
    @Value("${app.system}")
    private String appSystem;

    @Autowired
    private ContributionListMapper contribMapper;

    @Override
    public Map<String, Object> getGoodsList(Integer pageNo, Integer pageSize, Long videoId) {
        int startRow = (pageNo - 1) * pageSize;
        List<Map<String, Object>> goodsList = orderMapper.getGoodsListByVideoId(startRow, pageSize, videoId);
        
        // 添加土豪勋章信息
        String richestUserId = redisAdapter.strGet(RedisKey.RICHEST_MEDAL_OWNER);
        for(Map<String, Object> item:goodsList){
            Long userId = MapUtils.getLongValue(item, "userId", 0);
            item.put("isRichest", userId.toString().equals(richestUserId) ? 1 : 0);
            Integer level = RoleNobleHelper.userRoleNobleLevel(redisAdapter, roleInfoMapper, userId);
            item.put("nobleCode", level);
        }
        
        Map<String, Object> data = new HashMap<>();
        data.put("goodsList", goodsList);
        return data;
    }

    @Override
    public Map<String, Object> getEndInfo(Long videoId, Long anchorId, String streamName) {
        
        //结束时把流放到黑名单中, 并清除相关的缓存信息[videoInfo和正在直播列表]
        long viewers = addVideoToBlackList(videoId, anchorId, streamName);
        
        Map<String, Object> data = new HashMap<>();
        
        //获取用户数set
        //获取送礼数
        int goodsCnt = orderMapper.getGoodsCntByVideoId(videoId);
        //获取直播时长
        Map<String, Object> timeMap = videoMapper.getBeginAndEndTime(videoId);
        long duration = 0;
        if(timeMap != null){            
            Date beginTime = (Date) timeMap.get("beginTime");
            Date endTime = (Date) timeMap.get("endTime");
            if(endTime == null){
                duration = new Date().getTime() - beginTime.getTime();
            } else{
                duration = endTime.getTime() - beginTime.getTime();
            }
        }
        
        //获取公告
        String anchorKey = RedisKey.ANCHOR_INFO_ + anchorId;        
        //先从redis中获取主播信息
        Map<String, String> anchorInfo = redisAdapter.hashGetAll(anchorKey);
        if(anchorInfo == null 
                || anchorInfo.isEmpty() 
                || anchorInfo.get("userName") == null){
            //从数据库中查询并保存到redis中
            Anchor anchor = anchorMapper.findAnchorById(anchorId);        
            data.put("notice", anchor.getNotice());
            redisAdapter.hashMSet(anchorKey, anchor.putFieldValueToStringMap());
            redisAdapter.expireKey(anchorKey, RedisExpireTime.EXPIRE_DAY_30);
        } else{
            data.put("notice", anchorInfo.get("notice"));
        }
        
        String notice = (String) data.get("notice");
        if(StringUtils.isEmpty(notice)){
            data.put("notice", "主播很懒, 什么都没有留下");
        }
        
        data.put("viewers", viewers);
        data.put("giftCnt", goodsCnt);
        data.put("duration", duration);
        
        return data;
    }
    
    /**
     * 通知直播结束
     * @param info
     */
    private void noticeForLiveStop(String chatRoomId, Long anchorId) {
        try {
            List<String> chatIds = new ArrayList<String>();
            chatIds.add(chatRoomId);
            TxtMessage tx = new TxtMessage("");
            JSONObject json = new JSONObject();
            json.put("dataType", new Integer(5));
            json.put("dataValue", "");
            json.put("dataExtra", "");
            tx.setExtra(json.toJSONString());
            RongCloudFacade.publishChatroomMessage(anchorId.toString(), chatIds, tx, Constants.FORMAT_JSON);
        } catch (Exception e) {
            LOGGER.error("通知失败，ChatroomId：" + chatRoomId);
        }
    }

    /**
     * 视频流添加到黑名单中, 并清除相关缓存, 表现为"直播结束"
     * @param videoId
     * @param streamName
     * @return 该视频返回人数
     */
    private long addVideoToBlackList(Long videoId, Long anchorId, String streamName) {
        
        int exist = videoMapper.existLiving(videoId);
        long real = redisAdapter.setCard(RedisKey.LIVING_VIDEO_VIEWED_USERS_ + videoId);
        if (exist == 0) {
            String videoKey = RedisKey.VideoKeys.LIVING_VIDEOS_;
            redisAdapter.zsetRem(videoKey, anchorId + "");//从正在直播列表中移除该video
			videoKey = RedisKey.VideoGrayKeys.LIVING_VIDEOS_;//灰度代码
			redisAdapter.zsetRem(videoKey, anchorId + "");//灰度代码
            return real;
        }
        
        //将视频流加入黑名单
        String url = KSCloudFacade.addBlack(KSCloudFacade.LIVE_NAME, streamName);
        SdkHttpResult result = null;
        try {
            HttpURLConnection conn = HttpUtils
                    .createPostHttpConnection(url);
            result = HttpUtils.returnResult(conn);
        } catch (Exception e) {
            LOGGER.error("停止直播异常", e);
            return real;
        }
        
        int viewers = 0;
        if (result != null && result.getHttpCode() == 200) {
            //如果有录播设为4
            //从redis中移除该视频id
            String videoKey = RedisKey.VideoKeys.LIVING_VIDEOS_;
            long rem = redisAdapter.zsetRem(videoKey, anchorId + "");
            
			videoKey = RedisKey.VideoGrayKeys.LIVING_VIDEOS_;//灰度代码
			redisAdapter.zsetRem(videoKey, anchorId + "");//灰度代码
            
            LOGGER.info("关闭视频直播, " + videoId + ", 删除" + rem);
            
            if (LOGGER.isDebugEnabled()) {
                LOGGER.info("关闭视频直播, " + videoId + ", 删除" + rem);
            }
            if (rem > 0) {
            	viewers = updateLiveVideo(videoId, anchorId);
            	if (real < viewers) {
            		real = viewers;
            	}
            }
        }
        
        noticeForLiveStop(streamName, anchorId);
        LOGGER.info("直播停止成功, streamName = " + streamName);  
        return real;
    }
    
    /**
     * 更新直播信息，同步redis缓存
     * @param videoId
     * @param anchorId
     * @return viewers 观众数
     */
    private int updateLiveVideo(long videoId, long anchorId) {
        
        LiveVideoInfo info = videoMapper.findVideoInfoById(videoId);
        //从redis中取出观看人数持久化到数据库, 并删除redis中的记录
        userVideoMapper.updateVidoeIdByVideoId(info.getVideoId());

        LiveVideoInfo tmp = new LiveVideoInfo();
        tmp.setAnchorId(anchorId);
        tmp.setVideoId(videoId);
        
        //将缓存中直播的点击次数写入到数据表的viewers字段
        String clickCntKey = RedisKey.LIVING_CLICKCNT_ + tmp.getVideoId();
        String videoKey = RedisKey.LIVE_VIDEO_INFO_ + info.getVideoId();
        String viewers = redisAdapter.hashGet(videoKey, "viewers");
        String clickStr = redisAdapter.strGet(clickCntKey);
        redisAdapter.delKeys(videoKey);

        if(StringUtils.isNotBlank(clickStr)) {
        	Long clickCnt = Long.valueOf(clickStr);
            tmp.setViewers(clickCnt);
            redisAdapter.delKeys(clickCntKey, videoKey);
        }
        
        tmp.setEndTime(new Date());
        if (StringUtils.isBlank(info.getVdoid())) {
            tmp.setVideoStatus(Constants._2);
        } else {
            tmp.setVideoStatus(Constants._4);
        }
        if(info.getVideoStatus() == Constants._3) {
        	return 1;
        }
        int stop = 0;
        stop = videoMapper.updateByPKAndAnchorId(tmp);
        if (stop <= 0) {
        	LOGGER.info("更新视频失败" + videoId);
        }
        if (StringUtils.isBlank(viewers)) {
        	viewers = "0";
        }
        return Integer.parseInt(viewers);
    }

    public Map<String, Object> videoConsInfo(Long videoId) {
    	
    	return videoMapper.getConsInfoByVideoId(videoId);
    }
    
    @Override
    public Map<String, Object> addLaunchedVideo(String coverImgUrl, String videoName, Long anchorId, int formatType,
            boolean record, int videoType) {
    	
    	removeLingVideo(anchorId);
    	
        // 查询主播信息，创建流生成聊天室id，视频流id放入zset中
        //返回推流rtmp地址
        Anchor anchor = anchorMapper.getBriefAnchorInfo(anchorId);
        Map<String, Object> data = new HashMap<>();
        if (anchor != null) {
            LiveVideoInfo info = createLiveVideoInfo(anchor, videoName,
                    videoType, coverImgUrl, anchorId, record, formatType);
            videoMapper.delReadyVideos(anchorId);
            if( videoMapper.insertLiveVideoInfo(info) > 0) {
                try {
                    SdkHttpResult rongResult = RongCloudFacade.joinChatroom(anchorId + "", info.getStreamName(), Constants.FORMAT_JSON);
                    if (rongResult.getHttpCode() != 200) {
                        throw new IWanviException("创建视频流失败：聊天室失败。");
                    }
                } catch (Exception e) {
                    throw new IWanviException("创建视频流失败：", e);
                }
            }

            // 将主播牌面修改为直播封面
            anchorMapper.updateCardFace(anchorId, coverImgUrl);
            redisAdapter.delKeys(RedisKey.ANCHOR_INFO_ + anchorId);

            // 创建贡献值缓存
            ContribHelper.cacheNormal(anchorId, contribMapper, redisAdapter);

            data.put("rtmpURL", info.getRtmpUrl().substring(info.getRtmpUrl().lastIndexOf("/") + 1));
            data.put("videoId", info.getVideoId());
            data.put("chatroomId", info.getStreamName());
            return data;
        } else{
            throw new IWanviException("主播不存在!");
        }        
    }
    
    /**
     * 删除该主播正在直播的视频
     * @param anchorId
     */
    private void removeLingVideo(Long anchorId) {
        List<LiveVideoInfo> livings = videoMapper.getLivingsByAnchorId(anchorId);
        if(livings != null){
        	List<String> videoId = new ArrayList<String>();
            for(LiveVideoInfo video : livings){
            	videoId.add(video.getVideoId().toString());
                redisAdapter.delKeys(RedisKey.LIVE_VIDEO_INFO_ + video.getVideoId());
            	KSCloudFacade.addBlack(KSCloudFacade.LIVE_NAME, video.getStreamName());
            }
            
            if (videoId.size() > 0) {
            	videoMapper.delLivingVideoByAnchorId(anchorId);
                String videoKey = RedisKey.VideoKeys.LIVING_VIDEOS_;
                redisAdapter.zsetRem(videoKey, anchorId + "");
    			videoKey = RedisKey.VideoGrayKeys.LIVING_VIDEOS_;//灰度代码
    			redisAdapter.zsetRem(videoKey, anchorId + "");//灰度代码
            }
            
        }
    }
    
    /**
     * 获得Live 名称
     * 
     * @return
     */
    private String getStreamName() {
        int num = 7;
        if (Boolean.TRUE.toString().equals(appSystem)) {
            num = 12;
            return mysqlSequenceGen.getNextval(WebConstants.SEQ_STREAM_ID, "", num);
        }
        return mysqlSequenceGen.getNextval(WebConstants.SEQ_STREAM_ID,
                WebConstants.STREAMP_REFIX, num);
    }

    private String getVdoid() {
        return mysqlSequenceGen.getNextval(WebConstants.SEQ_VDOID, "", 7);
    }
    
    private String getRtmpUrl(String streamName, boolean needRecord, String vdoid) {
        String resource = KSCloudFacade.PRESET + Constants.EQU
                + KSCloudFacade.IWANVI_PRESET_NAME + Constants.AND
                + KSCloudFacade.PUBLIC + Constants.EQU + Constants._0;
        if (needRecord) {
            resource += Constants.AND + KSCloudFacade.VDOID + Constants.EQU
                    + vdoid;
        }
        return KSCloudFacade.buildAppRtmp(streamName, resource);
    }

    // 融云通讯
    private void createchatRoomId(String chatRoomId, String name) {
        ChatroomInfo chatroom = new ChatroomInfo(chatRoomId, name);
        SdkHttpResult result = RongCloudFacade.createChatroom(chatroom,
                Constants.FORMAT_JSON);
        if (result.getHttpCode() != 200) {
            throw new IWanviDataBaseException("生成聊天室失败。");
        }
    }
    
    /**
     * 生成点播文件m3u8
     * http://ks3-cn-beijing.ksyun.com/test-iwanvi/record/live/LIVEZX156491833A0/hls/LIVEZX156491833A0-1470998780.m3u8
     * @param streamName
     * @param vdoid
     * @return
     */
    private String m3u8URL(String streamName, String vdoid) {
        return KSCloudFacade.IWANVI_M3U8 + streamName 
                + Constants.SEPARATOR 
                + KSCloudFacade.HLS 
                + Constants.SEPARATOR + streamName + Constants.MINUS + vdoid + KSCloudFacade.M3U8;
    }
    
    /**
     * 生成视频对象
     * @param anchor
     * @param videoName
     * @param videoType
     * @param coverImg
     * @param anchorId
     * @param needRecord
     * @return
     */
    private LiveVideoInfo createLiveVideoInfo(Anchor anchor, String videoName,
            int videoType, String coverImg, long anchorId, boolean needRecord, int formatType) {

        Long roomNum = anchor.getRoomNum();
        Integer acctType = anchor.getAcctType();//0普通账号 1测试账号
        
        LiveVideoInfo info = new LiveVideoInfo();
        info.setAnchorId(anchorId);
        info.setRoomNum(roomNum);
        String streamName = getStreamName();
        if (acctType != null && acctType.intValue() == 1) {
        	streamName = Constants.VIDEO_TEST_SIGN + streamName;
        }
        info.setStreamName(streamName);

        String vdoid = "";
        String vdoidURL = "";
        if (needRecord) {
            vdoid = getVdoid();
            vdoidURL = m3u8URL(streamName, vdoid);
        }
        info.setVdoid(vdoidURL);
        info.setRtmpUrl(this.getRtmpUrl(streamName, needRecord, vdoid));
        createchatRoomId(streamName, videoName);
        info.setChatroomId(streamName);
        info.setCoverImg(coverImg);
        info.setVideoName(videoName);
        info.setFormatType(formatType);

        info.setVideoType(Constants._0);
        info.setVideoStatus(Constants._0);
        info.setViewers(0L);

        Date date = new Date();
        info.setCreateTime(date);
        info.setUpdateTime(date);
        info.setVersionOptimizedLock(0);

        return info;
    }

    @Override
    public Map<String, Object> getHistoryVideoDetail(long anchorId, long videoId) {
        Map<String, Object> resultJson = new HashMap<>();
        Map<String, Object> historyDetail = new HashMap<>();
        String anchorKey = RedisKey.ANCHOR_INFO_ + anchorId;
        Anchor anchor  = null;
        try {
            if (!redisAdapter.existsKey(anchorKey)) {
            	anchor = AnchorInfoHelper.getAndCacheAnchorInfo(redisAdapter, anchorMapper, anchorId, "userName", "headImg", "sex");
            } else {
                Map<String, String> anchorDetail = redisAdapter.hashMGet(anchorKey, "userName", "headImg", "sex");
                anchor = new Anchor();
                anchor.doStringMapToValue(anchorDetail);
            }
        } catch (Exception e) {
        	LOGGER.error("主播信息转换异常：", e);
        }
    	if (anchor != null) {
        	historyDetail.put("userName", anchor.getUserName() == null ? "" : anchor.getUserName());
        	historyDetail.put("headImg", anchor.getHeadImg() == null ? "" : anchor.getHeadImg());
        	historyDetail.put("sex", anchor.getSex() == null ? "" : anchor.getSex());
    	} else {
        	historyDetail.put("userName", "");
        	historyDetail.put("headImg", "");
        	historyDetail.put("sex", "");
    	}
        //获取历史直播详情
        String histKey = RedisKey.LIVE_VIDEO_INFO_ + videoId;
        if(redisAdapter.existsKey(histKey)){
            //从redis中获取直播详情
            Map<String, String> histDetail = redisAdapter.hashMGet(histKey, "videoId", "viewers", "roomNum", "vdoid", "streamName");
            historyDetail.putAll(histDetail);
        } else{
            //查询数据库获取直播详情
        	LiveVideoInfo info = videoMapper.findVideoInfoById(videoId);
        	historyDetail.put("videoId", info.getVideoId());
        	historyDetail.put("viewers", info.getViewers());
        	historyDetail.put("roomNum", info.getRoomNum());
        	historyDetail.put("vdoid", info.getVdoid());
        	historyDetail.put("streamName", info.getStreamName());
            //返回的字段值都转换成String类型
            Map<String, Object> map = info.putFieldValueToMap();
            map.put("anchorName",liveVideoInfoMapper.getAnchorNameById(info.getAnchorId()));
            redisAdapter.hashMSet(histKey, map);//缓存到redis中
            //设置缓存信息有效期
            redisAdapter.expireKey(histKey, RedisExpireTime.EXPIRE_DAY_7);
            
        }
        
        if (historyDetail == null || historyDetail.isEmpty()) {
            return resultJson;
        }
        
        resultJson.putAll(historyDetail);        
        return resultJson;
    }

    @Override
    public int getAnchorIncome(Long anchorId) {
        String deadline = DateTools.getAWeekAgoDate(Calendar.getInstance());
        int income = orderMapper.getWeekInocme(anchorId, deadline);
        return income;
    }
    
    public Integer sendLiveMsg(long msgType, long videoId) {
    	if (msgType == 0L) {// 0 停止 1开始
    		String key = RedisKey.STOP_VIDEO_NOTICE_ + videoId;
    		redisAdapter.strSetexByNormal(key, RedisExpireTime.EXPIRE_HOUR_1, "0");
    	}else if (msgType == 1L) {
    		String key = RedisKey.START_VIDEO_NOTICE_ + videoId;
    		redisAdapter.strSetexByNormal(key, RedisExpireTime.EXPIRE_HOUR_1, "0");
    	}
    	return new Integer(1);
    }
    
}
