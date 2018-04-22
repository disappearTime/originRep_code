package com.chineseall.iwanvi.wwlive.web.video.service.impl;

import java.math.BigInteger;
import java.text.ParseException;
import java.util.*;

import com.alibaba.fastjson.JSONObject;
import com.chineseall.iwanvi.wwlive.dao.wwlive.*;
import com.chineseall.iwanvi.wwlive.web.common.helper.LiveAdminHelper;
import com.chineseall.iwanvi.wwlive.web.common.helper.RoleNobleHelper;
import com.zw.zcf.util.JsonUtils;
import org.apache.commons.collections.map.HashedMap;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.chineseall.iwanvi.wwlive.common.cache.RedisClientAdapter;
import com.chineseall.iwanvi.wwlive.common.cache.RedisExpireTime;
import com.chineseall.iwanvi.wwlive.common.cache.RedisKey;
import com.chineseall.iwanvi.wwlive.domain.wwlive.Anchor;
import com.chineseall.iwanvi.wwlive.domain.wwlive.LiveVideoInfo;
import com.chineseall.iwanvi.wwlive.web.video.service.AnchorService;
import com.service.FollowAnchorService;
import com.service.impl.FollowAnchorServiceImpl;
import com.chineseall.iwanvi.wwlive.web.common.helper.AnchorInfoHelper;
import com.chineseall.iwanvi.wwlive.web.common.helper.LiveVideoInfoHelper;
import com.chineseall.iwanvi.wwlive.web.common.util.DateTools;
import com.chineseall.iwanvi.wwlive.web.event.service.MedalHonorService;

@Service
public class AnchorServiceImpl implements AnchorService{
    
    private Logger logger = Logger.getLogger(this.getClass());
    
    @Autowired
    private AnchorMapper anchorMapper;

    @Autowired
    private LiveAdminMapper adminMapper;

    @Autowired
    private RoleInfoMapper roleInfoMapper;
    
    @Autowired
    private LiveVideoInfoMapper videoMapper;
    
    @Autowired
    private RedisClientAdapter redisAdapter;

	@Autowired
	private LiveVideoInfoMapper liveVideoInfoMapper;

    @Autowired
    private ContributionListMapper contributionListMapper;
	
	private static FollowAnchorService followService = new FollowAnchorServiceImpl();
	
	@Autowired
	private MedalHonorService medalHonorService;
	
