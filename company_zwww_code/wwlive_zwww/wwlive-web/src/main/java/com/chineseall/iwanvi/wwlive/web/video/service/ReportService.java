package com.chineseall.iwanvi.wwlive.web.video.service;

import java.util.Map;


public interface ReportService {
    
    Map<String, Object> report(Long reportedKey, int reportKind, int reportType, Long reportedUserId, Long userId);

}
