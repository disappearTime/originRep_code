package com.chineseall.iwanvi.wwlive.web.video.service.impl;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.chineseall.iwanvi.wwlive.common.enums.DictInfoEnum;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.chineseall.iwanvi.wwlive.common.cache.RedisClientAdapter;
import com.chineseall.iwanvi.wwlive.common.cache.RedisExpireTime;
import com.chineseall.iwanvi.wwlive.common.cache.RedisKey;
import com.chineseall.iwanvi.wwlive.common.constants.Constants;
import com.chineseall.iwanvi.wwlive.dao.wwlive.CouponInfoMapper;
import com.chineseall.iwanvi.wwlive.dao.wwlive.DiscountInfoMapper;
import com.chineseall.iwanvi.wwlive.dao.wwlive.GoodsInfoMapper;
import com.chineseall.iwanvi.wwlive.dao.wwlive.RoleInfoMapper;
import com.chineseall.iwanvi.wwlive.domain.wwlive.CouponInfo;
import com.chineseall.iwanvi.wwlive.domain.wwlive.DiscountInfo;
import com.chineseall.iwanvi.wwlive.domain.wwlive.GoodsInfo;
import com.chineseall.iwanvi.wwlive.domain.wwlive.RoleInfo;
import com.chineseall.iwanvi.wwlive.web.common.helper.GoodsInfoHelper;
import com.chineseall.iwanvi.wwlive.web.common.helper.RoleNobleHelper;
import com.chineseall.iwanvi.wwlive.web.video.service.GoodsInfoService;

@Service
public class GoodsInfoServiceImpl implements GoodsInfoService {

    private static final Logger LOGGER = Logger.getLogger(GoodsInfoServiceImpl.class);

	@Autowired
	private GoodsInfoMapper goodsInfoMapper;

	@Autowired
	private RedisClientAdapter redisAdapter;

	@Autowired
	private CouponInfoMapper couponInfoMapper;

	@Autowired
	private DiscountInfoMapper discountInfoMapper;

    @Autowired
    private RoleInfoMapper roleInfoMapper;

	@Override
	public Map<String, Object> getShelfGoodsList(String userId) throws ParseException {
		// 从缓存中获取礼品列表
		String goodsListJson = redisAdapter.strGet(RedisKey.GOODS_LIST);
		if (goodsListJson == null || "".equals(goodsListJson)) {
			List<GoodsInfo> onOfferGoods = goodsInfoMapper.getOnOfferGoods();
			goodsListJson = JSON.toJSONString(onOfferGoods);
			redisAdapter.strSetexByNormal(RedisKey.GOODS_LIST, 
					RedisExpireTime.EXPIRE_DAY_30, goodsListJson);
		}
		
		StringBuilder sb = null;
		if (goodsListJson.startsWith("\"")) {
			goodsListJson = StringUtils.replace(goodsListJson, "\\", "");
			sb = new StringBuilder(goodsListJson);
			sb.replace(0, 1, "");
			sb.replace(sb.length() - 1, sb.length(), "");
		} else {
			sb = new StringBuilder(goodsListJson);
		}
		List<JSONObject> goodsList = (List<JSONObject>) JSON.parse(sb.toString());
		for (JSONObject goods : goodsList) {
			Integer priceStr = (Integer) goods.get("goodsPrice");
			if (priceStr != null) {
				Double price = Integer.valueOf(priceStr) / 100.0;
				goods.put("goodsPrice", price);
			}
			goods.put("type",0);
		}
		Map<String, Object> data = new HashMap<>();
		data.put("goodsList", goodsList);
        List<JSONObject> nobleGoods = getNobleGoods(userId);
        if(nobleGoods == null) {
            data.put("nobleList", new ArrayList<JSONObject>());
        } else {
        	data.put("nobleList", nobleGoods);
        }
		return data;
	}

