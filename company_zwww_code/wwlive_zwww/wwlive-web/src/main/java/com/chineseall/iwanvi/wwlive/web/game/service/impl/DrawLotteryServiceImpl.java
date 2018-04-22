package com.chineseall.iwanvi.wwlive.web.game.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.chineseall.iwanvi.wwlive.common.cache.RedisClientAdapter;
import com.chineseall.iwanvi.wwlive.common.cache.RedisKey;
import com.chineseall.iwanvi.wwlive.dao.wwlive.GameGiftMapper;
import com.chineseall.iwanvi.wwlive.dao.wwlive.JackPortMapper;
import com.chineseall.iwanvi.wwlive.dao.wwlive.JackpotDistributionMapper;
import com.chineseall.iwanvi.wwlive.domain.wwlive.GameGift;
import com.chineseall.iwanvi.wwlive.domain.wwlive.JackPort;
import com.chineseall.iwanvi.wwlive.web.game.service.DrawLotteryService;
import com.chineseall.iwanvi.wwlive.web.game.service.GameGiftService;
import com.chineseall.iwanvi.wwlive.web.game.utils.DrawLotteryUtil;
import com.chineseall.iwanvi.wwlive.web.game.utils.SortToListUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 抽奖算法
 * Created by lvliang on 2017/10/18.
 */
@Service
public class DrawLotteryServiceImpl implements DrawLotteryService{
    @Autowired
    private GameGiftService gameGiftService;
    @Autowired
    private RedisClientAdapter redisClientAdapter;
    @Autowired
    private JackPortMapper jackPortMapper;
    @Autowired
    private GameGiftMapper gameGiftMapper;
    @Autowired
    private JackpotDistributionMapper jackpotDistributionMapper;

    private static final Logger logger = Logger.getLogger(DrawLotteryServiceImpl.class);
    /**
     * 抽一次奖
     * 1.根据用户id获取用户的中奖概率,A牌面(缓存)
     * 2.判断用户是否存在中奖概率，若存在，使用用户的中奖概率
     *  若没有，获取基础的中奖概率
     * 3.根据中奖概率，获取中奖的奖品
     * 4.根据奖品的价值，去共享奖池里面获取是否有足够金额出奖
     * 5.若有足够金额，出奖 并减少对应的金钱，已经操作对应库表，若金额不够，返最小奖
     * 6.若返的奖品为A奖品并且出不了奖，对应将奖品的中奖概率增加2倍，上线为20%
     * @param giftType
     * @return
     */
    @Override
    public Set<GameGift> drawLottery(int giftType,int userId,int count,int anchorId) throws Exception{
        Set<GameGift> giftsSet = new HashSet<>();
        //1.获取用户的中奖概率(A牌面)
        String pro = "";
        String proKey = RedisKey.gameKey.DRAWLOTTERY_PRO_TYPE_USERID +giftType+"_"+ userId;

        if(redisClientAdapter.existsKey(proKey)){
            pro = redisClientAdapter.strGet(proKey);
        }
        //2.获取所有的奖牌信息
        List<GameGift> gameGiftsList = new ArrayList<GameGift>();
        String gameGiftKey = RedisKey.gameKey.GAME_GIFT_MAX_+giftType;
        if(redisClientAdapter.existsKey(gameGiftKey)){
            gameGiftsList = JSONArray.parseArray(redisClientAdapter.strGet(gameGiftKey),GameGift.class);
        }else{
            gameGiftsList = gameGiftService.getGameGifts(giftType);
        }
        //排序,最小的奖项在前面
        gameGiftsList = SortToListUtil.getSortToList(gameGiftsList,"desc");
        //判断 一次抽奖还是多次抽奖
        if(count==1){//一次
            //3.进行抽奖
            if(StringUtils.isNotBlank(pro)){
                //根据用户概率进行抽奖(0表明抽到奖了，-1表明没有抽到奖品)
                int index = DrawLotteryUtil.drawGiftByUserPro(Double.parseDouble(pro));
                if(index==1){
                    //表明抽中了A的奖，返回A的奖品
                    GameGift maxGameGift = gameGiftMapper.getMaxGameGift(giftType);
                    giftsSet.add(maxGameGift);
                }else{
                    //没有抽到，继续抽奖
                    int i = DrawLotteryUtil.drawGift(gameGiftsList);
                    giftsSet.add(gameGiftsList.get(i));
                }
            }else{
                int index = DrawLotteryUtil.drawGift(gameGiftsList);
                giftsSet.add(gameGiftsList.get(index));
            }
        }else{//多次
            int sum = 0;
            for(int j=0;j>-1;j++){
                int size = giftsSet.size();
                //循环获取奖品
                if(size<count){
                    if (StringUtils.isNotBlank(pro)) {
                        //根据用户概率进行抽奖(0表明抽到奖了，-1表明没有抽到A奖品)
                        int index = DrawLotteryUtil.drawGiftByUserPro(Double.parseDouble(pro));
                        switch (index){
                            case 1:
                                sum+=1;
                                if(sum<2){
                                    //表明抽中了A的奖，返回A的奖品
                                    giftsSet.add(gameGiftsList.get(gameGiftsList.size()-1));
                                }else{
                                    int index1 = DrawLotteryUtil.drawGift(gameGiftsList);
                                    giftsSet.add(gameGiftsList.get(index1));
                                }
                                break;
                            default:
                                //没有抽到A的奖品，抽取13中奖品种的一种
                                int index1 = DrawLotteryUtil.drawGift(gameGiftsList);
                                giftsSet.add(gameGiftsList.get(index1));
                                break;
                        }
                    } else {
                        int index = DrawLotteryUtil.drawGift(gameGiftsList);
                        giftsSet.add(gameGiftsList.get(index));
                    }

                }else{
                    break;
                }
            }
        }

        logger.info("最初抽到的,没有去奖池作比对的奖品-->>>"+giftsSet);
        //根据抽到的奖品，去共享池里面判断金额是否能够出奖
        Set<GameGift> dealGameGift = dealGift(giftsSet, giftType, userId, pro,gameGiftsList,count,anchorId);
        logger.info("已经处理过的奖品-->>>"+dealGameGift);
        return dealGameGift;

    }

    /**
     * 修改钱足够出奖
     * @param shareAmount
     * @param sumPrice
     * @param amount
     */
    public void updateEnoughMoney(double amount,double sumPrice,double shareAmount,int userId,JackPort jackPort,int giftType,int anchorId){

        //减少对应金额(biz_jack_port)
        JackPort jackPort1 = jackPort;
        jackPort1.setAmount(amount-sumPrice);
        jackPort1.setShareAmount(shareAmount-sumPrice);
        redisClientAdapter.hashMSet(RedisKey.gameKey.JACK_PORT,jackPort1.putFieldValueToMap());

        //插入到资金分配流水表
        JackPort jackPort2 = getJackPortDistribution(userId,jackPort,giftType,anchorId);
        jackPort2.setAmount(sumPrice);
        jackPort2.setShareAmount(sumPrice);
        jackpotDistributionMapper.insertJackpotDistribution(jackPort2);

    }

