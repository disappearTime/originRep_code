package com.chineseall.iwanvi.wwlive.pc.video.service;

import java.util.Map;

import com.chineseall.iwanvi.wwlive.pc.common.Page;

public interface LiveVideoService {

	/**
	 * 创建视频流
	 * @param videoName
	 * @param videoType
	 * @param coverImg
	 * @param needRecord
	 * @param anchorId
	 * @return
	 */
	public Map<String, Object> createVideoUrl(String videoName, int videoType,
			String coverImg, boolean needRecord, long anchorId);

	/**
	 * PC端主播获得该主播直播中视频和历史视频
	 * @param anchorId
	 * @param page
	 * @return
	 */
	public Page getVideoList(long anchorId, Page page);

	/**
	 * 修改视频信息
	 * @param videoId
	 * @param coverImg
	 * @param videoName
	 * @param anchorId
	 * @param videoType
	 * @return
	 */
	public int modifyVideoInfo(long videoId, String coverImg, String videoName, long anchorId, Integer videoType);
	
	/**
	 * 删除视频
	 * @param videoId
	 * @param anchorId
	 * @return
	 */
	public int deleteVideo(long videoId, long anchorId);
	
	/**
	 * 停止直播,加入黑名单
	 * @param videoId
	 * @param anchorId
	 * @param streamName
	 * @return
	 */
	public int stopVideo(long videoId, long anchorId, String streamName, int vdoid);

	/**
	 * 开始直播,移除黑名单
	 * @param videoId
	 * @param anchorId
	 * @param streamName
	 * @return
	 */
	public int startVideo(long videoId, long anchorId, String streamName);

	/**
	 * 直播详情
	 * @param videoId
	 * @param anchorId
	 * @return
	 */
	public Map<String, Object> videoInfo(long videoId, long anchorId);

	/**
	 * 正直播数
	 * @param anchorId
	 * @return
	 */
	public int getLivingCnt(long anchorId);

    public Map<String, Object> getViewers(long videoId, int videoStatus);

    public Map<String, Object> getIncome(long videoId);


}
