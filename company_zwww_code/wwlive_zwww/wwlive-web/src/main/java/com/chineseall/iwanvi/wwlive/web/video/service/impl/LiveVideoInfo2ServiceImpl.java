package com.chineseall.iwanvi.wwlive.web.video.service.impl;

import java.math.BigDecimal;
import java.util.*;

import com.chineseall.iwanvi.wwlive.dao.wwlive.*;
import com.chineseall.iwanvi.wwlive.web.common.helper.*;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import redis.clients.jedis.Tuple;

import com.chineseall.iwanvi.wwlive.common.cache.RedisClientAdapter;
import com.chineseall.iwanvi.wwlive.common.cache.RedisExpireTime;
import com.chineseall.iwanvi.wwlive.common.cache.RedisKey;
import com.chineseall.iwanvi.wwlive.common.constants.Constants;
import com.chineseall.iwanvi.wwlive.common.exception.IWanviDataBaseException;
import com.chineseall.iwanvi.wwlive.common.external.kscloud.KSCloudFacade;
import com.chineseall.iwanvi.wwlive.dao.wwlive.AcctInfoMapper;
import com.chineseall.iwanvi.wwlive.dao.wwlive.AnchorMapper;
import com.chineseall.iwanvi.wwlive.dao.wwlive.BaseDictInfoMapper;
import com.chineseall.iwanvi.wwlive.dao.wwlive.BlackListMapper;
import com.chineseall.iwanvi.wwlive.dao.wwlive.ContributionListMapper;
import com.chineseall.iwanvi.wwlive.dao.wwlive.LiveAdminMapper;
import com.chineseall.iwanvi.wwlive.dao.wwlive.LiveVideoInfoMapper;
import com.chineseall.iwanvi.wwlive.dao.wwlive.RoleInfoMapper;
import com.chineseall.iwanvi.wwlive.dao.wwlive.UserInfoMapper;
import com.chineseall.iwanvi.wwlive.domain.wwlive.Anchor;
import com.chineseall.iwanvi.wwlive.domain.wwlive.LiveVideoInfo;
import com.chineseall.iwanvi.wwlive.domain.wwlive.UserInfo;
import com.chineseall.iwanvi.wwlive.web.event.service.LevelEventService;
import com.chineseall.iwanvi.wwlive.web.event.service.MedalHonorService;
import com.chineseall.iwanvi.wwlive.web.video.service.LiveVideoInfo2Service;
import com.chineseall.iwanvi.wwlive.web.video.service.NoticeService;
import com.chineseall.iwanvi.wwlive.web.video.vo.LiveVideoInfoVo;

@Service
public class LiveVideoInfo2ServiceImpl implements LiveVideoInfo2Service {

    static final Logger LOGGER = Logger.getLogger(LiveVideoInfo2ServiceImpl.class);
    
	@Autowired
	LiveVideoInfoMapper liveVideoInfoMapper;

	@Autowired
	AnchorMapper anchorMapper;
	
	@Autowired
	RedisClientAdapter redisAdapter;

	@Autowired
	AdvertMapper advertMapper;

	@Autowired
	private BaseDictInfoMapper baseDictInfoMapper;

	@Autowired
	private BlackListMapper blackListMapper;
	
    @Autowired
    private UserInfoMapper userInfoMapper;

	@Autowired
	AcctInfoMapper acctInfoMapper;
	
	@Autowired
	private LiveAdminMapper adminMapper;
	
	@Autowired
    private ContributionListMapper contribMapper;
	
    @Value("${cx.ratio}")
    private String cxRatio;

    @Value("${sc.ratio}")
    private String scRatio;

	@Autowired
    private NoticeService noticeService;
	
	@Autowired
	private MedalHonorService medalHonorService;
	
    @Autowired
    private LevelEventService levelEventService;

    @Autowired
    private RoleInfoMapper roleInfoMapper;
    
