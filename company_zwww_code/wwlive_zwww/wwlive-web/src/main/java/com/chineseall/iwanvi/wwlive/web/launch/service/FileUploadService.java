package com.chineseall.iwanvi.wwlive.web.launch.service;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

public interface FileUploadService {
	public Map<String, Object> uploadAndSave(HttpServletRequest request);
}
