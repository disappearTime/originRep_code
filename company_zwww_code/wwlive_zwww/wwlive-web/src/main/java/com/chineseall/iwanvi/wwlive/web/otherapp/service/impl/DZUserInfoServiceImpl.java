package com.chineseall.iwanvi.wwlive.web.otherapp.service.impl;

import com.chineseall.iwanvi.wwlive.common.cache.RedisClientAdapter;
import com.chineseall.iwanvi.wwlive.common.cache.RedisKey;
import com.chineseall.iwanvi.wwlive.common.constants.Constants;
import com.chineseall.iwanvi.wwlive.common.external.rongcloud.RongCloudFacade;
import com.chineseall.iwanvi.wwlive.common.tools.StrMD5;
import com.chineseall.iwanvi.wwlive.dao.wwlive.UserInfoMapper;
import com.chineseall.iwanvi.wwlive.domain.wwlive.UserInfo;
import com.chineseall.iwanvi.wwlive.web.common.helper.UserInfoHelper;
import com.chineseall.iwanvi.wwlive.web.common.util.DateTools;
import com.chineseall.iwanvi.wwlive.web.otherapp.service.DZUserInfoService;
import com.zw.zcf.util.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Map;

/**
 * Created by Niu Qianghong on 2017-08-24 0024.
 */
@Service
public class DZUserInfoServiceImpl implements DZUserInfoService {

    @Autowired
    private UserInfoMapper userInfoMapper;

    @Autowired
    private RedisClientAdapter redisAdapter;

    private Logger logger = Logger.getLogger(this.getClass());

    @Override
    public UserInfo addUser(Map dzUser) {
        logger.info("dz: 定制版用户信息为: " + dzUser);
        UserInfo user = new UserInfo();

        String loginId = dzUser.get("userId") + "_dz";
        user.setLoginId(loginId);

        user.setUserName((String) dzUser.get("nickname"));
        user.setHeadImg((String) dzUser.get("avatar"));
        user.setSex((Integer) dzUser.get("sex"));
        user.setBirthday(null);
        user.setZodiac("双鱼座");
        user.setAcctStatus(0);
        user.setAcctType(0);
        user.setOrigin(3); // 3-来自定制版

        try {
            String userName = user.getUserName();
            String token = RongCloudFacade
                    .getToken(loginId, StringUtils.isBlank(userName) ? "" : userName,"", 1);
            user.setRongToken(token);
        } catch (Exception e) {
            logger.error("新建定制版用户: 获取融云token时异常", e);
        }

        Date now = new Date();
        user.setCreateTime(now);
        user.setUpdateTime(now);
        user.setLoginOn(now);
        user.setVirtualCurrency(0L);
        user.setToken(Constants.getUUID());

        String account = getNewAccount();
        user.setAccount(account);

        String date = DateTools.formatDate(now, "MMdd");
        String password = account + new StringBuilder(date).reverse().toString();
        password = StrMD5.getInstance().getStringMD5(password);
        user.setPassword(password);

        logger.info("dz: 处理后的用户信息为: " + user);
        int result = userInfoMapper.insertUserInfo(user);
        return result == 1 ? user : null;
    }

    private String getNewAccount() {
        String loginId;
        if (redisAdapter.existsKey(RedisKey.NEXT_LOGINID)) {
            loginId = redisAdapter.strGet(RedisKey.NEXT_LOGINID);
            redisAdapter.strIncr(RedisKey.NEXT_LOGINID); // loginId + 1
        } else {
            Long lastLoginId = userInfoMapper.getLastId();
            loginId = (lastLoginId + 1) + "";
            redisAdapter.strSetByNormal(RedisKey.NEXT_LOGINID, (lastLoginId + 2) + "");
        }

        String prefix = "";
        for (int i = 0; i < UserInfoHelper.DEFAULT_NAME_LEN - loginId.length(); i++) {
            prefix += "0";
        }
        return prefix + loginId;
    }

    @Override
    public Integer getNameCnt(String nickname) {
        return userInfoMapper.findUserByNickname(nickname);
    }

    /**
     * 修改昵称, 性别, 生日, 头像
     * @param dzUser
     * @return
     */
    @Override
    public int modifyInfo(Map dzUser) {
        try {
            Long userId = MapUtils.getLong(dzUser, "zbUserId");
            Long birthday = (Long) dzUser.get("birthday");
            String userName = (String) dzUser.get("nickname");
            String avatar = (String) dzUser.get("avatar");
            Integer sex = (Integer) dzUser.get("sex");

            UserInfo userInfo = new UserInfo();
            userInfo.setUserId(userId);
            userInfo.setBirthday(birthday == null ? null : new Date(birthday));
            userInfo.setUserName(userName);
            userInfo.setHeadImg(avatar);
            userInfo.setSex(sex);
            return userInfoMapper.updateUserInfoById(userInfo);
        } catch (Exception e) {
            logger.error("根据定制版修改用户信息异常", e);
        }
        return 0;
    }
}
