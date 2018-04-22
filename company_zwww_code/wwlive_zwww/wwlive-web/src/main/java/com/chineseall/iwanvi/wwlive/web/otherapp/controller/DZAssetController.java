package com.chineseall.iwanvi.wwlive.web.otherapp.controller;

import com.chineseall.iwanvi.wwlive.web.otherapp.service.DZAssetService;
import com.mongodb.util.JSON;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Niu Qianghong on 2017-08-28 0028.
 */
@Controller
public class DZAssetController {

    @Autowired
    private DZAssetService dzAssetService;

    private Logger logger = Logger.getLogger(this.getClass());

    @RequestMapping("/external/dz/trans/sync")
    @ResponseBody
    public Map<String, Object> syncDiamond(HttpServletRequest request){
        Long userId = Long.valueOf(request.getParameter("userId"));
        Long diamonds = Long.valueOf(request.getParameter("diamonds"));
        String transInfo = request.getParameter("transInfo");
        Map trans = (Map)JSON.parse(transInfo);
        logger.info("定制版同步交易信息: userId = " + userId + ", diamonds = " + diamonds + ", transInfo = " + trans);
        int result = dzAssetService.syncDiamond(userId, diamonds, trans);
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("result", result);
        return resultMap;
    }

}
