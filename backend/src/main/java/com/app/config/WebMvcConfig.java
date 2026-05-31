package com.app.config;

import com.app.interceptor.JwtInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;

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
                        "/images/batch-delete"
                )
                .excludePathPatterns(
                        "/auth/login",
                        "/auth/register",
                        "/images/view/**",
                        "/images/thumbnail/**",
                        "/samples/template",
                        "/samples/export"
                );
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String imageLocation = new File(imagePath).toURI().toString();
        String thumbLocation = new File(thumbnailPath).toURI().toString();
        registry.addResourceHandler("/images/**")
                .addResourceLocations(imageLocation);
        registry.addResourceHandler("/thumbnails/**")
                .addResourceLocations(thumbLocation);
        String videoLocation = new File(videoPath).toURI().toString();
        registry.addResourceHandler("/videos/**")
                .addResourceLocations(videoLocation);
    }
}
