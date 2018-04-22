package com.chineseall.iwanvi.wwlive.web.video.controller;

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;


@Controller
public class VideoTestController {
	
	/**
	 * 假官网
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/external/xingyu/index")
	public ModelAndView leaveLiveVideo(HttpServletRequest request, ModelAndView model) {
		model.addObject("download", "http://zb.cread.com/docs/apk/xingyu/xingyu-live-1.0.0.apk");
		model.setViewName("xingyu_index");
		return model;
	}
}
