package com.chineseall.iwanvi.wwlive.pc.follow.service.impl;

import com.chineseall.iwanvi.wwlive.common.cache.RedisClientAdapter;
import com.chineseall.iwanvi.wwlive.common.cache.RedisKey;
//import com.chineseall.iwanvi.wwlive.common.enums.DictInfoEnum;
import com.chineseall.iwanvi.wwlive.dao.wwlive.BaseDictInfoMapper;
import com.chineseall.iwanvi.wwlive.dao.wwlive.ContributionListMapper;
import com.chineseall.iwanvi.wwlive.dao.wwlive.LiveAdminMapper;
import com.chineseall.iwanvi.wwlive.dao.wwlive.RoleInfoMapper;
import com.chineseall.iwanvi.wwlive.dao.wwlive.UserInfoMapper;
import com.chineseall.iwanvi.wwlive.domain.wwlive.UserInfo;
import com.chineseall.iwanvi.wwlive.pc.common.Page;
import com.chineseall.iwanvi.wwlive.pc.common.helper.LiveAdminHelper;
import com.chineseall.iwanvi.wwlive.pc.common.helper.RoleNobleHelper;
import com.chineseall.iwanvi.wwlive.pc.common.helper.UserInfoHelper;
import com.chineseall.iwanvi.wwlive.pc.common.helper.UserRankHelper;
import com.chineseall.iwanvi.wwlive.pc.follow.service.AnchorService;
import com.service.FollowAnchorService;
import com.service.impl.FollowAnchorServiceImpl;
import org.apache.commons.collections.MapUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service("followAnchorService")
public class AnchorServiceImpl implements AnchorService{
    
    private static FollowAnchorService followService = new FollowAnchorServiceImpl();

    @Autowired
    private RedisClientAdapter redisAdapter;
    
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
    public Page getFollowPage(Long anchorId, Page page, Long timestamp) {
        List<Map<String, Object>> followPage = new ArrayList<>();
        try {
            List<Map<String, Object>> followers = followService.queryFans(anchorId, page.getPageIndex(), timestamp);
            for(Map<String, Object> follow:followers){
                Long userId = MapUtils.getLong(follow, "uid");
                UserInfo user = 
                        UserInfoHelper.getAndCacheUserInfo(redisAdapter, userInfoMapper, userId);

                if(user == null){
                    user = new UserInfo();
                    user.setUserName("UNKNOWN");
                    user.setSex(2);
                    user.setAcctType(0);
                }

                Map<String, Object> follower = new HashMap<>();
                follower.put("userId", user.getUserId());
                follower.put("headImg", user.getHeadImg());
                follower.put("userName", user.getUserName());
                follower.put("sex", user.getSex());
                follower.put("timestamp", MapUtils.getLong(follow, "timestamp"));
                
                // 获取贡献值
                Map<String, String> userInfo;
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
//    				DictInfoEnum dictInfo = DictInfoEnum.getDictInfoEnum(level);
//    				String nobleImg = "";
//    				if (dictInfo != null) {
//    					nobleImg = RoleNobleHelper.getNobleImg(dictInfo, baseDictInfoMapper, redisAdapter);
//    				}
					follower.put("nobleCode", level);
//					follower.put("nobleImg", nobleImg);
                }
                
                followPage.add(follower);                
            }
            page.setData(followPage);

            int fansNumber = followService.getFansNumber(anchorId);
            page.setTotal(fansNumber);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return page;
    }
}