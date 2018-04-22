package com.chineseall.iwanvi.wwlive.web.common.spring;

import java.util.Date;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSONObject;
import com.chineseall.iwanvi.wwlive.common.cache.RedisClientAdapter;
import com.chineseall.iwanvi.wwlive.common.cache.RedisExpireTime;
import com.chineseall.iwanvi.wwlive.common.cache.RedisKey;
import com.chineseall.iwanvi.wwlive.common.constants.Constants;
import com.chineseall.iwanvi.wwlive.dao.wwlive.CouponInfoMapper;
import com.chineseall.iwanvi.wwlive.domain.wwlive.CouponInfo;

@Component
public class CouponInfoComponent {

	@Autowired
	private RedisClientAdapter redisAdapter;

	@Autowired
	private CouponInfoMapper couponInfoMapper;
	
	/**
	 * 获得优惠信息
	 * @param userId
	 * @param goodsId
	 * @return 有返回，如果没有返回null
	 */
    public CouponInfo getCouponInfoByUidAndGid(Long userId, Long goodsId){
		//优惠券缓存
    	String jsonStr = null;
		//优惠券折扣信息
		CouponInfo couponInfo = null;
		//优惠券存储缓存key
		String key = RedisKey.NobleKey.USER_COUPONINFO_ 
				+ goodsId + Constants.UNDERLINE + userId;
    	if ((jsonStr = redisAdapter.strGet(key)) != null) {
    		 if (StringUtils.isEmpty(jsonStr)) {
    			 return null;
    		 }
    		 couponInfo = JSONObject.parseObject(jsonStr, CouponInfo.class);
    	} else {
            //根据用户userId获取用户开通那些权限
           couponInfo = couponInfoMapper.getCouponInfoByUidAndGid(userId, goodsId);
           if (couponInfo == null) {
        	   redisAdapter.strSetexByNormal(key, RedisExpireTime.EXPIRE_DAY_1, "");
           } else {

        	   long currentTime = new Date().getTime();
               int expire = expireTime(couponInfo.getEffectivenessTime().getTime(), currentTime);
        	   redisAdapter.strSetexByNormal(key, expire, JSONObject.toJSONString(couponInfo));
           }
    	}
        return couponInfo;
    }
    
    /**
   	 * 获得过期时间
   	 * @param effectiveEndTime
   	 * @param currentTime
   	 * @return
   	 */
   	private static int expireTime(long effectiveEndTime, long currentTime) {
   		long tmpExpire = (effectiveEndTime - currentTime) / 1000;
   		int expire = 0;
   		if (tmpExpire > Integer.MAX_VALUE) {
   			expire = Integer.MAX_VALUE;
   		} else {
   			expire = (int) tmpExpire;
   		}
   		return expire;
   	}
}
