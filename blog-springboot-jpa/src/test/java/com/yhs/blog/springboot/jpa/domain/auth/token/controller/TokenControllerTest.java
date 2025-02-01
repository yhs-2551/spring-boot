package com.yhs.blog.springboot.jpa.domain.auth.token.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yhs.blog.springboot.jpa.domain.auth.token.provider.TokenProvider;
import com.yhs.blog.springboot.jpa.domain.user.entity.User;
import com.yhs.blog.springboot.jpa.domain.user.factory.TestUserFactory;
import com.yhs.blog.springboot.jpa.domain.user.repository.UserRepository;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import java.time.Duration;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.containsString;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@ActiveProfiles("test")
@Transactional
public class TokenControllerTest {

        @Container
        private static final GenericContainer<?> redis;

        static {
                redis = new GenericContainer<>(DockerImageName.parse("redis:latest"))
                                .withExposedPorts(6379);
                redis.start(); // 여기서 확실하게 시작
        }

        @DynamicPropertySource
        private static void redisProperties(DynamicPropertyRegistry registry) {
                registry.add("spring.data.redis.host", redis::getHost);
                registry.add("spring.data.redis.port", redis::getFirstMappedPort);
        }

        @Autowired
        protected MockMvc mockMvc;

        @Autowired
        protected ObjectMapper objectMapper;

        // @Autowired
        // private WebApplicationContext context;

        @Autowired
        private RedisTemplate<String, String> redisTemplate;

        @Autowired
        private TokenProvider tokenProvider;

        @Autowired
        private UserRepository userRepository;

        private static User savedUser;

        @BeforeEach
        public void setUp() {

                User testUser = TestUserFactory.createTestUser();
                savedUser = userRepository.save(testUser);

        }

        @DisplayName("RefreshToken을 이용해 AccessToken 재발급 테스트")
        @Test
        public void refreshToken을_이용해_AccessToken_재발급_테스트() throws Exception {

                String refreshToken = tokenProvider.generateToken(savedUser, Duration.ofHours(1));
                redisTemplate.opsForValue().set("RT:" + savedUser.getId(), refreshToken);

                Cookie refreshTokenCookie = new Cookie("refresh_token", refreshToken);
                refreshTokenCookie.setHttpOnly(true);
                refreshTokenCookie.setPath("/");

                mockMvc.perform(get("/api/token/new-token").cookie(refreshTokenCookie))
                                .andExpect(status().isOk())
                                .andExpect(header().exists("Authorization"))
                                .andExpect(header().string("Authorization",
                                                containsString("Bearer")))
                                .andExpect(content().string(containsString("새로운 엑세스 토큰이"))); // Hamcrest matcher 사용
        }

}
