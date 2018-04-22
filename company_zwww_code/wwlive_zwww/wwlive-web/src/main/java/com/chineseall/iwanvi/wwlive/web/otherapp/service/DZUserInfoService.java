package com.chineseall.iwanvi.wwlive.web.otherapp.service;

import com.chineseall.iwanvi.wwlive.domain.wwlive.UserInfo;

import java.util.Map;

/**
 * Created by Niu Qianghong on 2017-08-24 0024.
 */
public interface DZUserInfoService {
    UserInfo addUser(Map dzUser);

    Integer getNameCnt(String nickname);

    int modifyInfo(Map dzUser);
}
