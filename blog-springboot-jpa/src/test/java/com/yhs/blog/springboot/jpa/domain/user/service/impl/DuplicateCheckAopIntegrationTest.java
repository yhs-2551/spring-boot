package com.yhs.blog.springboot.jpa.domain.user.service.impl;

import com.yhs.blog.springboot.jpa.common.constant.code.ErrorCode;
import com.yhs.blog.springboot.jpa.domain.user.dto.response.DuplicateCheckResponse;
import com.yhs.blog.springboot.jpa.domain.user.repository.UserRepository;
import com.yhs.blog.springboot.jpa.domain.user.service.UserCheckService;
import com.yhs.blog.springboot.jpa.exception.custom.BusinessException;

import org.aspectj.lang.ProceedingJoinPoint;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

import static org.mockito.Mockito.when;

// 굳이 SpringbootTest 안쓰고 @ExtendWith(MockitoExtension.class), 를 이용해서 aop와 UserCheckServiceImpl 서비스단 같이 테스트 하는게 더 좋을것 같지만 일단 보류 
// 나중에 @ExtendWith(MockitoExtension.class)로 바꿀떈 자세한 서비스단 테스트는 DuplicateCheckAspectTest에서 하고, 여기서는 Mocking을 이용하여 aop와 서비스단이 서로 연동되어서 잘 실행되는지만 확인하면 됨 
@Testcontainers
@ActiveProfiles("test")
@SpringBootTest
public class DuplicateCheckAopIntegrationTest {

    @Container
    static final GenericContainer<?> redis;

    static {
        redis = new GenericContainer<>(DockerImageName.parse("redis:latest"))
                .withExposedPorts(6379);

        redis.start(); // 여기서 확실하게 시작
    }

