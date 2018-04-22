package com.chineseall.iwanvi.wwlive.pc.video.service.impl;

import com.chineseall.iwanvi.wwlive.common.cache.RedisClient;
import com.chineseall.iwanvi.wwlive.common.cache.RedisClientAdapter;
import com.chineseall.iwanvi.wwlive.common.cache.RedisExpireTime;
import com.chineseall.iwanvi.wwlive.common.cache.RedisKey;
import com.chineseall.iwanvi.wwlive.common.constants.Constants;
import com.chineseall.iwanvi.wwlive.common.tools.StrMD5;
import com.chineseall.iwanvi.wwlive.dao.wwlive.*;
import com.chineseall.iwanvi.wwlive.domain.wwlive.Anchor;
import com.chineseall.iwanvi.wwlive.domain.wwlive.LiveVideoInfo;
import com.chineseall.iwanvi.wwlive.domain.wwlive.UserInfo;
import com.chineseall.iwanvi.wwlive.pc.common.Page;
import com.chineseall.iwanvi.wwlive.pc.common.helper.LiveAdminHelper;
import com.chineseall.iwanvi.wwlive.pc.common.helper.RoleNobleHelper;
import com.chineseall.iwanvi.wwlive.pc.common.helper.UserInfoHelper;
import com.chineseall.iwanvi.wwlive.pc.common.loginContext.LoginUser;
import com.chineseall.iwanvi.wwlive.pc.event.service.MedalHonorService;
import com.chineseall.iwanvi.wwlive.pc.login.service.LoginService;
import com.chineseall.iwanvi.wwlive.pc.video.common.PcConstants;
import com.chineseall.iwanvi.wwlive.pc.video.service.AnchorService;
import com.chineseall.iwanvi.wwlive.pc.video.util.DateUtil;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.text.ParseException;
import java.util.*;

@Service
public class AnchorServiceImpl implements AnchorService{
    
    private Logger logger = Logger.getLogger(this.getClass());
    
    @Autowired
    private AnchorMapper anchorMapper;
    
    @Autowired
    private ContributionListMapper contribMapper;
    
    @Autowired
    private OrderInfoMapper orderMapper;
    
    @Autowired
    private RedisClientAdapter redisAdapter;
    
    @Autowired
    private RoleInfoMapper roleInfoMapper;
    
    @Autowired
    private RedisClient redisClient;
    
    @Autowired
    private LoginService loginService;
    
    @Autowired
    private LiveAdminMapper adminMapper;
    
    @Autowired
    private MedalHonorService medalHonorService;

    @Autowired
    private UserInfoMapper userInfoMapper;

    @Autowired
    private LiveVideoInfoMapper liveVideoInfoMapper;
    
    @Override
    public Map<String,Object> modifyInfo(Anchor anchor, String loginCookie, String newPasswd) {
        Map<String, Object> result = new HashMap<>();
        try {
            //修改密码
            if(!("".equals(newPasswd)) && newPasswd != null){
                anchor.setPasswd(StrMD5.getInstance().encrypt(newPasswd, "iwanvi_salt"));
            }

            int count = anchorMapper.modifyAnchorInfo(anchor);//更改资料
            if (count > 0) {
            	modifyRedisAchorInfo(anchor); //将修改后的信息重新写入redis中
            	
                modifyCokieAchorInfo(anchor, loginCookie); //修改loginuser信息
            }
            result.put("result", count);
        } catch (Exception e) {
            logger.error("PC端主播修改个人资料接口, anchorId = " + anchor.getAnchorId() + "的主播修改资料时发生异常.", e);
            result.put("result", PcConstants.FAIL);
        }  
        return result;
    }

    private void modifyRedisAchorInfo(Anchor anchor) {
        String anchorKey = RedisKey.ANCHOR_INFO_ + anchor.getAnchorId();
        if (redisAdapter.existsKey(anchorKey)) {
            redisAdapter.hashMSet(anchorKey, anchor.putFieldValueToStringMap());
    		redisAdapter.expireKey(anchorKey, RedisExpireTime.EXPIRE_DAY_30);
        }
    }
    
