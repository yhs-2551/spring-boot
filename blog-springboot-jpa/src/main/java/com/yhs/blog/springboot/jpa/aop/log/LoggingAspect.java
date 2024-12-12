package com.yhs.blog.springboot.jpa.aop.log;


import com.yhs.blog.springboot.jpa.domain.post.dto.request.PostRequest;
import lombok.extern.log4j.Log4j2;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Log4j2
public class LoggingAspect {

    @Before("execution(* com.yhs.blog.springboot.jpa.domain.post.service.PostService.createPost(..)) && args(postDTO)")
    public void logBeforeCreatePost(PostRequest postRequest) {
        log.info("Post Content >>>> " + postRequest.getContent());
    }
}