	/**
	 * 
	 * @param userId
	 * @return 
	 */
	public Map<String, Object> findNobles(Long userId) {
		List<GoodsInfo> goodsInfoList = new ArrayList<GoodsInfo>();
		if (redisAdapter.existsKey(RedisKey.NobleKey.NOBILITY_ALLCAVALIER)) {
			goodsInfoList = JSONArray.parseArray(
					redisAdapter.strGet(RedisKey.NobleKey.NOBILITY_ALLCAVALIER),
					GoodsInfo.class);
		} else {
			// 根据用户userId获取用户开通那些权限
			goodsInfoList = goodsInfoMapper.getGoodsListByType(7);// 7为贵族称号
			if (goodsInfoList != null && goodsInfoList.size() > 0) {
				// 写入缓存
				redisAdapter.strSetexByNormal(RedisKey.NobleKey.NOBILITY_ALLCAVALIER,
						RedisExpireTime.EXPIRE_DAY_30,
						JSONObject.toJSONString(goodsInfoList));
			}
		}
		/*
		 * 如果用户开通了骑士，则根据不同的角色获得不同的优惠权限
		 * 如果没开通权限则返回默认的优惠权限
		 */
		List<Map<String, Object>> goodsList = new ArrayList<Map<String, Object>>();
		String selectedKey = RedisKey.NobleKey.NOBLE_SELECTED;
		String strId = redisAdapter.strGet(selectedKey);
		for (GoodsInfo goods : goodsInfoList) {
			Map<String, Object> goodsMap = new HashMap<String, Object>();

			getAllDiscount4Coupon(userId, goods, goodsMap);
			goodsMap.put("isVirtual", goods.getIsVirtual() == null ? 1 : goods.getIsVirtual());
			//选中标志
			if (StringUtils.isBlank(strId)) {
				strId = goods.getGoodsId() + "";
				redisAdapter.strSetByNormal(selectedKey, strId);
			}
			if (strId.equals(goods.getGoodsId() + "")) {
				goodsMap.put("selected", 0);
			} else {
				goodsMap.put("selected", 1);
			}
			goodsMap.putAll(goods.putFieldValueToNotNullValueMap());
			goodsMap.put("goodsPrice", goods.getGoodsPrice() / 100);
			goodsList.add(goodsMap);
		}
		Map<String, Object> result = new HashMap<String, Object>();
		result.put("goodsList", goodsList);
        return result;
	}