    /**
     * 修改钱不够出奖
     * @param personalAmount
     * @param minPrice
     * @param personalShareAmount
     */
    public void updateNoEnoughMoney(double personalAmount,double minPrice,double personalShareAmount,JackPort jackPort,int userId,int giftType,int anchorId){
        //减少对应金额(biz_jack_port)
        JackPort jackPort1 = jackPort;
        jackPort1.setAmount(personalAmount-minPrice);
        jackPort1.setShareAmount(personalShareAmount-minPrice);
        redisClientAdapter.hashMSet(RedisKey.gameKey.JACK_PORT_USER_+userId,jackPort1.putFieldValueToMap());

        //插入到资金分配流水表
        JackPort jackPort2 = getJackPortDistribution(userId,jackPort,giftType,anchorId);
        jackPort2.setAmount(minPrice);
        jackPort2.setShareAmount(minPrice);
        jackpotDistributionMapper.insertJackpotDistribution(jackPort2);
    }

    /**
     * 增加用户的A大奖的概率，上限为20%
     * @return
     */
    public void addProCacheByUserId(int userId,String p,int giftType,double probility){
        double pro = Double.parseDouble(p);
        double maxPro = 20;
        double prob = 0.0;
        String proKey = RedisKey.gameKey.DRAWLOTTERY_PRO_TYPE_USERID + giftType+ "_"+userId;
        if(redisClientAdapter.existsKey(proKey)){
            double value = Double.parseDouble(redisClientAdapter.strGet(proKey));
            prob = (value+probility)*2;
            if(prob>maxPro){
                prob = maxPro;
            }
        }else{
            prob = pro*2;
        }
        DecimalFormat df = new DecimalFormat("#.00");
        //处理概率
        prob = prob - probility;
        redisClientAdapter.strSetByNormal(proKey,String.valueOf(df.format(prob)));
    }

    /**
     * 删除相关key
     * @param userId
     * @param giftType
     */
    public void deleteCacheProByUserId(int userId,int giftType){
        String proKey = RedisKey.gameKey.DRAWLOTTERY_PRO_TYPE_USERID + giftType+ "_"+userId;
        redisClientAdapter.delKeys(proKey);
    }


