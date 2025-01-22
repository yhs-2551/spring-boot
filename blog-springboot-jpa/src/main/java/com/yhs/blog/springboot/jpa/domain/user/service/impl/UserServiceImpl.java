package com.yhs.blog.springboot.jpa.domain.user.service.impl;

import com.yhs.blog.springboot.jpa.aop.duplicatecheck.DuplicateCheck;
import com.yhs.blog.springboot.jpa.aop.log.Loggable;
import com.yhs.blog.springboot.jpa.common.constant.code.ErrorCode;
import com.yhs.blog.springboot.jpa.common.constant.token.TokenConstants;
import com.yhs.blog.springboot.jpa.domain.file.service.infrastructure.s3.S3Service;
import com.yhs.blog.springboot.jpa.domain.oauth2.dto.request.AdditionalInfoRequest;
import com.yhs.blog.springboot.jpa.domain.oauth2.dto.request.OAuth2SignUpResponse;
import com.yhs.blog.springboot.jpa.domain.token.jwt.provider.TokenProvider;
import com.yhs.blog.springboot.jpa.domain.user.dto.request.SignUpUserRequest;
import com.yhs.blog.springboot.jpa.domain.user.dto.request.UserSettingsRequest;
import com.yhs.blog.springboot.jpa.domain.user.dto.response.*;
import com.yhs.blog.springboot.jpa.domain.user.entity.User;
import com.yhs.blog.springboot.jpa.domain.user.repository.UserRepository;
import com.yhs.blog.springboot.jpa.domain.user.service.UserService;
import com.yhs.blog.springboot.jpa.exception.custom.BusinessException;
import com.yhs.blog.springboot.jpa.exception.custom.SystemException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import software.amazon.awssdk.services.s3.model.S3Exception;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import com.yhs.blog.springboot.jpa.domain.user.dto.request.LoginRequest;

import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