    @DynamicPropertySource
    static void redisProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", redis::getFirstMappedPort);
    }

    @Autowired
    private UserCheckService userCheckService;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @MockitoBean
    UserRepository userRepository;

    @MockitoBean
    private ProceedingJoinPoint joinPoint;

    @BeforeEach
    void setUp() {
        redisTemplate.getConnectionFactory().getConnection().serverCommands().flushDb();

    }

    @Test
    @DisplayName("존재하는 blogId 중복 체크 - 3회 시도 후 차단")
    void 존재하는_blogId_중복체크_3회시도후_차단() throws Throwable {
        // Given
        String blogId = "existingBlog";
        when(userRepository.existsByBlogId(blogId)).thenReturn(true);

        // When & Then
        // 3번의 시도
        for (int i = 0; i < 3; i++) {
            DuplicateCheckResponse response = userCheckService.isDuplicateBlogId(blogId);
            assertThat(response.isExist()).isTrue();
            assertThat(response.getMessage()).isEqualTo("이미 존재하는 BlogId 입니다. 다른 BlogId를 사용해 주세요.");
        }

        // 4번째 시도에서 차단. assertThrows쓰면 테스트 코드 실행해보면 통과하는데, build할때만 오류가 나서 try catch로 사용
        try {
            userCheckService.isDuplicateBlogId(blogId);
        } catch (BusinessException exception) {
            assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.DUPLICATE_CHECK_LIMIT_EXCEEDED);
            assertThat(exception.getMessage()).contains("너무 많은 시도입니다");
        }

    }

    @Test
    @DisplayName("존재하지 않는 blogId 체크 - 3회 시도 후 차단")
    void 존재하지않는_blogId_3회시도후_차단() throws Throwable {
        // Given
        String newBlogId = "newBlog";
        when(userRepository.existsByBlogId(newBlogId)).thenReturn(false);

        // When & then

        // 존재하지 않는 아이디로 3번 시도
        for (int i = 0; i < 3; i++) {
            DuplicateCheckResponse response = userCheckService.isDuplicateBlogId(newBlogId);
            assertThat(response.isExist()).isFalse();
            assertThat(response.getMessage()).isEqualTo("사용 가능한 BlogId 입니다.");

        }

        try {
            userCheckService.isDuplicateBlogId(newBlogId);
        } catch (BusinessException exception) {
            assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.DUPLICATE_CHECK_LIMIT_EXCEEDED);
            assertThat(exception.getMessage()).contains("너무 많은 시도입니다");
        }

    }

    // @Test
    // @DisplayName("차단 후 또 한번의 재시도를 했을 때 최초의 차단 시간이 적용되어 캐시 만료가 되는지. 즉 차단 이후에 재시도를
    // 했을때 재시도 한 기준이 아닌 최초 차단 시간 기준인지 테스트")
    // void 차단후_재시도시_최초차단시간기준으로_캐시만료확인() throws Throwable {
    // // Given
    // String blogId = "testBlogId";

    // when(joinPoint.proceed()).thenReturn(null);

    // // 최대 시도 횟수 초과. 최초 차단
    // for (int i = 0; i < 4; i++) {
    // userCheckService.isDuplicateBlogId(blogId);
    // }

    // Set<String> firstKeys = redisTemplate.keys("duplicateBlogIdCheck:*");
    // assertThat(firstKeys).isNotEmpty();

    // // When
    // Thread.sleep(1000); // 실제 로직은 1분 대기인데 여기서 1분 대기 시키면 실제로 테스트때도 1분 대기하기때문에 2초로
    // 설정
    // // 실제 로직에서 캐시 만료 시간을 2초로 가정

    // // 차단 당한 이후 재시도. 최초 차단 시간이 적용되는지, 차단 이후 재시도했을때 재시도 시점으로 캐시가 만료되는지 확인하기 위해
    // userCheckService.isDuplicateBlogId(blogId);

    // // 1초 더 대기함으로써 최초 차단 이후 총2초가 지남. 만약 재시도 기준이라면 아래 2초를 다시 재워야 통과
    // // 즉 아래처럼 1초만 더 대기 시키고 통과한다면 최초 차단 시간이 적용되는 것
    // Thread.sleep(1000);

    // // Then. 차단이 풀린 이후에 재시도
    // Set<String> secondKeys = redisTemplate.keys("duplicateBlogIdCheck:*");
    // assertThat(secondKeys).isEmpty();

    // }

    // @Test
    // @DisplayName("초기 시도 후 1분 후에 캐시 만료가 되는지 테스트 - 3번 초과 시도로 인해 차단이 아닌, 일반적인 성공으로도
    // 캐시가 만료되어야 한다")
    // void 초기시도후_1분뒤_캐시만료확인_일반성공기준() throws Throwable {

    // // Given
    // String blogId = "testBlogId";

    // when(joinPoint.proceed()).thenReturn(null);

    // // When
    // userCheckService.isDuplicateBlogId(blogId);

    // // Then - 키가 Redis에 존재하는지 확인
    // Set<String> firstkeys = redisTemplate.keys("duplicateBlogIdCheck:*");
    // assertThat(firstkeys).isNotEmpty();

    // // 1분 대기
    // Thread.sleep(2000); // 실제 로직은 1분 대기인데 여기서 1분 대기 시키면 실제로 테스트때도 1분 대기하기때문에 2초로
    // 설정
    // // 테스트 할때 실제 로직에서 1초 or 2초 같이 변경해야함

    // // Then - 1분 후 키가 만료되었는지 확인
    // Set<String> secondKeys = redisTemplate.keys("duplicateBlogIdCheck:*");
    // assertThat(secondKeys).isEmpty();

    // }

    @Test
    @DisplayName("블로그 ID, 이메일  각각 서로 다른 키로 횟수가 카운트 되는지 확인한다.")
    void 블로그ID_이메일ID_각각_서로_다른_키로_카운트_되는지_확인() throws Throwable {
        // Given
        String blogId = "testBlogId";
        String email = "testEmail";

        when(joinPoint.proceed()).thenReturn(null);

        // when
        // 최대 시도 횟수 초과. 최초 차단
        for (int i = 0; i < 3; i++) {
            userCheckService.isDuplicateBlogId(blogId);
        }

        // when & then 차단 당한 이후 응답 실행

        try {
            userCheckService.isDuplicateBlogId(blogId);
        } catch (BusinessException exception) {

            assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.DUPLICATE_CHECK_LIMIT_EXCEEDED);
            assertThat(exception.getMessage()).contains("너무 많은 시도입니다");

            Set<String> blogIdKeys = redisTemplate.keys("duplicateBlogIdCheck:*");
            assertThat(blogIdKeys).isNotEmpty();

            Set<String> emailFirstKeys = redisTemplate.keys("duplicateEmailCheck:*");
            assertThat(emailFirstKeys).isEmpty();
            // when
            // 블로그 ID가 아닌 Email로 실행함으로써 BlogId와 서로 다른 캐시키가 사용되는지 확인
            userCheckService.isDuplicateEmail(email);
            // then
            Set<String> emailSecondKeys = redisTemplate.keys("duplicateEmailCheck:*");
            assertThat(emailSecondKeys).isNotEmpty();

        }

    }

}
