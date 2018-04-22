package com.chineseall.iwanvi.wwlive.web.nobility.controller;

import com.alibaba.fastjson.JSONObject;
import com.chineseall.iwanvi.wwlive.common.check.ResponseResult;
import com.chineseall.iwanvi.wwlive.common.check.ResultMsg;
import com.chineseall.iwanvi.wwlive.common.tools.HttpRequestUtils;
import com.chineseall.iwanvi.wwlive.web.nobility.service.FreeSecretService;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@Controller
public class FreeSecretController {

    static final Logger logger = Logger.getLogger(FreeSecretController.class);

    /**
     * 免密支付协议申请
     */
    @Value("${wxpay.freesecret.url}")
    private String freesecretUrl;
    @Value("${wxpay.planid}")
    private String planid;
    @Value("${wxpay.usercode}")
    private String usercode;
    @Value("${wxpay.freesecret.notifyurl}")
    private String freesecretNotifyUrl;
    @Value("${wxpay.freesecret.payurl}")
    private String freesecretPayUrl;

    @Autowired
    private FreeSecretService freeSecretService;

    /**
     * 获取免密协议
     *
     * @return
     */
    @RequestMapping(value = "/external/freesecret/getFreesecret", method = RequestMethod.POST)
    @ResponseBody
    public ResponseResult getFreesecret(Long userId) {
        ResponseResult rr = new ResponseResult();
        Map<String, Object> params = new HashMap<String, Object>();

        params.put("planid", planid);
        params.put("userid", userId.toString());
        params.put("notifyurl", freesecretNotifyUrl);
        params.put("usercode", usercode);

        String result = HttpRequestUtils.httpGet(freesecretUrl, params);

        JSONObject resultJson = JSONObject.parseObject(result);

        if(resultJson!=null){
            int code  = resultJson.getInteger("errcode");
            String msg  = resultJson.getString("errmsg");
            String contractid  = resultJson.getString("contractid");
            String entrusturl  = resultJson.getString("entrusturl");
            //证明已签约
            if(StringUtils.isNotBlank(contractid)){
                rr.setResponseByResultMsg(ResultMsg.FREESECRET_ISEXIST);
                rr.setData(contractid);
            }
            //证明没有签约，返回签约的url
            if(StringUtils.isNotBlank(entrusturl)){
                rr.setResponseByResultMsg(ResultMsg.FREESECRET_ISNOTEXIST);
                rr.setData(entrusturl);
            }
        }
        return rr;

    }

    /**
     * 回调接口，获取相对应的续费协议号进行修改数据库表
     *
     * @param request
     * @return
     */
    @RequestMapping(value = "/external/freesecret/notify", method = RequestMethod.GET)
    public void notify(HttpServletRequest request) {
        freeSecretService.updateRenewOrder(request);
    }

    /**
     * 支付宝免密支付
     * usercode(微信扣费商户ID)
     * orderid(订单号)
     * title(扣费产品标题)
     * amount(扣费金额)
     * contractid(免密支付协议编码)
     * @return
     */
    @RequestMapping(value = "/external/freesecret/freesecretpay", method = RequestMethod.POST)
    @ResponseBody
    public ResponseResult freesecretpay(HttpServletRequest request) {
        ResponseResult rr = new ResponseResult();
        String orderid  = request.getParameter("orderId");
        String title  = request.getParameter("title");
        String amount  = request.getParameter("amount");
        String contractid  = request.getParameter("contractid");

        if (StringUtils.isBlank(orderid)||StringUtils.isBlank(title)||
                StringUtils.isBlank(amount)||StringUtils.isBlank(contractid)) {
            rr.setResponseByResultMsg(ResultMsg.LOST_PARAMS);
            return rr;
        }

        Map<String,Object> param = new HashMap<String,Object>();
        param.put("orderid",orderid);
        param.put("title",title);
        param.put("amount",amount);
        param.put("contractid",contractid);
        param.put("usercode",usercode);

        String result = HttpRequestUtils.httpGet(freesecretPayUrl, param);
        if (StringUtils.isNotBlank(result)) {
            JSONObject resultJson = JSONObject.parseObject(result);
            if (resultJson.getInteger("errcode") == 0) {
                rr.setResponseByResultMsg(ResultMsg.SUCCESS);
            } else {
                rr.setCode(resultJson.getInteger("errcode"));
                rr.setInfo(resultJson.getString("errmsg"));
            }
        }else{
            rr.setResponseByResultMsg(ResultMsg.FAIL);
        }
        return rr;
    }
}