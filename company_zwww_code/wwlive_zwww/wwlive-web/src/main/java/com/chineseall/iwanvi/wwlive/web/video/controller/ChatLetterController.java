package com.chineseall.iwanvi.wwlive.web.video.controller;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.chineseall.iwanvi.wwlive.common.check.ResponseResult;
import com.chineseall.iwanvi.wwlive.common.check.ResultMsg;
import com.chineseall.iwanvi.wwlive.web.common.util.ControllerRequestUtils;
import com.chineseall.iwanvi.wwlive.web.common.util.ValidationUtils;
import com.chineseall.iwanvi.wwlive.web.video.service.ChatLetterService;

@Controller
public class ChatLetterController {

	@Autowired
	private ChatLetterService chatLetterService;

	/**
	 * 客户端给主播发私信
	 * 
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/app/letter/send", method = RequestMethod.POST)
	@ResponseBody
	public ResponseResult<Integer> sendLetter(
			HttpServletRequest request) {
		ResponseResult<Integer> rr = checkSendLetter(request);
		if (rr != null) {
			return rr;
		}

		Long userId = Long.parseLong(request.getParameter("userId"));
		Long anchorId = Long.parseLong(request.getParameter("anchorId"));
		Long videoId = ControllerRequestUtils.parseLongFromRquest(request, "videoId");
		String content = request.getParameter("content");
		
		rr = new ResponseResult<>();
		rr.setData(new Integer(chatLetterService.sendLetter(userId, anchorId, videoId, content)));
		rr.setResponseByResultMsg(ResultMsg.SUCCESS);
		return rr;
	}

	private ResponseResult<Integer> checkSendLetter(
			HttpServletRequest request) {
		ResponseResult<Integer> rr = null;
		String requestId = request.getParameter("requestId");
		if (!ValidationUtils.isValid(request)) {
			rr = new ResponseResult<>();
			rr.setResponseByResultMsg(ResultMsg.COVER_KEY_CHECK_FAILED);
			rr.setRequestId(requestId);
			return rr;
		}
		if (StringUtils.isBlank(request.getParameter("userId"))
				|| StringUtils.isBlank(request.getParameter("anchorId"))
				|| StringUtils.isBlank(request.getParameter("content"))) {
			rr = new ResponseResult<>();
			rr.setResponseByResultMsg(ResultMsg.LOST_PARAMS);
			rr.setRequestId(requestId);
			return rr;
		}
		return rr;
	}

}
