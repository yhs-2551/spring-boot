package com.yhs.blog.springboot.jpa.domain.user.repository;

import com.yhs.blog.springboot.jpa.config.TestQueryDslConfig;
import com.yhs.blog.springboot.jpa.domain.user.entity.User;
import com.yhs.blog.springboot.jpa.domain.user.factory.TestUserFactory;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest // @transactional 포함
@ActiveProfiles("test")
@Import(TestQueryDslConfig.class) // UserRepository가 QueryDsl을 직접 사용하지 않더라도 스프링 컨텍스트에서 QueryDsl 설정이 필요. 아니면 오류발생.
class UserRepositoryTest {

    @Autowired
    UserRepository userRepository;

    @BeforeEach
    void setUp() {
        userRepository.save(TestUserFactory.createTestUser());
    }

    @Test
    @DisplayName("이메일값으로 사용자를 조회한다.")
    void findByEmail() {

        // when
        Optional<User> foundUser = userRepository.findByEmail("test@example.com");

        // then
        assertThat(foundUser)
                .isPresent()
                .get()
                .satisfies(user -> {
                    assertThat(user.getEmail()).isEqualTo("test@example.com");
                    assertThat(user.getUsername()).isEqualTo("testUser");
                });
    }

    @Test
    @DisplayName("blogId 값으로 특정 사용자의 존재 여부를 확인한다.")
    void existsByBlogId() {

        // when
        boolean exists = userRepository.existsByBlogId("testBlogId");

        // then
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("Email 값으로 특정 사용자의 존재 여부를 확인한다.")
    void existsByEmail() {

        // when
        boolean exists = userRepository.existsByEmail("test@example.com");

        // then
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("username 값으로 특정 사용자의 블로그 아이디 존재 여부를 확인한다.")
    void existsByUsername() {

        // when
        boolean exists = userRepository.existsByUsername("testUser");

        // then
        assertThat(exists).isTrue();
    }

}