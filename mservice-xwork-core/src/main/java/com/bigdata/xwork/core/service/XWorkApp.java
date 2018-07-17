package com.bigdata.xwork.core.service;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@MapperScan("com.banggood.xwork.dao.mapper")
@ComponentScan({"com.banggood.xwork", "com.banggood.xwork.api","com.banggood.xwork.scheduler.core.submit"})
public class XWorkApp {

    public static void main(String[] args) {
        SpringApplication.run(XWorkApp.class, args);
    }

}