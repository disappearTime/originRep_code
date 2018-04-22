package com.chineseall.iwanvi.wwlive.web.common.enums;
/**
 * 充值来源
 * @author DIKEPU
 *
 */
public enum ChargeType {

	//充值来源 1-积分, 2-微信, 3-支付宝
	/**
	 * 积分充值
	 */
	CXCHARGE(1){

		public String getChargeBeanName() {
			return "cxCharge";
		}
	},
	
	/**
	 * 微信充值
	 */
	WXCHARGE(2){

		public String getChargeBeanName() {
			return "wxCharge";
		}
	},
	
	/**
	 * 支付宝充值
	 */
	ZFBCHARGE(3){

		public String getChargeBeanName() {
			return "aliCharge";
		}
	},
    
    /**
     * 微信SDK充值
     */
    WXSDKCHARGE(4){
        
        public String getChargeBeanName() {
            return "wxSDKCharge";
        }
    };
	
	private int origin;
	
	private ChargeType(int origin) {
		this.origin = origin;
	}
	
	/**
	 * 默认支付宝
	 * @return
	 */
	public String getChargeBeanName() {
		return "aliCharge";
	}
	
	public int getOrigin() {
		return origin;
	}

	/**
	 * 
	 * @param 
	 * @return
	 */
	public static ChargeType getChargeType(int origin) {
		ChargeType[] types = ChargeType.values();
		for (ChargeType type : types) {
			if (type.getOrigin() == origin) {
				return type;
			}
		}
		return null;
	}
	
}
