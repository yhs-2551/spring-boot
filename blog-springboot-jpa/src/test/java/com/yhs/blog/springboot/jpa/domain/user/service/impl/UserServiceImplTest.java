package com.yhs.blog.springboot.jpa.domain.user.service.impl;

import com.yhs.blog.springboot.jpa.domain.user.dto.response.DuplicateCheckResponse;
import com.yhs.blog.springboot.jpa.domain.user.entity.User;
import com.yhs.blog.springboot.jpa.domain.user.factory.TestUserFactory;
import com.yhs.blog.springboot.jpa.domain.user.repository.UserRepository;
import com.yhs.blog.springboot.jpa.domain.user.dto.request.SignUpUserRequest;
import com.yhs.blog.springboot.jpa.domain.user.dto.response.SignUpUserResponse;
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
    @DisplayName("사용자 회원가입 서비스 테스트")
    class CreateUser {

        @Test
        @DisplayName("회원가입시 사용자를 생성")
        void createUserSuccess() {
            // given
            SignUpUserRequest request = new SignUpUserRequest(
                    "testBlogId",
                    "testUser",
                    "test@example.com",
                    "123"
            );


            User user = User.builder()
                    .blogId("testBlogId")
                    .username("testUser")
                    .email("test@example.com")
                    .build();

            ReflectionTestUtils.setField(user, "id", 1L);


            when(userRepository.save(any(User.class))).thenReturn(user);
            // when
            SignUpUserResponse response = userService.createUser(request);


            // then
            assertThat(response).isNotNull();
            assertThat(response.getId()).isEqualTo(1L);
            assertThat(response.getBlogId()).isEqualTo("test");
            assertThat(response.getUserName()).isEqualTo("testUser");
            assertThat(response.getEmail()).isEqualTo("test@example.com");
            verify(userRepository).save(any(User.class)); // save 메서드가 실제 호출되었는지 검증
        }

    }

    @Nested
    @DisplayName("존재하는 사용자 조회 서비스 테스트")
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


    /////// 아래 241126완료

    @Nested
    @DisplayName("Redis를 이용한 특정 사용자 고유 블로그 아이디 존재 확인 서비스 테스트")
    class CheckUserBlogIdForIdentifier {
        @Test
        @DisplayName("캐시에서 특정 사용자 블로그 아이디 조회 성공")
        void checkUserBlogIdFromCache() {
            // given
            String blogId = "testBlog";
            when(redisTemplate.opsForValue()).thenReturn(valueOperations);
            when(valueOperations.get("userBlogId:" + blogId)).thenReturn(true);

            // when
            DuplicateCheckResponse response = userService.existsByBlogId(blogId);

            // then
            assertThat(response.isExist()).isTrue();
            assertThat(response.getMessage()).isEqualTo("이미 존재하는 BlogId 입니다.");
            // userServiceImpl에서 userRepository가 실행 안되었는지 검증
            verify(userRepository, never()).existsByBlogId(anyString());
        }

        @Test
        @DisplayName("DB에서 특정 사용자 블로그 아이디 조회 후 Redis 캐시에 저장")
        void checkUserBlogIdFromDB() {
            // given
            String blogId = "testBlog";
            when(redisTemplate.opsForValue()).thenReturn(valueOperations);
            when(valueOperations.get("userBlogId:" + blogId))
                    .thenReturn(null);
            when(userRepository.existsByBlogId(blogId))
                    .thenReturn(true);


            // when
            DuplicateCheckResponse response = userService.existsByBlogId(blogId);

            // then
            assertThat(response.isExist()).isTrue();
            assertThat(response.getMessage()).isEqualTo("이미 존재하는 BlogId 입니다.");
            verify(valueOperations).set("userBlogId:" + blogId, true);
        }


        @Test
        @DisplayName("Redis 캐시 및 DB에도 사용자가 존재하지 않을때 테스트")
        void checkUserBlogId_WhenNotExists_ReturnsFalse() {
            // given
            String blogId = "testBlog";
            when(redisTemplate.opsForValue()).thenReturn(valueOperations);
            when(valueOperations.get("userBlogId:" + blogId)).thenReturn(null);
            when(userRepository.existsByBlogId(blogId)).thenReturn(false);

            // when
            DuplicateCheckResponse response = userService.existsByBlogId(blogId);

            // then
            assertThat(response.isExist()).isFalse();
            assertThat(response.getMessage()).isEqualTo("사용 가능한 BlogId 입니다.");
            verify(valueOperations, never()).set(anyString(), anyBoolean());
        }

    }

}
