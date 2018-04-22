package com.chineseall.iwanvi.wwlive.web.video.controller;

import com.alibaba.fastjson.JSONObject;
import com.chineseall.iwanvi.wwlive.common.cache.RedisClientAdapter;
import com.chineseall.iwanvi.wwlive.common.cache.RedisKey;
import com.chineseall.iwanvi.wwlive.domain.wwlive.UpgradeInfo;
import org.apache.commons.collections.map.HashedMap;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * 下载信息请求
 * @author DIKEPU
 *
 */
@Controller
public class DownLoadController {
	static final Logger LOGGER = Logger.getLogger(DownLoadController.class);

	/**
	 * android版本信息
	 */
	@Value("${app.android.version}")
	private String androidVersion;

	/**
	 * android最新下载地址
	 */
	@Value("${app.android.download.url}")
	private String androidDownloadUrl;

	/**
	 * android最新下载地址
	 */
	@Value("${app.android.md5}")
	private String md5;
	
	/**
	 * 灰度渠道
	 */
	@Value("${gray.releasee.cnids}")
	private String grayCnids;
	
    @Autowired
    private RedisClientAdapter redisAdapter;

    @RequestMapping(value = "/external/android/check", method = {
			RequestMethod.POST, RequestMethod.GET })
	@ResponseBody
	public Map<String, Object> checkVersion(HttpServletRequest request) {
		try {
//			DaoFactory daoFactory=new DaoFactory();

			String version = request.getParameter("version");
			String cnid = request.getHeader("cnid");
			String packname = request.getHeader("packname");
            String app = request.getParameter("app");
            String oscode = request.getHeader("oscode");//系统版本号
            if(StringUtils.isBlank(cnid)){
				cnid=request.getParameter("cnid");
				if(cnid==null){
					cnid="";
				}
			}
			int isUpdate = 0;
			if("com.mianfei.book".equals(packname)) {
				LOGGER.info("packname: " + packname);
				//蓝版
				return getBlueJson(request);
			}
			// 是否强制更新
            String newestVersion = null;

            if (StringUtils.isNotBlank(oscode) && Integer.valueOf(oscode) >= 24) {
            	 newestVersion = redisAdapter.hashGet(RedisKey.independentUpdownKeys.INDEPENDENT_CURRENT_VERSION, "version");
            	 app = "md";
            	 if (StringUtils.isEmpty(newestVersion)) {
            		 newestVersion = "3.0.0";
            	 }
            	 Map<String,Object> map = getAndroidVersionData(cnid, app, isUpdate, version, newestVersion);
 				return map;
            }
            
            if("dl".equals(app)) {
                newestVersion = redisAdapter.hashGet(RedisKey.independentUpdownKeys.INDEPENDENT_CURRENT_VERSION, "version");
            }else {
                newestVersion = redisAdapter.hashGet(RedisKey.UPGRADE_INFO, "version");
                if (redisAdapter.setIsMember(RedisKey.FORCE_UPGRADE_VERSIONS, version)) {
                    isUpdate = 2;
                }
            }

			if (StringUtils.isBlank(newestVersion)) {
				newestVersion = redisAdapter.strGet(RedisKey.VERSION_KEY);
			}

			if (newestVersion != null && redisAdapter.existsKey(RedisKey.DOWNLOAD_KEY) && redisAdapter.existsKey(RedisKey.MD5_KEY)) {
				if (StringUtils.isEmpty(version)) {
//					return JSONObject.toJSON(new AndroidVersion()).toString();
                    return addAnderVersionData(new AndroidVersion());
				}
				if (version.equals("-1")) {
//					return JSONObject.toJSON(getAndroidVersionMsgFromRedis(0, isUpdate, "",cnid)).toString();
                    return addAnderVersionData(getAndroidVersionMsgFromRedis(0, isUpdate, "",cnid,app));
				}
//				if (version.compareTo(newestVersion.replaceAll("\"", "")) < 0) {
				if (compareVersion(version,newestVersion) < 0) {
//                    return JSONObject.toJSON(getAndroidVersionMsgFromRedis(0, isUpdate, "", cnid)).toString();
                    return addAnderVersionData (getAndroidVersionMsgFromRedis(0, isUpdate, "", cnid,app));
				}
                return addAnderVersionData(getAndroidVersionMsgFromRedis(0, 1, "",cnid,app));
//				return JSONObject.toJSON(getAndroidVersionMsgFromRedis(0, 1, "",cnid)).toString();
			}

			if (StringUtils.isEmpty(version)) {
//				return JSONObject.toJSON(new AndroidVersion()).toString();
                return addAnderVersionData(new AndroidVersion());
			}
			if (version.equals("-1")) {
//				return JSONObject.toJSON(getAndroidVersionMsg(0, isUpdate, "")).toString();
                return addAnderVersionData(getAndroidVersionMsg(0, isUpdate, ""));
			}
			if (version.compareTo(androidVersion) < 0) {
//				return JSONObject.toJSON(getAndroidVersionMsg(0, isUpdate, "")).toString();
                return addAnderVersionData(getAndroidVersionMsg(0, isUpdate, ""));
			}
		}catch(Exception ex){
			LOGGER.error("",ex);
			ex.printStackTrace();
		}
//		return JSONObject.toJSON(getAndroidVersionMsg(0, 1, "")).toString();
        return addAnderVersionData(getAndroidVersionMsg(0, 1, ""));
	}
    
