package com.chineseall.iwanvi.wwlive.web.game.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.chineseall.iwanvi.wwlive.common.cache.RedisClientAdapter;
import com.chineseall.iwanvi.wwlive.common.cache.RedisExpireTime;
import com.chineseall.iwanvi.wwlive.common.cache.RedisKey;
import com.chineseall.iwanvi.wwlive.common.constants.Constants;
import com.chineseall.iwanvi.wwlive.common.exception.IWanviException;
import com.chineseall.iwanvi.wwlive.common.tools.RongMsgUtils;
import com.chineseall.iwanvi.wwlive.dao.wwlive.*;
import com.chineseall.iwanvi.wwlive.domain.util.DateUtil;
import com.chineseall.iwanvi.wwlive.domain.wwlive.*;
import com.chineseall.iwanvi.wwlive.web.common.enums.PayType;
import com.chineseall.iwanvi.wwlive.web.common.helper.*;
import com.chineseall.iwanvi.wwlive.web.common.pay.OutTradeNoUtil;
import com.chineseall.iwanvi.wwlive.web.common.util.DateTools;
import com.chineseall.iwanvi.wwlive.web.game.service.CardGameService;
import com.chineseall.iwanvi.wwlive.web.game.service.DrawLotteryService;
import com.zw.zcf.util.MapUtils;
import org.apache.commons.collections.map.HashedMap;
import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by Niu Qianghong on 2017-10-18 0018.
 */
@Service
public class CardGameServiceImpl implements CardGameService {

    private static final Logger LOGGER = Logger.getLogger(CardGameServiceImpl.class);

    @Autowired
    private BackpackGiftMapper bpGiftMapper;

    @Autowired
    private RedisClientAdapter redisAdapter;

    @Autowired
    private ContributionListMapper contribMapper;

    @Autowired
    private CardMapper  cardMapper;

    @Autowired
    private LuckyListMapper luckyListMapper;

    @Autowired
    private LiveVideoInfoMapper liveVideoInfoMapper;

    @Autowired
    private OutTradeNoUtil outTradeNoUtil;

    @Autowired
    private JackPortProMapper jackPortProMapper;

    @Autowired
    private JackPortMapper jackPortMapper;

    @Autowired
    private JackpotDistributionMapper jackpotDistributionMapper;

    @Autowired
    private ThreadPoolTaskExecutor taskExecutor;

    @Autowired
    private OrderInfoMapper orderInfoMapper;

    @Autowired
    private GoodsInfoMapper goodsInfoMapper;

    @Autowired
    private WinningRecordsMapper winningRecordsMapper;

    @Autowired
    private AcctInfoMapper acctInfoMapper;

    @Autowired
    private TransInfoMapper transInfoMapper;

    @Autowired
    private DrawLotteryService drawLotteryService;

    @Value("${game.card.bestGifts.url}")
    private String bestGiftsImgUrl;

    private Logger logger = Logger.getLogger(this.getClass());

    @Autowired
    private AnchorMapper anchorMapper;

    @Autowired
    private UserInfoMapper userInfoMapper;

    @Value("${game.card.AGift.url}")
    private String bestGiftUrl;

    @Override
    public Map<String, Object> getBpGiftList(Long userId) {
        Map<String, Object> data = new HashMap<>();
        List<Map<String, Object>> giftList;
        String bpGiftKey = RedisKey.gameKey.BACKPACK_GIFT_ + userId;
        if (redisAdapter.existsKey(bpGiftKey)) {
            String giftJson = redisAdapter.strGet(bpGiftKey);
            List<JSONObject> giftJSONList = (List<JSONObject>) JSON.parse(giftJson);
            data.put("bpGiftsList", giftJSONList);
        } else {
            giftList = bpGiftMapper.getListByUser(userId);
            String giftJson = JSON.toJSONString(giftList);
            redisAdapter.strSetByNormal(bpGiftKey, giftJson);
            redisAdapter.expireKey(bpGiftKey, RedisExpireTime.EXPIRE_DAY_7);
            data.put("bpGiftsList", giftList);
        }

        return data;
    }

    @Override
    public Map<String, Object> getGameCover(Long anchorId) {
        Map<String, Object> gameCover = new HashMap<>();
        gameCover.put("gameContrib", getGameContribByAnchor(anchorId));
        gameCover.put("gameGiftImg", bestGiftsImgUrl);
        List<Map<String, Object>> cardList = cardMapper.getList();
        gameCover.put("cardList", cardList);
        return gameCover;
    }

    /**
     * 获得主播游戏贡献值
     * @param anchorId
     * @return
     */
    private long getGameContribByAnchor(Long anchorId) {
        String today = DateTools.formatDate(new Date(), "yyyyMMdd");
        long contrib = 0L;
        String gameContribKey = RedisKey.gameKey.ANCHOR_GAME_CONTRIB_ + anchorId + "_" + today;
        if (redisAdapter.existsKey(gameContribKey)) {
            contrib = Long.valueOf(redisAdapter.strGet(gameContribKey));
        } else {
            contrib = winningRecordsMapper.getTodayContrib(anchorId, today);
            redisAdapter.strSetByNormal(gameContribKey, contrib + "");
            redisAdapter.expireKey(gameContribKey, RedisExpireTime.EXPIRE_HOUR_12);
        }
        return contrib;
    }

