package com.chineseall.iwanvi.wwlive.web.video.service.impl;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.chineseall.iwanvi.wwlive.web.video.service.PublicNoticeService;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.collections.map.HashedMap;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.log4j.Logger;
import org.apache.velocity.tools.view.servlet.ServletLogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import redis.clients.jedis.Tuple;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.chineseall.iwanvi.wwlive.common.cache.RedisClientAdapter;
import com.chineseall.iwanvi.wwlive.common.cache.RedisExpireTime;
import com.chineseall.iwanvi.wwlive.common.cache.RedisKey;
import com.chineseall.iwanvi.wwlive.common.constants.Constants;
import com.chineseall.iwanvi.wwlive.common.external.kscloud.KSCloudFacade;
import com.chineseall.iwanvi.wwlive.dao.wwlive.LiveVideoInfoMapper;
import com.chineseall.iwanvi.wwlive.domain.wwlive.LiveVideoInfo;
import com.chineseall.iwanvi.wwlive.web.common.helper.AnchorLastVideoForHistoryCacheHelper;
import com.chineseall.iwanvi.wwlive.web.common.helper.LiveVideoInfoHelper;
import com.chineseall.iwanvi.wwlive.web.common.helper.LivedVideoForHistoryCacheHelper;
import com.chineseall.iwanvi.wwlive.web.common.helper.RocketInfoHelper;
import com.chineseall.iwanvi.wwlive.web.common.util.Page;
import com.chineseall.iwanvi.wwlive.web.event.service.MedalHonorService;
import com.chineseall.iwanvi.wwlive.web.video.service.HomeIndexService;

@Service
public class HomeIndexServiceImpl implements HomeIndexService {

	private static final Logger LOGGER = Logger.getLogger(HomeIndexServiceImpl.class);
	
    @Autowired
    private RedisClientAdapter redisAdapter;
    
    @Autowired
    private LiveVideoInfoMapper liveVideoInfoMapper;
    
    @Autowired
    private MedalHonorService medalHonorService;

	@Autowired
	private PublicNoticeService publicNoticeService;

    /**
     * 直播视频列表，灰度代码
     */
    @Override
    public Map<String, Object>
    getGrayLivingVideos(Page page) {
        String snapShotKey = RedisKey.VideoGrayKeys.LIVING_VIDEOS_SNAPSHOT;
        Map<String, Object> resultJson = getLivings(page, snapShotKey);
        return resultJson;
    }
    /**
     * 直播视频列表
     * @param page
     * @return
     */
    @Override
    public Map<String, Object> getLivingVideos(Page page) {
        String snapShotKey = RedisKey.VideoKeys.LIVING_VIDEOS_SNAPSHOT;
        Map<String, Object> resultJson = getLivings(page, snapShotKey);
        return resultJson;
    }
    
    private Map<String, Object> getLivings(Page page, String snapShotKey) {
    	int pageSize = page.getPageSize(); 
    	int pageNo = page.getPageNo();
    	Set<Tuple> ids = null;
        String videoKey = "";
        Map<String, Object> resultJson = new HashMap<>();
    	if (pageNo > 1) {
        	JSONObject json = page.getExtra();
        	if (json != null) {
        		Object obj = json.get("score");
            	Double score = null;
        		if (obj instanceof String) {
        			score = Double.parseDouble((String) obj);
        			score -= 1;
        		} else if (obj instanceof Double) {
        			score = (Double) obj;
        			score -= 1;
        		}
                videoKey = (String) json.get("snapShotKey");
            	ids = redisAdapter.zsetRevrangeByScoreWithScores(videoKey, (score.toString()), "0", 0, pageSize);
        	}
        } else if (pageNo == 1) {
        	if (redisAdapter.existsKey(snapShotKey)) {
            	videoKey = redisAdapter.strGet(snapShotKey);
            } else {
                resultJson.put("page", page);//视频个数为0
                resultJson.put("videoList", new String[]{});
                getTodayNotice(resultJson);
            }
        	videoKey = videoKey.replace("\"", "");
        	ids = redisAdapter.zsetRevrangeByScoreWithScores(videoKey, Double.MAX_VALUE, 0, 0, -1);

            if (ids == null || ids.isEmpty()) {//防止 living_videos_ 无法取到值，故在此列表中放入假数据0
                resultJson.put("page", page);//视频个数为0
                resultJson.put("videoList", new String[]{});
                getTodayNotice(resultJson);
            	return resultJson;
            }
        }
    	List<Map<String, Object>> videoList = null;
        if (ids != null && !ids.isEmpty()) {//获得视频详情
			videoList = getLivingVideoListAndCacheByRedis(ids, page, videoKey);
    		page.getExtra().put("snapShotKey", videoKey);
        	redisAdapter.expireKey(snapShotKey, RedisExpireTime.EXPIRE_HOUR_1);
    	} else {
    		videoList = new ArrayList<Map<String, Object>>();
    	}
        
//        List<Map<String, Object>> lives = new ArrayList<>();
        /*for(Map<String, String> live : videoList){
            Map<String, Object> tempMap = new HashMap<>();
            tempMap.putAll(live);
            
            long anchorId = MapUtils.getLongValue(live, "anchorId", 0);
            List<String> medals = medalHonorService.getIndexAnchorMedal(anchorId);
            tempMap.put("medals", medals);
            lives.add(tempMap);
        }*/
        JSONObject extra = page.getExtra();
        resultJson.put("snapShotKey", videoKey);
        resultJson.put("score", extra.get("score")+"");
        resultJson.put("page", page);
        resultJson.put("videoList", videoList);
		resultJson.put("trailerList",new String[]{});
        return resultJson;
    }

