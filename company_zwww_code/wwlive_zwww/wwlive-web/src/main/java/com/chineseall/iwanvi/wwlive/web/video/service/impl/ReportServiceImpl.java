package com.chineseall.iwanvi.wwlive.web.video.service.impl;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.chineseall.iwanvi.wwlive.common.cache.RedisClientAdapter;
import com.chineseall.iwanvi.wwlive.common.cache.RedisKey;
import com.chineseall.iwanvi.wwlive.dao.wwlive.ReportDetailMapper;
import com.chineseall.iwanvi.wwlive.dao.wwlive.ReportInfoMapper;
import com.chineseall.iwanvi.wwlive.domain.wwlive.ReportInfo;
import com.chineseall.iwanvi.wwlive.web.common.constants.WebConstants;
import com.chineseall.iwanvi.wwlive.web.video.service.ReportService;

@Service
public class ReportServiceImpl implements ReportService {
    
    private Logger logger = Logger.getLogger(this.getClass());

    @Autowired
    private ReportInfoMapper reportInfoMapper;
    @Autowired
    private ReportDetailMapper reportDetailMapper;

    @Autowired
    private RedisClientAdapter redisAdapter;

    @Override
    public Map<String, Object> report(Long reportedKey, int reportKind, int reportType, Long reportedUserId, Long userId) {
        Map<String, Object> data = new HashMap<>();
        // 判断该用户是否举报过该对象
        if (redisAdapter.existsKey(RedisKey.REPORT_ + userId + reportedKey)) {
            data.put("result", WebConstants.SUCCESS);
        } else {
            // 添加举报记录到数据库中
            try {
                // 判断该对象是否被举报过
                ReportInfo reportInfo = reportInfoMapper.findByKeyAndType(reportedKey, reportType);
                if (reportInfo == null) {
                    // 第一次被举报
                    // 新建被举报记录, 返回记录id
                    reportInfo = new ReportInfo();
                    reportInfo.setReportedKey(reportedKey);
                    reportInfo.setReportKind(reportKind);
                    reportInfo.setReportedUserId(reportedUserId);
                    reportInfo.setReportType(reportType);
                    reportInfoMapper.add(reportInfo);
                } else {
                    // 举报次数+1
                    reportInfo.setReportCnt(reportInfo.getReportCnt() + 1);
                    // 更新被举报记录
                    reportInfoMapper.modifyReportCnt(reportInfo);
                }
                // 添加举报详情记录
                reportDetailMapper.add(reportInfo.getReportId(), userId);
                // 举报记录在redis中保存3分钟
                redisAdapter.strSetEx(RedisKey.REPORT_ + userId + reportedKey, "0", 3 * 60);
                data.put("result", WebConstants.SUCCESS);
            } catch (Exception e) {
                logger.error("App端举报接口中, userId = " + userId+ ", reprotedKey = " + reportedKey + "reportType = " + reportType + "的举报记录添加时异常.");
                data.put("result", WebConstants.FAIL);
            }
        }
        return data;
    }

}
