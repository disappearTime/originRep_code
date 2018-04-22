package com.chineseall.iwanvi.wwlive.web.game.service.impl;

import com.chineseall.iwanvi.wwlive.common.cache.RedisClientAdapter;
import com.chineseall.iwanvi.wwlive.common.cache.RedisKey;
import com.chineseall.iwanvi.wwlive.dao.wwlive.AnchorMapper;
import com.chineseall.iwanvi.wwlive.dao.wwlive.LiveVideoInfoMapper;
import com.chineseall.iwanvi.wwlive.domain.wwlive.LiveVideoInfo;
import com.chineseall.iwanvi.wwlive.web.game.service.AnchorService;
import com.chineseall.iwanvi.wwlive.web.launch.service.FileUploadService;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Niu Qianghong on 2017-10-19 0019.
 */
@Service("cardAnchorService")
public class AnchorServiceImpl implements AnchorService {

    @Autowired
    private FileUploadService fileUploadService;

    @Autowired
    private AnchorMapper anchorMapper;

    @Autowired
    private LiveVideoInfoMapper liveVideoInfoMapper;

    @Autowired
    private RedisClientAdapter redisAdapter;

    private Logger logger = Logger.getLogger(this.getClass());

    @Override
    public Map<String, Object> uploadCardFace(Long anchorId, Long videoId, HttpServletRequest request) {
        Map<String, Object> data = new HashMap<>();
        try {
            Map<String, Object> uploadResult = fileUploadService.uploadAndSave(request);
            String imgUrl = (String) uploadResult.get("imgUrl");
            if (!StringUtils.isBlank(imgUrl)) {
                int result = anchorMapper.updateCardFace(anchorId, imgUrl);
                // 同时修改视频封面
                LiveVideoInfo videoInfo = new LiveVideoInfo();
                videoInfo.setAnchorId(anchorId);
                videoInfo.setVideoId(videoId);
                videoInfo.setCoverImg(imgUrl);
                liveVideoInfoMapper.updateByPKAndAnchorId(videoInfo);
                redisAdapter.delKeys(
                        RedisKey.ANCHOR_INFO_ + anchorId,
                        RedisKey.LIVE_VIDEO_INFO_ + videoId);
                // 保存到数据库中
                data.put("result", result);
                data.put("imgUrl", imgUrl);
            } else {
                data.put("result", 0);
            }
        } catch (Exception e) {
            logger.error("上传牌面图片失败", e);
            data.put("result", 0);
        }
        return data;
    }
}