    /**
     * 幸运榜
     * @return
     */
    @Override
    public Map<String, Object> findLuckyList(Long userId) {
        Map<String, Object> lucky = new HashMap<>();
        List<JSONObject> luckyJSONList = new ArrayList<>();
        String reportDate = getNewDateFirstDay ();
        String key = RedisKey.gameKey.LUCKY_LIST_;
        String luckyJson = "";
        if (redisAdapter.existsKey(key)) {
            luckyJson = redisAdapter.strGet(key);
            luckyJSONList = (List<JSONObject>) JSON.parse(luckyJson);
        }else {
            Calendar calendar = Calendar.getInstance();
            int min = calendar.getActualMinimum(Calendar.DAY_OF_WEEK); //获取周开始基准
            int current = calendar.get(Calendar.DAY_OF_WEEK); //获取当天周内天数
            calendar.add(Calendar.DAY_OF_WEEK, min-current + 1); //当天-基准，获取周开始日期
            Date start = calendar.getTime();
            String startTime = DateUtil.formatDate(start, "yyyy-MM-dd");

            List<Map<String, Object>> luckList = winningRecordsMapper.getLuckList(startTime);
            List<Map<String, Object>> luckyUserList = new ArrayList<>();
            int i = 1;
            for (Map<String, Object> luck : luckList) {
                long userId2 = org.apache.commons.collections.MapUtils.getLongValue(luck, "userId");
                double totalPrice = org.apache.commons.collections.MapUtils.getDoubleValue(luck, "totalPrice", 0);
                if (totalPrice == 0) { // 若用户抽奖得到的奖品总价值为0, 说明该用户没有玩过抽奖游戏
                    break;
                }
                int bigPrizeTimes = org.apache.commons.collections.MapUtils.getIntValue(luck, "bigPrizeTimes", 0);
                UserInfo userInfo = userInfoMapper.findById(userId2);
                if (userInfo == null) {
                    continue;
                }
                Map<String, Object> luckyUser = new HashMap<>();
                luckyUser.put("userId", userId2);
                luckyUser.put("userName", userInfo.getUserName());
                String headImg = userInfo.getHeadImg();
                luckyUser.put("headImg", headImg == null ? "" : headImg);
                luckyUser.put("giftPrice", totalPrice);
                luckyUser.put("giftNum", bigPrizeTimes);
                luckyUser.put("rank", i++);
                luckyUser.put("loginId", userInfo.getLoginId());
                luckyUserList.add(luckyUser);
            }
            if (luckyUserList.size() > 0) {
                redisAdapter.strSetByNormal(RedisKey.gameKey.LUCKY_LIST_, JSON.toJSONString(luckyUserList));
                luckyJSONList = (List<JSONObject>) JSON.parse(JSON.toJSONString(luckyUserList));
            }
        }

        if (userId != null) {
            JSONObject parse = null;
            Map<String, Object> luckyUserId = winningRecordsMapper.getLuckyByUserId(userId, reportDate);
            if(luckyUserId != null) {
                //判断本机用户是否在排行榜中
                UserInfo userInfo = UserInfoHelper.getUserInfoFromCache(redisAdapter, userId, null);
                luckyUserId.put("rank",0);
                luckyUserId.put("giftPrice",luckyUserId.get("totalPrice"));
                luckyUserId.put("loginId",userInfo.getLoginId());
                luckyUserId.put("headImg",userInfo.getHeadImg());
                luckyUserId.put("userId",userId);
                luckyUserId.put("userName",userInfo.getUserName());
                String s = JSON.toJSONString(luckyUserId);
                parse = (JSONObject)JSON.parse(s);
            } else {
                luckyUserId = new HashMap<>();
                luckyUserId.put("rank",0);
            }
            if(parse != null) {
                luckyJSONList.add(parse);
            }
        }
        lucky.put("luckyList",luckyJSONList);
        return lucky;
    }

