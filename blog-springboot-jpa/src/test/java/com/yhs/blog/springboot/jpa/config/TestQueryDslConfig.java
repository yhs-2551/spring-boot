package com.yhs.blog.springboot.jpa.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import com.querydsl.jpa.impl.JPAQueryFactory;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

// JPA 테스트 시 QueryDsl 설정. QueryDSL을 직접적으로 사용하지 않는 Repository라도 없으면 오류 발생
@TestConfiguration
public class TestQueryDslConfig {

    @PersistenceContext
    private EntityManager entityManager;

    @Bean
    public JPAQueryFactory jpaQueryFactory() {
        return new JPAQueryFactory(entityManager);
    }

}
