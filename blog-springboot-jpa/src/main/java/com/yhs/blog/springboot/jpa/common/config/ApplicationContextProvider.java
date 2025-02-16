package com.yhs.blog.springboot.jpa.common.config;

import java.util.Arrays;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.env.Environment;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

@Component // 빈으로 등록하지 않으면 ApplicationContextAware 인터페이스의 setApplicationContext 메서드가 자동으로
           // 호출되지 않음
public class ApplicationContextProvider implements ApplicationContextAware {

    private static ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(@NonNull ApplicationContext ctx) throws BeansException {
        applicationContext = ctx;
    }

    private static ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    public static boolean isProd() {
        Environment env = getApplicationContext().getEnvironment();
        return Arrays.asList(env.getActiveProfiles()).contains("prod");
    }

}
