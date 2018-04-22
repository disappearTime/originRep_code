package com.chineseall.iwanvi.wwlive.web.common.enums;

/**
 * 用户来源 cx创新版 sc中文书城
 * @author DIKEPU
 *
 */
public enum Origin {
	FROMCX("cx"),//创新版 
	FROMSC("sc");//中文书城
	
	private String from;
	
	private Origin(String from) {
		this.from = from;
	}
	
	private String getFrom() {
		return from;
	}

	public static Origin getOrigin(String from) {
		Origin[] types = Origin.values();
		for (Origin type : types) {
			if (type.getFrom().equals(from)) {
				return type;
			}
		}
		return null;
	}

	@Override
	public String toString() {
		return this.from;
	}
	
}
