package com.chineseall.iwanvi.wwlive.pc.video.service.impl;

import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.chineseall.iwanvi.wwlive.dao.wwlive.ContributionListMapper;
import com.chineseall.iwanvi.wwlive.pc.common.helper.ContribHelper;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import com.chineseall.iwanvi.wwlive.common.cache.RedisClientAdapter;
import com.chineseall.iwanvi.wwlive.common.cache.RedisKey;
import com.chineseall.iwanvi.wwlive.common.constants.Constants;
import com.chineseall.iwanvi.wwlive.common.exception.IWanviDataBaseException;
import com.chineseall.iwanvi.wwlive.common.exception.IWanviException;
import com.chineseall.iwanvi.wwlive.common.external.kscloud.KSCloudFacade;
import com.chineseall.iwanvi.wwlive.common.external.rongcloud.RongCloudFacade;
import com.chineseall.iwanvi.wwlive.common.external.rongcloud.domain.ChatroomInfo;
import com.chineseall.iwanvi.wwlive.common.external.rongcloud.domain.SdkHttpResult;
import com.chineseall.iwanvi.wwlive.common.tools.HttpUtils;
import com.chineseall.iwanvi.wwlive.dao.base.mysql.MysqlSequenceGen;
import com.chineseall.iwanvi.wwlive.dao.wwlive.AnchorMapper;
import com.chineseall.iwanvi.wwlive.dao.wwlive.LiveVideoInfoMapper;
import com.chineseall.iwanvi.wwlive.dao.wwlive.UserVideoMapper;
import com.chineseall.iwanvi.wwlive.domain.wwlive.Anchor;
import com.chineseall.iwanvi.wwlive.domain.wwlive.LiveVideoInfo;
import com.chineseall.iwanvi.wwlive.pc.common.Page;
import com.chineseall.iwanvi.wwlive.pc.common.helper.AnchorInfoHelper;
import com.chineseall.iwanvi.wwlive.pc.common.helper.AnchorLastVideoForHistoryCacheHelper;
import com.chineseall.iwanvi.wwlive.pc.common.helper.VideoSynchroClickCnt;
import com.chineseall.iwanvi.wwlive.pc.event.service.LevelEventService;
import com.chineseall.iwanvi.wwlive.pc.video.common.PcConstants;
import com.chineseall.iwanvi.wwlive.pc.video.service.LiveVideoService;

@Service("liveVideoService")
public class LiveVideoServiceImpl implements LiveVideoService {

    static final Logger LOGGER = Logger.getLogger(LiveVideoServiceImpl.class);
	@Autowired
	LiveVideoInfoMapper liveVideoInfoMapper;

	@Autowired
	AnchorMapper anchorMapper;

	@Autowired
	MysqlSequenceGen mysqlSequenceGen;
	
	@Autowired
	RedisClientAdapter redisAdapter;
	
    @Autowired
    UserVideoMapper userVideoMapper;

	@Value("${pc.system}")
	private String pcSystem;
	
	@Autowired
	private LevelEventService levelEventService;

	@Autowired
	private ContributionListMapper contribMapper;

