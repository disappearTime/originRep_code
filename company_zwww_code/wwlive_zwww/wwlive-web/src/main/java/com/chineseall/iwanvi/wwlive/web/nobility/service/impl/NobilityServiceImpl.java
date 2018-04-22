package com.chineseall.iwanvi.wwlive.web.nobility.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.chineseall.iwanvi.wwlive.common.cache.RedisClientAdapter;
import com.chineseall.iwanvi.wwlive.common.cache.RedisExpireTime;
import com.chineseall.iwanvi.wwlive.common.cache.RedisKey;
import com.chineseall.iwanvi.wwlive.common.check.ResponseResult;
import com.chineseall.iwanvi.wwlive.common.check.ResultMsg;
import com.chineseall.iwanvi.wwlive.common.constants.Constants;
import com.chineseall.iwanvi.wwlive.dao.wwlive.*;
import com.chineseall.iwanvi.wwlive.domain.wwlive.*;
import com.chineseall.iwanvi.wwlive.web.common.enums.PayType;
import com.chineseall.iwanvi.wwlive.web.common.helper.RoleNobleHelper;
import com.chineseall.iwanvi.wwlive.web.common.pay.DefaultUserPay;
import com.chineseall.iwanvi.wwlive.web.common.pay.OutTradeNoUtil;
import com.chineseall.iwanvi.wwlive.web.common.util.SpringContextUtils;
import com.chineseall.iwanvi.wwlive.web.nobility.common.NobilityCommon;
import com.chineseall.iwanvi.wwlive.web.nobility.controller.NobilityController;
import com.chineseall.iwanvi.wwlive.web.nobility.service.NobilityService;

import org.apache.commons.collections.map.HashedMap;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;

import java.text.SimpleDateFormat;
import java.util.*;

@Service("nobilityServiceImpl")
public class NobilityServiceImpl implements NobilityService{

    static final Logger logger = Logger.getLogger(NobilityController.class);

    @Autowired
    private GoodsInfoMapper goodsInfoMapper;
    @Autowired
    private RoleInfoMapper roleInfoMapper;
    @Autowired
    private CouponInfoMapper couponInfoMapper;
    @Autowired
    private OutTradeNoUtil outTradeNoUtil;
    @Autowired
    private RedisClientAdapter redisClientAdapter;
    @Autowired
    private NobilityCommon nobilityCommon;
    @Autowired
    private OrderInfoMapper orderInfoMapper;
    @Autowired
    private RenewMapper renewMapper;
    
    @Autowired
    private DiscountInfoMapper discountInfoMapper;

    /**
     * 获取骑士相关信息
     * @param userId
     * @return
     */
    @Override
    public List<CavallierInfo> getCavalierInfo(Long userId){

        List<CavallierInfo> cavallierInfoList = new ArrayList<CavallierInfo>();

        //获取所有骑士
        List<GoodsInfo> goodsInfoList = getAllCavalier(null);

        //根据useriId和骑士的id获取用户开通的骑士
        //循环获取骑士的id，查看是否存在对应的信息
        if(goodsInfoList.size()>0){
            for(GoodsInfo goodsInfo:goodsInfoList){
                CavallierInfo cavallierInfo = new CavallierInfo();
                cavallierInfo.setCavalierId(goodsInfo.getGoodsId());//骑士id
                cavallierInfo.setCavalierName(goodsInfo.getGoodsName());//骑士名称
                cavallierInfo.setCavalierImg(goodsInfo.getGoodsImg());//骑士图片
                // 获取是否开通过骑士及有效期
                RoleInfo roleInfo =  getRoleInfoByUidAndGid(userId,goodsInfo.getGoodsId());
                //获取对应的抵用券信息
                CouponInfo couponInfo = getCouponInfoByUidAndGid(userId,goodsInfo.getGoodsId());
                //表明开通过，获取过期时间
                if(roleInfo!=null){
                    cavallierInfo.setIsCavalier(0);//存在骑士
                    cavallierInfo.setCavalierExpiryDate(DateFormatUtils.format(roleInfo.getEffectiveEndTime(), "yyyy-MM-dd HH:mm:ss"));
                    cavallierInfo.setSecretaries("您的专属小秘QQ：3075881783,添加Ta,Ta将为您服务!");
                }
                //表明存在优惠券，获取优惠取金额及过期时间
                if(couponInfo!=null){
                    cavallierInfo.setIsCoupon(0);//存在优惠券
                    cavallierInfo.setCoupon(String.valueOf(couponInfo.getCouponValue()));
                    cavallierInfo.setCouponExpiryDate(DateFormatUtils.format(couponInfo.getEffectivenessTime(), "yyyy-MM-dd HH:mm:ss"));
                }
                cavallierInfoList.add(cavallierInfo);
            }
        }
        return cavallierInfoList;
    }

