package com.chineseall.iwanvi.wwlive.web.otherapp.service.impl;

import com.alibaba.fastjson.JSON;
import com.chineseall.iwanvi.wwlive.common.cache.RedisClientAdapter;
import com.chineseall.iwanvi.wwlive.common.cache.RedisExpireTime;
import com.chineseall.iwanvi.wwlive.common.cache.RedisKey;
import com.chineseall.iwanvi.wwlive.common.constants.Constants;
import com.chineseall.iwanvi.wwlive.common.tools.HttpUtils;
import com.chineseall.iwanvi.wwlive.domain.wwlive.RechargeInfo;
import com.chineseall.iwanvi.wwlive.web.otherapp.service.DZDataSyncService;

import org.apache.commons.collections.MapUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.util.Map;

/**
 * Created by Niu Qianghong on 2017-08-31 0031.
 */
@Service
public class DZDataSyncServiceImpl implements DZDataSyncService {

    // TODO NIU: 线上url修改
    @Value("${dz.syncdata.url}")
    private String syncDataUrl;

    @Autowired
    private ThreadPoolTaskExecutor taskExecutor;

    @Autowired
    private RedisClientAdapter redisAdapter;

    private final Logger logger = Logger.getLogger(DZDataSyncServiceImpl.class);

    private final int SUCCESS = 1;

    private final int FAIL = 1;

    @Override
    public void syncRechargeData(final RechargeInfo rechargeInfo) {

        if (rechargeInfo == null || Constants._0 == 
				(rechargeInfo.getRechargeStatus() == null ? 0 : rechargeInfo.getRechargeStatus())) {
            return;
        }

        // 由于微信充值回调两次, 在此添加判重的处理
        String rechargeKey = RedisKey.SYNC_RECHARGE_ + rechargeInfo.getRechargeId();
        if (redisAdapter.existsKey(rechargeKey)) {
            return;
        } else {
            redisAdapter.strSetByNormal(rechargeKey, "0");
            redisAdapter.expireKey(rechargeKey, RedisExpireTime.EXPIRE_MIN_30);
        }
        taskExecutor.execute(new Runnable() {
            @Override
            public void run() {
                logger.info("开始和定制版同步数据, rechargeInfo = " + rechargeInfo);
                // 待同步的订单信息写到redis中
                String pendingKey = RedisKey.PENDING_RECHARGE_ + rechargeInfo.getRechargeId();
                redisAdapter.strSetByNormal(pendingKey, JSON.toJSONString(rechargeInfo));

                try {
                    HttpURLConnection conn = HttpUtils.createPostHttpConnection(syncDataUrl);

                    StringBuilder params = new StringBuilder();
                    params.append("type=0"); // 和定制版约定type=0为充值
                    params.append("&amount=" + rechargeInfo.getRechargeAmount()); // 到账的钻石数
                    params.append("&zbUserId=" + rechargeInfo.getUserId());
                    params.append("&goodsId=0"); // 定制版钻石id=0
                    params.append("&goodsName=钻石");
                    params.append("&goodsCnt=" + rechargeInfo.getRechargeAmount());
                    params.append("&anchorId=" + rechargeInfo.getReceiveNo());
                    params.append("&outTradNo=zbcz" + rechargeInfo.getRechargeId());// 外部订单号为zbcz(直播充值) + 充值记录id

                    OutputStream out = conn.getOutputStream();
                    out.write(params.toString().getBytes());
                    out.close();

                    int responseCode = conn.getResponseCode();
                    if (200 == responseCode) {
                        BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), "utf-8"));
                        String response = reader.readLine();
                        Map resultMap = (Map) JSON.parse(response);
                        int result = MapUtils.getIntValue(resultMap, "result", 0);
                        if (result == SUCCESS) {
                            // 将待同步的充值信息缓存删除
                            redisAdapter.delKeys(pendingKey);
                            logger.info("和定制版同步数据完成, rechargeInfo = " + rechargeInfo);
                        } else{
                            logger.warn("和定制版同步数据失败, 请求返回 = " + response +", rechargeInfo = " + rechargeInfo);
                        }
                    } else {
                        logger.warn("和定制版同步数据失败, code = " + responseCode +", rechargeInfo = " + rechargeInfo);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
