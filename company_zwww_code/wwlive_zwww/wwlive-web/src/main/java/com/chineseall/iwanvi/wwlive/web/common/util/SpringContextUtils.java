package com.chineseall.iwanvi.wwlive.web.common.util;

import java.io.File;

import org.apache.log4j.Logger;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;
import org.springframework.web.context.WebApplicationContext;

import com.chineseall.iwanvi.wwlive.common.tools.FileMD5Tools;

@Component
public class SpringContextUtils implements ApplicationContextAware {

    private static final Logger LOGGER = Logger.getLogger(SpringContextUtils.class);
    
	 private static ApplicationContext applicationContext;
	 
	@Override
	public void setApplicationContext(ApplicationContext applicationContext)
			throws BeansException {
		SpringContextUtils.applicationContext = applicationContext;
		if (applicationContext instanceof WebApplicationContext) {
            WebApplicationContext webApplicationContext = (WebApplicationContext) applicationContext;
            md5File2Application(new File(webApplicationContext.getServletContext().getRealPath("")),webApplicationContext);

        }
	}
	/**
     * 获取对象
     * 这里重写了bean方法，起主要作用
     * @param name
     * @return Object 一个以所给名字注册的bean的实例
     * @throws BeansException
     */
    public static Object getBean(String name) throws BeansException {
        return applicationContext.getBean(name);
    }
    
    /**
     * 生成静态js和css等文件md5加密字符串，放入到上下文中
     * @param file
     * @param context
     */
    private void md5File2Application(File file, WebApplicationContext context) {

        if (file.isDirectory()) {
            File[] childFiles = file.listFiles();
            for (File child : childFiles) {
                md5File2Application(child, context);
            }
        } else {
            String contextPath = context.getServletContext().getRealPath("");
            String filePath = file.getAbsolutePath().replace(contextPath, "");
            if (filePath.endsWith(".js") || filePath.endsWith(".css")) {

                String md5 = FileMD5Tools.getMd5ByFile(file);
                // 统一修改成linux文件分隔符
                filePath = filePath.replace(File.separator,"/");
                context.getServletContext().setAttribute(filePath,md5);

                LOGGER.info("md5File2Application() add static file md5 in application attribute,[key:" + filePath + ",value:" +md5 + "]");
            }
        }
    }
}
