package com.chineseall.iwanvi.wwlive.web.common.util;

import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.ArrayList;
import java.util.List;

/**
 * @author hekai
 * 
 */
public class Page {
	public static final int DEFAUTLPAGESIZE = 10;
	public static final int[] OPTIONALPAGESIZE = { 10, 20, 30, 50, 100, 200,
			300, 500 };
	private long total = 0L;
	private int pageSize = DEFAUTLPAGESIZE;
	private List<? extends Object> data = new ArrayList<>();
	private Integer pageNo = 1;
	private long id = 0L;// 主键
	private JSONObject extra;
	
	// 分页URL
	private String url;

	public long getTotal() {
		return total;
	}

	public void setTotal(long total) {
		this.total = total;
	}

	public int getPageSize() {

		for (int size : OPTIONALPAGESIZE) {
			if (size == pageSize) {
				return size;
			}
		}

		return DEFAUTLPAGESIZE;
	}

	public void setPageSize(Integer pageSize) {
		this.pageSize = pageSize == null ? DEFAUTLPAGESIZE : pageSize;
	}

	public List<? extends Object> getData() {
		return data;
	}

	public void setData(List<? extends Object> data) {
		this.data = data;
	}

	public Integer getPageNo() {
		return pageNo;
	}

	public void setPageNo(Integer pageNo) {
		this.pageNo = pageNo == null ? 1 : pageNo;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	@JsonIgnore
	public int getStart() {
		return pageNo == 1 ? 0 : (pageNo - 1) * pageSize;
	}

	@JsonIgnore
	public long getPageTotal() {

		if (this.total / pageSize == 0) {
			return 1;
		} else if (this.total % pageSize == 0) {
			return total / pageSize;
		} else {
			return total / pageSize + 1;
		}
	}

	/**
	 * 系统默认支持的不同pageSize的分页策略
	 * 
	 * @return
	 */
	@JsonIgnore
	public int[] getOptionalPageSize() {
		return OPTIONALPAGESIZE;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public JSONObject getExtra() {
		return extra;
	}

	public void setExtra(JSONObject extra) {
		this.extra = extra;
	}

	@Override
	public String toString() {
		return "Page{" + "total=" + total + ", pageSize=" + pageSize
				+ ", data=" + data + ", pageNo=" + pageNo + ", start="
				+ getStart() + ", extra=" + extra + "}";
	}
}
