package com.chineseall.iwanvi.wwlive.web.launch.controller;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSONObject;
import com.chineseall.iwanvi.wwlive.common.check.ResponseResult;
import com.chineseall.iwanvi.wwlive.common.check.ResultMsg;
import com.chineseall.iwanvi.wwlive.web.common.util.JsonUtils;
import com.chineseall.iwanvi.wwlive.web.common.util.ValidationUtils;
import com.chineseall.iwanvi.wwlive.web.launch.service.FileUploadService;

@Controller
public class FileUploadController {

    private Logger logger = Logger.getLogger(this.getClass());
    
	@Autowired
	FileUploadService fileUploadService;
	
	@RequestMapping(value = "/launch/common/img/upload", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public ResponseResult<JSONObject> uploadInfo(HttpServletRequest request){
	    
        //接口校验, 无额外加密字段, 只校验通用参数anchorId, nonce, requestId
        if(!ValidationUtils.isValidForLaunch(request)){
            return new ResponseResult<>(ResultMsg.COVER_KEY_CHECK_FAILED);
        }
		ResponseResult<JSONObject> result = new ResponseResult<JSONObject>();
	    try {
			Map<String, Object> upload = fileUploadService.uploadAndSave(request);
			if (upload == null || upload.isEmpty()) {
		    	ResultMsg fail = ResultMsg.FAIL_;
		    	fail.setInfo("上传图片失败");
				result.setResponseByResultMsg(fail);
				result.setData(new JSONObject());
				return result;
			}
			result.setResponseByResultMsg(ResultMsg.SUCCESS_);
			result.setData(JsonUtils.toValueOfJsonString(upload));
	    } catch (Exception e) {
	    	e.printStackTrace();
	    	ResultMsg fail = ResultMsg.FAIL_;
	    	fail.setInfo("上传图片失败");
			result.setResponseByResultMsg(fail);
			result.setData(new JSONObject());
	    	logger.error("上传图片失败：", e);
	    }
		return result;
	}
}
