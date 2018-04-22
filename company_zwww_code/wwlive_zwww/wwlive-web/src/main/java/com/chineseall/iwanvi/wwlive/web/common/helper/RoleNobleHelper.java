package com.chineseall.iwanvi.wwlive.web.common.helper;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.chineseall.iwanvi.wwlive.common.cache.RedisClientAdapter;
import com.chineseall.iwanvi.wwlive.common.cache.RedisExpireTime;
import com.chineseall.iwanvi.wwlive.common.cache.RedisKey;
import com.chineseall.iwanvi.wwlive.common.enums.DictInfoEnum;
import com.chineseall.iwanvi.wwlive.dao.wwlive.BaseDictInfoMapper;
import com.chineseall.iwanvi.wwlive.dao.wwlive.RoleInfoMapper;
import com.chineseall.iwanvi.wwlive.domain.wwlive.RoleInfo;

/**
 * 贵族信息
 * 
 * @author DIKEPU
 *
 */

public class RoleNobleHelper {

	/**
	 * 开通的贵族角色
	 * @param redisAdapter
	 * @param roleInfoMapper
	 * @param userId
	 * @return
	 */
	public static List<Integer> userRoleNobleLevels(RedisClientAdapter redisAdapter,
			RoleInfoMapper roleInfoMapper, Long userId) {
		if (redisAdapter == null || roleInfoMapper == null || userId == null) {
			return new ArrayList<Integer>();
		}
		String nobleKey = RedisKey.NobleKey.USER_ROLE_NOBLE_TITLES_ + userId;// 贵族key
		String levels = "";
		if ((levels = redisAdapter.strGet(nobleKey)) != null) {
			return JSONArray.parseArray(levels, Integer.class);
		} else {
			List<RoleInfo> roles = roleInfoMapper.findLevelsByUserId(userId);

			List<Integer> lvs = new ArrayList<Integer>();
			if (roles != null && !roles.isEmpty()) {
				long endTime = 0L;
				long currentTime = new Date().getTime();
				
				for (RoleInfo role : roles) {
					if (endTime == 0 
							&& role.getEffectiveEndTime() != null) {
						endTime = role.getEffectiveEndTime().getTime();
					}
					lvs.add(role.getRoleLevel());
				}

				/**
				 * 根据有效时间最短的贵族缓存
				 */
				int expire = expireTime(endTime, currentTime);
				if (expire != 0) {
					redisAdapter.strSetexByNormal(nobleKey,
							expire, JSONObject.toJSONString(lvs).toString());
				}
			} else {
				redisAdapter.strSetexByNormal(nobleKey,
						RedisExpireTime.EXPIRE_DAY_5, JSONObject.toJSONString(lvs).toString());
			}
			return lvs;
		}
	}

