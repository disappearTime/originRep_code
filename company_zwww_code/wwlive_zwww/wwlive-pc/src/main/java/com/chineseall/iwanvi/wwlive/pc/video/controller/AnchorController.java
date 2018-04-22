package com.chineseall.iwanvi.wwlive.pc.video.controller;

import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import com.chineseall.iwanvi.wwlive.common.tools.img.VideoCoverImg;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateUtils;
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

import com.chineseall.iwanvi.wwlive.common.tools.ImgFileUpLoadUtils;
import com.chineseall.iwanvi.wwlive.common.tools.img.AnchorHeadImg;
import com.chineseall.iwanvi.wwlive.domain.wwlive.Anchor;
import com.chineseall.iwanvi.wwlive.pc.common.Page;
import com.chineseall.iwanvi.wwlive.pc.common.RequestUtils;
import com.chineseall.iwanvi.wwlive.pc.common.WebConstants;
import com.chineseall.iwanvi.wwlive.pc.common.loginContext.UserThreadLocalContext;
import com.chineseall.iwanvi.wwlive.pc.video.common.PcConstants;
import com.chineseall.iwanvi.wwlive.pc.video.service.AnchorService;
import com.chineseall.iwanvi.wwlive.pc.video.util.DateUtil;

@Controller
@RequestMapping("/anchor")
public class AnchorController {

    @Autowired
    private AnchorService anchorService;

    private Logger logger = Logger.getLogger(this.getClass());

    @Value("${img.path}")
    private String imgPath;

    @Value("${img.url}")
    private String imgUrl;

    @Value("${default.anchor.img}")
    private String defaultAnchorImg;

    @Value("${anchor.img}")
    private String anchorImg;
    
    /**
     * pc端主播修改个人资料
     * 
     * @return
     * @throws ParseException
     */
    @RequestMapping(value = "/modifyinfo", method = RequestMethod.POST)
    @ResponseBody
    public Map<String, Object> modifyInfo(MultipartFile headImg, HttpServletRequest request) throws ParseException {

        String newPasswd = request.getParameter("newPasswd");
        
        String path = "";// 生成上传文件路径
        if (headImg != null) {
            //主播默认头像图片名必须为default_head
            MultipartHttpServletRequest multiRequest = (MultipartHttpServletRequest) request;
            MultipartFile file = multiRequest.getFile("headImg");
            try {
            	path = ImgFileUpLoadUtils.uploadFileToLocal(file, imgPath, imgUrl, new AnchorHeadImg());
            } catch (Exception e) {
                logger.error("上传图片失败：", e); 
                Map<String, Object> result = new HashMap<>();
                result.put("result", PcConstants.FAIL);
                return result;
            }
        }
        
        Long anchorId = UserThreadLocalContext.getCurrentUser().getUserId();
        if (anchorId == null) {
            Map<String, Object> result = new HashMap<>();
            result.put("result", PcConstants.FAIL);
            return result;
        }
        Anchor upateAnchor = new Anchor();
        upateAnchor.setAnchorId(anchorId);
        upateAnchor.setSex(Integer.valueOf(request.getParameter("sex")));
        String birthdayStr = request.getParameter("birthYear") + "-" + request.getParameter("birthMonth") + "-"
                + request.getParameter("birthDay");
        Date birthday = DateUtils.parseDate(birthdayStr, new String[] { "yyyy-MM-dd" });
        upateAnchor.setBirthday(birthday);
        upateAnchor.setZodiac(DateUtil.getZodiacByDate(birthday));
        upateAnchor.setUserName(request.getParameter("userName"));
        upateAnchor.setNotice(request.getParameter("notice"));
        if (StringUtils.isNotEmpty(path)) {
        	upateAnchor.setHeadImg(path);
        }
        final String loginCookie = RequestUtils.getCookieValue(request, WebConstants.Login.LOGIN_COOKIE_KEY);

        return anchorService.modifyInfo(upateAnchor, loginCookie, newPasswd);
    }

    /**
     * PC端主播查看详细资料
     * 
     * @param request
     * @return
     * @throws ParseException
     */
    @RequestMapping(value = "/detail", method = { RequestMethod.POST, RequestMethod.GET })
    public String detailInfo(HttpServletRequest request, Model model) throws ParseException {
        long anchorId = UserThreadLocalContext.getCurrentUser().getUserId();

        model.addAttribute("anchor", anchorService.getAnchorInfo(anchorId));

        return "/anchor/detail";
    }