    /**
     * 处理礼物
     * * 根据卡牌的类型和点数判断共享池里金额是否足够
     * 奖池分为公共奖池和个人奖池
     * 如果出QKA大奖，先判断公共奖池对应的卡牌的金额，再判断个人奖池里面的公共金额，再判断公共奖池里面的共享金额
     * 若不是QKA大奖，直接判断公共奖池里面的共享金额，若够，直接出奖，若不够，去个人奖池里面出小奖
     * 出奖完成后，将该用户本次剩余的金额加入到共享奖池里面
     * 1.若够，直接出奖
     * 2.若不够，出未出奖的最小奖
     * @param giftsSet
     * @param giftType
     * @param userId
     * @param pro
     * @param gameGiftsList
     * @return
     */
    private Set<GameGift> dealGift(Set<GameGift> giftsSet, int giftType, int userId, String pro, List<GameGift> gameGiftsList, int count,int anchorId) throws Exception{
        Set<GameGift> gameGiftList = new HashSet<>();
        GameGift minGameGift = new GameGift();
        String PUBLIC_JACK_PORT_KEY = RedisKey.gameKey.JACK_PORT;
        String PERSONAL_JACK_PORT_KEY = RedisKey.gameKey.JACK_PORT_USER_ + userId;

        //循环获取抽中的牌，判断金额是否足够
        for (GameGift gameGift:giftsSet){
            //获取所有牌的最小值
            minGameGift = gameGiftsList.get(0);

            //获取公共共享池里面的金额
            JackPort publicJackPort = new JackPort();
            if(redisClientAdapter.existsKey(PUBLIC_JACK_PORT_KEY)){
                publicJackPort.doStringMapToValue(redisClientAdapter.hashGetAll(PUBLIC_JACK_PORT_KEY));
            }else{
                publicJackPort = jackPortMapper.getJackPort();
            }
            logger.info("公共奖池的金额-->>>"+publicJackPort);

            //获取个人奖池里面的金额
            JackPort personalJackPort = new JackPort();
            personalJackPort.doStringMapToValue(redisClientAdapter.hashGetAll(PERSONAL_JACK_PORT_KEY));
            logger.info("用户"+userId+"奖池的金额-->>>"+personalJackPort);

            double personalShareAmount = personalJackPort.getShareAmount();//个人共享奖池金额
            double personalAmount = personalJackPort.getAmount();//个人奖池总金额
            String points = gameGift.getGameGiftPoints();//AKQ.....
            double sumPrice = gameGift.getGameGiftPrice()*gameGift.getGameGiftNum();//出奖的总金额
            double shareAmount = publicJackPort.getShareAmount();//公共共享奖池金额
            double amount = publicJackPort.getAmount();//公共奖池总金额
            //获取最小奖品
            double minPrice =minGameGift.getGameGiftPrice() * minGameGift.getGameGiftNum();
            double prob = gameGift.getGameGiftProbability();//中奖概率
            pro = StringUtils.isNotBlank(pro)?pro:String.valueOf(gameGift.getGameGiftProbability());

            //判断已出奖品里面是否已经出了.若已经出奖了,不在重复出奖,直接出最小奖
            if(gameGiftList.contains(gameGift)){
                logger.info("已出奖品已存在，出最小奖....");
                dealMinGamegift(personalShareAmount,personalAmount,minPrice,gameGiftList,
                        minGameGift,count,personalJackPort,userId,giftType,gameGiftsList,anchorId);
                continue;
            }

            switch(giftType){
                case 1:
                    if(points.equalsIgnoreCase("A")){
                        double gold_A = publicJackPort.getGold_A();//公共奖池里面的金钱
                        double personalGold_A = personalJackPort.getGold_A();//个人奖池里面的金钱
                        if(gold_A>=sumPrice){
                            logger.info("共享奖池里面金A的钱足够出奖");
                            //公共奖池里金A的钱足够出奖
                            gameGiftList.add(gameGift);
                            gameGiftsList.remove(gameGift);
                            logger.info("恭喜你出奖了......"+gameGift.getGameGiftPoints()+"---"+gameGift);
                            //减少对应金额(biz_jack_port)
                            JackPort jackPort1 = publicJackPort;
                            jackPort1.setAmount(amount-sumPrice);
                            jackPort1.setGold_A(gold_A-sumPrice);
                            redisClientAdapter.hashMSet(PUBLIC_JACK_PORT_KEY,jackPort1.putFieldValueToMap());
                            //删除概率缓存key
                            deleteCacheProByUserId(userId,giftType);

                            //插入到资金分配流水表
                           JackPort jackPort2 = getJackPortDistribution(userId,publicJackPort,giftType,anchorId);
                            jackPort2.setAmount(sumPrice);
                            jackPort2.setGold_A(sumPrice);
                            jackpotDistributionMapper.insertJackpotDistribution(jackPort2);

                        }else if ((gold_A+personalGold_A)>=sumPrice){
                            logger.info("共享奖池里面和个人奖池里面金A的钱足够出奖");
                            //公共奖池里金A的钱和个人奖池里面金A的钱足够出奖
                            gameGiftList.add(gameGift);
                            gameGiftsList.remove(gameGift);
                            logger.info("恭喜你出奖了......"+gameGift.getGameGiftPoints()+"---"+gameGift);
                            //减少对应的金额  1公共奖池 2个人奖池
                            JackPort pubJackPort = publicJackPort;
                            pubJackPort.setAmount(amount-gold_A);
                            pubJackPort.setGold_A(0.0);
                            redisClientAdapter.hashMSet(PUBLIC_JACK_PORT_KEY,pubJackPort.putFieldValueToMap());

                            JackPort perJackPort = personalJackPort;
                            perJackPort.setAmount(personalAmount-(sumPrice-gold_A));
                            perJackPort.setGold_A(personalGold_A-(sumPrice-gold_A));
                            redisClientAdapter.hashMSet(PERSONAL_JACK_PORT_KEY,perJackPort.putFieldValueToMap());

                            //删除概率缓存key
                            deleteCacheProByUserId(userId,giftType);

                            //插入到资金分配流水表
                           JackPort jackPort2 = getJackPortDistribution(userId,publicJackPort,giftType,anchorId);
                            jackPort2.setAmount(sumPrice);
                            jackPort2.setGold_A(sumPrice);
                            jackpotDistributionMapper.insertJackpotDistribution(jackPort2);

                        }else if((gold_A+shareAmount+personalGold_A)>=sumPrice){
                            logger.info("共享奖池里面和个人奖池里面金A的钱和共享奖池里面共享金额的钱足够出奖");
                            //金A的钱和共享奖池的钱够出奖
                            gameGiftList.add(gameGift);
                            gameGiftsList.remove(gameGift);
                            logger.info("恭喜你出奖了......"+gameGift.getGameGiftPoints()+"---"+gameGift);
                            //减少对应的金额  1公共奖池 2个人奖池
                            JackPort pubJackPort = publicJackPort;
                            pubJackPort.setAmount(amount-gold_A-(sumPrice-gold_A-personalGold_A));
                            pubJackPort.setGold_A(0.0);
                            pubJackPort.setShareAmount(shareAmount-(sumPrice-gold_A-personalGold_A));
                            redisClientAdapter.hashMSet(PUBLIC_JACK_PORT_KEY,pubJackPort.putFieldValueToMap());

                            JackPort perJackPort = personalJackPort;
                            perJackPort.setAmount(personalAmount-personalGold_A);
                            perJackPort.setGold_A(0.0);
                            redisClientAdapter.hashMSet(PERSONAL_JACK_PORT_KEY,perJackPort.putFieldValueToMap());

                            //插入到资金分配流水表
                            JackPort jackPort2 = getJackPortDistribution(userId,publicJackPort,giftType,anchorId);
                            jackPort2.setAmount(sumPrice);
                            jackPort2.setGold_A(gold_A+personalGold_A);
                            jackPort2.setShareAmount(sumPrice-gold_A-personalGold_A);
                            jackpotDistributionMapper.insertJackpotDistribution(jackPort2);
                            //删除概率缓存key
                            deleteCacheProByUserId(userId,giftType);
                        }else{
                            logger.info("金A出最小的奖");
                            dealMinGamegift(personalShareAmount,personalAmount,minPrice,gameGiftList,
                                    minGameGift,count,personalJackPort,userId,giftType,gameGiftsList,anchorId);
                            //增加下次抽奖的概率
                            addProCacheByUserId(userId,pro,giftType,prob);
                        }
                    }else if(points.equalsIgnoreCase("K")){
                        double gold_K = publicJackPort.getGold_k();
                        double personalGold_K = personalJackPort.getGold_k();
                        if(gold_K>=sumPrice){
                            logger.info("共享奖池里面金K的钱足够出奖");
                            //金k的钱足够出奖
                            gameGiftList.add(gameGift);
                            gameGiftsList.remove(gameGift);
                            logger.info("恭喜你出奖了......"+gameGift.getGameGiftPoints()+"---"+gameGift);
                            //减少对应金额(biz_jack_port)
                            JackPort jackPort1 = publicJackPort;
                            jackPort1.setAmount(amount-sumPrice);
                            jackPort1.setGold_k(gold_K-sumPrice);
                            redisClientAdapter.hashMSet(PUBLIC_JACK_PORT_KEY,jackPort1.putFieldValueToMap());
                            //插入到资金分配流水表
                           JackPort jackPort2 = getJackPortDistribution(userId,publicJackPort,giftType,anchorId);
                            jackPort2.setAmount(sumPrice);
                            jackPort2.setGold_k(sumPrice);
                            jackpotDistributionMapper.insertJackpotDistribution(jackPort2);
                        }else if ((gold_K+personalGold_K)>=sumPrice){
                            logger.info("共享奖池里面和个人奖池里面金K的钱足够出奖");
                            //公共奖池里金A的钱和个人奖池里面金A的钱足够出奖
                            gameGiftList.add(gameGift);
                            gameGiftsList.remove(gameGift);
                            logger.info("恭喜你出奖了......"+gameGift.getGameGiftPoints()+"---"+gameGift);
                            //减少对应的金额  1公共奖池 2个人奖池
                            JackPort pubJackPort = publicJackPort;
                            pubJackPort.setAmount(amount-personalGold_K);
                            pubJackPort.setGold_k(0.0);
                            redisClientAdapter.hashMSet(PUBLIC_JACK_PORT_KEY,pubJackPort.putFieldValueToMap());

                            JackPort perJackPort = personalJackPort;
                            perJackPort.setAmount(personalAmount-(sumPrice-gold_K));
                            perJackPort.setGold_k(personalGold_K-(sumPrice-gold_K));
                            redisClientAdapter.hashMSet(PERSONAL_JACK_PORT_KEY,perJackPort.putFieldValueToMap());

                            //插入到资金分配流水表
                           JackPort jackPort2 = getJackPortDistribution(userId,publicJackPort,giftType,anchorId);
                            jackPort2.setAmount(sumPrice);
                            jackPort2.setGold_k(sumPrice);
                            jackpotDistributionMapper.insertJackpotDistribution(jackPort2);

                        }else if((gold_K+shareAmount+personalGold_K)>=sumPrice){
                            logger.info("共享奖池里面和个人奖池里面金K的钱和共享奖池里面共享金额的钱足够出奖");
                            //金k的钱和共享奖池的钱够出奖
                            gameGiftList.add(gameGift);
                            gameGiftsList.remove(gameGift);
                            logger.info("恭喜你出奖了......"+gameGift.getGameGiftPoints()+"---"+gameGift);
                            //减少对应的金额  1公共奖池 2个人奖池
                            JackPort pubJackPort = publicJackPort;
                            pubJackPort.setAmount(amount-gold_K-(sumPrice-gold_K-personalGold_K));
                            pubJackPort.setGold_k(0.0);
                            pubJackPort.setShareAmount(shareAmount-(sumPrice-gold_K-personalGold_K));
                            redisClientAdapter.hashMSet(PUBLIC_JACK_PORT_KEY,pubJackPort.putFieldValueToMap());

                            JackPort perJackPort = personalJackPort;
                            perJackPort.setAmount(personalAmount-personalGold_K);
                            perJackPort.setGold_k(0.0);
                            redisClientAdapter.hashMSet(PERSONAL_JACK_PORT_KEY,perJackPort.putFieldValueToMap());

                            //插入到资金分配流水表
                            JackPort jackPort2 = getJackPortDistribution(userId,publicJackPort,giftType,anchorId);
                            jackPort2.setAmount(sumPrice);
                            jackPort2.setGold_k(gold_K+personalGold_K);
                            jackPort2.setShareAmount(sumPrice-gold_K-personalGold_K);
                            jackpotDistributionMapper.insertJackpotDistribution(jackPort2);
                        }else{
                            logger.info("金K出最小奖");
                            dealMinGamegift(personalShareAmount,personalAmount,minPrice,gameGiftList,
                                    minGameGift,count,personalJackPort,userId,giftType,gameGiftsList,anchorId);
                        }
                    }else if(points.equalsIgnoreCase("Q")){
                        double gold_Q = publicJackPort.getGold_Q();
                        double personalGold_Q = personalJackPort.getGold_Q();
                        if(gold_Q>=sumPrice){
                            logger.info("共享奖池里面金Q的钱足够出奖");
                            //金Q的钱足够出奖
                            gameGiftList.add(gameGift);
                            gameGiftsList.remove(gameGift);
                            logger.info("恭喜你出奖了......"+gameGift.getGameGiftPoints()+"---"+gameGift);
                            //减少对应金额(biz_jack_port)
                            JackPort jackPort1 = publicJackPort;
                            jackPort1.setAmount(amount-sumPrice);
                            jackPort1.setGold_Q(gold_Q-sumPrice);
                            redisClientAdapter.hashMSet(PUBLIC_JACK_PORT_KEY,jackPort1.putFieldValueToMap());
                            //插入到资金分配流水表
                           JackPort jackPort2 = getJackPortDistribution(userId,publicJackPort,giftType,anchorId);
                            jackPort2.setAmount(sumPrice);
                            jackPort2.setGold_Q(sumPrice);
                            jackpotDistributionMapper.insertJackpotDistribution(jackPort2);
                        }else if ((gold_Q+personalGold_Q)>=sumPrice){
                            logger.info("共享奖池里面和个人奖池里面金Q的钱足够出奖");
                        //公共奖池里金q的钱和个人奖池里面金Q的钱足够出奖
                        gameGiftList.add(gameGift);
                        gameGiftsList.remove(gameGift);
                        logger.info("恭喜你出奖了......"+gameGift.getGameGiftPoints()+"---"+gameGift);
                        //减少对应的金额  1公共奖池 2个人奖池
                        JackPort pubJackPort = publicJackPort;
                        pubJackPort.setAmount(amount-personalGold_Q);
                        pubJackPort.setGold_Q(0.0);
                        redisClientAdapter.hashMSet(PUBLIC_JACK_PORT_KEY,pubJackPort.putFieldValueToMap());

                        JackPort perJackPort = personalJackPort;
                        perJackPort.setAmount(personalAmount-(sumPrice-gold_Q));
                        perJackPort.setGold_Q(personalGold_Q-(sumPrice-gold_Q));
                        redisClientAdapter.hashMSet(PERSONAL_JACK_PORT_KEY,perJackPort.putFieldValueToMap());

                        //插入到资金分配流水表
                        JackPort jackPort2 = getJackPortDistribution(userId,publicJackPort,giftType,anchorId);
                        jackPort2.setAmount(sumPrice);
                        jackPort2.setGold_Q(sumPrice);
                        jackpotDistributionMapper.insertJackpotDistribution(jackPort2);

                    }else if((gold_Q+shareAmount+personalGold_Q)>=sumPrice){
                            logger.info("共享奖池里面和个人奖池里面金Q的钱和共享奖池里面共享金额的钱足够出奖");
                        //金k的钱和共享奖池的钱够出奖
                        gameGiftList.add(gameGift);
                        gameGiftsList.remove(gameGift);
                        logger.info("恭喜你出奖了......"+gameGift.getGameGiftPoints()+"---"+gameGift);
                        //减少对应的金额  1公共奖池 2个人奖池
                        JackPort pubJackPort = publicJackPort;
                        pubJackPort.setAmount(amount-gold_Q-(sumPrice-gold_Q-personalGold_Q));
                        pubJackPort.setGold_Q(0.0);
                        pubJackPort.setShareAmount(shareAmount-(sumPrice-gold_Q-personalGold_Q));
                        redisClientAdapter.hashMSet(PUBLIC_JACK_PORT_KEY,pubJackPort.putFieldValueToMap());

                        JackPort perJackPort = personalJackPort;
                        perJackPort.setAmount(personalAmount-personalGold_Q);
                        perJackPort.setGold_Q(0.0);
                        redisClientAdapter.hashMSet(PERSONAL_JACK_PORT_KEY,perJackPort.putFieldValueToMap());

                        //插入到资金分配流水表
                       JackPort jackPort2 = getJackPortDistribution(userId,publicJackPort,giftType,anchorId);
                        jackPort2.setAmount(sumPrice);
                        jackPort2.setGold_Q(gold_Q+personalGold_Q);
                        jackPort2.setShareAmount(sumPrice-gold_Q-personalGold_Q);
                        jackpotDistributionMapper.insertJackpotDistribution(jackPort2);
                        }else{
                            logger.info("金Q出最小奖");
                            dealMinGamegift(personalShareAmount,personalAmount,minPrice,gameGiftList,
                                    minGameGift,count,personalJackPort,userId,giftType,gameGiftsList,anchorId);
                        }
                    }else{
                        logger.info("2-J出奖.......");
                        //J1098765432....
                        //判断公共的共享池里面的金额是否大于出奖的价值
                        //若金钱足够,直接出奖
                        //若金钱不够,去个人奖池里面的共享金额去比对,若够出奖,若不够,返回未出奖的最小的金额
                        if(shareAmount>=sumPrice){
                            logger.info("共享奖池的共享金额足够出奖");
                            //公共奖池中的共享金额够返奖
                            gameGiftList.add(gameGift);
                            gameGiftsList.remove(gameGift);
                            logger.info("恭喜你出奖了......"+gameGift.getGameGiftPoints()+"---"+gameGift);
                            //减少对应金额
                            updateEnoughMoney(amount,sumPrice,shareAmount,userId,publicJackPort,giftType,anchorId);

                        }else{
                            logger.info("个人奖池里面出最小奖");
                            //未出奖中的最小值
                            dealMinGamegift(personalShareAmount,personalAmount,minPrice,gameGiftList,
                                    minGameGift,count,personalJackPort,userId,giftType,gameGiftsList,anchorId);
                        }
                    }
                    break;
                case 2:
                    if(points.equalsIgnoreCase("A")){
                        double silver_A = publicJackPort.getSilver_A();
                        double personalSilver_A = personalJackPort.getSilver_A();
                        if(silver_A>=sumPrice){
                            logger.info("共享奖池里面银A的钱足够出奖");
                            //银A的钱足够出奖
                            gameGiftList.add(gameGift);
                            gameGiftsList.remove(gameGift);
                            logger.info("恭喜你出奖了......"+gameGift.getGameGiftPoints()+"---"+gameGift);
                            //减少对应金额(biz_jack_port)
                            JackPort jackPort1 = publicJackPort;
                            jackPort1.setAmount(amount-sumPrice);
                            jackPort1.setSilver_A(silver_A-sumPrice);
                            redisClientAdapter.hashMSet(RedisKey.gameKey.JACK_PORT,jackPort1.putFieldValueToMap());
                            //删除概率缓存key
                            deleteCacheProByUserId(userId,giftType);
                            //插入到资金分配流水表
                           JackPort jackPort2 = getJackPortDistribution(userId,publicJackPort,giftType,anchorId);
                            jackPort2.setAmount(sumPrice);
                            jackPort2.setSilver_A(sumPrice);
                            jackpotDistributionMapper.insertJackpotDistribution(jackPort2);
                        }else if ((silver_A+personalSilver_A)>=sumPrice){
                            logger.info("共享奖池里面和个人奖池里面银A的钱足够出奖");
                            //公共奖池里银A的钱和个人奖池里面银A的钱足够出奖
                            gameGiftList.add(gameGift);
                            gameGiftsList.remove(gameGift);
                            logger.info("恭喜你出奖了......"+gameGift.getGameGiftPoints()+"---"+gameGift);
                            //减少对应的金额  1公共奖池 2个人奖池
                            JackPort pubJackPort = publicJackPort;
                            pubJackPort.setAmount(amount-silver_A);
                            pubJackPort.setSilver_A(0.0);
                            redisClientAdapter.hashMSet(PUBLIC_JACK_PORT_KEY,pubJackPort.putFieldValueToMap());

                            JackPort perJackPort = personalJackPort;
                            perJackPort.setAmount(personalAmount-(sumPrice-silver_A));
                            perJackPort.setSilver_A(personalSilver_A-(sumPrice-silver_A));
                            redisClientAdapter.hashMSet(PERSONAL_JACK_PORT_KEY,perJackPort.putFieldValueToMap());

                            //删除概率缓存key
                            deleteCacheProByUserId(userId,giftType);

                            //插入到资金分配流水表
                           JackPort jackPort2 = getJackPortDistribution(userId,publicJackPort,giftType,anchorId);
                            jackPort2.setAmount(sumPrice);
                            jackPort2.setSilver_A(sumPrice);
                            jackpotDistributionMapper.insertJackpotDistribution(jackPort2);

                        }else if((silver_A+shareAmount+personalSilver_A)>=sumPrice){
                            logger.info("共享奖池里面和个人奖池里面银A的钱和共享奖池里面共享金额的钱足够出奖");
                            //金A的钱和共享奖池的钱够出奖
                            gameGiftList.add(gameGift);
                            gameGiftsList.remove(gameGift);
                            logger.info("恭喜你出奖了......"+gameGift.getGameGiftPoints()+"---"+gameGift);
                            //减少对应的金额  1公共奖池 2个人奖池
                            JackPort pubJackPort = publicJackPort;
                            pubJackPort.setAmount(amount-silver_A-(sumPrice-silver_A-personalSilver_A));
                            pubJackPort.setSilver_A(0.0);
                            pubJackPort.setShareAmount(shareAmount-(sumPrice-silver_A-personalSilver_A));
                            redisClientAdapter.hashMSet(PUBLIC_JACK_PORT_KEY,pubJackPort.putFieldValueToMap());

                            JackPort perJackPort = personalJackPort;
                            perJackPort.setAmount(personalAmount-personalSilver_A);
                            perJackPort.setSilver_A(0.0);
                            redisClientAdapter.hashMSet(PERSONAL_JACK_PORT_KEY,perJackPort.putFieldValueToMap());

                            //插入到资金分配流水表
                           JackPort jackPort2 = getJackPortDistribution(userId,publicJackPort,giftType,anchorId);
                            jackPort2.setAmount(sumPrice);
                            jackPort2.setSilver_A(silver_A+personalSilver_A);
                            jackPort2.setShareAmount(sumPrice-silver_A-personalSilver_A);
                            jackpotDistributionMapper.insertJackpotDistribution(jackPort2);
                            //删除概率缓存key
                            deleteCacheProByUserId(userId,giftType);
                        }else{
                            logger.info("银A出最小的奖");
                            //处理出奖后的逻辑
                            dealMinGamegift(personalShareAmount,personalAmount,minPrice,gameGiftList,
                                    minGameGift,count,personalJackPort,userId,giftType,gameGiftsList,anchorId);
                            //----------------------------增加下次抽奖的概率
                            addProCacheByUserId(userId, pro, giftType, prob);
                        }
                    }else if(points.equalsIgnoreCase("K")){
                        double silver_K = publicJackPort.getSilver_k();
                        double personalSilver_K = personalJackPort.getSilver_k();
                        if(silver_K>=sumPrice){
                            logger.info("共享奖池里面银K的钱足够出奖");
                            //银k的钱足够出奖
                            gameGiftList.add(gameGift);
                            gameGiftsList.remove(gameGift);
                            logger.info("恭喜你出奖了......"+gameGift.getGameGiftPoints()+"---"+gameGift);
                            //减少对应金额(biz_jack_port)
                            JackPort jackPort1 = publicJackPort;
                            jackPort1.setAmount(amount-sumPrice);
                            jackPort1.setSilver_k(silver_K-sumPrice);
                            redisClientAdapter.hashMSet(PUBLIC_JACK_PORT_KEY,jackPort1.putFieldValueToMap());
                            //插入到资金分配流水表
                           JackPort jackPort2 = getJackPortDistribution(userId,publicJackPort,giftType,anchorId);
                            jackPort2.setAmount(sumPrice);
                            jackPort2.setSilver_k(sumPrice);
                            jackpotDistributionMapper.insertJackpotDistribution(jackPort2);
                        }else if ((silver_K+personalSilver_K)>=sumPrice){
                            logger.info("共享奖池里面和个人奖池里面银K的钱足够出奖");
                            //公共奖池里银K的钱和个人奖池里面银K的钱足够出奖
                            gameGiftList.add(gameGift);
                            gameGiftsList.remove(gameGift);
                            logger.info("恭喜你出奖了......"+gameGift.getGameGiftPoints()+"---"+gameGift);
                            //减少对应的金额  1公共奖池 2个人奖池
                            JackPort pubJackPort = publicJackPort;
                            pubJackPort.setAmount(amount-silver_K);
                            pubJackPort.setSilver_k(0.0);
                            redisClientAdapter.hashMSet(PUBLIC_JACK_PORT_KEY,pubJackPort.putFieldValueToMap());

                            JackPort perJackPort = personalJackPort;
                            perJackPort.setAmount(personalAmount-(sumPrice-silver_K));
                            perJackPort.setSilver_k(personalSilver_K-(sumPrice-silver_K));
                            redisClientAdapter.hashMSet(PERSONAL_JACK_PORT_KEY,perJackPort.putFieldValueToMap());

                            //插入到资金分配流水表
                           JackPort jackPort2 = getJackPortDistribution(userId,publicJackPort,giftType,anchorId);
                            jackPort2.setAmount(sumPrice);
                            jackPort2.setSilver_k(sumPrice);
                            jackpotDistributionMapper.insertJackpotDistribution(jackPort2);

                        }else if((silver_K+shareAmount+personalSilver_K)>=sumPrice){
                            logger.info("共享奖池里面和个人奖池里面银K的钱和共享奖池里面共享金额的钱足够出奖");
                            //金A的钱和共享奖池的钱够出奖
                            gameGiftList.add(gameGift);
                            gameGiftsList.remove(gameGift);
                            logger.info("恭喜你出奖了......"+gameGift.getGameGiftPoints()+"---"+gameGift);
                            //减少对应的金额  1公共奖池 2个人奖池
                            JackPort pubJackPort = publicJackPort;
                            pubJackPort.setAmount(amount-silver_K-(sumPrice-silver_K-personalSilver_K));
                            pubJackPort.setSilver_k(0.0);
                            pubJackPort.setShareAmount(shareAmount-(sumPrice-silver_K-personalSilver_K));
                            redisClientAdapter.hashMSet(PUBLIC_JACK_PORT_KEY,pubJackPort.putFieldValueToMap());

                            JackPort perJackPort = personalJackPort;
                            perJackPort.setAmount(personalAmount-personalSilver_K);
                            perJackPort.setSilver_k(0.0);
                            redisClientAdapter.hashMSet(PERSONAL_JACK_PORT_KEY,perJackPort.putFieldValueToMap());

                            //插入到资金分配流水表
                           JackPort jackPort2 = getJackPortDistribution(userId,publicJackPort,giftType,anchorId);
                            jackPort2.setAmount(sumPrice);
                            jackPort2.setSilver_k(silver_K+personalSilver_K);
                            jackPort2.setShareAmount(sumPrice-silver_K-personalSilver_K);
                            jackpotDistributionMapper.insertJackpotDistribution(jackPort2);
                        }else{
                            logger.info("银K出最小的奖");
                            dealMinGamegift(personalShareAmount,personalAmount,minPrice,gameGiftList,
                                    minGameGift,count,personalJackPort,userId,giftType,gameGiftsList,anchorId);
                        }
                    }else if(points.equalsIgnoreCase("Q")){
                        double silver_Q = publicJackPort.getSilver_Q();
                        double personalSilver_Q = personalJackPort.getSilver_Q();
                        if(silver_Q>=sumPrice){
                            logger.info("共享奖池里面银Q的钱足够出奖");
                            //银Q的钱足够出奖
                            gameGiftList.add(gameGift);
                            gameGiftsList.remove(gameGift);
                            logger.info("恭喜你出奖了......"+gameGift.getGameGiftPoints()+"---"+gameGift);
                            //减少对应金额(biz_jack_port)
                            JackPort jackPort1 = publicJackPort;
                            jackPort1.setAmount(amount-sumPrice);
                            jackPort1.setSilver_Q(silver_Q-sumPrice);
                            redisClientAdapter.hashMSet(PUBLIC_JACK_PORT_KEY,jackPort1.putFieldValueToMap());
                            //插入到资金分配流水表
                           JackPort jackPort2 = getJackPortDistribution(userId,publicJackPort,giftType,anchorId);
                            jackPort2.setAmount(sumPrice);
                            jackPort2.setSilver_Q(sumPrice);
                            jackpotDistributionMapper.insertJackpotDistribution(jackPort2);
                        }else if ((silver_Q+personalSilver_Q)>=sumPrice){
                            logger.info("共享奖池里面和个人奖池里面银Q的钱足够出奖");
                            //公共奖池里银Q的钱和个人奖池里面银Q的钱足够出奖
                            gameGiftList.add(gameGift);
                            gameGiftsList.remove(gameGift);
                            logger.info("恭喜你出奖了......"+gameGift.getGameGiftPoints()+"---"+gameGift);
                            //减少对应的金额  1公共奖池 2个人奖池
                            JackPort pubJackPort = publicJackPort;
                            pubJackPort.setAmount(amount-silver_Q);
                            pubJackPort.setSilver_Q(0.0);
                            redisClientAdapter.hashMSet(PUBLIC_JACK_PORT_KEY,pubJackPort.putFieldValueToMap());

                            JackPort perJackPort = personalJackPort;
                            perJackPort.setAmount(personalAmount-(sumPrice-silver_Q));
                            perJackPort.setSilver_Q(personalSilver_Q-(sumPrice-silver_Q));
                            redisClientAdapter.hashMSet(PERSONAL_JACK_PORT_KEY,perJackPort.putFieldValueToMap());

                            //插入到资金分配流水表
                           JackPort jackPort2 = getJackPortDistribution(userId,publicJackPort,giftType,anchorId);
                            jackPort2.setAmount(sumPrice);
                            jackPort2.setSilver_Q(sumPrice);
                            jackpotDistributionMapper.insertJackpotDistribution(jackPort2);

                        }else if((silver_Q+shareAmount+personalSilver_Q)>=sumPrice){
                            logger.info("共享奖池里面和个人奖池里面银Q的钱和共享奖池里面共享金额的钱足够出奖");
                            //金A的钱和共享奖池的钱够出奖
                            gameGiftList.add(gameGift);
                            gameGiftsList.remove(gameGift);
                            logger.info("恭喜你出奖了......"+gameGift.getGameGiftPoints()+"---"+gameGift);
                            //减少对应的金额  1公共奖池 2个人奖池
                            JackPort pubJackPort = publicJackPort;
                            pubJackPort.setAmount(amount-silver_Q-(sumPrice-silver_Q-personalSilver_Q));
                            pubJackPort.setSilver_Q(0.0);
                            pubJackPort.setShareAmount(shareAmount-(sumPrice-silver_Q-personalSilver_Q));
                            redisClientAdapter.hashMSet(PUBLIC_JACK_PORT_KEY,pubJackPort.putFieldValueToMap());

                            JackPort perJackPort = personalJackPort;
                            perJackPort.setAmount(personalAmount-personalSilver_Q);
                            perJackPort.setSilver_Q(0.0);
                            redisClientAdapter.hashMSet(PERSONAL_JACK_PORT_KEY,perJackPort.putFieldValueToMap());

                            //插入到资金分配流水表
                           JackPort jackPort2 = getJackPortDistribution(userId,publicJackPort,giftType,anchorId);
                            jackPort2.setAmount(sumPrice);
                            jackPort2.setSilver_Q(silver_Q+personalSilver_Q);
                            jackPort2.setShareAmount(sumPrice-silver_Q-personalSilver_Q);
                            jackpotDistributionMapper.insertJackpotDistribution(jackPort2);
                        }else{
                            logger.info("银Q出最小的奖");
                            dealMinGamegift(personalShareAmount,personalAmount,minPrice,gameGiftList,
                                    minGameGift,count,personalJackPort,userId,giftType,gameGiftsList,anchorId);
                        }
                    }else{
                        //J1098765432....
                        if(shareAmount>=sumPrice){
                            logger.info("共享奖池的共享金额足够出奖");
                            //公共奖池中的共享金额够返奖
                            gameGiftList.add(gameGift);
                            gameGiftsList.remove(gameGift);
                            logger.info("恭喜你出奖了......"+gameGift.getGameGiftPoints()+"---"+gameGift);
                            //减少对应金额
                            updateEnoughMoney(amount,sumPrice,shareAmount,userId,publicJackPort,giftType,anchorId);
                        }else{
                            logger.info("个人奖池里面出最小奖");
                            dealMinGamegift(personalShareAmount,personalAmount,minPrice,gameGiftList,
                                    minGameGift,count,personalJackPort,userId,giftType,gameGiftsList,anchorId);
                        }
                    }
                    break;
                case 3:
                    if(points.equalsIgnoreCase("A")){
                        double copper_A = publicJackPort.getCopper_A();
                        double personalCopper_A = personalJackPort.getCopper_A();
                        if(copper_A>=sumPrice){
                            logger.info("共享奖池里面铜A的钱足够出奖");
                            //铜A的钱足够出奖
                            gameGiftList.add(gameGift);
                            gameGiftsList.remove(gameGift);
                            logger.info("恭喜你出奖了......"+gameGift.getGameGiftPoints()+"---"+gameGift);
                            //减少对应金额(biz_jack_port)
                            JackPort jackPort1 = publicJackPort;
                            jackPort1.setAmount(amount-sumPrice);
                            jackPort1.setCopper_A(copper_A-sumPrice);
                            redisClientAdapter.hashMSet(PUBLIC_JACK_PORT_KEY,jackPort1.putFieldValueToMap());
                            //删除概率缓存key
                            deleteCacheProByUserId(userId,giftType);
                            //插入到资金分配流水表
                           JackPort jackPort2 = getJackPortDistribution(userId,publicJackPort,giftType,anchorId);
                            jackPort2.setAmount(sumPrice);
                            jackPort2.setCopper_A(sumPrice);
                            jackpotDistributionMapper.insertJackpotDistribution(jackPort2);
                        }else if ((copper_A+personalCopper_A)>=sumPrice){
                            logger.info("共享奖池里面和个人奖池里面铜A的钱足够出奖");
                            //公共奖池里银A的钱和个人奖池里面银A的钱足够出奖
                            gameGiftList.add(gameGift);
                            gameGiftsList.remove(gameGift);
                            logger.info("恭喜你出奖了......"+gameGift.getGameGiftPoints()+"---"+gameGift);
                            //减少对应的金额  1公共奖池 2个人奖池
                            JackPort pubJackPort = publicJackPort;
                            pubJackPort.setAmount(amount-copper_A);
                            pubJackPort.setCopper_A(0.0);
                            redisClientAdapter.hashMSet(PUBLIC_JACK_PORT_KEY,pubJackPort.putFieldValueToMap());

                            JackPort perJackPort = personalJackPort;
                            perJackPort.setAmount(personalAmount-(sumPrice-copper_A));
                            perJackPort.setCopper_A(personalCopper_A-(sumPrice-copper_A));
                            redisClientAdapter.hashMSet(PERSONAL_JACK_PORT_KEY,perJackPort.putFieldValueToMap());

                            //删除概率缓存key
                            deleteCacheProByUserId(userId,giftType);

                            //插入到资金分配流水表
                           JackPort jackPort2 = getJackPortDistribution(userId,publicJackPort,giftType,anchorId);
                            jackPort2.setAmount(sumPrice);
                            jackPort2.setCopper_A(sumPrice);
                            jackpotDistributionMapper.insertJackpotDistribution(jackPort2);

                        }else if((copper_A+shareAmount+personalCopper_A)>=sumPrice){
                            logger.info("共享奖池里面和个人奖池里面铜A的钱和共享奖池里面共享金额的钱足够出奖");
                            //金A的钱和共享奖池的钱够出奖
                            gameGiftList.add(gameGift);
                            gameGiftsList.remove(gameGift);
                            logger.info("恭喜你出奖了......"+gameGift.getGameGiftPoints()+"---"+gameGift);
                            //减少对应的金额  1公共奖池 2个人奖池
                            JackPort pubJackPort = publicJackPort;
                            pubJackPort.setAmount(amount-copper_A-(sumPrice-copper_A-personalCopper_A));
                            pubJackPort.setCopper_A(0.0);
                            pubJackPort.setShareAmount(shareAmount-(sumPrice-copper_A-personalCopper_A));
                            redisClientAdapter.hashMSet(PUBLIC_JACK_PORT_KEY,pubJackPort.putFieldValueToMap());

                            JackPort perJackPort = personalJackPort;
                            perJackPort.setAmount(personalAmount-personalCopper_A);
                            perJackPort.setCopper_A(0.0);
                            redisClientAdapter.hashMSet(PERSONAL_JACK_PORT_KEY,perJackPort.putFieldValueToMap());

                            //插入到资金分配流水表
                           JackPort jackPort2 = getJackPortDistribution(userId,publicJackPort,giftType,anchorId);
                            jackPort2.setAmount(sumPrice);
                            jackPort2.setCopper_A(copper_A+personalCopper_A);
                            jackPort2.setShareAmount(sumPrice-copper_A-personalCopper_A);
                            jackpotDistributionMapper.insertJackpotDistribution(jackPort2);
                            //删除概率缓存key
                            deleteCacheProByUserId(userId,giftType);
                        }else{
                            logger.info("铜A出最小的奖");
                            dealMinGamegift(personalShareAmount,personalAmount,minPrice,gameGiftList,minGameGift,
                                    count,personalJackPort,userId,giftType,gameGiftsList,anchorId);
                            //----------------------------增加下次抽奖的概率
                            addProCacheByUserId(userId, pro, giftType, prob);
                        }
                    }else if(points.equalsIgnoreCase("K")){
                        double copper_k = publicJackPort.getCopper_k();
                        double personalCopper_k = personalJackPort.getCopper_k();
                        if(copper_k>=sumPrice){
                            logger.info("共享奖池里面铜K的钱足够出奖");
                            //铜k的钱足够出奖
                            gameGiftList.add(gameGift);
                            gameGiftsList.remove(gameGift);
                            logger.info("恭喜你出奖了......"+gameGift.getGameGiftPoints()+"---"+gameGift);
                            //减少对应金额(biz_jack_port)
                            JackPort jackPort1 = publicJackPort;
                            jackPort1.setAmount(amount-sumPrice);
                            jackPort1.setCopper_k(copper_k-sumPrice);
                            redisClientAdapter.hashMSet(PUBLIC_JACK_PORT_KEY,jackPort1.putFieldValueToMap());
                            //插入到资金分配流水表
                           JackPort jackPort2 = getJackPortDistribution(userId,publicJackPort,giftType,anchorId);
                            jackPort2.setAmount(sumPrice);
                            jackPort2.setCopper_k(sumPrice);
                            jackpotDistributionMapper.insertJackpotDistribution(jackPort2);
                        }else if ((copper_k+personalCopper_k)>=sumPrice){
                            logger.info("共享奖池里面和个人奖池里面铜K的钱足够出奖");
                            //公共奖池里银A的钱和个人奖池里面银A的钱足够出奖
                            gameGiftList.add(gameGift);
                            gameGiftsList.remove(gameGift);
                            logger.info("恭喜你出奖了......"+gameGift.getGameGiftPoints()+"---"+gameGift);
                            //减少对应的金额  1公共奖池 2个人奖池
                            JackPort pubJackPort = publicJackPort;
                            pubJackPort.setAmount(amount-copper_k);
                            pubJackPort.setCopper_k(0.0);
                            redisClientAdapter.hashMSet(PUBLIC_JACK_PORT_KEY,pubJackPort.putFieldValueToMap());

                            JackPort perJackPort = personalJackPort;
                            perJackPort.setAmount(personalAmount-(sumPrice-copper_k));
                            perJackPort.setCopper_k(personalCopper_k-(sumPrice-copper_k));
                            redisClientAdapter.hashMSet(PERSONAL_JACK_PORT_KEY,perJackPort.putFieldValueToMap());

                            //插入到资金分配流水表
                           JackPort jackPort2 = getJackPortDistribution(userId,publicJackPort,giftType,anchorId);
                            jackPort2.setAmount(sumPrice);
                            jackPort2.setCopper_k(sumPrice);
                            jackpotDistributionMapper.insertJackpotDistribution(jackPort2);

                        }else if((copper_k+shareAmount+personalCopper_k)>=sumPrice){
                            logger.info("共享奖池里面和个人奖池里面铜K的钱和共享奖池里面共享金额的钱足够出奖");
                            //金A的钱和共享奖池的钱够出奖
                            gameGiftList.add(gameGift);
                            gameGiftsList.remove(gameGift);
                            logger.info("恭喜你出奖了......"+gameGift.getGameGiftPoints()+"---"+gameGift);
                            //减少对应的金额  1公共奖池 2个人奖池
                            JackPort pubJackPort = publicJackPort;
                            pubJackPort.setAmount(amount-copper_k-(sumPrice-copper_k-personalCopper_k));
                            pubJackPort.setCopper_k(0.0);
                            pubJackPort.setShareAmount(shareAmount-(sumPrice-copper_k-personalCopper_k));
                            redisClientAdapter.hashMSet(PUBLIC_JACK_PORT_KEY,pubJackPort.putFieldValueToMap());

                            JackPort perJackPort = personalJackPort;
                            perJackPort.setAmount(personalAmount-personalCopper_k);
                            perJackPort.setCopper_k(0.0);
                            redisClientAdapter.hashMSet(PERSONAL_JACK_PORT_KEY,perJackPort.putFieldValueToMap());

                            //插入到资金分配流水表
                           JackPort jackPort2 = getJackPortDistribution(userId,publicJackPort,giftType,anchorId);
                            jackPort2.setAmount(sumPrice);
                            jackPort2.setCopper_k(copper_k+personalCopper_k);
                            jackPort2.setShareAmount(sumPrice-copper_k-personalCopper_k);
                            jackpotDistributionMapper.insertJackpotDistribution(jackPort2);
                        }else{
                            logger.info("铜K出最小的奖");
                            dealMinGamegift(personalShareAmount,personalAmount,minPrice,gameGiftList,
                                    minGameGift,count,personalJackPort,userId,giftType,gameGiftsList,anchorId);
                        }
                    }else if(points.equalsIgnoreCase("Q")){
                        double copper_q = publicJackPort.getCopper_Q();
                        double personalCopper_q = personalJackPort.getCopper_Q();
                        if(copper_q>=sumPrice){
                            logger.info("共享奖池里面铜Q的钱足够出奖");
                            //铜Q的钱足够出奖
                            gameGiftList.add(gameGift);
                            gameGiftsList.remove(gameGift);
                            logger.info("恭喜你出奖了......"+gameGift.getGameGiftPoints()+"---"+gameGift);
                            //减少对应金额(biz_jack_port)
                            JackPort jackPort1 = publicJackPort;
                            jackPort1.setAmount(amount-sumPrice);
                            jackPort1.setCopper_Q(copper_q-sumPrice);
                            redisClientAdapter.hashMSet(PUBLIC_JACK_PORT_KEY,jackPort1.putFieldValueToMap());
                            //插入到资金分配流水表
                           JackPort jackPort2 = getJackPortDistribution(userId,publicJackPort,giftType,anchorId);
                            jackPort2.setAmount(sumPrice);
                            jackPort2.setCopper_Q(sumPrice);
                            jackpotDistributionMapper.insertJackpotDistribution(jackPort2);
                        }else if ((copper_q+personalCopper_q)>=sumPrice){
                            logger.info("共享奖池里面和个人奖池里面铜Q的钱足够出奖");
                            //公共奖池里银A的钱和个人奖池里面银A的钱足够出奖
                            gameGiftList.add(gameGift);
                            gameGiftsList.remove(gameGift);
                            logger.info("恭喜你出奖了......"+gameGift.getGameGiftPoints()+"---"+gameGift);
                            //减少对应的金额  1公共奖池 2个人奖池
                            JackPort pubJackPort = publicJackPort;
                            pubJackPort.setAmount(amount-copper_q);
                            pubJackPort.setCopper_Q(0.0);
                            redisClientAdapter.hashMSet(PUBLIC_JACK_PORT_KEY,pubJackPort.putFieldValueToMap());

                            JackPort perJackPort = personalJackPort;
                            perJackPort.setAmount(personalAmount-(sumPrice-copper_q));
                            perJackPort.setCopper_Q(personalCopper_q-(sumPrice-copper_q));
                            redisClientAdapter.hashMSet(PERSONAL_JACK_PORT_KEY,perJackPort.putFieldValueToMap());

                            //插入到资金分配流水表
                           JackPort jackPort2 = getJackPortDistribution(userId,publicJackPort,giftType,anchorId);
                            jackPort2.setAmount(sumPrice);
                            jackPort2.setCopper_Q(sumPrice);
                            jackpotDistributionMapper.insertJackpotDistribution(jackPort2);

                        }else if((copper_q+shareAmount+personalCopper_q)>=sumPrice){
                            logger.info("共享奖池里面和个人奖池里面铜Q的钱和共享奖池里面共享金额的钱足够出奖");
                            //金A的钱和共享奖池的钱够出奖
                            gameGiftList.add(gameGift);
                            gameGiftsList.remove(gameGift);
                            logger.info("恭喜你出奖了......"+gameGift.getGameGiftPoints()+"---"+gameGift);
                            //减少对应的金额  1公共奖池 2个人奖池
                            JackPort pubJackPort = publicJackPort;
                            pubJackPort.setAmount(amount-copper_q-(sumPrice-copper_q-personalCopper_q));
                            pubJackPort.setCopper_Q(0.0);
                            pubJackPort.setShareAmount(shareAmount-(sumPrice-copper_q-personalCopper_q));
                            redisClientAdapter.hashMSet(PUBLIC_JACK_PORT_KEY,pubJackPort.putFieldValueToMap());

                            JackPort perJackPort = personalJackPort;
                            perJackPort.setAmount(personalAmount-personalCopper_q);
                            perJackPort.setCopper_Q(0.0);
                            redisClientAdapter.hashMSet(PERSONAL_JACK_PORT_KEY,perJackPort.putFieldValueToMap());

                            //插入到资金分配流水表
                           JackPort jackPort2 = getJackPortDistribution(userId,publicJackPort,giftType,anchorId);
                            jackPort2.setAmount(sumPrice);
                            jackPort2.setCopper_Q(copper_q+personalCopper_q);
                            jackPort2.setShareAmount(sumPrice-copper_q-personalCopper_q);
                            jackpotDistributionMapper.insertJackpotDistribution(jackPort2);
                        }else{
                            logger.info("铜Q出最小的奖");
                            dealMinGamegift(personalShareAmount,personalAmount,minPrice,gameGiftList,
                                    minGameGift,count,personalJackPort,userId,giftType,gameGiftsList,anchorId);
                        }
                    }else{
                        if(shareAmount>=sumPrice){
                            logger.info("共享奖池的共享金额足够出奖");
                            //铜A的钱足够出奖
                            gameGiftList.add(gameGift);
                            gameGiftsList.remove(gameGift);
                            logger.info("恭喜你出奖了......"+gameGift.getGameGiftPoints()+"---"+gameGift);
                            //减少对应金额(biz_jack_port)
                            updateEnoughMoney(amount,sumPrice,shareAmount,userId,publicJackPort,giftType,anchorId);
                        }else{
                            logger.info("个人奖池里面出最小奖");
                            dealMinGamegift(personalShareAmount,personalAmount,minPrice,gameGiftList,
                                    minGameGift,count,personalJackPort,userId,giftType,gameGiftsList,anchorId);
                        }
                    }
                    break;
            }
        }
        return gameGiftList;
    }

