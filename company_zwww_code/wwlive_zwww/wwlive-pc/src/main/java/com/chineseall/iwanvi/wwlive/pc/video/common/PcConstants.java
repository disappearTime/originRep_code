package com.chineseall.iwanvi.wwlive.pc.video.common;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import com.chineseall.iwanvi.wwlive.common.constants.Constants;
import com.chineseall.iwanvi.wwlive.common.tools.Base64Tools;

public final class PcConstants {

	/**
	 * LIVE
	 */
	public static final String STREAMP_REFIX = "LIVE";

	public static final String SEQ_STREAM_ID = "stream_id";

	public static final String SEQ_VDOID = "vdoid_id";
	
	public static final String SEQ_IMG = "img_id";
	
	public static final int SUCCESS = 1;
		
	public static final int FAIL = 0;
	
	public static final String DEFAULT_ANCHOR_HEAD_FILENAME = "default_head";
	
	private PcConstants() {
	}
	
	/**
	 * 获得anchorId
	 * @param request
	 * @return
	 */
	public static long getAnchorIdByCookie(HttpServletRequest request) {
		Cookie[] cookies = request.getCookies();
        if (cookies!=null) {
            for (int i = 0; i < cookies.length; i++) {
                Cookie cookie = cookies[i];
                if (cookie.getName().equals(Constants.REDIS_SESSION)) {
                	String value = null;
                	if ((value = cookie.getValue()) != null) {
                    	value = new String(Base64Tools.decode(value));
                    	String[] values = value.split("_");
                    	return Long.parseLong(values[1]);
                	}
                }
            }
        }
		return 0L;
	}
	
}