    /**
     * 独立版7.0系统处理
     * @param cnid
     * @param app
     * @param isUpdate
     * @param version
     * @param newestVersion
     * @return
     * @throws Exception
     */
    private Map<String,Object> getAndroidVersionData(String cnid, String app, int isUpdate, String version, String newestVersion) throws Exception {
    	if (StringUtils.isEmpty(version)) {
            return addAnderVersionData(new AndroidVersion());
		}
		if (version.equals("-1")) {
            return addAnderVersionData(getAndroidVersionMsgFromRedis(0, isUpdate, "",cnid,app));
		}
		if (compareVersion(version, newestVersion) < 0) {
            return addAnderVersionData (getAndroidVersionMsgFromRedis(0, isUpdate, "", cnid,app));
		}
        return addAnderVersionData(getAndroidVersionMsgFromRedis(0, 1, "",cnid,app));
    	
    }
	/**
	 * 获得安卓客户端当前版本信息
	 * @param code
	 * @param isupdate
	 * @param errorMsg
	 * @return
	 */
	private AndroidVersion getAndroidVersionMsg(int code, int isupdate, String errorMsg) {
//		getAndroidVersionMsgFromRedis(0, 0, "");
		AndroidVersion msg = new AndroidVersion();
		msg.setCode(code);
		msg.setIsupdate(isupdate);
		msg.setErrorMsg(errorMsg);
		msg.setUrl(androidDownloadUrl);
		msg.setVersion(androidVersion);
		msg.setMd5(md5);
		return msg;
	}
	
