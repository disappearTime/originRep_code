package com.chineseall.iwanvi.wwlive.web.video.service.impl;

import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.chineseall.iwanvi.wwlive.common.cache.RedisClientAdapter;
import com.chineseall.iwanvi.wwlive.common.cache.RedisExpireTime;
import com.chineseall.iwanvi.wwlive.common.cache.RedisKey;
import com.chineseall.iwanvi.wwlive.common.constants.Constants;
import com.chineseall.iwanvi.wwlive.common.exception.IWanviDataBaseException;
import com.chineseall.iwanvi.wwlive.common.external.kscloud.KSCloudFacade;
import com.chineseall.iwanvi.wwlive.common.tools.HttpUtils;
import com.chineseall.iwanvi.wwlive.dao.wwlive.AnchorMapper;
import com.chineseall.iwanvi.wwlive.dao.wwlive.BlackListMapper;
import com.chineseall.iwanvi.wwlive.dao.wwlive.LiveVideoInfoMapper;
import com.chineseall.iwanvi.wwlive.dao.wwlive.RoleInfoMapper;
import com.chineseall.iwanvi.wwlive.dao.wwlive.UserInfoMapper;
import com.chineseall.iwanvi.wwlive.dao.wwlive.UserVideoMapper;
import com.chineseall.iwanvi.wwlive.domain.wwlive.Anchor;
import com.chineseall.iwanvi.wwlive.domain.wwlive.LiveVideoInfo;
import com.chineseall.iwanvi.wwlive.domain.wwlive.UserInfo;
import com.chineseall.iwanvi.wwlive.web.common.constants.WebConstants;
import com.chineseall.iwanvi.wwlive.web.common.helper.AnchorInfoHelper;
import com.chineseall.iwanvi.wwlive.web.common.helper.BlackListHelper;
import com.chineseall.iwanvi.wwlive.web.common.helper.LiveVideoInfoHelper;
import com.chineseall.iwanvi.wwlive.web.common.helper.RoleNobleHelper;
import com.chineseall.iwanvi.wwlive.web.common.helper.UserInfoHelper;
import com.chineseall.iwanvi.wwlive.web.common.util.DateTools;
import com.chineseall.iwanvi.wwlive.web.event.service.MedalHonorService;
import com.chineseall.iwanvi.wwlive.web.launch.service.impl.VideoServiceImpl;
import com.chineseall.iwanvi.wwlive.web.video.service.AnchorService;
import com.chineseall.iwanvi.wwlive.web.video.service.LiveVideoInfo2Service;
import com.chineseall.iwanvi.wwlive.web.video.service.LiveVideoInfoService;
import com.chineseall.iwanvi.wwlive.web.video.vo.LiveVideoInfoVo;

@Service
public class LiveVideoInfoServiceImpl implements LiveVideoInfoService {

    private static final Logger LOGGER = Logger.getLogger(VideoServiceImpl.class);

    @Autowired
    private LiveVideoInfoMapper liveVideoInfoMapper;

    @Autowired
    private UserInfoMapper userInfoMapper;

    @Autowired
    private RedisClientAdapter redisAdapter;

    @Autowired
    private RoleInfoMapper roleInfoMapper;

    @Autowired
    private AnchorService anchorService;

    @Autowired
    private UserVideoMapper userVideoMapper;

    @Autowired
    private AnchorMapper anchorMapper;

    @Autowired
    private BlackListMapper blackListMapper;
    
    @Autowired
    private MedalHonorService medalHonorService;

    @Autowired
    private LiveVideoInfo2Service liveVideoInfo2Service;