    /**
     * 今日牌面
     */
    @Override
    public Map<String,Object> getDayCardFace() {
        Map<String, Object> Dayface = new HashMap<>();
        String reportDate = getTheDayBefore();
        String key = RedisKey.gameKey.DAY_CARD_FACE_ + reportDate;
        if(redisAdapter.existsKey(key)) {
            String giftJson = redisAdapter.strGet(key);
            List<JSONObject> giftJSONList = (List<JSONObject>) JSON.parse(giftJson);
            for (JSONObject json : giftJSONList) {
                long anchorId = Long.parseLong(json.getString("anchorId"));
                Map<String,Object> anchorInfo = new HashedMap();
                Anchor anchor = AnchorInfoHelper.getAndCacheAnchorInfo(redisAdapter, anchorMapper, anchorId);
                Map<String, Object> videoInfo = liveVideoInfoMapper.getLivingByAnchorId(anchorId);
                if(videoInfo != null) {
                    json.put("cardFace",anchor.getCardFace());
                    json.put("isOnAir",1); //有直播
                    anchorInfo.put("type",videoInfo.get("formatType"));
                    anchorInfo.put("anchorId",anchorId);
                    anchorInfo.put("chatroomId",videoInfo.get("chatroomId"));
                    anchorInfo.put("videoId",videoInfo.get("videoId"));
                    anchorInfo.put("cover",anchor.getCardFace());
                    json.put("liveVideo",anchorInfo);
                }else {
                    json.put("isOnAir",0);
                    anchorInfo.put("cover",anchor.getCardFace());
                    json.put("liveVideo", new HashMap<>());
                }
            }
            Dayface.put("cardList", giftJSONList);
        }else {
            List<Map<String, Object>> dayfaceList = luckyListMapper.findDayfaceList(reportDate);
            String dayfaceJson = JSON.toJSONString(dayfaceList);
            redisAdapter.strSetByNormal(key, dayfaceJson);
            redisAdapter.expireKey(key, RedisExpireTime.EXPIRE_DAY_1);
            if(dayfaceList != null && dayfaceList.size() > 0) {
                for (Map<String,Object> map : dayfaceList) {
                    long anchorId = MapUtils.getLongValue(map, "anchorId");
                    Anchor anchor = AnchorInfoHelper.getAndCacheAnchorInfo(redisAdapter, anchorMapper, anchorId);
                    map.put("cardFace",anchor.getCardFace());
                    getAnchorManage (anchorId, map);
                }
            }
            Dayface.put("cardList", dayfaceList);
        }
        //现在到凌晨剩余时间
        Calendar c = Calendar.getInstance();
        long now = c.getTimeInMillis();
        c.add(Calendar.DAY_OF_MONTH, 1);
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        long millis = c.getTimeInMillis() - now;
        Dayface.put("sysTime", millis);
        return Dayface;
    }

    /**
     * 处理抽奖信息
     */
    @Override
    public synchronized Map<String, Object> processingLuckDraw(int type, long userId, String userName, long anchorId, long videoId,
                                                  int count, String roomNum, String chatroomId,String mode) throws Exception {

        double amt = 0D;
        //抽奖金额计算
        //获取牌面信息
        String key = RedisKey.gameKey.GAME_GOODS_ + type;
        Map<String, String> cardMap = null;
        if(redisAdapter.existsKey(key)) {
            cardMap = redisAdapter.hashGetAll(key);
        }else {
            cardMap = goodsInfoMapper.getGameGoodsByTypeAndSpecial(type);
            redisAdapter.hashMSet(key,cardMap);
        }
        amt = MapUtils.getDoubleValue(cardMap, "goodsPrice")/100*count;

        Map<String,Object> map = new HashedMap();
        //获得用户账户信息
        Map<String, Object> result = UserAcctInfoHelper.getUserAcctInfo(acctInfoMapper, redisAdapter, userId);
        double diamond = MapUtils.getDoubleValue(result, "diamond");
        LOGGER.info(this.getClass().getName() + "processingLuckDraw " + userId
                +" 卡牌类型：" + type + " 用户金额：" + diamond  + "；消费金额：" + amt);
        if(diamond < amt) {
            map.put("result",0);
            map.put("diamond",diamond);
            return map;
        }
        //获取奖池配置信息
        Map<String, String> jackPortPro = null;
        JackPortPro portPro = new JackPortPro();
        String jackPortProkey = RedisKey.gameKey.JACK_PORT_PRO;
        if(redisAdapter.existsKey(jackPortProkey)) {
            jackPortPro = redisAdapter.hashGetAll(jackPortProkey);
        }else {
            jackPortPro = jackPortProMapper.getJackPortProByType();
            redisAdapter.hashMSet(jackPortProkey,jackPortPro);
        }
        portPro.doStringMapToValue(jackPortPro);

        //redis jack_port  添加默认数据
        if(!redisAdapter.existsKey(RedisKey.gameKey.JACK_PORT)) {
            JackPort defaultPort = new JackPort();
            defaultPort.setJackPortType(portPro.getJackPortType());
            redisAdapter.hashMSet(RedisKey.gameKey.JACK_PORT,defaultPort.putFieldValueToMap());
        }
        //抽奖用户
        JackPort jp = jackPortDistributionAddData(userId, amt, portPro, portPro.getJackPortType(), 1, type,anchorId);
        redisAdapter.hashMSet(RedisKey.gameKey.JACK_PORT_USER_ + userId, jp.putFieldValueToMap());
        jackpotDistributionMapper.insertJackpotDistribution(jp);

        //抽奖
        Date st = new Date();
        Set<GameGift> gameGifts = drawLotteryService.drawLottery(type, (int) userId,count,(int)anchorId);

        //共享奖池金额
        JackPort jackPort = jackPortAddData(userId, amt, portPro, portPro.getJackPortType(), 1);
        LOGGER.info(this.getClass().getName() + "processingLuckDraw 共享奖池信息：" + jackPort.toString());
        //添加修改 共享奖池金额
        if(jackPortMapper.getJackPort() != null) {
            jackPortMapper.updateJackPort(jackPort);
        }else {
            jackPortMapper.insertJackPort(jackPort);
        }
        redisAdapter.hashMSet(RedisKey.gameKey.JACK_PORT,jackPort.putFieldValueToMap());

        Date et = new Date();
        long millis = et.getTime() - st.getTime();
        String val = millis > 1000 ? (millis / 1000) + "秒！" : (long) millis + "毫秒";
        System.out.println("抽奖用时--->>>"+val);

        //出奖后逻辑操作
        PrizeLogic (userId,anchorId, videoId, diamond, type, amt, count, roomNum, gameGifts,cardMap,
                userName,count,chatroomId, mode);

        //数据返回
        List<Map<String, Object>> cardList = new ArrayList<>();
        for (GameGift gift:gameGifts) {
            Map<String, Object> card = new HashMap<>();
            card.put("gameGiftPoints", gift.getGameGiftPoints());
            card.put("gameGiftImg", gift.getGameGiftImg());
            card.put("gameGiftNum", gift.getGameGiftNum());
            card.put("gameGiftName", gift.getGameGiftName());
            card.put("special",gift.getSpecial());
            card.put("mode",mode);
            //获取牌面对应的主播ID
            String reportDate = getTheDayBefore();
            Map<String, Object> anchorIdStr = luckyListMapper.findCardFaceImg(gift.getGameGiftPoints(),reportDate);
            if(anchorIdStr != null) {
                long anchorId1 = MapUtils.getLongValue(anchorIdStr, "anchorId");
                getAnchorManage(gift.getGameGiftAnchorId(), card);
                Anchor anchor = AnchorInfoHelper.getAndCacheAnchorInfo(redisAdapter, anchorMapper, anchorId1);
                card.put("cardFace", anchor.getCardFace());
            }else {
                card.put("cardFace", "");
            }

            cardList.add(card);
        }
        map.put("result",1);
        BigDecimal b = new BigDecimal(diamond - amt);
        map.put("diamond",b.setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue());
        map.put("cardList",cardList);
        return map;
    }

