package com.chineseall.iwanvi.wwlive.web.common.embedding;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.Iterator;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.log4j.Logger;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.chineseall.iwanvi.wwlive.web.common.util.DateTools;

/**
 * 数据埋点工具
 * @author DIKEPU
 *
 */
public class DataEmbeddingTools {

    static final Logger LOGGER = Logger.getLogger("dataEmbedding");

    private final static String TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
    
    private final static String APP_NAME = "zb";
    /**
     * 参数不能为空
     * @param request
     * @param uid
     * @param actionName
     * @param imei
     */
    @Deprecated
    public static void insertLog(HttpServletRequest request, String uid, String actionName,
            String imei) {
        if(request == null 
                || StringUtils.isEmpty(uid) 
                || StringUtils.isEmpty(actionName) || StringUtils.isEmpty(imei)) {
            return;
        }
        String ip = getIpAddress(request);
        LOGGER.info(actionName + "\t" + ip  + "\t" + DateFormatUtils.format(new Date(), TIME_FORMAT) + "\t"
                + uid + "\t" + imei + "\t" + request.getHeader("user-agent") + "\t" + "iwanvi-zb");
    }
    
    /** 
     * 获取用户真实IP地址
     * @param request 
     * @return 
     */ 
    public static String getIpAddress(HttpServletRequest request) { 
      String ip = request.getHeader("x-forwarded-for"); 
      if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) { 
        ip = request.getHeader("Proxy-Client-IP"); 
      } 
      if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) { 
        ip = request.getHeader("WL-Proxy-Client-IP"); 
      } 
      /*if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) { 
        ip = request.getHeader("HTTP_CLIENT_IP"); 
      } 
      if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) { 
        ip = request.getHeader("HTTP_X_FORWARDED_FOR"); 
      } 
      if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) { 
        ip = request.getRemoteAddr(); 
      }*/
      if(ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {  
    	  ip = request.getRemoteAddr();  
          if(ip.equals("127.0.0.1") || ip.equals("0:0:0:0:0:0:0:1")){  
              //根据网卡取本机配置的IP  
              InetAddress inet=null;  
              try {  
                  inet = InetAddress.getLocalHost();  
              } catch (UnknownHostException e) {  
                  e.printStackTrace();  
              }  
              ip= inet.getHostAddress();  
          }  
      }  
      //对于通过多个代理的情况，第一个IP为客户端真实IP,多个IP按照','分割  
      if(ip!=null && ip.length() > 15){ //"***.***.***.***".length() = 15  
          if(ip.indexOf(",")>0){  
        	  ip = ip.substring(0, ip.indexOf(","));  
          }  
      }  
      return ip; 
    }

    /**
     * 数组方式打印日志
     * @param actionTime
     * @param ip
     * @param userId 
     * @param actionName
     * @param IMEI
     * @param agent
     */
    @Deprecated
    public static void insertLog(String actionTime, String ip, String userId, String actionName, String IMEI, String agent) {
        if(StringUtils.isEmpty(actionTime)
                || StringUtils.isEmpty(userId) 
                || StringUtils.isEmpty(actionName) || StringUtils.isEmpty(IMEI)) {
            return;
        }

        LOGGER.info(actionName + "\t" + ip + "\t" + actionTime + "\t"
                + userId + "\t" + IMEI + "\t" + agent + "\t" + "iwanvi-zb");
    } 
    
    /**
     * 服务端，通用日志打印接口
     * @param pft
     * @param pfp
     * @param id
     * @param did
     * @param request
     */
    public static void insertLog(String pft, String pfp, String id, String did, HttpServletRequest request){
        if(request == null){
            return;
        }
        StringBuilder sb = new StringBuilder();
        if ("dl".equals(request.getParameter("app"))) {
        	pft = "1" + pft;
        }
        sb.append(pft + "\t");
        sb.append(pfp + "\t");
        //ip
        sb.append(getStrValue(getIpAddress(request)) + "\t");
        //时间    time
        sb.append(DateTools.formatDate(new Date(), TIME_FORMAT) + "\t");
        //用户id  uid
        sb.append(getStrValue(request.getParameter("userId")) + "\t");
        //渠道id  cnid
        sb.append(getStrValue(request.getParameter("cnid")) + "\t");
        //版本    version
        sb.append(getStrValue(request.getParameter("version")) + "\t");
        //终端号IMEI   imei
        sb.append( getStrValue(request.getParameter("IMEI")) + "\t");
        //机型    model
        sb.append(getStrValue(request.getParameter("model")) + "\t");
        //平台    platform
        sb.append(getStrValue(request.getParameter("platform")) + "\t");
        //user agent    ua
        sb.append(getStrValue( request.getHeader("user-agent")) + "\t");
        //app名称 appname
        sb.append(APP_NAME + "\t");
        //id    id
        sb.append(getStrValue(id) + "\t");
        //备用id  did
        sb.append(getStrValue(did) + "\t");
        //自定义字段     jsonstring
        sb.append(getStrValue(request.getParameter("jsonstring")));
        LOGGER.info(sb.toString());
    }

    /**
     * 客户端，用于处理通用log接口的请求日志打印
     * @param request
     */
    public static void commonLog(String uid, String cnid, String version, String model, String imei, String platform,
            String ua, String ip, String jsonData) {
        if(jsonData == null || "".equals(jsonData)){
            return;
        }
        JSONArray jsonArray = JSON.parseArray(jsonData);
        if(jsonArray != null){
            Iterator<Object> it = jsonArray.iterator();
            while(it.hasNext()){
                StringBuilder sb = new StringBuilder();
                JSONObject jsonObj = (JSONObject) it.next();
                String actionTime = jsonObj.getString("actionTime");
                String pft = jsonObj.getString("pft");
                String pfp = jsonObj.getString("pfp");
                String id = jsonObj.getString("id");
                String did = jsonObj.getString("did");
                String jsonstring = jsonObj.getString("jsonstring");
                sb.append(getStrValue(pft) + "\t")
                .append(getStrValue(pfp) + "\t")
                .append(getStrValue(ip) + "\t")
                .append(getStrValue(actionTime) + "\t")
                .append(getStrValue(uid) + "\t")
                .append(getStrValue(cnid) + "\t")
                .append(getStrValue(version) + "\t")
                .append(getStrValue(imei) + "\t")
                .append(getStrValue(model) + "\t")
                .append(getStrValue(platform) + "\t")
                .append(getStrValue(ua) + "\t")
                .append(APP_NAME + "\t")
                .append(getStrValue(id) + "\t")
                .append(getStrValue(did) + "\t")
                .append(getStrValue(jsonstring));
                LOGGER.info(sb.toString());
            } 
        }        
        
    }
    
    private static String getStrValue(String str) {
        if (StringUtils.isBlank(str)) {
            return "";
        }
        return str.trim();
    }
    
}