    /**
     * 获取所有骑士
     * @return
     */
    @Override
    public List<GoodsInfo> getAllCavalier(Long goodsId){
        List<GoodsInfo> goodsInfoList = new ArrayList<GoodsInfo>();
        if(goodsId!=null){
             goodsInfoList = goodsInfoMapper.getGoodsListByType(7);
        }else{
            //根据用户userId获取用户开通那些权限
             goodsInfoList = goodsInfoMapper.getGoodsListByType(7);

            if(goodsInfoList!=null&&goodsInfoList.size()>0){
                //写入缓存
                redisClientAdapter.strSetexByNormal(RedisKey.NobleKey.NOBILITY_ALLCAVALIER, RedisExpireTime.EXPIRE_DAY_30, JSONObject.toJSONString(goodsInfoList));
            }
        }
        return goodsInfoList;
    }

    /**
     * 根据uid和goodsid获取对应的开通信息
     * @param userId
     * @param goodsId
     * @return
     */
    @Override
   public RoleInfo getRoleInfoByUidAndGid(Long userId,Long goodsId){

        //根据用户userId获取用户开通那些权限
        RoleInfo roleInfo = roleInfoMapper.getRoleInfoByUidAndGid(userId,goodsId);

        return roleInfo;
    }
    /**
     * 根据用户id和骑士id获取对应的优惠券信息
     * @param userId
     * @param goodsId
     * @return
     */
    @Override
    public CouponInfo getCouponInfoByUidAndGid(Long userId, Long goodsId){

        //根据用户userId获取用户开通那些权限
        CouponInfo couponInfo = couponInfoMapper.getCouponInfoByUidAndGid(userId,goodsId);

        return couponInfo;
    }

    /**
     * 根据骑士id获取骑士所有的优惠信息
     * @param goodsId
     * @return
     */
    @Override
    public List<DiscountInfo> getAllDiscount(Long goodsId){
    	String key = RedisKey.NobleKey.NOBILITY_DISCOUNT_ + goodsId;
    	
		List<DiscountInfo> discountInfoList = null;
    	if (redisClientAdapter.existsKey(key)) {
    		String discountJson = redisClientAdapter.strGet(key);
    		discountInfoList = JSONArray.parseArray(discountJson, DiscountInfo.class);
    	} else {
    		discountInfoList = discountInfoMapper
					.findDiscountInfosByGoodsId(goodsId);
			// 加入缓存
			if (discountInfoList != null) {
				redisClientAdapter.strSetexByNormal(
						RedisKey.NobleKey.NOBILITY_DISCOUNT_ + goodsId,
						RedisExpireTime.EXPIRE_DAY_30,
						JSONObject.toJSONString(discountInfoList));
			} else {
				discountInfoList = new ArrayList<DiscountInfo>();
			}
    	}
        return discountInfoList;
    }

