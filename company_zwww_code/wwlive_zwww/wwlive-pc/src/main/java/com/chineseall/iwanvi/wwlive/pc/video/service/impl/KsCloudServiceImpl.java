package com.chineseall.iwanvi.wwlive.pc.video.service.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import com.alibaba.fastjson.JSONObject;
import com.chineseall.iwanvi.wwlive.common.cache.RedisClientAdapter;
import com.chineseall.iwanvi.wwlive.common.cache.RedisExpireTime;
import com.chineseall.iwanvi.wwlive.common.cache.RedisKey;
import com.chineseall.iwanvi.wwlive.common.constants.Constants;
import com.chineseall.iwanvi.wwlive.common.external.rongcloud.RongCloudFacade;
import com.chineseall.iwanvi.wwlive.common.external.rongcloud.domain.TxtMessage;
import com.chineseall.iwanvi.wwlive.dao.wwlive.*;
import com.chineseall.iwanvi.wwlive.domain.wwlive.Anchor;
import com.chineseall.iwanvi.wwlive.domain.wwlive.LiveVideoInfo;
import com.chineseall.iwanvi.wwlive.pc.common.helper.*;
import com.chineseall.iwanvi.wwlive.pc.video.service.KsCloudService;
import com.chineseall.iwanvi.wwlive.pc.video.service.NoticeService;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 接收到金山云通知后更新直播相关信息
 * @author DIKEPU
 *
 */
@Service
public class KsCloudServiceImpl implements KsCloudService {

    private static final Logger LOGGER = Logger.getLogger(KsCloudServiceImpl.class);
    
    @Autowired
	private LiveVideoInfoMapper liveVideoInfoMapper;
	
    @Autowired
    private RedisClientAdapter redisAdapter;
    
    @Autowired
    private UserVideoMapper userVideoMapper;
    
    @Autowired
    private UserInfoMapper userInfoMapper;

    @Autowired
    private AnchorMapper anchorMapper;

    @Autowired
    private ThreadPoolTaskExecutor taskExecutor;

    @Autowired
    private NoticeService noticeService;

	@Autowired
	private UserPushMapper userPushMapper;
    
    @Value("${live.test.anchor.id}")
    private String testAnchor;

    @Autowired
    private OrderInfoMapper orderInfoMapper;
    
    /**
     * 创新版用户通知
     */
    @Value("${cx.push.url}")
    private String cxPushUrl;
    
    /**
     * <p/>
     * 增加测试账号的通知
     */
    @Transactional
	public void noticeLiveStart(String streamName) {
		LiveVideoInfo info = liveVideoInfoMapper.findVideoInfoByStreamName(streamName);
		if (info == null) {
			LOGGER.error("info为null，streamName：" + streamName);
			return;
		}
		int videoStatus = info.getVideoStatus();
		if (videoStatus == Constants._1) {
			
			//是否在set中
			Double score = redisAdapter.zsetScore(RedisKey.VideoGrayKeys.LIVING_VIDEOS_, 
					info.getAnchorId().toString());
	    	if (score == null) {//加入进去
		        boolean isNotTest = (StringUtils.isBlank(testAnchor) || 
						!Arrays.asList(testAnchor.split(",")).contains(info.getAnchorId().toString()));
				if (isNotTest) {//增加测试账号的通知
					addVideoToRedis(info);
				}else {
					addVideoToGrayRedis(info);
				}
	    	}
			return;
		}
		if (videoStatus != Constants._3) {//只有未删除也能直播     --只有准备中的视频才可以，其他状态的不能直播
			Date now = new Date();

			if (videoStatus == Constants._0) {
				info.setBeginTime(now);
			}
			//视频状态  0准备中 1直播中 2结束无点播 3删除 4结束有点播
			info.setVideoStatus(Constants._1);
			int cnt = liveVideoInfoMapper.updateByPKAndAnchorId(info);
			if (cnt == 0) {
				LOGGER.error("LiveVideoInfo更新失败：" + info.getVideoId());
			} else {
				if (StringUtils.isBlank(testAnchor)) {
					testAnchor = "";
				}

		        boolean isNotTest = (StringUtils.isBlank(testAnchor) || 
						!Arrays.asList(testAnchor.split(",")).contains(info.getAnchorId().toString()));
				if (isNotTest) {//增加测试账号的通知
					addVideoToRedis(info);
				}else {
					addVideoToGrayRedis(info);
				}
				if(info.getFormatType() == 1 
						&& videoStatus == Constants._4){//直播从准备状态到开始状态不需要发送通知
			    	String key = RedisKey.START_VIDEO_NOTICE_ + info.getVideoId();//记录一次
					if (!redisAdapter.existsKey(key)) {
						noticeForLiveRestart(info);//发送直播重新开始的消息
					    redisAdapter.delKeys(RedisKey.STOP_VIDEO_NOTICE_ + info.getVideoId());
			    		redisAdapter.strSetexByNormal(key, RedisExpireTime.EXPIRE_HOUR_1, "0");
					}
		        }
				
	            if (videoStatus == Constants._4) {
	            	if (isNotTest) {
						AnchorLastVideoForHistoryCacheHelper.cacheAnchorLastVideo4Normal(liveVideoInfoMapper, redisAdapter, 
								info.getVideoId(), info.getAnchorId());
	            	} else {
						AnchorLastVideoForHistoryCacheHelper.cacheAnchorLastVideo4Gray(liveVideoInfoMapper, redisAdapter, 
								info.getVideoId(), info.getAnchorId());
	            		
	            	}
			        String videoKey = RedisKey.LIVE_VIDEO_INFO_ + info.getVideoId();
			        redisAdapter.delKeys(videoKey);
	            }
	            
	    		String key = RedisKey.ANCHOR_VIDEO_ + info.getAnchorId();//主播最近视频key
	    		
    			String anchorName = getAnchorName(info.getAnchorId());
    			StartVideoPushMsgThread start = new StartVideoPushMsgThread(cxPushUrl, info, redisAdapter, userInfoMapper, anchorName, noticeService,userPushMapper);
				taskExecutor.execute(start);
	    		redisAdapter.strSetexByNormal(key, RedisExpireTime.EXPIRE_DAY_5, info.getVideoId().toString());
			}
		}
		
	}
    