	@Override
	public Map<String, Object> createVideoUrl(String videoName, int videoType,
			String coverImg, boolean needRecord, long anchorId) {
		// 查询主播信息，创建流生成聊天室id，视频流id放入zset中
		//返回推流rtmp地址
		Map<String, Object> result = new HashMap<>();
		try {
			Anchor anchor = anchorMapper.getBriefAnchorInfo(anchorId);
			if (anchor != null) {
				LiveVideoInfo info = createLiveVideoInfo(anchor, videoName,
						videoType, coverImg, anchorId, needRecord);
				liveVideoInfoMapper.delReadyVideos(anchorId);
				if( liveVideoInfoMapper.insertLiveVideoInfo(info) > 0) {
						SdkHttpResult rongResult = RongCloudFacade.joinChatroom(anchorId + "", info.getStreamName(), Constants.FORMAT_JSON);
						if (rongResult.getHttpCode() != 200) {
							throw new IWanviException("创建视频流失败：聊天室失败。");
						}
				}
				result.put("rtmpURL", info.getRtmpUrl().substring(info.getRtmpUrl().lastIndexOf("/") + 1));
				result.put("videoId", info.getVideoId());

				// 同时修改牌面
				anchorMapper.updateCardFace(anchorId, coverImg);
				redisAdapter.delKeys(RedisKey.ANCHOR_INFO_ + anchorId);

				// 创建贡献值缓存
				ContribHelper.cacheNormal(anchorId, contribMapper, redisAdapter);
				return result;
			}
		} catch (Exception e) {
			LOGGER.error("创建视频流失败：", e);
			throw new IWanviException("创建视频流失败：", e);
		}
		return result;

	}

	/**
	 * 获得Live 名称
	 * 
	 * @return
	 */
	private String getStreamName() {
		int num = 7;
		if (Boolean.TRUE.toString().equals(pcSystem)) {
			num = 12;
			return mysqlSequenceGen.getNextval(PcConstants.SEQ_STREAM_ID, "", num);
		}
		return mysqlSequenceGen.getNextval(PcConstants.SEQ_STREAM_ID,
				PcConstants.STREAMP_REFIX, num);
	}

	private String getVdoid() {
		return mysqlSequenceGen.getNextval(PcConstants.SEQ_VDOID, "", 7);
	}