    /**
     * 根据userId和goodsId获取骑士的价格及优惠
     * @param userId
     * @param goodsId
     * @return
     */
    @Override
    public DiscountPriceInfo getMyNobilityPrice(String userId, String goodsId) {

        DiscountPriceInfo discountPriceInfo = new DiscountPriceInfo();

        //查询骑士对应的价格
        List<GoodsInfo> goodsInfoList = goodsInfoMapper.getGoodsInfo(7, Long.parseLong(goodsId));
//        List<GoodsInfo> goodsInfoList = getAllCavalier(Long.parseLong(goodsId));
        if (goodsInfoList != null && goodsInfoList.size() > 0) {
            discountPriceInfo.setPrice((double) goodsInfoList.get(0).getGoodsPrice()/100);

            //判断是否存在优惠信息(discount)
            List<DiscountInfo> discountInfoList = getAllDiscount(Long.parseLong(goodsId));

            //设置优惠信息
            iScoupon(discountPriceInfo, userId, goodsId, discountInfoList);
        }
        return discountPriceInfo;
    }

    private void iScoupon(DiscountPriceInfo discountPriceInfo,String userId,String goodsId,List<DiscountInfo> discountInfoList) {
        //判断是否存在优惠券(coupon--购买后返的优惠券)
        CouponInfo couponInfo = getCouponInfoByUidAndGid(Long.parseLong(userId), Long.parseLong(goodsId));
        
        if (discountInfoList == null) {
            logger.error("discountInfoList为空--->>>" + discountInfoList);
        	return;
        }
        discountPriceInfo.setIsFirstBuy(0);
        if (couponInfo != null) {
            discountPriceInfo.setIsFirstBuy(1);
        }
        for (DiscountInfo discountInfo : discountInfoList) {
            long discountType = discountInfo.getDiscountType();
            double discountPrice = discountInfo.getDiscountPrice();

            if (discountType == 1) {//初次抵用券
                discountPriceInfo.setFirstVoucher(discountPrice /100);
                discountPriceInfo.setSalePrice(discountPriceInfo.getPrice() - discountPriceInfo.getFirstVoucher());
            }
            if (discountType == 2) {//初次返钻石
                discountPriceInfo.setFirstDiamond(discountPrice /100);
            }
            if (discountType == 3) {//抵用券返钻石
                discountPriceInfo.setDiamond(discountPrice /100);
            }
            if (discountType == 4) {//再次抵用券
                discountPriceInfo.setVoucher(discountPrice /100);
                //贵族价格减去已优惠后的价格

            }
        }
    }

