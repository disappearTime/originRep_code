package com.chineseall.iwanvi.wwlive.web.event.controller;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.chineseall.iwanvi.wwlive.common.check.ResponseResult;
import com.chineseall.iwanvi.wwlive.common.check.ResultMsg;
import com.chineseall.iwanvi.wwlive.web.common.helper.PageUrlHelper;
import com.chineseall.iwanvi.wwlive.web.common.util.ControllerRequestUtils;
import com.chineseall.iwanvi.wwlive.web.common.util.RequestParamsUtils;
import com.chineseall.iwanvi.wwlive.web.event.service.LevelEventService;
import org.springframework.web.servlet.ModelAndView;

/**
 * 活动controller
 * @author Niu Qianghong
 *
 */

@Controller
@RequestMapping("/external/event")
public class EventController {
    
    @Autowired
    private LevelEventService levelService; // 关卡活动service
    
    /**
     * 访问关卡活动页, 页面上带回主播排行榜信息
     * @return
     */
    @RequestMapping("/level")
    public String toLevelPage(HttpServletRequest request, Model model){
        
        Long userId = ControllerRequestUtils.parseLongFromRquest(request, "userId");
        Map<String, String> params = ControllerRequestUtils.getCommonParam(request);
        model.addAttribute("consPage", PageUrlHelper.buildVideoContributionListUrl(params));
        Map<String, String> requestParams = RequestParamsUtils.defaultRequetParams(userId);
        params.putAll(requestParams);
        model.addAttribute("params", params);
        
        List<Map<String, Object>> anchorRank = levelService.getAnchorRank(); // 取出排名前十的主播
        int isInEvent = levelService.getEventStatus();
        model.addAttribute("anchorRank", anchorRank);
        model.addAttribute("isInEvent", isInEvent); // =1活动进行中; =0活动未开始; =2活动已结束
        return "activity/goddess_activity";
    }
    
    /**
     * 分页获取用户排行榜信息
     * @param request
     * @return
     */
    @RequestMapping("/level/rank/user")
    @ResponseBody
    public ResponseResult<List<Map<String, Object>>> getUserRankByPage(HttpServletRequest request){
        Integer pageNo = Integer.valueOf(request.getParameter("pageNo"));
        Integer pageSize = Integer.valueOf(request.getParameter("pageSize"));
        List<Map<String, Object>> userRank = levelService.getUserRank(pageNo, pageSize);
        ResponseResult<List<Map<String, Object>>> rr = new ResponseResult<>();
        rr.setResponseByResultMsg(ResultMsg.SUCCESS);
        rr.setData(userRank);
        return rr;
    }


}
