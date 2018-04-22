package com.chineseall.iwanvi.wwlive.pc.common.helper;

import static com.chineseall.iwanvi.wwlive.common.constants.Constants.AND;
import static com.chineseall.iwanvi.wwlive.common.constants.Constants.EQU;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.*;

import com.chineseall.iwanvi.wwlive.dao.wwlive.UserPushMapper;
import com.chineseall.iwanvi.wwlive.domain.wwlive.PushManage;
import com.chineseall.iwanvi.wwlive.domain.wwlive.UserPush;
import com.zw.zcf.util.JsonUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.alibaba.fastjson.JSONObject;
import com.chineseall.iwanvi.wwlive.common.cache.RedisClientAdapter;
import com.chineseall.iwanvi.wwlive.common.cache.RedisExpireTime;
import com.chineseall.iwanvi.wwlive.common.cache.RedisKey;
import com.chineseall.iwanvi.wwlive.common.external.rongcloud.domain.SdkHttpResult;
import com.chineseall.iwanvi.wwlive.common.tools.HttpUtils;
import com.chineseall.iwanvi.wwlive.dao.wwlive.UserInfoMapper;
import com.chineseall.iwanvi.wwlive.domain.wwlive.LiveVideoInfo;
import com.chineseall.iwanvi.wwlive.domain.wwlive.UserInfo;
import com.chineseall.iwanvi.wwlive.pc.video.service.NoticeService;
import com.service.FollowAnchorService;
import com.service.impl.FollowAnchorServiceImpl;
import com.service.impl.FollowNoticeServiceImpl;

/**
 * 推送消息通知
 * 
 * @author DIKEPU
 *
 */
public class StartVideoPushMsgThread implements Runnable {

    private static final Logger LOGGER = Logger.getLogger(StartVideoPushMsgThread.class);

    private String followUrl;

    private LiveVideoInfo videoInfo;

    private String anchorName;

    private NoticeService noticeService;

    private RedisClientAdapter redisAdapter;

    private UserInfoMapper userInfoMapper;

    private UserPushMapper userPushMapper;

    private String[] contents = new String[] { "你家主播美如画，来来来一起围观", "这里有一个宝宝一本正经的胡说八道", "该配合她演出的你，快去和她即兴表演" };

    private Random random = new Random();

    public String getFollowUrl() {
        return followUrl;
    }

    public void setFollowUrl(String followUrl) {
        this.followUrl = followUrl;
    }

    public LiveVideoInfo getVideoInfo() {
        return videoInfo;
    }

    public void setVideoInfo(LiveVideoInfo videoInfo) {
        this.videoInfo = videoInfo;
    }

    public String getAnchorName() {
        return anchorName;
    }

    public void setAnchorName(String anchorName) {
        this.anchorName = anchorName;
    }

    public RedisClientAdapter getRedisAdapter() {
        return redisAdapter;
    }

    public void setRedisAdapter(RedisClientAdapter redisAdapter) {
        this.redisAdapter = redisAdapter;
    }

    public UserInfoMapper getUserInfoMapper() {
        return userInfoMapper;
    }

    public void setUserInfoMapper(UserInfoMapper userInfoMapper) {
        this.userInfoMapper = userInfoMapper;
    }

    public NoticeService getNoticeService() {
        return noticeService;
    }

    public void setNoticeService(NoticeService noticeService) {
        this.noticeService = noticeService;
    }

    public StartVideoPushMsgThread() {

    }

    public StartVideoPushMsgThread(String followUrl, LiveVideoInfo info, RedisClientAdapter redisAdapter,
            UserInfoMapper userInfoMapper, String anchorName, NoticeService noticeService,UserPushMapper userPushMapper) {
        super();
        this.followUrl = followUrl;
        this.videoInfo = info;
        this.redisAdapter = redisAdapter;
        this.userInfoMapper = userInfoMapper;
        this.anchorName = anchorName;
        this.noticeService = noticeService;
        this.userPushMapper = userPushMapper;
    }