	@Override
	public strictfp LiveVideoInfoVo getLiveVideoInfo(long userId, long videoId, String loginId,String cnid,String version) {
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
		LiveVideoInfo info = getLiveVideo(videoId);
		LiveVideoInfoVo vo = null;
		Long anchorId = 0L;
		if (info != null && info.getVideoStatus() == 1) {
			vo = getLiveVideoVo(info, userId, videoId);//组装返回详情
			anchorId = vo.getAnchorId();
			//直播视频点击次数+1
			try {
                String clickCntKey = RedisKey.LIVING_CLICKCNT_ + videoId;
                if (redisAdapter.existsKey(clickCntKey)) {
                    redisAdapter.strIncrBy(clickCntKey, 1);
                } else {
                	String key = RedisKey.LIVE_VIDEO_INFO_ + videoId;
                	if (redisAdapter.existsKey(key)) {
                		redisAdapter.hashSet(key, "viewers", "1");
                	}
                    redisAdapter.strIncrBy(clickCntKey, 1);
                    redisAdapter.expireKey(clickCntKey, RedisExpireTime.EXPIRE_DAY_5);
                }
            } catch (Exception e) {
                LOGGER.error("直播详情视频点击次数+1异常", e);
                e.printStackTrace();
            }
			//用户id添加到观看过该直播的set中, 用于在主播移动端获取观看过的用户数
			String userIdsKey = RedisKey.LIVING_VIDEO_VIEWED_USERS_ + videoId;
            redisAdapter.setAdd(userIdsKey, userId + "");
            redisAdapter.expireKey(userIdsKey, RedisExpireTime.EXPIRE_DAY_1);
            noticeService.manJoinLive(videoId, vo.getChatroomId(), anchorId, userId);
		}
		//返回用户性别   0 女  1 男
        UserInfo user = UserInfoHelper.getUserInfoFromCache(redisAdapter, userId, "sex");
        if(user != null) {
            vo.setSex(user.getSex());
        }else {
            vo.setSex(1);
        }
        // 设置当前主播的钻石数
		vo.setDiamonds(levelEventService.getCurDiamonds(anchorId));
		// 设置当前主播关卡数
        vo.setLevels(levelEventService.getCurLevels(anchorId));
        // 设置通关所需钻石数
        vo.setDiamondsPerLv(Constants.PER_LEVEL_SCORE);
		// 关卡活动需求: 添加主播获取的勋章字段
		vo.setMedals(medalHonorService.getUserMedalsById(userId));
		//设置贵族信息
		setRoleNobleInfo(vo, userId);
		// 查询贡献值
		vo.setContrib(ContribHelper.getNormalByAnchor(anchorId, contribMapper, redisAdapter));

		//获取直播广告
		vo = liveAdvert(vo, cnid, version);
		return vo;
		
	}

	/**
	 * 增加 贵族列表、贵族数量、弹幕颜色、如果是贵族展示
	 * @param vo
	 * @param userId
	 */
	private void setRoleNobleInfo(LiveVideoInfoVo vo, long userId) {
		Integer lv = RoleNobleHelper.userRoleNobleLevel(redisAdapter, roleInfoMapper, userId);
		if (lv != null && lv.intValue() > 0) {
			addNobles(userId, lv, vo);
			vo.setNobleCode(lv.intValue());
		} else {
			vo.setNobleCode(0);
		}
	}
	
	private void addNobles(long userId, Integer lv, LiveVideoInfoVo vo) {
		redisAdapter.zsetAdd(RedisKey.NobleKey.VIDEO_NOBLES_ + vo.getVideoId(), lv.doubleValue(), userId + "");
		redisAdapter.expireKey(RedisKey.NobleKey.VIDEO_NOBLES_ + vo.getVideoId(), RedisExpireTime.EXPIRE_DAY_5);
	}
	