    private void modifyCokieAchorInfo(Anchor anchor, String loginCookie) {
        LoginUser loginUser = loginService.getLoginUser(loginCookie);
        loginUser.setUserName(anchor.getUserName());
        loginUser.setNotice(anchor.getNotice());
        loginUser.setAge(DateUtil.getAgeByDate(anchor.getBirthday()));
        loginUser.setSex(anchor.getSex());
        if (StringUtils.isNotBlank(anchor.getHeadImg())) {
            loginUser.setHeadImg(anchor.getHeadImg());
        }
        redisClient.set(RedisKey.REDIS_LOGIN_KEY_PREFIX + loginCookie, loginUser);
    	
    }
    
    @Override
    public Map<String, Object> getAnchorInfo(long anchorId) throws ParseException {
        Map<String, Object> anchor = anchorMapper.getAnchorInfo(anchorId);
        anchor.put("age", DateUtil.getAgeByDate((Date)anchor.get("birthday")));
        String birthday = DateUtil.formatDate((Date)anchor.get("birthday"), "yyyy-MM-dd");
        anchor.put("selYear", birthday.substring(0, 4));
        anchor.put("selMonth", birthday.substring(5, 7));
        anchor.put("selDay", birthday.substring(8));
        return anchor;
    }

    @Override
    public Page getContribList(Page page, long anchorId){

        List<Map<String, Object>> dataList = contribMapper.getAnchorContribList(page.getStart(),page.getPageSize(), anchorId);
        //判断房管
        for(Map<String, Object> data:dataList){
            Long userId = (Long)data.get("userId");
            boolean isAdmin = 
                    LiveAdminHelper.isAdmin(redisAdapter, adminMapper, anchorId, userId);
            data.put("isAdmin", isAdmin ? 1 : 0);
            
            // 用户勋章信息
            List<String> medals = medalHonorService.getUserMedalsById(userId);
            data.put("medals", medals);
            //增加贵族等级及贵族图片
            Integer level = RoleNobleHelper.userRoleNobleLevel(redisAdapter, roleInfoMapper, userId);
            if(level != null && level.intValue() > 0) {
            	data.put("nobleCode", level);
            }
        }

        page.setData(dataList);

        page.setTotal(contribMapper.countAnchorContribList(anchorId));

        return page;
    }

    /**
     * 查询主播收到的礼品总数和收入总数
     */
    @Override
    public Map<String, Object> getAllGoodsAndIncome(long anchorId) {
        Map<String, Object> statistics = new HashMap<>();
        // 总礼品数和总收入
        Map<String, Object> allGoodsAndIncome = contribMapper.getAllGoodsAndIncome(anchorId);
        statistics.put("totalGoodsNum", allGoodsAndIncome.get("goodsCnt"));
        statistics.put("totalIncome", allGoodsAndIncome.get("income"));

        // 普通礼物数目和收入
        Map<String, Object> giftSummary = orderMapper.getNobleSummary(anchorId, Constants.ANCHOR_INCOME_RATE, Constants._0);
        statistics.put("giftIncome", giftSummary.get("income"));

        // 背包礼物数目和收入
        Map<String, Object> bpGiftSummary = orderMapper.getNobleSummary(anchorId, Constants.ANCHOR_INCOME_RATE, 7);
        statistics.put("bpGiftIncome", bpGiftSummary.get("income"));

        //贵族礼物
        Map<String, Object> nobleGiftSummary = orderMapper.getNobleSummary(anchorId, Constants.ANCHOR_INCOME_RATE,Constants._1);
        statistics.put("nobleGiftCnt", nobleGiftSummary.get("goodsCnt"));
        BigDecimal nobleGiftIncome = new BigDecimal (nobleGiftSummary.get("income") == null ? 0+"" : nobleGiftSummary.get("income").toString());
        statistics.put("nobleGiftIncome", nobleGiftIncome);

        //弹幕
        Map<String, Object> barrageMonth = orderMapper.getNobleSummary(anchorId, Constants.ANCHOR_INCOME_RATE,Constants._4);
        if(barrageMonth != null){
            statistics.put("barrageCnt",barrageMonth.get("goodsCnt")  == null ? 0 : barrageMonth.get("goodsCnt"));
            statistics.put("barrageIncome",barrageMonth.get("income") == null ? 0 : barrageMonth.get("income"));
        }

        /*String p = redisAdapter.strGet(RedisKey.NobleKey.NOBLE_ANCHOR_PROPORTION);
        double percent = 0.5;
        try {
            percent = Integer.valueOf(p) / 100.0;
        } catch (NumberFormatException e) {
            logger.error("计算主播分成比例异常", e);
        }*/

        //贵族
        Map<String, Object> noble1 = orderMapper.getNobleSummary(anchorId, Constants.ANCHOR_INCOME_RATE,Constants._5); // 直接购买
        if(noble1 != null) {
            BigDecimal goodsCnt1 = new BigDecimal (noble1.get("goodsCnt") == null ? 0+"" : noble1.get("goodsCnt").toString());
            statistics.put("nobleCnt", goodsCnt1.intValue());
            BigDecimal income1 = new BigDecimal (noble1.get("income") == null ? 0+"" : noble1.get("income").toString());
            statistics.put("nobleIncome", income1.intValue());
        }
        System.out.println(statistics.toString());
        return statistics;
    }

