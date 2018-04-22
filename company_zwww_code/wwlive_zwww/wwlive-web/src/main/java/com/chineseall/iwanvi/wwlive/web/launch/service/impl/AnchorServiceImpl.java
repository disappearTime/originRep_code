package com.chineseall.iwanvi.wwlive.web.launch.service.impl;

import java.math.BigDecimal;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.chineseall.iwanvi.wwlive.dao.wwlive.*;
import org.apache.commons.collections.map.HashedMap;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.chineseall.iwanvi.wwlive.common.cache.RedisClientAdapter;
import com.chineseall.iwanvi.wwlive.common.cache.RedisExpireTime;
import com.chineseall.iwanvi.wwlive.common.cache.RedisKey;
import com.chineseall.iwanvi.wwlive.common.constants.Constants;
import com.chineseall.iwanvi.wwlive.domain.wwlive.Anchor;
import com.chineseall.iwanvi.wwlive.domain.wwlive.UserInfo;
import com.chineseall.iwanvi.wwlive.web.common.helper.AnchorInfoHelper;
import com.chineseall.iwanvi.wwlive.web.common.helper.FolloServiceSingleton;
import com.chineseall.iwanvi.wwlive.web.common.helper.LiveAdminHelper;
import com.chineseall.iwanvi.wwlive.web.common.helper.RoleNobleHelper;
import com.chineseall.iwanvi.wwlive.web.common.helper.UserInfoHelper;
import com.chineseall.iwanvi.wwlive.web.common.util.DateTools;
import com.chineseall.iwanvi.wwlive.web.event.service.LevelEventService;
import com.chineseall.iwanvi.wwlive.web.event.service.MedalHonorService;
import com.chineseall.iwanvi.wwlive.web.launch.service.AnchorService;
import com.service.FollowAnchorService;
import com.service.impl.FollowAnchorServiceImpl;

@Service("launchAnchorService")
public class AnchorServiceImpl implements AnchorService{

	static final Logger LOGGER = Logger.getLogger(AnchorServiceImpl.class);
    
    @Autowired
    private ChatLetterMapper letterMapper;

    @Autowired
    private AnchorMapper anchorMapper;
    
    @Autowired
    private LiveVideoInfoMapper liveVideoInfoMapper;

    @Autowired
    private RedisClientAdapter redisAdapter;
    
    @Autowired
    private MedalHonorService medalHonorService;
    
    @Autowired
    private LevelEventService levelEventService;

    @Autowired
    private RoleInfoMapper roleInfoMapper;

    @Autowired
    private OrderInfoMapper orderInfoMapper;

    @Autowired
    private UserInfoMapper userInfoMapper;

    @Autowired
    private LiveAdminMapper liveAdminMapper;

    @Autowired
    private LiveAdminMapper adminMapper;

	@Autowired
    private ContributionListMapper contribMapper;

    @Autowired
    private WinningRecordsMapper winningRecordsMapper;
	
    private static FollowAnchorService followService = new FollowAnchorServiceImpl();
    
    @Override
    public Map<String, Object> getMsgList(Integer pageNo, Integer pageSize, Long anchorId) {
        int startRow = (pageNo - 1) * pageSize;
        List<Map<String, Object>> msgList = letterMapper.getMsgListByPage(startRow, pageSize, anchorId);
        
        // 添加土豪勋章信息
        String richestUserId = redisAdapter.strGet(RedisKey.RICHEST_MEDAL_OWNER);
        for(Map<String, Object> msg:msgList){
            String uid = (msg.get("userId")).toString();
            msg.put("isRichest", uid.equals(richestUserId) ? 1 : 0);
            //增加贵族等级及贵族图片
            Integer level = RoleNobleHelper.userRoleNobleLevel(redisAdapter, roleInfoMapper, Long.valueOf(uid));
            if(level != null && level.intValue() > 0) {
				msg.put("nobleCode", level);
            } else {
				msg.put("nobleCode", 0);
            }
        }
        
        Map<String, Object> data = new HashMap<>();
        data.put("msgList", msgList);
        return data;
    }