	private LiveVideoInfo getLiveVideo(long videoId) {
		LiveVideoInfo info = null;
		String key = RedisKey.LIVE_VIDEO_INFO_ + videoId;
		if (redisAdapter.existsKey(key)) {
			info = LiveVideoInfoHelper.getVideoInfoCache(redisAdapter, liveVideoInfoMapper, videoId, 
					"videoId", "chatroomId", "viewers", "videoName", "anchorId", "vdoid", "roomNum", "streamName", "videoStatus", "formatType");
		} else {
			info = LiveVideoInfoHelper.getAndCacheVideoInfo(redisAdapter, liveVideoInfoMapper, videoId);
		}
		if (info == null) {
			return null;
		}
		String livingKey = RedisKey.VideoGrayKeys.LIVING_VIDEOS_;//灰度存在的，观众的一定存在 灰度代码
		Double score = redisAdapter.zsetScore(livingKey, info.getAnchorId() + "");
//		String livingGrayKey = RedisKey.VideoGrayKeys.LIVING_VIDEOS_;
//		Double scoreGray = redisAdapter.zsetScore(livingGrayKey, info.getAnchorId() + "");
		if (score == null) {
			return null;
		}
		
		if (info != null) {
			Long anchorId = info.getAnchorId();
			String anchorKey = RedisKey.ANCHOR_INFO_ + anchorId;
			if (redisAdapter.existsKey(anchorKey)) {
				Map<String, String> anchorMap =  redisAdapter.hashMGet(anchorKey, "userName");
				info.setUserName(anchorMap.get("userName"));
			} else {
				Anchor anchor = AnchorInfoHelper.getAndCacheAnchorInfo(redisAdapter, anchorMapper, anchorId);
				info.setUserName(anchor.getUserName());
			}
		}

		return info;
	}
	
	private LiveVideoInfoVo getLiveVideoVo(LiveVideoInfo info, long userId, long videoId) {

		LiveVideoInfoVo vo = corvertVideoDomain2Vo(info);
		
		//获得用户融云id，即login_id
		setLoginId(vo, userId, vo.getAnchorId());//LiveVideoInfoVo 赋值loginId、用户类型、 虚拟货币、融云token、用户昵称，及设置是否被禁言
		setVideoViewers(userId, vo);
		setIsCustomer(vo, vo.getAnchorId(), userId);//设置是否送过礼字段
		
		vo.setPointRate(vo.getOrigin() == 0 ? Integer.parseInt(cxRatio) : Integer.parseInt(scRatio));
		return vo;
	}
	
	private void setIsCustomer(LiveVideoInfoVo vo, Long anchorId, Long userId) {
	    String key = RedisKey.USER_CONTRIBUTION_CNT_ + anchorId + Constants.UNDERLINE + userId;
        if (redisAdapter.existsKey(key)) {
            String cnt = redisAdapter.strGet(key);
            vo.setIsCustomer(Integer.valueOf(cnt));
        } else {
            int cnt = contribMapper.countByAnchorAndUser(anchorId, userId, 1);
            redisAdapter.strSet(key, cnt);
            redisAdapter.expireKey(key, RedisExpireTime.EXPIRE_DAY_1);
            vo.setIsCustomer(cnt);
        }
    }

    private void setVideoViewers(long userId, LiveVideoInfoVo vo) {
		String livingViwers = RedisKey.LIVING_VIDEO_VIEWRES_ + vo.getVideoId();
		//如果用户非正常退出时，某正在直播视频人数不正常的bug修改
		removeOldWatchingRecord(userId, livingViwers, vo.getVideoId());
		if (redisAdapter.existsKey(livingViwers)) {
			boolean isMember = redisAdapter.setIsMember(livingViwers, userId + "");
			if (!isMember) {
				setUserWatchingVideo(livingViwers, userId, vo);//设置用户观看视频
			}
		} else {
			setUserWatchingVideo(livingViwers, userId, vo);//设置用户观看视频
			redisAdapter.expireKey(livingViwers, RedisExpireTime.EXPIRE_HOUR_36);
		}

	}
	
    private void setUserWatchingVideo(String livingViwers, long userId, LiveVideoInfoVo vo) {
		redisAdapter.setAdd(livingViwers, userId + "");
		redisAdapter.strSetexByNormal(RedisKey.USER_WATCHING_VIDEO_ + userId, 
				RedisExpireTime.EXPIRE_DAY_5, vo.getVideoId() + "");
		synchronLivingViewers(vo, userId);
    }
    
	private void synchronLivingViewers(LiveVideoInfoVo vo, long userId) {
		incrViewers(userId, vo.getAnchorId());//增加排序权重
		
		String videoKey = RedisKey.LIVE_VIDEO_INFO_ + vo.getVideoId();
        long cnt = 0;
		if (redisAdapter.existsKey(videoKey)) {//增加改人数
	        cnt = redisAdapter.hashIncrBy(videoKey, "viewers", 1);
		} else {
			LiveVideoInfoHelper.getAndCacheVideoInfo(redisAdapter, liveVideoInfoMapper, vo.getVideoId());
			cnt = redisAdapter.hashIncrBy(videoKey, "viewers", 1);
		}
		if (cnt <= 0) {
			cnt = 1;
		}
		vo.setViewers(cnt);
		upsertUseVideoRecord(userId, vo.getVideoId());//添加user_video记录
	}
	
