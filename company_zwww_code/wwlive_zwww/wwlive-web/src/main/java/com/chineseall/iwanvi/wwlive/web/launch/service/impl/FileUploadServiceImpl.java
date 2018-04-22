package com.chineseall.iwanvi.wwlive.web.launch.service.impl;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;

import com.chineseall.iwanvi.wwlive.web.common.enums.img.UploadImgFormatType;
import com.chineseall.iwanvi.wwlive.web.launch.service.FileUploadService;

@Service
public class FileUploadServiceImpl implements FileUploadService{

    private final Logger logger = Logger
            .getLogger(this.getClass());
    
	/**
	 * 图片路径
	 */
    @Value("${img.path}")
    private String imgPath;

    /**
     * 具体图片路径
     */
    @Value("${img.url}")
    private String imgUrl;
    
	/**
	 * 图片上传
	 * @param request
	 * @return
	 */
    public Map<String, Object> uploadAndSave(HttpServletRequest request) {
		CommonsMultipartResolver multipartResolver = new CommonsMultipartResolver(
                request.getSession().getServletContext());
		Map<String, Object> result = new HashMap<String, Object>();
        if (multipartResolver.isMultipart(request)) {
            MultipartHttpServletRequest multiRequest = (MultipartHttpServletRequest) request;
            MultipartFile file = multiRequest.getFile("imgFile");
            if (file == null) {
        		return result;
            }
            String coverImg = null;
            try {
                Integer uploadType = Integer.parseInt(request.getParameter("uploadType"));
                coverImg = uploadFile2Local(file, uploadType.intValue());// 生成上传文件路径
                if (StringUtils.isBlank(coverImg)) {
            		return result;         
                }
            } catch (Exception e) {
            	logger.error("上传图片失败：", e);
        		return result;         
            }
        	result.put("imgUrl", coverImg);
            return result;
        }
		return result;
	}
	
	
    /**
     * 上传图片到服务器
     * 
     * @param file
     *            上传文件
     * @param uploadType
     *            上传文件类型
     * @return 返回URL地址
     * @throws Exception 
     */
    public String uploadFile2Local(MultipartFile file, int uploadType) throws Exception {
    	UploadImgFormatType type = UploadImgFormatType.getUploadImgFormatType(uploadType);
    	
    	String pFile = type.getPath();//上级文件夹
        
        String fileName = file.getOriginalFilename();// 文件名称
        String suffix = fileName.substring(fileName.lastIndexOf("."));
        
        String tmpFileName = type.saveCropPhoto(file, this.imgPath + pFile, suffix);
        if (StringUtils.isBlank(tmpFileName)) {
        	return "";
        }
        String imgURL = type.getImgURLPrefix(pFile, this.imgUrl) + tmpFileName;//url地址
        
    	return imgURL;
    }

}
