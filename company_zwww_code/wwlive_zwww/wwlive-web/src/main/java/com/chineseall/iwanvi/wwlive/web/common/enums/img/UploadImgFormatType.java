package com.chineseall.iwanvi.wwlive.web.common.enums.img;

import java.awt.Image;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Date;

import javax.imageio.ImageIO;

import org.apache.commons.lang.time.DateFormatUtils;
import org.springframework.web.multipart.MultipartFile;

import com.chineseall.iwanvi.wwlive.common.constants.Constants;
import com.chineseall.iwanvi.wwlive.common.tools.ImageCompressUtils;

public enum UploadImgFormatType implements UploadImgOperation {

	VIDEOIMG(0) {
		
		@Override
		public String getPath() {
			return "video/" + DateFormatUtils.format(new Date(), "yyMM") + Constants.SEPARATOR;
		}

		@Override
		public boolean isAppropriateAspectRatio(Image src) {
			return true;
		}

		@Override
		public String saveCropPhoto(MultipartFile file, String imgPath, String suffix) throws Exception {
			String uploadFileName = Constants.getUUID();
	        String tmpFileName = uploadFileName + "_400" + suffix;
	        String maxFile = imgPath + tmpFileName;
	    	File localFile = getLocalFile(maxFile, file);
			try{
				Image src = ImageIO.read(localFile);
				ImageCompressUtils.saveMinPhoto(src, maxFile, 400, 1.0d);
				ImageCompressUtils.saveMinPhoto(src, imgPath + uploadFileName + "_200" + suffix, 200, 1.0d);
				ImageCompressUtils.saveMinPhoto(src, imgPath + uploadFileName + "_100" + suffix, 100, 1.0d);
			} catch (Exception e) {
				return "";
			}
			return tmpFileName;
		}

	},
	
    HEADIMG(1) {
        
        @Override
        public String getPath() {
            return "anchor/" + DateFormatUtils.format(new Date(), "yyMM") + Constants.SEPARATOR;
        }
        
        @Override
        public boolean isAppropriateAspectRatio(Image src) {
            int srcHeight = src.getHeight(null);
            int srcWidth = src.getWidth(null);
            double imgScale = new BigDecimal(srcWidth).divide(new BigDecimal(srcHeight), 3, BigDecimal.ROUND_HALF_UP
                    ).doubleValue();
            if ( imgScale > 1.05 || imgScale < 0.95) {
                return false;
            }
            return true;
        }
        
        @Override
        public String saveCropPhoto(MultipartFile file, String imgPath, String suffix) throws Exception {
            String uploadFileName = Constants.getUUID();
            String tmpFileName = uploadFileName + "_300" + suffix;
            String maxFile = imgPath + tmpFileName;
            File localFile = getLocalFile(maxFile, file);
            try{
                Image src = ImageIO.read(localFile);
                ImageCompressUtils.saveMinPhoto(src, maxFile, 300, 1.0d);
                ImageCompressUtils.saveMinPhoto(src, imgPath + uploadFileName + "_150" + suffix, 150, 1.0d);
                ImageCompressUtils.saveMinPhoto(src, imgPath + uploadFileName + "_70" + suffix, 70, 1.0d);
            } catch (Exception e) {
                return "";
            }
            return tmpFileName;
        }
        
    },

	/**
	 * 用户头像
	 */
	USERIMG(2) {

		@Override
		public String getPath() {
			return "user/" + DateFormatUtils.format(new Date(), "yyMM") + Constants.SEPARATOR;
		}

		@Override
		public boolean isAppropriateAspectRatio(Image src) {
			int srcHeight = src.getHeight(null);
			int srcWidth = src.getWidth(null);
			double imgScale = new BigDecimal(srcWidth).divide(new BigDecimal(srcHeight), 3, BigDecimal.ROUND_HALF_UP
			).doubleValue();
			if ( imgScale > 1.05 || imgScale < 0.95) {
				return false;
			}
			return true;
		}

		@Override
		public String saveCropPhoto(MultipartFile file, String imgPath, String suffix) throws Exception {
			String uploadFileName = Constants.getUUID();
			String tmpFileName = uploadFileName + "_300" + suffix;
			String maxFile = imgPath + tmpFileName;
			File localFile = getLocalFile(maxFile, file);
			try{
				Image src = ImageIO.read(localFile);
				ImageCompressUtils.saveMinPhoto(src, maxFile, 300, 1.0d);
				ImageCompressUtils.saveMinPhoto(src, imgPath + uploadFileName + "_150" + suffix, 150, 1.0d);
				ImageCompressUtils.saveMinPhoto(src, imgPath + uploadFileName + "_70" + suffix, 70, 1.0d);
			} catch (Exception e) {
				return "";
			}
			return tmpFileName;
		}

	},

	/**
	 * 主播牌面
	 */
	CARD_FACE(3) {

		@Override
		public String getPath() {
			return "cardFace/" + DateFormatUtils.format(new Date(), "yyMM") + Constants.SEPARATOR;
		}

		@Override
		public boolean isAppropriateAspectRatio(Image src) {
			return true;
		}

		@Override
		public String saveCropPhoto(MultipartFile file, String imgPath, String suffix) throws Exception {
			String uploadFileName = Constants.getUUID();
			String tmpFileName = uploadFileName + suffix;
			String maxFile = imgPath + tmpFileName;
			getLocalFile(maxFile, file); // 牌面保存原图
			return tmpFileName;
		}

	};
	
	private int type;

	private UploadImgFormatType(int type) {
		this.type = type;
	}

	public String getImgURLPrefix(String path, String imgUrl) {
		return imgUrl + path;
	}
	
	public File getLocalFile(String path, MultipartFile file) throws IllegalStateException, IOException {
		File localFile = new File(path);
        File pfile = new File(localFile.getParent());
	    if (!pfile.exists()) {
	    	pfile.mkdirs();
	    }
	    try{
	    	file.transferTo(localFile);
		} catch (Exception e) {
			if (localFile.exists()) {
				localFile.delete();
			}
			e.printStackTrace();
		}
		return localFile;
	}
	
	/**
	 * 根据数字获得该上传格式类型
	 * @param 
	 * @return
	 */
	public static UploadImgFormatType getUploadImgFormatType(int uploadType) {
		UploadImgFormatType[] types = UploadImgFormatType.values();
		for (UploadImgFormatType type : types) {
			if (type.type == uploadType) {
				return type;
			}
		}
		return null;
	}
}
