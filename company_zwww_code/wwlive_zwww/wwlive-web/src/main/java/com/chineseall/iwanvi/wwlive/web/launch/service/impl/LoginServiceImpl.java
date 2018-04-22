package com.chineseall.iwanvi.wwlive.web.launch.service.impl;

import java.text.ParseException;
import java.util.Date;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.chineseall.iwanvi.wwlive.dao.wwlive.AnchorMapper;
import com.chineseall.iwanvi.wwlive.domain.wwlive.Anchor;
import com.chineseall.iwanvi.wwlive.web.launch.service.LoginService;
import com.chineseall.iwanvi.wwlive.common.cache.RedisClientAdapter;
import com.chineseall.iwanvi.wwlive.common.cache.RedisExpireTime;
import com.chineseall.iwanvi.wwlive.common.cache.RedisKey;
import com.chineseall.iwanvi.wwlive.common.exception.IWanviException;
import com.chineseall.iwanvi.wwlive.common.exception.LoginException;

@Service("launchLoginService")
public class LoginServiceImpl implements LoginService {

    private final Logger LOGGER = Logger
            .getLogger(this.getClass());
    
	@Autowired
	private AnchorMapper anchorMapper;

    @Autowired
    private RedisClientAdapter redisAdapter;
    
    /**
     * {@inheritDoc}
     */
    @Override
	public Anchor doLogin(String passport, String passwd) {
		String key = RedisKey.ANCHOR_PASSPORT_ + passport;
		Anchor tmp = null;
		if (redisAdapter.existsKey(key)) {
			String anchorId = redisAdapter.strGet(key);
			Map<String, String> tmpMap = redisAdapter.hashMGet(RedisKey.ANCHOR_INFO_ + anchorId, 
					"anchorId","rongToken", "acctStatus", "userName", "headImg", "roomNum", "passwd");
			if (tmpMap != null && !tmpMap.isEmpty() 
					&& StringUtils.isNotBlank(tmpMap.get("rongToken"))) {
				tmp = new Anchor();
				try {
					tmp.doStringMapToValue(tmpMap);
				} catch (ParseException e) {
		            LOGGER.info("获得主播信息异常:" + e.toString());
		            throw new IWanviException("获得主播信息异常");
				}
			} else {
				redisAdapter.delKeys(RedisKey.ANCHOR_INFO_ + anchorId);
				tmp = anchorMapper.findAnchorByPassport(passport);
			}
			
		} else {
			tmp = anchorMapper.findAnchorByPassport(passport);
		}
		
		if(tmp == null ) {
		    LOGGER.info("登录失败,登录名不存在,登录名:" + passport);
		    throw new LoginException("用户名或密码错误");
		    
		}
        
		if(tmp.getAcctStatus() != 0){
		    //账户被禁用
		    LOGGER.info("登录失败, 账号被禁用, 登录名:" + passport);
            throw new LoginException("账号已被禁用");
		}
		
        if (!passwd.equals(tmp.getPasswd())) {
            LOGGER.info("登录失败,登录名和密码不匹配,登录名:" + passport);
            throw new LoginException("用户名或密码错误");
        }

        Date loginOn = new Date();
        
        Anchor anchor = getLoginReturnAnchor(tmp, loginOn);
        Long id = anchor.getAnchorId();
        anchorMapper.updateAnchorLogOnTime(id, loginOn);
        
		return anchor;
	}
	
	private Anchor getLoginReturnAnchor(Anchor tmp, Date loginOn) {
		if ( tmp == null) {
			return new Anchor();
		}
		Anchor anchor = new Anchor();
		anchor.setAnchorId(tmp.getAnchorId());
		anchor.setRongToken(tmp.getRongToken() == null ? "" : tmp.getRongToken());
		anchor.setUserName(tmp.getUserName());
		anchor.setHeadImg(tmp.getHeadImg());
		anchor.setRoomNum(tmp.getRoomNum());
		if (!redisAdapter.existsKey(RedisKey.ANCHOR_INFO_ + tmp.getAnchorId())) {
	        redisAdapter.hashMSet(RedisKey.ANCHOR_INFO_ + tmp.getAnchorId(), tmp.putFieldValueToStringMap());
		} else {
			tmp.setLoginOn(loginOn);
			tmp.setUpdateTime(loginOn);
			redisAdapter.hashMSet(RedisKey.ANCHOR_INFO_ + tmp.getAnchorId(), tmp.putFieldValueToStringMap());
		}
		String key = RedisKey.ANCHOR_PASSPORT_ + tmp.getPassport();
		if (!redisAdapter.existsKey(key)) {
            redisAdapter.strSetByNormal(key, tmp.getAnchorId().toString());
        }
        redisAdapter.expireKey(RedisKey.ANCHOR_INFO_ + tmp.getAnchorId(), RedisExpireTime.EXPIRE_DAY_30);
        redisAdapter.expireKey(key, RedisExpireTime.EXPIRE_DAY_30);
		return anchor;
	}
	
}
