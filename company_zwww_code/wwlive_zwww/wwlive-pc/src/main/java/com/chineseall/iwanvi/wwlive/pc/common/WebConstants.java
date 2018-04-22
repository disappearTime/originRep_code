package com.chineseall.iwanvi.wwlive.pc.common;

import java.util.UUID;

/**
 * Created by kai on 16/8/25.
 */
public class WebConstants {


    public static class Login {

        public static final String LOGIN_COOKIE_KEY = "wwlive_token";

        public static final String LOGIN_URL = "/login/in";
        
        public static final String LOGIN_COOKIE_SPLIT = "\001";

        public static final String INDEX_URL = "/live/index";

        public static final String LOGIN_USER_ATTR_KEY = "loginuser";

        public static final String LOGIN_TOKEN_KEY = "captchaToken";

        public static final String NO_ACCESS_URL = "/login/noaccess";
        /**
         * cookie 30天
         */
        public static final Integer COOKIE_EXPIRY_30_DAYS = 30 * 24 * 60;
        
        /**
         * 视频收入
         */
        public static final String LOGIN_USER_INCOME_VIDEOCNT_KEY = "income_cnt";
        
    }

    /**
     * 金山通知开始
     */
    public static final String KSCLOUD_START = "/kscloud/live/start";

    /**
     * 金山通知结束
     */
    public static final String KSCLOUD_STOP = "/kscloud/live/stop";

    /**
     * 获得去除-的UUID
     * @return
     */
    public static String getUUID() {
		UUID uuid = UUID.randomUUID();
	    String str = uuid.toString(); 
	    String uuidStr = str.replace("-", "");
	    return uuidStr;
    }
    
}