    /**
     * 获取每月礼品详细和统计, 返回的list中的元素为map, 其中包括年月信息, 当月礼品总数, 当月总收入, 当月礼品列表
     */
    @Override
    public Page getMonthDetail(Page page, long anchorId) {
        List<Map<String, Object>> dataList = new ArrayList<>();
        //获得统计信息, 包括年月信息, 当月礼品总数, 当月总收入
//        List<Map<String, Object>> statsList = orderMapper.getMonthStatistics(page.getStart(),page.getPageSize(), anchorId, Constants.ANCHOR_INCOME_RATE);
        List<Map<String, Object>> goodsstatsList = orderMapper.getMonthGoodsStatistics(page.getStart(),page.getPageSize(), anchorId, Constants.ANCHOR_INCOME_RATE); // 统计普通礼物和背包礼物数据
        String yearMonth = null;
        for(Map<String, Object> stats:goodsstatsList){
            yearMonth = (String)stats.get("yearMonth");
            //获取主播当月礼品列表
            List<Map<String, Object>> goodsList = orderMapper.getMonthGoodsDetail(anchorId, yearMonth,0);
            stats.put("goodsList", goodsList);

            // 背包礼物
            List<Map<String, Object>> bpGiftList = orderMapper.getMonthGoodsDetail(anchorId, yearMonth,7);
            stats.put("bpGiftList", bpGiftList);

            //贵族礼物
            Map<String, Object> nobleByMonth = orderMapper.getNobleByMonth(yearMonth, anchorId, Constants.ANCHOR_INCOME_RATE,Constants._1);
            List<Map<String, Object>> nobleList = orderMapper.getMonthGoodsDetail(anchorId, yearMonth,Constants._1);
            if(nobleList != null && nobleList.size() > 0) {
                stats.put("nobleList",nobleList);
                if (nobleByMonth == null || nobleByMonth.isEmpty()) {
                	nobleByMonth =  new HashMap<String, Object>();
                	nobleByMonth.put("goodsCnt", 0);
                	nobleByMonth.put("income", 0.0);
                }
                stats.put("nobleCnt",nobleByMonth.get("goodsCnt"));
                stats.put("nobleIncome",nobleByMonth.get("income"));
            }

            //弹幕
            Map<String, Object> barrageMonth = orderMapper.getNobleByMonth(yearMonth, anchorId, Constants.ANCHOR_INCOME_RATE,Constants._4);
            List<Map<String, Object>> barrageList = orderMapper.getMonthGoodsDetail(anchorId, yearMonth,Constants._4);
            if(barrageList != null && barrageList.size() > 0) {
                stats.put("barrageList",barrageList);
                if (barrageMonth == null || barrageMonth.isEmpty()) {
                	barrageMonth =  new HashMap<String, Object>();
                	barrageMonth.put("goodsCnt", 0);
                	barrageMonth.put("income", 0.0);
                }
                stats.put("barrageCnt",barrageMonth.get("goodsCnt"));
                stats.put("barrageIncome",barrageMonth.get("income"));
            }

            //贵族
//            Map<String, Object> levelMonth1 = orderMapper.getNobleByMonth(yearMonth, anchorId, Constants.ANCHOR_INCOME_RATE,Constants._2);
//            List<Map<String, Object>> levelList1 = orderMapper.getMonthGoodsDetail(anchorId, yearMonth,Constants._2);

            Map<String, Object> levelMonth2 = orderMapper.getNobleByMonth(yearMonth, anchorId, Constants.ANCHOR_INCOME_RATE,Constants._5);
            List<Map<String, Object>> levelList = orderMapper.getMonthGoodsDetail(anchorId, yearMonth,Constants._5);
            if(levelList != null && levelList.size() > 0) {
                getLevelListUserInfo(levelList);
                stats.put("levelList",levelList);
                BigDecimal income = new BigDecimal(0);
                if (levelMonth2 != null && !levelMonth2.isEmpty()) {
                    income = new BigDecimal(levelMonth2.get("income").toString());
                }
                stats.put("levelCnt", levelList.size());
                stats.put("levelIncome", income.longValue());
            }
            dataList.add(stats);
        }

        //根据月份获取当月的礼品列表
        page.setData(dataList);

        page.setTotal(orderMapper.getMonthGoodsListCnt(anchorId));

        return page;
    }