    @Override
    public void run() {
        try {
            noticeService.manJoinLive(videoInfo.getVideoId(), videoInfo.getChatroomId(), videoInfo.getAnchorId(), 0L);
            Long anchorId = this.videoInfo.getAnchorId();
            FollowNoticeServiceImpl followNoticeService = FolloServiceSingleton.getInstance();
            try {
                followNoticeService.noticeAnchorOpenLive(videoInfo.getAnchorId(), 1, (new Date()).getTime());
            } catch (Exception e) {
                e.printStackTrace();
                LOGGER.error("followNoticeService 直播开始通知失败：", e);
            }

            String startKey = RedisKey.VIDEO_START_ + videoInfo.getVideoId();
            if (!redisAdapter.existsKey(startKey)) {
                redisAdapter.strSetexByNormal(startKey, RedisExpireTime.EXPIRE_DAY_5, "0");
                LOGGER.info("---------------开始发送push---------------");
                FollowAnchorService followService = new FollowAnchorServiceImpl();

                int cnt = followService.getFansNumber(anchorId);
                LOGGER.info(cnt + "---------------开始发送push---------------" + anchorId);
                if (cnt > 0) {
                    List<Map<String, Object>> fans = followService.queryFans(anchorId, 1, 0L);//每次最多返回10个粉丝
    				if (fans != null && !fans.isEmpty()) {
	                    String uids = getFans(fans);
                        String dluids = getDlFans(fans);

	                    Object tt = fans.get(fans.size() - 1).get("timestamp");
    					Long timestamp = (tt == null ? Long.MAX_VALUE : (Long) tt);

    					int totalPage = cnt / 10;// 默认粉丝取10个
	                    for (int i = 1; i <= totalPage; i++) {
	                        fans = followService.queryFans(anchorId, i + 1, timestamp);// 每次最多返回10个粉丝
	                        if (fans == null || fans.isEmpty()) {
	                            break;
	                        }
	                        uids += getFans(fans);
                            dluids += getDlFans(fans);
	                        tt = fans.get(fans.size() - 1).get("timestamp");
	                        timestamp =  (tt == null ? Long.MAX_VALUE : (Long) tt);
	                        if ((i + 1) % 1000 == 0) {// 1000人发一次
                                pushMsg(uids, anchorId);
                                dlpushMsg(dluids, anchorId);
	                            uids = "";
	                        }
	                    }
	                    if (StringUtils.isNotBlank(uids)) {
	                        pushMsg(uids, anchorId);
                            dlpushMsg(dluids, anchorId);

                        }
      				}

                }

            }
        } catch (IOException e) {
            LOGGER.error("请求发送push失败，IO异常：", e);
        } catch (Exception e) {
            LOGGER.error("请求发送push失败，请求异常：", e);
        }

    }

    private String getFans(List<Map<String, Object>> fans) throws Exception {
        StringBuilder uids = new StringBuilder();
        String uid = "";
        String userKey = "";
        Long userId = new Long(0);
        UserInfo user = null;
        for (Map<String, Object> fan : fans) {
            userId = (Long) fan.get("uid");
            userKey = RedisKey.USER_INFO_ + userId;
            if (redisAdapter.existsKey(userKey)) {
                uid = redisAdapter.hashGet(userKey, "loginId");
                if (uid.contains("_cx")) {
                    uid = uid.split("_")[0];
                    uids.append(uid + ",");
                }
            } else {
                user = UserInfoHelper.getAndCacheUserInfo(redisAdapter, userInfoMapper, userId);
                if (user != null 
                		&& user.getLoginId().contains("_cx")) {
                    uid = user.getLoginId();
                    uid = uid.split("_")[0];
                    uids.append(uid + ",");
                }
            }
        }

        return uids.toString();
    }

