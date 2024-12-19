// package com.yhs.blog.springboot.jpa.domain.user.service.impl;

// import com.yhs.blog.springboot.jpa.domain.user.dto.response.DuplicateCheckResponse;
// import com.yhs.blog.springboot.jpa.domain.user.repository.UserRepository;
// import com.yhs.blog.springboot.jpa.domain.user.service.UserService;
// import org.junit.jupiter.api.BeforeEach;
// import org.junit.jupiter.api.DisplayName;
// import org.junit.jupiter.api.Test;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
// import org.springframework.boot.test.context.SpringBootTest;
// import org.springframework.boot.test.mock.mockito.MockBean;
// import org.springframework.data.redis.core.RedisTemplate;
// import org.springframework.data.redis.core.ValueOperations;
// import org.springframework.test.context.ActiveProfiles;
// import org.springframework.test.context.DynamicPropertyRegistry;
// import org.springframework.test.context.DynamicPropertySource;
// import org.testcontainers.containers.GenericContainer;
// import org.testcontainers.utility.DockerImageName;

// import java.util.Set;

// import static org.assertj.core.api.Assertions.assertThat;
// import static org.mockito.Mockito.when;

// @AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
// @ActiveProfiles("test")
// @SpringBootTest
// public class UserServiceAopIntegrationTest {

//     static final GenericContainer<?> redis;

//     static {
//         redis = new GenericContainer<>(DockerImageName.parse("redis:latest"))
//                 .withExposedPorts(6379);
//         redis.start();  // 여기서 확실하게 시작
//     }

//     @DynamicPropertySource
//     static void redisProperties(DynamicPropertyRegistry registry) {
//         registry.add("spring.redis.host", redis::getHost);
//         registry.add("spring.redis.port", redis::getFirstMappedPort);
//     }


//     @Autowired
//     private UserService userService;

//     @Autowired
//     private RedisTemplate<String, String> redisTemplate;

//     @MockBean
//     UserRepository userRepository;

//     @MockBean
//     private ValueOperations<String, String> valueOperations;

//     @BeforeEach
//     void setUp() {
//         redisTemplate.getConnectionFactory().getConnection().flushDb();
//     }

//     @Test
//     @DisplayName("존재하는 blogId 중복 체크 - 3회 시도 후 차단")
//     void 존재하는_blogId_중복체크_3회시도후_차단() {
//         // Given
//         String blogId = "existingBlog";
//         when(userRepository.existsByBlogId(blogId)).thenReturn(true);

//         // When & Then
//         // 3번의 시도
//         for (int i = 0; i < 3; i++) {
//             DuplicateCheckResponse response = userService.existsByBlogId(blogId);
//             assertThat(response.isExist()).isTrue();
//             assertThat(response.getMessage()).isEqualTo("이미 존재하는 BlogId 입니다. 다른 BlogId를 사용해 주세요.");
//             assertThat(response.isLimited()).isFalse();
//         }

//         // 4번째 시도에서 차단
//         DuplicateCheckResponse blockedResponse = userService.existsByBlogId(blogId);
//         assertThat(blockedResponse.isLimited()).isTrue();
//         assertThat(blockedResponse.getMessage()).contains("너무 많은 시도입니다");
//     }

//     @Test
//     @DisplayName("존재하지 않는 blogId 체크 - 3회 시도 후 차단")
//     void 존재하지않는_blogId_3회시도후_차단() {
//         // Given
//         String newBlogId = "newBlog";
//         when(userRepository.existsByBlogId(newBlogId)).thenReturn(false);

//         // When


//         // 존재하지 않는 아이디로 3번 시도
//         for (int i = 0; i < 3; i++) {
//             DuplicateCheckResponse response = userService.existsByBlogId(newBlogId);
//             assertThat(response.isExist()).isFalse();
//             assertThat(response.getMessage()).isEqualTo("사용 가능한 BlogId 입니다.");
//             assertThat(response.isLimited()).isFalse();
//         }

//         // 한번 더 시도
//         DuplicateCheckResponse response = userService.existsByBlogId(newBlogId);

//         // Then
//         assertThat(response.isExist()).isFalse();
//         assertThat(response.getMessage()).isEqualTo("너무 많은 시도입니다. 1분 후 다시 시도해주세요.");
//         assertThat(response.isLimited()).isTrue();

//     }

