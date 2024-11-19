package com.yhs.blog.springboot.jpa.domain.user.controller;

import com.yhs.blog.springboot.jpa.domain.token.entity.RefreshToken;
import com.yhs.blog.springboot.jpa.domain.token.repository.RefreshTokenRepository;
import com.yhs.blog.springboot.jpa.domain.user.entity.User;
import com.yhs.blog.springboot.jpa.domain.user.repository.UserRepository;
import com.yhs.blog.springboot.jpa.security.dto.request.LoginRequest;
import com.yhs.blog.springboot.jpa.security.dto.request.SignUpUserRequest;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.hamcrest.Matchers.startsWith;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@Transactional
class UserIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @LocalServerPort
    private int port;

    @Test
    @DisplayName("회원가입 후 로그인 시 액세스/리프레시 토큰 발급 통합 테스트")
    void signup_login_integration_test() {
        // 1. 회원가입
        SignUpUserRequest signUpRequest = new SignUpUserRequest(
                "testUser",
                "test@example.com",
                "password123"
        );

                RestAssured
                        .given()
                        .port(port)
                        .contentType(ContentType.JSON)
                        .body(signUpRequest)
                        .when()
                        .post("/api/users/signup")
                        .then()
                        .statusCode(201);

        // 2. 로그인
        LoginRequest loginRequest = new LoginRequest(
                "test@example.com",
                "password123"
        );

        RestAssured
                .given()
                .port(port)
                .contentType(ContentType.JSON)
                .body(loginRequest)
                .when()
                .post("/api/users/login")
                .then()
                .statusCode(200)
                .header("Authorization", startsWith("Bearer "))
                .cookie("refresh_token");

        // 3. DB 검증
        User savedUser = userRepository.findByEmail("test@example.com")
                .orElseThrow();
        assertThat(savedUser.getUsername()).isEqualTo("testUser");

        // 4. 리프레시 토큰 DB 저장 검증
        RefreshToken refreshToken = refreshTokenRepository
                .findByUserId(savedUser.getId())
                .orElseThrow();
        assertThat(refreshToken).isNotNull();
    }

    @Test
    @DisplayName("로그아웃 후 재로그인 시 리프레시/액세스 토큰 업데이트 통합 테스트")
    void logout_relogin_refresh_token_update_test() throws InterruptedException {
        // 1. 회원가입
        SignUpUserRequest signUpRequest = new SignUpUserRequest(
                "testUser2",
                "test2@example.com",
                "password123"
        );

        RestAssured
                .given()
                .port(port)
                .contentType(ContentType.JSON)
                .body(signUpRequest)
                .when()
                .post("/api/users/signup")
                .then()
                .statusCode(201);

        // 2. 첫 번째 로그인
        LoginRequest loginRequest = new LoginRequest(
                "test2@example.com",
                "password123"
        );

        Response loginResponse = RestAssured
                .given()
                .port(port)
                .contentType(ContentType.JSON)
                .body(loginRequest)
                .when()
                .post("/api/users/login")
                .then()
                .statusCode(200)
                .extract()
                .response();

        String firstAccessToken = loginResponse.getHeader("Authorization");
        String firstRefreshToken = loginResponse.getCookie("refresh_token");


        // 시간 차이를 위해 잠시 대기해야함. 첫 로그인 -> 로그아웃 -> 재로그인할때 테스트시에는 동일한 시간떄에 재생성됨.
        // 즉 동일한 시간대에 재생성되면 첫 리프레시/액세스 토큰값과 재로그인 후 새롭게 생성되는 리프레시/액세스 토큰 값이 동일한 값으로 생성.
        // -> 원하는 테스트 결과를 얻을 수 없음
        Thread.sleep(1000);

        // 3. 로그아웃
        RestAssured
                .given()
                .port(port)
                .header("Authorization", firstAccessToken)
                .when()
                .post("/api/users/logout")
                .then()
                .statusCode(200);

        // 4. 재로그인
        Response reLoginResponse = RestAssured
                .given()
                .port(port)
                .contentType(ContentType.JSON)
                .body(loginRequest)
                .when()
                .post("/api/users/login")
                .then()
                .statusCode(200)
                .extract()
                .response();

        String newAccessToken = reLoginResponse.getHeader("Authorization");
        String newRefreshToken = reLoginResponse.getCookie("refresh_token");

        // 5. 검증
        User savedUser = userRepository.findByEmail("test2@example.com")
                .orElseThrow();

        RefreshToken updatedRefreshToken = refreshTokenRepository
                .findByUserId(savedUser.getId())
                .orElseThrow();

        // accessToken/refreshToken 값이 변경되었는지 확인
        assertThat(firstAccessToken.substring(7)).isNotEqualTo(newAccessToken.substring(7));
        assertThat(firstRefreshToken).isNotEqualTo(newRefreshToken);
        // DB의 refreshToken이 새로운 값으로 업데이트되었는지 확인
        assertThat(updatedRefreshToken.getRefreshToken()).isEqualTo(newRefreshToken);
    }
}