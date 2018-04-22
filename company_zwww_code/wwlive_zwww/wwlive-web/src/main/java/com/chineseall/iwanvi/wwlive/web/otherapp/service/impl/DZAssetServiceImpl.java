package com.chineseall.iwanvi.wwlive.web.otherapp.service.impl;

import com.chineseall.iwanvi.wwlive.common.cache.RedisClientAdapter;
import com.chineseall.iwanvi.wwlive.common.cache.RedisKey;
import com.chineseall.iwanvi.wwlive.common.constants.Constants;
import com.chineseall.iwanvi.wwlive.dao.wwlive.AcctInfoMapper;
import com.chineseall.iwanvi.wwlive.dao.wwlive.OrderInfoMapper;
import com.chineseall.iwanvi.wwlive.dao.wwlive.RechargeInfoMapper;
import com.chineseall.iwanvi.wwlive.domain.wwlive.AcctInfo;
import com.chineseall.iwanvi.wwlive.domain.wwlive.OrderInfo;
import com.chineseall.iwanvi.wwlive.domain.wwlive.RechargeInfo;
import com.chineseall.iwanvi.wwlive.web.common.util.DateTools;
import com.chineseall.iwanvi.wwlive.web.otherapp.service.DZAssetService;
import org.apache.commons.collections.MapUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Map;

/**
 * Created by Niu Qianghong on 2017-08-28 0028.
 */
@Service
public class DZAssetServiceImpl implements DZAssetService {

    @Autowired
    private OrderInfoMapper orderInfoMapper;

    @Autowired
    private RechargeInfoMapper rechargeInfoMapper;

    @Autowired
    private AcctInfoMapper acctInfoMapper;

    @Autowired
    private RedisClientAdapter redisAdapter;

    private Logger logger = Logger.getLogger(this.getClass());

    private final int FAIL = 0;

    @Override
    public int syncDiamond(Long userId, Long diamonds, Map trans) {
        try {
            String outTradeNo = (String) trans.get("orderNo");
            int payType = (int) trans.get("payType");
            Long targetId = MapUtils.getLongValue(trans, "targetId", 0);
            int result;
            if (diamonds < 0) {
                Long money = Math.abs(diamonds);// 取绝对值
                // 添加消费记录
                OrderInfo orderInfo = new OrderInfo();
                orderInfo.setOutTradeNo(outTradeNo);
                orderInfo.setUserId(userId);
                orderInfo.setReceiverKey(targetId);
                orderInfo.setGoodsId(0L);
                orderInfo.setGoodsName("定制版消费");
                orderInfo.setGoodsNum(1);
                orderInfo.setPayType(4); // 定制版消费都为钻石类型, payType=4
                orderInfo.setOrderStatus(1);
                orderInfo.setOrderYearMonth(DateTools.formatDate(new Date(), "yyyy-MM"));
                orderInfo.setAmt(money);
                orderInfo.setTotalGoodsPrice(money.intValue());
                orderInfo.setIncome(money.doubleValue());
                orderInfo.setOriginKey(0L);
                result = orderInfoMapper.insertOrderInfo(orderInfo);
            } else {
                // 添加充值记录
                RechargeInfo rechargeInfo = new RechargeInfo();
                rechargeInfo.setUserId(userId);
                rechargeInfo.setOutTradeNo(outTradeNo);
                rechargeInfo.setRechargeStatus(1);
                rechargeInfo.setRechargeAmount(diamonds);
                rechargeInfo.setAmt(diamonds);
                rechargeInfo.setGoodsId(0L);
                rechargeInfo.setGoodsName("定制版充值");
                rechargeInfo.setOrigin(payType + 1); // 定制版1-微信, 2-支付宝; 直播2-微信, 3-支付宝
                rechargeInfo.setWay(2);
                rechargeInfo.setAnchorId(targetId);
                rechargeInfo.setRechargeType(1); // 定制版充钻类型为1
                result = rechargeInfoMapper.insertRechargeInfo(rechargeInfo);
            }
            return result;
            /*if (result == FAIL){
                return FAIL;
            }

            // 插入或修改acctInfo表信息
            AcctInfo acct = acctInfoMapper.findAcctByUserId(userId);
            logger.info("账户信息 = " + acct);
            if (acct != null) {
                acct.setVideoCoin(acct.getVideoCoin() + diamonds);
                result = acctInfoMapper.updateAcctCoinById(acct);
                // 删除缓存
                redisAdapter.delKeys(RedisKey.USER_COIN_ + userId, RedisKey.USER_CTB_DO_ + userId);
            } else {
                acct = new AcctInfo();
                acct.setUserId(userId);
                acct.setVideoCoin(diamonds);//100coin为1钻
                acct.setAcctType(Constants._0);//0 普通
                acct.setAcctStatus(Constants._0);//0 正常 1冻结
                result = acctInfoMapper.insertAcctInfo(acct);
            }*/
        } catch (Exception e) {
            logger.error("插入记录失败", e);
            return FAIL;
        }
    }
}