    @Override
    public Map<String, Object> getLivingVideos() {
        Map<String, Object> resultJson = new HashMap<>();
        // 从redis中获取正在直播的视频id集合
        String videoKey = RedisKey.VideoKeys.LIVING_VIDEOS_;// + dateInfo

        Set<String> ids = redisAdapter.zsetRevRange(videoKey, 0, -1);

        if (ids == null || ids.size() == 0) {
            resultJson.put("videoCnt", 0);// 视频个数为0
            resultJson.put("videoList", new ArrayList<Map<String, String>>());
            return resultJson;
        }

        List<Long> idsList = new ArrayList<>();
        // 将String类型的id转换为Integer类型的
        for (String id : ids) {
            idsList.add(Long.valueOf(id));
        }

        if (idsList.size() == 1 && idsList.get(0) == 0) {// 防止 living_videos_
                                                         // 无法取到值，故在此列表中放入假数据0
            resultJson.put("videoCnt", 0);// 视频个数为0
            resultJson.put("videoList", new ArrayList<Map<String, String>>());
            return resultJson;
        }

        // 根据视频id获得视频信息
        List<Map<String, String>> livingVideos = getLivingVideoListAndCacheByRedis(idsList, videoKey);
        if (CollectionUtils.isEmpty(livingVideos)) {
            resultJson.put("videoCnt", 0);
            resultJson.put("videoList", livingVideos);
            return resultJson;
        }

        List<Map<String, Object>> lives = new ArrayList<>();
        // 直播首页勋章信息只展示一周
        for(Map<String, String> live:livingVideos){
            Map<String, Object> tempMap = new HashMap<>();
            tempMap.putAll(live);
            long anchorId = MapUtils.getLongValue(live, "anchorId", 0);
            List<String> medals = medalHonorService.getIndexAnchorMedal(anchorId);
            tempMap.put("medals", medals);
            lives.add(tempMap);
        }
        
        resultJson.put("videoCnt", lives.size());
        resultJson.put("videoList", lives);

        return resultJson;
    }

    private List<Map<String, String>> getLivingVideoListAndCacheByRedis(List<Long> idsList, String videoKey) {
        List<Map<String, String>> videoList = new ArrayList<Map<String, String>>();
        for (Long id : idsList) {
            if (id.longValue() == 0L) {
                continue;
            }
            // 排除定制版的主播直播，以后更改为创建直播时控制
            if(redisAdapter.setIsMember(RedisKey.DINGZHI_ANCHOR,id+"")){
                continue;
            }
            String livingKey = RedisKey.ANCHOR_LIVING_VIDEO_ + id;
            String key = RedisKey.LIVE_VIDEO_INFO_;
            Long videoId = new Long(0);
            if (redisAdapter.existsKey(livingKey)) {
                videoId = Long.valueOf(redisAdapter.strGet(livingKey));
                key += videoId.toString();
            } else {
                LiveVideoInfo info = findBannerVideoInfoByAnchorId(id);
                if (info == null) {
                    redisAdapter.zsetRem(videoKey, id.toString());
                } else {
                    videoId = info.getVideoId();
                    key += videoId.toString();
                    redisAdapter.hashMSet(key, info.putFieldValueToStringMap());
                    redisAdapter.expireKey(key, RedisExpireTime.EXPIRE_DAY_7);
                }
            }

            Map<String, String> videoMap = null;
            if (redisAdapter.existsKey(key)) {
                videoMap = redisAdapter.hashMGet(key, "viewers", "chatroomId", "videoId", "videoName", "coverImg",
                        "anchorId", "formatType");
            } else {
                videoMap = LiveVideoInfoHelper.getAndCacheVideoInfoStringMap(redisAdapter, liveVideoInfoMapper,
                        videoId);
            }
            if (videoMap != null && !videoMap.isEmpty()) {
                videoList.add(videoMap);
            } else {
                redisAdapter.zsetRem(videoKey, id.toString());
            }
        }
        return videoList;

    }

    private LiveVideoInfo findBannerVideoInfoByAnchorId(Long videoId) {
        LiveVideoInfo info = liveVideoInfoMapper.findBannerVideoInfoByAnchorId(videoId);
        return info;
    }
    /*
     * private int minNum(int num1, int num2) { if (num1 > num2) { return num2;
     * } return num1; }
     */

