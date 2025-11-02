package com.swp.project.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class ImageConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Map /images/** URLs to the images folder at project root level (outside src)
        // This overrides the default static resources mapping
        registry.addResourceHandler("/images/**")
                .addResourceLocations("file:images/");
    }
}