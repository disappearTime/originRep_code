package com.chineseall.iwanvi.wwlive.pc.common.component;

import com.chineseall.iwanvi.wwlive.common.tools.FileMD5Tools;
import org.apache.log4j.Logger;
import org.apache.velocity.tools.generic.DateTool;
import org.apache.velocity.tools.generic.EscapeTool;
import org.apache.velocity.tools.generic.MathTool;
import org.apache.velocity.tools.generic.NumberTool;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;
import org.springframework.web.context.WebApplicationContext;

import java.io.File;

@Component
public class SpringContextUtil implements ApplicationContextAware {

    private static final Logger LOGGER = Logger.getLogger(SpringContextUtil.class);
    // Spring应用上下文环境
    private static ApplicationContext applicationContext;

    public void setApplicationContext(ApplicationContext applicationContext) {
        SpringContextUtil.applicationContext = applicationContext;

        if (applicationContext instanceof WebApplicationContext) {
            WebApplicationContext webApplicationContext = (WebApplicationContext) applicationContext;

            md5File2Application(new File(webApplicationContext.getServletContext().getRealPath("")),webApplicationContext);

            initVelocityTool(webApplicationContext);
        }
    }


    /**
     * velocity添加tool
     * @param context
     */
    private void initVelocityTool(WebApplicationContext context) {

        EscapeTool escapeTool = new EscapeTool();
        DateTool dateTool = new DateTool();
        MathTool mathTool = new MathTool();
        NumberTool numberTool = new NumberTool();

        context.getServletContext().setAttribute("escapeTool", escapeTool);
        context.getServletContext().setAttribute("dateTool", dateTool);
        context.getServletContext().setAttribute("mathTool", mathTool);
        context.getServletContext().setAttribute("numberTool", numberTool);

        LOGGER.info("initVelocityTool() add velocityTool in application attribute key:escapeTool,dateTool,mathTool,numberTool");

    }
    /**
     * 添加静态js和css文件md5进appliction
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


    public static ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    public static Object getBean(String name) throws BeansException {
        Object obj = applicationContext.getBean(name);
        return obj;
    }
}
