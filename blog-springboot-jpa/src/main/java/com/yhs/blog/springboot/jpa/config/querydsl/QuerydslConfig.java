package com.yhs.blog.springboot.jpa.config.querydsl;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.querydsl.jpa.JPQLTemplates;
import com.querydsl.jpa.impl.JPAQueryFactory;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
 

@Configuration
public class QuerydslConfig {
    
    @PersistenceContext
    private EntityManager entityManager;

    @Bean
    public JPAQueryFactory jpaQueryFactory() {
        return new JPAQueryFactory(JPQLTemplates.DEFAULT, entityManager); // jpa hibernate 6.x이상 부터 JPQLTemplates.DEFAULT를 추가해야함. 안하면 querydsl transform 및 그 외 몇몇 기능 사용이 안됨
    }
}