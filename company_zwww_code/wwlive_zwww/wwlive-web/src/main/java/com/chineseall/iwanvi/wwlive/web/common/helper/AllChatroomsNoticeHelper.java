package com.chineseall.iwanvi.wwlive.web.common.helper;

import java.util.List;

import com.chineseall.iwanvi.wwlive.common.tools.RongMsgUtils;
import com.chineseall.iwanvi.wwlive.dao.wwlive.LiveVideoInfoMapper;

/**
 * 所有聊天室通知
 * @author DIKEPU
 *
 */
public class AllChatroomsNoticeHelper {

	public static void sendMsg4AllChatrooms(LiveVideoInfoMapper liveVideoInfoMapper, Integer dataType, 
            String dataValue, String dataExtra) {
		if (liveVideoInfoMapper == null) {
			return;
		}
		List<String> chatIds = liveVideoInfoMapper.findLivingChatroomIds();
		RongMsgUtils.sendChatroomMsg(chatIds, 0L, dataType, dataValue, dataExtra);
	}

	public static void sendMsg4NotThisChatrooms(LiveVideoInfoMapper liveVideoInfoMapper, Integer dataType,
											String dataValue, String dataExtra,String chatrRooom) {
		if (liveVideoInfoMapper == null) {
			return;
		}
		List<String> chatIds = liveVideoInfoMapper.findLivingNotChatroomId(chatrRooom);
		RongMsgUtils.sendChatroomMsg(chatIds, 0L, dataType, dataValue, dataExtra);
	}
	
}
