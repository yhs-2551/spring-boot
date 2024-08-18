package com.yhs.board2.springboot.mybatis.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource("classpath:log4jdbc.log4j2.properties")
@MapperScan("com.yhs.board2.springboot.mybatis.mapper")
public class RootConfig {
}
