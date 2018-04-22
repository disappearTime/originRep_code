package com.chineseall.iwanvi.wwlive.pc.video.controller;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.chineseall.iwanvi.wwlive.pc.common.Page;
import com.chineseall.iwanvi.wwlive.pc.video.service.OrderInfoService;

@Controller
@RequestMapping("/pc/order")
public class OrderInfoController {
	
	@Autowired
	OrderInfoService orderInfoService;
	
	@RequestMapping(value = "detailList", method = RequestMethod.POST)
	public String getVideoInfo(Page page, HttpServletRequest request, Model model){
		long videoId = Long.valueOf(request.getParameter("videoId"));
		
		Page resultPage = orderInfoService.getOrderInfoByOrigKey(videoId, page);
//        page.setUrl("/pc/order/detailList/?pageIndex=%pageIndex%");
        model.addAttribute("page", resultPage);
		return "/live/live_income_detail";
	}
	
	
}
