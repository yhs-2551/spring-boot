package com.yhs.blog.springboot.jpa.domain.user.service.impl;

import com.yhs.blog.springboot.jpa.domain.user.dto.response.DuplicateCheckResponse;
import com.yhs.blog.springboot.jpa.domain.user.repository.UserRepository;
import com.yhs.blog.springboot.jpa.domain.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@SpringBootTest
public class UserServiceAopIntegrationTest {

    static final GenericContainer<?> redis;

    static {
        redis = new GenericContainer<>(DockerImageName.parse("redis:latest"))
                .withExposedPorts(6379);
        redis.start();  // 여기서 확실하게 시작
    }

    @DynamicPropertySource
    static void redisProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.redis.host", redis::getHost);
        registry.add("spring.redis.port", redis::getFirstMappedPort);
    }


    @Autowired
    private UserService userService;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @MockBean
    UserRepository userRepository;

    @MockBean
    private ValueOperations<String, String> valueOperations;

    @BeforeEach
    void setUp() {
        redisTemplate.getConnectionFactory().getConnection().flushDb();
    }

    @Test
    @DisplayName("존재하는 blogId 중복 체크 - 3회 시도 후 차단")
    void whenExistingBlogId_AfterThreeAttempts_ThenBlocked() {
        // Given
        String blogId = "existingBlog";
        when(userRepository.existsByBlogId(blogId)).thenReturn(true);

        // When & Then
        // 3번의 시도
        for (int i = 0; i < 3; i++) {
            DuplicateCheckResponse response = userService.existsByBlogId(blogId);
            assertThat(response.isExist()).isTrue();
            assertThat(response.getMessage()).isEqualTo("이미 존재하는 BlogId 입니다.");
            assertThat(response.isLimited()).isFalse();
        }

        // 4번째 시도에서 차단
        DuplicateCheckResponse blockedResponse = userService.existsByBlogId(blogId);
        assertThat(blockedResponse.isLimited()).isTrue();
        assertThat(blockedResponse.getMessage()).contains("너무 많은 시도입니다");
    }

    @Test
    @DisplayName("존재하지 않는 blogId 체크 - 3회 시도 후 차단")
    void whenNonExistingBlogId_ThenResetAttempts() {
        // Given
        String newBlogId = "newBlog";
        when(userRepository.existsByBlogId(newBlogId)).thenReturn(false);

        // When


        // 존재하지 않는 아이디로 3번 시도
        for (int i = 0; i < 3; i++) {
            DuplicateCheckResponse response = userService.existsByBlogId(newBlogId);
            assertThat(response.isExist()).isFalse();
            assertThat(response.getMessage()).isEqualTo("사용 가능한 BlogId 입니다.");
            assertThat(response.isLimited()).isFalse();
        }

        // 한번 더 시도
        DuplicateCheckResponse response = userService.existsByBlogId(newBlogId);

        // Then
        assertThat(response.isExist()).isFalse();
        assertThat(response.getMessage()).isEqualTo("너무 많은 시도입니다. 1분 후 다시 시도해주세요.");
        assertThat(response.isLimited()).isTrue();

    }

    @Test
    @DisplayName("차단 후 1분 후에 재시도 가능 및 캐시 만료가 되는지")
    void whenBlockedAfterMaxAttempts_AndWaitOneMinute_ThenCanRetryAgain_AndIsCacheInvalidate() throws InterruptedException {
        // Given
        String blogId = "existingBlog";

        when(userRepository.existsByBlogId(blogId)).thenReturn(true);

        // 최대 시도 횟수 초과
        for (int i = 0; i < 4; i++) {
            userService.existsByBlogId(blogId);
        }

        Set<String> firstKeys = redisTemplate.keys("duplicateCheck:*");
        assertThat(firstKeys).isNotEmpty();

        // When
        Thread.sleep(2000); // 실제 로직은 1분 대기인데 여기서 1분 대기 시키면 실제로 테스트때도 1분 대기하기때문에 2초로 설정
        // 테스트 할때 실제 로직에서 1초 or 2초 같이 변경해야함

        // Then
        Set<String> secondKeys = redisTemplate.keys("duplicateCheck:*");
        assertThat(secondKeys).isEmpty();

        DuplicateCheckResponse response = userService.existsByBlogId(blogId);
        assertThat(response.isLimited()).isFalse();
        assertThat(response.isExist()).isTrue();

    }

    @Test
    @DisplayName("초기 시도 후 1분 후에 캐시 만료가 되는지 테스트 - 3번 초과 시도로 인해 차단이 아닌, 일반적인 성공으로도 캐시가 만료되어야 한다")
    void whenInitialAttempt_AndWaitOneMinute_ThenCacheExpires() throws InterruptedException {


        // Given
        String blogId = "existingBlog";

        when(userRepository.existsByBlogId(blogId)).thenReturn(true);

        // When
        userService.existsByBlogId(blogId);

        // Then - 키가 Redis에 존재하는지 확인
        Set<String> firstkeys = redisTemplate.keys("duplicateCheck:*");
        assertThat(firstkeys).isNotEmpty();


        // 1분 대기
        Thread.sleep(2000); // 실제 로직은 1분 대기인데 여기서 1분 대기 시키면 실제로 테스트때도 1분 대기하기때문에 2초로 설정
        // 테스트 할때 실제 로직에서 1초 or 2초 같이 변경해야함

        // Then - 1분 후 키가 만료되었는지 확인
        Set<String> secondKeys = redisTemplate.keys("duplicateCheck:*");
        assertThat(secondKeys).isEmpty();

    }


}
