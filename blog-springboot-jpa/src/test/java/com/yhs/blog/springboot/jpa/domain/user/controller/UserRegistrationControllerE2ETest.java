
// package com.yhs.blog.springboot.jpa.domain.user.controller;

// import com.yhs.blog.springboot.jpa.domain.user.entity.User;
// import com.yhs.blog.springboot.jpa.domain.user.repository.UserRepository;
// import com.fasterxml.jackson.databind.ObjectMapper;
// import com.yhs.blog.springboot.jpa.BlogSpringbootJpaApplication;
// import com.yhs.blog.springboot.jpa.domain.user.dto.request.LoginRequest;
// import com.yhs.blog.springboot.jpa.domain.user.dto.request.SignUpUserRequest;
// import
// com.yhs.blog.springboot.jpa.domain.user.dto.request.VerifyEmailRequest;

// import io.restassured.RestAssured;
// import io.restassured.config.ObjectMapperConfig;
// import io.restassured.config.RestAssuredConfig;
// import io.restassured.http.ContentType;
// import io.restassured.response.Response;

// import org.junit.jupiter.api.BeforeEach;
// import org.junit.jupiter.api.DisplayName;
// import org.junit.jupiter.api.Test;
// import org.springframework.beans.factory.annotation.Autowired;
// import
// org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
// import org.springframework.boot.test.context.SpringBootTest;
// import org.springframework.boot.test.web.server.LocalServerPort;
// import org.springframework.security.crypto.password.PasswordEncoder;
// import org.springframework.test.context.ActiveProfiles;
// import org.springframework.test.context.DynamicPropertyRegistry;
// import org.springframework.test.context.DynamicPropertySource;
// import org.springframework.transaction.annotation.Transactional;
// import org.testcontainers.containers.GenericContainer;
// import org.testcontainers.utility.DockerImageName;

// import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
// import static org.hamcrest.Matchers.startsWith;

// 이메일 인증코드를 실제로 받아야 하기 때문에 이 부분은 실제 dev.yml파일 및 실제 redis 컨테이너 사용 / 아니면 swagger
// or 프론트를 통해서 진행
// ResetAssured는 E2E테스트라고 봐도 무방. (완전 실제와 같은 테스트)
// ResetAssured 대신 TestRestTemplate를 사용할수도 있지만 전자를 쓰도록 하는게 좋을 듯. 후자는 스프링 부트에서
// 간편하게 쓸 수 있지만 전자를 쓰면 가독성이 좋음
// @SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
// classes = {
// BlogSpringbootJpaApplication.class } // 메인 애플리케이션 클래스 추가. 하지 않으면 오류 발생
// )
// @ActiveProfiles("dev")
// @Transactional
// class UserRegistrationControllerE2ETest {

// @Autowired
// private UserRepository userRepository;

// @Autowired
// private PasswordEncoder passwordEncoder;

// @LocalServerPort
// private int port;

// 현재 이 테스트만 실행 되는데, 실제 이메일로 자꾸 인증코드 발송됨 + 개발 Redis 켜두어야 테스트 통과하기 때문에 주석 처리
// @Test
// @DisplayName("이메일 인증코드 발송 요청 시 Mail Server 연결 실패로 인한 500 에러 발생")
// void 이메일_인증코드_발송_요청_시_메일서버_연결_실패로_인한_500_상태코드_응답() {

// // 1. 회원가입(이메일 인증코드 발송 요청)
// SignUpUserRequest signUpRequest = new SignUpUserRequest(
// "test_blog_id",
// "testUser",
// "test@example.com",
// "Password123*",
// "Password123*");
// RestAssured
// .given()
// .port(port)
// .contentType(ContentType.JSON)
// .body(signUpRequest)
// .when()
// .post("/api/users/signup")
// .then()
// .statusCode(200);
// }

// VerifyEmailRequest verifyEmailRequest = new VerifyEmailRequest();

