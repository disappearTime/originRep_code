package com.chineseall.iwanvi.wwlive.web.video.controller;

import java.util.HashMap;
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
import com.chineseall.iwanvi.wwlive.web.common.util.ValidationUtils;
import com.chineseall.iwanvi.wwlive.web.follow.service.UserService;
import com.chineseall.iwanvi.wwlive.web.video.service.AnchorService;
import com.chineseall.iwanvi.wwlive.web.video.service.LiveVideoInfo2Service;
import com.chineseall.iwanvi.wwlive.web.video.service.LiveVideoInfoService;

@Controller
@RequestMapping("/app/anchor/")
public class AnchorController {
    
    @Autowired
    private AnchorService anchorService;

    @Autowired
    private LiveVideoInfoService liveVideoInfoService;
    
	@Autowired
	LiveVideoInfo2Service liveVideoInfo2Service;
	
	@Autowired
	private UserService userService;
	
    /**
     * 根据id获得主播详细资料
     * @param request
     * @return
     */
    @RequestMapping(value = "/detail", method = RequestMethod.POST)
    @ResponseBody    
    public ResponseResult<Map<String,Object>> getDetailForApp(HttpServletRequest request){
        ResponseResult<Map<String, Object>> rr = new ResponseResult<>();
        //接口校验
        if(!ValidationUtils.isValid(request, "anchorId")){
            rr.setResponseByResultMsg(ResultMsg.COVER_KEY_CHECK_FAILED);
            return rr;
        }
        
        Long userId = Long.valueOf(request.getParameter("userId"));// 直播2.2.0添加, 判断该用户是否关注该主播
        Long anchorId = Long.valueOf(request.getParameter("anchorId"));
        rr.setData(anchorService.getAnchorInfo(anchorId, userId));
        
        return rr;
    }
    
    /**
     * 主播个人页
     * @return
     */
    @RequestMapping("/personalpage")
    public String toPersonalPage(HttpServletRequest request, Model model){
        Long anchorId = Long.valueOf(request.getParameter("anchorId"));
        String userIdStr = request.getParameter("userId");
        
        Long userId = 0L;
        if(userIdStr.contains("cx")){// 未安装过插件的用户id使用创新版id
            userId = userService.getUserIdByLogin(userIdStr);
        } else{
            userId = Long.valueOf(userIdStr);
        }
        
        //获取主播资料
        Map<String, Object> basicInfo = anchorService.getBasicInfo(anchorId);
        Map<String, Object> videoInfo = liveVideoInfoService.getLivingByAnchorId(anchorId);
    	Map<String, String> params = ControllerRequestUtils.getCommonParam(request);
    	params.put("userId", userId.toString());

    	String videoListPage = PageUrlHelper.buildDynamicUrl(params, anchorId.toString());
        model.addAttribute("anchorInfo", basicInfo);
        model.addAttribute("listPage", videoListPage);
        model.addAttribute("videoInfo", videoInfo);
        model.addAttribute("userId", userId);
        
        return "video/anchor_home";
    }
    
    /**
     * 根据id获得主播详细资料
     * @param request
     * @return
     */
    @RequestMapping(value = "/videolist", method = RequestMethod.POST)
    @ResponseBody    
    public ResponseResult<Map<String,Object>> getAnchorVideoList(HttpServletRequest request){
        ResponseResult<Map<String, Object>> rr = new ResponseResult<>();
        //接口校验
        if(!ValidationUtils.isValid(request, "anchorId")){
            rr.setResponseByResultMsg(ResultMsg.COVER_KEY_CHECK_FAILED);
            return rr;
        }
        //Integer startRow, Integer pageSize
        Long anchorId = Long.valueOf(request.getParameter("anchorId"));
        Integer pageSize = Integer.valueOf(request.getParameter("pageSize"));
        Integer pageNo = Integer.valueOf(request.getParameter("pageNo"));
        rr.setData(anchorService.getAnchorVideoList(anchorId, pageNo, pageSize));
        
        return rr;
    }
    
    @RequestMapping(value = "/video", method = RequestMethod.POST)
    @ResponseBody    
    public ResponseResult<Map<String, Object>> getAnchorVideo(HttpServletRequest request){
        ResponseResult<Map<String, Object>> rr = new ResponseResult<>();
        //接口校验
        if(!ValidationUtils.isValid(request, "anchorId")){
            rr.setResponseByResultMsg(ResultMsg.COVER_KEY_CHECK_FAILED);
            rr.setData(new HashMap<String, Object>());
            return rr;
        }
        //Integer startRow, Integer pageSize
        Long anchorId = Long.valueOf(request.getParameter("anchorId"));
//        Long userId = Long.valueOf(request.getParameter("userId"));
        String loginId = request.getParameter("login_id");
        Map<String, Object> videoInfo = anchorService.getAnchorVideo(anchorId, loginId);
        if (videoInfo != null && !videoInfo.isEmpty()) {
    		rr.setData(videoInfo);
        } else {
            rr.setResponseByResultMsg(ResultMsg.FAIL);
            rr.setData(new HashMap<String, Object>());
        }
        return rr;
    }

    @RequestMapping(value = "/contriblist", method = {RequestMethod.POST,RequestMethod.GET})
    @ResponseBody
    public ResponseResult<Map<String, Object>> getContribList(HttpServletRequest request) {
        ResponseResult<Map<String, Object>> rr = new ResponseResult<>();
        //接口校验
//        if(!ValidationUtils.isValid(request, "anchorId")){
//            rr.setResponseByResultMsg(ResultMsg.COVER_KEY_CHECK_FAILED);
//            return rr;
//        }
        int anchorId = Integer.valueOf(request.getParameter("anchorId"));

        rr.setData(anchorService.getContribList(anchorId));
        return rr;
    }
}