    @Override
    public Map<String, Object> getAnchorInfo(Long anchorId, Long userId) {
    	
        Map<String, Object> data = getBasicInfo(anchorId);
        
        //查询该主播点击数最高的10个历史直播视频, 视频状态=4
        List<Map<String, Object>> videoList = videoMapper.findByAnchorId(anchorId);
        List<Map<String, Object>> dataList = new ArrayList<>();
        for(Map<String, Object> video : videoList){
            Map<String, Object> map = new HashMap<>();
            map.put("anchorId", anchorId);
            map.put("videoId", video.get("videoId"));
            
            BigInteger viewers = (BigInteger) video.get("viewers");
            String cntKey = RedisKey.HISTORY_VIEW_CNT_ + map.get("videoId");
            String redisViewers = redisAdapter.strGet(cntKey);            
            map.put("viewers", viewers.intValue() + (redisViewers == null?0:Integer.valueOf(redisViewers)));
            
            map.put("coverImg", video.get("coverImg"));
            map.put("videoName", video.get("videoName"));
            map.put("vdoid", video.get("vdoid"));
            map.put("formatType", video.get("formatType"));
            dataList.add(map);
        }
        
        // 视频个数
        data.put("videoCnt", videoList.size());        
        data.put("videoList", dataList);
        
        // 用户是否关注
        boolean isFollower = false;
        int followerCnt = 1;
        try {
            isFollower = followService.isFollow(userId, anchorId);
            followerCnt = followService.getFansNumber(anchorId);
        } catch (Exception e) {
            e.printStackTrace();
        }        
        data.put("isFollower", isFollower ? 1 : 0);// 0=未关注, 1=已关注
        data.put("followerCnt", followerCnt + 1);
        
        // 关卡活动需求: 获得主播的勋章
        data.put("medals", medalHonorService.getGoddessMedal(anchorId));
        
        Map<String, Object> resultJson = new HashMap<>();
        resultJson.put("anchorInfo", data);  
        return resultJson;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, Object> getBasicInfo(Long anchorId) {
        String anchorKey = RedisKey.ANCHOR_INFO_ + anchorId;

        Map<String, Object> data = new HashMap<>();
        if (redisAdapter.existsKey(anchorKey)) {
        	//先从redis中获取主播信息
            Map<String, String> anchorInfo = redisAdapter.hashMGet(anchorKey, "anchorId", "headImg", "userName",
            		"sex", "birthday", "zodiac", "notice");//头像 昵称 性别 年龄 星座 公告
            try {
                anchorInfo.put("age", DateTools.getAgeByDate(anchorInfo.get("birthday")) + ""); //年龄实时计算
            } catch (ParseException e) {
                logger.error("App端获取主播资料接口中, anchorId为" + anchorId + "的主播年龄计算时出现格式转换异常.");
                //异常时年龄为0
                anchorInfo.put("age", "0");
            }
            data.putAll(anchorInfo);
        } else {
        	Anchor anchor = AnchorInfoHelper.getAndCacheAnchorInfo(redisAdapter, anchorMapper, anchorId);
        	//id
        	data.put("anchorId", anchor.getAnchorId());
        	//头像
        	data.put("headImg", anchor.getHeadImg());
            //昵称
            data.put("userName", anchor.getUserName());
            //性别
            data.put("sex", anchor.getSex());
            //年龄
            data.put("age", DateTools.getAgeByDate(anchor.getBirthday()));
            //星座
            data.put("zodiac", anchor.getZodiac());
            //公告
            data.put("notice", anchor.getNotice());
        }
        
        // 关卡活动需求: 获取主播的勋章信息
        data.put("medals", medalHonorService.getGoddessMedal(anchorId));

        String liveKey = RedisKey.VideoKeys.LIVING_VIDEOS_;
        /*String channel = redisAdapter.strGet("qudao");// 灰度渠道控制
        if(StringUtils.isNotBlank(channel) && channel.equals(cnid)){
            liveKey = RedisKey.VideoGrayKeys.LIVING_VIDEOS_;
        }*/
        Set<String> livingAnchorIds = redisAdapter.zsetRevRange(liveKey, 0, -1);
        int videoCnt = videoMapper.countVideos(anchorId);
        // 补充回放视频个数字段, 2017-7-17 15:22:17
        data.put("livedCnt", videoCnt);
        
    	return data;
    }

	@Override
	public Map<String, Object> getAnchorVideoList(Long anchorId,
			Integer startRow, Integer pageSize) {
		
		if (startRow <= 0) {
			return new HashMap<String, Object>();
		}
        Map<String, Object> data = new HashMap<String, Object>();
        
        //查询该主播历史直播视频, 视频状态=4
        List<Map<String, Object>> videoList = videoMapper.findLivedListByAnchorId(anchorId, (startRow - 1) * pageSize, pageSize) ;
        //视频个数
        data.put("videoCnt", videoList.size());        
        data.put("videoList", videoList);
        
        return data;
	}

	@Override
	public Map<String, Object> getAnchorVideo(Long anchorId, String loginId) {
		String key = RedisKey.ANCHOR_VIDEO_ + anchorId;
		Map<String, Object> videoInfo = new HashMap<String, Object>();
		if (redisAdapter.existsKey(key)) {
			String vidoIdStr = redisAdapter.strGet(key);
			if (StringUtils.isNotBlank(vidoIdStr) && vidoIdStr.contains("\"")) {
				vidoIdStr = vidoIdStr.replace("\"", "");
				redisAdapter.strSetByNormal(key, vidoIdStr);
			}
			String tmpKey = RedisKey.LIVE_VIDEO_INFO_ + vidoIdStr;
			if (redisAdapter.existsKey(tmpKey)) {
				videoInfo.putAll(redisAdapter.hashMGet(tmpKey, "videoId", "videoStatus", 
						"chatroomId", "formatType", "coverImg", "anchorId"));
			} else {
				Long videoId = Long.parseLong(vidoIdStr);
				if(videoId.longValue() != 0) {
					videoInfo.putAll(LiveVideoInfoHelper.getAndCacheVideoInfoStringMap(redisAdapter, liveVideoInfoMapper, videoId));
				}
			}
		} else {
			Long videoId = new Long(0);
			LiveVideoInfo video = liveVideoInfoMapper
					.findBannerVideoInfoByAnchorId(anchorId);
			if (video != null) {
				Map<String, Object> map_ = video.putFieldValueToMap();
                map_.put("anchorName",liveVideoInfoMapper.getAnchorNameById(anchorId));
				videoInfo.putAll(video.putFieldValueToStringMap());
				redisAdapter.hashMSet(RedisKey.LIVE_VIDEO_INFO_  + video.getVideoId(), map_);
				redisAdapter.expireKey(RedisKey.LIVE_VIDEO_INFO_  + video.getVideoId(), RedisExpireTime.EXPIRE_HOUR_5);
				videoId = video.getVideoId();
			}
			redisAdapter.strSetByNormal(key, videoId.toString());
			redisAdapter.expireKey(key, RedisExpireTime.EXPIRE_DAY_5);
		}
		if (videoInfo != null && !videoInfo.isEmpty()) {
			String videoStatus = (String) videoInfo.get("videoStatus");
			videoInfo.put("accessType", videoStatus);
			if ("4".equals(videoStatus)) {
				videoInfo.put("accessType", "5");
			}
		} else {
			videoInfo.put("accessType", "5");
		}
		return videoInfo;
	}

    public Map<String,Object> getContribList(long anchorId){
        Map<String,Object> result = new HashedMap();
        List<Map<String, Object>> dataList = getRankList(anchorId);
//        List<Map<String, Object>> dataList = contributionListMapper.getAnchorContribList(0,30, anchorId);
        //判断房管
        for(Map<String, Object> data:dataList){
            long userId = Long.parseLong(data.get("userId").toString());
            boolean isAdmin =
                    LiveAdminHelper.isAdmin(redisAdapter, adminMapper, anchorId, userId);
            data.put("isAdmin", isAdmin ? 1 : 0);

            //增加贵族等级及贵族图片
            Integer level = RoleNobleHelper.userRoleNobleLevel(redisAdapter, roleInfoMapper, userId);
            data.put("level", level);
        }
        result.put("rankList",dataList);
        return result;
    }

    public List<Map<String, Object>> getRankList (long anchorId) {
        List<Map<String,Object>> dataList = null;
        String key = "anchor_rank_" + anchorId;
        String s = redisAdapter.strGet(key);
        if(StringUtils.isNotEmpty(s)) {
            dataList = (List<Map<String, Object>>) JSONObject.parse(s);
        }else {
            dataList = contributionListMapper.getAnchorContribList(0,30, anchorId);
            String json = JsonUtils.toJSON(dataList);
            redisAdapter.strSetexByNormal(key,RedisExpireTime.EXPIRE_MIN_1,json);
        }
        return dataList;
    }
}
