package com.chineseall.iwanvi.wwlive.web.common.util;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;

import com.chineseall.iwanvi.wwlive.common.check.ResponseResult;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class LogUtils {
    
    private Logger logger;
    
    public LogUtils(Class<?> clazz){
        this.logger = Logger.getLogger(clazz);
    }
    
    public void logParam(String actionName, HttpServletRequest request, String... params){
        logger.info("----->" + actionName + "参数列表<-----");
        for(String param:params){
            String value = request.getParameter(param);
            logger.info(param + "->" + value);
        }
    }
    
    public <T> void logResult(String actionName, ResponseResult<T> rr){
        ObjectMapper om = new ObjectMapper();
        try {
            String json = om.writeValueAsString(rr);
            logger.info("----->" + actionName + "结果json<-----");
            logger.info(json);
        } catch (JsonProcessingException e) {
            logger.info("+++++++++++对象转json的时候出现异常+++++++++++");
            e.printStackTrace();
        }
    }
    
    public Logger getLogger() {
    	return this.logger;
    }
}