    public void  getTodayNotice (Map<String, Object> resultJson) {
        String notice = publicNoticeService.getTodayNotice();
        List<Map<String,Object>> list = new ArrayList<>();
        if(StringUtils.isNotEmpty(notice)) {
            String[] split = notice.split("\\|");
            for (String s:split) {
                Pattern p = Pattern.compile("\\s*|\t|\r|\n");
                Matcher m = p.matcher(s);
                String replace = m.replaceAll("");
                Map<String,Object> map = new HashedMap();
                map.put("content",replace);
                list.add(map);
            }
            resultJson.put("trailerList",list);
        }else {
            resultJson.put("trailerList",new String[]{});
        }
    }

   
    /**
     * 从缓存中获得，如果不存在就从数据库中获得
     * @param ids
     * @param page
     * @param videoKey 用来清除数据库或缓存不存在的videoId值
     * @return
     */
    private List<Map<String, Object>> getLivingVideoListAndCacheByRedis(Set<Tuple> ids, Page page, String videoKey) {
    	List<Map<String, Object>> videoList = new ArrayList<Map<String, Object>>();
    	Map<String, Object> element;
    	JSONObject json = new JSONObject();
    	for (Tuple id : ids) {
    		element = new HashMap<>();
			// 排除定制版的主播直播，以后更改为创建直播时控制
			String anchorId = id.getElement();
			if(redisAdapter.setIsMember(RedisKey.DINGZHI_ANCHOR,anchorId)){
				continue;
			}
			String livingKey = RedisKey.ANCHOR_LIVING_VIDEO_ + anchorId;
    		String key = RedisKey.LIVE_VIDEO_INFO_;
    		Long videoId = new Long(0);
    		if (redisAdapter.existsKey(livingKey)) {
    			videoId = Long.valueOf(redisAdapter.strGet(livingKey));
    			key += videoId.toString();
    		} else {
    			LiveVideoInfo info = findBannerVideoInfoByAnchorId(Long.parseLong(anchorId));
    			if (info == null) {
        			redisAdapter.zsetRem(videoKey, anchorId);
    			} else {
    				redisAdapter.strSetexByNormal(RedisKey.ANCHOR_LIVING_VIDEO_ + info.getAnchorId(), 
    						RedisExpireTime.EXPIRE_DAY_5, info.getVideoId().toString());
    				videoId = info.getVideoId();
        			key += videoId.toString();
    			}
    		}
    		
    		Map<String, String> videoMap = null;
    		if (redisAdapter.existsKey(key)) {
    			videoMap = redisAdapter.hashMGet(key, "viewers", "chatroomId",
    					"videoId", "videoName", "coverImg", "anchorId", "formatType","anchorName");
    		} else {
    			videoMap = LiveVideoInfoHelper.getAndCacheVideoInfoStringMap(redisAdapter, liveVideoInfoMapper, videoId);
    		}
    		
    		if (videoMap != null && !videoMap.isEmpty()) {
                anchorId = videoMap.get("anchorId");
                Map<String, String> userName = redisAdapter.hashMGet(RedisKey.ANCHOR_INFO_ + anchorId, "userName");
                videoMap.put("anchorName",userName.get("userName"));
                if (!redisAdapter.existsKey(RedisKey.LIVING_CLICKCNT_ + videoId)) {//直播断流后重新设置人数为正常人数，上机器人后此处删除
    				videoMap.put("viewers", redisAdapter.setCard(RedisKey.LIVING_VIDEO_VIEWRES_ + videoMap.get("anchorId")) + "");
    				redisAdapter.hashMSet(key, videoMap);
    				redisAdapter.strSetexByNormal(RedisKey.LIVING_CLICKCNT_ + videoId, RedisExpireTime.EXPIRE_DAY_5, "0");
    			}
    			//将信息封装，加快直播的展示 from tang
    			String[] liuArgs = KSCloudFacade.getRtmpURLs(videoMap.get("chatroomId"));
    			Map<String, String> map = new HashMap<String, String>();
    			map.put("standURL", liuArgs[0]);
    			map.put("heighURL", liuArgs[1]);
    			map.put("fullHeighURL", liuArgs[2]);
				try {
					videoMap.put("ext", URLEncoder.encode(JSON.toJSONString(map),"utf-8"));
				} catch (UnsupportedEncodingException e) {
					LOGGER.error("转换地址时异常：", e);
				}

				element.putAll(videoMap);
	        	//火箭推送 since 2017-08-30
				Map<String, Object> temp = RocketInfoHelper.homeIndexNobleRecommend(anchorId, redisAdapter);
				element.putAll(temp);
	        	
	        	 // 直播首页勋章信息只展示一周
	        	Long aid = Long.valueOf(anchorId);
	            List<String> medals = medalHonorService.getIndexAnchorMedal(aid);
	            element.put("medals", medals);
	            
                videoList.add(element);
            	json.put("score", id.getScore());
    		} else {
    			redisAdapter.zsetRem(videoKey, anchorId);
    		}

		}
    	page.setExtra(json);
    	return videoList;
    	
    }
    
