package com.chineseall.iwanvi.wwlive.web.launch.service;

import java.text.ParseException;
import java.util.List;
import java.util.Map;

import com.chineseall.iwanvi.wwlive.domain.wwlive.Anchor;

public interface AnchorService {

    Map<String, Object> getMsgList(Integer pageNo, Integer pageSize, Long anchorId);

    public Map<String, Object> getAnchorInfo(Long anchorId);

    public List<Map<String, Object>> getAnchorIncomeList(Integer pageNo, Integer pageSize, Long anchorId);

	Map<String, Object> getAnchorDetail(Long anchorId);
	
	/**
	 * 修改主播资料
	 * @param anchor
	 */
	Integer modifyAnchorInfo(Anchor anchor);
	
	/**
	 * 获取主播资料信息
	 * @param anchorId
	 * @return
	 * @throws ParseException
	 */
    Map<String, Object> getAnchorInfoForModify(Long anchorId) throws ParseException;
    
    /**
     * 获得主播信息 "anchorId","rongToken", "acctStatus", "userName", "headImg", "roomNum"
     * @param anchorId
     * @return
     */
    Anchor getAnchorInfo4UpdateIos(Long anchorId) throws ParseException ;

	/**
	 * IOS端 直播收入
	 */
	Map<String, Object> getAnchorIncome(long anchorId, int pageNo, int pageSize);

	/**
	 *
	 */
	Map<String, Object> findIncomeDetailsBy(int pageNo, int pageSize, long anchorId, long videoId);

    Map<String, Object> getGameInfo(Long anchorId);
}
