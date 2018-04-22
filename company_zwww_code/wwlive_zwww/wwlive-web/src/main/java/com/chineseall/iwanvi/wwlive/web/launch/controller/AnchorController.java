package com.chineseall.iwanvi.wwlive.web.launch.controller;

import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.chineseall.iwanvi.wwlive.web.common.util.*;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.alibaba.fastjson.JSONObject;
import com.chineseall.iwanvi.wwlive.common.check.ResponseResult;
import com.chineseall.iwanvi.wwlive.common.check.ResultMsg;
import com.chineseall.iwanvi.wwlive.common.tools.StrMD5;
import com.chineseall.iwanvi.wwlive.domain.wwlive.Anchor;
import com.chineseall.iwanvi.wwlive.web.common.helper.PageUrlHelper;
import com.chineseall.iwanvi.wwlive.web.launch.service.AnchorService;

/**
 * 主播操作controller
 * @author Niu Qianghong
 *
 */
@Controller("launchAnchorController")
@RequestMapping("/launch")
public class AnchorController {
    
    @Autowired
    private AnchorService anchorService;
    
    private Logger log = Logger.getLogger(this.getClass());
    
    /**
     * 获取私信列表
     * @return
     */
    @ResponseBody
    @RequestMapping("/anchor/msglist")
    public ResponseResult<JSONObject> getMsgList(HttpServletRequest request){
        
        //接口校验, 无额外加密字段, 只校验通用参数anchorId, nonce, requestId
        if(!ValidationUtils.isValidForLaunch(request)){
            return new ResponseResult<>(ResultMsg.COVER_KEY_CHECK_FAILED);
        }
        
        ResponseResult<JSONObject> rr = new ResponseResult<>();
        String pageNoStr = request.getParameter("pageNo");//页号从1开始
        String pageSizeStr = request.getParameter("pageSize");
        String anchorIdStr = request.getParameter("anchorId");//收件人id, 当前为主播id
        try {
            Integer pageNo = Integer.valueOf(pageNoStr);
            Integer pageSize = Integer.valueOf(pageSizeStr);
            //分页参数判断
            if(pageNo < 1 || pageSize < 0){
                return new ResponseResult<>(ResultMsg.FAIL_);
            }
            Long anchorId = Long.valueOf(anchorIdStr);
            rr.setData(JsonUtils.toValueOfJsonString(anchorService.getMsgList(pageNo, pageSize, anchorId)));
            rr.setResponseByResultMsg(ResultMsg.SUCCESS_);
        } catch (NumberFormatException e) {
            rr.setResponseByResultMsg(ResultMsg.FAIL_);
            rr.setData(new JSONObject());
            log.error("无法解析数字:", e);
        }   
        return rr;   
    }
    
    /**
     * 获取主播详细信息
     * @param request
     * @return
     */
    @RequestMapping(value = "/anchor/getinfo", method = RequestMethod.POST)
    @ResponseBody    
    public ResponseResult<JSONObject> getDetail(HttpServletRequest request){
        
        //接口校验, 无额外加密字段, 只校验通用参数anchorId, nonce, requestId
        if(!ValidationUtils.isValidForLaunch(request)){
            return new ResponseResult<>(ResultMsg.COVER_KEY_CHECK_FAILED);
        }
        ResponseResult<JSONObject> rr = new ResponseResult<>();
        try {
            Long anchorId = Long.valueOf(request.getParameter("anchorId"));
            rr.setData(JsonUtils.toValueOfJsonString(anchorService.getAnchorDetail(anchorId)));
            rr.setResponseByResultMsg(ResultMsg.SUCCESS_);
        } catch (Exception e) {
            rr.setResponseByResultMsg(ResultMsg.FAIL_);
            rr.setData(new JSONObject());
            log.error("发生异常:", e);
        }
        return rr;
    }
    
    @RequestMapping("/anchor/home")
    public ModelAndView anchorHome(HttpServletRequest request){
        
        ModelAndView mv = new ModelAndView("/launch/anchor_home");
        //接口校验, 无额外加密字段, 只校验通用参数anchorId, nonce, requestId
        if(!ValidationUtils.isValidForLaunch(request)){
            return mv;
        }
        
        String anchorIdStr = request.getParameter("anchorId");//当前主播id
        Long anchorId = Long.valueOf(anchorIdStr);
    	Map<String, String> params = ControllerRequestUtils.getLaunchCommonParam(request);
    	if (params != null && !params.isEmpty()) {
        	String incomePage = PageUrlHelper.buildSortCommonUrl(params);
        	mv.addObject("incomePage", incomePage);
    	}
    	try {
        	mv.addAllObjects(anchorService.getAnchorInfo(anchorId));
    	} catch (Exception e) {
            log.error("发生异常:", e);
    	}
    	return mv;
    }