    @Override
    public Map<String, Object> getRankList(int pageNo, int pageSize, int anchorId) {
        int startRow = (pageNo - 1) * pageSize;
        Map<String, Object> resultJson = new HashMap<>();
        List<Map<String, Object>> rankList = liveVideoInfoMapper.getRankList(startRow, pageSize, anchorId);
        if (rankList == null) {
            resultJson.put("rankList", new ArrayList<Map<String, Object>>());
            return resultJson;
        }
        for (Map<String, Object> userInfo : rankList) {
            
            // 添加土豪勋章信息
            long userId = MapUtils.getLongValue(userInfo, "userId", 0);
            userInfo.put("medals", medalHonorService.getUserMedalsById(userId));
            
            if (userInfo.get("place") instanceof Double) {
                userInfo.put("place", ((Double) userInfo.get("place")).intValue());
            }
            Date birthday = (Date) userInfo.get("birthday");
            userInfo.put("age", DateTools.getAgeByDate(birthday));
            //增加贵族等级及贵族图片
            Integer level = RoleNobleHelper.userRoleNobleLevel(redisAdapter, roleInfoMapper, userId);
            if(level != null && level.intValue() > 0) {
            	userInfo.put("nobleCode", level);
            }
        }
        
        resultJson.put("rankList", rankList);
        return resultJson;
    }

    @Override
    public Map<String, Object> getHistotyVideoList(int pageSize, int pageNo, String cnid) {
        List<Map<String, Object>> hisList = new ArrayList<Map<String, Object>>();
        int startRow = (pageNo - 1) * pageSize;
        // 视频状态 0准备中 1直播中 2结束无点播 3删除 4结束有点播
        // 从数据库中查出直播结束的视频(videoStatus = 4)
        String deadline = DateTools.getAWeekAgoDate(Calendar.getInstance());
        String channel = redisAdapter.strGet("qudao");
        int videoCnt = 0;

        if (StringUtils.isNotBlank(channel) && channel.equals(cnid)) {
            videoCnt = liveVideoInfoMapper.countGrayLivedInTime(deadline);
        } else {
            videoCnt = liveVideoInfoMapper.countLivedInTime(deadline);
        }

        Map<String, Object> resultJson = new HashMap<>();
        if (videoCnt > 0) {

            List<Map<String, Object>> historyList = null;
            if (StringUtils.isNotBlank(channel) && channel.equals(cnid)) {
                historyList = liveVideoInfoMapper.findGrayHistoryVideos(startRow, pageSize, deadline);
            } else{
                historyList = liveVideoInfoMapper.findHistoryVideos(startRow, pageSize, deadline);
            }

            if (historyList != null) {
                // 历史直播实际观看人次 = 表中viewers + redis中clickers
                for (Map<String, Object> map : historyList) {
                    long anchorId = MapUtils.getLongValue(map, "anchorId");
                    if(redisAdapter.setIsMember(RedisKey.DINGZHI_ANCHOR,anchorId+"")){
                        continue;
                    }
                    BigInteger viewers = (BigInteger) map.get("viewers");
                    String cntKey = RedisKey.HISTORY_VIEW_CNT_ + map.get("videoId");
                    String redisViewers = redisAdapter.strGet(cntKey);
                    map.put("viewers", viewers.intValue() + (redisViewers == null ? 0 : Integer.valueOf(redisViewers)));
                    hisList.add(map);
                }

                resultJson.put("videoCnt", videoCnt);
                resultJson.put("videoList", hisList);
            }
        } else {
            resultJson.put("videoCnt", 0);
            resultJson.put("videoList", new ArrayList<Map<String, String>>());
        }

        return resultJson;
    }