	/**
	 * 更新用户正在观看的直播，如果观看的是同一个直播，则不更新数据库
	 * @param userId
	 * @param videoId
	 */
	private void upsertUseVideoRecord(long userId, long videoId) {
		String key = RedisKey.HAVE_USER_VIDEO_ + userId;
		if (redisAdapter.existsKey(key)) {
			String strId = redisAdapter.strGet(key);
			if (strId != null && strId.equals(videoId + "")) {
				return;
			} else {
		        redisAdapter.strSet(key, videoId);
		        redisAdapter.expireKey(key, RedisExpireTime.EXPIRE_DAY_1);
			}
		} else {
	        redisAdapter.strSet(key, videoId);
	        redisAdapter.expireKey(key, RedisExpireTime.EXPIRE_DAY_1);
		}
	}
	
	/**
	 * 用户离开直播视频
	 */
	@Override
	public strictfp int leaveLiveVideo(long userId, long videoId) {
		if (videoId == 0) {
			return 0;
		}
		LiveVideoInfo info = getLiveVideo(videoId);
		if(info == null) {
			return 0;
		}
		String livingViwers = RedisKey.LIVING_VIDEO_VIEWRES_ + info.getVideoId();
		if (redisAdapter.setIsMember(livingViwers, userId + "")) {
			
			//如果用户非正常退出时，某正在直播视频人数不正常的bug修改
			removeOldWatchingRecord(userId, livingViwers, videoId);
			
			int result = removeUserRecord(userId, videoId);
			decrViewers(livingViwers, userId, info.getAnchorId(), videoId);
			String videoKey = RedisKey.LIVE_VIDEO_INFO_ + videoId;
			
			//配合完成机器人问题
			if (redisAdapter.existsKey(videoKey)) {
				Map<String, String> videoMap = redisAdapter.hashMGet(videoKey, "chatroomId", "anchorId");
				noticeService.manExitLive(videoId, videoMap.get("chatroomId"), Long.valueOf(StringUtils.isNotBlank(videoMap.get("anchorId"))
						? "0" : videoMap.get("anchorId")), userId);
			} else {
				LiveVideoInfo video = LiveVideoInfoHelper.getAndCacheVideoInfo(redisAdapter, liveVideoInfoMapper, videoId);
				if (video != null) {
					noticeService.manExitLive(videoId, video.getChatroomId(), video.getAnchorId(), userId);
					
				}
			}
			//删除贵族信息
			removeNobles(userId, videoId);
			return result;
		}
		return 0;
	}

	private void removeNobles(long userId, long videoId) {
		redisAdapter.zsetRem(RedisKey.NobleKey.VIDEO_NOBLES_ + videoId, userId + "");
	}
	