//     @Test
//     @DisplayName("차단 후 또 한번의 재시도를 했을 때 최초의 차단 시간이 적용되어 캐시 만료가 되는지. 즉 차단 이후에 재시도를 했을때 재시도 한 기준이 아닌 최초 차단 시간 기준인지 테스트")
//     void 차단후_재시도시_최초차단시간기준으로_캐시만료확인() throws InterruptedException {
//         // Given
//         String blogId = "existingBlog";

//         when(userRepository.existsByBlogId(blogId)).thenReturn(true);

//         // 최대 시도 횟수 초과. 최초 차단
//         for (int i = 0; i < 4; i++) {
//             userService.existsByBlogId(blogId);
//         }

//         Set<String> firstKeys = redisTemplate.keys("duplicateBlogIdCheck:*");
//         assertThat(firstKeys).isNotEmpty();

//         // When
//         Thread.sleep(1000); // 실제 로직은 1분 대기인데 여기서 1분 대기 시키면 실제로 테스트때도 1분 대기하기때문에 2초로 설정
//         // 실제 로직에서 캐시 만료 시간을 2초로 가정


//         // 차단 당한 이후 재시도. 최초 차단 시간이 적용되는지, 차단 이후 재시도했을때 재시도 시점으로 캐시가 만료되는지 확인하기 위해
//         userService.existsByBlogId(blogId);

//         // 1초 더 대기함으로써 최초 차단 이후 총2초가 지남. 만약 재시도 기준이라면 아래 2초를 다시 재워야 통과
//         // 즉 아래처럼 1초만 더 대기 시키고 통과한다면 최초 차단 시간이 적용되는 것
//         Thread.sleep(1000);

//         // Then
//         Set<String> secondKeys = redisTemplate.keys("duplicateBlogIdCheck:*");
//         assertThat(secondKeys).isEmpty();

//         DuplicateCheckResponse response = userService.existsByBlogId(blogId);
//         assertThat(response.isLimited()).isFalse();
//         assertThat(response.isExist()).isTrue();

//     }

//     @Test
//     @DisplayName("초기 시도 후 1분 후에 캐시 만료가 되는지 테스트 - 3번 초과 시도로 인해 차단이 아닌, 일반적인 성공으로도 캐시가 만료되어야 한다")
//     void 초기시도후_1분뒤_캐시만료확인_일반성공기준() throws InterruptedException {


//         // Given
//         String blogId = "existingBlog";

//         when(userRepository.existsByBlogId(blogId)).thenReturn(true);

//         // When
//         userService.existsByBlogId(blogId);

//         // Then - 키가 Redis에 존재하는지 확인
//         Set<String> firstkeys = redisTemplate.keys("duplicateBlogIdCheck:*");
//         assertThat(firstkeys).isNotEmpty();


//         // 1분 대기
//         Thread.sleep(2000); // 실제 로직은 1분 대기인데 여기서 1분 대기 시키면 실제로 테스트때도 1분 대기하기때문에 2초로 설정
//         // 테스트 할때 실제 로직에서 1초 or 2초 같이 변경해야함

//         // Then - 1분 후 키가 만료되었는지 확인
//         Set<String> secondKeys = redisTemplate.keys("duplicateBlogIdCheck:*");
//         assertThat(secondKeys).isEmpty();

//     }


//     @Test
//     @DisplayName("블로그 ID, 이메일 ID 각각 서로 다른 키로 횟수가 카운트 되는지 확인한다.")
//     void 블로그ID_이메일ID_각각_서로_다른_키로_카운트_되는지_확인() throws InterruptedException {
//         // Given
//         String blogId = "existingBlog";
//         String email = "existingEmail";

//         when(userRepository.existsByBlogId(blogId)).thenReturn(true);
//         when(userRepository.existsByEmail(email)).thenReturn(true);

//         // when
//         // 최대 시도 횟수 초과. 최초 차단
//         for (int i = 0; i < 4; i++) {
//             userService.existsByBlogId(blogId);
//         }

//         // 차단 당한 이후 응답 실행
//         DuplicateCheckResponse responseBlogId = userService.existsByBlogId(blogId);

//         // then
//         assertThat(responseBlogId.isLimited()).isTrue();

//         // when
//         // 블로그 ID가 아닌 Email로 실행함으로써 BlogId와 서로 다른 캐시키가 사용되는지 확인
//         DuplicateCheckResponse responseEmail = userService.existsByEmail(email);

//         // then
//         assertThat(responseEmail.isLimited()).isFalse();
//     }



// }
