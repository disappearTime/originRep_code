package com.chineseall.iwanvi.wwlive.pc.video.controller;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.chineseall.iwanvi.wwlive.pc.common.Page;
import com.chineseall.iwanvi.wwlive.pc.common.loginContext.UserThreadLocalContext;
import com.chineseall.iwanvi.wwlive.pc.video.service.ChatLetterService;

@Controller
@RequestMapping("/pc/letter")
public class ChatLetterController {

	@Autowired
	ChatLetterService chatLetterService;

	@RequestMapping(value = "/page", method = RequestMethod.GET)
	public String toPage(HttpServletRequest request) {
		return "/letter/index_page";	
	}
	
	/**
	 * 客户端给主播发私信
	 * 
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/list", method = RequestMethod.POST)
	public String getLetters(@ModelAttribute Page page,
			HttpServletRequest request, Model model) {
        long anchorId = UserThreadLocalContext.getCurrentUser().getUserId();
        Map<String, Object> result = chatLetterService.getLetters(page, anchorId);
		
		model.addAllAttributes(result);
		return "/letter/chat_letter";	
	}

    /**
     * 返回用户的视频数、收入和未读消息数
     * @param request
     * @return
     */
    @RequestMapping(value = "/notread", method = RequestMethod.POST)
    @ResponseBody
    public String getNoReadLetterNum(HttpServletRequest request){
        long anchorId = UserThreadLocalContext.getCurrentUser().getUserId();
        return chatLetterService.getNoReadLetterNum(anchorId);
    }
}
