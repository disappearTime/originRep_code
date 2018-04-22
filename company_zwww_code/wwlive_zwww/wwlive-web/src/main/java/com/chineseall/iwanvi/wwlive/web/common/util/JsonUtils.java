package com.chineseall.iwanvi.wwlive.web.common.util;

import java.math.BigDecimal;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.ValueFilter;

public class JsonUtils {

	public static JSONObject toValueOfJsonString(Object obj) {
		if (obj == null) {
			return new JSONObject();
		}
		return JSONObject.parseObject(JSONObject.toJSONString(obj, new JsonValueFilter()));
	}

	public static String toJsonString(Object obj) {
		return JSONObject.toJSONString(obj, new JsonValueFilter());
	}
	
	private static class JsonValueFilter implements ValueFilter {

		@Override
		public Object process(Object object, String name, Object value) {
			if (value == null) {
				return null;
			}
			if(value instanceof BigDecimal){
				//格式化BigDecimal ，去除小数点后面的0
				value = (BigDecimal)value;
				return String.valueOf(value);
			}
			if(value instanceof Double){
				return String.valueOf(value);
			}
			if(value instanceof Integer){
				return String.valueOf(value);
			}
			if(value instanceof Long){
				try{
					return String.valueOf(value);
				}catch(Exception e){
					return value;
				}
			}
			return value;
		}
		
	}
}