    public Map<String, Object> getAnchorInfo(Long anchorId) {
    	Map<String, Object> anchorInfo = anchorMapper.getAnchorHomeInfo(anchorId);
    	Date birthday = (Date)anchorInfo.get("birthday");
    	if (birthday != null) {
    		// 年龄实时计算
			anchorInfo.put("age", DateTools.getAgeByDate(birthday));
    		
    	}
    	Object totalAmt = anchorInfo.get("totalAmt");
    	if (totalAmt != null && totalAmt instanceof Double) {
    		BigDecimal total = new BigDecimal((Double)totalAmt).divide(new BigDecimal(100)).setScale(2, BigDecimal.ROUND_HALF_UP);
    		anchorInfo.put("totalAmt", total);
    	}
    	
    	// 获取粉丝数
    	int followerCnt = 0;
    	try {
            followerCnt = followService.getFansNumber(anchorId);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    	anchorInfo.put("followerCnt", followerCnt);
    	
        // 关卡活动需求: 获取主播的勋章信息
    	anchorInfo.put("medals", medalHonorService.getGoddessMedal(anchorId));    	
    	
        return anchorInfo;
    }

    public List<Map<String, Object>> getAnchorIncomeList(Integer pageNo, Integer pageSize, Long anchorId) {
    	int startRow = (pageNo - 1) * pageSize;
    	List<Map<String, Object>> incomeList = liveVideoInfoMapper.findIncomeByAnchorId(anchorId, startRow, pageSize);
        return incomeList;
    }

    @Override
    public Map<String, Object> getAnchorDetail(Long anchorId) {
        Map<String, Object> data = new HashMap<>();
        Map<String, Object> anchorInfo = new HashMap<String, Object>();
    	if (anchorId <= 0) {
    		data.put("anchorInfo", anchorInfo);
    		return data;
    	}
        String anchorKey = RedisKey.ANCHOR_INFO_ + anchorId;
        if(redisAdapter.existsKey(anchorKey)) {//先从redis中获取主播信息
        	 Map<String, String> anchorTmp = redisAdapter.hashMGet(anchorKey, 
        			 "headImg", "userName", "sex", "birthday", "zodiac", "notice", "acctType");
            if(anchorTmp == null 
                    || anchorTmp.isEmpty() 
                    || anchorTmp.get("userName") == null){
            	anchorInfo = AnchorInfoHelper.getAndCacheAnchorMapInfo(redisAdapter, anchorMapper, anchorId, 
            			"headImg", "userName", "sex", "birthday", "zodiac", "notice", "acctType");
            } else{
                anchorInfo.putAll(anchorTmp);
            }
        } else {//从数据库中查询并保存到redis中
        	anchorInfo = AnchorInfoHelper.getAndCacheAnchorMapInfo(redisAdapter, anchorMapper, anchorId, 
        			"headImg", "userName", "sex", "birthday", "zodiac", "notice", "acctType");
        }
        anchorInfo.put("contrib", contribMapper.getContribByAnchorId(anchorId, 1));//主播获得的贡献值(借用的)
        //年龄实时计算
        try {
            anchorInfo.put("age", DateTools.getAgeByDate(anchorInfo.get("birthday").toString()) + "");
            int followerCnt = FolloServiceSingleton.getFollowAnchorServiceInstance().getFansNumber(anchorId);// 获取粉丝数
            anchorInfo.put("followerCnt", followerCnt);//粉丝数
            anchorInfo.put("videoCnt", liveVideoInfoMapper.countVideos(anchorId));//收入
        } catch (Exception e) {
            LOGGER.error("计算年龄或获得粉丝数异常", e);
            anchorInfo.put("age","0");
        }
        
        //如果公告为空则设置默认内容
        String notice = (String) anchorInfo.get("notice");
        if(StringUtils.isEmpty(notice)){
            anchorInfo.put("notice", "主播很懒, 什么都没有留下");
        }
        String key = RedisKey.ANCHOR_GIFT_SCORE_ + anchorId;
        String strScore = redisAdapter.strGet(key);
        long currentLv = 0L;// 当前等级
        long currentScore = 0L;// 当前分数
        if (org.apache.commons.lang.StringUtils.isNotBlank(strScore)) {
            currentScore = (Long.valueOf(strScore)).longValue();
            currentLv = (currentScore / Constants.PER_LEVEL_COIN_SCORE);
        }
        anchorInfo.put("currentLv", currentLv);
        double totalDiamonds = getTotalDiamonds(currentScore - (currentLv * Constants.PER_LEVEL_COIN_SCORE));
        anchorInfo.put("currentScore", totalDiamonds);
        anchorInfo.put("levelDiamonds",new Long(20));

        Object acctType = anchorInfo.get("acctType");
        setGoddessInfo(anchorInfo, acctType, anchorId);
        
        data.put("anchorInfo", anchorInfo);
        return data;
    }
    
    private int getAnchorIncomeVideoCnt(long anchorId) {
        String key = RedisKey.ANCHOR_INCOME_VIEDOCNT_ + anchorId;
        Map<String, String> incomeAndVideoCnt = redisAdapter.hashGetAll(key);
        Map<String, String> income = incomeAndVideoCnt;
        if (income == null || income.isEmpty()) {// 放入redis中
            Map<String, Object> in = anchorMapper.getAnchorIncomeVideoCnt(anchorId);

            if (in == null || in.isEmpty()) {// 排空检查
                return 0;
            } else {
                redisAdapter.hashMSet(key, in);
                redisAdapter.expireKey(key, RedisExpireTime.EXPIRE_HOUR_12);// 12 小时
                return ((Integer) in.get("videoCnt"));
            }
        } else {
            return Integer.valueOf(incomeAndVideoCnt.get("videoCnt"));
        }
    }
    
    private void setGoddessInfo(Map<String, Object> anchorInfo, Object acctType, Long anchorId) {
    	if (!redisAdapter.existsKey(RedisKey.GODDESS_EVNENT_SHOW)) {//不存在就展示
            anchorInfo.put("diamonds", levelEventService.getCurDiamonds(anchorId));
            anchorInfo.put("levels", levelEventService.getCurLevels(anchorId));
            anchorInfo.put("medals", medalHonorService.getGoddessMedal(anchorId));
            anchorInfo.put("diamondsPerLv", Constants.PER_LEVEL_SCORE);// 每关所需钻石数
            return;
    	}
        boolean isTester = false;
        if (acctType instanceof String) {
        	isTester = ("1".equals((String) acctType));
        } else if (acctType instanceof Integer) {
        	isTester = ("1".equals(((Integer) acctType) + ""));
        }
        if (isTester) {
            anchorInfo.put("diamonds", levelEventService.getCurDiamonds(anchorId));
            anchorInfo.put("levels", levelEventService.getCurLevels(anchorId));
            anchorInfo.put("medals", medalHonorService.getGoddessMedal(anchorId));
        } else {
            // 添加主播关卡信息
            anchorInfo.put("diamonds", new Integer(-1));
            anchorInfo.put("levels", new Integer(-1));
            anchorInfo.put("medals", new ArrayList<String>());
        }
        anchorInfo.put("diamondsPerLv", Constants.PER_LEVEL_SCORE);// 每关所需钻石数
    }
    
    @Override
    public Map<String, Object> getAnchorInfoForModify(Long anchorId) throws ParseException {

        Map<String, Object> anchor = new HashMap<String, Object>();
        String anchorKey = RedisKey.ANCHOR_INFO_ + anchorId;
    	if (redisAdapter.existsKey(anchorKey)) {
    		Map<String, String> cache = 
    				redisAdapter.hashMGet(anchorKey, "headImg", "userName", "sex", "birthday", "zodiac", "notice");
    		anchor.putAll(cache);
    	} else {
    		anchor = AnchorInfoHelper.getAndCacheAnchorMapInfo(redisAdapter, anchorMapper, anchorId, "headImg", "userName", "sex", "birthday", "zodiac", "notice");
    	}
    	 //年龄实时计算
        try {
        	anchor.put("age", DateTools.getAgeByDate(anchor.get("birthday").toString()));
        } catch (ParseException e) {
            LOGGER.error("计算年龄异常", e);
            anchor.put("age","0");
        }
        return anchor;
    }
    
    @Override
    public Integer modifyAnchorInfo(Anchor anchor) {
    	Integer count = anchorMapper.modifyAnchorInfo(anchor);
    	 if (count > 0) {
         	modifyRedisAchorInfo(anchor); //将修改后的信息重新写入redis中
         }
    	 return count;
    }

    @Override
    public Anchor getAnchorInfo4UpdateIos(Long anchorId) throws ParseException {
    	String key = RedisKey.ANCHOR_INFO_ + anchorId;
		Anchor tmp = null;
    	if (redisAdapter.existsKey(key)) {
			Map<String, String> tmpMap = redisAdapter.hashMGet(key, 
					"anchorId","rongToken", "acctStatus", "userName", "headImg", "roomNum");
			if (tmpMap == null || tmpMap.isEmpty()) {
				redisAdapter.delKeys(key);
			} else {
				tmp = new Anchor();
				tmp.doStringMapToValue(tmpMap);
			}
    	} else {
    		tmp = AnchorInfoHelper.getAndCacheAnchorInfo(redisAdapter, anchorMapper, anchorId);
    	}
    	
    	return tmp;
    }
    
    private void modifyRedisAchorInfo(Anchor anchor) {
        String anchorKey = RedisKey.ANCHOR_INFO_ + anchor.getAnchorId();
        if (redisAdapter.existsKey(anchorKey)) {
            redisAdapter.hashMSet(anchorKey, anchor.putFieldValueToStringMap());
    		redisAdapter.expireKey(anchorKey, RedisExpireTime.EXPIRE_DAY_30);
        }
    }

    /**
     * IOS端 直播收入
     */
    public Map<String, Object> getAnchorIncome(long anchorId, int pageNo, int pageSize) {
        Map<String,Object> anchor = new HashedMap();
        int startRow = (pageNo - 1) * pageSize;
        List<Map<String, Object>> result = new ArrayList<>();
        //获取主播直播列表
        List<Map<String, Object>> livedList = liveVideoInfoMapper.findLivedListByAnchorId(anchorId, startRow, pageSize);
        if(livedList != null && livedList.size() > 0) {
            for(Map<String, Object> map : livedList) {
                String videoName = (String) map.get("videoName");
                String startTime = (String) map.get("startTime");
                if(startTime != null) {
                    String reg = "[\u4e00-\u9fa5]";
                    Pattern pat = Pattern.compile(reg);
                    Matcher mat=pat.matcher(startTime);
                    startTime = mat.replaceAll("-");
                    startTime = startTime.substring(0, 10);
                }
                long videoId = Long.parseLong(map.get("videoId") + "");
                List<Map<String, Object>> anchorIncome = orderInfoMapper.findAnchorByVideoId(anchorId, videoId);
                Map<String, Object> map1 = anchorDataIncome(anchorIncome, videoName,videoId,startTime);
                result.add(map1);
            }
        }
        anchor.put("anchorIncome",result);
        return anchor;
    }

    public Map<String, Object> anchorDataIncome (List<Map<String, Object>> anchorIncome, String name,long videoId,String startTime) {
        Map<String, Object> data = new HashedMap();
        if(anchorIncome != null) {
            int count = 0;
            for(Map<String, Object> map : anchorIncome) {
                int orderType = (int) map.get("orderType");//礼物类型  0.普通礼物 1.贵族礼物 4.弹幕 5.购买贵族
                if(orderType == 0) {
                    int income = Integer.parseInt(map.get("income")+"");//收入
                    count += income;
                    data.put("goodsIncome",income);
                }
                if(orderType == 1) {
                    int income = Integer.parseInt(map.get("income")+"");
                    count += income;
                    data.put("nobleIncome",income);
                }
                if(orderType == 4) {
                    int income = Integer.parseInt(map.get("income")+"");
                    count += income;
                    data.put("barrageIncome",income);
                }
                if(orderType == 5) {
                    int income = Integer.parseInt(map.get("income")+"");
                    count += income;
                    data.put("levelIncome",income);
                }
                if(orderType == 7) {
                    int income = Integer.parseInt(map.get("income")+"");
                    count += income;
                    data.put("backpackIncome",income);
                }
            }
            data.put("createTime",startTime);
            data.put("videoName",name);
            data.put("videoId",videoId);
            data.put("count",count);
            return data;
        }
        return null;
    }

    /**
     *
     */
    public Map<String, Object> findIncomeDetailsBy(int pageNo, int pageSize, long anchorId, long videoId) {
        int startRow = (pageNo - 1) * pageSize;
        Map<String, Object> result = new HashMap<String, Object>();
        List<Map<String, Object>> incomeList = orderInfoMapper.findIncomeDetails(anchorId,videoId, startRow, pageSize);
        if(incomeList != null && incomeList.size() > 0) {
            for(Map<String, Object> map : incomeList) {
                long userId = Long.parseLong(map.get("userId").toString());
                UserInfo userInfo = UserInfoHelper.getUserInfoFromCache(redisAdapter, userId, "userName", "headImg","sex","acctType");
                if(userInfo == null) {
                    userInfo = UserInfoHelper.getAndCacheUserInfo(redisAdapter, userInfoMapper, userId);
                }
                if(userInfo == null) {
                    map.put("headImg", "");
                    map.put("userName","");
                }else {
                    map.put("headImg", userInfo.getHeadImg());
                    map.put("userName",userInfo.getUserName());
                    map.put("sex",userInfo.getSex());
                }

                boolean isAdmin =
                        LiveAdminHelper.isAdmin(redisAdapter, liveAdminMapper, anchorId, Long.valueOf(userId));
                if(isAdmin){
                    map.put("isAdmin", "1");
                } else{
                    map.put("isAdmin", "0");
                }
                int acctType = userInfo.getAcctType();
                if(acctType == 0){
                    //普通用户是否为房管
                    if (LiveAdminHelper.isAdmin(redisAdapter, adminMapper, anchorId, userId)) {//普通用户是否为房管
                        map.put("acctType", "2");
                    }
                }

                Integer level = RoleNobleHelper.userRoleNobleLevel(redisAdapter, roleInfoMapper, userId);
                if(level != null && level.intValue() > 0) {
                    map.put("level", level);
                } else {
                    map.put("level", 0);
                }
            }
            result.put("incomeList",incomeList);
            return result;
        }
        result.put("incomeList",new ArrayList<>());
        return result;
    }

    @Override
    public Map<String, Object> getGameInfo(Long anchorId) {
        String cardFaceUrl = "";
        Long contrib = 0L;
        Map<String, Object> gameInfo = new HashMap<>();
        try {
            String anchorKey = RedisKey.ANCHOR_INFO_ + anchorId;
            if (redisAdapter.existsKey(anchorKey)) {
                cardFaceUrl = redisAdapter.hashGet(anchorKey, "cardFace");
            } else {
                Anchor anchor = AnchorInfoHelper.getAndCacheAnchorInfo(redisAdapter, anchorMapper, anchorId, "cardFace");
                cardFaceUrl = anchor.getCardFace();
            }

            String today = DateTools.formatDate(new Date(), "yyyyMMdd");
            String gameContribKey = RedisKey.gameKey.ANCHOR_GAME_CONTRIB_ + anchorId + "_" + today;
            if (redisAdapter.existsKey(gameContribKey)) {
                contrib = Long.valueOf(redisAdapter.strGet(gameContribKey));
            } else {
                contrib = winningRecordsMapper.getTodayContrib(anchorId, today);
                redisAdapter.strSetByNormal(gameContribKey, contrib + "");
                redisAdapter.expireKey(gameContribKey, RedisExpireTime.EXPIRE_HOUR_12);
            }
        } catch (ParseException e) {
            LOGGER.error("获取主播牌面异常", e);
        }

        gameInfo.put("cardFaceUrl", cardFaceUrl == null ? "" : cardFaceUrl);
        gameInfo.put("gameContrib", contrib);

        return gameInfo;
    }

    private double getTotalDiamonds(long score) {
        return new BigDecimal(score).divide(new BigDecimal(100))
                .setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
    }
}
