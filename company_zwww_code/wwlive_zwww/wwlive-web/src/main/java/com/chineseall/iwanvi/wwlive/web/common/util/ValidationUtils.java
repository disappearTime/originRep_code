package com.chineseall.iwanvi.wwlive.web.common.util;

import com.chineseall.iwanvi.wwlive.common.tools.StrMD5;
import org.apache.commons.lang.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ValidationUtils {

    private final static String COVERKEY = "coverKey";

    /**
     * md5校验参数
     *
     * @param request
     * @param parameters
     * @return
     */
    public static boolean isValid(HttpServletRequest request, String... parameters) {
        String md5Str = getMD5(request, parameters);
        if (StringUtils.isBlank(request.getParameter(COVERKEY))) {
            return false;
        }
        return request.getParameter(COVERKEY).equals(md5Str);
    }

    /**
     * 主播端接口校验
     *
     * @param request
     * @param parameters
     * @return
     */
    public static boolean isValidForLaunch(HttpServletRequest request, String... parameters) {
        String md5Str = getMD5ForLaunch(request, parameters);
        if (StringUtils.isBlank(request.getParameter(COVERKEY))) {
            return false;
        }
        return request.getParameter(COVERKEY).equals(md5Str);
    }

    /**
     * 未验证其他参数，只验证通用参数 userId nonce requestId
     *
     * @param request
     * @return
     */
    public static boolean isValid(HttpServletRequest request) {
        return isValid(request, "");
    }

    /**
     * 获得md5字符串
     *
     * @param request
     * @param parameters
     * @return
     */
    private static String getMD5(HttpServletRequest request, String[] parameters) {
        String key = "";
        key += request.getParameter("userId");
        key += request.getParameter("nonce");
        key += request.getParameter("requestId");
        if (parameters != null) {
            for (String param : parameters) {
                if (StringUtils.isBlank(param)) {
                    continue;
                }
                key += request.getParameter(param);
            }
        }
        if (StringUtils.isBlank(key)) {
            return "";
        }
        String md5Str = StrMD5.getInstance().getStringMD5(key);
        return md5Str;
    }

    /**
     * 按照
     *
     * @param request
     * @param parameters
     * @return
     */
    private static String getMD5ForLaunch(HttpServletRequest request, String... parameters) {
        String key = "";
        // 每个接口单独的参数
        if (parameters != null) {
            List<String> paramList = new ArrayList<>(Arrays.asList(parameters));
            paramList.add("anchorId");
            paramList.add("nonce");
            paramList.add("requestId");
            Collections.sort(paramList);
            for (String param : paramList) {
                if (StringUtils.isBlank(param)) {
                    continue;
                }
                key += request.getParameter(param);
            }
        } else {
            // 通用字段
            key += request.getParameter("anchorId");
            key += request.getParameter("nonce");
            key += request.getParameter("requestId");
        }
        if (StringUtils.isBlank(key)) {
            return "";
        }
        String md5Str = StrMD5.getInstance().getStringMD5(key);
        return md5Str;
    }

    /**
     * IMEI校验码算法：
     * (1).将偶数位数字分别乘以2，分别计算个位数和十位数之和
     * (2).将奇数位数字相加，再加上上一步算得的值
     * (3).如果得出的数个位是0则校验位为0，否则为10减去个位数
     * 如：35 89 01 80 69 72 41 偶数位乘以2得到5*2=10 9*2=18 1*2=02 0*2=00 9*2=18 2*2=04 1*2=02,计算奇数位数字之和和偶数位个位十位之和，
     * 得到 3+(1+0)+8+(1+8)+0+(0+2)+8+(0+0)+6+(1+8)+7+(0+4)+4+(0+2)=63
     * 校验位 10-3 = 7
     *
     * @param IMEI
     * @return
     */
    public static boolean checkIMEI(String IMEI) {
        try {
            String rexp = "^[0-9]+$";
            if (!regExMatches(IMEI, rexp)) {
                return false;// IMEI由纯数字组成
            }

            int length = IMEI.length();
            if (length < 14 || length > 16) {
                return false;// 14 <= IMEI位数 <= 16
            }

            if (length == 14) {
                return true;// 14位IMEI不做校验
            }

            char[] imeiChar = IMEI.substring(0, 14).toCharArray();
            int resultInt = 0;
            for (int i = 0; i < imeiChar.length; i++) {
                int a = Integer.parseInt(String.valueOf(imeiChar[i]));
                i++;
                final int temp = Integer.parseInt(String.valueOf(imeiChar[i])) * 2;
                final int b = temp < 10 ? temp : temp - 9;
                resultInt += a + b;
            }
            resultInt %= 10;
            resultInt = resultInt == 0 ? 0 : 10 - resultInt;
            char checkBit = IMEI.charAt(14);
            return ((int) checkBit - resultInt == 48);// ASCII码和实际数字数值相差48
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * 正则表达式校验
     *
     * @param str   待检测字符串
     * @param regEx 正则表达式
     * @return
     */
    public static boolean regExMatches(String str, String regex) {
        if (StringUtils.isBlank(str) || StringUtils.isBlank(regex)) {
            return false;
        }
        return str.matches(regex);
    }

    /**
     * 校验mac地址是否有效
     *
     * @param mac
     * @return
     */
    public static boolean checkMac(String mac) {
        return regExMatches(mac, "^([A-Fa-f0-9]{2}[-,:]){5}[A-Fa-f0-9]{2}$");
    }

    /**
     * 校验IP地址是否有效
     *
     * @param IP
     * @return
     */
    public static boolean checkIP(String IP) {
        return regExMatches(IP, "^(\\d{1,2}|1\\d\\d|2[0-4]\\d|25[0-5])\\.(\\d{1,2}|1\\d\\d|2[0-4]\\d|25[0-5])\\.(\\d{1,2}|1\\d\\d|2[0-4]\\d|25[0-5])\\.(\\d{1,2}|1\\d\\d|2[0-4]\\d|25[0-5])$");
    }

}