	/**
	 * 返回用户开通的贵族id集合
	 * @param redisAdapter
	 * @param roleInfoMapper
	 * @param userId
	 * @return
	 */
	public static List<Long> userRoleNobleIds(RedisClientAdapter redisAdapter,
			RoleInfoMapper roleInfoMapper, Long userId) {
		if (redisAdapter == null || roleInfoMapper == null || userId == null) {
			return new ArrayList<Long>();
		}
		String nobleKey = RedisKey.NobleKey.USER_ROLE_NOBLE_GOODSIDS_ + userId;// 贵族key
		String ids = "";
		if ((ids = redisAdapter.strGet(nobleKey)) != null) {
			return JSONArray.parseArray(ids, Long.class);
		} else {
			List<RoleInfo> roles = roleInfoMapper.findLevelsByUserId(userId);

			List<Long> lvs = new ArrayList<Long>();
			if (roles != null && !roles.isEmpty()) {
				long endTime = 0L;
				long currentTime = new Date().getTime();
				
				for (RoleInfo role : roles) {
					if (endTime == 0 
							&& role.getEffectiveEndTime() != null) {
						endTime = role.getEffectiveEndTime().getTime();
					}
					lvs.add(role.getGoodsId());
				}

				/**
				 * 根据有效时间最短的贵族缓存
				 */
				int expire = expireTime(endTime, currentTime);
				if (expire != 0) {
					redisAdapter.strSetexByNormal(nobleKey,
							expire, JSONObject.toJSONString(lvs).toString());
				}
			} else {
				redisAdapter.strSetexByNormal(nobleKey,
						RedisExpireTime.EXPIRE_DAY_5, JSONObject.toJSONString(lvs).toString());
			}
			return lvs;
		}
	}
	/**
	 * 用户角色，如果是非贵族则返回0。
	 * 如果是贵族，返回贵族等级
	 * @param redisAdapter
	 * @param roleInfoMapper
	 * @param userId
	 * @return
	 */
	public static Integer userRoleNobleLevel(RedisClientAdapter redisAdapter,
			RoleInfoMapper roleInfoMapper, Long userId) {
		if (redisAdapter == null || roleInfoMapper == null || userId == null) {
			return new Integer(0);
		}
		String nobleKey = RedisKey.NobleKey.USER_ROLE_NOBLE_ + userId;// 贵族key
		String level = "";
		if ((level = redisAdapter.strGet(nobleKey)) != null) {
			return Integer.valueOf(level);
		} else {
			RoleInfo lv = roleInfoMapper.findLevelByUserId(userId);
			long currentTime = new Date().getTime();
			if (lv != null 
					&& lv.getEffectiveEndTime() != null) {
				/**
				 * 缓存时间为贵族有效时间
				 */
				int expire = expireTime(lv.getEffectiveEndTime().getTime(), currentTime);
				if (expire > 0) {
					redisAdapter.strSetexByNormal(nobleKey,
							expire, lv.getRoleLevel() + "");
					return lv.getRoleLevel();
				}
			}
		}
		redisAdapter.strSetexByNormal(nobleKey,
				RedisExpireTime.EXPIRE_DAY_5, "0");
		return new Integer(0);
	}

	/**
	 * 某用户是否为贵族
	 * @param redisAdapter
	 * @param roleInfoMapper
	 * @param userId
	 * @return
	 */
	public static boolean isNoble(RedisClientAdapter redisAdapter,
			RoleInfoMapper roleInfoMapper, Long userId) {
		if (redisAdapter == null || roleInfoMapper == null || userId == null) {
			return false;
		}
		String nobleKey = RedisKey.NobleKey.USER_ROLE_NOBLE_ + userId;// 贵族key
		String level = "";
		if ((level = redisAdapter.strGet(nobleKey)) != null) {
			if (!"0".equals(level)) {
				return true;
			}
		} else {
			RoleInfo lv = roleInfoMapper.findLevelByUserId(userId);
			long currentTime = new Date().getTime();
			if (lv != null 
					&& lv.getEffectiveEndTime() != null) {
				
				//过期时间
				int expire = expireTime(lv.getEffectiveEndTime().getTime(), currentTime);
				if (expire > 0) {
					redisAdapter.strSetexByNormal(nobleKey,
							(int) expire, lv.getRoleLevel() + "");
					return true;
				}
			}
		}
		redisAdapter.strSetexByNormal(nobleKey,
				RedisExpireTime.EXPIRE_DAY_5, "0");
		return false;
	}

