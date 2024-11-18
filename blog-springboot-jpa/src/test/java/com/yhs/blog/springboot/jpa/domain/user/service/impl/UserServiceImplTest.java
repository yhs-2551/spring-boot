package com.yhs.blog.springboot.jpa.domain.user.service.impl;

import com.yhs.blog.springboot.jpa.domain.user.entity.User;
import com.yhs.blog.springboot.jpa.domain.user.factory.TestUserFactory;
import com.yhs.blog.springboot.jpa.domain.user.repository.UserRepository;
import com.yhs.blog.springboot.jpa.security.dto.request.SignUpUserRequest;
import com.yhs.blog.springboot.jpa.security.dto.response.SignUpUserResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RedisTemplate<String, Boolean> redisTemplate;

    @Mock
    private ValueOperations<String, Boolean> valueOperations;

    @InjectMocks
    private UserServiceImpl userService;

    @Nested
    @DisplayName("사용자 회원가입(생성) 테스트")
    class CreateUser {

        @Test
        @DisplayName("회원가입시 사용자 생성를 생성")
        void createUserSuccess() {
            // given
            SignUpUserRequest request = new SignUpUserRequest(
                    "testUser",
                    "test@example.com",
                    "123"
            );


            User user = User.builder()
                    .username("testUser")
                    .email("test@example.com")
                    .userIdentifier("test")
                    .build();

            when(userRepository.save(any(User.class))).thenReturn(user);
            // when
            SignUpUserResponse response = userService.createUser(request);


            // then
            assertThat(response).isNotNull();
            assertThat(response.getUsername()).isEqualTo("testUser");
            assertThat(response.getEmail()).isEqualTo("test@example.com");
            assertThat(response.getUserIdentifier()).isEqualTo("test");
            verify(userRepository).save(any(User.class)); // save 메서드가 실제 호출되었는지 검증
        }

    }

    @Nested
    @DisplayName("존재하는 사용자 조회 테스트")
    class FindUser {
        @Test
        @DisplayName("userID로 사용자 조회")
        void findUserById() {
            // given

            User user = TestUserFactory.createTestUser();
            ReflectionTestUtils.setField(user, "id", 1L);


            when(userRepository.findById(1L)).thenReturn(Optional.of(user));

            // when
            User foundUser = userService.findUserById(1L);

            // then
            assertThat(foundUser.getId()).isEqualTo(1L);
        }

        @Test
        @DisplayName("이메일로 사용자 조회")
        void findUserByEmail() {
            // given
            User user = User.builder()
                    .email("test@example.com")
                    .build();
            when(userRepository.findByEmail("test@example.com"))
                    .thenReturn(Optional.of(user));

            // when
            User foundUser = userService.findUserByEmail("test@example.com");

            // then
            assertThat(foundUser.getEmail()).isEqualTo("test@example.com");
        }

    }

    @Nested
    @DisplayName("특정 사용자 식별자 존재 확인 테스트")
    class ExistsUserIdentifier {
        @Test
        @DisplayName("캐시에서 사용자 조회 성공")
        void checkUserIdentifierFromCache() {
            // given
            String userIdentifier = "test";
            when(redisTemplate.opsForValue()).thenReturn(valueOperations);
            when(valueOperations.get("user:" + userIdentifier))
                    .thenReturn(true);

            // when
            boolean exists = userService.existsByUserIdentifier(userIdentifier);

            // then
            assertThat(exists).isTrue();
            // userServiceImpl에서 userRepository가 실행 안되었고, existsByUserIdentifier() 함수가 호출되었는지 검증
            verify(userRepository, never()).existsByUserIdentifier(any());
        }

        @Test
        @DisplayName("DB에서 사용자 조회 후 캐시 저장")
        void checkUserIdentifierFromDB() {
            // given
            String userIdentifier = "test";
            when(redisTemplate.opsForValue()).thenReturn(valueOperations);
            when(valueOperations.get("user:" + userIdentifier))
                    .thenReturn(null);
            when(userRepository.existsByUserIdentifier(userIdentifier))
                    .thenReturn(true);


            // when
            boolean exists = userService.existsByUserIdentifier(userIdentifier);

            // then
            assertThat(exists).isTrue();
            verify(valueOperations).set(
                    eq("user:" + userIdentifier),
                    eq(true)
            );
        }
    }

}