    /**
     * 生成贵族订单
     * @param request
     * @return
     */
    @Transactional
    public ResponseResult<Map<String, Object>> insertMyNobilityOrder(HttpServletRequest request){
        Map<String, Object> dataMap = new HashMap<String, Object>();
        ResponseResult<Map<String, Object>> rr = new ResponseResult<Map<String, Object>>();

        /**
         * userId, videoId, anchorId, 
            		 way, goodsId, payType
         */
        String isFirstBuy = request.getParameter("isFirstBuy");//是否为第一次购买 0是  1否
        String userId = request.getParameter("userId");
        String goodsId = request.getParameter("goodsId");
        String payType = request.getParameter("payType");//支付方式
        //区分骑士的类型，龙骑士和圣骑士为一类，为0，其余的为一类，为1
        String splitCavalier = request.getParameter("splitCavalier");
        String isRenew = request.getParameter("isRenew");//是否自动续费(0是 1否)

        OrderInfo orderInfo = new OrderInfo();
        String orderNo = outTradeNoUtil.getTradeNo(PayType.NBWXPAY);

        //获取骑士的价格、名称、图片
        orderInfo = nobilityCommon.getOrderInfo(goodsId);

        if(splitCavalier.equals("0")){//表明是圣骑士和龙骑士一类的
            nobilityCommon.insertOrder(orderInfo,payType,userId);
            if(isRenew.equals("0")){
                //续费标志
                List<RenewInfo> rs = renewMapper.getRenewInfoByUserId(Long.valueOf(userId));
                if (rs != null && rs.size() > 0) {
                    String key = RedisKey.NobleKey.ORDER_CHECKED_RENEW_ + orderInfo.getOutTradeNo();
                    redisClientAdapter.strSetexByNormal(key, RedisExpireTime.EXPIRE_DAY_5, rs.get(0).getRenewId() + "");
                }
            }
        }else{
            //黑骑士、紫荆骑士、魔法骑士、神殿骑士 存在代金券抵用现金的问题
            //第一次购买不存在抵用券问题
            if(isFirstBuy.equals("0")){
               nobilityCommon.insertOrder(orderInfo,payType,userId);
            }else{
            	//修改下面的代码 修改 金额及打折信息等
                //获取使用抵用券后的价格
                JSONArray array = nobilityCommon.getDiscountInfoByGoodsId(goodsId);
                JSONObject jsonStr = new JSONObject();
                int discountRate = 0;
                for(Object obj : array){
                    jsonStr = JSONObject.parseObject(obj.toString());
                    int discountType = jsonStr.getInteger("discountType");//获取类型为4的，表示有抵用券
                    if(discountType==4){
                        discountRate = jsonStr.getInteger("discountRate");//获取到使用抵用券后的金额
                    }
                }
                //不是第一次购买，有抵用券抵现金的计算
                orderInfo.setOutTradeNo(orderNo);
                orderInfo.setUserId(Long.parseLong(userId));
                orderInfo.setOriginKey(0L);
                orderInfo.setReceiverKey(0L);
                orderInfo.setGoodsNum(1);
                orderInfo.setPayType(Integer.parseInt(payType));
                orderInfo.setOrderType(2);//使用代金券
                orderInfo.setOrderStatus(Constants._0);//未支付
                orderInfo.setOrderYearMonth(DateFormatUtils.format(new Date(), "yyyy-MM"));
                orderInfo.setIncome(0.0);

                //重新赋值
                double discount = (double)discountRate/(double)(orderInfo.getTotalGoodsPrice());
                orderInfo.setDiscount((int)(discount*100));
                orderInfo.setAmt((long)discountRate);


            }
        }
        if (StringUtils.isEmpty(payType)) {
        	payType = "2";
        }
        orderInfo.setAmt(1L);//TODO
        PayType pay = PayType.getPayType(18 + Integer.valueOf(payType));//贵族购买// 2微信支付// 3支付宝支付  // 4钻石支付
        DefaultUserPay userPay = (DefaultUserPay) SpringContextUtils.getBean(pay.getPayBeanName());
        dataMap = userPay.pay(orderInfo, null);
        if(dataMap!=null){
            rr.setResponseByResultMsg(ResultMsg.SUCCESS);
            dataMap.put("orderNo",orderNo);
            rr.setData(dataMap);
        }else{
            rr.setResponseByResultMsg(ResultMsg.FAIL);
        }
        return rr;
    }

    /**
     * 获取用户开通贵族有效时间
     * user_role_noble_goodsids_1145
     */
    public List<Map<String,Object>> getUserNobleTimeEnd(long userId) {
        List<Map<String,Object>> list = new ArrayList<>();
        if(RoleNobleHelper.userRoleNobleLevel(redisClientAdapter, roleInfoMapper, userId) > 0){
            List<RoleInfo> roles = roleInfoMapper.findLevelsByUserId(userId);
            if(roles != null && roles.size() > 0){
                nobleTimeAddData (roles, list, userId);
            }
        }
        return list;
    }

    public void nobleTimeAddData (List<RoleInfo> roles,List<Map<String,Object>> list,long userId) {
        for(RoleInfo roleInfo :roles){
            Map<String,Object> map = new HashedMap();
            map.put("goodsId",roleInfo.getGoodsId());//贵族ID
            map.put("level",roleInfo.getRoleLevel());//贵族等级
            map.put("endTime",getTimeToString(roleInfo.getEffectiveEndTime()));//贵族过期时间
            CouponInfo couponInfo = couponInfoMapper.getCouponInfoByUidAndGid(userId, roleInfo.getGoodsId());
            if(couponInfo != null){
                map.put("couponCndTime",getTimeToString(couponInfo.getEffectivenessTime()));//贵族过期时间coupon_balance
                if (couponInfo.getCouponValue() == null) {
                	couponInfo.setCouponValue(0L);
                }
                map.put("balance", (couponInfo.getCouponValue() / 100));
            }
            list.add(map);
        }
    }

    public String getTimeToString(Date time) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        try{
            return sdf.format(time);
        }catch (Exception E) {
            return "";
        }
    }
}