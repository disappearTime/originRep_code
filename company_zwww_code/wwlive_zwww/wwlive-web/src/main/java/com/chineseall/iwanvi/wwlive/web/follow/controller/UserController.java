package com.chineseall.iwanvi.wwlive.web.follow.controller;

import com.alibaba.fastjson.JSON;
import com.chineseall.iwanvi.wwlive.common.check.ResponseResult;
import com.chineseall.iwanvi.wwlive.common.check.ResultMsg;
import com.chineseall.iwanvi.wwlive.common.external.kscloud.KSCloudFacade;
import com.chineseall.iwanvi.wwlive.web.common.embedding.DataEmbeddingTools;
import com.chineseall.iwanvi.wwlive.web.common.helper.PageUrlHelper;
import com.chineseall.iwanvi.wwlive.web.common.util.ControllerRequestUtils;
import com.chineseall.iwanvi.wwlive.web.common.util.LogUtils;
import com.chineseall.iwanvi.wwlive.web.follow.service.UserService;
import com.chineseall.iwanvi.wwlive.web.video.vo.VideoJsVo;
import com.zw.zcf.util.StringUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller("followUserController")
public class UserController {
    
    @Autowired
    private UserService userService;
    
    // private Logger logger = Logger.getLogger(this.getClass());
    private LogUtils logUtil = new LogUtils(this.getClass());
    
    private static final String FROM_ANCHOR_CENTER = "anchorCenter"; // 来自主播个人页 
    
    //private static final String FROM_CHATROOM = "chatroom"; // 来自app聊天室
    
    private static final String FROM_TAB_PAGE = "tabPage"; // 来自tab页     
    
    @RequestMapping("/external/app/user/getid")
    @ResponseBody
    public Map<String, Object> getUserIdByLoginId(HttpServletRequest request){
        String userIdStr = request.getParameter("userId");
        Long userId = userService.getUserIdByLogin(userIdStr);
        Map<String, Object> map = new HashMap<>();
        map.put("userId", userId);
        return map;
    }
    
    /**
     * 获取关注列表, 若没有关注任何主播, 显示推荐列表
     * @return
     */
    @RequestMapping("/external/app/user/follow/list")
    public String toFollowList(HttpServletRequest request, Model model){
      try {
          Long userId = null;
          String loginId = request.getParameter("loginId");
          String app = request.getParameter("app");
          if (StringUtils.isNotBlank(loginId)) {

              Map<String, String> params = ControllerRequestUtils.getCommonParam(request);
              String commonParams = "";
              if (params != null && !params.isEmpty()) {
                  params.remove("version");
                  commonParams = PageUrlHelper.buildCommonUrlWithoutUser(params);
              }

              userId = userService.getUserIdByLogin(loginId);
              model.addAttribute("commonParams", commonParams + "&userId=" + userId);
          }

          model.addAttribute("app",app);
          String userIdStr = request.getParameter("userId");
          if (StringUtils.isNotBlank(userIdStr)) {
              userId = Long.valueOf(userIdStr);
              // 通用参数设定, 在页面上发送ajax请求时使用
              Map<String, String> params = ControllerRequestUtils.getCommonParam(request);
              if (params != null && !params.isEmpty()) {
                  String commonParams = PageUrlHelper.buildCommonUrl(params);
                  model.addAttribute("commonParams", commonParams);
              }
          }

          // 在页面上添加版本号, 做版本控制; 低于220版本的用户不能跳转到主播个人页
          model.addAttribute("version", request.getParameter("version"));

          String cnid = request.getParameter("cnid");

          int follows = userService.getFollowedCnt(userId);// 获取用户关注的主播个数
          if (follows == 0) {
              return getRecommend(model, cnid);
          } else {
              model.addAttribute("userId", userId);
              model.addAttribute("followCnt", follows);
              return "follow/follow_page";
          }
      }catch (Exception ex){
          ex.printStackTrace();
      }
        return "follow/follow_page";
    }   
    
    /**
     * 分页获取关注列表
//     * @param userId
     * @param request
     * @param model
     * @return
     */
    @RequestMapping("/external/app/user/follow/list/page")
    public String getFollowList(HttpServletRequest request, Model model) {
        try {
            Long userId = Long.valueOf(request.getParameter("userId"));
            String pageNoStr = request.getParameter("pageNo");
            String timestampStr = request.getParameter("timestamp");
            String cnid = request.getParameter("cnid");
            int pageNo = 1;// 默认第1页
            long timestamp = 0L;
            if(StringUtils.isNotBlank(pageNoStr) && StringUtils.isNotBlank(timestampStr)){
                pageNo = Integer.valueOf(pageNoStr);
                timestamp = new BigDecimal(timestampStr).longValue();
            }
            Map<String, Object> followMap = userService.getFollowList(userId, pageNo, timestamp, cnid);
            model.addAttribute("followMap", followMap);
            model.addAttribute("pageNo", pageNo);
        } catch (Exception e) {
            e.printStackTrace();
        }
        //model.addAttribute("userId", userId);
        return "follow/follow_list";
    }
    