	private String getRtmpUrl(String streamName, boolean needRecord, String vdoid) {
		String resource = KSCloudFacade.PRESET + Constants.EQU
				+ KSCloudFacade.IWANVI_PRESET_NAME + Constants.AND
				+ KSCloudFacade.PUBLIC + Constants.EQU + Constants._0;
		if (needRecord) {
			resource += Constants.AND + KSCloudFacade.VDOID + Constants.EQU
					+ vdoid;
		}
		return KSCloudFacade.buildRtmp(streamName, resource);
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
	 * 生成直播信息
	 * @param anchor
	 * @param videoName
	 * @param videoType
	 * @param coverImg
	 * @param anchorId
	 * @param needRecord
	 * @return
	 */
	private LiveVideoInfo createLiveVideoInfo(Anchor anchor, String videoName,
			int videoType, String coverImg, long anchorId, boolean needRecord) {

		Long roomNum = anchor.getRoomNum();
		LiveVideoInfo info = new LiveVideoInfo();
		info.setAnchorId(anchorId);
		info.setRoomNum(roomNum);
		String streamName = getStreamName();
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

		info.setVideoType(Constants._0);
		info.setVideoStatus(Constants._0);
		info.setViewers(0L);

		Date date = new Date();
		info.setCreateTime(date);
		info.setUpdateTime(date);
		info.setVersionOptimizedLock(0);

		return info;
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
	 * 生成点播文件mp4
	 * http://ks3-cn-beijing.ksyun.com/iwanvi-test/LIVEZX156491833A0-1470998780.mp4
	 * @param streamName
	 * @param vdoid
	 * @return
	 */
	/*private String mp4URL(String streamName, String vdoid) {
		return KSCloudFacade.IWANVI_MP4 + vdoid + KSCloudFacade.MP4;
	}*/
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public Page getVideoList(long anchorId, Page page) {
		//视频状态  0准备中 1直播中 2结束无点播 3删除 4结束有点播
		List<Map<String, Object>> lives = new ArrayList<Map<String, Object>>();
		int cnt = liveVideoInfoMapper.countVideos(anchorId);

		page.setTotal(cnt);
		if (cnt == 0 || cnt < page.getStart()) {
			page.setData(lives);
			return page;
		}
		if (page.getStart() == 0) {//第一页
			List<Map<String, Object>> living = liveVideoInfoMapper.findVideoByPCAnchorId(anchorId, Constants._1, 0, 1);
			List<Map<String, Object>> liveds = null; 
			if (CollectionUtils.isNotEmpty(living) && living.get(0) != null) {
				setLiving(living);
				lives.addAll(living);
				liveds = liveVideoInfoMapper.findHistoryVideoByPC(anchorId, 
						page.getStart(), page.getPageSize() - 1);
			} else {
				liveds = liveVideoInfoMapper.findHistoryVideoByPC(anchorId, 0, page.getPageSize());
			}
			if (CollectionUtils.isNotEmpty(liveds) && liveds.get(0) != null) {
				setLived(liveds);
				lives.addAll(liveds);
			}
		} else {
			lives = liveVideoInfoMapper.findHistoryVideoByPC(anchorId, page.getStart(), page.getPageSize());
            if (CollectionUtils.isNotEmpty(lives) && lives.get(0) != null) {
            	setLived(lives);
            }
		}
		
		page.setData(lives);
		return page;
	}
	
	private void setLiving(List<Map<String, Object>> living) {
        String videoId = living.get(0).get("videoId") + "";
    	String viewers = redisAdapter.hashGet(RedisKey.LIVE_VIDEO_INFO_ + videoId, "viewers");
    	if (viewers == null) {
    		viewers = "0";
    	}
    	 double income = 0D;
    	 if(living.get(0).get("income") != null){
    		 income = ((Double) living.get(0).get("income")).intValue() / 100.0;
         }
    	living.get(0).put("viewers", viewers);
        living.get(0).put("income", income);
	}
	
	private void setLived(List<Map<String, Object>> liveds) {
		for(Map<String, Object> video : liveds){
		    BigInteger viewers = (BigInteger) video.get("viewers");
		    String cntKey = RedisKey.HISTORY_VIEW_CNT_ + video.get("videoId");
		    String redisViewers = redisAdapter.strGet(cntKey);            
		    video.put("viewers", viewers.intValue() + (redisViewers == null?0:Integer.valueOf(redisViewers)));
		    if(video.get("income") == null){
                video.put("income", 0);                    
            } else{
                double income = ((Double) video.get("income")).intValue() / 100.0;
                video.put("income", income);
            }
		}
	} 

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int modifyVideoInfo(long videoId, String coverImg, String videoName, long anchorId, Integer videoType) {
		LiveVideoInfo video = new LiveVideoInfo();
		video.setVideoId(videoId);
		video.setAnchorId(anchorId);
		video.setVideoName(videoName);
		video.setCoverImg(coverImg);
		video.setVideoType(videoType);
		int result = liveVideoInfoMapper.updateByPKAndAnchorId(video);
		String videoKey = RedisKey.LIVE_VIDEO_INFO_ + videoId;
    	if(result > 0 && redisAdapter.existsKey(videoKey)) {
			Map<String, Object> map = video.putFieldValueToMapNotNull();
			map.put("anchorName",liveVideoInfoMapper.getAnchorNameById(video.getAnchorId()));
    		redisAdapter.hashMSet(videoKey, map);
    	}
		return result;
	}

	/**
	 * 主播删除视频
	 */
	@Override
	public int deleteVideo(long videoId, long anchorId) {
		//视频状态  0准备中 1直播中 2结束 3删除
		int result = liveVideoInfoMapper.updateVideoStatus(Constants._3, videoId, anchorId);
		String videoKey = RedisKey.LIVE_VIDEO_INFO_ + videoId;
		if(result > 0) {
    		String key = RedisKey.ANCHOR_VIDEO_ + anchorId;//主播最近视频key
			redisAdapter.delKeys(videoKey, key);
    	}
		
		String anchorInfoKey = RedisKey.ANCHOR_INFO_ + anchorId;
		System.out.println("-----------------------anchorInfoKey-----" + anchorInfoKey);
        if (redisAdapter.existsKey(anchorInfoKey)) {
        	String acctType = redisAdapter.hashGet(anchorInfoKey, "acctType");
        	if ("1".equals(acctType)) {
        		System.out.println("------------1-----------cacheAnchorLastVideo4Gray-----" + anchorInfoKey);
        		AnchorLastVideoForHistoryCacheHelper.cacheAnchorLastVideo4Gray(liveVideoInfoMapper, redisAdapter, videoId, anchorId);
        	} else if ("0".equals(acctType)) {
        		System.out.println("------------1-----------cacheAnchorLastVideo4Normal-----" + anchorInfoKey);
        		AnchorLastVideoForHistoryCacheHelper.cacheAnchorLastVideo4Normal(liveVideoInfoMapper, redisAdapter, videoId, anchorId);
        	}
        } else {
        	Anchor anchor = AnchorInfoHelper.getAndCacheAnchorInfo(redisAdapter, anchorMapper, anchorId);
        	if (anchor != null) {
        		if (Constants._1 == anchor.getAcctType().intValue()) {
            		System.out.println("------------2-----------cacheAnchorLastVideo4Gray-----" + anchorInfoKey);
            		AnchorLastVideoForHistoryCacheHelper.cacheAnchorLastVideo4Gray(liveVideoInfoMapper, redisAdapter, videoId, anchorId);
        		} else if (Constants._0 == anchor.getAcctType().intValue()) {
            		System.out.println("------------2-----------cacheAnchorLastVideo4Normal-----" + anchorInfoKey);
            		AnchorLastVideoForHistoryCacheHelper.cacheAnchorLastVideo4Normal(liveVideoInfoMapper, redisAdapter, videoId, anchorId);
        			
        		}
        	}
        }
		
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int stopVideo(long videoId, long anchorId, String streamName, int vdoid) {
		//视频状态  0准备中 1直播中 2结束 3删除 4有直播视频停止
		// 要不要用到乐观锁
		//获得加入黑名单地址
        int exist = liveVideoInfoMapper.existLiving(videoId);
		if (exist == 0) {
			String videoKey = RedisKey.VideoKeys.LIVING_VIDEOS_;
			redisAdapter.zsetRem(videoKey, anchorId + "");
			videoKey = RedisKey.VideoGrayKeys.LIVING_VIDEOS_;//灰度代码
			redisAdapter.zsetRem(videoKey, anchorId + "");//灰度代码
			return 1;
		}
        
		String url = KSCloudFacade.addBlack(KSCloudFacade.LIVE_NAME, streamName);

		SdkHttpResult result = null;
		try {
			HttpURLConnection conn = HttpUtils
					.createPostHttpConnection(url);
			result = HttpUtils.returnResult(conn);
		} catch (Exception e) {
			LOGGER.error("停止直播异常", e);
			return 0;
		}
		int stop = 0;
		if (result != null && result.getHttpCode() == 200) {
			//如果有录播设为4
			//从redis中移除该视频id
			String videoKey = RedisKey.VideoKeys.LIVING_VIDEOS_;
			long rem = redisAdapter.zsetRem(videoKey, anchorId + "");

			videoKey = RedisKey.VideoGrayKeys.LIVING_VIDEOS_;//灰度代码
			redisAdapter.zsetRem(videoKey, anchorId + "");
			
			if (LOGGER.isDebugEnabled()) {
				LOGGER.info("关闭视频直播, " + videoId + ", 删除" + rem);
			}
			stop = updateLiveVideo(videoId, anchorId, vdoid);
		}
		return stop;
	}

	/**
	 * 更新直播信息，同步redis缓存
	 * @param videoId
	 * @param anchorId
	 * @param vdoid
	 * @return
	 */
	private int updateLiveVideo(long videoId, long anchorId, int vdoid) {
		int stop = 0;
		LiveVideoInfo info = liveVideoInfoMapper.findVideoInfoById(videoId);
		if(info.getVideoStatus() == Constants._3) {
		    return 1;
		}
		userVideoMapper.updateVidoeIdByVideoId(info.getVideoId());
		//从redis中取出观看人数持久化到数据库, 并删除redis中的记录
//    	String livingViwerKey = RedisKey.LIVING_VIDEO_VIEWRES_ + info.getVideoId();
//        redisAdapter.delKeys(livingViwerKey);

        LiveVideoInfo tmp = new LiveVideoInfo();
        tmp.setAnchorId(anchorId);
        tmp.setVideoId(videoId);
        
        VideoSynchroClickCnt.setViewers(redisAdapter, tmp); //将缓存中直播的点击次数写入到数据表的viewers字段
        
        tmp.setEndTime(new Date());
        if (info != null && info.getVideoStatus() == Constants._0) {//没有直播过的改为删除
			tmp.setVideoStatus(Constants._3);
		} else if (vdoid == 0) {
			tmp.setVideoStatus(Constants._2);
		} else if (vdoid == 1) {//有录播视频
			tmp.setVideoStatus(Constants._4);
		}
        stop = liveVideoInfoMapper.updateByPKAndAnchorId(tmp);
        String videoKey = RedisKey.LIVE_VIDEO_INFO_ + videoId;
		if(stop > 0) {
    		String key = RedisKey.ANCHOR_VIDEO_ + anchorId;//主播最近视频key
			redisAdapter.delKeys(videoKey, key);
    	}
		
		String anchorInfoKey = RedisKey.ANCHOR_INFO_ + anchorId;
        if (redisAdapter.existsKey(anchorInfoKey)) {
        	String acctType = redisAdapter.hashGet(anchorInfoKey, "acctType");
        	if ("1".equals(acctType)) {
        		AnchorLastVideoForHistoryCacheHelper.cacheAnchorLastVideo4Gray(liveVideoInfoMapper, redisAdapter, videoId, anchorId);
        	} else if ("0".equals(acctType)) {
        		AnchorLastVideoForHistoryCacheHelper.cacheAnchorLastVideo4Normal(liveVideoInfoMapper, redisAdapter, videoId, anchorId);
        	}
        } else {
        	Anchor anchor = AnchorInfoHelper.getAndCacheAnchorInfo(redisAdapter, anchorMapper, anchorId);
        	if (anchor != null) {
        		if (Constants._1 == anchor.getAcctType().intValue()) {
            		AnchorLastVideoForHistoryCacheHelper.cacheAnchorLastVideo4Gray(liveVideoInfoMapper, redisAdapter, videoId, anchorId);
        		} else if (Constants._0 == anchor.getAcctType().intValue()) {
            		AnchorLastVideoForHistoryCacheHelper.cacheAnchorLastVideo4Normal(liveVideoInfoMapper, redisAdapter, videoId, anchorId);
        			
        		}
        	}
        }
        
		return stop;
	}
	

	/**
	 * 移除黑名单
	 * @param videoId
	 * @param anchorId
	 * @param streamName
	 * @return
	 */
	@Override
	public int startVideo(long videoId, long anchorId, String streamName) {
		//视频状态  0准备中 1直播中 2结束 3删除
		// 要不要用到乐观锁
		//获得加入黑名单地址
		String url = KSCloudFacade.delBlack(KSCloudFacade.LIVE_NAME, streamName);

		SdkHttpResult result = null;
		try {
			HttpURLConnection conn = HttpUtils
					.createPostHttpConnection(url);
			result = HttpUtils.returnResult(conn);
		} catch (Exception e) {
			LOGGER.error("开始直播异常", e);
			return 0;
		}
		int stop = 0;
		if (result != null && result.getHttpCode() == 200) {
			LiveVideoInfo info = liveVideoInfoMapper.findVideoInfoById(videoId);
			
			if (DateUtils.addDays(info.getCreateTime(), 1).compareTo(new Date()) <= 0) {//判断时间是否超过一天，超过一天就直播结束了
				stop = liveVideoInfoMapper.updateVideoStatus(Constants._2, videoId, anchorId);
			} else {
				stop = liveVideoInfoMapper.updateVideoStatus(Constants._1, videoId, anchorId);
			}
			//将该视频id添加到redis中
			String videoKey = RedisKey.VideoKeys.LIVING_VIDEOS_;
			redisAdapter.zsetAdd(videoKey, 0, anchorId + "");
		}
		
		return stop;
	}
	
	/**
	 * 直播信息
	 * @param videoId
	 * @param anchorId
	 * @return
	 */
	@Override
	public Map<String, Object> videoInfo(long videoId, long anchorId) {
		Map<String, Object> videoInfo = liveVideoInfoMapper.findVideoByPC(videoId, anchorId);
		
		if (!ObjectUtils.isEmpty(videoInfo)) {
			int videoStatus = (int) videoInfo.get("videoStatus");
			if (Constants._0 == videoStatus || Constants._1 == videoStatus) {//正在直播，返回三种类型清晰度的视频
				String streamName = (String) videoInfo.get("streamName");
//				String standURL = KSCloudFacade.getHlsURL(streamName);
//				videoInfo.put("standURL", standURL);
				String rtmps = KSCloudFacade.getHdlURL(streamName);
				videoInfo.put("standURL", rtmps);
//				videoInfo.put("heighURL", rtmps[1]);
//				videoInfo.put("fullHeighURL", rtmps[2]);
				videoInfo.put("secret", RongCloudFacade.getAppKey());
			} else{
			    String cntKey = RedisKey.HISTORY_VIEW_CNT_ + videoInfo.get("videoId");
			    String redisViewers = redisAdapter.strGet(cntKey);
			    Long viewers = ((BigInteger)videoInfo.get("viewers")).longValue();
			    videoInfo.put("viewers", viewers + (redisViewers == null?0:Long.valueOf(redisViewers)));
			}
		}
		
		if(videoInfo != null){
		    double income = ((Double)videoInfo.get("income")).intValue() / 100.0;
		    videoInfo.put("income", income);
		}
		
		// 添加关卡相关数据
		videoInfo.put("diamonds", levelEventService.getCurDiamonds(anchorId));
		videoInfo.put("levels", levelEventService.getCurLevels(anchorId));
		
		return videoInfo;
	}
	
	public int getLivingCnt(long anchorId) {
		Integer cnt = liveVideoInfoMapper.countLivings(anchorId);
		if (cnt == null ||cnt.intValue() == 0) {
			return 0;
		}
		return cnt;
	}

    @Override
    public Map<String, Object> getViewers(long videoId, int videoStatus) {  
        Map<String, Object> resultJson = new HashMap<>();
        if(videoStatus != 4){
            //非历史直播
            String videoKey = RedisKey.LIVE_VIDEO_INFO_ + videoId;
            String str = redisAdapter.hashGet(videoKey, "viewers");
            if (StringUtils.isBlank(str)) {
            	str = "0";
            }
            //从redis中获取观看人数
            Long viewers = Long.valueOf(str);
            resultJson.put("realtimeViewers", viewers);
        } else{
            //历史直播
            LiveVideoInfo videoInfo = liveVideoInfoMapper.findVideoInfoById(videoId);
            String cntKey = RedisKey.HISTORY_VIEW_CNT_ + videoInfo.getVideoId();
            String redisViewers = redisAdapter.strGet(cntKey);
            resultJson.put("realtimeViewers", videoInfo.getViewers() + (redisViewers == null?0:Long.valueOf(redisViewers)));
        }
        return resultJson;
    }

    @Override
    public Map<String, Object> getIncome(long videoId) {
        double income = liveVideoInfoMapper.asynGetVideoIncome(videoId);
        Map<String, Object> resultJson = new HashMap<>();
        resultJson.put("realtimeIncome", income);
        return resultJson;
    }
}
