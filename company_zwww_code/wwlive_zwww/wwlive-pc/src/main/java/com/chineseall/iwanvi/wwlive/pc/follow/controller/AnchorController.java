package com.chineseall.iwanvi.wwlive.pc.follow.controller;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.chineseall.iwanvi.wwlive.pc.common.Page;
import com.chineseall.iwanvi.wwlive.pc.common.loginContext.UserThreadLocalContext;
import com.chineseall.iwanvi.wwlive.pc.follow.service.AnchorService;

@Controller("followAnchorController")
@RequestMapping("/anchor")
public class AnchorController {
    
    @Autowired
    private AnchorService anchorService;
    
    /**
     * 主播端分页获取粉丝列表
     * @param request
     * @return
     */
    @RequestMapping(value = "/follower/page", method = RequestMethod.GET)
    public String getFollowerPage(Page page, Model model, HttpServletRequest request){
        long anchorId = UserThreadLocalContext.getCurrentUser().getUserId();
        // Integer pageNo = Integer.valueOf(request.getParameter("pageNo"));
        Long timestamp = Long.valueOf(request.getParameter("timestamp"));
        Page followerPage = anchorService.getFollowPage(anchorId, page, timestamp);
        //Page resultPage = anchorService.getAdmins(anchorId, page);
        model.addAttribute("page", followerPage);
        return "anchor/follower_table";
    }
    
    @RequestMapping(value = "/follower/list", method = RequestMethod.GET)
    public String toFansList(){
        return "anchor/follower_list";
    }
}