    /**
     * 消息通知
     * @param gameGifts
     * @param type 1金，2银，3铜
     * @param userName
     * @param count 抽奖次数
     * @param userId
     */
    public void sendLotteryNotice (Set<GameGift> gameGifts, int type,String userName,int count,
                     long userId, String chatroomId,long videoId,String mode,long anchorId) {
        //消息通知
        for (GameGift gameGift:gameGifts) {
            String points = gameGift.getGameGiftPoints();
            if (type == 1 && "A".equalsIgnoreCase(points)) { // 第一档最大奖
                //跳转直播间
                LiveVideoInfo videoInfo = liveVideoInfoMapper.findVideoInfoById(videoId);
                Map<String, Object> param =  new HashMap<String, Object>();
                param.put("type", videoInfo.getFormatType());
                param.put("anchorId", videoInfo.getAnchorId());
                param.put("chatroomId", videoInfo.getChatroomId());
                param.put("videoId", videoId);
                param.put("cover", videoInfo.getCoverImg());

                // 全站通知
                List<String> livingChatroomIds = liveVideoInfoMapper.findLivingChatroomIds();
                JSONObject dataExtra = new JSONObject();
                dataExtra.put("game", "翻江龙");
                dataExtra.put("userName", userName);
                dataExtra.put("giftName", gameGift.getGameGiftName());
                dataExtra.put("giftImg", bestGiftUrl);
                dataExtra.put("giftCnt", gameGift.getGameGiftNum());
                dataExtra.put("videoId", videoId);
                dataExtra.put("mode", mode);
                dataExtra.put("liveInfo", param);
                RongMsgUtils.sendChatroomMsg(livingChatroomIds, 0L, 35, null, dataExtra.toJSONString());
                LOGGER.info(this.getClass().getName() + " sendLotteryNotice" + " 全站通知: " + dataExtra.toJSONString());
            } else if ("A".equalsIgnoreCase(points) || "K".equalsIgnoreCase(points) || "Q".equalsIgnoreCase(points)) {
                // 聊天室公屏通知
                JSONObject dataExtra = new JSONObject();
                dataExtra.put("game", "翻江龙");
                dataExtra.put("userName", userName);
                dataExtra.put("giftName", gameGift.getGameGiftName());
                dataExtra.put("giftCnt", gameGift.getGameGiftNum());
                dataExtra.put("pickTimes", count);
                dataExtra.put("videoId", videoId);
                dataExtra.put("mode", mode);
                LOGGER.info(this.getClass().getName() + " sendLotteryNotice" + " 聊天室公屏通知: " + dataExtra.toJSONString());
                RongMsgUtils.sendMsg(chatroomId, userId, 34, null, dataExtra.toJSONString());
            }
        }
    }

