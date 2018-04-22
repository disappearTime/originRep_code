package com.chineseall.iwanvi.wwlive.web.video.service.impl;

import java.text.ParseException;
import java.util.Date;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.chineseall.iwanvi.wwlive.common.cache.RedisClientAdapter;
import com.chineseall.iwanvi.wwlive.common.cache.RedisExpireTime;
import com.chineseall.iwanvi.wwlive.common.cache.RedisKey;
import com.chineseall.iwanvi.wwlive.common.constants.Constants;
import com.chineseall.iwanvi.wwlive.common.exception.IWanviException;
import com.chineseall.iwanvi.wwlive.common.external.rongcloud.RongCloudFacade;
import com.chineseall.iwanvi.wwlive.dao.wwlive.UserInfoMapper;
import com.chineseall.iwanvi.wwlive.domain.wwlive.UserInfo;
import com.chineseall.iwanvi.wwlive.web.video.service.LoginService;
import com.chineseall.iwanvi.wwlive.web.common.util.DateTools;

@Service("loginService")
public class LoginServiceImpl implements LoginService {

	@Autowired
	private UserInfoMapper userInfoMapper;

    @Autowired
    private RedisClientAdapter redisAdapter;
    
    private Logger logger = Logger.getLogger(this.getClass());
    
	@Override
	@Transactional(propagation=Propagation.REQUIRED, isolation=Isolation.READ_COMMITTED)
	@Deprecated
	public Long saveUserInfo(String loginId, String userName, String headImg, int sex, int origin, Date birthday) {
		//1. 检查此用户是否存在 2.如果不存在给此用户注册信息

		UserInfo userInfo = null;
		String userLogin = RedisKey.USER_LOGIN_ID_ + loginId;
		if (redisAdapter.existsKey(userLogin)) {
			String userId = redisAdapter.strGet(userLogin);
			userId = userId.replaceAll("\"", "");
			userInfo = new UserInfo();
			userInfo.setLoginId(loginId);
			userInfo.setUserId(Long.valueOf(userId));
		} else {
			userInfo = userInfoMapper.findUserInfoByLoginId(loginId);
		}
		
		//发到redis中保存用户信息
		Date date = new Date();
		if (userInfo != null) {
			userInfo.setLoginOn(date);
			userInfo.setLoginId(loginId);
			userInfoMapper.updateUserInfoByLoginId(userInfo);
			redisAdapter.strSetByNormal(userLogin, userInfo.getUserId().toString());
			redisAdapter.expireKey(userLogin, RedisExpireTime.EXPIRE_DAY_7);
			return userInfo.getUserId();
		} else {
			//注册
			userInfo = new UserInfo();
			userInfo.setLoginId(loginId);
			userInfo.setUserName(userName);
			userInfo.setHeadImg(headImg);
			userInfo.setSex(sex);
			userInfo.setOrigin(origin);
			userInfo.setBirthday(birthday);
			
			userInfo.setAcctStatus(Constants._0);
			userInfo.setAcctType(Constants._0);
			//星座
			userInfo.setZodiac(DateTools.getZodiacByDate(birthday));
			userInfo.setCreateTime(date);
			userInfo.setUpdateTime(date);
			userInfo.setLoginOn(date);
			userInfo.setVersionOptimizedLock(0);
			String token = "";
			try {
				token = RongCloudFacade.getToken(loginId, StringUtils.isBlank(userName) ?  "": userName, 
						StringUtils.isBlank(headImg) ?  "": headImg, 1);
				userInfo.setRongToken(token);
			} catch (Exception e) {
				logger.error("获取融云token时异常");
			}
			
			if (userInfoMapper.insertUserInfo(userInfo) > 0) {
				String userInfoKey = RedisKey.USER_INFO_ + userInfo.getUserId();
				redisAdapter.hashMSet(userInfoKey, userInfo.putFieldValueToMap());
				redisAdapter.expireKey(userInfoKey, RedisExpireTime.EXPIRE_DAY_7);
			}
			return userInfo.getUserId();
		}
		
	}

	@Override
	public Long upsertUserInfo(UserInfo loginUser) {
		//1. 检查此用户是否存在 2.如果不存在给此用户注册信息
		if (loginUser.getUserId() != null) {
			loginUser.setLoginOn(new Date());
			if (loginUser.getOrigin() == null) {
				loginUser.setOrigin(0);
			}
			//0创新版 1中文书城 2直接注册  当origin为2时此用户用创新版账户登录过，所以不同步用户信息
			if (0 == loginUser.getOrigin()) {
				//2017-09-19 根据新需求 修改，如果已经注册过新用户则不同步头像信息
//				loginUser.setHeadImg(null);
				userInfoMapper.updateUserInfoByLoginId(loginUser);
			} else if (2 == loginUser.getOrigin()) {
				UserInfo u = new UserInfo();
				u.setLoginOn(new Date());
				u.setUserId(loginUser.getUserId());
				userInfoMapper.updateUserInfoByLoginId(u);
			}
		} else {
			fromCxUser(loginUser);
			setUserInfo(loginUser);
			userInfoMapper.insertUserInfo(loginUser);
		}
		
		setUserInfoRedisCache(loginUser);
		return loginUser.getUserId();
	}
	
	/**
	 * 缓存用户信息
	 * @param loginUser
	 */
	private void setUserInfoRedisCache(UserInfo loginUser) {
		String loginId = loginUser.getLoginId();
		String userLogin = RedisKey.USER_LOGIN_ID_ + loginId;
		redisAdapter.strSetByNormal(userLogin, loginUser.getUserId().toString());
		redisAdapter.expireKey(userLogin, RedisExpireTime.EXPIRE_DAY_7);
		
		String userInfoKey = RedisKey.USER_INFO_ + loginUser.getUserId();
		if (redisAdapter.existsKey(userInfoKey)) {//已注册过，获得该用户最新数据
			redisAdapter.hashMSet(userInfoKey, loginUser.putFieldValueToMap());
			try {
				loginUser.doStringMapToValue(redisAdapter.hashMGet(userInfoKey, "userId", "userName", "headImg", "rongToken"));
			} catch (ParseException e) {
				logger.error("转换用户信息时失败。");
				throw new IWanviException("转换用户信息时失败。");
			}
		} else {//没注册过插入数据
			redisAdapter.hashMSet(userInfoKey, loginUser.putFieldValueToMap());
		}
		//保存一周
		redisAdapter.expireKey(userInfoKey, RedisExpireTime.EXPIRE_DAY_7);
	}
	
	private void setUserInfo(UserInfo loginUser) {
		Date date = new Date();
		loginUser.setCreateTime(date);
		// 星座
		loginUser.setZodiac(DateTools.getZodiacByDate(loginUser.getBirthday()));
		loginUser.setUpdateTime(date);
		loginUser.setLoginOn(date);
		loginUser.setVersionOptimizedLock(0);
		if (loginUser.getUserName() == null) {
			loginUser.setUserName("");
		}
	}
	
	private void fromCxUser(UserInfo loginUser) {
		//注册
		loginUser.setOrigin(Constants.USER_INFO_ORIGIN_0);
		//0 正常
		loginUser.setAcctStatus(Constants._0);
		//0 普通用户
		loginUser.setAcctType(Constants._0);
		String token = "";
		try {
			//注册融云
			token = RongCloudFacade.getToken(loginUser.getLoginId(), loginUser.getUserName(), 
					loginUser.getHeadImg(), 1);
			loginUser.setRongToken(token);
		} catch (Exception e) {
			logger.error("注册融云token异常");
		}
		
	}
	
	
}