	@Override
	public Map<String, Object> getWatchingVideoNobles(long videoId) {
		Map<String, Object> result = new HashMap<String, Object>();
		
		List<Map<String, Object>> nobleList = new ArrayList<Map<String,Object>>();
		String key = RedisKey.NobleKey.VIDEO_NOBLES_ + videoId;
        long anchorId = 0;
        LiveVideoInfo videoInfo = null;
		String videoKey = RedisKey.LIVE_VIDEO_INFO_ + videoId;
		if (redisAdapter.existsKey(videoKey)) {
			videoInfo = LiveVideoInfoHelper.getVideoInfoCache(redisAdapter, liveVideoInfoMapper, videoId, "anchorId");
		} else {
			videoInfo = LiveVideoInfoHelper.getAndCacheVideoInfo(redisAdapter, liveVideoInfoMapper, videoId);
		}
        if(videoInfo != null) {
            anchorId = videoInfo.getAnchorId();
        } else {
        	return new HashMap<String, Object>(); 
        }
        //userIds贵族信息 userId和等级
		Set<Tuple> userIds = redisAdapter.zsetRevrangeByScoreWithScores(key, Double.MAX_VALUE, 0.0, 0, 50);
		Long cnt = redisAdapter.zsetCard(key);
		result.put("nobleNum", cnt);//贵族数
		
		if (userIds != null) {//获得贵族信息
			for (Tuple id : userIds) {
				Map<String, String> user = null;
				String userKey = RedisKey.USER_INFO_ + id.getElement();
				if (redisAdapter.existsKey(userKey)) {
					user = redisAdapter.hashMGet(userKey, "headImg", 
							"loginId", "userId", "userName","sex");
				} else {
					user = UserInfoHelper.getAndCacheUserMap(redisAdapter, userInfoMapper, new BigDecimal(id.getElement()).longValue());
				}
				if (user == null) {
					continue;
				}
                String userId = user.get("userId");
                int level = (int) id.getScore();
//				DictInfoEnum dictInfo = DictInfoEnum.getDictInfoEnum(level);
//				String nobleImg = "";
//				if (dictInfo != null) {
//					nobleImg = RoleNobleHelper.getNobleImg(dictInfo, baseDictInfoMapper, redisAdapter);
//				}
//				user.put("nobleImg", nobleImg);
				
				Map<String, Object> userM = new HashMap<String, Object>();
				userM.put("nobleLevel", new Integer(level));
                userM.put("totalAmt", UserRankHelper.getUserInAnchorIdAmt(redisAdapter,contribMapper,userId,anchorId));
                userM.put("sex",user.get("sex"));

                // 查询全站贡献值contrib和全站排名rank
				String rankKey = RedisKey.USER_RANK_CONTRIB_ + userId;
				if (redisAdapter.existsKey(rankKey)) {
					user.putAll(UserRankHelper.getUserStrRankCache(redisAdapter, contribMapper, Long.valueOf(userId)));
				} else {
					user.putAll(UserRankHelper.getAndCacheStrUserRank(redisAdapter, contribMapper, Long.valueOf(userId)));
				}

				userM.putAll(user);
				nobleList.add(userM);
			}
		}
		Collections.sort(nobleList, new Comparator<Map<String, Object>>() {
			@Override
			public int compare(Map<String, Object> noble1, Map<String, Object> noble2) {
				int amt1 = MapUtils.getIntValue(noble1, "contrib", 0);
				int amt2 = MapUtils.getIntValue(noble2, "contrib", 0);
				return amt2 - amt1;
			}
		});
		result.put("nobleList", nobleList);
		return result;
	}
	
	/**
	 * 删除用户观看记录
	 * @param userId
	 * @param videoId
	 * @return
	 */
	private int removeUserRecord(long userId, long videoId) {
		int result = 1;
		String key = RedisKey.HAVE_USER_VIDEO_ + userId;
		if (redisAdapter.existsKey(key)) {
		    redisAdapter.strSet(key, 0);
	        redisAdapter.expireKey(key, RedisExpireTime.EXPIRE_DAY_1);
		} else {
		    redisAdapter.strSet(key, 0);
	        redisAdapter.expireKey(key, RedisExpireTime.EXPIRE_DAY_1);
		}
		return result;
	}
	
	/**
	 * 同步用户旧的观看记录
	 * @param livingViwers
	 * @param userId
	 */
	private void removeOldWatchingRecord(long userId, String livingViwers, long videoId) {
		String key = RedisKey.USER_WATCHING_VIDEO_ + userId;
		if (redisAdapter.existsKey(key)) {
			String strId = redisAdapter.strGet(key);
			String tmpLvingViwers = RedisKey.LIVING_VIDEO_VIEWRES_ + strId;
	        if (!livingViwers.equals(tmpLvingViwers) && redisAdapter.existsKey(tmpLvingViwers)) {
	        	decrViewers(tmpLvingViwers, userId, Long.valueOf(strId), videoId);
	        }
			
		}
	}
	