    @RequestMapping("/anchor/income")
    public ModelAndView anchorIncome(HttpServletRequest request){
        ModelAndView mv = new ModelAndView("/launch/anchor_income");
        
        //接口校验, 无额外加密字段, 只校验通用参数anchorId, nonce, requestId
        if(!ValidationUtils.isValidForLaunch(request)){
            return mv;
        }
        
    	Map<String, String> params = ControllerRequestUtils.getCommonParam(request);
    	if (params != null && !params.isEmpty()) {
        	String listPage = PageUrlHelper.buildSortCommonUrl(params);
        	String giftPage = PageUrlHelper.buildSortCommonUrl(params);
        	mv.addObject("listPage", listPage);
        	mv.addObject("giftPage", giftPage);
        	mv.addAllObjects(params);
    	}
    	return mv;
    }

    @RequestMapping("/anchor/goods/list")
    public ModelAndView goodsList(HttpServletRequest request){
        ModelAndView mv = new ModelAndView("/launch/anchor_gift_list");
        
        //接口校验, 无额外加密字段, 只校验通用参数anchorId, nonce, requestId
        if(!ValidationUtils.isValidForLaunch(request)){
            return mv;
        }
        
    	Map<String, String> params = ControllerRequestUtils.getCommonParam(request);
    	if (params != null && !params.isEmpty()) {
        	String videoId =  request.getParameter("videoId");
        	params.put("videoId", videoId);
        	String listPage = PageUrlHelper.buildSortCommonUrl(params);
        	mv.addObject("listPage", listPage);
        	params.putAll(RequestParamsUtils.defaultRequetParams(new Long(0)));
        	mv.addObject("params", params);
    	}
    	return mv;
    }
    
    @RequestMapping("/anchor/income/list")
    @ResponseBody
    public ResponseResult<Map<String, Object>> getAnchorIncomeList(HttpServletRequest request){
        
        //接口校验, 无额外加密字段, 只校验通用参数anchorId, nonce, requestId
        if(!ValidationUtils.isValidForLaunch(request)){
            return new ResponseResult<>(ResultMsg.COVER_KEY_CHECK_FAILED);
        }
        
    	Long anchorId = ControllerRequestUtils.parseLongFromRquest(request, "anchorId");//当前主播id
    	Integer pageNo = ControllerRequestUtils.parseIntFromRquest(request, "pageNo");//页号
    	Integer pageSize = ControllerRequestUtils.parseIntFromRquest(request, "pageSize");//页大小

    	//分页参数判断
        if(pageNo < 1 || pageSize < 0){
            return new ResponseResult<>(ResultMsg.FAIL_);
        }
    	
        ResponseResult<Map<String, Object>> rr = new ResponseResult<>();
    	try {
        	List<Map<String, Object>> incomeList = anchorService.getAnchorIncomeList(pageNo, pageSize, anchorId);
        	Map<String, Object> result = new HashMap<String, Object>();
        	result.put("incomeList", incomeList);
        	rr.setData(result);
        	rr.setResponseByResultMsg(ResultMsg.SUCCESS_);
    	} catch (Exception e) {
        	rr.setResponseByResultMsg(ResultMsg.FAIL_);
        	log.error("获取收入列表发生异常:", e);
    	}
    	
    	return rr;
    }
    
    /**
     * 跳转到主播修改资料页
     * @param request
     * @param model
     * @return
     */
    @RequestMapping("/anchor/modify/page")
    @ResponseBody
    public ModelAndView modifyHomePage(HttpServletRequest request, ModelAndView model) {
    	Long anchorId = ControllerRequestUtils.parseLongFromRquest(request, "anchorId");//当前主播id
    	model.setViewName("launch/anchor_modify_information");
    	try {
			Map<String, Object> anchor = anchorService.getAnchorInfoForModify(anchorId);
			model.addObject("anchor", anchor);
	    	Map<String, String> params = ControllerRequestUtils.getCommonParam(request);
			model.addObject("modifyUrl", PageUrlHelper.buildSortCommonUrl(params));
			model.addObject("params", params);
		} catch (ParseException e) {
			log.error("跳转到主播修改资料页异常：", e);
		}
    	return model;
    }

