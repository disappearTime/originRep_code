package com.chineseall.iwanvi.wwlive.web.video.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.chineseall.iwanvi.wwlive.common.cache.RedisClientAdapter;
import com.chineseall.iwanvi.wwlive.common.cache.RedisKey;
import com.chineseall.iwanvi.wwlive.common.constants.Constants;
import com.chineseall.iwanvi.wwlive.common.external.rongcloud.RongCloudFacade;
import com.chineseall.iwanvi.wwlive.common.external.rongcloud.domain.TxtMessage;
import com.chineseall.iwanvi.wwlive.dao.wwlive.BlackListMapper;
import com.chineseall.iwanvi.wwlive.dao.wwlive.ChatLetterMapper;
import com.chineseall.iwanvi.wwlive.dao.wwlive.LiveVideoInfoMapper;
import com.chineseall.iwanvi.wwlive.domain.wwlive.ChatLetter;
import com.chineseall.iwanvi.wwlive.domain.wwlive.LiveVideoInfo;
import com.chineseall.iwanvi.wwlive.web.common.helper.BlackListHelper;
import com.chineseall.iwanvi.wwlive.web.common.helper.LiveVideoInfoHelper;
import com.chineseall.iwanvi.wwlive.web.video.service.ChatLetterService;

@Service
public class ChatLetterServiceImpl implements ChatLetterService {

    private static final Logger LOGGER = Logger.getLogger(ChatLetterServiceImpl.class);
    
	@Autowired
	ChatLetterMapper chatLetterMapper;
	
    @Autowired
    private RedisClientAdapter redisAdapter;
    
    @Autowired
    private LiveVideoInfoMapper liveVideoInfoMapper;

	@Autowired
	private BlackListMapper blackListMapper;
	
	public int sendLetter(Long sendId, Long receiveId, Long videoId, String content) {
		// 0 失败 1成功 2禁言
    	String key = RedisKey.BLACKLIST_ + receiveId + Constants.UNDERLINE + sendId;
		if (redisAdapter.existsKey(key)) {
			return 2;
		}
    	if (BlackListHelper.isOnBlackList(blackListMapper, redisAdapter, sendId)) {
    		return 2;
    	}
		ChatLetter letter = getLetter(sendId, receiveId, content);
		int cnt = chatLetterMapper.insert(letter);
		if (cnt > 0) {
			redisAdapter.strIncr(RedisKey.LETTER_NO_READ_CNT_ + receiveId);
		}
		sendNotice(sendId, videoId);
		return cnt;
	}
	
	
	private ChatLetter getLetter(Long userId, Long anchorId, String content) {
		ChatLetter letter = new ChatLetter();
		letter.setSendId(userId);
		letter.setReceiveId(anchorId);
		letter.setContent(content);
		return letter;
	}
	
	/**
	 * 发送主播私信通知 
	 * @param videoId
	 */
	private void sendNotice(Long sendId, Long videoId) {
		if (videoId != null && videoId.longValue() > 0) {
			String key = RedisKey.LIVE_VIDEO_INFO_ + videoId;
			String chatroomId = "";
			LiveVideoInfo vo = null;
			if (redisAdapter.existsKey(key)) {
				vo = LiveVideoInfoHelper.getVideoInfoCache(redisAdapter, liveVideoInfoMapper, videoId, "chatroomId");
			} else {
				vo = LiveVideoInfoHelper.getAndCacheVideoInfo(redisAdapter, liveVideoInfoMapper, videoId);
			}
			if (vo != null) {
				chatroomId = vo.getChatroomId();
				try {
					List<String> chatIds = new ArrayList<String>();
					chatIds.add(chatroomId);
					TxtMessage tx = new TxtMessage("");
					JSONObject json = new JSONObject();
					json.put("dataType", new Integer(11));
					json.put("dataValue", "");
					json.put("dataExtra", "");
					tx.setExtra(json.toJSONString());
					RongCloudFacade.publishChatroomMessage(sendId.toString(), chatIds, tx, Constants.FORMAT_JSON);
				} catch (Exception e) {
					LOGGER.error("通知失败，ChatroomId：" + chatroomId, e);
				}
			}
		}
	}
	
}