	public static String getNobleImg(DictInfoEnum dictInfo, 
			BaseDictInfoMapper baseDictInfoMapper, RedisClientAdapter redisAdapter) {
		String nobleImg = "";
		if ((nobleImg = redisAdapter.strGet(RedisKey.NobleKey.NOBLE_IMG_
				+ dictInfo.getCode())) != null) {
		} else {
			nobleImg = baseDictInfoMapper.getDictContentByCode(dictInfo
					.getCode());
			if (nobleImg == null) {
				nobleImg = "";
			}
			redisAdapter.strSetexByNormal(RedisKey.NobleKey.NOBLE_IMG_
					+ dictInfo.getCode(), RedisExpireTime.EXPIRE_DAY_180,
					nobleImg);
		}
		return nobleImg;
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

	/**
	 * 获得该用户的贵族的id
	 * @param redisAdapter
	 * @param roleInfoMapper
	 * @param userId
	 * @return
	 */
    public static Long userRoleNobleGoodsId(RedisClientAdapter redisAdapter,
                                             RoleInfoMapper roleInfoMapper, Long userId) {
        if (redisAdapter == null || roleInfoMapper == null || userId == null) {
            return new Long(0);
        }
        String nobleKey = RedisKey.NobleKey.USER_ROLE_ + userId;// 贵族key
        String goodsId = "";
        if ((goodsId = redisAdapter.hashGet(nobleKey, "goodsId")) != null) {
            return Long.valueOf(goodsId);
        } else {
            RoleInfo lv = roleInfoMapper.findHighLevelGoodsIdByUserId(userId);
            long currentTime = new Date().getTime();
            if (lv != null && lv.getEffectiveEndTime() != null) {
                /**
                 * 缓存时间为贵族有效时间
                 */
                int expire = expireTime(lv.getEffectiveEndTime().getTime(), currentTime);
                if (expire > 0) {
                    redisAdapter.hashMSet(nobleKey, lv.putFieldValueToMapAndDateString());
                    redisAdapter.expireKey(nobleKey,expire);
                    return lv.getGoodsId();
                }
            }
        }
        Map<String, Object> map = new HashMap<>();
        map.put("goodsId", "0");
        redisAdapter.hashMSet(nobleKey, map);
        redisAdapter.expireKey(nobleKey, RedisExpireTime.EXPIRE_DAY_5);
        return new Long(0);
    }
    
    /**
	 * 获得该用户的贵族的信息
	 * @param redisAdapter
	 * @param roleInfoMapper
	 * @param userId
	 * @return
     * @throws ParseException 
	 */
    public static RoleInfo userRoleInfoGoodsId(RedisClientAdapter redisAdapter,
                                             RoleInfoMapper roleInfoMapper, Long userId) throws ParseException {
        if (redisAdapter == null || roleInfoMapper == null || userId == null) {
            return new RoleInfo();
        }
        String nobleKey = RedisKey.NobleKey.USER_ROLE_ + userId;// 贵族key
        Map<String, String> nobleMap;
        RoleInfo info = new RoleInfo();
        if ((nobleMap = redisAdapter.hashGetAll(nobleKey)) != null && !nobleMap.isEmpty()) {
        	info.doStringMapToValue(nobleMap);
            return info;
        } else {
            RoleInfo lv = roleInfoMapper.findHighLevelGoodsIdByUserId(userId);
            long currentTime = new Date().getTime();
            if (lv != null && lv.getEffectiveEndTime() != null) {
                /**
                 * 缓存时间为贵族有效时间
                 */
                int expire = expireTime(lv.getEffectiveEndTime().getTime(), currentTime);
                if (expire > 0) {
                    redisAdapter.hashMSet(nobleKey, lv.putFieldValueToMapAndDateString());
                    redisAdapter.expireKey(nobleKey,expire);
                    return lv;
                }
            }
        }
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("roleId", 0L);
        map.put("userId", userId);
        map.put("goodsId", 0L);
        map.put("goodsName", "");
        map.put("roleLevel", 0);
        redisAdapter.hashMSet(nobleKey, map);
        redisAdapter.expireKey(nobleKey, RedisExpireTime.EXPIRE_DAY_5);
        return info;
    }
    public static Map<String, Object> getRoleInfoToMap(RoleInfo lv) {
        Map<String,Object> map = new HashMap<>();
        map.put("roleId", lv.getRoleId());
        map.put("userId", lv.getUserId());
        map.put("goodsId", lv.getGoodsId());
        map.put("goodsName", lv.getGoodsName());
        map.put("roleLevel", lv.getRoleLevel());
        map.put("roleStatus", lv.getRoleStatus());
        map.put("effectiveStartTime", lv.getEffectiveStartTime());
        map.put("effectiveEndTime", lv.getEffectiveEndTime());
        return map;
    }

}
