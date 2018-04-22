package com.chineseall.iwanvi.wwlive.web.my.service;

import java.util.Map;

public interface MyAcctInfoService {
	
	public Map<String, Object> getUserAcctInfo(long userId, String app);

	public Map<String, Object> getExpenseList(Long userId, int origin, int pageNum,
			int pageSize);
}
