package com.chineseall.iwanvi.wwlive.web.common.spring;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;

public class LaunchObjectMapper extends ObjectMapper {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8207900096073525372L;

	public LaunchObjectMapper() {
		this.getSerializerProvider().setNullValueSerializer(
				new JsonSerializer<Object>() {

					@Override
					public void serialize(Object value, JsonGenerator gen,
							SerializerProvider serializers) throws IOException,
							JsonProcessingException {
						gen.writeString("");
					}
				});
	}

}
