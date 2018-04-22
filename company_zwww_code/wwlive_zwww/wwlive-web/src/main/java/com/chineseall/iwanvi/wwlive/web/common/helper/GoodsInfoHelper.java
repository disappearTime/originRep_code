package com.chineseall.iwanvi.wwlive.web.common.helper;

import java.text.ParseException;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.chineseall.iwanvi.wwlive.common.cache.RedisClientAdapter;
import com.chineseall.iwanvi.wwlive.common.cache.RedisExpireTime;
import com.chineseall.iwanvi.wwlive.common.cache.RedisKey;
import com.chineseall.iwanvi.wwlive.dao.wwlive.GoodsInfoMapper;
import com.chineseall.iwanvi.wwlive.domain.wwlive.GoodsInfo;

/**
 * 商品信息缓存到redis中
 * <p>
 * 对应的goods信息更新后请更新相应的redis信息，使其保持一致
 * @author DIKEPU
 * @since 2017-01-22 二期
 */
public class GoodsInfoHelper {

	private static final Logger LOGGER = Logger.getLogger(GoodsInfoHelper.class);
	
	/**
	 * 缓存goods信息
	 * @param redisAdapter
	 * @param goodsInfoMapper
	 * @param goodsId
	 * @return
	 */
	public static GoodsInfo getAndCacheGoodsInfo(RedisClientAdapter redisAdapter, 
			GoodsInfoMapper goodsInfoMapper, long goodsId) {
		if (redisAdapter == null 
				|| goodsInfoMapper == null || goodsId == 0) {
			return null;
		}
		GoodsInfo goods = goodsInfoMapper.findGoodsInfoById(goodsId);
		if (goods != null) {
            // 添加用户信息到redis中
            redisAdapter.hashMSet(RedisKey.GOODS_INFO_ + goodsId, goods.putFieldValueToStringMap());
    		redisAdapter.expireKey(RedisKey.GOODS_INFO_ + goodsId, RedisExpireTime.EXPIRE_DAY_7);
        } else {
        	LOGGER.error("商品：" + goodsId + "不存在。");
        }
		
		return goods;
	}

	/**
	 * 从缓存中获得缓存goods信息，如果不存在则从数据库获得并缓存
	 * @param redisAdapter
	 * @param goodsInfoMapper
	 * @param goodsId
	 * @return goodsInfo信息
	 */
	public static GoodsInfo getFromCacheIfNotExistsCacheGoodsInfo(RedisClientAdapter redisAdapter, 
			GoodsInfoMapper goodsInfoMapper, long goodsId) {
		if (redisAdapter == null 
				|| goodsInfoMapper == null || goodsId == 0) {
			return null;
		}
		String goodsKey = RedisKey.GOODS_INFO_ + goodsId;
		GoodsInfo goods = null;
        if (redisAdapter.existsKey(goodsKey)) {
			goods = getGoodsAllInfo(redisAdapter, goodsId);
        } else {
			goods = getAndCacheGoodsInfo(redisAdapter, goodsInfoMapper, goodsId);
        }
		return goods;
	}
	/**
	 * 从缓存获得商品信息
	 * @param redisAdapter
	 * @param goodsId
	 * @param fields
	 * @return
	 */
	public static GoodsInfo getGoodsInfo(RedisClientAdapter redisAdapter, 
			long goodsId, String... fields) {
		if (redisAdapter == null || goodsId == 0) {
			return null;
		}

		Map<String, String> goodsInfo = redisAdapter.hashMGet(RedisKey.GOODS_INFO_ + goodsId, fields);
		if (goodsInfo == null || goodsInfo.isEmpty()) {
			return null;
		}
		GoodsInfo goods = new GoodsInfo();
		try {
			goods.doStringMapToValue(goodsInfo);
		} catch (ParseException e) {
			e.printStackTrace();
        	LOGGER.error("商品转换异常(ParseException)：" + e.toString());
			return null;
        	
		}
		return goods;
	}

	/**
	 * 从缓存获得商品信息
	 * @param redisAdapter
	 * @param goodsId
	 * @param fields
	 * @return
	 */
	public static GoodsInfo getGoodsAllInfo(RedisClientAdapter redisAdapter, 
			long goodsId) {
		if (redisAdapter == null || goodsId == 0) {
			return null;
		}

		Map<String, String> goodsInfo = redisAdapter.hashGetAll(RedisKey.GOODS_INFO_ + goodsId);
		if (goodsInfo == null || goodsInfo.isEmpty()) {
			return null;
		}
		GoodsInfo goods = new GoodsInfo();
		try {
			goods.doStringMapToValue(goodsInfo);
		} catch (ParseException e) {
			e.printStackTrace();
        	LOGGER.error("商品转换异常(ParseException)：" + e.toString());
			return null;
		}
		return goods;
	}
	
	/**
	 * 获得贵族id
	 * @param redisAdapter
	 * @param goodsInfoMapper
	 * @return
	 */
	public static List<Long> getAllNobleIds(RedisClientAdapter redisAdapter, GoodsInfoMapper goodsInfoMapper) {
		String key = RedisKey.NobleKey.ALL_NOBLES_ID;
		String rs;
		if (StringUtils.isNotBlank((rs = redisAdapter.strGet(key)))) {
			return JSONArray.parseArray(rs, Long.class);
		} else {
			List<Long> ids = goodsInfoMapper.findNobleGoodsId();
			redisAdapter.strSetexByNormal(key, RedisExpireTime.EXPIRE_DAY_10, JSONObject.toJSONString(ids));
			return ids;
		}
	}
	
}