    /**
     * 出奖逻辑
     */
    public void PrizeLogic (final long userId, final long anchorId, final long videoId, final Double diamond,
                            final int type, final Double amt, final int num, final String roomNum,
                            final Set<GameGift>  gameList,final Map<String, String> map,final String userName,
                            final int count,final String chatroomId,final String mode) {
        taskExecutor.execute(new Runnable() {
            @Override
            public void run() {
                //消息通知
                sendLotteryNotice (gameList, type, userName, count, userId, chatroomId,videoId,mode,anchorId);

                // biz_acct_info 用户金额 减少
                AcctInfo acct = acctInfoMapper.findAcctByUserId(userId);
                acct.setVideoCoin((long) ((diamond - amt)*100));
                acctInfoMapper.updateAcctCoinById(acct);
                LOGGER.info(this.getClass().getName() + " PrizeLogic userId=：" + userId + " 用户账户信息：" + acct.toString());
                //订单信息
                OrderInfo orderInfo = buildLuckDrawOrder(userId, anchorId, videoId,type,num,map);
                int cnt = orderInfoMapper.insertOrderInfo(orderInfo);
                if(gameList != null  && gameList.size() > 0) {
                    for(GameGift game : gameList) {
                        //背包礼物添加
                        GoodsInfo goods = GoodsInfoHelper.getFromCacheIfNotExistsCacheGoodsInfo(redisAdapter,goodsInfoMapper, game.getGameGoodInfoId());
                        if(goods != null) {
                            game.setGameGiftImg(goods.getGoodsImg());
                        }else {
                            game.setGameGiftImg("");
                        }

                        BackpackGift bg = bpGiftMapper.getCnt(userId, game.getGameGoodInfoId());
                        if (bg != null) {
                            bpGiftMapper.updateGiftCnt(userId, game.getGameGoodInfoId(), bg.getBpGiftNum() + game.getGameGiftNum());
                        } else {
                            insertBackPackGift(game, userId);
                        }

                        //中奖信息
                        WinningRecords records = buildWinningRecords(userId, anchorId, videoId, type, roomNum,
                                map, game, orderInfo.getOrderId());
                        winningRecordsMapper.insertWinningRecords(records);
                    }
                }
                // biz_trans_info  添加记录
                buildTransInfo(orderInfo);

                // 添加用户游戏贡献值
                insertContrib(userId, anchorId, num, new BigDecimal(amt + "").multiply(new BigDecimal("100")).longValue(), 2);
                //相关redis 清除[余额, 贡献值, 幸运值, 背包礼物列表]
                redisAdapter.delKeys(
                        RedisKey.USER_COIN_ + userId,
                        RedisKey.USER_CTB_DO_ + userId,
                        RedisKey.gameKey.LUCKY_USER_ + userId,
                        RedisKey.gameKey.BACKPACK_GIFT_ + userId,
                        RedisKey.gameKey.ANCHOR_GAME_CONTRIB_ + anchorId + "_" + DateTools.formatDate(new Date(), "yyyyMMdd"),
                        RedisKey.USER_RANK_SCORE_ + userId,
                        RedisKey.USER_RANK_CONTRIB_ + userId);
            }
        });
    }

    /**
     * 添加背包礼物
     */
    public void insertBackPackGift (GameGift game,long userId) {
        BackpackGift bpg = new BackpackGift();
        bpg.setBpGoodsId(game.getGameGoodInfoId());
        bpg.setBpGiftName(game.getGameGiftName());
        bpg.setBpGiftImg(game.getGameGiftImg());
        bpg.setBpGiftPrice(new BigDecimal(game.getGameGiftPrice()));
        bpg.setBpGiftNum(game.getGameGiftNum());
        bpg.setUserId(userId);
        bpGiftMapper.insertGiftByUserId(bpg);
    }

    /**
     * 生成交易流水信息
     * @param order
     * @return
     */
    private void buildTransInfo(OrderInfo order) {
        TransInfo trans = new TransInfo();
        trans.setUserId(order.getUserId());
        trans.setOutId(order.getOrderId());
        trans.setTransType(Constants._0);//交易类型 0-消费 1充值
        trans.setTransStatus(Constants._0);
        trans.setAmt(order.getAmt());
        trans.setPayType(order.getPayType());//支付类型  0-铜币, 1-积分, 2-微信, 3-支付宝, 4-钻石
        System.out.println(trans.toString());
        transInfoMapper.insertTransInfo(trans);
    }


