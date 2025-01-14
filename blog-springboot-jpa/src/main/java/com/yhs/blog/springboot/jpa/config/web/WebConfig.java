package com.yhs.blog.springboot.jpa.config.web;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    /**
     * Spring MVC 처리와 관련된 CustomPageableResolver.
     * 이 리졸버는 웹 요청의 커스텀 페이지네이션 설정에 사용된다. MVC와 관련이 있기 때문에 Pathvariable에는 적용이 안되고, RequestParam을 사용할때 적용한다.
     */

    private final CustomPageableResolver customPageableResolver;

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
        argumentResolvers.add(customPageableResolver);
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}