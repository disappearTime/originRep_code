package com.chineseall.iwanvi.wwlive.web.my.controller;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections.map.HashedMap;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.chineseall.iwanvi.wwlive.common.tools.ImgFileUpLoadUtils;
import com.chineseall.iwanvi.wwlive.common.tools.img.AnchorBecomeImg;
import com.chineseall.iwanvi.wwlive.common.tools.img.AnchorHeadImg;
import com.chineseall.iwanvi.wwlive.domain.wwlive.BecomeAnchor;
import com.chineseall.iwanvi.wwlive.web.common.helper.PageUrlHelper;
import com.chineseall.iwanvi.wwlive.web.common.util.ControllerRequestUtils;
import com.chineseall.iwanvi.wwlive.web.common.util.RequestParamsUtils;
import com.chineseall.iwanvi.wwlive.web.my.service.BecomeAnchorService;

/**
 * Created by 云瑞 on 2017/6/22.
 */
@Controller
@RequestMapping("/external")
public class BecomeAnchorController {

    private Logger logger = Logger.getLogger(this.getClass());

    @Value("${img.path}")
    private String imgPath;

    @Value("${img.url}")
    private String imgUrl;

    @Autowired
    private BecomeAnchorService becomeAnchorService;

    /**
     * 跳转成为主播注页
     * @param model
     * @param request
     * @return
     */
    @RequestMapping(value = "/app/become/anchor",method = RequestMethod.GET)
    public String toBecomAnchor (Model model,HttpServletRequest request) {
        Long userId = ControllerRequestUtils.parseLongFromRquest(request, "userId");
        Map<String, String> params = ControllerRequestUtils.getCommonParam(request);
        model.addAttribute("consPage", PageUrlHelper.buildVideoContributionListUrl(params));
        Map<String, String> requestParams = RequestParamsUtils.defaultRequetParams(userId);
        params.putAll(requestParams);
        model.addAttribute("params", params);
        return "toBeAnchor/toBeAnchor";
    }

    /**
     * 创建主播信息
     * @param request
     */
    @ResponseBody
    @RequestMapping(value = "/app/become/createanchor", method = {RequestMethod.POST,RequestMethod.GET})
    public Map<String,Object>  becomeAnchor (@RequestParam("fileName") MultipartFile[] fileName, /*@RequestParam("uploadImg") CommonsMultipartFile files[],*/ HttpServletRequest request, HttpServletResponse response) {
        Map<String,Object> result = new HashedMap();
        String userId = request.getParameter("userId");
        if(userId == null) {
            result.put("data",false);
            result.put("info","用户未登录");
            return result;
        }
        String path = "";
        if (fileName != null) try {
            for (int i = 0; i < fileName.length; i++) {
//                MultipartHttpServletRequest multiRequest = (MultipartHttpServletRequest) request;
//                MultipartFile files = multiRequest.getFile("uploadImg");
                MultipartFile file = fileName[i];
                path += ImgFileUpLoadUtils.uploadFileToLocal(file, imgPath, imgUrl, new AnchorBecomeImg());
                path += ",";
            }
        } catch (Exception e) {
            result.put("data", false);
            result.put("info", "图片上传失败");
            logger.error("图片上传失败", e);
        }
        BecomeAnchor become = new BecomeAnchor();
        become.setUserId(Long.valueOf(userId));
        become.setRealname(request.getParameter("nameval"));
        become.setSex(Integer.valueOf(request.getParameter("sexVal")));
        become.setSpecialty(request.getParameter("skillVal"));
        become.setExperience(request.getParameter("experVal"));
        become.setContactmode(request.getParameter("telVal"));
        become.setOthercontent(request.getParameter("otherSayVal"));
        
        try{
            becomeAnchorService.insertBecomeAnchor(become,path);
            result.put("data",true);
            result.put("info","申请成功");
        }catch (Exception e) {
            logger.error("成为主播失败",e);
            result.put("data",false);
            result.put("info","申请主播失败");
        }
        return result;
    }

    @ResponseBody
    @RequestMapping(value = "/app/become/uploadImg", method = {RequestMethod.GET,RequestMethod.POST})
    public Map<String,Object> uploadImg(@RequestParam("fileName") MultipartFile fileName,HttpServletRequest request) {
        Map<String,Object> result = new HashedMap();
        String path = "";
        if (fileName != null) {
            try {
                MultipartHttpServletRequest multiRequest = (MultipartHttpServletRequest) request;
                MultipartFile file = multiRequest.getFile("fileName");
                path = ImgFileUpLoadUtils.uploadFileToLocal(file, imgPath, imgUrl, new AnchorHeadImg());
                result.put("data",true);
                result.put("info",path);
            }catch (Exception e) {
                result.put("data",false);
                result.put("info","图片上传失败");
                logger.error("图片上传失败", e);
            }
        }
        return result;
    }

    /**
     * 跳转宝贝宣传页
     * @return
     */
    @RequestMapping("/toBabyBooks")
    public String tobBabyBooks () {
        return "babyreading/shuchengdl";
    }
}