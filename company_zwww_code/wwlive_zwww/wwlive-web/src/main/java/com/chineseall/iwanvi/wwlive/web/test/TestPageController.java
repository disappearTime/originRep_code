package com.chineseall.iwanvi.wwlive.web.test;

import com.chineseall.iwanvi.wwlive.common.cache.RedisClientAdapter;
import com.chineseall.iwanvi.wwlive.common.cache.RedisKey;
import com.chineseall.iwanvi.wwlive.web.common.util.ControllerRequestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;

/**
 * 测试页面效果使用
 * @author Niu Qianghong
 *
 */
@Controller
@RequestMapping("/test")
public class TestPageController {

    @Autowired
    private RedisClientAdapter redisAdapter;

    @RequestMapping("/fans")// 此处填写页面名, 作为访问路径使用
    public String testPage1(){
        return "launch/fans_list";// 此处填写页面所在路径, 例如 "my/test_page", 不需要写".vm"
    }
    @RequestMapping("/video_index")// 此处填写页面名, 作为访问路径使用
    public String testPage2(){
        return "tab/video_index";// 此处填写页面所在路径, 例如 "my/test_page", 不需要写".vm"
    }
    @RequestMapping("/video_rank")// 此处填写页面名, 作为访问路径使用
    public String testPage3(){
        return "launch/video_rank_info";// 此处填写页面所在路径, 例如 "my/test_page", 不需要写".vm"
    }
    @RequestMapping("/anchorSelf")// 此处填写页面名, 作为访问路径使用
    public String testPage4(){
        return "video/anchor_home";// 此处填写页面所在路径, 例如 "my/test_page", 不需要写".vm"
    }
    @RequestMapping("/goddessAct")
    public String testPage5(){
        return "activity/goddess_activity";// 此处填写页面所在路径, 例如 "my/test_page", 不需要写".vm"
    }
    public ModelAndView tabVideoHome(HttpServletRequest request, ModelAndView model) {
        model.setViewName("tab/video_indexTry");
        String snapShotKey = RedisKey.VideoKeys.LIVING_VIDEOS_SNAPSHOT;
        if (redisAdapter.existsKey(snapShotKey)) {
            String videoKey = redisAdapter.strGet(snapShotKey);
            Long cnt = redisAdapter.zsetCard(videoKey);
            model.addObject("params", ControllerRequestUtils.getParam(request));
            model.getModel().put("livingVideoCnt", cnt);
        } else {
            model.getModel().put("livingVideoCnt", "0");
        }
        return model;
    }
}
