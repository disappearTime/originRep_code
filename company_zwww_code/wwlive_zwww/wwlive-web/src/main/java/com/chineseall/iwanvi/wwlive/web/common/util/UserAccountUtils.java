package com.chineseall.iwanvi.wwlive.web.common.util;

import com.chineseall.iwanvi.wwlive.common.cache.RedisClientAdapter;
import com.chineseall.iwanvi.wwlive.common.cache.RedisExpireTime;
import com.chineseall.iwanvi.wwlive.common.cache.RedisKey;
import com.chineseall.iwanvi.wwlive.dao.wwlive.OauthInfoMapper;
import com.chineseall.iwanvi.wwlive.dao.wwlive.UserInfoMapper;
import com.chineseall.iwanvi.wwlive.domain.wwlive.OauthInfo;
import org.apache.commons.lang.StringUtils;

import java.util.Map;

/**
 * Created by Niu Qianghong on 2017-07-04 0004.
 */
public class UserAccountUtils {

    /**
     * 通过userId获得token
     * @param userId
     * @param redisAdapter
     * @param userInfoMapper
     * @return
     */
    public static String getTokenByUserId(Long userId, RedisClientAdapter redisAdapter, UserInfoMapper userInfoMapper){
        String getKey = RedisKey.UID_TO_TOKEN_ + userId;
        String token = null;
        if(redisAdapter.existsKey(getKey)) {
            token = redisAdapter.strGet(getKey);
        } else {
            token = userInfoMapper.getTokenById(userId);
            redisAdapter.strSetexByNormal(getKey, RedisExpireTime.EXPIRE_DAY_10,token);
        }
        return token;
    }

    /**
     * 根据token获得userId
     * @param token
     * @param redisAdapter
     * @param userInfoMapper
     * @return
     */
    public static Long getUerIdByToken(String token, RedisClientAdapter redisAdapter, UserInfoMapper userInfoMapper){
        String getKey = RedisKey.TOKEN_TO_UID_ + token;
        Long userId = 0L;
        if(redisAdapter.existsKey(getKey)){
            userId = Long.valueOf(redisAdapter.strGet(getKey));
        } else {
            userId = userInfoMapper.getIdByToken(token);
            redisAdapter.strSetexByNormal(getKey, RedisExpireTime.EXPIRE_DAY_10, userId.toString());
        }
        return userId;
    }

    /**
     * 根据第三方类型和userId获得绑定信息
     * @param userId
     * @param oatuType
     * @param redisAdapter
     * @param oauthInfoMapper
     * @return
     */
    public static OauthInfo getOauthByTypeAndId(Long userId, Integer oatuType,
                                                RedisClientAdapter redisAdapter, OauthInfoMapper oauthInfoMapper){
        OauthInfo oauthInfo = null;
        String oauthKey = RedisKey.OAUTH_ + oatuType + "_" + userId;
        if(redisAdapter.existsKey(oauthKey)){
            Map<String, String> oauthMap = redisAdapter.hashGetAll(oauthKey);
            oauthInfo = OauthInfo.getFromMap(oauthMap);
        } else {
            oauthInfo = oauthInfoMapper.getByTypeAndUid(oatuType, userId);
            if (oauthInfo != null) {
                redisAdapter.hashMSet(oauthKey, oauthInfo.toStringMap());
                redisAdapter.expireKey(oauthKey, RedisExpireTime.EXPIRE_DAY_1);
            }
        }
        return oauthInfo;
    }
}
