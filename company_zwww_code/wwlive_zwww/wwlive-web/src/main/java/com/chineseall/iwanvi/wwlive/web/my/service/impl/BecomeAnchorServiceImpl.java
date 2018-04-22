package com.chineseall.iwanvi.wwlive.web.my.service.impl;

import com.chineseall.iwanvi.wwlive.dao.wwlive.BecomeAnchorMapper;
import com.chineseall.iwanvi.wwlive.domain.wwlive.BecomeAnchor;
import com.chineseall.iwanvi.wwlive.web.my.service.BecomeAnchorService;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class BecomeAnchorServiceImpl implements BecomeAnchorService {

    private Logger logger = Logger.getLogger(this.getClass());

    @Autowired
    private BecomeAnchorMapper becomeAnchorMapper;

    public void insertBecomeAnchor(BecomeAnchor become,String paths) {
        becomeAnchorMapper.insertBecomeAnchor(become);
        if(StringUtils.isNotEmpty(paths)) {
            String[] split = paths.split(",");
            Long userId = become.getUserId();
            for (String imgUrl : split) {
                becomeAnchorMapper.insertAnchorImg(imgUrl,userId, become.getBecomeId());
            }
        }
    }
}