    private void pushMsg(String userIds, Long anchorId) throws Exception {
        if (StringUtils.isNotBlank(userIds)) {// 去掉拼接的userIds中的最后一个","
            if (userIds.length() > 0) {
                userIds = userIds.substring(0, userIds.length() - 1);
            }
        }

        StringBuilder sb = new StringBuilder();
        JSONObject fun = this.getPush(anchorId);

        sb.append("userIds").append(EQU).append(userIds).append(AND).append("title").append(EQU)
                .append("你关注的 " + this.anchorName + " 直播啦！！！").append(AND).append("content").append(EQU)
                .append(randomContent()).append(AND).append("linkUrl").append(EQU).append("live://" + fun.toString());
        LOGGER.info("---------------开始发送push---------------" + sb.toString());
        HttpURLConnection conn = HttpUtils.createPostHttpConnection(this.followUrl);

        SdkHttpResult result = HttpUtils.returnResult(conn, sb.toString());
        String code = null;
        if (result.getResult() != null && StringUtils.isNotBlank(result.getResult())) {
            JSONObject json = JSONObject.parseObject(result.getResult());
            code = json.getString("code");
        }
        if ("0".equals(code)) {
            LOGGER.info("anchorId: " + anchorId + ", push发送成功！");
        } else if ("1".equals(code)) {
            LOGGER.info("anchorId: " + anchorId + ", push发送失败！");
        }
    }

    private String randomContent() {
        return contents[random.nextInt(3)];
    }

    /**
     * 
     * @param anchorId
     * @return
     */
    private JSONObject getPush(Long anchorId) {
        JSONObject fun = new JSONObject();
        fun.put("fun", "live");
        JSONObject data = new JSONObject();
        data.put("anchorId", anchorId);
        data.put("push", new Integer(1));
        fun.put("data", data);
        return fun;
    }


    private String getDlFans (List<Map<String, Object>> fans) throws Exception {
        StringBuilder uids = new StringBuilder();
        String uid = "";
        String userKey = "";
        Long userId = new Long(0);
        UserInfo user = null;
        for (Map<String, Object> fan : fans) {
            userId = (Long) fan.get("uid");
            userKey = RedisKey.USER_INFO_ + userId;
            if (redisAdapter.existsKey(userKey)) {
                uid = redisAdapter.hashGet(userKey, "userId");
                String origin = redisAdapter.hashGet(userKey, "origin");
                if("2".equals(origin)) {
                    uids.append(uid + ",");
                }
            } else {
                user = UserInfoHelper.getAndCacheUserInfo(redisAdapter, userInfoMapper, userId);
                if (user != null 
                		&& user.getOrigin() == 2) {
                    uid = user.getUserId()+"";
                    uids.append(uid + ",");
                }
            }
        }
        return uids.toString();
    }

    public void dlpushMsg (String userIds, Long anchorId) {
        String token = getToken(userIds);
        blockNotice(redisAdapter,anchorId, token);
    }

    public String   getToken(String userIds) {
        StringBuffer buffer = new StringBuffer();
        String[] split = userIds.split(",");
        String token = null;
        for (String uid : split) {
            token = userPushMapper.findTokenByUid(uid);
            if(StringUtils.isNotEmpty(token)) {
                buffer.append(token + "\n");
            }

        }
        return buffer.toString();
    }


    public void blockNotice(RedisClientAdapter redisAdapter,Long anchorId, String token){
        try {
            JSONObject fun = this.getPush(anchorId);
            Map<String, Object> map = new HashMap<String, Object>();

            Map<String, Object> param = new HashMap<String, Object>();
            param.put("m", "sendAndroidFilecast");//key不变
            param.put("deviceToken",token);//参数可变
            //相关业务参数
//            param.put("deviceToken","ArSBotFtluhjmgY8B5buEQ9in-JfgQB6Lf65III5tuhO"+"\n"+"AkUc3iPpUjXrWLw3IbGXdRYZeSKUfRuq5TCej2VZhQO7");//参数可变
            param.put("title","你关注的 " + this.anchorName + " 直播啦！！！");
            param.put("ticker","你关注的 " + this.anchorName + " 直播啦！！！");
            param.put("text",randomContent());
            param.put("type",3);
            param.put("url","live://" + fun.toString());

            map.put("params", param);//key不变
            map.put("action", "pushHandler");//key不变
            String json = JsonUtils.toJSON(map);
//            redisAdapter.rpush("pushKey", json);
            redisAdapter.pushRpush("pushKey", json);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
