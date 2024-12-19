// package com.yhs.blog.springboot.jpa.security.jwt.config.jwt;

// import com.yhs.blog.springboot.jpa.domain.user.entity.User;
// import com.yhs.blog.springboot.jpa.domain.user.repository.UserRepository;
// import com.yhs.blog.springboot.jpa.domain.token.jwt.config.JwtProperties;
// import com.yhs.blog.springboot.jpa.security.jwt.config.jwt.factory.JwtFactory;
// import com.yhs.blog.springboot.jpa.domain.token.jwt.provider.TokenProvider;
// import io.jsonwebtoken.Jwts;
// import org.junit.jupiter.api.DisplayName;
// import org.junit.jupiter.api.Test;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.boot.test.context.SpringBootTest;
// import org.springframework.security.core.Authentication;
// import org.springframework.security.core.userdetails.UserDetails;
// import org.springframework.transaction.annotation.Transactional;

// import java.time.Duration;
// import java.util.Date;
// import java.util.Map;

// import static org.assertj.core.api.Assertions.assertThat;

// @SpringBootTest
// public class TokenProviderTest {

//     @Autowired
//     private TokenProvider tokenProvider;

//     @Autowired
//     private UserRepository userRepository;

//     @Autowired
//     private JwtProperties jwtProperties;


//     // generateToken 검증 테스트
//     @DisplayName("generateToken(): 유저 정보와 만료 기간을 전달해 토큰을 만들 수 있다.")
//     @Test
//     @Transactional
//     void generateToken() {

// //        given
//         User testUser =
//                 userRepository.save(User.builder().username("test").email("user@gmail.com").password(
//                         "test").build());

// //        when
//         String token = tokenProvider.generateToken(testUser, Duration.ofDays(14));

// //        then
//         Long userId =
//                 Jwts.parser().verifyWith(jwtProperties.getSecretKey()).build().parseSignedClaims(token).getPayload().get(
//                         "id", Long.class);

//         assertThat(userId).isEqualTo(testUser.getId());
//     }

//     //    validToken() 검증 테스트
//     @DisplayName("invalidToken(): 만료된 토큰인 때에 유효성 검증에 실패한다.")
//     @Test
//     void validToken_invalidToken() {

// //        given 상황
//         // Duration.ofDays(7)을 뺌으로써 현재 시간보다 7일 전을 나타낸다. 즉, 토큰은 무조건 만료된 상태이다
//         String token =
//                 JwtFactory.builder().expiration(new Date(new Date().getTime() - Duration.ofDays(7).toMillis())).build().createToken(jwtProperties);

// //        when given에 대한 실행을 했을때
//         boolean result = tokenProvider.validToken(token);

// //        then 실제 검증
//         assertThat(result).isFalse();
//     }

//     @DisplayName("validToken(): 유효한 토큰인 때에 유효성 검증에 성공한다.")
//     @Test
//     void validToken_validToken() {
// //        given
//         String token = JwtFactory.withDefaultValues().createToken(jwtProperties);

// //    when
//         boolean result = tokenProvider.validToken(token);
//         assertThat(result).isTrue();

//     }

//     @DisplayName("getAuthentication(): 토큰 기반으로 인증 정보를 가져올 수 있다.")
//     @Test
//     void getAuthentication() {

// //        givn, 유저이메일을 기반으로 jWT 토큰 생성
//         String userEmail = "user@email.com";
//         String token = JwtFactory.builder().subject(userEmail).build().createToken(jwtProperties);

// //        when, token값을 기반으로 사용자에 대한 인증 정보를 가져옴
//         Authentication authentication = tokenProvider.getAuthentication(token);

// //        then
//         assertThat(((UserDetails) authentication.getPrincipal()).getUsername()).isEqualTo(userEmail);
//     }

//     @DisplayName("getUserId(): 토큰으로 유저 ID를 가져올 수 있다.")
//     @Test
//     void getUserId() {

// //        given
//         Long userId = 1L;
//         String token =
//                 JwtFactory.builder().claims(Map.of("id", userId)).build().createToken(jwtProperties);

// //        when
//         Long userIdByToken = tokenProvider.getUserId(token);

// //        then
//         assertThat(userIdByToken).isEqualTo(userId);
//     }
// }