    /**
     * 共享奖池数据添加
     */
    public JackPort jackPortDistributionAddData (long userId, Double amt, JackPortPro portPro,
                                                 int type, int distributiontype,int gameType,long anchorId) throws ParseException {

        JackPort jackPort = new JackPort();
        jackPort.setUserId(userId);
        jackPort.setAmount(amt);
        Double sysProbability = portPro.getSysProbability()/100;
        jackPort.setSysAmount(amt * sysProbability);
        jackPort.setShareAmount(amt * (portPro.getShareProbability()/100));
        //金
        jackPort.setGold_A(amt * (portPro.getGoldAProbability()/100));
        jackPort.setGold_k(amt * (portPro.getGoldkProbability()/100));
        jackPort.setGold_Q(amt * (portPro.getGoldQProbability()/100));
        //银
        jackPort.setSilver_A(amt * (portPro.getSilverAProbability()/100));
        jackPort.setSilver_k(amt * (portPro.getSilverkProbability()/100));
        jackPort.setSilver_Q(amt * (portPro.getSilverQProbability()/100));
        //铜
        jackPort.setCopper_A(amt * (portPro.getCopperAProbability()/100));
        jackPort.setCopper_k(amt * (portPro.getCopperkProbability()/100));
        jackPort.setCopper_Q(amt * (portPro.getCopperQProbability()/100));

        jackPort.setJackPortType(type);
        jackPort.setDistributionType(distributiontype);
        jackPort.setGameType(gameType);
        jackPort.setAnchorId(Integer.parseInt(anchorId + ""));

        return jackPort;
    }

    /**
     * 奖池数据添加
     */
    public JackPort jackPortAddData (long userId, Double amt, JackPortPro portPro,
                                     int type, int distributiontype) throws ParseException {
        JackPort port = new JackPort();
        String key = RedisKey.gameKey.JACK_PORT;
        if(redisAdapter.existsKey(key)) {
            port.doStringMapToValue(redisAdapter.hashGetAll(key));
        }

        JackPort port2 = new JackPort();
        if(redisAdapter.existsKey(key)) {
            port2.doStringMapToValue(redisAdapter.hashGetAll(RedisKey.gameKey.JACK_PORT_USER_ + userId));
        }

        JackPort jackPort = new JackPort();
        jackPort.setUserId(userId);
        jackPort.setAmount(port.getAmount() + port2.getAmount());
//        Double sysProbability = portPro.getSysProbability()/100;
        jackPort.setSysAmount(port.getSysAmount() + port2.getSysAmount());
        jackPort.setShareAmount(port.getShareAmount() + port2.getShareAmount());
        //金
        jackPort.setGold_A(port.getGold_A() + port2.getGold_A());
        jackPort.setGold_k(port.getGold_k() + port2.getGold_k());
        jackPort.setGold_Q(port.getGold_Q() + port2.getGold_Q());
        //银
        jackPort.setSilver_A(port.getSilver_A() + port2.getSilver_A());
        jackPort.setSilver_k(port.getSilver_k() + port2.getSilver_k());
        jackPort.setSilver_Q(port.getSilver_Q() + port2.getSilver_Q());
        //铜
        jackPort.setCopper_A(port.getCopper_A() + port2.getCopper_A());
        jackPort.setCopper_k(port.getCopper_k() + port2.getCopper_k());
        jackPort.setCopper_Q(port.getCopper_Q() + port2.getCopper_Q());

        jackPort.setJackPortType(type);
        jackPort.setDistributionType(distributiontype);
        redisAdapter.delKeys(RedisKey.gameKey.JACK_PORT_USER_ + userId);
        return jackPort;
    }

    /**
     * 生成订单信息
     */
    private OrderInfo buildLuckDrawOrder(long userId, long anchorId, long videoId, int type, int num, Map<String, String> map) {
        //订单信息
        OrderInfo order = new OrderInfo();
        order.setOutTradeNo(outTradeNoUtil.getTradeNo(PayType.GAME_PAY));
        order.setUserId(userId);
        order.setReceiverKey(anchorId);
        order.setOriginKey(videoId);
        order.setGoodsId(MapUtils.getLongValue(map,"goodsId"));
        order.setGoodsName(map.get("goodsName"));
        order.setGoodsImg(map.get("goodsImg"));
        order.setGoodsNum(num);
        int goodsPrice = MapUtils.getIntValue(map, "goodsPrice") * num;
        order.setTotalGoodsPrice(goodsPrice);//物品价格
        order.setDiscount(100);//折扣，百分比数
        order.setPayType(4);
        order.setOrderType(6);
        order.setOrderStatus(Constants._1);// orderStatus 支付状态 0未支付， 1成功， 2失败，
        // 3关闭， 4异常
        order.setOrderYearMonth(DateFormatUtils.format(new Date(), "yyyy-MM"));
        order.setAmt(Long.valueOf(goodsPrice));//实际支付金额
        order.setIncome(Double.valueOf(goodsPrice));//收入金额
        return order;
    }