	/**
	 * 
	 * @param livingViwers 观看的观众信息
	 * @param userId
	 * @param anchorId
	 * @param videoId
	 */
	private strictfp void decrViewers(String livingViwers, long userId, Long anchorId, long videoId) {
    	if (redisAdapter.setIsMember(livingViwers, userId + "")) {
        	redisAdapter.setRem(livingViwers, userId + "");
    		String key = RedisKey.USER_WATCHING_VIDEO_ + userId;
			redisAdapter.delKeys(key);
			
			Double rank = redisAdapter.zsetScore(RedisKey.ANCHOR_RANK, 
					anchorId.toString());
			if (rank != null && rank.doubleValue() > 0.0) {//后台排序不参与 livingKey的增长
				return;
			}
			decrAnchorScore(anchorId);
	    	String videoKey = RedisKey.LIVE_VIDEO_INFO_ + videoId;
	    	String viewers = redisAdapter.hashGet(videoKey, "viewers");
	    	if (StringUtils.isNotBlank(viewers) && Long.valueOf(viewers) <= 0) {
	    		return;
	    	}
	    	if (redisAdapter.existsKey(videoKey)) {
	    		redisAdapter.hashIncrBy(videoKey, "viewers", -1);
	    	}
    	}
	}
	
	private void decrAnchorScore(Long anchorId) {
		String livingKey = RedisKey.VideoKeys.LIVING_VIDEOS_;
		decrScore(livingKey, anchorId);
		livingKey = RedisKey.VideoGrayKeys.LIVING_VIDEOS_;//灰度代码
		decrScore(livingKey, anchorId);
		
	}
	
	private void decrScore(String livingKey, Long anchorId) {
		Double score = 0.0D;
    	if (redisAdapter.existsKey(livingKey) 
    			&& (score = redisAdapter.zsetScore(livingKey, anchorId + "")) != null && score > 0) {
    		if (score >= Constants.LIVING_VIDEO_PAY_SCORE) {
				redisAdapter.zsetIncrBy(livingKey, -0.001D, anchorId + "");
    		} else {
				redisAdapter.zsetIncrBy(livingKey, -1.0D, anchorId + "");
    		}
		}
	}
	
	/**
	 * 增加人数
	 * @param userId
	 * @param anchorId
	 */
	private strictfp void incrViewers(long userId, Long anchorId) {
		Double rank = redisAdapter.zsetScore(RedisKey.ANCHOR_RANK, 
				anchorId.toString());
		if (rank != null && rank.doubleValue() > 0.0) {//后台定义的排序不做人数比较
			return;
		}
		incrAnchorScore(anchorId);
	}
	
	private void incrAnchorScore(Long anchorId) {
		String livingKey = RedisKey.VideoKeys.LIVING_VIDEOS_;
		incrScore(livingKey, anchorId);
		livingKey = RedisKey.VideoGrayKeys.LIVING_VIDEOS_;//灰度代码
		incrScore(livingKey, anchorId);
	}

	private void incrScore(String livingKey, Long anchorId) {
		Double score = 0.0D;
    	if (redisAdapter.existsKey(livingKey) 
    			&& (score = redisAdapter.zsetScore(livingKey, anchorId.toString())) != null && score > 0) {
    		if (score >= Constants.LIVING_VIDEO_PAY_SCORE) {
				redisAdapter.zsetIncrBy(livingKey, 0.001D, anchorId.toString());
    		} else {
				redisAdapter.zsetIncrBy(livingKey, 1.0D, anchorId.toString());
    		}
		}
	}
	
	
	private String[] getRtmpURLs(String streamName) {
		return KSCloudFacade.getRtmpURLs(streamName);
	}
	
	private LiveVideoInfoVo corvertVideoDomain2Vo(LiveVideoInfo info) {
		LiveVideoInfoVo vo = new LiveVideoInfoVo();
		vo.setAnchorId(info.getAnchorId());
		vo.setVideoId(info.getVideoId());
		vo.setRoomNum(info.getRoomNum());
		vo.setChatroomId(info.getChatroomId());
		String[] rtmps = getRtmpURLs(info.getStreamName());
		vo.setStandURL(rtmps[0]);
		vo.setHeighURL(rtmps[1]);
		vo.setFullHeighURL(rtmps[2]);
		vo.setAnchorName(info.getUserName());
		vo.setViewers(info.getViewers() == null ? 1 : info.getViewers().longValue() + 1L);
		return vo;
	}
	
