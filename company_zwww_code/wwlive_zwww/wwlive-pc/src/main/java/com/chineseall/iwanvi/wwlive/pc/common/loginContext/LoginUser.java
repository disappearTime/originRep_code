package com.chineseall.iwanvi.wwlive.pc.common.loginContext;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

import com.chineseall.iwanvi.wwlive.pc.video.util.DateUtil;

/**
 * Created by kai on 15/8/5.
 */
public class LoginUser implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = -3022130783656521962L;

    /**
     * 用户id
     */
    private long userId;

    /**
     * 登录名称
     */
    private String userName;

    /**
     * 真实名称
     */
    private String realName;

    /**
     * 登录时间
     */
    private Long loginTime;

    /**
     * 登录失效时间
     */
    private Long expireTime;
    
    private Integer acctStatus;//账户状态

    private String headImg;// 用户头像
    private Integer sex;// 性别
    private Long roomNum;// 房间号
    private String notice;// 主播公告签名
    private Integer age;// 年龄

    public Integer getAcctStatus() {
        return acctStatus;
    }

    public void setAcctStatus(Integer acctStatus) {
        this.acctStatus = acctStatus;
    }

    public Long getUserId() {
        return userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public Long getLoginTime() {
        return loginTime;
    }

    public void setLoginTime(Long loginTime) {
        this.loginTime = loginTime;
    }

    public String getRealName() {
        return realName;
    }

    public void setRealName(String realName) {
        this.realName = realName;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public Long getExpireTime() {
        return expireTime;
    }

    public void setExpireTime(Long expireTime) {
        this.expireTime = expireTime;
    }

    public String getHeadImg() {
        return headImg;
    }

    public void setHeadImg(String headImg) {
        this.headImg = headImg;
    }

    public Integer getSex() {
        return sex;
    }

    public void setSex(Integer sex) {
        this.sex = sex;
    }

    public Long getRoomNum() {
        return roomNum;
    }

    public void setRoomNum(Long roomNum) {
        this.roomNum = roomNum;
    }

    public String getNotice() {
        return notice;
    }

    public void setNotice(String notice) {
        this.notice = notice;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    @Override
    public String toString() {
        return "LoginUser{" + "userId=" + userId + ", userName='" + userName + '\'' + ", realName='" + realName + '\''
                + ", loginTime=" + loginTime + ", expireTime=" + expireTime + '}';
    }

    /**
     * 转换Map为this对象
     * 
     * @param map
     * @param isDealNull
     */
    public void doMapToDtoValue(Map<String, Object> map, boolean isDealNull) {
        if (map == null || map.isEmpty()) {
            return;
        }
        if (null != map.get("userId")) {
            this.userId = (long) map.get("userId");
        } else {
            if (isDealNull && map.containsKey("userId")) {
                this.userId = (long) map.get("userId");
            }
        }

        if (userId == 0L) {
            this.userId = (long) (map.get("anchorId"));
        } else {
            if (isDealNull && map.containsKey("anchorId")) {
                this.userId = 0L;
            }
        }

        if (null != map.get("userName")) {
            this.userName = (String) map.get("userName");
        } else {
            if (isDealNull && map.containsKey("userName")) {
                this.userName = (String) map.get("userName");
            }
        }
        if (null != map.get("realName")) {
            this.realName = (String) map.get("realName");
        } else {
            if (isDealNull && map.containsKey("realName")) {
                this.realName = (String) map.get("realName");
            }
        }

        if (null == this.realName) {
            if (null != map.get("passport")) {
                this.realName = (String) map.get("passport");
            } else {
                if (isDealNull && map.containsKey("passport")) {
                    this.realName = (String) map.get("passport");
                }
            }
        }

        if (null != map.get("loginTime")) {
            this.loginTime = (Long) map.get("loginTime");
        } else {
            if (isDealNull && map.containsKey("loginTime")) {
                this.loginTime = (Long) map.get("loginTime");
            }
        }
        if (null != map.get("expireTime")) {
            this.expireTime = (Long) map.get("expireTime");
        } else {
            if (isDealNull && map.containsKey("expireTime")) {
                this.expireTime = (Long) map.get("expireTime");
            }
        }
        if (null != map.get("headImg")) {
            this.headImg = (String) map.get("headImg");
        } else {
            if (isDealNull && map.containsKey("headImg")) {
                this.headImg = (String) map.get("headImg");
            }
        }
        if (null != map.get("sex")) {
            this.sex = (Integer) map.get("sex");
        } else {
            if (isDealNull && map.containsKey("sex")) {
                this.sex = (Integer) map.get("sex");
            }
        }
        if (null != map.get("roomNum")) {
            this.roomNum = (Long) map.get("roomNum");
        } else {
            if (isDealNull && map.containsKey("roomNum")) {
                this.roomNum = (Long) map.get("roomNum");
            }
        }
        if (null != map.get("notice")) {
            this.notice = (String) map.get("notice");
        } else {
            if (isDealNull && map.containsKey("notice")) {
                this.notice = (String) map.get("notice");
            }
        }
        if (null != map.get("age")) {
            this.age = (Integer) map.get("age");
        } else {
            if (isDealNull && map.containsKey("age")) {
                this.age = (Integer) map.get("age");
            }
        }
        if (null != map.get("birthday")) {
            this.age = DateUtil.getAgeByDate((Date) map.get("birthday"));
        } else {
            if (isDealNull && map.containsKey("birthday")) {
                this.age = Integer.valueOf(0);
            }
        }
        if (null != map.get("acctStatus")) {
            this.acctStatus = Integer.valueOf((Integer)map.get("acctStatus"));
        } else {
            if (isDealNull && map.containsKey("acctStatus")) {
                this.acctStatus = Integer.valueOf(0);
            }
        }
    }

}