    /**
     * 中奖礼品表
     */
    private WinningRecords buildWinningRecords(long userId, long anchorId, long videoId, int type, String roomNum,
                                               Map<String, String> map, GameGift game, Long orderId) {
        WinningRecords winningRecords = new WinningRecords();
        winningRecords.setUserId(userId);
        winningRecords.setGameGiftId(game.getGameGiftId());
        winningRecords.setGameGiftName(game.getGameGiftName());
        winningRecords.setGameGiftPoints(game.getGameGiftPoints());
        winningRecords.setGameGiftPrice(new BigDecimal(game.getGameGiftPrice()));
        winningRecords.setGameGiftNum(game.getGameGiftNum());
        winningRecords.setCardId(MapUtils.getIntValue(map,"goodsId"));
        winningRecords.setCardType(type);
        winningRecords.setCardPrice(MapUtils.getDouble(map,"goodsPrice"));
        winningRecords.setAnchorId(anchorId);
        winningRecords.setVideoId(videoId);
        winningRecords.setRoomNum(roomNum);
        winningRecords.setOrderId(orderId);
        return winningRecords;
    }

    /**
     * 获取主播信息
     */
    public void getAnchorManage (long anchorId,Map<String,Object> map) {
        Map<String,Object> anchorInfo = new HashedMap();
        Map<String, Object> videoInfo = liveVideoInfoMapper.getLivingByAnchorId(anchorId);
        if(videoInfo != null) {
            map.put("isOnAir",1); //有直播
            anchorInfo.put("type",videoInfo.get("formatType"));
            anchorInfo.put("anchorId",anchorId);
            anchorInfo.put("chatroomId",videoInfo.get("chatroomId"));
            anchorInfo.put("videoId",videoInfo.get("videoId"));
            anchorInfo.put("cover",videoInfo.get("coverImg"));
            map.put("liveVideo",anchorInfo);
        }else {
            map.put("isOnAir",0);
            map.put("liveVideo", new HashMap<>());
        }
    }

    /**
     * 获取当天时间的前一天
     * 时间格式 ： yyyyMMdd
     * @return
     */
    public String getTheDayBefore () {
        Calendar ca = Calendar.getInstance();
        ca.setTime(new Date());
        ca.add(Calendar.DATE, -1);
        return new SimpleDateFormat("yyyy-MM-dd").format(ca.getTime());
    }

    /**
     * result = 0, 礼品数量不足; result = 1, 成功; result = 2, 支付失败
     * @param userId
     * @param anchorId
     * @param videoId
     * @param goodsId
     * @param giftCnt   @return
     * @param app    */
    @Transactional
    @Override
    public Map<String, Object> giveGift(final Long userId, final Long anchorId, final Long videoId, Integer goodsId, Integer giftCnt, String app) {
        final Map<String, Object> result = new HashMap<>();
        //判断礼品个数; 插入订单记录; 插入交易记录; 主播贡献值增加; 用户对应礼品数量-1; 清除缓存
        try {
            BackpackGift bg = bpGiftMapper.getCnt(userId, goodsId);
            if (bg == null || bg.getBpGiftNum() < giftCnt) {
                result.put("result", 0);
                return result;
            }
            GoodsInfo goods;
            if(redisAdapter.existsKey(RedisKey.GOODS_INFO_ + goodsId)) {
                goods = GoodsInfoHelper.getGoodsInfo(redisAdapter,
                        goodsId, "goodsId", "goodsName", "goodsImg", "goodsPrice", "discount", "goodsType");
            } else {
                goods = goodsInfoMapper.getById(goodsId);
                if (goods != null) {
                    // 添加用户信息到redis中
                    redisAdapter.hashMSet(RedisKey.GOODS_INFO_ + goodsId, goods.putFieldValueToStringMap());
                    redisAdapter.expireKey(RedisKey.GOODS_INFO_ + goodsId, RedisExpireTime.EXPIRE_DAY_7);
                } else {
                    logger.error("商品：" + goodsId + "不存在。");
                }
            }

            final Long amt = new BigDecimal(goods.getGoodsPrice() + "").multiply(new BigDecimal(giftCnt)).longValue();

            bpGiftMapper.updateGiftCnt(userId, goodsId, bg.getBpGiftNum() - giftCnt);
            Long orderId = insertOrderInfo(userId, anchorId, videoId, giftCnt, amt, app, goods);
            insertTransInfo(userId, orderId, amt);
            insertContrib(userId, anchorId, giftCnt, amt, 1);

            redisAdapter.delKeys(
                    RedisKey.gameKey.BACKPACK_GIFT_ + userId,
                    RedisKey.USER_CTB_DO_ + userId,
                    RedisKey.USER_RANK_CONTRIB_ + userId,
                    RedisKey.USER_RANK_SCORE_ + userId,
                    RedisKey.ANCHOR_USER_AMT_ + anchorId + "_" + userId);

            taskExecutor.execute(new Runnable() {
                @Override
                public void run() {
                    // 发送贡献值变动消息
                    JSONObject dataExtra = new JSONObject();

                    Long contrib = 0L;
                    String contribKey = RedisKey.ANCHOR_CONTRIB_ + anchorId;
                    if (redisAdapter.existsKey(contribKey)) {
                        contrib = redisAdapter.strIncrBy(contribKey, amt);
                    } else {
                        contrib = ContribHelper.getNormalByAnchor(anchorId, contribMapper, redisAdapter);
                    }

                    dataExtra.put("contribVal", contrib.toString());
                    LiveVideoInfo videoInfo = LiveVideoInfoHelper.getVideoInfoCache(redisAdapter, liveVideoInfoMapper, videoId, "chatroomId");
                    if (videoInfo != null) {
                        RongMsgUtils.sendMsg(videoInfo.getChatroomId(), userId, 27, "", dataExtra.toJSONString());
                    }
                }
            });

            result.put("result", 1);
        } catch (Exception e) {
            logger.error("背包送礼异常", e);
            result.put("result", 0);
        }

        return result;
    }

