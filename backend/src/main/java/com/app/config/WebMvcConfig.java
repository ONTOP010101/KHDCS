package com.app.config;

import com.app.interceptor.JwtInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Autowired
    private JwtInterceptor jwtInterceptor;

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
                        "/images/thumbnail/**"
                );
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/images/**")
                .addResourceLocations("file:D:/photo-management/images/");
        registry.addResourceHandler("/thumbnails/**")
                .addResourceLocations("file:D:/photo-management/thumbnails/");
    }
}