    private String getAnchorName(Long anchorId) {
    	String anchorName = "";
    	if (redisAdapter.existsKey(RedisKey.ANCHOR_INFO_ + anchorId)) {//数据库anchorName为userNmae
			anchorName = redisAdapter.hashGet(RedisKey.ANCHOR_INFO_ + anchorId, "userName");
		} else {
			Anchor anchor = AnchorInfoHelper.getAndCacheCurrentAnchorInfo(redisAdapter, anchorMapper, anchorId);
			if (anchor != null) {
				anchorName = anchor.getUserName();
			}
		}
    	return anchorName;
    }
    
    private void addVideoToRedis(LiveVideoInfo info) {
    	String key = RedisKey.VideoKeys.LIVING_VIDEOS_;// + DateFormatUtils.format(new Date(), Constants.YY_MM_DD)
		String viewersKey = RedisKey.LIVING_VIDEO_VIEWRES_ + info.getVideoId();
		Double score = null;
    	String gray = RedisKey.VideoGrayKeys.LIVING_VIDEOS_;
		if ((score = redisAdapter.zsetScore(RedisKey.ANCHOR_RANK, info.getAnchorId().toString())) != null) {//后台排序
			redisAdapter.zsetAdd(key, score, info.getAnchorId().toString());
			redisAdapter.zsetAdd(gray, score, info.getAnchorId().toString());
		} else {
			BigDecimal viewers = new BigDecimal(0);
			if (redisAdapter.existsKey(viewersKey)) {
				viewers = viewers.add(new BigDecimal(redisAdapter.setCard(RedisKey.LIVING_VIDEO_VIEWRES_ + info.getVideoId())));
			}
			
			//主播被送礼的排前面
			String orderKey = RedisKey.VIDEO_GIFT_+ info.getAnchorId() + Constants.UNDERLINE 
					+ DateFormatUtils.format(new Date(), Constants.YY_MM_DD);//当日主播所有视频有效
			if (redisAdapter.existsKey(orderKey)) {
				String str = redisAdapter.strGet(orderKey);
				if (StringUtils.isNotBlank(str)) {
					viewers = new BigDecimal(str).add(viewers.divide(new BigDecimal(1000)).setScale(3));
				}
			}
			redisAdapter.zsetAdd(key, viewers.doubleValue(), info.getAnchorId().toString());
			redisAdapter.zsetAdd(gray, viewers.doubleValue(), info.getAnchorId().toString());
		}
		redisAdapter.strSetexByNormal(RedisKey.ANCHOR_LIVING_VIDEO_ + info.getAnchorId(), 
				RedisExpireTime.EXPIRE_DAY_5, info.getVideoId().toString());
    }
    
