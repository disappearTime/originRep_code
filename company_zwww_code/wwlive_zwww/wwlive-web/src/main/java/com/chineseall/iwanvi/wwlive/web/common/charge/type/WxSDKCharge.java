package com.chineseall.iwanvi.wwlive.web.common.charge.type;

import java.io.InputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.chineseall.iwanvi.wwlive.common.cache.RedisClientAdapter;
import com.chineseall.iwanvi.wwlive.dao.wwlive.RechargeInfoMapper;
import com.chineseall.iwanvi.wwlive.domain.wwlive.RechargeInfo;
import com.chineseall.iwanvi.wwlive.web.common.charge.DefaultUserCharge;
import com.chineseall.iwanvi.wwlive.web.common.charge.domain.WCPrePayInfo;
import com.chineseall.iwanvi.wwlive.web.common.security.PaymentSecurity;
import com.chineseall.iwanvi.wwlive.web.common.util.WCPayUtils;

@Component("wxSDKCharge")
public class WxSDKCharge extends DefaultUserCharge{
    
    private Logger log = Logger.getLogger(this.getClass());
    
    @Value("${wechat.pay.api.unifiedorder}")
    private String unifiedOrderUrl;// 统一下单API访问地址
    
    @Value("${wechat.pay.appid}")
    private String appId;
    
    @Value("${wechat.pay.mchid}")
    private String mchId;
    
    @Value("${wechat.pay.notifyurl}")
    private String notifyUrl;
    
    @Value("${wechat.pay.apikey}")
    private String apiKey;
    
    private static final String GOODS_DESC = "爱阅直播-用户充值";
    
    private static final String TRADE_TYPE = "APP";
    
    @Autowired
    private RedisClientAdapter redisAdapter;
    
    @Autowired
    private RechargeInfoMapper chargeMapper;

    @Override
    public Map<String, Object> resultMap(RechargeInfo info) {
        
        Map<String, Object> params = new HashMap<>();
        params.put("appid", appId);//应用号
        params.put("body", GOODS_DESC);// 商品描述
        params.put("mch_id", mchId);// 商户号
        params.put("nonce_str", WCPayUtils.getRandomNumber(16));// 16随机字符串(大小写字母加数字)
        
        String outTradeNo = info.getOutTradeNo();
        
        params.put("out_trade_no", outTradeNo);// 商户订单号
        params.put("total_fee", info.getAmt());// 银行币种支付的钱钱啦
        params.put("spbill_create_ip", info.getIp());// IP地址
        params.put("notify_url", notifyUrl); // 微信回调地址
        params.put("trade_type", TRADE_TYPE);// 支付类型 APP
        params.put("key", apiKey);                
        try {
            String paramXml = WCPayUtils.getXmlFromParamsMap(params);
            WCPrePayInfo prepayInfo = new WCPrePayInfo();
            setPrepayInfo(paramXml, prepayInfo);
            if(prepayInfo != null && StringUtils.isNotBlank(prepayInfo.getPrepayid())){
                Map<String, Object> resultMap = new HashMap<>();
                resultMap.put("prepayid", prepayInfo.getPrepayid());
                resultMap.put("appid", appId);
                resultMap.put("partnerid", mchId);
                resultMap.put("noncestr", WCPayUtils.getRandomNumber(16));
                resultMap.put("timestamp", new Date().getTime() / 1000);// 微信时间戳以秒为单位
                resultMap.put("package", "Sign=WXPay");
                resultMap.put("key", apiKey);
                resultMap.put("sign", WCPayUtils.getSignFromParamMap(resultMap));
                resultMap.remove("key");// 把API密钥信息去掉
                resultMap.put("outtradeno", outTradeNo);
                String tradeNo = PaymentSecurity.makeSecurity(outTradeNo, redisAdapter);
                PaymentSecurity.securityCache(resultMap, tradeNo, redisAdapter);//微信加密机制
                
                int cnt = chargeMapper.insertRechargeInfo(info);
                log.info("微信充值--生成后台订单, 结果: " + cnt);
                resultMap.put("result", cnt);
                log.info("微信充值--预支付订单, 信息" + resultMap);
                return resultMap;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }  
        return null;
    }
    
    /**
     * 调起微信统一下单接口
     * @param xmlStr
     * @param respInfo
     */
    private void setPrepayInfo(String xmlStr, WCPrePayInfo respInfo) {
        try {
            HttpClientBuilder httpClientBuilder = HttpClientBuilder.create();
            CloseableHttpClient closeableHttpClient = httpClientBuilder.build();
            HttpPost httpPost = new HttpPost(unifiedOrderUrl);
            HttpClientContext context = HttpClientContext.create();
            StringEntity se = new StringEntity(new String(xmlStr.getBytes("utf-8") ,"iso-8859-1"));
            se.setContentType("text/xml");
            se.setContentEncoding(new BasicHeader(HTTP.CONTENT_TYPE, "application/xml"));
            httpPost.setEntity(se);
            httpPost.setConfig(RequestConfig.DEFAULT);
            HttpResponse httpResponse = closeableHttpClient.execute(httpPost, context);
            HttpEntity httpEntity = httpResponse.getEntity();
            if (httpEntity != null) {
                // 打印响应内容
                InputStream content = httpEntity.getContent();
                Map<String, String> params = WCPayUtils.getParamsMapFromXml(content);
                log.info("微信充值--预支付订单接口返回信息: " + params);
                params.put("key", apiKey);
                if(params.containsKey("sign") && params.get("prepay_id") != null &&
                        !"".equals(params.get("prepay_id")) && !"null".equals(params.get("prepay_id"))){
                    if(WCPayUtils.checkSign(params)){//签名认证成功
                        for (Map.Entry<String, String> param : params.entrySet()) {
                            if("appid".equals(param.getKey()))
                                respInfo.setAppid(param.getValue()); 
                            else if("mch_id".equals(param.getKey()))
                                respInfo.setPartnerid(param.getValue());
                            else if("prepay_id".equals(param.getKey()))
                                respInfo.setPrepayid(param.getValue()); //将prepayid放进WCPayGetPrePayIdRespInfo对象中
                            else
                                continue;
                        }
                    }
                }else{
                    log.info("第一次响应签名认证失败");
                }
            }
            // 释放资源
            closeableHttpClient.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