    @Override
    public Map<String, Object> getHistoryVideoDetail(long userId, long anchorId, long videoId, String loginId,String cnid,String version) {
    	if (userId <= 0) {
			if ("-1".equals(loginId)) {
				throw new IWanviDataBaseException("无法获得用户信息, userId: " + userId + ", loginId: " + loginId);
			}
    		String userLogin = RedisKey.USER_LOGIN_ID_ + loginId;
			if (redisAdapter.existsKey(userLogin)) {
				String str = redisAdapter.strGet(userLogin);
				if (str.contains("\"")) {
					str = str.replace("\"", "");
					redisAdapter.strSetByNormal(userLogin, str);
					redisAdapter.expireKey(userLogin, RedisExpireTime.EXPIRE_DAY_7);
				}
				userId = Integer.valueOf(str);
			} else {
		        UserInfo userInfo = UserInfoHelper.getAndCacheUserInfoByLoginId(redisAdapter, userInfoMapper, loginId);
		        if (userInfo == null) {
					throw new IWanviDataBaseException("无法获得用户信息, userId: " + userId + ", loginId: " + loginId);
		        } else {
		        	userId = userInfo.getUserId();
		        }
			}
			
		}
        Map<String, Object> resultJson = new HashMap<>();
        Map<String, Object> historyDetail = new HashMap<>();
        getVideoinfo4History(historyDetail, userId, anchorId, videoId);// 获得历史信息
        if (historyDetail == null || historyDetail.isEmpty()) {
            return resultJson;
        }
        getAnchorInfo4History(historyDetail, anchorId);// 主播信息
        setVideoViewers(userId, videoId);// 视频观看者

        synchroHistoryVideoCnt(historyDetail, videoId);// 同步点击量

        getBlackMsg4History(historyDetail, userId);// 是否是超管禁言
        resultJson.putAll(historyDetail);

        LiveVideoInfoVo vo = liveVideoInfo2Service.liveAdvert(new LiveVideoInfoVo(), cnid, version);
        resultJson.put("liveImg",vo.getLiveImg());
        resultJson.put("jumpUrl",vo.getJumpUrl());
        resultJson.put("advId",vo.getAdvId());
        resultJson.put("advName",vo.getAdvName());
        return resultJson;
    }

    private void getVideoinfo4History(Map<String, Object> historyDetail, long userId, long anchorId, long videoId) {
        // 获取历史直播详情
        String histKey = RedisKey.LIVE_VIDEO_INFO_ + videoId;
        if (redisAdapter.existsKey(histKey)) {
            // 从redis中获取直播详情
            Map<String, String> histDetail = redisAdapter.hashMGet(histKey, "videoId", "viewers", "roomNum", "vdoid",
                    "streamName");
            historyDetail.putAll(histDetail);
        } else {
            // 查询数据库获取直播详情
            LiveVideoInfo info = liveVideoInfoMapper.findVideoInfoById(videoId);
            if (info == null) {
                return;
            }
            historyDetail.put("videoId", info.getVideoId());
            historyDetail.put("viewers", info.getViewers());
            historyDetail.put("roomNum", info.getRoomNum());
            historyDetail.put("vdoid", info.getVdoid());
            historyDetail.put("streamName", info.getStreamName());
            // 返回的字段值都转换成String类型
            Map<String, Object> map = info.putFieldValueToMap();
            map.put("anchorName",liveVideoInfoMapper.getAnchorNameById(info.getAnchorId()));
            redisAdapter.hashMSet(histKey, map);// 缓存到redis中
            // 设置缓存信息有效期
            redisAdapter.expireKey(histKey, RedisExpireTime.EXPIRE_DAY_7);

        }

        if (historyDetail == null || historyDetail.isEmpty()) {
            return;
        }

        String userKey = RedisKey.USER_INFO_ + userId;
        if (redisAdapter.existsKey(userKey)) {
            Map<String, String> userNameMap = redisAdapter.hashMGet(userKey, "userName");
            historyDetail.put("viewerName", userNameMap.get("userName"));
        } else {
            UserInfo user = UserInfoHelper.getAndCacheUserInfo(redisAdapter, userInfoMapper, userId);
            historyDetail.put("viewerName", user.getUserName());
        }

    }

