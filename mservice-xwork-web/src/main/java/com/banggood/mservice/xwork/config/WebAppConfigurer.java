package com.banggood.mservice.xwork.config;

import com.banggood.mservice.xwork.interceptor.InterfaceOptions;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

/**
 * Created by zouyi on 2018/1/16.
 */
@Configuration
public class WebAppConfigurer extends WebMvcConfigurerAdapter {
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 多个拦截器组成一个拦截器链
        // addPathPatterns 用于添加拦截规则
        // excludePathPatterns 用户排除拦截
        registry.addInterceptor(new InterfaceOptions()).addPathPatterns("/**");
        super.addInterceptors(registry);
    }
}
