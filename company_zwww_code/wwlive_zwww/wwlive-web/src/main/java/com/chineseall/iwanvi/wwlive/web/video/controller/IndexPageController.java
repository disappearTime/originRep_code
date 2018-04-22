package com.chineseall.iwanvi.wwlive.web.video.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.chineseall.iwanvi.wwlive.common.check.ResponseResult;
import com.chineseall.iwanvi.wwlive.common.check.ResultMsg;
import com.chineseall.iwanvi.wwlive.web.video.service.PublicNoticeService;

@Controller
public class IndexPageController {
    
    @Autowired
    private PublicNoticeService publicNoticeService;

    /**
     * 获取当天最新的一则预告
     * @return
     */
    @RequestMapping(value = "/external/index/todayprenotice")
    @ResponseBody
    public ResponseResult<String> getTodayPrenotice(){
        String notice = publicNoticeService.getTodayNotice();
        if(notice.isEmpty()){
            return new ResponseResult<>(ResultMsg.FAIL);
        }
        ResponseResult<String> rr = new ResponseResult<>(ResultMsg.SUCCESS);
        rr.setData(notice);
        return rr;
    }
}