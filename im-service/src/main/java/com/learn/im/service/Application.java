package com.learn.im.service;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 逻辑层
 */
@SpringBootApplication(scanBasePackages = {"com.learn.im.service", "com.learn.im.common"})
@MapperScan("com.learn.im.service.*.dao.mapper")
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

}




