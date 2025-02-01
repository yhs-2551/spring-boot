package com.yhs.blog.springboot.jpa.domain.auth.token.provider;

import static org.junit.Assert.assertThat;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.yhs.blog.springboot.jpa.domain.user.entity.User;
import com.yhs.blog.springboot.jpa.domain.user.factory.TestUserFactory;
import com.yhs.blog.springboot.jpa.domain.user.repository.UserRepository;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class TokenProviderTest {

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

    @DisplayName("TokenProvider에서 토큰이 정상적으로 발급되는지 확인")
    @Test
    public void TokenProvider클래스에서_토큰_발급_테스트() throws Exception {

        String accessToken = tokenProvider.generateToken(savedUser, Duration.ofHours(1));

        assertThat(accessToken).isNotNull();
        assertThat(accessToken).isNotEmpty();

    }

}
