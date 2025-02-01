package com.yhs.blog.springboot.jpa.domain.user.service.impl;

import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.yhs.blog.springboot.jpa.aop.log.Loggable;
import com.yhs.blog.springboot.jpa.common.constant.code.ErrorCode;
import com.yhs.blog.springboot.jpa.common.constant.token.TokenConstants;
import com.yhs.blog.springboot.jpa.common.response.RateLimitResponse;
import com.yhs.blog.springboot.jpa.domain.auth.token.provider.TokenProvider;
import com.yhs.blog.springboot.jpa.domain.oauth2.dto.request.AdditionalInfoRequest;
import com.yhs.blog.springboot.jpa.domain.oauth2.dto.response.OAuth2SignUpResponse;
import com.yhs.blog.springboot.jpa.domain.user.dto.request.SignUpUserRequest;
import com.yhs.blog.springboot.jpa.domain.user.entity.User;
import com.yhs.blog.springboot.jpa.domain.user.repository.UserRepository;
import com.yhs.blog.springboot.jpa.domain.user.service.UserRegistrationService;
import com.yhs.blog.springboot.jpa.exception.custom.SystemException;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Log4j2
@RequiredArgsConstructor
@Service
public class UserRegistrationServiceImpl implements UserRegistrationService {

    private final BCryptPasswordEncoder encoder; // 원래는 createUser 메서드 내 new를 통해 인스턴스를 생성해서 진행했으나, 테스트 코드에서 @Mock로 사용시 호출되기 위해선 의존성 주입을 받아야 함
    private final UserRepository userRepository;
    private final RedisTemplate<String, String> redisTemplate;
    private final TokenProvider tokenProvider;

    @Override
    @Transactional
    @Loggable
    public void createUser(SignUpUserRequest signUpUserRequest) {

        log.info("[UserRegistrationServiceImpl] createUser 메서드 시작");

        try {
            User user = User.builder()
                    .blogId(signUpUserRequest.getBlogId())
                    .username(signUpUserRequest.getUsername())
                    .email(signUpUserRequest.getEmail())
                    .password(encoder.encode(signUpUserRequest.getPassword()))
                    // .role(User.UserRole.ADMIN) 일단 기본값인 user로 사용
                    .build();

            userRepository.save(user);

        } catch (Exception ex) {
            throw new SystemException(
                    ErrorCode.USER_CREATE_ERROR,
                    "사용자 생성 중 오류가 발생하였습니다. ",
                    "UserRegistrationServiceImpl",
                    "createUser",
                    ex);
        }
    }

    @Override
    @Transactional
    public RateLimitResponse<OAuth2SignUpResponse> createOAuth2User(String email,
            AdditionalInfoRequest additionalInfoRequest) {

        log.info("[UserRegistrationServiceImpl] createOAuth2User 메서드 시작");

        User user = User.builder()
                .blogId(additionalInfoRequest.getBlogId())
                .username(additionalInfoRequest.getUsername())
                .email(email)
                .build();

        userRepository.save(user); // 영속성 컨텍스트에 등록됨에 따라 user의 pk인 id값이 결정됨

        String rememberMe = redisTemplate.opsForValue().get("RM:" + email);
        boolean isRememberMe = Boolean.parseBoolean(rememberMe);

        // Redis에 저장된 rememberMe 정보 삭제
        redisTemplate.delete("RM:" + email);

        String refreshToken = oAuth2NewUserGenerateRefreshToken(email, user,
                isRememberMe);

        String accessToken = oAuth2NewUserGenerateAccessToken(user);

        OAuth2SignUpResponse oAuth2SignUpResponse = new OAuth2SignUpResponse(refreshToken, accessToken,
                isRememberMe);

        return new RateLimitResponse<OAuth2SignUpResponse>(true, oAuth2SignUpResponse);

    }

    private String oAuth2NewUserGenerateRefreshToken(String email, User user, boolean isRememberMe) {

        log.info("[UserRegistrationServiceImpl] oAuth2NewUserGenerateRefreshToken 메서드 시작");

        // 아래는 OAuth2 신규 사용자 토큰 발급 로직
        // 리프레시 토큰 발급 및 Redis에 저장
        String refreshToken;
        if (isRememberMe) {
            refreshToken = tokenProvider.generateToken(user,
                    TokenConstants.REMEMBER_ME_REFRESH_TOKEN_DURATION);
            redisTemplate.opsForValue().set(TokenConstants.RT_PREFIX + email, refreshToken,
                    TokenConstants.REMEMBER_ME_REFRESH_TOKEN_TTL, TimeUnit.SECONDS);
        } else {
            refreshToken = tokenProvider.generateToken(user, TokenConstants.REFRESH_TOKEN_DURATION);
            redisTemplate.opsForValue().set(TokenConstants.RT_PREFIX + email, refreshToken,
                    TokenConstants.REFRESH_TOKEN_TTL, TimeUnit.SECONDS);

        }

        return refreshToken;
    }

    private String oAuth2NewUserGenerateAccessToken(User user) {

        log.info("[UserRegistrationServiceImpl] oAuth2NewUserGenerateAccessToken 메서드 시작");

        // Access Token 생성
        String accessToken = tokenProvider.generateToken(user, TokenConstants.ACCESS_TOKEN_DURATION);
        return accessToken;

    }

}
