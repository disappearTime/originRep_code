package com.chineseall.iwanvi.wwlive.web.common.helper;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

/***
 * http请求文件辅助类
 * 
 * @author DIKEPU
 *
 */
public class HttpFileHelper {
	static final Logger LOGGER = Logger.getLogger(HttpFileHelper.class);

	public static void downLoadFile(HttpServletRequest request,
			HttpServletResponse response, String downLoadPath) {
		// 下载方式：0,从头开始的全文下载；1,从某字节开始的下载（bytes=27000-）；2,从某字节开始到某字节结束的下载（bytes=27000-39000）
		LOGGER.info("开始下载：" + downLoadPath);
		if (StringUtils.isBlank(downLoadPath)) {
			return;
		}
		
		File file = new File(downLoadPath);
		if (file.exists()) {
			//重新设置response
			String fileName = file.getName();
			setHttpResponse(response, fileName);
			
			long p = 0L;
			long contentLength = 0L;
			long fileLength = file.length();
			String rangBytes = "";
			
			// 客户端请求文件块下载开始字节
			String range = request.getHeader("Range");
			
//			request.
			LOGGER.info("range: " + range);
			if (StringUtils.isNotBlank(range) && !"null".equals(range)) {
				response.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT);
				rangBytes = range.replaceAll("bytes=", "");
				if (rangBytes.endsWith("-")) {
					p = Long.parseLong(rangBytes.substring(0,
							rangBytes.indexOf("-")));
					contentLength = fileLength - p; // 客户端请求的是270000之后的字节（包括bytes下标索引为270000的字节）
					readFileByStartingPosition(file, response, p, fileLength, contentLength);
				} else {// bytes=270000-320000
					String[] ranges = rangBytes.split("-");
					p = Long.parseLong(ranges[0]);
					contentLength = Long.parseLong(ranges[1])
							- Long.parseLong(ranges[0]) + 1;
					readFileByRange(file, response, p, range, fileLength, contentLength);
				}
			} else {
				contentLength = fileLength;
				readFileByStartingPosition(file, response, p, fileLength, contentLength);
			}
			
		} else {
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("Error: file " + downLoadPath + " not found.");
			}
		}

	}
	
	/**
	 * 下载响应设置响应头和下载内容类型
	 * @param response
	 * @param fileName
	 */
	private static void setHttpResponse(HttpServletResponse response, String fileName) {
		response.reset();//客户端允许的范围
		response.setHeader("Accept-Ranges", "bytes");
		response.setContentType("application/octet-stream");
		response.addHeader("Content-Disposition",
				"attachment;filename=" + fileName);
	}
	
	/**
	 * 下载方式：0,从头开始的全文下载；2,从某字节开始到某字节结束的下载（bytes=27000-39000）
	 * @param file 文件
	 * @param response 响应
	 * @param p 跳过的字节数
	 * @param fileLength 文件大小
	 * @param contentLength 返回文件内容大小
	 */
	private static void readFileByStartingPosition(File file,
			HttpServletResponse response, long p, long fileLength, long contentLength) {
		// Content-Length:需下载文件块大小
		// 断点开始
		// 响应的格式是: Content-Range, bytes [文件块的开始字节]-[文件的总大小 - 1]/[文件的总大小]
		// get file content
		response.setHeader("Content-Length", contentLength + "");
		String contentRange = new StringBuffer("bytes ")
				.append(new Long(p).toString()).append("-")
				.append(new Long(fileLength - 1).toString())
				.append("/")
				.append(new Long(fileLength).toString()).toString();
		response.setHeader("Content-Range", contentRange);
		try {
			InputStream ins = new FileInputStream(file);
			BufferedInputStream bis = new BufferedInputStream(ins);
			bis.skip(p);
			OutputStream out = response.getOutputStream();
			int n = 0;
			int bsize = 1024;
			byte[] bytes = new byte[bsize];
			while ((n = bis.read(bytes)) != -1) {
				out.write(bytes, 0, n);
			}
			out.flush();
			out.close();
			bis.close();
		} catch (IOException e) {
			e.printStackTrace();
			LOGGER.info("下载时出现异常" + e.getMessage());
		}
	}
	
	/**
	 * 下载方式：1,从某字节开始的下载（bytes=27000-）
	 * @param file 文件
	 * @param response 响应
	 * @param p 跳过的字节数
	 * @param range 当次要下载的范围
	 * @param fileLength 文件大小
	 * @param contentLength 返回文件内容大小
	 */
	private static void readFileByRange(File file,
			HttpServletResponse response, long p, String range, long fileLength, long contentLength) {
		// Content-Length:需下载文件块大小
		// 断点开始
		// 响应的格式是: Content-Range, bytes [文件块的开始字节]-[文件的总大小 - 1]/[文件的总大小]
		// get file content
		response.setHeader("Content-Length", contentLength + "");
		String contentRange = range.replace("=", " ") + "/"
				+ new Long(fileLength).toString();
		response.setHeader("Content-Range", contentRange);

		try {
			InputStream ins = new FileInputStream(file);
			BufferedInputStream bis = new BufferedInputStream(ins);
			bis.skip(p);

			OutputStream out = response.getOutputStream();
			int n = 0;
			long readLength = 0;
			int bsize = 1024;
			byte[] bytes = new byte[bsize];
			// 针对 bytes=27000-39000 的请求，从27000开始写数据
			while (readLength <= contentLength - bsize) {
				n = bis.read(bytes);
				readLength += n;
				out.write(bytes, 0, n);
			}
			if (readLength <= contentLength) {
				n = bis.read(bytes, 0,
						(int) (contentLength - readLength));
				out.write(bytes, 0, n);
			}

			out.flush();
			out.close();
			bis.close();
		} catch (IOException e) {
			LOGGER.info("下载时出现异常" + e.getMessage());
		}
		
	}
	
	
}