	private AndroidVersion getAndroidVersionMsgFromRedis(int code, int isupdate, String errorMsg,String cnid,String app) {
	    AndroidVersion msg = new AndroidVersion();
	    msg.setCode(code);
	    msg.setIsupdate(isupdate);
	    msg.setErrorMsg(errorMsg);
		LOGGER.info("--getAndroidVersionMsgFromRedis----code:"+code+",isupdate:"+isupdate+",errorMsg:"+errorMsg+",cnid:"+cnid);
        if("dl".equals(app)) {
            if(redisAdapter.existsKey(RedisKey.independentUpdownKeys.INDEPENDENT_CURRENT_VERSION)){
                String objQudao=redisAdapter.strGet("dlqudao");
                if(StringUtils.isNotBlank(objQudao)) {//"1062"
                    if(objQudao.contains(cnid)||objQudao.equals("0")){//0代表所有用户都下载
                        Map<String, String> upgradeInfo = redisAdapter.hashGetAll(RedisKey.independentUpdownKeys.INDEPENDENT_CURRENT_VERSION);
                        if(upgradeInfo!=null&&!upgradeInfo.isEmpty()){
                            UpgradeInfo info = UpgradeInfo.fromMap(upgradeInfo);
                            msg.setUrl(info.getUrl());
                            msg.setVersion(info.getVersion());
                            msg.setMd5(info.getMd5());
                            msg.setUpdateMsg(info.getUpdateMsg());
                            msg.setApkSize(info.getApkSize());
                            return msg;
                        }
                    }
                }
            }
            
			msg.setIsupdate(1);
        } else if ("md".equals(app)) {
        	if(redisAdapter.existsKey(RedisKey.independentUpdownKeys.MD_CURRENT_VERSION)){
                Map<String, String> upgradeInfo = redisAdapter.hashGetAll(RedisKey.independentUpdownKeys.MD_CURRENT_VERSION);
                if(upgradeInfo!=null&&!upgradeInfo.isEmpty()){
                    UpgradeInfo info = UpgradeInfo.fromMap(upgradeInfo);
                    msg.setUrl(info.getUrl());
                    msg.setVersion(info.getVersion());
                    msg.setMd5(info.getMd5());
                    msg.setUpdateMsg(info.getUpdateMsg());
                    msg.setApkSize(info.getApkSize());
                    return msg;
                }
            }
        	if(redisAdapter.existsKey(RedisKey.independentUpdownKeys.INDEPENDENT_CURRENT_VERSION)){
                String objQudao=redisAdapter.strGet("dlqudao");
                if(StringUtils.isNotBlank(objQudao)) {//"1062"
                    if(objQudao.contains(cnid)||objQudao.equals("0")){//0代表所有用户都下载
                        Map<String, String> upgradeInfo = redisAdapter.hashGetAll(RedisKey.independentUpdownKeys.INDEPENDENT_CURRENT_VERSION);
                        if(upgradeInfo!=null&&!upgradeInfo.isEmpty()){
                            UpgradeInfo info = UpgradeInfo.fromMap(upgradeInfo);
                            msg.setUrl(info.getUrl());
                            msg.setVersion(info.getVersion());
                            msg.setMd5(info.getMd5());
                            msg.setUpdateMsg(info.getUpdateMsg());
                            msg.setApkSize(info.getApkSize());
                            return msg;
                        }
                    }
                }
            }
			msg.setIsupdate(1);
        } else {
            if(redisAdapter.existsKey(RedisKey.UPGRADE_INFO)){
                String objQudao=redisAdapter.strGet("qudao");
                if(StringUtils.isNotBlank(objQudao)) {//"1062"
                    if(objQudao.equals(cnid)||objQudao.equals("0")){//0代表所有用户都下载
                        Map<String, String> upgradeInfo = redisAdapter.hashGetAll(RedisKey.UPGRADE_INFO);
                        if(upgradeInfo!=null&&!upgradeInfo.isEmpty()){
                            UpgradeInfo info = UpgradeInfo.fromMap(upgradeInfo);
                            msg.setUrl(info.getUrl());
                            msg.setVersion(info.getVersion());
                            msg.setMd5(info.getMd5());
                            msg.setUpdateMsg(info.getUpdateMsg());
                            msg.setApkSize(info.getApkSize());
                            return msg;
                        }
                    }
					msg.setIsupdate(1);
                }
            }
    		msg.setUrl(StringUtils.isBlank(redisAdapter.strGet(RedisKey.DOWNLOAD_KEY)) ? "" :
				redisAdapter.strGet(RedisKey.DOWNLOAD_KEY).replaceAll("\"", ""));

			msg.setVersion(StringUtils.isBlank(redisAdapter.strGet(RedisKey.VERSION_KEY)) ? "" : 
							redisAdapter.strGet(RedisKey.VERSION_KEY).replaceAll("\"", ""));
			
			msg.setMd5(StringUtils.isBlank(redisAdapter.strGet(RedisKey.MD5_KEY)) ? "" : 
							redisAdapter.strGet(RedisKey.MD5_KEY).replaceAll("\"", ""));
			msg.setApkSize(1);
			
			msg.setUpdateMsg("");
        }
		return msg;
	}
	