    /**
     * PC端主播查看贡献榜列表
     *
     * @return
     */
    @RequestMapping(value = "/contriblist")
    public String getContribList(Page page, Model model) {
        long anchorId = UserThreadLocalContext.getCurrentUser().getUserId();

        Page pageResult = anchorService.getContribList(page, anchorId);

        page.setUrl("/anchor/contriblist?pageIndex=%pageIndex%");

        model.addAttribute("page", pageResult);

        return "anchor/contribution_list";
    }

    /**
     * 跳转到修改资料页面, 在页面中展示主播的资料
     * 
     * @param request
     * @param model
     * @return
     * @throws ParseException
     */
    @RequestMapping(value = "/tomodify")
    public String toModifyInfo(HttpServletRequest request, Model model) throws ParseException {
        // 获取主播资料
        long anchorId = UserThreadLocalContext.getCurrentUser().getUserId();
        Map<String, Object> anchorInfo = anchorService.getAnchorInfo(anchorId);
        model.addAttribute("anchor", anchorInfo);
        return "/anchor/modify_info";
    }

    /**
     * PC端主播查看收入
     *
     * @param page
     * @param model
     * @return
     */
    @RequestMapping(value = "/income")
    public String viewIncome(Page page, Model model) {
        try {
            long anchorId = UserThreadLocalContext.getCurrentUser().getUserId();

            //每月收入详细
            Page pageResult = anchorService.getMonthDetail(page, anchorId);
            page.setUrl("/anchor/income?pageIndex=%pageIndex%");
            //礼品总数统计信息
            Map<String, Object> statistics = anchorService.getAllGoodsAndIncome(anchorId);
            if (statistics != null) {
                model.addAllAttributes(statistics);
            }

            model.addAttribute("page", pageResult);
        } catch (Exception e) {
        	e.printStackTrace();
        	logger.error("获取主播收入异常：", e);
        }
        return "/anchor/my_income";
    }

    /**
     * 返回用户的视频数、收入和未读消息数
     * @param request
     * @return
     */
    @RequestMapping(value = "headerInfo", method = RequestMethod.POST)
    @ResponseBody
    public Map<String, Object> getHeaderInfo(HttpServletRequest request){
        long anchorId = UserThreadLocalContext.getCurrentUser().getUserId();
        return anchorService.getVideoCntAndIncome(anchorId);
    }
    
    @RequestMapping(value = "/adminList")
    public String toAdminList(){
        return "anchor/admin_list";
    }
    
    @RequestMapping(value = "/adminTable")
    public String setAdminTable(Page page, Model model){
        long anchorId = UserThreadLocalContext.getCurrentUser().getUserId();
        Page resultPage = anchorService.getAdmins(anchorId, page);
        model.addAttribute("page", resultPage);
        return "anchor/admin_table";
    }

    @RequestMapping(value = "/cardface/upload", method = RequestMethod.POST)
    @ResponseBody
    public Map<String, Object> uploadCardFace(HttpServletRequest request){
        Map<String, Object> data = new HashMap<>();
        try {
            long anchorId = UserThreadLocalContext.getCurrentUser().getUserId();
            Long videoId = Long.valueOf(request.getParameter("videoId"));
            logger.info("debug1108: videoId =" + videoId);
            String cardFaceUrl = "";// 生成上传文件路径
            //主播默认头像图片名必须为default_head
            MultipartHttpServletRequest multiRequest = (MultipartHttpServletRequest) request;
            MultipartFile file = multiRequest.getFile("imgFile");
            cardFaceUrl = ImgFileUpLoadUtils.uploadFileToLocal(file, imgPath, imgUrl, new VideoCoverImg());//上传图片
            if (StringUtils.isBlank(cardFaceUrl)) {
                data.put("result", 0);
                return data;
            }
            data.put("result", anchorService.setCardFace(anchorId, videoId, cardFaceUrl));
            return data;
        } catch (Exception e) {
            logger.error("传牌面异常", e);
            data.put("result", 0);
            return data;
        }
    }
}
