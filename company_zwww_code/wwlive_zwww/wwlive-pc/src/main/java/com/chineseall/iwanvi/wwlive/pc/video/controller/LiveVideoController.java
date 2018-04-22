package com.chineseall.iwanvi.wwlive.pc.video.controller;

import com.alibaba.fastjson.JSON;
import com.chineseall.iwanvi.wwlive.common.cache.RedisClientAdapter;
import com.chineseall.iwanvi.wwlive.common.check.ResponseResult;
import com.chineseall.iwanvi.wwlive.common.check.ResultMsg;
import com.chineseall.iwanvi.wwlive.common.constants.Constants;
import com.chineseall.iwanvi.wwlive.common.tools.ImgFileUpLoadUtils;
import com.chineseall.iwanvi.wwlive.common.tools.img.VideoCoverImg;
import com.chineseall.iwanvi.wwlive.pc.common.Page;
import com.chineseall.iwanvi.wwlive.pc.common.RequestUtils;
import com.chineseall.iwanvi.wwlive.pc.common.WebConstants;
import com.chineseall.iwanvi.wwlive.pc.common.loginContext.UserThreadLocalContext;
import com.chineseall.iwanvi.wwlive.pc.video.common.PcConstants;
import com.chineseall.iwanvi.wwlive.pc.video.service.LiveVideoService;
import com.chineseall.iwanvi.wwlive.pc.video.util.DateUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/live")
public class LiveVideoController {

    private Logger logger = Logger.getLogger(this.getClass());

    @Autowired
    private LiveVideoService liveVideoService;

    @Value("${img.path}")
    private String imgPath;

    @Value("${img.url}")
    private String imgUrl;

    @Value("${video.img}")
    private String videoImg;

    @Autowired
    private RedisClientAdapter redisAdapter;
    /**
     * 首页跳转
     * 
     * @param page
     * @param request
     * @param model
     * @return
     */
    @RequestMapping("/index")
    public String toLivePage(Page page, HttpServletRequest request, Model model) {
        long anchorId = UserThreadLocalContext.getCurrentUser().getUserId();

        Page pageResult = liveVideoService.getVideoList(anchorId, page);

        page.setUrl("/live/index?pageIndex=%pageIndex%");

        model.addAttribute("page", pageResult);
        return "live/anchor_video";
    }

    /**
     * 创建视频
     * 
     * @param request
     * @return
     */
    @RequestMapping(value = "create", method = RequestMethod.POST)
    @ResponseBody
    public ResponseResult<Map<String, Object>> createVideo(HttpServletRequest request) {
        String videoName = request.getParameter("videoName");
        int videoType = Integer.valueOf(request.getParameter("videoType"));
        int needRecord = Integer.valueOf(request.getParameter("needRecord"));
        long anchorId = UserThreadLocalContext.getCurrentUser().getUserId();
        
        ResponseResult<Map<String, Object>> result = null; 
		Map<String, Object> res = new HashMap<String, Object>();

		String coverImg = null;
		CommonsMultipartResolver multipartResolver = new CommonsMultipartResolver(
				request.getSession().getServletContext());
		if (multipartResolver.isMultipart(request)) {
			MultipartHttpServletRequest multiRequest = (MultipartHttpServletRequest) request;
			MultipartFile file = multiRequest.getFile("coverImg");
			try {
				coverImg = ImgFileUpLoadUtils.uploadFileToLocal(file, imgPath, imgUrl, new VideoCoverImg());//上传图片
				if (StringUtils.isBlank(coverImg)) {
					result = new ResponseResult<Map<String, Object>>(ResultMsg.FAIL);
					res.put("result", 0);
					result.setInfo("上传图片失败");
					result.setData(res);
					return result;
				}
			} catch (Exception e) {
				logger.error("上传图片失败：", e);
				result = new ResponseResult<Map<String, Object>>(ResultMsg.FAIL);
				res.put("result", 0);
				result.setInfo(e.getMessage());
				result.setData(res);
				return result;
			}// 生成上传文件路径
		}
		
		res = liveVideoService.createVideoUrl(videoName, videoType,
				coverImg, needRecord > 0, anchorId);// 保存视频信息
		result = new ResponseResult<Map<String, Object>>(ResultMsg.SUCCESS);
		result.setData(res);
       
        return result;
    }

