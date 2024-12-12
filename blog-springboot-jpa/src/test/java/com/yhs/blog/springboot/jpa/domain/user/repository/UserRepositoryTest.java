package com.yhs.blog.springboot.jpa.domain.user.repository;

import com.yhs.blog.springboot.jpa.domain.user.entity.User;
import com.yhs.blog.springboot.jpa.domain.user.factory.TestUserFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest // @transactional 포함
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
class UserRepositoryTest {

    @Autowired
    UserRepository userRepository;

    @Nested
    @DisplayName("User Repository 테스트")
    class JPACRTest {

        @Test
        @DisplayName("신규 사용자를 저장한다.")
        void saveUser() {
            User user = TestUserFactory.createTestUser();
            User savedUser = userRepository.save(user);

            assertAll(
                    () -> assertNotNull(savedUser),
                    () -> assertNotNull(savedUser.getId()),
                    () -> assertEquals("testUser", savedUser.getUsername())
            );
        }

        @Test
        @DisplayName("userId로 특정 사용자를 조회한다.")
        void findById() {
            // given
            User savedUser = userRepository.save(TestUserFactory.createTestUser());

            // when
            Optional<User> foundUser = userRepository.findById(savedUser.getId());

            // then
            assertThat(foundUser)
                    .isPresent()
                    .get()
                    .satisfies(user -> { // assertThat 사용시, satisfies() 사용해서 하나씩 검증
                        assertThat(user.getId()).isEqualTo(savedUser.getId());
                        assertThat(user.getEmail()).isEqualTo("test@example.com");
                    });
        }


        @Test
        @DisplayName("이메일값으로 사용자를 조회한다.")
        void findByEmail() {
            // given
            userRepository.save(TestUserFactory.createTestUser());

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
            // given
            userRepository.save(TestUserFactory.createTestUser());

            // when
            boolean exists = userRepository.existsByBlogId("testBlogId");

            // then
            assertThat(exists).isTrue();
        }

        @Test
        @DisplayName("Email 값으로 특정 사용자의 존재 여부를 확인한다.")
        void existsByEmail() {
            // given
            userRepository.save(TestUserFactory.createTestUser());

            // when
            boolean exists = userRepository.existsByEmail("test@example.com");

            // then
            assertThat(exists).isTrue();
        }

        @Test
        @DisplayName("username 값으로 특정 사용자의 블로그 아이디 존재 여부를 확인한다.")
        void existsByUsername() {
            // given
            userRepository.save(TestUserFactory.createTestUser());

            // when
            boolean exists = userRepository.existsByUsername("testUser");

            // then
            assertThat(exists).isTrue();
        }

    }
}