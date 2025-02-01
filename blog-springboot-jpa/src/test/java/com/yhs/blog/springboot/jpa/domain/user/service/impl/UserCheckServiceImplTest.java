package com.yhs.blog.springboot.jpa.domain.user.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import com.yhs.blog.springboot.jpa.common.constant.cache.CacheConstants;
import com.yhs.blog.springboot.jpa.domain.user.dto.response.DuplicateCheckResponse;
import com.yhs.blog.springboot.jpa.domain.user.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class UserCheckServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RedisTemplate<String, Boolean> redisTemplate;

    @Mock
    private ValueOperations<String, Boolean> valueOperations;

    @InjectMocks
    private UserCheckServiceImpl userCheckService;

    @BeforeEach
    void setUp() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Nested
    @DisplayName("isExistsBlogId 메서드 테스트")
    class IsExistsBlogIdTest {

        @Test
        @DisplayName("캐시에 존재하는 경우 캐시값 반환")
        void blogId값이_캐시에_존재하는_경우_캐시값_반환() {
            // given
            String blogId = "testBlog";
            String cacheKey = "isExists:" + blogId;
            when(valueOperations.get(cacheKey)).thenReturn(true);

            // when
            boolean result = userCheckService.isExistsBlogId(blogId);

            // then
            assertThat(result).isTrue();
            verify(userRepository, never()).existsByBlogId(blogId);
        }

        @Test
        @DisplayName("캐시 미스 + DB에 존재하는 경우")
        void blogId값이_캐시에_존재하지_않고_DB에_존재하는_경우_DB값_반환() {
            // given
            String blogId = "testBlog";
            String cacheKey = "isExists:" + blogId;
            when(valueOperations.get(cacheKey)).thenReturn(null);
            when(userRepository.existsByBlogId(blogId)).thenReturn(true);

            // when
            boolean result = userCheckService.isExistsBlogId(blogId);

            // then
            assertThat(result).isTrue();
            verify(valueOperations).set(eq(cacheKey), eq(true),
                    eq(CacheConstants.IS_EXISTS_BLOG_ID_CACHE_HOURS), eq(TimeUnit.HOURS));
        }

        @Test
        @DisplayName("캐시 미스 + DB에 존재하지 않는 경우")
        void blogId값이_캐시_DB에_모두_존재하지_않는_경우_false_반환() {
            // given
            String blogId = "testBlog";
            String cacheKey = "isExists:" + blogId;
            when(valueOperations.get(cacheKey)).thenReturn(null);
            when(userRepository.existsByBlogId(blogId)).thenReturn(false);

            // when
            boolean result = userCheckService.isExistsBlogId(blogId);

            // then
            assertThat(result).isFalse();
            verify(valueOperations, never()).set(anyString(), anyBoolean(), anyLong(), any());
        }
    }

    @Nested
    @DisplayName("isDuplicateBlogId 메서드 테스트")
    class IsDuplicateBlogIdTest {

        @Test
        @DisplayName("캐시에 존재하는 경우")
        void blogId값이_캐시에_존재하는_경우_중복된_blogId() {
            // given
            String blogId = "testBlog";
            String cacheKey = "isDuplicateBlogId:" + blogId;
            when(valueOperations.get(cacheKey)).thenReturn(true);

            // when
            DuplicateCheckResponse result = userCheckService.isDuplicateBlogId(blogId);

            // then
            assertThat(result.isExist()).isTrue();
            assertThat(result.getMessage()).isEqualTo("이미 존재하는 BlogId 입니다. 다른 BlogId를 사용해 주세요.");
            verify(userRepository, never()).existsByBlogId(blogId);
        }

        @Test
        @DisplayName("캐시 미스 + DB에 존재하는 경우")
        void blogId값이_DB에_존재하는_경우_중복된_blogId() {
            // given
            String blogId = "testBlog";
            String cacheKey = "isDuplicateBlogId:" + blogId;
            when(valueOperations.get(cacheKey)).thenReturn(null);
            when(userRepository.existsByBlogId(blogId)).thenReturn(true);

            // when
            DuplicateCheckResponse result = userCheckService.isDuplicateBlogId(blogId);

            // then
            assertThat(result.isExist()).isTrue();
            verify(valueOperations).set(eq(cacheKey), eq(true),
                    eq(CacheConstants.DUPLICATE_CHECK_CACHE_HOURS), eq(TimeUnit.HOURS));
        }

        @Test
        @DisplayName("캐시 미스 + DB에 존재하지 않는 경우")
        void blogId값이_캐시_DB_모두에_존재하지_않는_경우_사용가능한_blogId() {
            // given
            String blogId = "testBlog";
            String cacheKey = "isDuplicateBlogId:" + blogId;
            when(valueOperations.get(cacheKey)).thenReturn(null);
            when(userRepository.existsByBlogId(blogId)).thenReturn(false);

            // when
            DuplicateCheckResponse result = userCheckService.isDuplicateBlogId(blogId);

            // then
            assertThat(result.isExist()).isFalse();
            assertThat(result.getMessage()).isEqualTo("사용 가능한 BlogId 입니다.");
        }
    }

    @Nested
    @DisplayName("isDuplicateEmail 메서드 테스트")
    class IsDuplicateEmailTest {

        @Test
        @DisplayName("캐시에 존재하는 경우")
        void email값이_캐시에_존재하는_경우_중복된_email() {
            // given
            String email = "test@example.com";
            String cacheKey = "isDuplicateEmail:" + email;
            when(valueOperations.get(cacheKey)).thenReturn(true);

            // when
            DuplicateCheckResponse result = userCheckService.isDuplicateEmail(email);

            // then
            assertThat(result.isExist()).isTrue();
            assertThat(result.getMessage()).isEqualTo("이미 존재하는 이메일 입니다. 다른 이메일을 사용해 주세요.");
            verify(userRepository, never()).existsByEmail(email);
        }

        @Test
        @DisplayName("캐시 미스 + DB에 존재하는 경우")
        void email값이_DB에_존재하는_경우_중복된_email() {
            // given
            String email = "test@example.com";
            String cacheKey = "isDuplicateEmail:" + email;
            when(valueOperations.get(cacheKey)).thenReturn(null);
            when(userRepository.existsByEmail(email)).thenReturn(true);

            // when
            DuplicateCheckResponse result = userCheckService.isDuplicateEmail(email);

            // then
            assertThat(result.isExist()).isTrue();
            verify(valueOperations).set(eq(cacheKey), eq(true),
                    eq(CacheConstants.DUPLICATE_CHECK_CACHE_HOURS), eq(TimeUnit.HOURS));
        }

        @Test
        @DisplayName("캐시 미스 + DB에 존재하지 않는 경우")
        void email값이_캐시_DB에_존재하지_않는_경우_사용가능한_email() {
            // given
            String email = "test@example.com";
            String cacheKey = "isDuplicateEmail:" + email;
            when(valueOperations.get(cacheKey)).thenReturn(null);
            when(userRepository.existsByEmail(email)).thenReturn(false);

            // when
            DuplicateCheckResponse result = userCheckService.isDuplicateEmail(email);

            // then
            assertThat(result.isExist()).isFalse();
            assertThat(result.getMessage()).isEqualTo("사용 가능한 이메일 입니다.");
        }
    }

    @Nested
    @DisplayName("isDuplicateUsername 메서드 테스트")
    class IsDuplicateUsernameTest {

        @Test
        @DisplayName("캐시에 존재하는 경우")
        void username값이_캐시에_존재하는_경우_중복된_username() {
            // given
            String username = "testUser";
            String cacheKey = "isDuplicateUsername:" + username;
            when(valueOperations.get(cacheKey)).thenReturn(true);

            // when
            DuplicateCheckResponse result = userCheckService.isDuplicateUsername(username);

            // then
            assertThat(result.isExist()).isTrue();
            assertThat(result.getMessage()).isEqualTo("이미 존재하는 사용자명 입니다. 다른 사용자명을 사용해 주세요.");
            verify(userRepository, never()).existsByUsername(username);
        }

        @Test
        @DisplayName("캐시 미스 + DB에 존재하는 경우")
        void username값이_DB에_존재하는_경우_중복된_username() {
            // given
            String username = "testUser";
            String cacheKey = "isDuplicateUsername:" + username;
            when(valueOperations.get(cacheKey)).thenReturn(null);
            when(userRepository.existsByUsername(username)).thenReturn(true);

            // when
            DuplicateCheckResponse result = userCheckService.isDuplicateUsername(username);

            // then
            assertThat(result.isExist()).isTrue();
            verify(valueOperations).set(eq(cacheKey), eq(true),
                    eq(CacheConstants.DUPLICATE_CHECK_CACHE_HOURS), eq(TimeUnit.HOURS));
        }

        @Test
        @DisplayName("캐시 미스 + DB에 존재하지 않는 경우")
        void username값이_캐시_DB에_존재하지_않는_경우_사용가능한_username() {
            // given
            String username = "testUser";
            String cacheKey = "isDuplicateUsername:" + username;
            when(valueOperations.get(cacheKey)).thenReturn(null);
            when(userRepository.existsByUsername(username)).thenReturn(false);

            // when
            DuplicateCheckResponse result = userCheckService.isDuplicateUsername(username);

            // then
            assertThat(result.isExist()).isFalse();
            assertThat(result.getMessage()).isEqualTo("사용 가능한 사용자명 입니다.");
        }
    }
}