	private List<DiscountInfo> getDiscountInfo(Long goodsId) {
		String jsonStr = null;
		List<DiscountInfo> discountInfoList = null;
		if ((jsonStr = redisAdapter.strGet(RedisKey.NobleKey.NOBILITY_DISCOUNT_ + goodsId)) != null) {
			discountInfoList = JSONArray.parseArray(jsonStr, DiscountInfo.class);
		} else {
			discountInfoList = discountInfoMapper
					.findDiscountInfosByGoodsId(goodsId);
			// 加入缓存
			if (discountInfoList != null) {
				redisAdapter.strSetexByNormal(
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
	 * 有优惠券
	 * @param goodsId
	 * @param goodsMap
	 */
	private void getAllDiscount4Coupon(Long userId, GoodsInfo goods, Map<String, Object> goodsMap) {
		List<DiscountInfo> discountInfoList = getDiscountInfo(goods.getGoodsId());
		if (discountInfoList == null) {
			return;
		}
        //用户未使用的优惠券
        CouponInfo couponInfo = getCouponInfoByUidAndGid(userId, goods.getGoodsId());
        long balance = 0L;
        if (couponInfo != null && couponInfo.getCouponValue() != null) {
    		goodsMap.put("couponValue", couponInfo.getCouponValue() / 100);
    		if ((balance = goods.getGoodsPrice() - couponInfo.getCouponValue()) <= 10000000) {//小于10万
        		goodsMap.put("balance", balance / 100);
    		} else {
				goodsMap.put("balance", balance);
			}
			goods.setIsVirtual(1);
        } else {
        	goodsMap.put("balance", balance);
			goods.setIsVirtual(1);
			if("temple".equals(goods.getSpecial())) {
				goods.setIsVirtual(0);
			}
        }

		for (DiscountInfo discountInfo : discountInfoList) {
			long discountType = discountInfo.getDiscountType();
			
			if (couponInfo == null) {//没有就认为是初次开通，初次开通就返回初次优惠券
				goodsMap.put("firstBuy", 0);//firstBuy 0首次 1非首次
				if (discountInfo.getDiscountType() == 1) {//初次抵用券
					goodsMap.put("voucher", discountInfo.getDiscountPrice() / 100);
	            }
	            if (discountInfo.getDiscountType() == 2) {//初次返钻石
					goodsMap.put("diamond", discountInfo.getDiscountPrice() / 100);
	            }

				if (discountType == 3) {//抵用券返钻石
					goodsMap.put("secondDiamond", discountInfo.getDiscountPrice() / 100);
	            }
	            if (discountType == 4) {//再次抵用券
					goodsMap.put("secondVoucher", discountInfo.getDiscountPrice() / 100);
					goodsMap.put("secondGoodsPrice", (goods.getGoodsPrice() - discountInfo.getDiscountPrice()) / 100);
	            }
			} else {
				goodsMap.put("firstBuy", 1);
				if (discountType == 3) {//抵用券返钻石
					goodsMap.put("diamond", discountInfo.getDiscountPrice() / 100);
                }
                if (discountType == 4) {//再次抵用券
					goodsMap.put("voucher", discountInfo.getDiscountPrice() / 100);
                }
			}
			
           
		}

	}
	
	/**
	 * 
	 * @param userId
	 * @param goodsId
	 * @return
	 */
    private CouponInfo getCouponInfoByUidAndGid(Long userId, Long goodsId){
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
	
	public Map<String, String> getShelfBarrage () {
		Map<String, String> map = null;
		try {
			map = redisAdapter.hashMGet(RedisKey.BARRAGE, "goodsId", "goodsImg", "goodsPrice", "goodsName", "goodsType");
			if(map.isEmpty()) {
				map = goodsInfoMapper.getShelfBarrage();
				redisAdapter.hashMSet(RedisKey.BARRAGE,map);
				redisAdapter.expireKey(RedisKey.BARRAGE,RedisExpireTime.EXPIRE_DAY_7);
			}

		}catch (Exception e) {
			LOGGER.error("barrage error：",e);
		}

		return map;
	}

	/**
	 * 获得贵族礼物
	 * @param userId 用户id
	 * @throws ParseException 
	 */
	// TODO public ---> private
	public List<JSONObject> getNobleGoods(String userIdStr) throws ParseException {
        //1.获得该贵族的专属礼物  2.获得不含专属礼物的礼物列表
        long userId = 0;
        if(StringUtils.isNotEmpty(userIdStr)) {
        	userId = Long.valueOf(userIdStr);
        }
        List<JSONObject> nobleList = new ArrayList<JSONObject>();

        RoleInfo info = RoleNobleHelper.userRoleInfoGoodsId(redisAdapter, roleInfoMapper, userId);
    	
        //专属礼物
    	nobleList = getExclusiveRoleGoods(userId);
        if(!(info != null && info.getGoodsId() != null
        		&& info.getGoodsId() > 0)) {
        	info = new RoleInfo();
        	info.setRoleLevel(0);
        	info.setGoodsId(0L);
        }
    	//获得所有贵族礼物
    	getAllNobleGoods(info, nobleList);
        if (nobleList != null && !nobleList.isEmpty()) {
        	Collections.sort(nobleList, new Comparator<JSONObject>() {
				@Override
				public int compare(JSONObject o1, JSONObject o2) {
					if ("rocket".equals(o1.getString("special")) 
							|| "rocket".equals(o2.getString("special"))) {

						if (!"rocket".equals(o1.getString("special"))) {
							return 10;
						}else if ("rocket".equals(o1.getString("special"))) {
							return -10;
						}
					}
					int isUse1 = (o1.getInteger("isUse") == null ? 1 : o1.getInteger("isUse"));//0可用
					int orderRow1 = (o1.getInteger("orderRow") == null ? 99999 : o1.getInteger("orderRow"));//默认99999 越靠前越小
					int isUse2 = (o2.getInteger("isUse") == null ? 1 : o2.getInteger("isUse"));
					int orderRow2 = (o2.getInteger("orderRow") == null ? 99999 : o2.getInteger("orderRow"));
					if (isUse1 == isUse2) {
						return (orderRow1 - orderRow2);
					}
					return (isUse1 - isUse2);
				}
        		
			});
        }
        return nobleList;
	}
	
	/**
	 * 获得贵族专属礼物（包含用户可用和不可用专属礼物）
	 * @param userId
	 * @return 专属礼物
	 */
	private List<JSONObject> getExclusiveRoleGoods(Long userId) {
		// 专属贵族礼物 ids
        List<JSONObject> exclusiveList = new ArrayList<JSONObject>();
		List<Long> roleIds = RoleNobleHelper.userRoleNobleIds(redisAdapter, roleInfoMapper, userId);
		List<Long> nobleIds = GoodsInfoHelper.getAllNobleIds(redisAdapter, goodsInfoMapper);
		String exclusiveNobleJson = "";
        for (long nobleId : nobleIds) {
        	if (roleIds != null && !roleIds.isEmpty() && roleIds.contains(nobleId)) {//拥有
                exclusiveNobleJson = getExclusiveNobleJson(nobleId, true);
        	} else {//不拥有
                exclusiveNobleJson = getExclusiveNobleJson(nobleId, false);
        	}
            if (StringUtils.isNotBlank(exclusiveNobleJson)) {
            	exclusiveList.addAll(JSONArray.parseArray(exclusiveNobleJson, JSONObject.class));
            }
        }
    	rebuildNobleGoods(exclusiveList);
    	return exclusiveList;
	}

	/**
	 * 获得所有的礼物信息
	 * @param info
	 * @param exclusiveList
	 */
	private void getAllNobleGoods(RoleInfo info, List<JSONObject> exclusiveList) {
		List<JSONObject> nobleList = getRoleGoods(info.getRoleLevel(), info.getGoodsId());
		rebuildNobleGoods(nobleList);
		if (nobleList != null) {
			exclusiveList.addAll(nobleList);
		}
        return;
	}
	
	/**
	 * 获得专属礼物
	 * @param roleId
	 * @param isOwner true拥有该专属礼物 false不拥有
	 * @return
	 */
	private String getExclusiveNobleJson(long roleId, boolean isOwner) {
		 String exclusiveKey = RedisKey.NobleKey.NOT_OWN_EXCLUSIVE_NOBLE_JSON_ + roleId;
		 if (isOwner) {
			 exclusiveKey = RedisKey.NobleKey.OWN_EXCLUSIVE_NOBLE_JSON_ + roleId;
		 }
         String exclusiveNobleJson = redisAdapter.strGet(exclusiveKey);
         if(StringUtils.isEmpty(exclusiveNobleJson)) {
             List<Map<String, Object>> nobleGoodsList = 
            		 goodsInfoMapper.findExclusiveByNobleId(roleId, isOwner);
			 for(Map<String, Object> map : nobleGoodsList) {
                 String nobleName = MapUtils.getString(map, "nobleName");
                 map.put("nobleName",nobleName.substring(0, 2) + "专属");
             }
             exclusiveNobleJson = JSON.toJSONString(nobleGoodsList);
             if (StringUtils.isEmpty(exclusiveNobleJson)) {
            	 exclusiveNobleJson = "";
             }
             redisAdapter.strSetexByNormal(exclusiveKey, RedisExpireTime.EXPIRE_DAY_30, exclusiveNobleJson);
         }
         
         return exclusiveNobleJson;
	}
	
	/**
	 * 获得某个角色的贵族礼物
	 * @param nobleLevel 角色等级
	 * @param roleId 角色id
	 * @return
	 */
	private List<JSONObject> getRoleGoods(int nobleLevel, long roleId) {
        String nobleJson = redisAdapter.strGet(RedisKey.NobleKey.NOBLE_JSON_ + roleId);
        if(StringUtils.isEmpty(nobleJson)) {
            List<Map<String, Object>> nobleGoodsList = goodsInfoMapper.findNobleGoodsByGrade(nobleLevel);// 0基本 1礼物 2折扣 3专属礼物
            for(Map<String, Object> map : nobleGoodsList) {
                String nobleName = MapUtils.getString(map, "nobleName");
                map.put("nobleName",nobleName.substring(0, 2) + "专属");
            }
            nobleJson = JSON.toJSONString(nobleGoodsList);
            redisAdapter.strSetexByNormal(RedisKey.NobleKey.NOBLE_JSON_ + roleId, RedisExpireTime.EXPIRE_DAY_30, nobleJson);
        }

        List<JSONObject> nobleList = JSONArray.parseArray(nobleJson, JSONObject.class);
    	return nobleList;
	}
	
	/**
	 * 计算贵族礼物价格
	 * @param list 遍历计算价格的贵族集合
	 */
	private void rebuildNobleGoods(List<JSONObject> list) {
        if(list != null) {
    		for (JSONObject goods : list) {
    			Integer priceStr = (Integer) goods.get("goodsPrice");
    			if (priceStr != null) {
    				Double price = Integer.valueOf(priceStr) / 100.0;
    				goods.put("goodsPrice", price);
    			}
				goods.put("type",1);
    		}
        } else {
        	list = new ArrayList<JSONObject>();
        }
	}
	
	public Map<String, Object> getShelfNobleList() {
        Map<String,Object> map = new HashMap<String, Object>();
        List<Map<String, Object>> nobleSpecial =null;
        String nobleListJson = redisAdapter.strGet(RedisKey.NOBLE_JSON_STRING);
        if(StringUtils.isEmpty(nobleListJson)) {
            nobleSpecial = goodsInfoMapper.getNobleSpecial();
            nobleListJson = JSON.toJSONString(nobleSpecial);
            redisAdapter.strSetexByNormal(RedisKey.NOBLE_JSON_STRING, RedisExpireTime.EXPIRE_DAY_7, nobleListJson);
        }
        List<JSONObject> nobleList = JSONArray.parseArray(nobleListJson, JSONObject.class);
        map.put("specialList", nobleList);
        return map;
    }

	public Map<String, Object> getShelfNobleById(String goodsId) {
		Map<String,Object> map = new HashMap<String, Object>();
		try {
			Map<String, String> nobleSpecial = redisAdapter.hashGetAll(RedisKey.NOBLE_+goodsId);
			if(nobleSpecial == null || nobleSpecial.isEmpty()) {
				GoodsInfo info = GoodsInfoHelper.getFromCacheIfNotExistsCacheGoodsInfo(redisAdapter, goodsInfoMapper, Long.valueOf(goodsId));
				nobleSpecial = goodsInfoMapper.getNobleSpecialByGoodsId(info.getSpecial());
				if(nobleSpecial != null && nobleSpecial.size() > 0) {
					redisAdapter.hashMSet(RedisKey.NOBLE_+goodsId,nobleSpecial);
					redisAdapter.expireKey(RedisKey.NOBLE_,RedisExpireTime.EXPIRE_DAY_1);
				}
			}
			map.put("nobleSpecial", nobleSpecial);
		} catch (Exception e) {
			
		}
		return map;
	}
	
}
