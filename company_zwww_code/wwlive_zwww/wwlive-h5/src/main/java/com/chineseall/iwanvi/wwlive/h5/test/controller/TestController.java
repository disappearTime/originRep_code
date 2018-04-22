package com.chineseall.iwanvi.wwlive.h5.test.controller;

//import com.chineseall.iwanvi.template.api.ITestAPI;
//import com.chineseall.iwanvi.template.api.vo.UserVO;
//import com.chineseall.iwanvi.wwlive.web.test.service.TestService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;


@Controller
@RequestMapping("index")
public class TestController {

    private Log logger = LogFactory.getLog(getClass());

//    @Autowired
//    private TestService testService;


    /**
     * 注入dubbo服务
     * @return
     */
//    @Autowired(required = false)
//    private ITestAPI iTestAPI;

    @RequestMapping("")
    public ModelAndView index() {
        logger.debug("method : index() ");
        ModelAndView mav = new ModelAndView("index");

//        User user  = testService.findUser(1l);
//        UserVO user = iTestAPI.m("hello");
//        mav.addObject("user", user );

        return mav;
    }


}
