package com.chineseall.iwanvi.wwlive.web.nobility.service;

import com.chineseall.iwanvi.wwlive.common.check.ResponseResult;
import com.chineseall.iwanvi.wwlive.domain.wwlive.*;

import javax.servlet.http.HttpServletRequest;

import java.util.List;
import java.util.Map;

public interface NobilityService {
    /**
     * 获取骑士以及相关信息
     * @return
     */
    List<CavallierInfo> getCavalierInfo(Long userId);

    /**
     * 根据userId和goodsId获取骑士的价格及优惠
     * @param userId
     * @param goodsId
     * @return
     */
    DiscountPriceInfo getMyNobilityPrice(String userId, String goodsId);

    /**
     * 获取所有骑士
     * @return
     */
    List<GoodsInfo> getAllCavalier(Long goodsId);

    /**
     * 根据用户id获取用户开通的相应权限
     * @param userId
     * @return
     */
    RoleInfo getRoleInfoByUidAndGid(Long userId,Long goodsId);

    /**
     * 根据用户id和骑士id获取对应的优惠券信息
     * @param userId
     * @param goodsId
     * @return
     */
    CouponInfo getCouponInfoByUidAndGid(Long userId, Long goodsId);

    /**
     * 根据骑士id获取骑士所有的优惠信息
     * @param goodsId
     * @return
     */
    List<DiscountInfo> getAllDiscount(Long goodsId);

    /**
     * 生成订单
     * @param request
     * @return
     */
    ResponseResult<Map<String, Object>> insertMyNobilityOrder(HttpServletRequest request);

    /**
     * 获取用户开通贵族有效时间
     */
    List getUserNobleTimeEnd(long userId);
}
