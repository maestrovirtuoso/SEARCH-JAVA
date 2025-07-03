package com.company.search.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.AsyncSupportConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class AsyncMvcConfig implements WebMvcConfigurer {

    @Override
    public void configureAsyncSupport(AsyncSupportConfigurer configurer) {
        // Set a longer timeout for async requests (5 minutes)
        configurer.setDefaultTimeout(5 * 60 * 1000L);
        
        // Register a custom CallableProcessingInterceptor if needed
        // configurer.registerCallableInterceptors(...);
        
        // Register a custom DeferredResultProcessingInterceptor if needed
        // configurer.registerDeferredResultInterceptors(...);
    }
}
