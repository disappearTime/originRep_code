package com.chineseall.iwanvi.wwlive.web.launch.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONObject;
import com.chineseall.iwanvi.wwlive.common.cache.RedisClientAdapter;
import com.chineseall.iwanvi.wwlive.common.cache.RedisKey;
import com.chineseall.iwanvi.wwlive.common.constants.Constants;
import com.chineseall.iwanvi.wwlive.common.exception.IWanviException;
import com.chineseall.iwanvi.wwlive.common.external.rongcloud.RongCloudFacade;
import com.chineseall.iwanvi.wwlive.common.external.rongcloud.domain.TxtMessage;
import com.chineseall.iwanvi.wwlive.dao.wwlive.LiveAdminMapper;
import com.chineseall.iwanvi.wwlive.dao.wwlive.UserInfoMapper;
import com.chineseall.iwanvi.wwlive.domain.wwlive.LiveAdmin;
import com.chineseall.iwanvi.wwlive.domain.wwlive.UserInfo;
import com.chineseall.iwanvi.wwlive.web.common.helper.LiveAdminHelper;
import com.chineseall.iwanvi.wwlive.web.common.helper.UserInfoHelper;
import com.chineseall.iwanvi.wwlive.web.launch.service.AdminService;

@Service
public class AdminServiceImpl implements AdminService{
    
    static final Logger LOGGER = Logger.getLogger(AdminServiceImpl.class);

    @Autowired
    private LiveAdminMapper adminMapper;
    
    @Autowired
    private UserInfoMapper userMapper;

    @Autowired
    private RedisClientAdapter redisAdapter;
    
    @Override
    public Map<String, Object> getListByAnchorId(Long anchorId, Integer pageNo, Integer pageSize) {
        int startRow = (pageNo - 1) * pageSize;
        List<Map<String, Object>> adminList = adminMapper.getListByAnchorId(anchorId, startRow, pageSize);
        Map<String, Object> data = new HashMap<>();
        data.put("adminList", adminList);
        return data;
    }

    @Override
    public Map<String, Object> remove(String chatRoomId, Long anchorId, Long userId) {
        
        Map<String, Object> data = new HashMap<>();
        
        if(!LiveAdminHelper.isAdmin(redisAdapter, adminMapper, anchorId, userId)){
            data.put("result", 1);
            return data;
        }
        
        String userName = null;
        String key = RedisKey.USER_INFO_ + userId;
        String loginId = "";
        if (!redisAdapter.existsKey(key)) {
            UserInfo user = UserInfoHelper.getAndCacheUserInfo(redisAdapter, userMapper, userId);
            if (user == null || user.getUserId() == null) {
                throw new IWanviException("此用户不存在！");
            }
            userName = user.getUserName();
            loginId = user.getLoginId().toString();
        } else {
        	userName = redisAdapter.hashGet(key, "userName");
        	loginId = redisAdapter.hashGet(key, "loginId");
        }
        
        int result = adminMapper.updateAdminStatus(anchorId, userId, new Integer(1));
        //清除缓存信息
        String adminKey = RedisKey.LIVE_ADMIN_INFO_ + anchorId
                + Constants.UNDERLINE + userId;
        redisAdapter.delKeys(adminKey);
        
        data.put("result", result);
        
        noticeRemoveAdmin(chatRoomId, anchorId, userId, userName, loginId);
        return data;
    }

