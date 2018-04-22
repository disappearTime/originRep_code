package com.chineseall.iwanvi.wwlive.web.nobility.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.chineseall.iwanvi.wwlive.common.cache.RedisClientAdapter;
import com.chineseall.iwanvi.wwlive.common.cache.RedisExpireTime;
import com.chineseall.iwanvi.wwlive.common.cache.RedisKey;
import com.chineseall.iwanvi.wwlive.common.check.ResponseResult;
import com.chineseall.iwanvi.wwlive.common.check.ResultMsg;
import com.chineseall.iwanvi.wwlive.common.constants.Constants;
import com.chineseall.iwanvi.wwlive.dao.wwlive.*;
import com.chineseall.iwanvi.wwlive.domain.wwlive.*;
import com.chineseall.iwanvi.wwlive.web.common.enums.PayType;
import com.chineseall.iwanvi.wwlive.web.common.helper.AllChatroomsNoticeHelper;
import com.chineseall.iwanvi.wwlive.web.common.helper.RoleNobleHelper;
import com.chineseall.iwanvi.wwlive.web.common.helper.UserInfoHelper;
import com.chineseall.iwanvi.wwlive.web.common.pay.OutTradeNoUtil;
import com.chineseall.iwanvi.wwlive.web.nobility.common.NobilityCommon;
import com.chineseall.iwanvi.wwlive.web.nobility.controller.NobilityController;
import com.chineseall.iwanvi.wwlive.web.nobility.service.FreeSecretService;
import com.chineseall.iwanvi.wwlive.web.nobility.service.NobilityService;
import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service("freeSecretServiceImpl")
public class FreeSecretServiceImpl implements FreeSecretService{

    static final Logger logger = Logger.getLogger(NobilityController.class);

    @Autowired
    private RenewMapper renewMapper;

    @Autowired
    private RedisClientAdapter redisAdapter;

    @Autowired
    private UserInfoMapper userInfoMapper;

    @Autowired
    private RoleInfoMapper roleInfoMapper;

    @Autowired
    private LiveVideoInfoMapper liveVideoInfoMapper;

    @Override
    public int updateRenewOrder(HttpServletRequest request){
        int num = 0;
        String userId = request.getParameter("userid");
        String planid = request.getParameter("planid");
        String openid = request.getParameter("openid");
        String changetype = request.getParameter("changetype");//协议状态 ADD 为签约，DELETE 为取消签约
        String contractcode = request.getParameter("contractcode");
        String contractid = request.getParameter("contractid");

        //判断存不存在
        List<RenewInfo> renewInfoList = renewMapper.getRenewInfoByUserId(Long.parseLong(userId));

        if(renewInfoList.size()>0){
            RenewInfo renewInfo = new RenewInfo();
            renewInfo.setUserId(Long.valueOf(userId));
            if(changetype.equals("ADD")){
                renewInfo.setRenewStatus(0);//自动续费
                sendMsg (Long.valueOf(userId));
            }else{
                renewInfo.setRenewStatus(1);//取消自动续费
            }
            //更新协议状态
            renewMapper.updateRenewOrder(renewInfo);

        }else{
            RenewInfo renewInfo = new RenewInfo();
            renewInfo.setUserId(Long.valueOf(userId));
            if(changetype.equals("ADD")){
                renewInfo.setRenewStatus(0);//自动续费
                sendMsg (Long.valueOf(userId));
            }else{
                renewInfo.setRenewStatus(1);//取消自动续费
            }

            renewInfo.setUserId(Long.valueOf(userId));
            renewInfo.setPayType(2);//表名为微信支付
            renewInfo.setContractCode(Integer.parseInt(contractcode));
            renewInfo.setContractId(contractid);
            renewInfo.setOpenId(openid);
            renewInfo.setPlanid(Integer.parseInt(planid));
            //插入数据到续费表
            renewMapper.insertRenewOrder(renewInfo);
        }

        return num;
    }

    public void sendMsg (Long userId){
        String userName = "";
        String userKey = RedisKey.USER_INFO_ + userId;
        if (redisAdapter.existsKey(userKey)) {
            UserInfo u =
                    UserInfoHelper.getUserInfoFromCache(redisAdapter, userId, "userName");
            userName = (u == null) ? "" : u.getUserName();
        } else {
            UserInfo u =
                    UserInfoHelper.getAndCacheUserInfo(redisAdapter, userInfoMapper, userId);
            userName = (u == null) ? "" : u.getUserName();
        }
        JSONObject json = new JSONObject();
        json.put("userId",userId);
        json.put("userName",userName);
        json.put("lv", RoleNobleHelper.userRoleNobleLevel(redisAdapter, roleInfoMapper, userId));
        AllChatroomsNoticeHelper.sendMsg4AllChatrooms(liveVideoInfoMapper, new Integer(29), json.toString(), "");
    }
}