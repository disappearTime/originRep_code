package com.chineseall.iwanvi.wwlive.web.common.enums.share;

/**
 * 
 * @author DIKEPU
 * @since 2017-02-27 主播端开发一期
 */
public enum ShareType {
	VIDEOSHARE(0) {

		@Override
		public String getBeanName() {
			return "launchVideoShare";
		}
		
	};

	private int type;

	private ShareType(int type) {
		this.type = type;
	}

	public abstract String getBeanName();
	
	/**
	 * 根据数字获得该上传格式类型
	 * @param 
	 * @return
	 */
	public static ShareType getShareType(int uploadType) {
		ShareType[] types = ShareType.values();
		for (ShareType type : types) {
			if (type.type == uploadType) {
				return type;
			}
		}
		return null;
	}
	
}