    /**
     * 添加贡献值记录
     * @param userId
     * @param anchorId
     * @param giftCnt
     * @param amt
     * @param type =1, 普通贡献值; =2, 游戏贡献值
     */
    private void insertContrib(Long userId, Long anchorId, Integer giftCnt, Long amt, int type) {
        int cnt = contribMapper.countByAnchorAndUser(anchorId, userId, type);
        if (cnt >= 1) {
            contribMapper.updateTotalAmt(amt.doubleValue(), amt.intValue(), giftCnt, anchorId, userId, type);
        } else {
            ContributionList contrib = new ContributionList();
            contrib.setAnchorId(anchorId);
            contrib.setUserId(userId);
            contrib.setGoodsNum(giftCnt);
            contrib.setOriginalAmt(amt.intValue());
            contrib.setTotalAmt(amt.doubleValue());
            contrib.setType(type);
            contribMapper.insertContribution(contrib);
        }
    }

    /**
     * 插入交易记录
     * @param userId
     * @param orderId
     * @param amt
     */
    private void insertTransInfo(Long userId, Long orderId, Long amt) {
        TransInfo transInfo = new TransInfo();
        transInfo.setUserId(userId);
        transInfo.setOutId(orderId);
        transInfo.setTransType(0);
        transInfo.setTransStatus(0);
        transInfo.setPayType(8); // 8-背包礼物支付
        transInfo.setAmt(amt);
        Date now = new Date();
        transInfo.setCreateTime(now);
        transInfo.setUpdateTime(now);
        transInfoMapper.insertTransInfo(transInfo);
    }

    /**
     * 插入消费记录
     * @param userId
     * @param anchorId
     * @param videoId
     * @param giftCnt
     * @param app
     * @param goods
     */
    private Long insertOrderInfo(Long userId, Long anchorId, Long videoId, Integer giftCnt, Long amt, String app, GoodsInfo goods) {
        OrderInfo orderInfo = new OrderInfo();
        PayType payType = PayType.getPayType(5); // 5-背包礼物送礼
        orderInfo.setOutTradeNo(outTradeNoUtil.getTradeNo(payType));
        LiveVideoInfo info = null;
        if (redisAdapter.existsKey(RedisKey.LIVE_VIDEO_INFO_ + videoId)) {
            info = LiveVideoInfoHelper.getVideoInfoCache(redisAdapter, liveVideoInfoMapper, videoId, "videoStatus");
        } else {
            info = LiveVideoInfoHelper.getAndCacheVideoInfo(redisAdapter, liveVideoInfoMapper, videoId);
        }
        if (info != null && info.getVideoStatus() != null
                && info.getVideoStatus().intValue() ==4) {
            orderInfo.setReceiveNo("01");//01表示回放礼物
            if ("dl".equalsIgnoreCase(app)) {
                orderInfo.setReceiveNo("03");//独立03表示回放礼物
            }
        } else {
            orderInfo.setReceiveNo("02");
        }
        orderInfo.setUserId(userId);
        orderInfo.setOriginKey(videoId);
        orderInfo.setReceiverKey(anchorId);
        orderInfo.setGoodsId(goods.getGoodsId());
        orderInfo.setGoodsNum(giftCnt);
        orderInfo.setGoodsName(goods.getGoodsName());
        orderInfo.setGoodsImg(goods.getGoodsImg());
        orderInfo.setTotalGoodsPrice(goods.getGoodsPrice());
        orderInfo.setDiscount(100);
        orderInfo.setPayType(5);
        orderInfo.setOrderType(7);
        orderInfo.setOrderStatus(1);
        Date now = new Date();
        orderInfo.setOrderYearMonth(DateTools.formatDate(now, "yyyy-MM"));
        orderInfo.setAmt(amt);
        orderInfo.setIncome(amt.doubleValue());
        orderInfo.setCreateTime(now);
        orderInfo.setUpdateTime(now);
        int cnt = orderInfoMapper.insertOrderInfo(orderInfo);
        if (cnt == 1) {
            return orderInfo.getOrderId();
        } else {
            throw new IWanviException("背包送礼插入订单记录失败");
        }
    }

    public String getNewDateFirstDay () {
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        int d = 0;
        if(cal.get(Calendar.DAY_OF_WEEK)==1){
            d = -6;
        }else{
            d = 2-cal.get(Calendar.DAY_OF_WEEK);
        }
        cal.add(Calendar.DAY_OF_WEEK, d);
        //所在周开始日期
        return new SimpleDateFormat("yyyy-MM-dd").format(cal.getTime());
    }
}
