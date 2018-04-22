package com.chineseall.iwanvi.wwlive.web.common.util;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
/**
 * 日期工具类
 * @author niuqianghong
 *
 */
public class DateTools {
    /**
     * 根据日期获得星座
     * @param date
     * @return
     */
    public static String getZodiacByDate(Date date){
    	if (date == null) {
    		return "";
    	}
        int[] dayArr = new int[] { 20, 19, 21, 20, 21, 22, 23, 23, 23, 24, 23, 22 };  
        String[] constellationArr = new String[] { "摩羯座", "水瓶座", "双鱼座", "白羊座", "金牛座", "双子座", "巨蟹座", "狮子座", "处女座", "天秤座", "天蝎座", "射手座", "摩羯座" };  
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int month = calendar.get(Calendar.MONTH) + 1;
        return day < dayArr[month - 1] ? constellationArr[month - 1] : constellationArr[month];  
    }
    /**
     * 根据日期计算年龄
     * @param date
     * @return
     */
    public static int getAgeByDate(Date date){
    	if (date == null) {
    		return 0;
    	}
        Calendar oldTime = Calendar.getInstance();
        oldTime.setTime(date);
        Calendar now = Calendar.getInstance();
        int age = now.get(Calendar.YEAR) - oldTime.get(Calendar.YEAR);
        return age > 0 ? age : 0;
    }
    
    /**
     * 根据日期计算年龄
     * @param date
     * @return
     * @throws ParseException 
     */
    public static int getAgeByDate(String date) throws ParseException{
    	if (StringUtils.isBlank(date)) {
    		return 0;
    	}
        Calendar oldTime = Calendar.getInstance();
        oldTime.setTime(DateUtils.parseDate(date, new String[]{"yyyy-MM-dd", "yyyy-MM-dd hh:mm:ss"}));
        Calendar now = Calendar.getInstance();
        int age = now.get(Calendar.YEAR) - oldTime.get(Calendar.YEAR);
        return age > 0 ? age : 0;
    }
    
    public static String formatDate(Date date, String pattern){
        SimpleDateFormat sdf = new SimpleDateFormat(pattern);
        return sdf.format(date);
    }
    
    /**
     * 获得一周前的日期
     * @param calendar
     * @return
     */
    public static String getAWeekAgoDate(Calendar calendar){
        calendar.add(Calendar.DAY_OF_MONTH, -7);
        int month = calendar.get(Calendar.MONTH) + 1;
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        String date = calendar.get(Calendar.YEAR) + "-" +  (month > 9 ? month: ("0" + month)) + "-" + (day > 9 ? day : ("0" + day));
        return date;
    }
}
