package com.chineseall.iwanvi.wwlive.pc.event.controller;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.chineseall.iwanvi.wwlive.pc.common.loginContext.UserThreadLocalContext;
import com.chineseall.iwanvi.wwlive.pc.event.service.LevelEventService;

@Controller
@RequestMapping("/pc/event/level")
public class LevelEventController {
    
    @Autowired
    private LevelEventService levelEventService;
    
    /**
     * 查询某主播的关卡数和钻石数消息
     * @param requst
     * @return
     */
    @RequestMapping("/data")
    @ResponseBody
    public Map<String, Object> getLevelsAndDiamonds(HttpServletRequest requst){
        long anchorId = UserThreadLocalContext.getCurrentUser().getUserId();
        Double diamonds = levelEventService.getCurDiamonds(anchorId);
        Integer levels = levelEventService.getCurLevels(anchorId);
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("levels", levels);
        resultMap.put("diamonds", diamonds);
        return resultMap;
    }
}
