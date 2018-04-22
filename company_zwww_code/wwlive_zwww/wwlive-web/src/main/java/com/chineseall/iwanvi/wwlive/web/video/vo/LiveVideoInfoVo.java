package com.chineseall.iwanvi.wwlive.web.video.vo;

import java.util.List;

public class LiveVideoInfoVo {

    private Long videoId;// 主键id
    private Long anchorId;// 主播id
	/**
	 * 用于返回视频详情时的字段，数据不存在此列
	 */
	private String userName;// 昵称
	private String anchorName;// 主播昵称
    private Long roomNum;// 房间号
    private String chatroomId;// 聊天室id
    private String videoName;// 主题
    private Long viewers;// 观看人数
    private String loginId;// 用户注册融云id
    private String standURL;// 标准
    private String heighURL;// 高清
    private String fullHeighURL;// 全高清
    private Integer virtualCurrency;// 用户积分
    private Integer pointRate;// 积分百分比
    private Integer origin;// 用户来源
    private Integer acctType;//用户类型
    private String rongToken;// 融云token
    private Integer userType;//客户端2.0.0版本[包括]以后的用户类型字段, 0-普通用户, 1-超管, 2-房管

    private Integer videoStatus;//视频状态
    
    private int isblack;//是否黑名的
    private long gagTime;//禁言时间
    private int gagOfSuper;//被超管禁言禁言
    
    private int isCustomer;//0 = 该用户未给该主播送过礼品, >0 = 该用户给该主播送过礼品, 同时可作为送礼次数  
	private Integer formatType;// 视频格式
	
	private Integer levels;// 主播当前关卡
	private Double diamonds;// 主播钻石数
	private List<String> medals; // 主播获得的勋章

	private Long diamondsPerLv; // 通关所需钻石数	
    
//	private List<Map<String, Object>> nobles;//贵族信息
	private long nobleNum;//贵族数量
	private int nobleCode;//贵族等级
	

    private String liveImg;//直播广告URL
    private String jumpUrl;//广告跳转/下载 URL
    private String advId;//广告ID
    private String advName;//广告名称

    private Long contrib; // 该主播得到的贡献值

    private int sex; //性别

    public int getSex() {
        return sex;
    }

    public void setSex(int sex) {
        this.sex = sex;
    }

    public Long getContrib() {
        return contrib;
    }

    public void setContrib(Long contrib) {
        this.contrib = contrib;
    }

    public String getAdvName() {
        return advName;
    }

    public void setAdvName(String advName) {
        this.advName = advName;
    }

    public String getAdvId() {
        return advId;
    }

    public void setAdvId(String advId) {
        this.advId = advId;
    }

    public String getLiveImg() {
        return liveImg;
    }

    public void setLiveImg(String liveImg) {
        this.liveImg = liveImg;
    }

    public String getJumpUrl() {
        return jumpUrl;
    }

    public void setJumpUrl(String jumpUrl) {
        this.jumpUrl = jumpUrl;
    }

    public Long getDiamondsPerLv() {
        return diamondsPerLv;
    }

    public void setDiamondsPerLv(Long diamondsPerLv) {
        this.diamondsPerLv = diamondsPerLv;
    }

    public List<String> getMedals() {
        return medals;
    }

    public void setMedals(List<String> medals) {
        this.medals = medals;
    }

    public Integer getLevels() {
        return levels;
    }

    public void setLevels(Integer levels) {
        this.levels = levels;
    }

    public Double getDiamonds() {
        return diamonds;
    }

    public void setDiamonds(Double diamonds) {
        this.diamonds = diamonds;
    }

    public Integer getUserType() {
        return userType;
    }

    public void setUserType(Integer userType) {
        this.userType = userType;
    }

    public int getIsCustomer() {
        return isCustomer;
    }

    public void setIsCustomer(int isCustomer) {
        this.isCustomer = isCustomer;
    }

    public Long getVideoId() {
        return videoId;
    }

    public void setVideoId(Long videoId) {
        this.videoId = videoId;
    }

    public Integer getVirtualCurrency() {
        return virtualCurrency;
    }

    public void setVirtualCurrency(Integer virtualCurrency) {
        this.virtualCurrency = virtualCurrency;
    }

    public Integer getPointRate() {
        return pointRate;
    }

    public void setPointRate(Integer pointRate) {
        this.pointRate = pointRate;
    }

