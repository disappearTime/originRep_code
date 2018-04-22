package com.chineseall.iwanvi.wwlive.web.nobility.service.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.chineseall.iwanvi.wwlive.common.cache.RedisClientAdapter;
import com.chineseall.iwanvi.wwlive.common.cache.RedisExpireTime;
import com.chineseall.iwanvi.wwlive.common.cache.RedisKey;
import com.chineseall.iwanvi.wwlive.common.constants.Constants;
import com.chineseall.iwanvi.wwlive.dao.wwlive.GoodsInfoMapper;
import com.chineseall.iwanvi.wwlive.dao.wwlive.RenewMapper;
import com.chineseall.iwanvi.wwlive.dao.wwlive.RoleInfoMapper;
import com.chineseall.iwanvi.wwlive.domain.wwlive.CouponInfo;
import com.chineseall.iwanvi.wwlive.domain.wwlive.GoodsInfo;
import com.chineseall.iwanvi.wwlive.domain.wwlive.OrderInfo;
import com.chineseall.iwanvi.wwlive.domain.wwlive.RoleInfo;
import com.chineseall.iwanvi.wwlive.web.common.enums.PayType;
import com.chineseall.iwanvi.wwlive.web.common.helper.GoodsInfoHelper;
import com.chineseall.iwanvi.wwlive.web.common.pay.DefaultUserPay;
import com.chineseall.iwanvi.wwlive.web.common.pay.OutTradeNoUtil;
import com.chineseall.iwanvi.wwlive.web.common.security.PaymentSecurity;
import com.chineseall.iwanvi.wwlive.web.common.spring.CouponInfoComponent;
import com.chineseall.iwanvi.wwlive.web.common.util.SpringContextUtils;
import com.chineseall.iwanvi.wwlive.web.nobility.service.Nobility2Service;

/**
 * Created by Niu Qianghong on 2017-07-13 0013.
 */
@Service
public class Nobility2ServiceImpl implements Nobility2Service {
	
	private static final Logger LOGGER = Logger.getLogger(Nobility2ServiceImpl.class);

	@Autowired
	private CouponInfoComponent couponInfoComponent;
	
    @Autowired
    private RoleInfoMapper roleInfoMapper;

    @Autowired
    private RedisClientAdapter redisAdapter;

    @Autowired
    private GoodsInfoMapper goodsInfoMapper;

    @Autowired
    private OutTradeNoUtil outTradeNoUtil;

    /**
     * 免密支付协议申请
     */
    @Value("${wxpay.freesecret.url}")
    private String freesecretUrl;

	@Value("${real.pay.app.system}")
	private String realPayAppSystem;
    /**
     * 微信免密支付
     */
    @Value("${wxpay.planid}")
    private String planid;

    /**
     * 商户编码
     */
    @Value("${wxpay.usercode}")
    private String usercode;

    /**
     * 开通免密协议后通知地址
     */
    @Value("${wxpay.freesecret.notifyurl}")
    private String freesecretNotifyUrl;

    /**
     * 
     */
    @Value("${wxpay.freesecret.payurl}")
    private String freesecretPayUrl;

    @Autowired
    private RenewMapper renewMapper;
    
    /**
     * result = 0, 不需要通知; =1, 需要通知
     * @param userId
     * @return
     */
    @Override
    public Map<String, Object> checkExpire(Long userId) {
        Map<String, Object> resultMap = new HashMap<>();
        List<RoleInfo> roles = null;
        String checkKey = RedisKey.NobleKey.NOBLE_CHECK_  + userId;
        String jsonStr = "";
        if (StringUtils.isBlank((jsonStr = redisAdapter.strGet(checkKey)))) {
        	roles = roleInfoMapper.findRoleInfoByUserId(userId);
        	if (roles == null){
        		roles = new ArrayList<RoleInfo>();
        	}
        	redisAdapter.strSetexByNormal(checkKey, RedisExpireTime.EXPIRE_DAY_360, JSONObject.toJSONString(roles));
        } else {
        	roles = JSONObject.parseArray(jsonStr, RoleInfo.class);
        }
        
        if (roles == null || roles.size() == 0){
            resultMap.put("result", 0);
            return resultMap;
        }
        resultMap.put("result", 0);//先赋值不弹窗，如果有失效的则弹窗

        long now = new Date().getTime();
        List<RoleInfo> viableRoles =  new ArrayList<RoleInfo>();
        int maxLevel = 0;
        long wee = getStartTime().getTime();//超过凌晨就丢弃
        long endTime = 0L;
        for(RoleInfo role : roles){
        	endTime = role.getEffectiveEndTime().getTime();
            if (endTime < wee) {//丢弃该信息
                continue;
            }
            if(endTime < now){//丢弃该信息
            	if (role.getRoleLevel() > maxLevel) {//只提示最大等级
            		maxLevel = role.getRoleLevel();
                    resultMap.put("result", 1);
                    resultMap.put("nobleName", role.getGoodsName());
            	}
            } else {//需要保存信息
            	viableRoles.add(role);
            }
        }

    	redisAdapter.strSetexByNormal(checkKey, RedisExpireTime.EXPIRE_DAY_360, JSONObject.toJSONString(viableRoles));
        return resultMap;
    }
    
