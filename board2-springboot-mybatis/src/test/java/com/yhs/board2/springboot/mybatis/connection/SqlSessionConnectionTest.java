package com.yhs.board2.springboot.mybatis.connection;


import org.apache.ibatis.session.SqlSessionFactory;
import org.junit.jupiter.api.Test;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;


@SpringBootTest
public class SqlSessionConnectionTest {

    @Autowired
    private SqlSessionTemplate sqlSessionTemplate;

    @Autowired SqlSessionFactory sqlSessionFactory;

    @Test
    void testSqlSessionFactory() {
        assertThat(sqlSessionFactory).isNotNull();
    }

    @Test
    void testSqlSessionTemplate() {
        assertThat(sqlSessionTemplate).isNotNull();
    }

}
