package com.chineseall.iwanvi.wwlive.web.otherapp.controller;

import com.chineseall.iwanvi.wwlive.domain.wwlive.Anchor;
import com.chineseall.iwanvi.wwlive.web.common.util.DateTools;
import com.chineseall.iwanvi.wwlive.web.launch.service.AnchorService;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Niu Qianghong on 2017-09-29 0029.
 */
@Controller
public class DZAnchorController {

    @Autowired
    private AnchorService anchorService;

    private Logger logger = Logger.getLogger(this.getClass());

    /**
     * 定制版修改主播资料
     * @param request
     * @return
     */
    @RequestMapping("/external/dz/anchor/modify")
    @ResponseBody
    public Map<String, Object> modifyInfo(HttpServletRequest request){
        Map<String, Object> resultMap = new HashMap<>();
        try {
            Long anchorId = Long.valueOf(request.getParameter("anchorId"));
            String headImg = request.getParameter("avatar");
            String birth = request.getParameter("birthday");
            Long birthday = (birth == null ? null : Long.valueOf(birth));
            String sexStr = request.getParameter("sex");
            Integer sex = (sexStr == null ? null : Integer.valueOf(sexStr));
            String notice = request.getParameter("profile");
            String userName = request.getParameter("nickname");
            Anchor anchor = new Anchor();
            anchor.setHeadImg(headImg);
            anchor.setUserName(userName);
            if(birthday != null) {
                Date birthDate = new Date(birthday);
                anchor.setBirthday(birthDate);
                anchor.setZodiac(DateTools.getZodiacByDate(birthDate));
            }
            anchor.setNotice(notice);
            anchor.setSex(sex);
            anchor.setAnchorId(anchorId);
            Integer result = anchorService.modifyAnchorInfo(anchor);
            resultMap.put("result", result);
        } catch (Exception e) {
            logger.error("定制版修改主播信息异常", e);
            resultMap.put("result", 0);
        }
        return resultMap;
    }
}
