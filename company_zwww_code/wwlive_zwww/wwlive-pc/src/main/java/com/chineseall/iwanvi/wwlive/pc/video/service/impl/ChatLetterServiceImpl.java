package com.chineseall.iwanvi.wwlive.pc.video.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.chineseall.iwanvi.wwlive.common.cache.RedisClientAdapter;
import com.chineseall.iwanvi.wwlive.common.cache.RedisKey;
import com.chineseall.iwanvi.wwlive.dao.wwlive.ChatLetterMapper;
import com.chineseall.iwanvi.wwlive.dao.wwlive.LiveAdminMapper;
import com.chineseall.iwanvi.wwlive.dao.wwlive.RoleInfoMapper;
import com.chineseall.iwanvi.wwlive.pc.common.Page;
import com.chineseall.iwanvi.wwlive.pc.common.helper.LiveAdminHelper;
import com.chineseall.iwanvi.wwlive.pc.common.helper.RoleNobleHelper;
import com.chineseall.iwanvi.wwlive.pc.event.service.MedalHonorService;
import com.chineseall.iwanvi.wwlive.pc.video.service.ChatLetterService;

/**
 * 私信通知
 * @author DIKEPU
 *
 */
@Service
public class ChatLetterServiceImpl implements ChatLetterService {

	@Autowired
	private ChatLetterMapper chatLetterMapper;

    @Autowired
    private RedisClientAdapter redisAdapter;

    @Autowired
    private RoleInfoMapper roleInfoMapper;
    
    @Autowired
    private LiveAdminMapper liveAdminMapper;
    
    @Autowired
    private MedalHonorService medalHonorService;
    
	public Map<String, Object> getLetters(Page page, Long anchorId) {
		Long lastId = null;
		long cnt = 0L;
		Map<String, Object>  result = new HashMap<String, Object>();
		result.put("notRead", "0");
		if (page.getPageIndex() != 1) {
			lastId = new Long(page.getId());
		} else {
			Map<String, Object> cntAndMax = chatLetterMapper.cntLettersByReceive(anchorId);//最后一个id
			cnt = (cntAndMax.get("cnt") == null ? 0L : (long)cntAndMax.get("cnt"));
			page.setId((cntAndMax.get("lastId") == null ? 0L : (long) cntAndMax.get("lastId")));
			if (cnt > 0) {
				page.setTotal(cnt);
			}
			String notRead = redisAdapter.strGet(RedisKey.LETTER_NO_READ_CNT_ + anchorId);
			if (StringUtils.isNotBlank(notRead)) {
				result.put("notRead", notRead);
				redisAdapter.delKeys(RedisKey.LETTER_NO_READ_CNT_ + anchorId);
			}
		}
		if (page.getStart() <= page.getTotal() && page.getTotal() != 0) {
			List<Map<String, Object>> list = chatLetterMapper.findLettersByReceive(anchorId, 
					page.getStart(), page.getPageSize(), lastId);
			if (CollectionUtils.isNotEmpty(list)) {
			    // 添加房管判断
			    for(Map<String, Object> map:list){
			        Long userId = (Long) map.get("sendId");
                    boolean isAdmin = 
			                LiveAdminHelper.isAdmin(redisAdapter, liveAdminMapper, anchorId, userId);
			        if(isAdmin){
			            map.put("isAdmin", "1");
			        } else{
			            map.put("isAdmin", "0");
			        }
			        
			        // 添加用户勋章信息
			        List<String> medals = medalHonorService.getUserMedalsById(userId);
			        map.put("medals", medals);
		            //增加贵族等级及贵族图片
		            Integer level = RoleNobleHelper.userRoleNobleLevel(redisAdapter, roleInfoMapper, userId);
		            if(level != null && level.intValue() > 0) {
		            	map.put("nobleCode", level);
		            }
			    }
			    
				page.setData(list);
			}
			result.put("page", page);
			return result;
		} else {
			result.put("page", new Page());
			return result;
		}
		
	}

	@Override
	public String getNoReadLetterNum(Long anchorId) {
		if (redisAdapter.existsKey(RedisKey.LETTER_NO_READ_CNT_ + anchorId)) {
        	return redisAdapter.strGet(RedisKey.LETTER_NO_READ_CNT_ + anchorId);
        } else {
        	return "0";
        }
	}
	
}
