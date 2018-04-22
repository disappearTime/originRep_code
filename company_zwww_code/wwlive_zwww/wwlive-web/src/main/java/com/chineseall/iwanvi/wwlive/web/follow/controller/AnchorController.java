package com.chineseall.iwanvi.wwlive.web.follow.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

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
import com.chineseall.iwanvi.wwlive.web.follow.service.AnchorService;
import com.chineseall.iwanvi.wwlive.web.video.service.LiveVideoInfoService;

@Controller("followAnchorController")
public class AnchorController {
    
    @Autowired
    private AnchorService anchorService;
    
    @Autowired
    private LiveVideoInfoService liveVideoInfoService;

    @RequestMapping(value = "/external/app/anchor/followcnt", method = RequestMethod.POST)
    @ResponseBody
    public ResponseResult<Map<String, Object>> getFollowerCnt(HttpServletRequest request){
        ResponseResult<Map<String, Object>> rr = new ResponseResult<>();
        try {
            Long anchorId = Long.valueOf(request.getParameter("anchorId"));
            int cnt = anchorService.getFollowerCnt(anchorId);
            Map<String, Object> resultJson = new HashMap<>();
            resultJson.put("followerCnt", cnt);
            rr.setResponseByResultMsg(ResultMsg.SUCCESS);
            rr.setData(resultJson);
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        return rr;
    }
    
    @RequestMapping(value = "/external/app/anchor/info/page", method = RequestMethod.GET)
    public String toInfoPage(HttpServletRequest request, Model model){
        Long anchorId = Long.valueOf(request.getParameter("anchorId"));
        model.addAttribute("anchor", anchorService.getInfo(anchorId));
        return "video/anchor_info";
    }
    
    /**
     * 主播个人页
     * @return
     */
    @RequestMapping("/external/app/anchor/personalpage")
    public String toPersonalPage(HttpServletRequest request, Model model){
        Long anchorId = Long.valueOf(request.getParameter("anchorId"));
        Long userId = Long.valueOf(request.getParameter("userId"));
        //获取主播资料
        Map<String, Object> basicInfo = anchorService.getBasicInfo(anchorId);
        Map<String, Object> videoInfo = liveVideoInfoService.getLivingByAnchorId(anchorId);
        Map<String, String> params = ControllerRequestUtils.getCommonParam(request);

        String videoListPage = PageUrlHelper.buildDynamicUrl(params, anchorId.toString());
        model.addAttribute("anchorInfo", basicInfo);
        model.addAttribute("listPage", videoListPage);
        model.addAttribute("videoInfo", videoInfo);
        model.addAttribute("userId", userId);
        return "video/anchor_home";
    }
    
    /**
     * 主播端分页获取粉丝列表
     * @param request
     * @return
     */
    @RequestMapping(value = "/launch/anchor/follower/page", method = RequestMethod.GET)
    @ResponseBody
    public ResponseResult<List<Map<String, Object>>> getFollowerPage(HttpServletRequest request){
        ResponseResult<List<Map<String, Object>>> rr = new ResponseResult<>();
        Long anchorId = Long.valueOf(request.getParameter("anchorId"));
        Integer pageNo = Integer.valueOf(request.getParameter("pageNo"));
        Long timestamp = Long.valueOf(request.getParameter("timestamp"));
        List<Map<String, Object>> followerPage = anchorService.getFollowPage(anchorId, pageNo, timestamp);
        rr.setData(followerPage);
        rr.setResponseByResultMsg(ResultMsg.SUCCESS);
        return rr;
    }
    
    /**
     * 跳转到粉丝列表
     * @return
     */
    @RequestMapping("/launch/anchor/follower/list")
    public String toFollowerList(Model model, HttpServletRequest request){
        Map<String, String> params = ControllerRequestUtils.getCommonParam(request);
        if (params != null && !params.isEmpty()) {
            String listPage = PageUrlHelper.buildSortCommonUrl(params);
            model.addAttribute("commonParams", listPage);
            params.putAll(RequestParamsUtils.defaultRequetParams(new Long(0)));
            model.addAttribute("params", params);
        }
        return "launch/fans_list";
    }
}