    private LiveVideoInfo findBannerVideoInfoByAnchorId(Long anchorId) {
    	LiveVideoInfo info = liveVideoInfoMapper.findBannerVideoInfoByAnchorId(anchorId);
    	return info;		
    }
    
	/**
     * 回放
     * @param page
     * @return
     */
    public Map<String, Object> getHistotyVideoListFromCache(Page page) {
		String tmpKey = RedisKey.VideoKeys.LIVED_VIDEOS_;
		return getLiveds(page, tmpKey, false);
    }
    
    /**
     * 回放，灰度代码
     */
	@Override
	public Map<String, Object> getGrayHistotyVideoListFromCache(Page page) {
		String tmpKey = RedisKey.VideoGrayKeys.LIVED_VIDEOS_;
		return getLiveds(page, tmpKey, true);
	}

	@Override
	public List<Map<String, Object>> getBabeAnchorInfo() {
		String anchorsKey = RedisKey.READING_ANCHOR;
		List<String> anchorIds = redisAdapter.listRange(anchorsKey, 0, -1);
		String liveAnchors = RedisKey.VideoKeys.LIVING_VIDEOS_;
		Set<String> liveAnchorIds = redisAdapter.zsetRange(liveAnchors, 0, -1);
		List<Map<String, Object>> anchorList = new ArrayList<>();
		for (String anchorIdStr : anchorIds){
			Long anchorId = Long.valueOf(anchorIdStr);
			Map<String, Object> anchorInfo = new HashMap<>();
			anchorInfo.put("anchorId", anchorId);
			anchorInfo.put("isLiving", 0);
			anchorList.add(anchorInfo);
			if (liveAnchorIds.contains(anchorIdStr)) {
				Map<String, Object> videoInfo = liveVideoInfoMapper.getLivingByAnchorId(anchorId);
				if (videoInfo == null || videoInfo.isEmpty()){
					continue;
				}
				anchorInfo.put("isLiving", 1);
				Map<String, Object> videoMap = new HashMap<>();
				videoMap.put("videoId", videoInfo.get("videoId"));
				videoMap.put("chatroomId", videoInfo.get("chatroomId"));
				videoMap.put("formatType", videoInfo.get("formatType"));
				videoMap.put("coverImg", videoInfo.get("coverImg"));
				anchorInfo.put("videoInfo", videoMap);
			}
		}

		return anchorList;
	}