    @RequestMapping(value = "getAnchorVideos")
    public ResponseResult<Map<String, Object>> getVideoList(Page page, HttpServletRequest request) {
        long anchorId = UserThreadLocalContext.getCurrentUser().getUserId();

        page = liveVideoService.getVideoList(anchorId, page);
        ResponseResult<Map<String, Object>> result = new ResponseResult<Map<String, Object>>(ResultMsg.SUCCESS);
        return result;
    }

    @RequestMapping(value = "modify", method = RequestMethod.POST)
    @ResponseBody
    public ResponseResult<Map<String, Object>> modifyVideo(HttpServletRequest request) {
    	//1.获得修改参数 2.如果有图片则上传 3.修改完后更新相应的视频信息
    	//1.获得修改参数 
        long anchorId = UserThreadLocalContext.getCurrentUser().getUserId();
        String videoName = request.getParameter("videoName");
        long videoId = Long.parseLong(request.getParameter("videoId"));
        Integer videoType = request.getParameter("videoType") == null ? null
                : Integer.parseInt(request.getParameter("videoType"));
//        String oldImg = request.getParameter("oldImg");
        
        ResponseResult<Map<String, Object>> result = null;
        Map<String, Object> resultMap = new HashMap<>();
        
        // 2.如果有图片则上传 
        String coverImg = null;
        CommonsMultipartResolver multipartResolver = new CommonsMultipartResolver(
                request.getSession().getServletContext());
        if (multipartResolver.isMultipart(request)) {
            MultipartHttpServletRequest multiRequest = (MultipartHttpServletRequest) request;
            MultipartFile file = multiRequest.getFile("coverImg");
            try {
				coverImg = ImgFileUpLoadUtils.uploadFileToLocal(file, imgPath, imgUrl, new VideoCoverImg());//上传图片
            } catch (Exception e) {
           		logger.error("上传图片失败：", e);
                result = new ResponseResult<Map<String, Object>>(ResultMsg.FAIL);
                resultMap.put("result", 0);
                result.setInfo(e.getMessage());
                result.setData(resultMap);
                return result;            
            }
        }
        
        //3.修改完后更新相应的视频信息
        int data = liveVideoService.modifyVideoInfo(videoId, coverImg, videoName, anchorId, videoType);
        result = new ResponseResult<Map<String, Object>>(ResultMsg.SUCCESS);
        resultMap.put("result", data);
        result.setData(resultMap);
        return result;
    }

    /**
     * 删除视频信息
     * 
     * @param request
     */
    @RequestMapping(value = "delete", method = RequestMethod.POST)
    @ResponseBody
    public ResponseResult<Integer> deleteVideo(HttpServletRequest request) {
        long anchorId = UserThreadLocalContext.getCurrentUser().getUserId();
        long videoId = Long.valueOf(request.getParameter("videoId"));
        int data = liveVideoService.deleteVideo(videoId, anchorId);
        ResponseResult<Integer> result = new ResponseResult<Integer>(ResultMsg.SUCCESS);
        result.setData(data);
        return result;
    }

