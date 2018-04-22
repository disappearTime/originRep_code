package com.chineseall.iwanvi.wwlive.web.follow.service.impl;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.MapUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.chineseall.iwanvi.wwlive.common.cache.RedisClientAdapter;
import com.chineseall.iwanvi.wwlive.common.cache.RedisKey;
import com.chineseall.iwanvi.wwlive.common.enums.DictInfoEnum;
import com.chineseall.iwanvi.wwlive.dao.wwlive.AnchorMapper;
import com.chineseall.iwanvi.wwlive.dao.wwlive.BaseDictInfoMapper;
import com.chineseall.iwanvi.wwlive.dao.wwlive.ContributionListMapper;
import com.chineseall.iwanvi.wwlive.dao.wwlive.LiveAdminMapper;
import com.chineseall.iwanvi.wwlive.dao.wwlive.RoleInfoMapper;
import com.chineseall.iwanvi.wwlive.dao.wwlive.UserInfoMapper;
import com.chineseall.iwanvi.wwlive.domain.wwlive.Anchor;
import com.chineseall.iwanvi.wwlive.domain.wwlive.UserInfo;
import com.chineseall.iwanvi.wwlive.web.common.helper.AnchorInfoHelper;
import com.chineseall.iwanvi.wwlive.web.common.helper.LiveAdminHelper;
import com.chineseall.iwanvi.wwlive.web.common.helper.RoleNobleHelper;
import com.chineseall.iwanvi.wwlive.web.common.helper.UserInfoHelper;
import com.chineseall.iwanvi.wwlive.web.common.helper.UserRankHelper;
import com.chineseall.iwanvi.wwlive.web.common.util.DateTools;
import com.chineseall.iwanvi.wwlive.web.follow.service.AnchorService;
import com.service.FollowAnchorService;
import com.service.impl.FollowAnchorServiceImpl;
import com.zw.zcf.util.StringUtils;

@Service("followAnchorService")
public class AnchorServiceImpl implements AnchorService{
    
    private FollowAnchorService followService = new FollowAnchorServiceImpl();

    @Autowired
    private RedisClientAdapter redisAdapter;
    
    @Autowired
    private AnchorMapper anchorMapper;
    
    @Autowired
    private UserInfoMapper userInfoMapper;
    
    @Autowired
    private ContributionListMapper contribMapper;
    
    @Autowired
    private LiveAdminMapper adminMapper;

    @Autowired
    private RoleInfoMapper roleInfoMapper;

	@Autowired
	private BaseDictInfoMapper baseDictInfoMapper;
    
    @Override
    public int getFollowerCnt(Long anchorId) {
        try {
            return followService.getFansNumber(anchorId);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    @Override
    public Map<String, Object> getInfo(Long anchorId) {
        String anchorKey = RedisKey.ANCHOR_INFO_ + anchorId;

        Map<String, Object> data = new HashMap<>();
        if (redisAdapter.existsKey(anchorKey)) {
            //先从redis中获取主播信息
            Map<String, String> anchorInfo = redisAdapter
                    .hashMGet(anchorKey, "anchorId", "roomNum", "birthday", "zodiac", "notice");//头像 昵称 性别 年龄 星座 公告
            try {
                anchorInfo.put("age", DateTools.getAgeByDate(anchorInfo.get("birthday")) + ""); //年龄实时计算
            } catch (ParseException e) {
                e.printStackTrace();
                //异常时年龄为0
                anchorInfo.put("age", "0");
            }
            data.putAll(anchorInfo);
        } else {
            Anchor anchor = AnchorInfoHelper.getAndCacheAnchorInfo(redisAdapter, anchorMapper, anchorId);
            //id
            data.put("anchorId", anchor.getAnchorId());
            //头像
            data.put("roomNum", anchor.getHeadImg());
            //年龄
            data.put("age", DateTools.getAgeByDate(anchor.getBirthday()));
            //星座
            data.put("zodiac", anchor.getZodiac());
            //公告
            data.put("notice", anchor.getNotice());
        }
        
        if(StringUtils.isBlank((String) data.get("notice"))){
            data.put("notice", "主播很懒，什么都没有留下");
        }
        return data;
    }
    
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
        return data;
    }

    @Override
    public List<Map<String, Object>> getFollowPage(Long anchorId, Integer pageNo, Long timestamp) {
        List<Map<String, Object>> followPage = new ArrayList<>();
        try {
            List<Map<String, Object>> followers = followService.queryFans(anchorId, pageNo, timestamp);
            for(Map<String, Object> follow:followers){
                Long userId = MapUtils.getLong(follow, "uid");
                UserInfo user = 
                        UserInfoHelper.getAndCacheUserInfo(redisAdapter, userInfoMapper, userId);
                
                if(user == null){
                    continue;
                }
                
                Map<String, Object> follower = new HashMap<>();
                follower.put("userId", user.getUserId());
                follower.put("loginId", user.getLoginId());
                follower.put("headImg", user.getHeadImg());
                follower.put("userName", user.getUserName());
                follower.put("sex", user.getSex());
                follower.put("timestamp", MapUtils.getLong(follow, "timestamp"));
                
                // 获取贡献值
                Map<String, String> userInfo = new HashMap<>();
                String rankKey = RedisKey.USER_RANK_CONTRIB_ + userId;
                if (redisAdapter.existsKey(rankKey)) {
                     userInfo = UserRankHelper.getUserStrRankCache(redisAdapter, contribMapper, userId);
                } else {
                    userInfo = UserRankHelper.getAndCacheStrUserRank(redisAdapter, contribMapper, userId);
                }
                follower.put("contrib", userInfo.get("contrib"));
                
                
                // 设置acctType 0-普通用户, 1-超管, 2-房管
                int acctType = user.getAcctType();
                follower.put("acctType", acctType);
                if(acctType == 0){
                  //普通用户是否为房管
                    if (LiveAdminHelper.isAdmin(redisAdapter, adminMapper, anchorId, userId)) {//普通用户是否为房管
                        follower.put("acctType", "2");
                    }
                }
                //增加贵族等级及贵族图片
                Integer level = RoleNobleHelper.userRoleNobleLevel(redisAdapter, roleInfoMapper, userId);
                if(level != null && level.intValue() > 0) {
    				DictInfoEnum dictInfo = DictInfoEnum.getDictInfoEnum(level);
    				String nobleImg = "";
    				if (dictInfo != null) {
    					nobleImg = RoleNobleHelper.getNobleImg(dictInfo, baseDictInfoMapper, redisAdapter);
    				}
					follower.put("nobleCode", level);
					follower.put("nobleImg", nobleImg);
                }
                
                followPage.add(follower);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return followPage;
    }
    
}