	/**
	 * LiveVideoInfoVo 赋值loginId、用户类型、 虚拟货币、融云token、用户昵称，及设置是否被禁言
	 * @param vo
	 * @param userId
	 */
	private void setLoginId(LiveVideoInfoVo vo, long userId, long anchorId) {
		Map<String, String> userInfo = redisAdapter.hashMGet(RedisKey.USER_INFO_ + userId, 
				"loginId", "acctType", "userName","virtualCurrency", "rongToken");
        if (userInfo == null || userInfo.isEmpty() 
        		|| StringUtils.isEmpty(userInfo.get("virtualCurrency"))) {//获得虚拟货币信息
            UserInfo user = UserInfoHelper.getAndCacheUserInfo(redisAdapter, 
            		userInfoMapper, userId);
            if (user != null) {
                userInfo = user.putFieldValueToStringMap();
            }
        }
    	vo.setLoginId(userInfo.get("loginId"));
    	String acctType = userInfo.get("acctType");
    	vo.setAcctType(StringUtils.isEmpty(acctType) ? 0 : Integer.parseInt(acctType));
    	
    	vo.setUserType(vo.getAcctType());//兼容2.0.0之前的用户资料接口
    	if(vo.getUserType() == 0){
            //普通用户是否为房管
              if (LiveAdminHelper.isAdmin(redisAdapter, adminMapper, anchorId, userId)) {//普通用户是否为房管
                  vo.setUserType(2);
              }
          }
    	
    	vo.setVirtualCurrency(Integer.parseInt(userInfo.get("virtualCurrency")));
    	vo.setOrigin(0);
    	vo.setRongToken(userInfo.get("rongToken"));
    	vo.setUserName(userInfo.get("userName"));
    	
    	String key = RedisKey.BLACKLIST_ + anchorId + Constants.UNDERLINE + userId;
		vo.setGagOfSuper(Constants._0);
		vo.setIsblack(Constants._0);
		if (redisAdapter.existsKey(key)) {//禁言时间
			vo.setIsblack(Constants._1);
			vo.setGagTime(redisAdapter.ttl(key));
			vo.setGagOfSuper(Constants._2);
		}else {//是否是超管禁言
	    	if (BlackListHelper.isOnBlackList(blackListMapper, redisAdapter, userId)) {
				vo.setIsblack(Constants._1);
				vo.setGagOfSuper(Constants._1);
				vo.setGagTime(Integer.MAX_VALUE);
	    	}
		}
    	if (userInfo.get("origin") != null) {
    		vo.setOrigin(Integer.parseInt(userInfo.get("origin")));
    	}
	}

	public LiveVideoInfoVo liveAdvert(LiveVideoInfoVo vo,String cnid,String version) {
		Map<String, String> advMap = null;
		try {
			List<String> advids = advertMapper.findadvertByCnidVersion(3, cnid, version);
			if(advids!=null && advids.size() > 0) {
				int i = new Random().nextInt(advids.size()) % (advids.size() - 1 + 1) + 1;
				String advId = advids.get(i - 1);
				advMap = getAdvertRedis(vo, advId);
				if (advMap == null) {
					advAddRedis();
					advMap = getAdvertRedis(vo, advId);
				}
			}
			if(advMap == null) {
				vo.setLiveImg("");
				vo.setJumpUrl("");
				vo.setAdvId("");
				vo.setAdvName("");
			}else {
				vo.setLiveImg((String )advMap.get("advUrl"));
				vo.setJumpUrl((String) advMap.get("url"));
				vo.setAdvId((String) advMap.get("advId"));
				vo.setAdvName((String)advMap.get("advName"));
			}
		}catch (Exception e) {
			LOGGER.error("直播广告错误",e);
		}
		return vo;
	}

	public Map<String, String>  getAdvertRedis (LiveVideoInfoVo vo, String advId) {
		if(redisAdapter.existsKey(RedisKey.ADVERT_LIVE_IMAGE + advId)){
			Map<String, String> map = redisAdapter.hashMGet(RedisKey.ADVERT_LIVE_IMAGE + advId, "advId", "advUrl", "url", "advName");
			return map;
		}
		return null;
	}
	public void advAddRedis () {
		List<Map<String, Object>> liveAdvert = advertMapper.findLiveAdvert();
		if(liveAdvert!=null && liveAdvert.size() > 0) {
			for(Map<String, Object> map :liveAdvert) {
				String key = RedisKey.ADVERT_LIVE_IMAGE + map.get("advId");
				redisAdapter.hashMSet(key, map);
				redisAdapter.expireKey(RedisKey.ADVERT_LIVE_IMAGE + map.get("advId"),RedisExpireTime.EXPIRE_MIN_20);
			}
		}
	}
}