    @RequestMapping("/anchor/modify/name/page")
    public String modifyNamePage(HttpServletRequest request, Model model){
    	if(!ValidationUtils.isValidForLaunch(request)){
            return "launch/anchor_modify_username";   
    	}
    	Map<String, String> params = ControllerRequestUtils.getCommonParam(request);
		model.addAttribute("modifyUrl", PageUrlHelper.buildSortCommonUrl(params));
		model.addAttribute("userName", request.getParameter("userName"));
		model.addAttribute("params", params);
        return "launch/anchor_modify_username";   
    }
    
    @RequestMapping("/anchor/modify/username")
    @ResponseBody
    public ResponseResult<Integer> modifyName(HttpServletRequest request){
        //接口校验, 无额外加密字段, 只校验通用参数anchorId, nonce, requestId
        ResponseResult<Integer> rr = validModifyParams(request);
        if(rr != null){
        	return rr;
        }
    	Long anchorId = ControllerRequestUtils.parseLongFromRquest(request, "anchorId");//当前主播id
    	String userName = request.getParameter("userName");
    	if (StringUtils.isBlank(userName)) {
    		rr = new ResponseResult<>(ResultMsg.FAIL_);
    		rr.setData(new Integer(0));
        	return rr;
    	}
    	Anchor anchor = new Anchor();
    	anchor.setAnchorId(anchorId);
    	anchor.setUserName(userName);
    	rr = modifyAnchorInfo(anchor, "修改主播昵称异常：");
    	return rr;
    }
    
    @RequestMapping("/anchor/modify/passwd/page")
    public String modifyPasswdPage(HttpServletRequest request, Model model){
    	if(!ValidationUtils.isValidForLaunch(request)){
            return "launch/anchor_modify_passwd";  
    	}
    	Map<String, String> params = ControllerRequestUtils.getCommonParam(request);
		model.addAttribute("modifyUrl", PageUrlHelper.buildSortCommonUrl(params));
		model.addAttribute("params", params);
        return "launch/anchor_modify_passwd";   
    }

    @RequestMapping("/anchor/modify/passwd")
    @ResponseBody
    public ResponseResult<Integer> modifyPasswd(HttpServletRequest request){
    	ResponseResult<Integer> rr = validModifyParams(request); //接口校验, 无额外加密字段, 只校验通用参数anchorId, nonce, requestId
        if(rr != null){
        	return rr;
        }
    	Long anchorId = ControllerRequestUtils.parseLongFromRquest(request, "anchorId");//当前主播id
    	String passwd = request.getParameter("passwd");
    	if (StringUtils.isBlank(passwd)) {
            rr = new ResponseResult<>(ResultMsg.FAIL_);
    		rr.setData(new Integer(0));
        	return rr;
    	}
    	Anchor anchor = new Anchor();
    	anchor.setAnchorId(anchorId);
        anchor.setPasswd(StrMD5.getInstance().encrypt(passwd, "iwanvi_salt"));
    	rr = modifyAnchorInfo(anchor, "修改主播密码异常：");
    	return rr;
    }
    
    @RequestMapping("/anchor/modify/headImg")
    @ResponseBody
    public ResponseResult<Integer> modifyHeadImg(HttpServletRequest request){
    	ResponseResult<Integer> rr = validModifyParams(request); //接口校验, 无额外加密字段, 只校验通用参数anchorId, nonce, requestId
        if(rr != null){
        	return rr;
        }
    	Long anchorId = ControllerRequestUtils.parseLongFromRquest(request, "anchorId");//当前主播id
    	String headImg = request.getParameter("headImg");
    	if (StringUtils.isBlank(headImg)) {
            rr = new ResponseResult<>(ResultMsg.FAIL_);
    		rr.setData(new Integer(0));
        	return rr;
    	}
    	Anchor anchor = new Anchor();
    	anchor.setAnchorId(anchorId);
    	anchor.setHeadImg(headImg);
    	rr = modifyAnchorInfo(anchor, "修改主播头像异常：");
    	return rr;
    }
    
    @RequestMapping("/anchor/modify/sex")
    @ResponseBody
    public ResponseResult<Integer> modifySex(HttpServletRequest request){
    	ResponseResult<Integer> rr = validModifyParams(request); //接口校验, 无额外加密字段, 只校验通用参数anchorId, nonce, requestId
        if(rr != null){
        	return rr;
        }
    	Long anchorId = ControllerRequestUtils.parseLongFromRquest(request, "anchorId");//当前主播id
    	String sex = request.getParameter("sex");
    	if (StringUtils.isBlank(sex) || !RegexUtils.isNum(sex)) {
            rr = new ResponseResult<>(ResultMsg.FAIL_);
    		rr.setData(new Integer(0));
        	return rr;
    	}
    	Anchor anchor = new Anchor();
    	anchor.setAnchorId(anchorId);
    	anchor.setSex(Integer.parseInt(sex));
    	rr = modifyAnchorInfo(anchor, "修改主播性别异常：");
    	return rr;
    }
    