@Log4j2
@RequiredArgsConstructor
@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RedisTemplate<String, Boolean> redisTemplateBoolean;
    private final RedisTemplate<String, String> redisTemplateString;
 
    private final RedisTemplate<String, UserPublicProfileResponse> userPublicProfileRedisTemplate;
    private final RedisTemplate<String, UserPrivateProfileResponse> userPrivateProfileRedisTemplate;
    private final S3Service s3Service;
    private final TokenProvider tokenProvider;

    private static final long PROFILE_CACHE_HOURS = 24L; // 프론트는 12시간, redis 캐시를 활용해 DB 부하를 감소하기 위해 2배인 24시간으로 설정
    private static final long DUPLICATE_CHECK_CACHE_HOURS = 6L; // 중복확인의 경우 메모리 낭비가 될 수 있기 때문에 6시간으로 설정
 
    // private static final long CACHE_TTL = 24 * 60 * 60; // 1일

    @Override
    @Transactional
    @Loggable
    public SignUpUserResponse createUser(SignUpUserRequest signUpUserRequest) {

        try {
            BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
            User user = User.builder()
                    .blogId(signUpUserRequest.getBlogId())
                    .username(signUpUserRequest.getUsername())
                    .email(signUpUserRequest.getEmail())
                    .password(encoder.encode(signUpUserRequest.getPassword()))
                    // .role(User.UserRole.ADMIN) 일단 기본값인 user로 사용
                    .build();

            User responseUser = userRepository.save(user);
            return new SignUpUserResponse(responseUser.getId(), responseUser.getBlogId(), responseUser.getUsername(),
                    responseUser.getEmail());

        } catch (Exception ex) {
            throw new SystemException(
                    ErrorCode.USER_CREATE_ERROR,
                    "사용자 생성 중 오류가 발생하였습니다. ",
                    "UserServiceImpl",
                    "createUser",
                    ex);
        }
    }

    @Override
    @Transactional
    public RateLimitResponse<OAuth2SignUpResponse> createOAuth2User(String email,
            AdditionalInfoRequest additionalInfoRequest) {

        User user = User.builder()
                .blogId(additionalInfoRequest.getBlogId())
                .username(additionalInfoRequest.getUsername())
                .email(email)
                .build();

        User responseUser = userRepository.save(user); // 영속성 컨텍스트에 등록됨에 따라 user의 pk인 id값이 결정됨

        String rememberMe = redisTemplateString.opsForValue().get("RM:" + email);
        boolean isRememberMe = Boolean.parseBoolean(rememberMe);

        // Redis에 저장된 rememberMe 정보 삭제
        redisTemplateString.delete("RM:" + email);

        String refreshToken = oAuth2NewUserGenerateRefreshToken(email, user,
                isRememberMe);

        String accessToken = oAuth2NewUserGenerateAccessToken(user);

        SignUpUserResponse userInfo = new SignUpUserResponse(responseUser.getId(), responseUser.getBlogId(),
                responseUser.getUsername(),
                responseUser.getEmail());

        OAuth2SignUpResponse oAuth2SignUpResponse = new OAuth2SignUpResponse(userInfo, refreshToken, accessToken,
                isRememberMe);

        return new RateLimitResponse<OAuth2SignUpResponse>(true, oAuth2SignUpResponse);

    }

    public LoginResultToken getTokenForLoginUser(User user, LoginRequest loginRequest) {

        // 리프레시 토큰 생성
        String refreshToken;

        if (loginRequest.getRememberMe()) {
            refreshToken = tokenProvider.generateToken(user,
                    TokenConstants.REMEMBER_ME_REFRESH_TOKEN_DURATION);
            redisTemplateString.opsForValue().set(TokenConstants.RT_PREFIX + user.getId(),
                    refreshToken,
                    TokenConstants.REMEMBER_ME_REFRESH_TOKEN_TTL, TimeUnit.SECONDS);

        } else {
            refreshToken = tokenProvider.generateToken(user, TokenConstants.REFRESH_TOKEN_DURATION);

            redisTemplateString.opsForValue().set(TokenConstants.RT_PREFIX + user.getId(),
                    refreshToken,
                    TokenConstants.REFRESH_TOKEN_TTL, TimeUnit.SECONDS);

        }
        // Access Token 생성
        String accessToken = tokenProvider.generateToken(user, TokenConstants.ACCESS_TOKEN_DURATION);
        return new LoginResultToken(refreshToken, accessToken);

    }

    @Loggable
    @Override
    @Transactional(readOnly = true)
    public User findUserById(Long userId) {
        return userRepository.findById(userId).orElseThrow(() -> new BusinessException(
                ErrorCode.USER_NOT_FOUND,
                userId + "를 가지고 있는 사용자를 찾지 못하였습니다.",
                "UserServiceImpl",
                "findUserById"));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> findUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    // 글생성시 필요, redis를 사용할까 했지만 redis에 json 변환시 여러 문제, redis에 저장되면 영속성 detached 상태로 변경되는 문제 등 때문에 사용x
    @Override
    @Transactional(readOnly = true)
    public User findUserByBlogId(String blogId) {

        // If not in cache, get from database
        Optional<User> optionalUser = userRepository.findByBlogId(blogId);
        if (optionalUser.isEmpty()) {
            throw new BusinessException(
                    ErrorCode.USER_NOT_FOUND,
                    blogId + "를 가지고 있는 사용자를 찾지 못하였습니다.",
                    "UserServiceImpl",
                    "findUserByBlogId");
        }

        return optionalUser.get();

    }

    @Loggable
    @Override
    @Transactional(readOnly = true)
    public UserPublicProfileResponse findUserByBlogIdAndConvertDTO(String blogId) {
        String cacheKey = "userPublicProfile:" + blogId;

        // Try to get user from cache first
        UserPublicProfileResponse cachedUser = userPublicProfileRedisTemplate.opsForValue().get(cacheKey);
        if (cachedUser != null) {
            return cachedUser;
        }

        // If not in cache, get from database
        Optional<User> optionalUser = userRepository.findByBlogId(blogId);
        if (optionalUser.isEmpty()) {
            throw new BusinessException(
                    ErrorCode.USER_NOT_FOUND,
                    blogId + "를 가지고 있는 사용자를 찾지 못하였습니다.",
                    "UserServiceImpl",
                    "findUserByBlogIdAndConvertDTO");
        }

        User user = optionalUser.get();

        UserPublicProfileResponse userPublicProfileResponseDTO = new UserPublicProfileResponse(user.getId(),
                user.getBlogId(),
                user.getBlogName(),
                user.getUsername(), user.getProfileImageUrl());

        // blogId는 사용자가 프론트측에서 계정 정보를 변경하지 않는 한 유지된다.
        // 따라서 그때 캐시 무효화를 할 수 있지만, 만약 사용자가 탈퇴를 하거나, 또 다른 의도치 않은 상황을 대비해 만료 시간 명시적으로 설정
        userPublicProfileRedisTemplate.opsForValue().set(cacheKey, userPublicProfileResponseDTO, PROFILE_CACHE_HOURS,
                TimeUnit.HOURS);

        return userPublicProfileResponseDTO;
    }

    @Loggable
    @Transactional(readOnly = true)
    @Override
    public UserPrivateProfileResponse findUserByTokenAndByBlogId(String blogId) { // email 민감한 정보는 개인정보 보호를 위해 캐시 삭제

        Optional<User> optionalUser = userRepository.findByBlogId(blogId);
        if (optionalUser.isEmpty()) {
            throw new BusinessException(
                    ErrorCode.USER_NOT_FOUND,
                    blogId + "를 가지고 있는 사용자를 찾지 못하였습니다.",
                    "UserServiceImpl",
                    "findUserByTokenAndByBlogId");
        }

        User findUser = optionalUser.get();

        UserPrivateProfileResponse userPrivateProfileResponseDTO = new UserPrivateProfileResponse(findUser.getEmail(),
                findUser.getBlogId(), findUser.getBlogName(),
                findUser.getUsername(), findUser.getProfileImageUrl());

        return userPrivateProfileResponseDTO;
    }

    @Loggable
    @Override
    @Transactional
    public void updateUserSettings(String blogId, UserSettingsRequest userSettingsRequest) throws IOException {

        User user = userRepository.findByBlogId(blogId).orElseThrow(() -> new BusinessException(
                ErrorCode.USER_NOT_FOUND,
                blogId + "를 가지고 있는 사용자를 찾지 못하였습니다.",
                "UserServiceImpl",
                "updateUserSettings"));

        String oldUsername = user.getUsername();

        try {
            if (userSettingsRequest.profileImage() != null && !userSettingsRequest.profileImage().isEmpty()) {
                String awsS3FileUrl = s3Service.uploadProfileImage(userSettingsRequest.profileImage(), blogId);

                user.profileUpdate(userSettingsRequest.username(), userSettingsRequest.blogName(),
                        awsS3FileUrl);
            } else {

                s3Service.deleteProfileImage(blogId);

                user.profileUpdate(userSettingsRequest.username(), userSettingsRequest.blogName(),
                        "https://iceamericano-blog-storage.s3.ap-northeast-2.amazonaws.com/default/default-avatar-profile.webp");
            }

            userRepository.save(user);

            TransactionSynchronizationManager.registerSynchronization(
                    new TransactionSynchronization() {
                        @Override
                        public void afterCommit() {
                            // 사용자 정보 변경 시 캐시 무효화. 트랜잭션이 성공적으로 커밋되어야만 redis 캐시 무효화
                            userPublicProfileRedisTemplate.delete("userPublicProfile:" + blogId);
                            userPrivateProfileRedisTemplate.delete("userPrivateProfile:" + blogId);
                            redisTemplateBoolean.delete("isDuplicateUsername:" + oldUsername);
                        }

                    });
        } catch (S3Exception e) {
            throw new SystemException(
                    ErrorCode.USER_PROFILE_UPDATE_ERROR,
                    "프로필 업데이트 중 오류가 발생하였습니다.",
                    "UserServiceImpl",
                    "updateUserSettings", e);
        }

    }

    @Override
    @Transactional(readOnly = true)
    public boolean isExistsBlogId(String blogId) {

        String cacheKey = "isExists:" + blogId;

        Boolean exists = redisTemplateBoolean.opsForValue().get(cacheKey);
        if (exists != null) {
            return exists;
        }

        boolean userExists = userRepository.existsByBlogId(blogId);

        if (userExists) {
            // 캐시에 저장.
            redisTemplateBoolean.opsForValue().set(cacheKey, true, PROFILE_CACHE_HOURS, TimeUnit.HOURS);
        }

        return false;
    }

    // 아래는 회원가입 시 중복확인 관련
    @Override
    @Loggable
    @DuplicateCheck(type = "BlogId")
    @Transactional(readOnly = true)
    public DuplicateCheckResponse isDuplicateBlogId(String blogId) {

        String cacheKey = "isDuplicateBlogId:" + blogId;
        return checkDuplicate(cacheKey, () -> userRepository.existsByBlogId(blogId), "이미 존재하는 " +
                "BlogId 입니다. 다른 BlogId를 사용해 주세요.", "사용 가능한 BlogId 입니다.");
    }

    @Override
    @Loggable
    @DuplicateCheck(type = "Email")
    @Transactional(readOnly = true)
    public DuplicateCheckResponse isDuplicateEmail(String email) {
        String cacheKey = "isDuplicateEmail:" + email;
        return checkDuplicate(cacheKey, () -> userRepository.existsByEmail(email), "이미 존재하는 이메일 " +
                "입니다. 다른 이메일을 사용해 주세요.", "사용 가능한 이메일 입니다.");

    }

    @Override
    @Loggable
    @DuplicateCheck(type = "Username")
    @Transactional(readOnly = true)
    public DuplicateCheckResponse isDuplicateUsername(String username) {
        String cacheKey = "isDuplicateUsername:" + username;
        return checkDuplicate(cacheKey, () -> userRepository.existsByUsername(username), "이미 존재하는" +
                " 사용자명 입니다. 다른 사용자명을 사용해 주세요.", "사용 가능한 사용자명 입니다.");
    }

    private DuplicateCheckResponse checkDuplicate(String cacheKey, Supplier<Boolean> dbCheck, String existMessage,
            String notExistMessage) {
        // boolean 기본 타입은 null값을 가질 수 없기 때문에 null 비교 하려면 래퍼 클래스 사용필요.
        Boolean exists = redisTemplateBoolean.opsForValue().get(cacheKey);
        if (exists != null) {
            // 캐시 조회 성공
            return new DuplicateCheckResponse(true, existMessage);
        }

        boolean isExists = dbCheck.get();
        if (isExists) {
            // DB 조회 성공
            // 캐시에 저장. 일단 무한대. 사용자 계정 변경 및 계정 탈퇴 시 무효화 필요요
            redisTemplateBoolean.opsForValue().set(cacheKey, true, DUPLICATE_CHECK_CACHE_HOURS, TimeUnit.HOURS);
            return new DuplicateCheckResponse(true, existMessage);
        }

        return new DuplicateCheckResponse(false, notExistMessage);
    }

    private String oAuth2NewUserGenerateRefreshToken(String email, User user, boolean isRememberMe) {

        // 아래는 OAuth2 신규 사용자 토큰 발급 로직
        // 리프레시 토큰 발급 및 Redis에 저장
        String refreshToken;
        if (isRememberMe) {
            refreshToken = tokenProvider.generateToken(user, TokenConstants.REMEMBER_ME_REFRESH_TOKEN_DURATION);
            redisTemplateString.opsForValue().set(TokenConstants.RT_PREFIX + email, refreshToken,
                    TokenConstants.REMEMBER_ME_REFRESH_TOKEN_TTL, TimeUnit.SECONDS);
        } else {
            refreshToken = tokenProvider.generateToken(user, TokenConstants.REFRESH_TOKEN_DURATION);
            redisTemplateString.opsForValue().set(TokenConstants.RT_PREFIX + email, refreshToken,
                    TokenConstants.REFRESH_TOKEN_TTL, TimeUnit.SECONDS);

        }

        return refreshToken;
    }

    private String oAuth2NewUserGenerateAccessToken(User user) {

        // Access Token 생성
        String accessToken = tokenProvider.generateToken(user, TokenConstants.ACCESS_TOKEN_DURATION);
        return accessToken;

    }

}