    private void addVideoToGrayRedis(LiveVideoInfo info) {
    	String key = RedisKey.VideoGrayKeys.LIVING_VIDEOS_;// + DateFormatUtils.format(new Date(), Constants.YY_MM_DD)
		String viewersKey = RedisKey.LIVING_VIDEO_VIEWRES_ + info.getVideoId();
		Double score = null;
		if ((score = redisAdapter.zsetScore(RedisKey.ANCHOR_RANK, info.getAnchorId().toString())) != null) {
			redisAdapter.zsetAdd(key, score, info.getAnchorId().toString());
		} else {
			BigDecimal viewers = new BigDecimal(0);
			if (redisAdapter.existsKey(viewersKey)) {
				viewers = viewers.add(new BigDecimal(redisAdapter.setCard(RedisKey.LIVING_VIDEO_VIEWRES_ + info.getVideoId())));
			}
			
			//主播被送礼的排前面
			String orderKey = RedisKey.VIDEO_GIFT_+ info.getAnchorId() + Constants.UNDERLINE 
					+ DateFormatUtils.format(new Date(), Constants.YY_MM_DD);//当日主播所有视频有效
			if (redisAdapter.existsKey(orderKey)) {
				String str = redisAdapter.strGet(orderKey);
				if (StringUtils.isNotBlank(str)) {
					viewers = new BigDecimal(str).add(viewers.divide(new BigDecimal(1000)).setScale(3));
				}
			}

			redisAdapter.zsetAdd(key, viewers.doubleValue(), info.getAnchorId().toString());
		}
		redisAdapter.strSetexByNormal(RedisKey.ANCHOR_LIVING_VIDEO_ + info.getAnchorId(), 
				RedisExpireTime.EXPIRE_DAY_5, info.getVideoId().toString());
    }

    /**
     * 发送直播重新开始消息
     * @param info
     */
    private void noticeForLiveRestart(LiveVideoInfo info) {
        try {
            List<String> chatIds = new ArrayList<String>();
            chatIds.add(info.getChatroomId());
            TxtMessage tx = new TxtMessage("");
            JSONObject json = new JSONObject();
            json.put("dataType", new Integer(13));
            json.put("dataValue", "");
            json.put("dataExtra", "");
            tx.setExtra(json.toJSONString());
            RongCloudFacade.publishChatroomMessage(info.getAnchorId().toString(), chatIds, tx, Constants.FORMAT_JSON);
        } catch (Exception e) {
            LOGGER.error("通知失败，ChatroomId：" + info.getChatroomId());
        }        
    }
    
    /**
     * 停止直播通知
     * <p/>
     * 增加测试账号的通知
     */
    @Transactional
	public void noticeLiveStop(String streamName) {
		//去掉 zset中的数据
		//发送通知到 chatroom
		//更新所有 user_video表
		if (StringUtils.isEmpty(streamName)) {
			LOGGER.error("streamName为空或null：" + streamName);
			return;
		}

		LiveVideoInfo info = liveVideoInfoMapper.findVideoInfoByStreamName(streamName);
		if (info == null) {
			LOGGER.error("info为null，streamName：" + streamName);
			return;
		}

        boolean isNotTest = (StringUtils.isBlank(testAnchor) || 
				!Arrays.asList(testAnchor.split(",")).contains(info.getAnchorId().toString()));
		if (isNotTest) {//增加测试账号的通知
			removeVideoFromRedis(info, streamName);
		}else {
			removeVideoFromGrayRedis(info, streamName);
		}
		Integer videoStatus = info.getVideoStatus();

        taskExecutor.execute(new StopVideoThread(redisAdapter, info.getAnchorId(), 
        		info.getChatroomId(), info.getFormatType(), info.getVideoId(), orderInfoMapper));
        
		if (videoStatus != null 
				&& videoStatus.intValue() == Constants._2) {
			return;
		}
		if (videoStatus.intValue() == Constants._4) {//为4或者1的需要缓存到回放中
			if (isNotTest) {//灰度代码
				AnchorLastVideoForHistoryCacheHelper.cacheAnchorLastHistoryVideo4TabH5(liveVideoInfoMapper, redisAdapter, info);
			}else {
				AnchorLastVideoForHistoryCacheHelper.cacheAnchorLastHistoryVideo4GrayTabH5(liveVideoInfoMapper, redisAdapter, info);
			}
			return;
		}
		if (videoStatus.intValue() == Constants._1) {//为4或者1的需要缓存到回放中) {
			if (isNotTest) {//灰度代码
				AnchorLastVideoForHistoryCacheHelper.cacheAnchorLastHistoryVideo4TabH5(liveVideoInfoMapper, redisAdapter, info);
			}else {
				AnchorLastVideoForHistoryCacheHelper.cacheAnchorLastHistoryVideo4GrayTabH5(liveVideoInfoMapper, redisAdapter, info);
			}
		}
		
		if (info.getVideoStatus().intValue() == Constants._3) {//3已删除
            return;
		}
        stopVideo(info);//停止视频
	}
	