    @Override
    @Transactional
    public Map<String, Object> set(String chatRoomId, Long anchorId, Long userId) {
        
        String userName = null;
        String key = RedisKey.USER_INFO_ + userId;
        String loginId = "";// 用于融云消息的dataextra部分
    	if (!redisAdapter.existsKey(key)) {
    		UserInfo user = UserInfoHelper.getAndCacheUserInfo(redisAdapter, userMapper, userId);
    		if (user == null || user.getUserId() == null) {
    	        throw new IWanviException("此用户不存在！");
    		} else if(user.getAcctType() == 1){
    		    throw new IWanviException("超管用户不能设置为房管!");
    		}
    		userName = user.getUserName();
    		loginId = user.getLoginId().toString();
    	} else{
    	    Map<String, String> userInfo = redisAdapter.hashMGet(key, "acctType", "userName", "loginId");
    	    userName = userInfo.get("userName");
    	    loginId = userInfo.get("loginId");
    	    if("1".equals(userInfo.get("acctType"))){
    	        throw new IWanviException("超管用户不能设置为房管!");
    	    }    	    
    	}
    	
    	Map<String, Object> data = new HashMap<>();
        int result = upsertLiveAdmin(anchorId, userId);
        data.put("result", result);
        //发出设置房管通知
        noticeSetAdmin(chatRoomId, anchorId, userId, userName, loginId);
        return data;
    }
    
    /**
     * 通知设置房管
     */
    private void noticeSetAdmin(String chatRoomId, Long anchorId, Long userId, String userName, String loginId) {
        try {
            List<String> chatIds = new ArrayList<String>();
            chatIds.add(chatRoomId);
            TxtMessage tx = new TxtMessage("");
            JSONObject json = new JSONObject();
            json.put("dataType", "14");
            json.put("dataValue", "");
            json.put("dataExtra", "{\"userId\":\"" + userId + "\", \"userName\":\"" + userName 
                    + "\", \"loginId\":\"" + loginId + "\"}");
            tx.setExtra(json.toJSONString());
            RongCloudFacade.publishChatroomMessage(anchorId.toString(), chatIds, tx, Constants.FORMAT_JSON);
        } catch (Exception e) {
            LOGGER.error("通知失败，ChatroomId：" + chatRoomId);
        }
    }
    
    
    /**
     * 通知解除房管
     */
    private void noticeRemoveAdmin(String chatRoomId, Long anchorId, Long userId, String userName, String loginId) {
        try {
            List<String> chatIds = new ArrayList<String>();
            chatIds.add(chatRoomId);
            TxtMessage tx = new TxtMessage("");
            JSONObject json = new JSONObject();
            json.put("dataType", "15");
            json.put("dataValue", "");
            json.put("dataExtra", "{\"userId\":\"" + userId + "\", \"userName\":\"" + userName + "\", \"loginId\":\""
                    + loginId + "\"}");
            tx.setExtra(json.toJSONString());
            RongCloudFacade.publishChatroomMessage(anchorId.toString(), chatIds, tx, Constants.FORMAT_JSON);
        } catch (Exception e) {
            LOGGER.error("通知失败，ChatroomId：" + chatRoomId);
        }
    }
    
    /**
     * 增加房管，如果此用户被删除，则重新更新用户的状态为正常
     * <p/>
     * 0正常，1解除
     * @param anchorId
     * @param userId
     * @return
     */
    private int upsertLiveAdmin(Long anchorId, Long userId) {
    	String adminKey = RedisKey.LIVE_ADMIN_INFO_ + anchorId + Constants.UNDERLINE + userId;
    	int status = 0;
    	if (redisAdapter.existsKey(adminKey)){
    		String adminStatus = redisAdapter.hashGet(adminKey, "adminStatus");
    		String adminId = redisAdapter.hashGet(adminKey, "adminId");
    		
    		if ("0".equals(adminStatus) && StringUtils.isNotBlank(adminId)) {
        		status = adminMapper.updateAdminStatus(anchorId, userId, new Integer(0)); //如果房管记录存在, 说明数据库中是有记录的, 则更新房管状态即可
        		return status;
    		}
    	}
    	//redis中无缓存记录, 先缓存; 查看库中是否有记录, 如果有则更新, 没有则插入
		LiveAdmin admin = LiveAdminHelper.getAndCacheAdminInfo(redisAdapter, adminMapper, anchorId, userId);
		if (admin == null || admin.getUserId() <= 0) {
    		status = adminMapper.addAdmin(anchorId, userId);
    	} else {
    		status = adminMapper.updateAdminStatus(anchorId, userId, new Integer(0));
    	}
    	redisAdapter.delKeys(adminKey);//删除存信息
    	return status;
    }
    
}
