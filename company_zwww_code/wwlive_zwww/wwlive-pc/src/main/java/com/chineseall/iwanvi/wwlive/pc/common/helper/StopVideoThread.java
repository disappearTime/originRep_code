package com.chineseall.iwanvi.wwlive.pc.common.helper;

import java.util.Calendar;
import java.util.Date;

import com.chineseall.iwanvi.wwlive.dao.wwlive.OrderInfoMapper;
import com.chineseall.iwanvi.wwlive.pc.common.util.DateTools;
import org.apache.log4j.Logger;

import com.chineseall.iwanvi.wwlive.common.cache.RedisClientAdapter;
import com.chineseall.iwanvi.wwlive.common.cache.RedisExpireTime;
import com.chineseall.iwanvi.wwlive.common.cache.RedisKey;
import com.chineseall.iwanvi.wwlive.common.tools.RongMsgUtils;
import com.service.impl.FollowNoticeServiceImpl;

public class StopVideoThread implements Runnable {

	private static final Logger LOGGER = Logger
			.getLogger(StopVideoThread.class);

	private Long anchorId;

	private RedisClientAdapter redisAdapter;

	private String chatRoomId;

	private Integer formatType;

	private Long videoId;

	private OrderInfoMapper orderInfoMapper;

	public StopVideoThread(Long anchorId) {
		super();
		this.anchorId = anchorId;
	}

	public StopVideoThread(RedisClientAdapter redisAdapter, Long anchorId,
			String chatRoomId, Integer formatType, Long videoId, OrderInfoMapper orderInfoMapper) {
		this.anchorId = anchorId;
		this.redisAdapter = redisAdapter;
		this.chatRoomId = chatRoomId;
		this.formatType = formatType;
		this.videoId = videoId;
		this.orderInfoMapper = orderInfoMapper;
	}

	@Override
	public void run() {
		LOGGER.info("-----------------------开始发送停止通知---------------------------------------------------------------");
		FollowNoticeServiceImpl followNoticeService = FolloServiceSingleton
				.getInstance();
		try {
			followNoticeService.noticeAnchorOpenLive(this.anchorId, 0,
					(new Date()).getTime());

			// 上报该主播最近七天收入
			String deadline = DateTools.getAWeekAgoDate(Calendar.getInstance());
			int income = orderInfoMapper.getWeekInocme(anchorId, deadline);
			followNoticeService.noticeAnchorMoney(this.anchorId, income, System.currentTimeMillis());

		} catch (Exception e) {
			LOGGER.error("followNoticeService 直播停止通知失败：", e);
		}
		if (formatType == null) {
			return;
		}
		if (1 == formatType.intValue()) {
			String stopKey = RedisKey.STOP_VIDEO_NOTICE_ + videoId;
			if (!redisAdapter.existsKey(stopKey).booleanValue()) {
				noticeForLivePause();
				redisAdapter.delKeys(RedisKey.START_VIDEO_NOTICE_ + videoId);
				redisAdapter.strSetexByNormal(stopKey,
						RedisExpireTime.EXPIRE_HOUR_1, "0");
			}
		}

	}

	/**
	 * 发送直播暂停消息
	 * 
	 * @param info
	 */
	private void noticeForLivePause() {
		String key = RedisKey.STOP_VIDEO_NOTICE_ + videoId;
		if (!"0".equals(redisAdapter.strGet(key))) {
			return;
		}
		RongMsgUtils.sendChatroomMsg(chatRoomId, this.anchorId, 12, "", "");
	}

}