	private Map<String,Object> getBlueJson(HttpServletRequest request) {
		try {
			int isUpdate = 0;
			String version = request.getParameter("version");
			String cnid = request.getHeader("cnid");
			// 是否强制更新
			if (redisAdapter.setIsMember(RedisKey.BlueUpdownKeys.BLUE_FORCE_UPGRADE_VERSIONS, version)) {
				isUpdate = 2;
			}
			LOGGER.info("---------------------getBlueJson-----------------------------------");
			String newestVersion = redisAdapter.hashGet(RedisKey.BlueUpdownKeys.BLUE_UPGRADE_INFO, "version");
			if (StringUtils.isBlank(newestVersion)) {
				newestVersion = redisAdapter.strGet(RedisKey.BlueUpdownKeys.BLUE_VERSION_KEY);
			}
			if (newestVersion != null 
					&& redisAdapter.existsKey(RedisKey.BlueUpdownKeys.BLUE_DOWNLOAD_KEY) 
					&& redisAdapter.existsKey(RedisKey.BlueUpdownKeys.BLUE_MD5_KEY)) {
				if (StringUtils.isEmpty(version)) {
//					return JSONObject.toJSONString(new AndroidVersion());
                    return addAnderVersionData(new AndroidVersion());
				}
//				if (version.equals("-1")) {
////					return JSONObject.toJSONString(getBlueAndroidVersionMsgFromRedis(0, isUpdate, "", cnid));
//                    return addAnderVersionData(new AndroidVersion());
//				}
				if (version.compareTo(newestVersion.replaceAll("\"", "")) < 0) {
//					return JSONObject.toJSONString(getBlueAndroidVersionMsgFromRedis(0, isUpdate, "", cnid));
                    return addAnderVersionData(getBlueAndroidVersionMsgFromRedis(0, isUpdate, "", cnid));
				}
	
//				return JSONObject.toJSONString(getBlueAndroidVersionMsgFromRedis(0, 1, "", cnid));
                return addAnderVersionData(getBlueAndroidVersionMsgFromRedis(0, 1, "", cnid));
			}
	
			if (StringUtils.isEmpty(version)) {
//				return JSONObject.toJSONString(new AndroidVersion());
                return addAnderVersionData(new AndroidVersion());
			}
		}catch(Exception ex){
			LOGGER.error("",ex);
			ex.printStackTrace();
		}
		return null;
	}
	
	/**
	 * 获得蓝版插件信息
	 * @param code
	 * @param isupdate
	 * @param errorMsg
	 * @param cnid
	 * @return
	 */
	private AndroidVersion getBlueAndroidVersionMsgFromRedis(int code, int isupdate, String errorMsg, String cnid) {
		
		 AndroidVersion msg = new AndroidVersion();
		    msg.setCode(code);
		    msg.setIsupdate(isupdate);
		    msg.setErrorMsg(errorMsg);
//			LOGGER.info("--getBlueAndroidVersionMsgFromRedis----code: " + code + ",isupdate: " + isupdate +", errorMsg: " + errorMsg + ",cnid: "+cnid);
//			if(redisAdapter.existsKey(RedisKey.BlueUpdownKeys.BLUE_UPGRADE_INFO)){
////				String objQudao=redisAdapter.strGet("qudao");
////				if(StringUtils.isNotBlank(objQudao)&& (objQudao.equals(cnid)||objQudao.equals("0"))){//0代表所有用户都下载
//					Map<String, String> upgradeInfo = redisAdapter.hashGetAll(RedisKey.BlueUpdownKeys.BLUE_UPGRADE_INFO);
//					if(upgradeInfo!=null&&!upgradeInfo.isEmpty()){
//						UpgradeInfo info = UpgradeInfo.fromMap(upgradeInfo);
//						msg.setUrl(info.getUrl());
//						msg.setVersion(info.getVersion());
//						msg.setMd5(info.getMd5());
//						msg.setUpdateMsg(info.getUpdateMsg());
//						msg.setApkSize(info.getApkSize());
//						return msg;
//					}
////				}
//
//			}
			msg.setUrl(StringUtils.isBlank(redisAdapter.strGet(RedisKey.BlueUpdownKeys.BLUE_DOWNLOAD_KEY)) ? "" :
												redisAdapter.strGet(RedisKey.BlueUpdownKeys.BLUE_DOWNLOAD_KEY).replaceAll("\"", ""));
			
			msg.setVersion(StringUtils.isBlank(redisAdapter.strGet(RedisKey.BlueUpdownKeys.BLUE_VERSION_KEY)) ? "" : 
												redisAdapter.strGet(RedisKey.BlueUpdownKeys.BLUE_VERSION_KEY).replaceAll("\"", ""));
			
			msg.setMd5(StringUtils.isBlank(redisAdapter.strGet(RedisKey.BlueUpdownKeys.BLUE_MD5_KEY)) ? "" : 
												redisAdapter.strGet(RedisKey.BlueUpdownKeys.BLUE_MD5_KEY).replaceAll("\"", ""));
	  		msg.setApkSize(1);
			
			msg.setUpdateMsg("");
			return msg;
	}
	
