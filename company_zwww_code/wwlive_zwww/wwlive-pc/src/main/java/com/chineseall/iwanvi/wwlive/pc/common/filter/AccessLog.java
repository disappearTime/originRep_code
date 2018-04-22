package com.chineseall.iwanvi.wwlive.pc.common.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.log4j.Logger;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

/**
 * 访问日志纪录类
 * Created by kai
 */
public class AccessLog implements Serializable {

    /**
	 * 
	 */
	private static final long serialVersionUID = -4632769226228911623L;

	private static Logger accessLog = Logger.getLogger("accessLog");

    private static Logger logger = Logger.getLogger(AccessLog.class);

    private static final String SPILT = "\001";

    private SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    // 系统名称
    private String system = "wwlive";

    // 访问时间
    private Date accessDate;

    // 访问的用户pin
    private String pin;

    // referer
    private String referer;

    // 请求uri
    private String uri;

    // 请求url
    private String url;

    // 请求方式
    private String methodType;

    // 请求IP
    private String ip;

    // 请求ua
    private String ua;

    // 处理时间，单位毫秒
    private Integer procTime;

    // 会话id，浏览器关闭切换
    private String ssid;

    // 万维client标识
    private String wwa;

    // 请求方式 form｜ajax
    private String reqType;

    //所有参数集合
    private Map<String, String> paramMap;

    //扩展字段集合
    private Map<String, String> extendMap;


    public void writeToFile() {

        try {

            StringBuilder stringBuilder = new StringBuilder();

            stringBuilder.append(system).append(SPILT)
                    .append(DATE_FORMAT.format(accessDate)).append(SPILT)
                    .append(pin).append(SPILT)
                    .append(referer).append(SPILT)
                    .append(uri).append(SPILT)
                    .append(url).append(SPILT)
                    .append(methodType).append(SPILT)
                    .append(ip).append(SPILT)
                    .append(ua).append(SPILT)
                    .append(procTime).append(SPILT)
                    .append(ssid).append(SPILT)
                    .append(wwa).append(SPILT)
                    .append(reqType).append(SPILT);

            if (paramMap != null) {
                ObjectMapper objectMapper = new ObjectMapper();
                stringBuilder.append(objectMapper.writeValueAsString(paramMap));
            }
            stringBuilder.append(SPILT);
            if (extendMap != null) {
                ObjectMapper objectMapper = new ObjectMapper();
                stringBuilder.append(objectMapper.writeValueAsString(extendMap));
            }

            accessLog.info(stringBuilder.toString());
        } catch (Exception ex) {
            logger.error("accessLog记录出错",ex);
        }

    }

    public String getSystem() {
        return system;
    }

    public void setSystem(String system) {
        this.system = system;
    }

    public Date getAccessDate() {
        return accessDate;
    }

    public void setAccessDate(Date accessDate) {
        this.accessDate = accessDate;
    }

    public String getPin() {
        return pin;
    }

    public void setPin(String pin) {
        this.pin = pin;
    }

    public String getReferer() {
        return referer;
    }

    public void setReferer(String referer) {
        this.referer = referer;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getMethodType() {
        return methodType;
    }

    public void setMethodType(String methodType) {
        this.methodType = methodType;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getUa() {
        return ua;
    }

    public void setUa(String ua) {
        this.ua = ua;
    }

    public Integer getProcTime() {
        return procTime;
    }

    public void setProcTime(Integer procTime) {
        this.procTime = procTime;
    }

    public String getSsid() {
        return ssid;
    }

    public void setSsid(String ssid) {
        this.ssid = ssid;
    }

    public String getWwa() {
        return wwa;
    }

    public void setWwa(String wwa) {
        this.wwa = wwa;
    }

    public String getReqType() {
        return reqType;
    }

    public void setReqType(String reqType) {
        this.reqType = reqType;
    }

    public Map<String, String> getParamMap() {
        return paramMap;
    }

    public void setParamMap(Map<String, String> paramMap) {
        this.paramMap = paramMap;
    }

    public Map<String, String> getExtendMap() {
        return extendMap;
    }

    public void setExtendMap(Map<String, String> extendMap) {
        this.extendMap = extendMap;
    }
}
