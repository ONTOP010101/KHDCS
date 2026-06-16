package com.app.config;

import com.app.interceptor.JwtInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Autowired
    private JwtInterceptor jwtInterceptor;

    @Value("${app.upload.image-path}")
    private String imagePath;

    @Value("${app.upload.thumbnail-path}")
    private String thumbnailPath;

    @Value("${app.upload.video-path}")
    private String videoPath;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(jwtInterceptor)
                .addPathPatterns(
                        "/users/**",
                        "/roles/**",
                        "/samples/**",
                        "/gallery/**",
                        "/friends/**",
                        "/chat/**",
                        "/logs/**",
                        "/images/upload/**",
                        "/images/gallery/**",
                        "/images/batch-delete",
                        "/report-templates/**"
                )
                .excludePathPatterns(
                        "/auth/login",
                        "/auth/register",
                        "/images/view/**",
                        "/images/thumbnail/**",
                        "/photos/**",
                        "/samples/template",
                        "/samples/export",
                        "/samples/vendor-confirm-report",
                        "/samples/vendor-confirm-session"
                );
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/photos/**")
                .addResourceLocations(new FileSystemResource(imagePath + "/"));
        registry.addResourceHandler("/images/**")
                .addResourceLocations(new FileSystemResource(imagePath + "/"));
        registry.addResourceHandler("/thumbnails/**")
                .addResourceLocations(new FileSystemResource(thumbnailPath + "/"));
        registry.addResourceHandler("/videos/**")
                .addResourceLocations(new FileSystemResource(videoPath + "/"));
    }
}
