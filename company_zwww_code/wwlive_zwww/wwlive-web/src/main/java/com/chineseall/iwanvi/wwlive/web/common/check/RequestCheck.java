package com.chineseall.iwanvi.wwlive.web.common.check;

import org.springframework.util.Assert;

import com.alibaba.fastjson.JSONObject;
import com.chineseall.iwanvi.wwlive.common.check.ResponseResult;
import com.chineseall.iwanvi.wwlive.common.check.ResultMsg;
import com.chineseall.iwanvi.wwlive.common.tools.StrMD5;

/**
 * 校验前端参数请求是否符合加密
 * @author DIKEPU
 *
 */
public class RequestCheck {

	/**
	 * 如果没有成功，返回string类型的json字符串，如果通过返回null
	 * @return
	 */
	public String checkParamMd5Hex(String coverKey, Object... args) {
		Assert.noNullElements(args, "请求参数不能为空.");
		Assert.hasText(coverKey, "");
		String tempKey = StrMD5.getInstance().signature(args);
		
		if (!coverKey.equals(tempKey)) {
			ResponseResult<String> response = new ResponseResult<String>();
			response.setResponseByResultMsg(ResultMsg.COVER_KEY_CHECK_FAILED);
			return JSONObject.toJSONString(response);
		}
		return null;
	}
}