    /**
     * 当日凌晨信息
     * @return
     */
	private static Date getStartTime() {
		Calendar todayStart = Calendar.getInstance();
		todayStart.set(Calendar.HOUR_OF_DAY, 0);
		todayStart.set(Calendar.MINUTE, 0);
		todayStart.set(Calendar.SECOND, 0);
		todayStart.set(Calendar.MILLISECOND, 0);
		return todayStart.getTime();
	}
	
    /**
     * 生成购买贵族的支付参数信息，或生成微信的自动扣费信息
     * @param userId
     * @param anchorId
     * @param way  1直播间 0H5
     * @param goodsId
     * @param payType 支付类型
     * @return
     */
    public JSONObject noblePurchase(Long userId, Long videoId, Long anchorId, 
    		Integer way, Long goodsId, Integer payType) {
    	
    	String goodsKey = RedisKey.GOODS_INFO_ + goodsId;
    	GoodsInfo goods = null;
    	if (redisAdapter.existsKey(goodsKey)) {
    		goods = GoodsInfoHelper.getGoodsInfo(redisAdapter, goodsId, 
    				"goodsName", "special", "goodsPrice", "goodsId", "goodsImg");
    	} else {
    		goods = GoodsInfoHelper.getAndCacheGoodsInfo(redisAdapter, goodsInfoMapper, goodsId);
    	}

    	JSONObject json = new JSONObject();
    	if (goods == null) {
    		return json;
    	}
    	try {

        	OrderInfo order = buildNobleOrderByGoods(userId, videoId, anchorId, way, goods, payType);
        	Map<String, Object> dataMap = new HashMap<String, Object>();
        	int type = payType.intValue() + 18;

            PayType pay = PayType.getPayType(type);//贵族购买 //微信支付 20 //支付宝支付 21 //钻石支付支付 22
            DefaultUserPay userPay = (DefaultUserPay) SpringContextUtils.getBean(pay.getPayBeanName());
            if (Boolean.FALSE.toString().equals(realPayAppSystem)) {
            	order.setAmt(1L);
    		}
            dataMap = userPay.pay(order, null);

    		String outTradeNo = order.getOutTradeNo();
    		outTradeNo = PaymentSecurity.makeSecurity(outTradeNo, redisAdapter);
    		PaymentSecurity.securityCache(dataMap, outTradeNo, redisAdapter);//加密机制
    		
            json.putAll(dataMap);
            json.put("dataType", 0);
    	} catch(Exception e) {
    		LOGGER.error("生成购买贵族的支付参数信息异常：", e);
    	}
    	
    	return json;
    }
    
    /**
     * 
     * @param userId
     * @param videoId
     * @param anchorId
     * @param way  1直播间 0H5
     * @param goods
     * @param payType
     * @return
     */
    private OrderInfo buildNobleOrderByGoods(Long userId, Long videoId, Long anchorId, Integer way, GoodsInfo goods, Integer payType) {
    	OrderInfo order = new OrderInfo();

    	order.setUserId(userId);
    	order.setGoodsId(goods.getGoodsId());
    	order.setGoodsImg(goods.getGoodsImg());
    	order.setGoodsName(goods.getGoodsName());
    	order.setTotalGoodsPrice(goods.getGoodsPrice());
    	order.setAmt((long) goods.getGoodsPrice());//先写入默认支付金额，如果是续费再重新修改
    	order.setGoodsNum(1);
    	order.setIncome(0.0D);
    	order.setDiscount(100);

    	order.setReceiverKey(0L);
    	order.setOriginKey(0L);
    	if (way.intValue() == 1) {
    		order.setReceiverKey(anchorId);
//        	String p = redisAdapter.strGet(RedisKey.NobleKey.NOBLE_ANCHOR_PROPORTION);
        	int percent = 100;
//        	if (RegexUtils.isNum(p)) {
//        		percent = Integer.valueOf(p);
//        	}
        	order.setDiscount(percent);
        	order.setOriginKey(videoId);
        	order.setIncome(new BigDecimal(goods.getGoodsPrice()).doubleValue());
    	}
    	order.setPayType(payType);
    	order.setOrderYearMonth(DateFormatUtils.format(new Date(), "yyyy-MM"));
    	order.setOutTradeNo(outTradeNoUtil.getTradeNo(PayType.NBWXPAY));
    	Date now = new Date();
    	order.setCreateTime(now);
    	order.setUpdateTime(now);
    	order.setOrderType(5);// 纯现金购买贵族
    	order.setOrderStatus(Constants._0);//未支付

		CouponInfo couponInfo = couponInfoComponent.getCouponInfoByUidAndGid(userId, goods.getGoodsId());
        long discountRate = 0L;
		if (couponInfo != null) {
			discountRate = couponInfo.getCouponValue();
			order.setOrderType(2);//使用代金券
            //重新赋值
			long realPrice = order.getTotalGoodsPrice() - discountRate;
            int discount = new BigDecimal(realPrice).divide(
            		new BigDecimal(goods.getGoodsPrice()), 0, BigDecimal.ROUND_HALF_UP).intValue();
            order.setDiscount(discount);
            order.setAmt(realPrice);
		}
    	return order;
    }

	
}
