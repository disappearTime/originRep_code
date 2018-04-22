package test;

import static com.chineseall.iwanvi.wwlive.common.constants.Constants.AND;
import static com.chineseall.iwanvi.wwlive.common.constants.Constants.EQU;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.log4j.Logger;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.alibaba.fastjson.JSONObject;
import com.chineseall.iwanvi.wwlive.common.cache.RedisClientAdapter;
import com.chineseall.iwanvi.wwlive.dao.wwlive.RoleInfoMapper;
import com.chineseall.iwanvi.wwlive.dao.wwlive.UserInfoMapper;
import com.chineseall.iwanvi.wwlive.dao.wwlive.UserPushMapper;
import com.chineseall.iwanvi.wwlive.domain.wwlive.UserInfo;
import com.chineseall.iwanvi.wwlive.web.event.service.RedEnvelopeService;
import com.zw.zcf.util.JsonUtils;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:spring/application.xml" })
public class TestSpring231 {

	//======================fileds==================================
	@Autowired
	RedEnvelopeService redEnvelopeService;

	// TODO
	@Test
	public void test1() {
		int i = 0;
		while (i < 1) {
			 nobleExpire();
			i++;
		}
	}
	
	public void test2() throws InterruptedException {
		for (int i = 0; i < 10; i++) {
			System.out.println(redEnvelopeService.getLuckyDiamond("video_re_gate_10065_92792", 1L));
			Thread.sleep(300);
		}
	}

    @Autowired
    private RoleInfoMapper roleInfoMapper;

    @Autowired
    private RedisClientAdapter redisAdapter;

    private Logger logger = Logger.getLogger(this.getClass());

    @Autowired
    private UserInfoMapper userInfoMapper;

    @Autowired
    private UserPushMapper userPushMapper;
    
    private String cxPushUrl = "http://192.168.1.243:9036/admin/messagePush.do?method=livePushTask";

    /**
     * 每天20点判断贵族是否过期
     */
    public void nobleExpire() {
        List<Map<String, Object>> nobles = roleInfoMapper.getRoleExpiryInfo();
        String expireInfo = null;
        for (Map<String, Object> noble : nobles) {
            // 有效天数转换失败时默认没有过期
            int effDays = MapUtils.getIntValue(noble, "effDays", 99);
            String nobleName = (String) noble.get("nobleName");
            int balance = (MapUtils.getIntValue(noble, "balance", 0))/100;
            Long userId = MapUtils.getLongValue(noble, "userId", 0);
            int nobleIndex = MapUtils.getIntValue(noble, "nobleIndex");
            if (effDays < -3 || effDays > 1) {
                continue; // 只在前一天, 当天, 第二天, 第三天, 第四天提醒
            }
            if (effDays == 1) {
                if (balance > 0) {
                    expireInfo = "大人，您的" + nobleName + "将于明天过期，您有" + balance + "大洋可用，主播大大在主播间@您去续费啦～";
                } else {
                    expireInfo = "大人，您的" + nobleName + "将于明天过期，主播大大在主播间@您去续费啦～";
                    ;
                }
            } else if (effDays >= -3 && effDays <= 0) {
                if (balance > 0) {
                    expireInfo = "大人，您的" + nobleName + "已过期了，您还有" + balance + "大洋没花呢，再不花就过期了～";
                } else {
                    expireInfo = "大人，您的" + nobleName + "已过期，主播大大刚刚在直播间说想您了";
                }
            }

            try {
                pushToUser(userId.toString(), "贵族到期通知", expireInfo,nobleIndex);
            } catch (Exception e) {
                e.printStackTrace();
                logger.error("发送贵族到期通知push失败, userId = " + userId, e);
            }
        }
    }

    private void pushToUser(String userId, String title, String content,int nobleIndex) throws Exception {
        if (StringUtils.isBlank(userId)) {// 去掉拼接的userIds中的最后一个","
            return;
        }

        // 直播uid转换为免电uid
        Long uid = Long.valueOf(userId);
        UserInfo userInfo = userInfoMapper.findById(uid);
        if (userInfo == null || userInfo.getOrigin() == null) {
        	return;
        }
        String loginId = userInfo.getLoginId();
        if (loginId.contains("_cx")) {
            userId = loginId.split("_")[0];
        }
        // TODO 牛强鸿: 和客户端协定点击push操作
        JSONObject fun = new JSONObject();
        fun.put("fun", "nobleDue");
        JSONObject data = new JSONObject();
        data.put("userId", userId);
        data.put("nobleIndex",nobleIndex);
        data.put("uid",String.valueOf(uid));
        fun.put("data", data);
        
        if (2 == userInfo.getOrigin()) {
        	blockNotice(userInfo, getToken(userId), fun, content);
        	return;
        }
        StringBuilder sb = new StringBuilder();
        
        sb.append("userIds").append(EQU).append(userId).append(AND).append("title").append(EQU)
                .append(title).append(AND).append("content").append(EQU)
                .append(content).append(AND).append("linkUrl").append(EQU).append("live://" + fun.toString());
        Map<String, String> params = new HashMap<>();
        params.put("userIds", userId);
        params.put("title", title);
        params.put("content", content);
        params.put("linkUrl", "live://" + fun.toString());
//        String response = HttpRequestUtils.httpPost(cxPushUrl, params);
        
        String response = "{\"code\":0}";//TODO
        logger.info("---------------开始发送push---------------cxPushUrl: " + cxPushUrl);
        String code = null;
        if (StringUtils.isNotBlank(response)) {
            JSONObject json = JSONObject.parseObject(response);
            code = json.getString("code");
        }
        if ("0".equals(code)) {
            logger.info("userId: " + userId + ", 贵族到期push发送成功！");
        } else if ("1".equals(code)) {
            logger.info("userId: " + userId + ", 贵族到期push发送失败！");
        }
    }
    
    /**
     * 独立版贵族的到期push
     * @param userInfo
     * @param token
     * @param fun
     * @param content
     */
    public void blockNotice(UserInfo userInfo, String token, JSONObject fun, String content){
        try {
            Map<String, Object> map = new HashMap<String, Object>();

            Map<String, Object> param = new HashMap<String, Object>();
            if (userInfo.getPlatform() != null && userInfo.getPlatform() == 0) {//安卓
                param.put("m", "sendAndroidFilecast");//key不变
                param.put("title","贵族到期通知");
                param.put("text", content);
            } else {//ios
                param.put("m", "sendIosFilecast");//key不变
                param.put("alert","贵族到期通知");
                param.put("desc", content);
            }
            param.put("deviceToken", token);//参数可变
            //相关业务参数
//            param.put("deviceToken","ArSBotFtluhjmgY8B5buEQ9in-JfgQB6Lf65III5tuhO"+"\n"+"AkUc3iPpUjXrWLw3IbGXdRYZeSKUfRuq5TCej2VZhQO7");//参数可变
           
            param.put("type",5);
            param.put("url","live://" + fun.toJSONString());

            map.put("params", param);
            map.put("action", "pushHandler");//key不变
            String json = JsonUtils.toJSON(map);
            final Date date =  new Date();
            System.out.println(DateFormatUtils.format(date, "yyyy-MM-dd hh:mm:ss ===> ") + json);
//            Thread.sleep(1000);
            redisAdapter.pushRpush("pushKey", json);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public String getToken(String userId) {
        StringBuffer buffer = new StringBuffer();
        String token = userPushMapper.findTokenByUid(userId);
            if(StringUtils.isNotEmpty(token)) {
                buffer.append(token);
            }

        return buffer.toString();
    }

}