    @RequestMapping("/anchor/modify/birthday")
    @ResponseBody
    public ResponseResult<Integer> modifyBirthday(HttpServletRequest request){
    	ResponseResult<Integer> rr = validModifyParams(request); //接口校验, 无额外加密字段, 只校验通用参数anchorId, nonce, requestId
        if(rr != null){
        	return rr;
        }
    	Long anchorId = ControllerRequestUtils.parseLongFromRquest(request, "anchorId");//当前主播id
    	String birthday = request.getParameter("birthday");
    	if (StringUtils.isBlank(birthday)) {
            rr = new ResponseResult<>(ResultMsg.FAIL_);
    		rr.setData(new Integer(0));
        	return rr;
    	}
    	Anchor anchor = new Anchor();
    	anchor.setAnchorId(anchorId);
    	try {
    		Date day = DateUtils.parseDate(birthday, new String[] { "yyyy-MM-dd" });
			anchor.setBirthday(day);
			String zodiac = DateTools.getZodiacByDate(day);
			anchor.setZodiac(zodiac);
		} catch (Exception e) {
			log.error("日期转换异常：", e);
		}
    	rr = modifyAnchorInfo(anchor, "修改主播生日异常：");
    	return rr;
    	
    }
    
    @RequestMapping("/anchor/modify/notice/page")
    public String modifyNoticePage(HttpServletRequest request, Model model) throws UnsupportedEncodingException{
    	if(!ValidationUtils.isValidForLaunch(request)){
            return "launch/anchor_modify_notice";   
    	}
    	Map<String, String> params = ControllerRequestUtils.getCommonParam(request);
		model.addAttribute("modifyUrl", PageUrlHelper.buildSortCommonUrl(params));
		model.addAttribute("params", params);
		model.addAttribute("notice", request.getParameter("notice"));//new String((request.getParameter("notice")).getBytes("iso-8859-1"),"utf-8"));
        return "launch/anchor_modify_notice";   
    }

    @RequestMapping("/anchor/modify/notice")
    @ResponseBody
    public ResponseResult<Integer> modifyNotice(HttpServletRequest request){
    	ResponseResult<Integer> rr = validModifyParams(request); //接口校验, 无额外加密字段, 只校验通用参数anchorId, nonce, requestId
        if(rr != null){
        	return rr;
        }
    	Long anchorId = ControllerRequestUtils.parseLongFromRquest(request, "anchorId");//当前主播id
    	String notice = request.getParameter("notice");
    	if (StringUtils.isBlank(notice)) {
            notice = "";
    	}
    	Anchor anchor = new Anchor();
    	anchor.setAnchorId(anchorId);
        anchor.setNotice(notice);
    	rr = modifyAnchorInfo(anchor, "修改主播公告异常：");
    	return rr;
    }

    @RequestMapping("/anchor/get/info")
    @ResponseBody
    public ResponseResult<JSONObject> getAnchorInfo4UpdateIos(HttpServletRequest request){
    	ResponseResult<JSONObject> rr = null; //接口校验, 无额外加密字段, 只校验通用参数anchorId, nonce, requestId
    	if(!ValidationUtils.isValidForLaunch(request)){
            rr = new ResponseResult<>(ResultMsg.COVER_KEY_CHECK_FAILED);
    		rr.setData(new JSONObject());
        }
    	Long anchorId = ControllerRequestUtils.parseLongFromRquest(request, "anchorId");//当前主播id
    	try {
    		Anchor anchor = anchorService.getAnchorInfo4UpdateIos(anchorId);
            rr = new ResponseResult<>(ResultMsg.SUCCESS_);
			rr.setData(JsonUtils.toValueOfJsonString(anchor));
    	} catch (Exception e) {
    		e.printStackTrace();
    		log.error("获取主播信息异常：", e);
            rr = new ResponseResult<>(ResultMsg.FAIL_);
    		rr.setData(new JSONObject());
    	}
    	return rr;
    }
    
    private ResponseResult<Integer> modifyAnchorInfo(Anchor anchor, String error) {
        ResponseResult<Integer> rr = null;
    	try {
    		rr = new ResponseResult<>(ResultMsg.SUCCESS_);
    		rr.setData(anchorService.modifyAnchorInfo(anchor));
		} catch (Exception e) {
			log.error(error, e);
    		rr = new ResponseResult<>(ResultMsg.FAIL_);
    		rr.setData(new Integer(0));
		}
    	return rr;
    }