	private Map<String, Object> getLiveds(Page page, String tmpKey, boolean isGray) {
		int pageSize = page.getPageSize();
    	int pageNo = page.getPageNo();
    	
        //视频状态  0准备中 1直播中 2结束无点播 3删除 4结束有点播
        //从数据库中查出直播结束的视频(videoStatus = 4)
        Map<String, Object> resultJson = new HashMap<>();
		List<Map<String, String>> videoList = null;
        Set<Tuple> ids = null;
        if (pageNo > 1) {
        	JSONObject json = page.getExtra();
        	if (json != null) {
        		Object obj = json.get("score");
//            	Long score = null;
                Double score = null;
                if (obj instanceof String) {
                    score = Double.parseDouble((String) obj);
                    score -= 1;
                } else if (obj instanceof Double) {
                    score = (Double) obj;
                    score -= 1;
                }
                tmpKey = (String) json.get("videoKey");
                ids = redisAdapter.zsetRevrangeByScoreWithScores(tmpKey, (score.toString()), "0", 0, pageSize);
            }
        } else if (pageNo == 1) {
        	Date now = new Date();
        	String today = DateFormatUtils.format(now, Constants.YY_MM_DD);
        	tmpKey += today;
        	if (!redisAdapter.existsKey(tmpKey)) {//如果不存在，就用生成
        		if (isGray) {
            		LivedVideoForHistoryCacheHelper.livedVideosForGrayCache(redisAdapter, liveVideoInfoMapper, tmpKey, now);
        		} else {
            		LivedVideoForHistoryCacheHelper.livedVideosForCache(redisAdapter, liveVideoInfoMapper, tmpKey, now);
        		}
        	}
        	if (redisAdapter.zsetCard(tmpKey) <= 0){
            	page.setTotal(0L);
                resultJson.put("page", page);
                resultJson.put("videoList", videoList);
                return resultJson;
            }
        	ids = redisAdapter.zsetRevrangeByScoreWithScores(tmpKey, Double.MAX_VALUE, 0, 0, pageSize);
        }
        if (ids != null && !ids.isEmpty()) {//获得视频详情
			videoList = getVideoListAndCacheByRedis(ids, page, tmpKey);
    		page.getExtra().put("videoKey", tmpKey);
            JSONObject extra = page.getExtra();
            resultJson.put("videoKey", tmpKey);
            resultJson.put("score", extra.get("score")+"");
        	redisAdapter.expireKey(tmpKey, RedisExpireTime.EXPIRE_DAY_1);
    	} else {
            resultJson.put("videoKey", tmpKey);
            JSONObject extra = page.getExtra();
            if (extra == null) {
            	resultJson.put("score", "0");
            } else {
                resultJson.put("score", extra.get("score")+"");
            }
    	}
        resultJson.put("page", page);
        resultJson.put("videoList", videoList);
        return resultJson;
	}
	
    /**
     * 从缓存中获得，如果不存在就从数据库中获得
     * @param ids member主播id，score时间戳
     * @param page
     * @param videoKey 用来清除数据库或缓存不存在的videoId值
     * @return
     */
    private List<Map<String, String>> getVideoListAndCacheByRedis(Set<Tuple> ids, Page page, String videoKey) {
    	List<Map<String, String>> videoList = new ArrayList<Map<String,String>>();
    	JSONObject json = new JSONObject();
    	for (Tuple id : ids) {
			String anchorId = id.getElement();
			if(redisAdapter.setIsMember(RedisKey.DINGZHI_ANCHOR,anchorId)){
				continue;
			}
			String cache = redisAdapter.strGet(RedisKey.ANCHOR_LIVED_VIDEO_ + anchorId);
    		if (StringUtils.isNotBlank(cache) && "0".equals(cache)) {
    			redisAdapter.zsetRem(videoKey, anchorId);
    			continue;
    		}

    		Map<String, String> videoMap = null;
			String key = "";
    		if (StringUtils.isBlank(cache)) {
    			videoMap = AnchorLastVideoForHistoryCacheHelper.cacheAnchorLastVideo(liveVideoInfoMapper, redisAdapter, Long.parseLong(anchorId));
    		} else {
    			key = RedisKey.LIVE_VIDEO_INFO_ + cache;
        		if (redisAdapter.existsKey(key)) {
        			videoMap = redisAdapter.hashMGet(key, "viewers", 
        					"videoId", "videoName", "coverImg", "anchorId", "formatType","anchorName");
        		} else {
        			videoMap = LiveVideoInfoHelper.getAndCacheVideoInfoStringMap(redisAdapter, liveVideoInfoMapper, Long.parseLong(cache));
        		}
    		}
    		
    		if (videoMap != null && !videoMap.isEmpty()) {
                anchorId = videoMap.get("anchorId");
                Map<String, String> userName = redisAdapter.hashMGet(RedisKey.ANCHOR_INFO_ + anchorId, "userName");
                videoMap.put("anchorName",userName.get("userName"));
        		setVideoInfoViewers(key, videoMap);
                videoList.add(videoMap);
            	json.put("score", id.getScore());
    		} else {
    			redisAdapter.zsetRem(videoKey, anchorId);
    		}
    		//AnchorLastVideoForHistoryCacheHelper.cacheAnchorLastVideo(liveVideoInfoMapper, redisAdapter, videoId, id.getElement());
		}
    	page.setExtra(json);
    	return videoList;
    	
    }
    
    private void setVideoInfoViewers(String videoKey, Map<String, String> videoMap) {
        String cntKey = RedisKey.HISTORY_VIEW_CNT_ + videoMap.get("videoId");
        String redisViewers = redisAdapter.strGet(cntKey);
        if (StringUtils.isBlank(videoMap.get("viewers"))) {
        	videoMap.put("viewers", "0");
        }
        String viewers = new BigDecimal(videoMap.get("viewers")).add(new BigDecimal(redisViewers == null 
        		? 0 :Integer.valueOf(redisViewers))).toString();
        videoMap.put("viewers", viewers);
    }
    
}