    /**
     * 停止视频
     * 
     * @param request
     */
    @RequestMapping(value = "stop", method = RequestMethod.POST)
    @ResponseBody
    public ResponseResult<String> stopVideo(HttpServletRequest request) {
        long anchorId = UserThreadLocalContext.getCurrentUser().getUserId();
        long videoId = Long.valueOf(request.getParameter("videoId"));
        int vdoid = Integer.valueOf(request.getParameter("vdoid"));
        String streamName = request.getParameter("streamName");
        int data = liveVideoService.stopVideo(videoId, anchorId, streamName, vdoid);
        ResponseResult<String> result = new ResponseResult<String>(ResultMsg.SUCCESS);
        if (data > 0) {
            String returnURL = RequestUtils.getStringParamDef(request, "returnUrl", WebConstants.Login.INDEX_URL);
            result.setData(returnURL);
        }
        try (BufferedReader br = request.getReader()) {
            String str, wholeStr = "";
            while ((str = br.readLine()) != null) {
                wholeStr += str;
            }
            redisAdapter.listLpush(Constants.TRACE_LIVE_STREAM_STATUS_KEY, (DateUtil.formatDate(new Date(), "yyyy-MM-dd hh:mm:ss") + "--关闭直播(PC端)---"+ wholeStr+"---" + JSON.toJSONString(result)));
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }


    /**
     * 开始视频
     * 
     * @param request
     */
    @RequestMapping(value = "start", method = RequestMethod.POST)
    @ResponseBody
    public ResponseResult<Integer> startVideo(HttpServletRequest request) {
        long anchorId = PcConstants.getAnchorIdByCookie(request);
        long videoId = Long.valueOf(request.getParameter("videoId"));
        String streamName = request.getParameter("streamName");
        int data = liveVideoService.startVideo(videoId, anchorId, streamName);
        ResponseResult<Integer> result = new ResponseResult<Integer>(ResultMsg.SUCCESS);
        result.setData(data);
        try {

            try (BufferedReader br = request.getReader()) {
                String str, wholeStr = "";
                while ((str = br.readLine()) != null) {
                    wholeStr += str;
                }
                redisAdapter.listLpush(Constants.TRACE_LIVE_STREAM_STATUS_KEY, (DateUtil.formatDate(new Date(), "yyyy-MM-dd hh:mm:ss") + "--开始直播(PC端)--"+ wholeStr+"----" + JSON.toJSONString(result)));
            } catch (Exception e) {
                e.printStackTrace();
            }

        }catch(Exception ex){
            ex.printStackTrace();
        }
        return result;
    }

    @RequestMapping(value = "detail", method = RequestMethod.POST)
    public String getVideoInfo(HttpServletRequest request, Model model) {
        long anchorId = UserThreadLocalContext.getCurrentUser().getUserId();
        long videoId = Long.valueOf(request.getParameter("videoId"));
        Map<String, Object> data = liveVideoService.videoInfo(videoId, anchorId);
        data.put("anchorId", anchorId);
        model.addAttribute("videoInfo", data);
        return "/live/live_page";
    }

    /**
     * 异步请求获取正在直播视频的观看人数, 每隔10s访问一次
     * 
     * @param request
     * @return
     */
    @RequestMapping(value = "getRealtimeViewers", method = RequestMethod.POST)
    @ResponseBody
    public Map<String, Object> asynGetViewers(HttpServletRequest request) {
        long videoId = Long.valueOf(request.getParameter("videoId"));
        if (request.getParameter("videoStatus") == null) {
        	return null;
        }
        int videoStatus = Integer.valueOf(request.getParameter("videoStatus"));
        return liveVideoService.getViewers(videoId, videoStatus);
    }

    /**
     * 异步请求获取正在直播视频的收入, 每隔3分钟访问一次
     * 
     * @param request
     * @return
     */
    @RequestMapping(value = "getRealtimeIncome", method = RequestMethod.POST)
    @ResponseBody
    public Map<String, Object> asynGetIncome(HttpServletRequest request) {
        long videoId = Long.valueOf(request.getParameter("videoId"));
        return liveVideoService.getIncome(videoId);
    }

    @RequestMapping(value = "livingcnt", method = RequestMethod.POST)
    @ResponseBody
    public ResponseResult<Integer> getLivingCnt(HttpServletRequest request) {
        long anchorId = UserThreadLocalContext.getCurrentUser().getUserId();
        int data = liveVideoService.getLivingCnt(anchorId);
        ResponseResult<Integer> result = new ResponseResult<Integer>(ResultMsg.SUCCESS);
        result.setData(data);
        return result;
    }

}