    @RequestMapping("/external/app/user/follow/page1")
    @ResponseBody
    public String get10Follows(HttpServletRequest request){
        
        Long userId = null;
        String loginId = request.getParameter("loginId");
        if(StringUtils.isNotBlank(loginId)){
            userId = userService.getUserIdByLogin(loginId);
        }
        
        String userIdStr = request.getParameter("userId");
        if(StringUtils.isNotBlank(userIdStr)){
            userId = Long.valueOf(userIdStr);
        }
        String anchorIds = "";
        try {
            anchorIds = userService.get1stPageFollows(userId);
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        return anchorIds;        
    }

    /**
     * 获取推荐列表
     * @param model
     * @param cnid 
     * @return
     */
    private String getRecommend(Model model, String cnid){
        List<Map<String, Object>> recommends = userService.getRecommend(cnid);
        if(recommends != null){
        //List<Map<String, Object>> lives =(List<Map<String, Object>>)recommends.get("lives");
        for (Map map:recommends){
            //将信息封装，加快直播的展示
            VideoJsVo js=new VideoJsVo();
            String stream_name= org.apache.commons.collections.MapUtils.getString(map,"chatroomId","");
            String[] liuArgs= KSCloudFacade.getRtmpURLs(stream_name);
            js.setStandURL(liuArgs!=null?liuArgs[0]:"");
            js.setHeighURL(liuArgs!=null?liuArgs[1]:"");
            js.setFullHeighURL(liuArgs!=null?liuArgs[2]:"");
            try {
                map.put("ext", URLEncoder.encode(JSON.toJSONString(js),"utf-8"));
            } catch (Exception e) {
                e.printStackTrace();
            }
         }
        }
        model.addAttribute("recommends", recommends);
        return "follow/recommend_list";
    }
    
    /**
     * 用户关注主播
     * @param request
     * @return
     */
    @RequestMapping(value = "/external/user/follow", method = RequestMethod.POST)
    @ResponseBody
    public ResponseResult<Map<String, Object>> follow(HttpServletRequest request){
        ResponseResult<Map<String, Object>> rr = new ResponseResult<>();
        
        String userIdStr = request.getParameter("userId");
        Long userId = 0L;
        if(userIdStr.contains("cx")){// 创新版和插件访问关注列表都用这个接口, userId格式不同, 这条语句进行判断获取插件userId
            userId = userService.getUserIdByLogin(userIdStr);
        } else{
            userId = Long.valueOf(userIdStr);
        }        
        String anchorIdStr = request.getParameter("anchorId");       
        String followFrom = request.getParameter("followFrom");        
        try {
            Long anchorId = Long.valueOf(anchorIdStr);
            int result = userService.follow(userId, anchorId);
            Map<String, Object> data = new HashMap<>();
            data.put("result", result);
            rr.setResponseByResultMsg(ResultMsg.SUCCESS);
            rr.setData(data );
            
            if(FROM_ANCHOR_CENTER.equals(followFrom)){
                DataEmbeddingTools.insertLog("7007", "1-2", "", anchorId.toString(), request);
            } else if(FROM_TAB_PAGE.equals(followFrom)){
                DataEmbeddingTools.insertLog("7007", "1-1", "", anchorId.toString(), request);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return rr;
    }
    
    /**
     * 用户取消关注主播
     * @param request
     * @return
     */
    @RequestMapping(value = "/app/user/unfollow", method = RequestMethod.POST)
    @ResponseBody
    public ResponseResult<Map<String, Object>> unfollow(HttpServletRequest request){
        ResponseResult<Map<String, Object>> rr = new ResponseResult<>();
        
        logUtil.logParam("取消关注", request, "userId", "anchorId");
        
        String userIdStr = request.getParameter("userId");
        String anchorIdStr = request.getParameter("anchorId");
        String unfollowFrom = request.getParameter("unfollowFrom");// 主播个人页 = anchorCenter; 客户端聊天室 = chatroom
        
        try {
            Long userId = Long.valueOf(userIdStr);
            Long anchorId = Long.valueOf(anchorIdStr);
            int result = userService.unfollow(userId, anchorId);
            Map<String, Object> data = new HashMap<>();
            data.put("result", result);
            rr.setResponseByResultMsg(ResultMsg.SUCCESS);
            rr.setData(data );
            
            if(FROM_ANCHOR_CENTER.equals(unfollowFrom)){
                DataEmbeddingTools.insertLog("7007", "1-3", "", anchorId.toString(), request);
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        logUtil.logResult("取消关注", rr);
        return rr;
    }
    
    @RequestMapping(value = "/external/user/follow/check", method = RequestMethod.POST)
    @ResponseBody
    public ResponseResult<Map<String, Object>> checkfollow(HttpServletRequest request){
        ResponseResult<Map<String, Object>> rr = new ResponseResult<>();
        String userIdStr = request.getParameter("userId");
        String anchorIdStr = request.getParameter("anchorId");
        try {
            Long userId = Long.valueOf(userIdStr);
            Long anchorId = Long.valueOf(anchorIdStr);
            int result = userService.isFollower(anchorId, userId);
            Map<String, Object> data = new HashMap<>();
            data.put("result", result);
            rr.setResponseByResultMsg(ResultMsg.SUCCESS);
            rr.setData(data );
        } catch (Exception e) {
            e.printStackTrace();
        }
        return rr;
    }
    
    /**
     * 在我的页面展示自己关注的3个主播
     * @param request
     * @return
     */
    @RequestMapping(value = "/app/user/follow/top3")//, method = RequestMethod.POST)
    @ResponseBody
    public ResponseResult<Map<String, Object>> getFollowTop3(HttpServletRequest request){
        ResponseResult<Map<String, Object>> rr = new ResponseResult<>();
        try {
            Long userId = Long.valueOf(request.getParameter("userId"));
            String cnid = request.getParameter("cnid");
            Map<String, Object> followTop3 = userService.getFollowTop3(userId, cnid);
            rr.setData(followTop3);
            rr.setResponseByResultMsg(ResultMsg.SUCCESS);
            return rr;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return rr;
    }
}