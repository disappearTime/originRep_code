package com.chineseall.iwanvi.wwlive.web.video.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.chineseall.iwanvi.wwlive.common.cache.RedisClientAdapter;
import com.chineseall.iwanvi.wwlive.common.cache.RedisKey;
import com.chineseall.iwanvi.wwlive.common.check.ResponseResult;
import com.chineseall.iwanvi.wwlive.common.check.ResultMsg;
import com.chineseall.iwanvi.wwlive.common.external.kscloud.KSCloudFacade;
import com.chineseall.iwanvi.wwlive.web.common.embedding.DataEmbeddingTools;
import com.chineseall.iwanvi.wwlive.web.common.helper.PageUrlHelper;
import com.chineseall.iwanvi.wwlive.web.common.util.ControllerRequestUtils;
import com.chineseall.iwanvi.wwlive.web.common.util.RequestParamsUtils;
import com.chineseall.iwanvi.wwlive.web.common.util.ValidationUtils;
import com.chineseall.iwanvi.wwlive.web.video.service.LiveVideoInfoService;
import com.chineseall.iwanvi.wwlive.web.video.vo.VideoJsVo;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.*;

/**
 * 视频直播controller
 *
 */
@Controller
public class LiveVideoInfoController {
    
	private Logger logger = Logger.getLogger(this.getClass());
	
    @Autowired
    private LiveVideoInfoService liveVideoInfoService;
    
    @Value("${cxapp.android.download.path}")
    private String apkAddr;
    
    // 补充ios软件下载地址
    @Value("")
    private String iosAddr;
    
    @Autowired
    private RedisClientAdapter redisAdapter;
    
