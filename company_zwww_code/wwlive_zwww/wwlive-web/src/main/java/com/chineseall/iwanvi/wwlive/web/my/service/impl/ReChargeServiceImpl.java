package com.chineseall.iwanvi.wwlive.web.my.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.chineseall.iwanvi.wwlive.common.cache.RedisClientAdapter;
import com.chineseall.iwanvi.wwlive.common.cache.RedisExpireTime;
import com.chineseall.iwanvi.wwlive.common.cache.RedisKey;
import com.chineseall.iwanvi.wwlive.common.constants.Constants;
import com.chineseall.iwanvi.wwlive.dao.wwlive.GoodsInfoMapper;
import com.chineseall.iwanvi.wwlive.dao.wwlive.RechargeInfoMapper;
import com.chineseall.iwanvi.wwlive.dao.wwlive.UserInfoMapper;
import com.chineseall.iwanvi.wwlive.domain.wwlive.GoodsInfo;
import com.chineseall.iwanvi.wwlive.domain.wwlive.RechargeInfo;
import com.chineseall.iwanvi.wwlive.domain.wwlive.UserInfo;
import com.chineseall.iwanvi.wwlive.web.common.charge.DefaultUserCharge;
import com.chineseall.iwanvi.wwlive.web.common.enums.ChargeType;
import com.chineseall.iwanvi.wwlive.web.common.enums.PayType;
import com.chineseall.iwanvi.wwlive.web.common.helper.GoodsInfoHelper;
import com.chineseall.iwanvi.wwlive.web.common.helper.UserInfoHelper;
import com.chineseall.iwanvi.wwlive.web.common.pay.OutTradeNoUtil;
import com.chineseall.iwanvi.wwlive.web.common.util.RequestParamsUtils;
import com.chineseall.iwanvi.wwlive.web.common.util.SpringContextUtils;
import com.chineseall.iwanvi.wwlive.web.my.service.ReChargeService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ReChargeServiceImpl implements ReChargeService {

    @Autowired
    private GoodsInfoMapper goodsInfoMapper;

    @Autowired
    private OutTradeNoUtil outTradeNoUtil;

    @Autowired
    private UserInfoMapper userInfoMapper;

    @Autowired
    private RedisClientAdapter redisAdapter;

    @Autowired
    private RechargeInfoMapper rechargeMapper;

    @Value("${cx.ratio}")
    private String cxRatio;

/*    // TODO NIU: 线上url修改
    @Value("${dz.syncdata.url}")
    private String syncDataUrl;

    @Autowired
    private ThreadPoolTaskExecutor taskExecutor;*/

//    private Logger LOGGER = Logger.getLogger(this.getClass());

	@Value("${real.pay.app.system}")
	private String realPayAppSystem;

    /**
     * 充值
     */
    public Map<String, Object> reCharge(long goodsId, RechargeInfo info, int amt) {

        GoodsInfo goods = getGoodsInfo(goodsId, amt, info.getOrigin());

        System.out.println(this.getClass().getName() + "                      " + info.getAmt());
        //充值来源 1-积分, 2-微信, 3-支付宝
        ChargeType charge = ChargeType.getChargeType(info.getOrigin());
        if (charge == null) {
            return new HashMap<String, Object>();
        }

        DefaultUserCharge userCharge = (DefaultUserCharge) SpringContextUtils.getBean(charge.getChargeBeanName());
        userCharge.reBuildRechargeInfo(info, goods);
        info.setOutTradeNo(outTradeNoUtil.getTradeNo(PayType.CZPAY));
        if (Boolean.FALSE.toString().equals(realPayAppSystem)) {
        	info.setAmt(1L);
		}
        System.out.println(this.getClass().getName() + "   " + info.getAmt());
        Map<String, Object> rechargeResultMap = userCharge.resultMap(info);
        return rechargeResultMap;

    }

    private GoodsInfo getGoodsInfo(long goodsId, int amt, int chargeType) {
        GoodsInfo goods = null;
        if (goodsId == 0L) {
            goods = new GoodsInfo();
            goods.setGoodsName(amt + "");
            goods.setGoodsId(0L);
            Integer discount = 100;
            int tmpAmt = amt * 100;
            if (chargeType != 1) {//积分兑换 100
                discount = goodsInfoMapper.getDiscount(tmpAmt);
            }
            goods.setDiscount(discount == null ? 100 : discount);
            goods.setGoodsPrice(tmpAmt);
        } else {
            if (redisAdapter.existsKey(RedisKey.GOODS_INFO_ + goodsId)) {
                goods = GoodsInfoHelper.getGoodsInfo(redisAdapter,
                        goodsId, "goodsId", "goodsName", "goodsImg", "goodsPrice", "discount");
            } else {
                goods = GoodsInfoHelper.getAndCacheGoodsInfo(redisAdapter, goodsInfoMapper, goodsId);
            }
        }
        return goods;
    }

    public Map<String, Object> getRechageList() {
        Map<String, Object> result = new HashMap<String, Object>();
        if (redisAdapter.existsKey(RedisKey.RECHARGE_GOODS_LIST)) {
            String goods = redisAdapter.strGet(RedisKey.RECHARGE_GOODS_LIST);
            List<GoodsInfo> goodsList = JSONArray.parseArray(goods, GoodsInfo.class);
            result.put("rechargeList", goodsList);
            result.put("jsonRecharges", goods);
        } else {
            List<GoodsInfo> goodsList = goodsInfoMapper.getRechageList();
            String goods = JSONArray.toJSONString(goodsList);
            redisAdapter.strSetByNormal(RedisKey.RECHARGE_GOODS_LIST, goods);
            redisAdapter.expireKey(RedisKey.RECHARGE_GOODS_LIST, RedisExpireTime.EXPIRE_DAY_10);
            result.put("rechargeList", goodsList);
            result.put("jsonRecharges", goods);
        }
        return result;
    }

    /**
     * 积分兑换钻石
     */
    public Map<String, String> exchangePage(Long userId) {
        Map<String, String> exCharge = new HashMap<String, String>();
        Map<String, String> userInfo = getUserCurrency(userId);
        exCharge.putAll(userInfo);

        Map<String, String> params = RequestParamsUtils.defaultRequetParams(userId);
        params.putAll(userInfo);
        return params;
    }

    /**
     * 用户昵称，用户的登录id，来源
     *
     * @param userId
     * @return
     */
    private Map<String, String> getUserCurrency(Long userId) {
        Map<String, String> userInfo = redisAdapter.hashMGet(
                RedisKey.USER_INFO_ + userId, "virtualCurrency", "origin");

        if (userInfo == null || userInfo.isEmpty()
                || StringUtils.isBlank(userInfo.get("virtualCurrency"))) {
            UserInfo user = UserInfoHelper.getAndCacheUserInfo(redisAdapter,
                    userInfoMapper, userId);
            if (user == null) {
                return new HashMap<String, String>();
            }
            userInfo = new HashMap<String, String>();
            userInfo.put("virtualCurrency",
                    user.getVirtualCurrency() == null
                            ? "0" : user.getVirtualCurrency().toString());
            userInfo.put("origin",
                    user.getOrigin() == null ? "0" : user.getOrigin().toString());
        }
        String origin = userInfo.get("origin");
        if (StringUtils.isNotBlank(origin) && origin.equals(Constants._0 + "")) {// 0创新版 1中文书城
            userInfo.put("pointRate", cxRatio);
        }

        return userInfo;
    }

    @Override
    public boolean isPaid(String outTradeNo) {
        RechargeInfo recharge = rechargeMapper.getRechargeInfoByOutNo(outTradeNo);
        if (recharge != null && recharge.getRechargeStatus() == 1) {
            return true;
        }
        return false;
    }

    /*@Override
    public void syncDataWithDZ(Map<String, Object> map) {

        if (map == null || map.get("rechargeInfo") == null) {
            return ;
        }

        final RechargeInfo rechargeInfo = (RechargeInfo) map.get("rechargeInfo");

        taskExecutor.execute(new Runnable() {
            @Override
            public void run() {
                LOGGER.info("开始和定制版同步数据, rechargeInfo = " + rechargeInfo);

                try {
                    HttpURLConnection conn = HttpUtils.createPostHttpConnection(syncDataUrl);

                    StringBuilder params = new StringBuilder();
                    params.append("type=0"); // 和定制版约定type=0为充值
                    params.append("&amount=" + rechargeInfo.getRechargeAmount()); // 到账的钻石数
                    params.append("&zbUserId=" + rechargeInfo.getUserId());
                    params.append("&goodsId=0"); // 定制版钻石id=0
                    params.append("&goodsName=钻石");
                    params.append("&goodsCnt=" + rechargeInfo.getRechargeAmount());
                    params.append("&anchorId=" + rechargeInfo.getReceiveNo());

                    OutputStream out = conn.getOutputStream();
                    out.write(params.toString().getBytes());
                    out.close();

                    int responseCode = conn.getResponseCode();
                    if (200 != responseCode) {
                        LOGGER.warn("和定制版同步数据失败, rechargeInfo = " + rechargeInfo);
                    }
                    LOGGER.info("和定制版同步数据完成, rechargeInfo = " + rechargeInfo);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        map.remove("rechargeInfo");

    }*/
}