    public void getLevelListUserInfo(List<Map<String, Object>> levelList) {
        for(Map<String, Object> map : levelList) {
            Long userId = MapUtils.getLong(map, "userId");
            UserInfo user = UserInfoHelper.getAndCacheUserInfo(redisAdapter, userInfoMapper, userId);
            if(user == null){
                user = new UserInfo();
                user.setUserName("UNKNOWN");
                user.setSex(2);
                user.setAcctType(0);
            }
            map.put("userId", user.getUserId());
            map.put("headImg", user.getHeadImg());
            map.put("userName", user.getUserName());
            map.put("sex", user.getSex());
        }
    }


    @Override
    public Anchor getById(long anchorId) {
        return anchorMapper.findAnchorById(anchorId);
    }

    @Override
    public Map<String, Object> getVideoCntAndIncome(long anchorId) {
        Map<String, Object> resultMap = anchorMapper.getAnchorIncomeVideoCnt(anchorId);
        if (redisAdapter.existsKey(RedisKey.LETTER_NO_READ_CNT_ + anchorId)) {
        	resultMap.put("noRead", redisAdapter.strGet(RedisKey.LETTER_NO_READ_CNT_ + anchorId));
        } else {
        	resultMap.put("noRead", "0");
        }
        resultMap.put("result", 1);
        return resultMap;
    }

    @Override
    public Page getAdmins(long anchorId, Page page) {
        int startRow = (page.getPageIndex() - 1) * page.getPageSize();
        List<Map<String, Object>> adminList = 
                adminMapper.getListByAnchorId(anchorId, startRow, page.getPageSize()); 
        int cnt = adminMapper.getCnt(anchorId);
        for(Map<String, Object> map : adminList) {
            Integer level = RoleNobleHelper.userRoleNobleLevel(redisAdapter, roleInfoMapper, Long.parseLong(map.get("userId").toString()));
            if(level != null && level.intValue() > 0) {
                map.put("nobleCode", level);
            }
        }

        page.setData(adminList);
        page.setTotal(cnt);
        return page;
    }

    @Transactional
    @Override
    public int setCardFace(long anchorId, Long videoId, String imgUrl) {
        try {
            LiveVideoInfo videoInfo = new LiveVideoInfo();
            videoInfo.setVideoId(videoId);
            videoInfo.setCoverImg(imgUrl);
            videoInfo.setAnchorId(anchorId);
            int cnt = liveVideoInfoMapper.updateByPKAndAnchorId(videoInfo); // 修改直播封面
            redisAdapter.delKeys(RedisKey.LIVE_VIDEO_INFO_ + videoId);

            cnt &= anchorMapper.updateCardFace(anchorId, imgUrl); // 修改牌面信息
            redisAdapter.delKeys(RedisKey.ANCHOR_INFO_ + anchorId);
            return cnt;
        } catch (Exception e) {
            logger.info("上传牌面发生异常", e);
            return 0;
        }
    }

}