    private void removeVideoFromRedis(LiveVideoInfo info, String streamName) {
    	String livingKey = RedisKey.VideoKeys.LIVING_VIDEOS_;//从redis中移除该视频id，只要是停止视频都从redis中删除此视频
		long cnt = redisAdapter.zsetRem(livingKey, info.getAnchorId().toString());
		if (cnt <= 0) {
			cnt = redisAdapter.zsetRem(livingKey, info.getAnchorId().toString());
			if (cnt <=0) {
				LOGGER.error("删除直播中的视频，streamName：" + streamName + " 失败");
			}
		}
		redisAdapter.delKeys(RedisKey.ANCHOR_LIVING_VIDEO_ + info.getAnchorId(), RedisKey.LIVE_VIDEO_INFO_ + info.getVideoId());
    	removeVideoFromGrayRedis(info, streamName);
    }

    private void removeVideoFromGrayRedis(LiveVideoInfo info, String streamName) {
    	String livingKey = RedisKey.VideoGrayKeys.LIVING_VIDEOS_;//从redis中移除该视频id，只要是停止视频都从redis中删除此视频
		long cnt = redisAdapter.zsetRem(livingKey, info.getAnchorId().toString());
		if (cnt <= 0) {
			cnt = redisAdapter.zsetRem(livingKey, info.getAnchorId().toString());
			if (cnt <=0) {
				LOGGER.error("删除直播中的视频，streamName：" + streamName + " 失败");
			}
		}
		redisAdapter.delKeys(RedisKey.ANCHOR_LIVING_VIDEO_ + info.getAnchorId(), RedisKey.LIVE_VIDEO_INFO_ + info.getVideoId());
    }
    
    private void stopVideo(LiveVideoInfo info) {
    	Date now = new Date();
		info.setEndTime(now);

		if (StringUtils.isNotEmpty(info.getVdoid())) {//有录播视频
			info.setVideoStatus(Constants._4);
		} else {
			info.setVideoStatus(Constants._2);
		}
    	
		VideoSynchroClickCnt.setViewers(redisAdapter, info);
		int cnt = liveVideoInfoMapper.updateByPKAndAnchorId(info);
		if (cnt > 0) {
			//从redis中取出观看人数持久化到数据库, 并删除redis中的记录
        	String livingViwerKey = RedisKey.LIVING_VIDEO_VIEWRES_ + info.getVideoId();
        	if (redisAdapter.existsKey(livingViwerKey)) {
	            if(info.getFormatType() == 0){
		        	noticeForLiveStop(info);//停止消息
		        }
		        redisAdapter.delKeys(livingViwerKey);
	        }
		}
    }
    
    /**
     * 通知直播结束
     * @param info
     */
    private void noticeForLiveStop(LiveVideoInfo info) {
    	try {
			List<String> chatIds = new ArrayList<String>();
			chatIds.add(info.getChatroomId());
			TxtMessage tx = new TxtMessage("");
			JSONObject json = new JSONObject();
			json.put("dataType", new Integer(5));
			json.put("dataValue", "");
			json.put("dataExtra", "");
			tx.setExtra(json.toJSONString());
			RongCloudFacade.publishChatroomMessage(info.getAnchorId().toString(), chatIds, tx, Constants.FORMAT_JSON);
		} catch (Exception e) {
			LOGGER.error("通知失败，ChatroomId：" + info.getChatroomId(), e);
		}
    }
    
}