    private void getAnchorInfo4History(Map<String, Object> historyDetail, long anchorId) {
        String anchorKey = RedisKey.ANCHOR_INFO_ + anchorId;
        Anchor anchor = null;
        try {
            if (!redisAdapter.existsKey(anchorKey)) {
                anchor = AnchorInfoHelper.getAndCacheAnchorInfo(redisAdapter, anchorMapper, anchorId, "userName",
                        "headImg", "sex");
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
    }

    private void getBlackMsg4History(Map<String, Object> historyDetail, long userId) {
        historyDetail.put("isblack", Constants._0);
        historyDetail.put("gagOfSuper", Constants._0);
        if (BlackListHelper.isOnBlackList(blackListMapper, redisAdapter, userId)) {
            historyDetail.put("isblack", Constants._1);
            historyDetail.put("gagOfSuper", Constants._1);
            historyDetail.put("gagTime", Integer.MAX_VALUE);
        }

    }

    /**
     * 同步点播视频点击量
     * 
     * @param historyDetail
     * @param videoId
     */
    private void synchroHistoryVideoCnt(Map<String, Object> historyDetail, long videoId) {
        // 获取记录次数
        String cntKey = RedisKey.HISTORY_VIEW_CNT_ + videoId;
        long cnt = redisAdapter.strIncr(cntKey);
        redisAdapter.expireKey(cntKey, RedisExpireTime.EXPIRE_DAY_20);

        if (cnt == WebConstants.HISTORY_VIEWERS_UPDATE_BUFFER) {
            liveVideoInfoMapper.addViewers(videoId, WebConstants.HISTORY_VIEWERS_UPDATE_BUFFER);
            String histKey = RedisKey.LIVE_VIDEO_INFO_ + videoId;
            if (redisAdapter.existsKey(histKey)) {
                // 更新缓存中直播详情信息的观看人数
                redisAdapter.hashIncrBy(histKey, "viewers", WebConstants.HISTORY_VIEWERS_UPDATE_BUFFER);
            }
            redisAdapter.strSetByNormal(cntKey, "0");
        }
        String redisViewers = redisAdapter.strGet(cntKey);
        BigInteger viewers = new BigInteger("0");
        Object obj = historyDetail.get("viewers");
        if (obj != null) {
            if (obj instanceof String) {
                viewers = new BigInteger(((String) obj));
            } else if (obj instanceof Long) {
                viewers = BigInteger.valueOf(((Long) obj));
            }
        }
        String streamName = (String) historyDetail.get("streamName");
        if (StringUtils.isNotBlank(streamName)) {
            String[] names = KSCloudFacade.getDemandHlsURLs(streamName);
            String standVdoid = names[0];
            String vdoid = (String) historyDetail.get("vdoid");
            if (StringUtils.isNotBlank(vdoid)) {
                standVdoid = vdoid;
            }
            historyDetail.put("standvdoid", standVdoid);
            historyDetail.put("highvdoid", names[1]);
        }

        int inc = 0;
        if (cnt == WebConstants.HISTORY_VIEWERS_UPDATE_BUFFER) {
            inc = 10;
        } else {
            inc = redisViewers == null ? 0 : Integer.valueOf(redisViewers);
        }

        historyDetail.put("viewers", viewers.intValue() + inc);

    }

    /**
     * 
     * @param userId
     * @param videoId
     */
    private void setVideoViewers(long userId, long videoId) {
        String viwersKey = RedisKey.HISTORY_VIDEO_VIEWERS_ + videoId;
        if (redisAdapter.existsKey(viwersKey)) {
            boolean isMember = redisAdapter.setIsMember(viwersKey, userId + "");
            if (!isMember) {
                synchronHistryViwers(userId, videoId);
            }
        } else {
            synchronHistryViwers(userId, videoId);
        }
    }

    private void synchronHistryViwers(long userId, long videoId) {
        // 添加user_video记录
        int userVideoCnt = userVideoMapper.getCntByUser(userId);
        if (userVideoCnt == 0) {
            // 添加记录
            userVideoMapper.add(userId, videoId);
        } else {
            // 修改记录
            userVideoMapper.modify(userId, videoId);
        }
        String viwersKey = RedisKey.HISTORY_VIDEO_VIEWERS_ + videoId;
        if (redisAdapter.existsKey(viwersKey)) {
            redisAdapter.setAdd(viwersKey, userId + "");
        } else {
            redisAdapter.setAdd(viwersKey, userId + "");
            redisAdapter.expireKey(viwersKey, RedisExpireTime.EXPIRE_DAY_30);

        }
    }

    @Override
    public Map<String, Object> exitHistoryVideo(long userId, long videoId) {
        Map<String, Object> resultJson = new HashMap<>();
        // 退出观看, videoId设为0
        String viwersKey = RedisKey.HISTORY_VIDEO_VIEWERS_ + videoId;
        if (redisAdapter.existsKey(viwersKey)) {
            redisAdapter.setRem(viwersKey, userId + "");
            int result = userVideoMapper.updateByLeave(userId, 0);
            resultJson.put("result", result);
        } else {
            resultJson.put("result", 0);
        }
        return resultJson;
    }

    /**
     * 分享
     */
    @Override
    public Map<String, Object> getShareModel(Long anchorId, Long videoId) {
        Map<String, Object> resultMap = getAnchorInf(anchorId);// 主播信息
        getVideoInfo(resultMap, anchorId, videoId);
        return resultMap;
    }

    /**
     * 分享
     */
    @Override
    public Map<String, Object> appStroreTest() {
        Anchor anchor = anchorMapper.getAnchorByLogin("appletest1@chineseall.com");
        Map<String, Object> resultMap = getAnchorInf(anchor.getAnchorId());// 主播信息
        LiveVideoInfo videoInfo = liveVideoInfoMapper.getAppStoreTestVideo(anchor.getAnchorId());
        if (videoInfo == null) {
            resultMap.put("playAddress", "");
            resultMap.put("videoStatus", 3);// 根据状态判断播放or展示静态文字
            resultMap.put("coverImg", "");
            return resultMap;
        }
        resultMap.put("videoName", videoInfo.getVideoName());
        // 直播视频和历史视频分开处理
        Integer videoStatus = videoInfo.getVideoStatus();
        String pullStreamAddress = null;
        if (videoStatus == 4) {
            // 历史视频
            String vdoid = videoInfo.getVdoid();
            pullStreamAddress = m3u8URL(videoInfo.getStreamName(),
                    vdoid.substring(vdoid.lastIndexOf("-") + 1, vdoid.lastIndexOf(".")));
        } else if (videoStatus == 1) {
            // 正在直播视频, 获取标清格式
            pullStreamAddress = KSCloudFacade.getHlsURL(videoInfo.getStreamName());
        }

        resultMap.put("playAddress", pullStreamAddress);
        resultMap.put("videoStatus", videoInfo.getVideoStatus());// 根据状态判断播放or展示静态文字
        resultMap.put("coverImg", videoInfo.getCoverImg());
        resultMap.put("formatType", videoInfo.getFormatType());
        return resultMap;
    }

    private Map<String, Object> getAnchorInf(Long anchorId) {
        Map<String, Object> resultMap = new HashMap<>();

        String anchorKey = RedisKey.ANCHOR_INFO_ + anchorId;
        // 直接从缓存中读取数据
        Map<String, String> anchorInfo = redisAdapter.hashGetAll(anchorKey);
        if (anchorInfo == null || anchorInfo.isEmpty()) {
            Anchor anchor = anchorMapper.findAnchorById(anchorId);
            if (anchor != null) {
                redisAdapter.hashMSet(anchorKey, anchor.putFieldValueToMap());
                redisAdapter.expireKey(anchorKey, RedisExpireTime.EXPIRE_DAY_30);
                anchorInfo = anchor.putFieldValueToStringMap();
            } else {
                resultMap.put("userName", "");
                resultMap.put("headImg", "");
                resultMap.put("roomNum", "");
                resultMap.put("zodiac", "");
                resultMap.put("notice", "");
                resultMap.put("sex", "");
            }

        } else {
            resultMap.put("userName", anchorInfo.get("userName"));
            resultMap.put("headImg", anchorInfo.get("headImg"));
            resultMap.put("roomNum", anchorInfo.get("roomNum"));
            resultMap.put("zodiac", anchorInfo.get("zodiac"));
            resultMap.put("notice", anchorInfo.get("notice"));
            resultMap.put("sex", anchorInfo.get("sex"));
        }
        return resultMap;
    }

    private void getVideoInfo(Map<String, Object> resultMap, Long anchorId, Long videoId) {
        // 按照人数排序, 获取该主播的最多4个历史直播
        List<Map<String, Object>> historyVideos = liveVideoInfoMapper.find4BestHstVideos(anchorId);

        resultMap.put("videoList", historyVideos);

        LiveVideoInfo videoInfo = liveVideoInfoMapper.findVideoInfoById(videoId);
        if (videoInfo == null) {
            resultMap.put("playAddress", "");
            resultMap.put("videoStatus", 3);// 根据状态判断播放or展示静态文字
            resultMap.put("coverImg", "");
            return;
        }
        resultMap.put("videoName", videoInfo.getVideoName());
        // 直播视频和历史视频分开处理
        Integer videoStatus = videoInfo.getVideoStatus();
        String pullStreamAddress = null;
        if (videoStatus == 4) {
            // 历史视频
            String vdoid = videoInfo.getVdoid();
            pullStreamAddress = m3u8URL(videoInfo.getStreamName(),
                    vdoid.substring(vdoid.lastIndexOf("-") + 1, vdoid.lastIndexOf(".")));
        } else if (videoStatus == 1) {
            // 正在直播视频, 获取标清格式
            pullStreamAddress = KSCloudFacade.getHlsURL(videoInfo.getStreamName());
        }

        resultMap.put("playAddress", pullStreamAddress);
        resultMap.put("videoStatus", videoInfo.getVideoStatus());// 根据状态判断播放or展示静态文字
        resultMap.put("coverImg", videoInfo.getCoverImg());
        resultMap.put("formatType", videoInfo.getFormatType());
    }

    /**
     * 生成点播文件m3u8
     * http://wwlive.ks3-cn-beijing.ksyun.com/record/live/LIVEZX156491833A0/hls/
     * LIVEZX156491833A0-1470998780.m3u8
     * 
     * @param streamName
     * @param vdoid
     * @return
     */
    private String m3u8URL(String streamName, String vdoid) {
        return KSCloudFacade.IWANVI_HLSLIVE_OUT + streamName + Constants.SEPARATOR + KSCloudFacade.HLS
                + Constants.SEPARATOR + streamName + Constants.MINUS + vdoid + KSCloudFacade.M3U8;
    }

    @Override
    public Map<String, Object> getAnchorInfoForHst(long anchorId) {
        // 获取主播资料

        // 该方法没有被用到, 所以将用户id简单设为null保证语法正确 --牛强鸿 2017-5-10 11:07:07
        Map<String, Object> anchorInfo = anchorService.getAnchorInfo(anchorId, null);
        Map<String, Object> resultJson = new HashMap<>();
        resultJson.putAll(anchorInfo);
        return resultJson;
    }

    public Map<String, Object> getLivingByAnchorId(long anchorId) {
        Map<String, Object> videoInfo = liveVideoInfoMapper.getLivingByAnchorId(anchorId);

        long videoId = MapUtils.getLongValue(videoInfo, "videoId", 0);
        String videoKey = RedisKey.LIVE_VIDEO_INFO_ + videoId;
        if (videoInfo == null) {
            return null;
        }

        // 独立版添加正在直播的观看人数 2017-7-14 11:17:29
        String viewers = redisAdapter.hashGet(videoKey, "viewers");
        videoInfo.put("viewers", StringUtils.isBlank(viewers) ? 0 : viewers);

        Double score = redisAdapter.zsetScore(RedisKey.VideoGrayKeys.LIVING_VIDEOS_, anchorId + "");//灰度代码
        if (score != null) {
        	return videoInfo;
        }
        score = redisAdapter.zsetScore(RedisKey.VideoKeys.LIVING_VIDEOS_, anchorId + "");
    	Object streamName = videoInfo.get("streamName");
        if (score == null && streamName != null) {
            String url = KSCloudFacade.addBlack(KSCloudFacade.LIVE_NAME, (String) streamName); //将视频流加入黑名单
            try {
                HttpURLConnection conn = HttpUtils
                        .createPostHttpConnection(url);
                HttpUtils.returnResult(conn);
            } catch (Exception e) {
                LOGGER.error("停止直播异常", e);
            }
            // 删除正在直播的信息
            liveVideoInfoMapper.delLivingVideoByAnchorId(anchorId);
            return null;
        }
        return videoInfo;

    }

    @Override
    public Map<String, Object> getGrayLivingVideos() {
        Map<String, Object> resultJson = new HashMap<>();
        // 从redis中获取正在直播的视频id集合
        // String dateInfo = DateUtil.formatDate(new Date(), "yyMMdd");

        String videoKey = RedisKey.VideoGrayKeys.LIVING_VIDEOS_;// + dateInfo

        Set<String> ids = redisAdapter.zsetRevRange(videoKey, 0, -1);

        if (ids == null || ids.size() == 0) {
            resultJson.put("videoCnt", 0);// 视频个数为0
            resultJson.put("videoList", new ArrayList<Map<String, String>>());
            return resultJson;
        }

        List<Long> idsList = new ArrayList<>();
        // 将String类型的id转换为Integer类型的
        for (String id : ids) {
            idsList.add(Long.valueOf(id));
        }

        if (idsList.size() == 1 && idsList.get(0) == 0) {// 防止 living_videos_
                                                         // 无法取到值，故在此列表中放入假数据0
            resultJson.put("videoCnt", 0);// 视频个数为0
            resultJson.put("videoList", new ArrayList<Map<String, String>>());
            return resultJson;
        }

        // 根据视频id获得视频信息
        List<Map<String, String>> livingVideos = getLivingVideoListAndCacheByRedis(idsList, videoKey);
        if (CollectionUtils.isEmpty(livingVideos)) {
            resultJson.put("videoCnt", 0);
            resultJson.put("videoList", livingVideos);
            return resultJson;
        }

        List<Map<String, Object>> lives = new ArrayList<>();
        // 直播首页勋章信息只展示一周
        for(Map<String, String> live:livingVideos){
            Map<String, Object> tempMap = new HashMap<>();
            tempMap.putAll(live);
            long anchorId = MapUtils.getLongValue(live, "anchorId", 0);
            List<String> medals = medalHonorService.getIndexAnchorMedal(anchorId);
            tempMap.put("medals", medals);
            lives.add(tempMap);
        }
        
        resultJson.put("videoCnt", lives.size());
        resultJson.put("videoList", lives);

        return resultJson;
    }
    
    
    /**
     * 获得直播间主播获得火箭列表详情
     * @param anchorId
     * @return
     */
    public List<JSONObject> getRocketInfoList(long anchorId) {
		String rocketKey = RedisKey.ROCKET_GIVER_LIST_ + anchorId + Constants.UNDERLINE 
				+ DateFormatUtils.format(new Date(), Constants.YY_MM_DD);//当日主播所有视频有效
		if(redisAdapter.existsKey(rocketKey)) {
			long currentTime = new Date().getTime();
			List<String> list = redisAdapter.listRange(rocketKey, 0, -1);
			if (CollectionUtils.isEmpty(list)) {
				return new ArrayList<>();
			}
			List<JSONObject> result = new ArrayList<>();
			for (String str : list) {
				JSONObject giver = JSONObject.parseObject(str, JSONObject.class);
				long expireTime = giver.getLong("expireTime");
				if (expireTime > currentTime) {
					long remainingTime = ((expireTime - currentTime) / 1000);//时间算出来
					if (remainingTime >= RedisExpireTime.EXPIRE_MIN_10) {
						giver.put("expireTime", RedisExpireTime.EXPIRE_MIN_10);
					} else {
						giver.put("expireTime", remainingTime);
					}
					long userId = giver.getLongValue("userId");
					String userKey = RedisKey.USER_INFO_ + userId;
					String userName = "";
					if (redisAdapter.existsKey(userKey)) {
						userName = redisAdapter.hashGet(userKey, "userName");
					} else {
						UserInfo user = UserInfoHelper.getAndCacheUserInfo(redisAdapter, userInfoMapper, userId);
						userName = (user == null) ? "" : user.getUserName();
					}
					giver.put("userName", (userName));
					result.add(giver);
				} else {//移除
					redisAdapter.listRem(rocketKey, str);
				}
			}
			return result;
		} else {
			return new ArrayList<>();
		}
    	
    }
    
}