	/**
	 * 安卓校验返回参数
	 * 
	 * @author DIKEPU
	 *
	 */
	private class AndroidVersion {
		private int code = 1;
		private String errorMsg = "";
		private String version = "";
		private int isupdate = 1;
		private String url = "";
		private String md5 = "";
		private String updateMsg = "";
		private long apkSize = 16172701L;
		
		public int getCode() {
			return code;
		}

		public void setCode(int code) {
			this.code = code;
		}

		public String getErrorMsg() {
			return errorMsg;
		}

		public void setErrorMsg(String errorMsg) {
			this.errorMsg = errorMsg;
		}

		public String getVersion() {
			return version;
		}

		public void setVersion(String version) {
			this.version = version;
		}

		public int getIsupdate() {
			return isupdate;
		}

		public void setIsupdate(int isupdate) {
			this.isupdate = isupdate;
		}

		public String getUrl() {
			return url;
		}

		public void setUrl(String url) {
			this.url = url;
		}

		public String getMd5() {
			return md5;
		}

		public void setMd5(String md5) {
			this.md5 = md5;
		}

		public String getUpdateMsg() {
			return updateMsg;
		}

		public void setUpdateMsg(String updateMsg) {
			this.updateMsg = updateMsg;
		}

		public long getApkSize() {
			return apkSize;
		}

		public void setApkSize(long apkSize) {
			this.apkSize = apkSize;
		}

	}
	@RequestMapping(value = "/external/ios/checkIosOpen", method = {
			RequestMethod.POST, RequestMethod.GET })
	@ResponseBody
	public String checkIosOpen(HttpServletRequest request) {
		int isOpen=0;
		try {
			String version = request.getParameter("version");
			String md5 = request.getParameter("md5");
			if (StringUtils.isNotBlank(md5)&&md5.equals("x1$19p)2fG")) {
				String iosOpenLoginVersion = redisAdapter.strGet("iosOpenLoginVersion_" + version);
				if (StringUtils.isNotBlank(iosOpenLoginVersion) && iosOpenLoginVersion.equals("1")) {
					isOpen = 1;
				}
			}
		}catch(Exception e){e.printStackTrace();}
		Map<String, Object> result =  new HashMap<String, Object>();
		result.put("code", 0);// "code":0, -- 0成功 1失败
		result.put("isOpen", isOpen);//"isOpen":0, --是否打开登陆按钮。0关闭 1打开
		return JSONObject.toJSONString(result);
	}

	public  Map<String,Object> addAnderVersionData (AndroidVersion android) {
        Map<String,Object> result = new HashedMap();
        result.put("updateMsg",android.getUpdateMsg());
        result.put("md5",android.getMd5());
        result.put("errorMsg",android.getErrorMsg());
        result.put("code",android.getCode());
        result.put("url",android.getUrl());
        result.put("apkSize",android.getApkSize());
        result.put("version",android.getVersion());
        result.put("isupdate",android.getIsupdate());
        return result;
    }

	//比较版本号的大小,前者大则返回一个正数,后者大返回一个负数,相等则返回0
	public static int compareVersion(String version1, String version2) throws Exception {
		if (version1 == null || version2 == null) {
			throw new Exception("compareVersion error:illegal params.");
		}
        if ("-1".equals(version1)) {
            return -1;
        }
		String[] versionArray1 = version1.split("\\.");//注意此处为正则匹配，不能用"."；
		String[] versionArray2 = version2.split("\\.");
		int idx = 0;
		int minLength = Math.min(versionArray1.length, versionArray2.length);//取最小长度值
		int diff = 0;
		while (idx < minLength
				&& (diff = versionArray1[idx].length() - versionArray2[idx].length()) == 0//先比较长度
				&& (diff = versionArray1[idx].compareTo(versionArray2[idx])) == 0) {//再比较字符
			++idx;
		}
		//如果已经分出大小，则直接返回，如果未分出大小，则再比较位数，有子版本的为大；
		diff = (diff != 0) ? diff : versionArray1.length - versionArray2.length;
		return diff;
	}
}