// RestAssured
// .given()
// .port(port)
// .contentType(ContentType.JSON)
// .body(verifyEmailRequest)
// .when()
// .post("/api/users/verify-code")
// .then()
// .statusCode(200);

// // 2. 로그인
// LoginRequest loginRequest = new LoginRequest(
// "test@example.com",
// "Password123*");

// RestAssured
// .given()
// .port(port)
// .contentType(ContentType.JSON)
// .body(loginRequest)
// .when()
// .post("/api/users/login")
// .then()
// .statusCode(200)
// .header("Authorization", startsWith("Bearer "))
// .cookie("refresh_token");

// // 3. DB 검증
// User savedUser = userRepository.findByEmail("test@example.com")
// .orElseThrow();
// assertThat(savedUser.getUsername()).isEqualTo("testUser");

// 4. 리프레시 토큰 DB 저장 검증
// RefreshToken refreshToken = refreshTokenRepository
// .findByUserId(savedUser.getId())
// .orElseThrow();
// assertThat(refreshToken).isNotNull();

// @Test
// @DisplayName("로그아웃 후 재로그인 시 리프레시/액세스 토큰 업데이트 통합 테스트")
// void logout_relogin_refresh_token_update_test() throws InterruptedException {
// // 1. 회원가입
// SignUpUserRequest signUpRequest = new SignUpUserRequest(
// "testBlogId",
// "testUser",
// "test@example.com",
// "password123",
// "password123");

// RestAssured
// .given()
// .port(port)
// .contentType(ContentType.JSON)
// .body(signUpRequest)
// .when()
// .post("/api/users/signup")
// .then()
// .statusCode(201);

// // 2. 첫 번째 로그인
// LoginRequest loginRequest = new LoginRequest(
// "test2@example.com",
// "password123");

// Response loginResponse = RestAssured
// .given()
// .port(port)
// .contentType(ContentType.JSON)
// .body(loginRequest)
// .when()
// .post("/api/users/login")
// .then()
// .statusCode(200)
// .extract()
// .response();

// String firstAccessToken = loginResponse.getHeader("Authorization");
// String firstRefreshToken = loginResponse.getCookie("refresh_token");

// // 시간 차이를 위해 잠시 대기해야함. 첫 로그인 -> 로그아웃 -> 재로그인할때 테스트시에는 동일한 시간떄에 재생성됨.
// // 즉 동일한 시간대에 재생성되면 첫 리프레시/액세스 토큰값과 재로그인 후 새롭게 생성되는 리프레시/액세스 토큰 값이 동일한 값으로
// 생성.
// // -> 원하는 테스트 결과를 얻을 수 없음
// Thread.sleep(1000);

// // 3. 로그아웃
// RestAssured
// .given()
// .port(port)
// .header("Authorization", firstAccessToken)
// .when()
// .post("/api/users/logout")
// .then()
// .statusCode(200);

// // 4. 재로그인
// Response reLoginResponse = RestAssured
// .given()
// .port(port)
// .contentType(ContentType.JSON)
// .body(loginRequest)
// .when()
// .post("/api/users/login")
// .then()
// .statusCode(200)
// .extract()
// .response();

// String newAccessToken = reLoginResponse.getHeader("Authorization");
// String newRefreshToken = reLoginResponse.getCookie("refresh_token");

// // 5. 검증
// User savedUser = userRepository.findByEmail("test2@example.com")
// .orElseThrow();

// // RefreshToken updatedRefreshToken = refreshTokenRepository
// // .findByUserId(savedUser.getId())
// // .orElseThrow();

// // accessToken/refreshToken 값이 변경되었는지 확인
// assertThat(firstAccessToken.substring(7)).isNotEqualTo(newAccessToken.substring(7));
// assertThat(firstRefreshToken).isNotEqualTo(newRefreshToken);
// // DB의 refreshToken이 새로운 값으로 업데이트되었는지 확인
// //
// assertThat(updatedRefreshToken.getRefreshToken()).isEqualTo(newRefreshToken);
// }
// }