    /**
     * 最小奖项处理
     * @param personalShareAmount
     * @param personalAmount
     * @param minGameGift
     * @param count
     */
    public Set<GameGift> dealMinGamegift(double personalShareAmount,double personalAmount,double minPrice ,Set<GameGift> gameGiftList,
                                         GameGift minGameGift,int count,JackPort jackPort,
                                         int userId,int giftType, List<GameGift> gameGiftsList,int anchorId){
        //判断共享奖池里面的钱是否够出最小奖
        if(personalShareAmount>=minPrice) {
            logger.info("恭喜你出最小奖了......"+minGameGift.getGameGiftPoints()+"---"+minGameGift);
            //不够出奖,出剩余奖品里面的最小奖
            gameGiftList.add(minGameGift);
            if(count>1){
                //移除最小的奖项，并将无法出奖的奖品加入到剩余奖品列表中，并重新排序
                gameGiftsList.remove(minGameGift);
            }
            //减少对应金额(biz_jack_port)
            updateNoEnoughMoney(personalAmount, minPrice, personalShareAmount,jackPort,userId,giftType,anchorId);
        }
        return gameGiftList;
    }

    /**
     * 插入到资金流水表
     * @param userId
     * @param jack
     * @return
     */
    public JackPort getJackPortDistribution(int userId,JackPort jack,int giftType,int anchorId){
        JackPort jackport = new JackPort();
        jackport.setUserId((long)userId);
        jackport.setSysAmount(0.0);
        jackport.setGameType(giftType);
        jackport.setJackPortType(jack.getJackPortType());
        jackport.setDistributionType(2);
        jackport.setAnchorId(anchorId);
        return jackport;
    }

}
