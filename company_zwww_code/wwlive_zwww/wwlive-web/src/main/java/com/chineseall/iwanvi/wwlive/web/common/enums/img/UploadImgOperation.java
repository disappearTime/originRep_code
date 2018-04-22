package com.chineseall.iwanvi.wwlive.web.common.enums.img;

import java.awt.Image;

import org.springframework.web.multipart.MultipartFile;


public interface UploadImgOperation {
	
	public String getPath();

	public boolean isAppropriateAspectRatio(Image src);
	
	public String saveCropPhoto(MultipartFile file, String imgPath, String suffix) throws Exception;

//	public String getReturnName(String uploadFileName, String suffix);

}