    private ResponseResult<Integer> validModifyParams(HttpServletRequest request) {
        ResponseResult<Integer> rr = null;
    	if(!ValidationUtils.isValidForLaunch(request)){
            rr = new ResponseResult<>(ResultMsg.COVER_KEY_CHECK_FAILED);
    		rr.setData(new Integer(0));
        }
    	return rr;
    }


    /**
     * 跳转主播收入页
     */
    @RequestMapping("/anchor/toVideoincome")
    public String toVideoincome(HttpServletRequest request, Model model) {
        String anchorId = request.getParameter("anchorId");
        model.addAttribute("anchorId",anchorId);
        return "/launch/anchor_income";
    }

    /**
     * IOS端 主播收入
     */
    @RequestMapping(value = "/anchor/videoincome",method = {RequestMethod.GET,RequestMethod.POST})
    @ResponseBody
	public ResponseResult<Map<String, Object>> getAnchorIncome (HttpServletRequest request, Page page, Model model) {
        String anchorId = request.getParameter("anchorId");
        Integer pageNo = ControllerRequestUtils.parseIntFromRquest(request, "pageNo");//页号
        Integer pageSize = ControllerRequestUtils.parseIntFromRquest(request, "pageSize");//页大小
        ResponseResult<Map<String, Object>> rr = new ResponseResult<>();
        try {
            Map<String, Object> result = anchorService.getAnchorIncome(Long.parseLong(anchorId), pageNo, pageSize);
            rr.setData(result);
            rr.setResponseByResultMsg(ResultMsg.SUCCESS_);
        }catch (Exception e) {
            rr.setResponseByResultMsg(ResultMsg.FAIL_);
            log.error("获取收入列表发生异常:", e);
        }
        return rr;
    }

    /**
     * 跳转收入详情页
     */
    @RequestMapping("/anchor/toDetails")
    public String toDetails(HttpServletRequest request, Model model) {
        String anchorId = request.getParameter("anchorId");
        String videoId = request.getParameter("videoId");
        model.addAttribute("anchorId",anchorId);
        model.addAttribute("videoId",videoId);
        return "/launch/anchor_incomeDetail";
    }

    /**
     * IOS端 收入详情
     */
    @RequestMapping(value = "/anchor/details",method = {RequestMethod.POST,RequestMethod.GET})
    @ResponseBody
    public ResponseResult<Map<String, Object>> incomeDetails (HttpServletRequest request, Page page) {
        Long anchorId = ControllerRequestUtils.parseLongFromRquest(request, "anchorId");//当前主播id
        Integer pageNo = ControllerRequestUtils.parseIntFromRquest(request, "pageNo");//页号
        Integer pageSize = ControllerRequestUtils.parseIntFromRquest(request, "pageSize");//页大小
        Long videoId = ControllerRequestUtils.parseLongFromRquest(request, "videoId");

        ResponseResult<Map<String, Object>> rr = new ResponseResult<>();
        try {
//            anchorService.findIncomeDetailsBy(pageNo,pageSize,anchorId,videoId);
            Map<String, Object> result = anchorService.findIncomeDetailsBy(pageNo, pageSize, anchorId, videoId);
            rr.setData(result);
            rr.setResponseByResultMsg(ResultMsg.SUCCESS_);
        } catch (Exception e) {
            e.printStackTrace();
            rr.setResponseByResultMsg(ResultMsg.FAIL_);
            log.error("获取收入列表发生异常:", e);
        }

        return rr;
    }

    @RequestMapping(value = "/anchor/gameinfo", method = RequestMethod.POST)
    @ResponseBody
    public ResponseResult<Map<String, Object>> getCardFace(HttpServletRequest request, HttpServletResponse response){
        ResponseResult<Map<String, Object>> rr = new ResponseResult<>();
        try {
            Long anchorId = Long.valueOf(request.getParameter("anchorId"));
            Map<String, Object> gameInfo = anchorService.getGameInfo(anchorId);
            rr.setResponseByResultMsg(ResultMsg.SUCCESS);
            rr.setData(gameInfo);
        } catch (Exception e) {
            log.error("获取主播牌面异常", e);
            rr.setResponseByResultMsg(ResultMsg.FAIL);
        }
        String url = request.getHeader("Origin");
        response.setHeader("Access-Control-Allow-Credentials", "true");
        response.setHeader("Access-Control-Allow-Origin", url);
        return rr;
    }
}
