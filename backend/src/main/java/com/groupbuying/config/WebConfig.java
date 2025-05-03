package com.groupbuying.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.beans.factory.annotation.Value;
import java.io.File;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    
    private static final Logger log = LoggerFactory.getLogger(WebConfig.class);
    
    @Value("${file.upload-dir}")
    private String uploadDir;
    
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        try {
            // 确保上传目录存在
            File baseDir = new File(uploadDir);
            if (!baseDir.exists()) {
                boolean created = baseDir.mkdirs();
                if (!created) {
                    log.error("无法创建上传目录：{}", baseDir.getAbsolutePath());
                    throw new RuntimeException("无法创建上传目录");
                }
            }

            // 确保avatars子目录存在
            File avatarsDir = new File(baseDir, "avatars");
            if (!avatarsDir.exists()) {
                boolean created = avatarsDir.mkdirs();
                if (!created) {
                    log.error("无法创建头像目录：{}", avatarsDir.getAbsolutePath());
                    throw new RuntimeException("无法创建头像目录");
                }
            }

            // 获取上传目录的绝对路径
            String absoluteUploadPath = baseDir.getAbsolutePath();
            log.info("上传目录绝对路径: {}", absoluteUploadPath);

            // 配置上传文件的访问路径（使用绝对路径）
            String uploadPath = "file:" + absoluteUploadPath + File.separator;
            log.info("上传文件访问路径: {}", uploadPath);
            registry.addResourceHandler("/uploads/**")
                    .addResourceLocations(uploadPath)
                    .setCachePeriod(3600); // 设置缓存时间为1小时

            // 配置静态资源的访问路径
            registry.addResourceHandler("/public/**")
                    .addResourceLocations("classpath:/public/")
                    .setCachePeriod(3600);

            // 配置默认头像的访问路径
            registry.addResourceHandler("/assets/**")
                    .addResourceLocations("classpath:/public/assets/")
                    .setCachePeriod(3600);
        } catch (Exception e) {
            log.error("配置资源处理器时发生错误", e);
            throw new RuntimeException("配置资源处理器失败", e);
        }
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .exposedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }
} 