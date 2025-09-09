package org.example.iceapplehome2.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.*;

@Configuration
public class StaticResourceConfig implements WebMvcConfigurer {

    @Value("${app.upload.dir}")
    private String uploadDir;
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // /media/<파일명> 요청을 파일시스템 uploadDir로 매핑
        registry.addResourceHandler("/media/**")
                .addResourceLocations("file:" + uploadDir + "/");

        registry.addResourceHandler("/api/home/media/**")
                .addResourceLocations("file:" + uploadDir + "/");
    }

}