    public Integer getOrigin() {
        return origin;
    }

    public void setOrigin(Integer origin) {
        this.origin = origin;
    }

    public Long getAnchorId() {
        return anchorId;
    }

    public void setAnchorId(Long anchorId) {
        this.anchorId = anchorId;
    }

    public Long getRoomNum() {
        return roomNum;
    }

    public void setRoomNum(Long roomNum) {
        this.roomNum = roomNum;
    }

    public String getChatroomId() {
        return chatroomId;
    }

    public void setChatroomId(String chatroomId) {
        this.chatroomId = chatroomId;
    }

    public String getVideoName() {
        return videoName;
    }

    public void setVideoName(String videoName) {
        this.videoName = videoName;
    }

    public Long getViewers() {
        return viewers;
    }

    public void setViewers(Long viewers) {
        this.viewers = viewers;
    }

    public String getStandURL() {
        return standURL;
    }

    public void setStandURL(String standURL) {
        this.standURL = standURL;
    }

    public String getHeighURL() {
        return heighURL;
    }

    public void setHeighURL(String heighURL) {
        this.heighURL = heighURL;
    }

    /**
     * 获得全高清地址
     * 
     * @return
     */
    public String getFullHeighURL() {
        return fullHeighURL;
    }

    /**
     * 设置全高清地址
     * 
     * @param fullHeighURL
     */
    public void setFullHeighURL(String fullHeighURL) {
        this.fullHeighURL = fullHeighURL;
    }

    public String getLoginId() {
        return loginId;
    }

    public void setLoginId(String loginId) {
        this.loginId = loginId;
    }

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getRongToken() {
		return rongToken;
	}

	public void setRongToken(String rongToken) {
		this.rongToken = rongToken;
	}

	public String getAnchorName() {
		return anchorName;
	}

	public void setAnchorName(String anchorName) {
		this.anchorName = anchorName;
	}

	public int getIsblack() {
		return isblack;
	}

	public void setIsblack(int isblack) {
		this.isblack = isblack;
	}

	public long getGagTime() {
		return gagTime;
	}

	public void setGagTime(long gagTime) {
		this.gagTime = gagTime;
	}

    public Integer getAcctType() {
        return acctType;
    }

    public void setAcctType(Integer acctType) {
        this.acctType = acctType;
    }

	public Integer getVideoStatus() {
		return videoStatus;
	}

	public void setVideoStatus(Integer videoStatus) {
		this.videoStatus = videoStatus;
	}

	public Integer getFormatType() {
		return formatType;
	}

	public void setFormatType(Integer formatType) {
		this.formatType = formatType;
	}

	public int getGagOfSuper() {
		return gagOfSuper;
	}

	public void setGagOfSuper(int gagOfSuper) {
		this.gagOfSuper = gagOfSuper;
	}

	public long getNobleNum() {
		return nobleNum;
	}

	public void setNobleNum(long nobleNum) {
		this.nobleNum = nobleNum;
	}

	public int getNobleCode() {
		return nobleCode;
	}

	public void setNobleCode(int nobleCode) {
		this.nobleCode = nobleCode;
	}

	@Override
	public String toString() {
		return "LiveVideoInfoVo [videoId=" + videoId + ", anchorId=" + anchorId
				+ ", userName=" + userName + ", anchorName=" + anchorName
				+ ", roomNum=" + roomNum + ", chatroomId=" + chatroomId
				+ ", videoName=" + videoName + ", viewers=" + viewers
				+ ", loginId=" + loginId + ", standURL=" + standURL
				+ ", heighURL=" + heighURL + ", fullHeighURL=" + fullHeighURL
				+ ", virtualCurrency=" + virtualCurrency + ", pointRate="
				+ pointRate + ", origin=" + origin + ", acctType=" + acctType
				+ ", rongToken=" + rongToken + ", userType=" + userType
				+ ", videoStatus=" + videoStatus + ", isblack=" + isblack
				+ ", gagTime=" + gagTime + ", gagOfSuper=" + gagOfSuper
				+ ", isCustomer=" + isCustomer + ", formatType=" + formatType
				+ ", levels=" + levels + ", diamonds=" + diamonds + ", medals="
				+ medals + ", diamondsPerLv=" + diamondsPerLv + ", nobleNum="
				+ nobleNum + ", nobleCode=" + nobleCode + "]";
	}

}
