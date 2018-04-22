package com.chineseall.iwanvi.wwlive.web.standalone.service;

import com.chineseall.iwanvi.wwlive.domain.wwlive.OauthInfo;
import com.chineseall.iwanvi.wwlive.domain.wwlive.UserInfo;

import java.util.Map;

/**
 * Created by Niu Qianghong on 2017-06-27 0027.
 */
public interface UserAccountService {
    /**
     * 使用mac和imei注册用户
     * @return
     */
    Map<String,Object> register(String credential, String ip, Integer platform,
                                String version, Integer pushType, String deviceToken, String cnid);

    Map<String, Object> bindToWeChat(Long userId, String token, String code, String platform);

    Map<String,Object> bindToQQ(Long userId, String token, String openId, String accessToken, String sex, String avatar, String nickname);

    Map<String, Object> loginByWeChat(Long userId, String token, String code,
                                      Integer pushType, String deviceToken, Integer platform, String version, String cnid);

    Map<String, Object> loginByQQ(Long userId, String token, String openId,
                                  Integer pushType, String deviceToken, Integer platform, String version, String cnid);

    Map<String, Object> loginByNormal(Long userId, String token, String userName, String password,
                                      Integer pushType, String deviceToken, Integer platform, String version, String cnid);

    OauthInfo queryBound(int oauthWechat, Long userId);

    void updateDeviceToken(Integer pushType, Long userId, String deviceToken, Integer platform, String version, String cnid);

    UserInfo getAllInfoById(Long userId);
}
