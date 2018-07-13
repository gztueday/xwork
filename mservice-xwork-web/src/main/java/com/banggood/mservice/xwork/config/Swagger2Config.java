package com.banggood.mservice.xwork.config;

import io.swagger.annotations.Api;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

/**
 * swagger的配置对象
 */
@Configuration
@EnableSwagger2
public class Swagger2Config    {

    /**
     * swagger2的配置对象
     * 把一个配置好的docket,(swagger2的配置对象) 交给springboot来管理
     * http://localhost:8082/v2/api-docs
     *
     * @return
     */
    @Bean
    public Docket apiConnfig() {
        return new Docket(DocumentationType.SWAGGER_2).select()
                //apis需要暴露给swagger的
                .apis(RequestHandlerSelectors.withClassAnnotation(Api.class)).build();
    }

}