    /**
     * 获取正在直播视频列表
     * @param request
     * @return
     */
//    @RequestMapping(value = "/external/livevideo/list", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public ResponseResult<Map<String, Object>> getLiveVideoList(HttpServletRequest request){
        ResponseResult<Map<String, Object>> rr = new ResponseResult<>();        
        rr.setData(liveVideoInfoService.getLivingVideos());
        rr.setResponseByResultMsg(ResultMsg.SUCCESS);
        return rr;
    }

    /**
     * 获取正在直播视频列表，H5页面
     * @param request
     * @return
     */
    @RequestMapping(value = "/external/video/living", method = {RequestMethod.GET, RequestMethod.POST})
    public String getLiveVideos(HttpServletRequest request, Model model){
        try {
            
            Map<String, Object> result = null;
            
            String cnid = request.getParameter("cnid");
            //灰度
            String channel = redisAdapter.strGet("qudao");
            if(StringUtils.isNotBlank(channel) && channel.equals(cnid)){
                result = liveVideoInfoService.getGrayLivingVideos();
            } else{
                result = liveVideoInfoService.getLivingVideos();
            }
            
            String version=request.getParameter("version");
            //将信息封装，加快直播的展示
            if(result!=null&&!result.isEmpty()){
                if(result.get("videoList")!=null){
                    List<Map<String, String>> videoList= (List<Map<String, String>>) result.get("videoList");
                    for(Map<String, String> obj:videoList){
                        VideoJsVo js=new VideoJsVo();
                        String stream_name= MapUtils.getString(obj,"chatroomId","");
                        String[]	liuArgs=KSCloudFacade.getRtmpURLs(stream_name);
                        js.setStandURL(liuArgs[0]);
                        js.setHeighURL(liuArgs[1]);
                        js.setFullHeighURL(liuArgs[2]);
                        try {
                            obj.put("ext", URLEncoder.encode(JSON.toJSONString(js),"utf-8"));
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }
                        obj.put("version", version==null?"":version);
                    }
                }
            }
            model.addAllAttributes(result);
        	
        } catch (Exception e) {
        	logger.error("获得直播视频列表异常", e);
        }
        return "video/index_videos";
    }
    
    /**
     * 直播首页
     * @param request
     * @param model
     * @return
     */
    @RequestMapping(value = "/external/video/home", method = {RequestMethod.GET, RequestMethod.POST})
    public ModelAndView videoHome(HttpServletRequest request, ModelAndView model){
        model = new ModelAndView("video/index_home");
        model.addObject("params", getParam(request));
        
        //埋点
        DataEmbeddingTools.insertLog("7001", "1-1", "", "", request);
        return model;
    }
    
    /**
     * 获得参数
     * @param request
     * @return Map 参数名称为key，值为value
     */
    private Map<String, String> getParam(HttpServletRequest request) {
		Enumeration<?> enumeration = request.getParameterNames();
		Map<String, String> paramMap = new HashMap<String, String>();
		while (enumeration.hasMoreElements()) {
			Object name = enumeration.nextElement();
			if (name instanceof String) {
				paramMap.put(name.toString(),
						request.getParameter(name.toString()));
			}
		}

		return paramMap;
	}
    
    /**
     * 回放
     * @param request
     * @return
     */
    @RequestMapping(value = "/external/video/lived", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public Map<String, Object> getHistoryVideos(HttpServletRequest request){
        int pageSize = Integer.valueOf(request.getParameter("pageSize"));
        int pageNo = Integer.valueOf(request.getParameter("pageNo"));
        String cnid = request.getParameter("cnid");
        
        Map<String, Object> result =  liveVideoInfoService.getHistotyVideoList(pageSize, pageNo, cnid);
        
        return result;
    }
    
    /**
     * 获取历史直播视频列表, 按照人数排序
     * @param request
     * @return
     */
//    @RequestMapping(value = "/external/livevideo/history", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public ResponseResult<Map<String,Object>> getHistoryVideoList(HttpServletRequest request){
        ResponseResult<Map<String, Object>> rr = new ResponseResult<>();
        
        int pageSize = Integer.valueOf(request.getParameter("pageSize"));
        int pageNo = Integer.valueOf(request.getParameter("pageNo"));
        String cnid = request.getParameter("cnid");
        
        rr.setData(liveVideoInfoService.getHistotyVideoList(pageSize, pageNo, cnid));
        rr.setResponseByResultMsg(ResultMsg.SUCCESS);
        
        return rr;        
    }
    
    /**
     * 在直播间中查看排行榜
     * @param request
     * @return
     */
    @RequestMapping(value = "/app/livevideo/ranklist", method = RequestMethod.POST)
    @ResponseBody
    public ResponseResult<Map<String,Object>> getRankList(HttpServletRequest request){
        ResponseResult<Map<String, Object>> rr = checkGetRankList(request);
        if (rr != null) {
        	return rr;
        }
        //接口校验
        int anchorId = Integer.valueOf(request.getParameter("anchorId"));
        int pageNo = Integer.valueOf(request.getParameter("pageNo"));
        int pageSize = Integer.valueOf(request.getParameter("pageSize"));

    	rr = new ResponseResult<>();
        rr.setData(liveVideoInfoService.getRankList(pageNo, pageSize, anchorId));
        rr.setResponseByResultMsg(ResultMsg.SUCCESS);
        
        return rr;
    }
    
    /**
     * 校验查看排行参数
     * @param request
     * @return
     */
    private ResponseResult<Map<String, Object>> checkGetRankList(HttpServletRequest request) {
        ResponseResult<Map<String, Object>> rr = null;

        if(!ValidationUtils.isValid(request, "anchorId")){
        	rr = new ResponseResult<>();
            rr.setResponseByResultMsg(ResultMsg.COVER_KEY_CHECK_FAILED);
            return rr;
        }
        if (StringUtils.isBlank(request.getParameter("anchorId")) 
        		|| StringUtils.isBlank(request.getParameter("pageNo")) 
        				|| StringUtils.isBlank(request.getParameter("pageSize"))) {
        	rr = new ResponseResult<>();
            rr.setResponseByResultMsg(ResultMsg.LOST_PARAMS);
            return rr;
        }
        return rr;
    }
    
    /**
     * 点击观看历史直播, 获得历史视频详情
     * @param request
     * @return
     */
    @RequestMapping(value = "/app/livevideo/historydetail", method = {RequestMethod.POST,RequestMethod.GET})
    @ResponseBody
    public ResponseResult<Map<String, Object>> enterHistoryVideo(HttpServletRequest request){
        ResponseResult<Map<String, Object>> rr = checkEnterHistoryVideo(request);
        if (rr != null) {
        	return rr;
        }

        //参数需要用户id, 和videoid
        long userId = Integer.valueOf(request.getParameter("userId"));
        long anchorId = Long.valueOf(request.getParameter("anchorId"));
        long videoId = Integer.valueOf(request.getParameter("videoId"));
 		String loginId = request.getParameter("loginId");
        String cnid = request.getParameter("cnid");
        String version = request.getParameter("version");
 		
        try {
            rr = new ResponseResult<>();
            rr.setData(liveVideoInfoService.getHistoryVideoDetail(userId, anchorId, videoId, loginId,cnid,version));
        }catch (Exception e) {
        	logger.error("获得历史信息失败", e);
            rr = new ResponseResult<>(ResultMsg.FAIL);
            return rr;
        }
        //埋点
        DataEmbeddingTools.insertLog("7003", "1-3", anchorId + "", videoId + "", request);
    
        return rr;
    }

    private ResponseResult<Map<String, Object>> checkEnterHistoryVideo(HttpServletRequest request) {
        ResponseResult<Map<String, Object>> rr = null;
        if(!ValidationUtils.isValid(request)){
        	rr = new ResponseResult<>();
        	rr.setResponseByResultMsg(ResultMsg.COVER_KEY_CHECK_FAILED);
            return rr;
        }

        if (StringUtils.isBlank(request.getParameter("userId")) 
        		|| StringUtils.isBlank(request.getParameter("anchorId")) 
        				|| StringUtils.isBlank(request.getParameter("videoId"))) {
        	rr = new ResponseResult<>();
            rr.setResponseByResultMsg(ResultMsg.LOST_PARAMS);
            return rr;
        	
        }
        return rr;
    }
    
    /**
     * 用户观看历史直播退出时, 修改表中记录
     * @param request
     * @return
     */
    @RequestMapping(value = "/app/livevideo/endwatch", method = RequestMethod.POST)
    @ResponseBody
    public ResponseResult<Map<String, Object>> exitHistoryVideo(HttpServletRequest request){
        ResponseResult<Map<String, Object>> rr = checkExitHistoryVideo(request);
        if (rr != null) {
        	return rr;
        }
        
        long userId = Integer.valueOf(request.getParameter("userId"));
        long videoId = 0;
        if (StringUtils.isNotBlank(request.getParameter("videoId"))) {
        	videoId = Integer.valueOf(request.getParameter("videoId"));
        }

    	rr = new ResponseResult<>();
        rr.setData(liveVideoInfoService.exitHistoryVideo(userId, videoId));
        return rr;
    }

    private ResponseResult<Map<String, Object>> checkExitHistoryVideo(HttpServletRequest request) {
        ResponseResult<Map<String, Object>> rr = null;
        if(!ValidationUtils.isValid(request)){
        	rr = new ResponseResult<>();
        	rr.setResponseByResultMsg(ResultMsg.COVER_KEY_CHECK_FAILED);
            return rr;
        }
        if (StringUtils.isBlank(request.getParameter("userId"))) {
        	rr = new ResponseResult<>();
            rr.setResponseByResultMsg(ResultMsg.LOST_PARAMS);
            return rr;
        	
        }
        return rr;
    }
    
    /**
     * 通用log接口, 适用于静音, 送礼 
     * @param request
     */
    @RequestMapping(value = "/app/livevideo/commonlog", method = RequestMethod.POST)
    @ResponseBody
    public ResponseResult<Map<String, Object>> commonLog(HttpServletRequest request){

        ResponseResult<Map<String, Object>> rr = new ResponseResult<>();
        if(!ValidationUtils.isValid(request)){
        	rr.setResponseByResultMsg(ResultMsg.COVER_KEY_CHECK_FAILED);
            return rr;
        }
        
        String uid = request.getParameter("userId");
        String cnid = request.getParameter("cnid");
        String version = request.getParameter("version");
        String model = request.getParameter("model");
        String imei = request.getParameter("IMEI");
        String platform = request.getParameter("platform"); 
        String ua = request.getHeader("user-agent");
        String ip = DataEmbeddingTools.getIpAddress(request);
        String jsonData = request.getParameter("jsonData");
        
        DataEmbeddingTools.commonLog(uid, cnid, version, model, imei, platform, ua, ip, jsonData);

        rr = new ResponseResult<>(ResultMsg.SUCCESS);
        return rr;
    }
    
    /**
     * 分享直播视频
     * @param request
     * @return
     */
    @RequestMapping(value = "/external/livevideo/share", method = RequestMethod.GET)
    public String share(HttpServletRequest request, HttpServletResponse response, Model model){
        try {
        	String anchorIdStr = request.getParameter("anchorId");
        	String videoIdStr = request.getParameter("videoId");
        	//String formatType = request.getParameter("formatType");
        	response.setHeader("Cache-Control","no-store");
        	response.setHeader("Pragrma","no-cache");
        	response.setDateHeader("Expires",0);
        	if (StringUtils.isBlank(anchorIdStr) 
        			|| StringUtils.isBlank(videoIdStr)) {
        		return "video/share_video_mobile";
        	}
            Long anchorId = Long.valueOf(anchorIdStr);
            Long videoId = Long.valueOf(videoIdStr);
            int osType = Integer.valueOf(StringUtils.isBlank(request.getParameter("osType")) ? "0" : request.getParameter("osType"));
            Map<String, Object> shareModel = liveVideoInfoService.getShareModel(anchorId, videoId);
            Integer formatType = (Integer) shareModel.get("formatType");
            //手机操作系统 0=安卓, 1=ios
            if(osType == 0){
                shareModel.put("downloadLink", apkAddr);
            } else if(osType == 1){
                shareModel.put("downloadLink", iosAddr);
            }
            model.addAttribute("shareModel", shareModel);
            
            String[] deviceArray = new String[]{"Android", "iPhone"}; 
            String userAgent = request.getHeader("User-Agent");
            for(String device:deviceArray){
                if(userAgent.indexOf(device) > 0){
                    //此时为移动设备
                    if(formatType != null && formatType == 1){
                        //竖版分享着陆页
                        return "video/share_video_vertical";
                    } else{
                        //旧版分享着陆页
                        return "video/share_video_mobile";  
                    }
                }
            }
            //此时为PC访问
        } catch (Exception e) {
        	logger.error("获得历史信息失败", e);
        }
        return "video/share_video_pc";
    }
    
    /**
     * appStore 直播视频
     * @param request
     * @return
     */
    @RequestMapping(value = "/external/video/show", method = RequestMethod.GET)
    public String appStroreTest(HttpServletRequest request, HttpServletResponse response, Model model){
        try {
        	response.setHeader("Cache-Control","no-store");
        	response.setHeader("Pragrma","no-cache");
        	response.setDateHeader("Expires",0);
            Map<String, Object> shareModel = liveVideoInfoService.appStroreTest();
            //手机操作系统 0=安卓, 1=ios 
            model.addAttribute("shareModel", shareModel);
        } catch (Exception e) {
        	logger.error("获得历史信息失败", e);
        }
        return "video/appstore_video";
    }
    /**
     * 贡献榜
     * @param request
     * @return
     */
    @RequestMapping("/app/video/consInfo")
    public ModelAndView videoConsInfo(HttpServletRequest request){
    	ModelAndView mv = new ModelAndView("/video/video_rank_info");
        // String videoIdStr = request.getParameter("videoId");
        String anchorIdStr = request.getParameter("anchorId");
        if (StringUtils.isBlank(anchorIdStr)){ 
        		//|| StringUtils.isBlank(videoIdStr)) {
        	return mv;
        }
        Long userId = ControllerRequestUtils.parseLongFromRquest(request, "userId");
    	Map<String, String> params = ControllerRequestUtils.getCommonParam(request);
        mv.addObject("consPage", PageUrlHelper.buildVideoContributionListUrl(params));
        Map<String, String> requestParams = RequestParamsUtils.defaultRequetParams(userId);
        params.putAll(requestParams);
        mv.addObject("params", params);
    	return mv;
    }

    /**
     * 查询火箭获得集合接口
     * @param request
     * @return
     */
    @RequestMapping(value = "/app/video/rocketinfos", method = {RequestMethod.POST, RequestMethod.GET })
    @ResponseBody
    public ResponseResult<Map<String, Object>> getRocketInfoList(HttpServletRequest request){
        ResponseResult<Map<String, Object>> rr = new ResponseResult<>();
        Map<String, Object> result =  new HashMap<String, Object>();
        try {
            Long anchorId = Long.valueOf(request.getParameter("anchorId"));
            List<JSONObject> data = liveVideoInfoService.getRocketInfoList(anchorId);
            result.put("rocketInfoList", data);
            rr.setData(result);
            rr.setResponseByResultMsg(ResultMsg.SUCCESS);
        } catch (Exception e) {
        	e.printStackTrace();
        	logger.error("获得火箭信息失败：", e);
            rr.setResponseByResultMsg(ResultMsg.FAIL);
        }
    	return rr;
    }
    
    /**
     * 获取随机的一个直播间
     * @param request
     * @return
     */
    @RequestMapping(value = "/external/rad/getRandomLive", method = {RequestMethod.POST, RequestMethod.GET })
    @ResponseBody
    public Map<String, Object> getRandomLive(HttpServletRequest request){
        Map<String, Object> result =  new HashMap<String, Object>();
        Map<String, Object> param =  new HashMap<String, Object>();
        int anchorId=0;
        try {
            // 从redis中获取正在直播的视频id集合
            String videoKey = RedisKey.VideoKeys.LIVING_VIDEOS_;// + dateInfo
            Set<String> ids =redisAdapter.zsetRevRange(videoKey, 0, -1);

            if(ids!=null&&!ids.isEmpty()){
                List<Integer> idsList = new ArrayList<>();
                for (String id : ids) {
                    // 排除定制版的主播直播，以后更改为创建直播时控制
                    if(redisAdapter.setIsMember(RedisKey.DINGZHI_ANCHOR,id)){
                        continue;
                    }
                    if(!id.equals("0")){
                    idsList.add(Integer.valueOf(id));
                    }
                }
                Collections.shuffle(idsList);
                anchorId=idsList.get(0);
                //{"fun":"live","data":{"anchorId": 1633524,"push":1,"isRandom":1}}
                param.put("push", 1);
                param.put("isRandom", 1);
                param.put("anchorId", anchorId);
                result.put("fun", "live");
                result.put("data", param);
                return result;
            }
        } catch (Exception e) {
            logger.error("获得赚钱页随机直播间异常", e);
        }
        result.put("fun", "live");
        result.put("data", param);
        return result;
    }
}
