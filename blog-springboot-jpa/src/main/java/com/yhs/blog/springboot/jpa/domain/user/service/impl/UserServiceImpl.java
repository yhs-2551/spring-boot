package com.yhs.blog.springboot.jpa.domain.user.service.impl;

import com.yhs.blog.springboot.jpa.aop.duplicatecheck.DuplicateCheck;
import com.yhs.blog.springboot.jpa.aop.ratelimit.RateLimit;
import com.yhs.blog.springboot.jpa.domain.oauth2.dto.request.AdditionalInfoRequest;
import com.yhs.blog.springboot.jpa.domain.token.jwt.provider.TokenProvider;
import com.yhs.blog.springboot.jpa.domain.token.jwt.service.TokenManagementService;
import com.yhs.blog.springboot.jpa.domain.user.dto.request.SignUpUserRequest;
import com.yhs.blog.springboot.jpa.domain.user.dto.response.DuplicateCheckResponse;
import com.yhs.blog.springboot.jpa.domain.user.dto.response.RateLimitResponse;
import com.yhs.blog.springboot.jpa.domain.user.dto.response.SignUpResponseWithHeaders;
import com.yhs.blog.springboot.jpa.domain.user.dto.response.SignUpUserResponse;
import com.yhs.blog.springboot.jpa.domain.user.dto.response.UserProfileResponse;
import com.yhs.blog.springboot.jpa.domain.user.entity.User;
import com.yhs.blog.springboot.jpa.domain.user.repository.UserRepository;
import com.yhs.blog.springboot.jpa.domain.user.service.UserService;
import com.yhs.blog.springboot.jpa.exception.custom.ResourceNotFoundException;
import com.yhs.blog.springboot.jpa.exception.custom.UserCreationException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private final RedisTemplate<String, UserProfileResponse> userProfileRedisTemplate;
    private final TokenProvider tokenProvider;
    private final TokenManagementService tokenManagementService;

    // private static final long CACHE_TTL = 24 * 60 * 60; // 1일

    @Override
    @Transactional
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

            User reponseUser = userRepository.save(user);
            return new SignUpUserResponse(reponseUser.getId(), reponseUser.getBlogId(), reponseUser.getUsername(),
                    reponseUser.getEmail());

        } catch (Exception ex) {
            throw new UserCreationException("An error occurred while creating the user: " + ex.getMessage());
        }
    }

    @Override
    @Transactional
    @RateLimit(key = "OAuth2Signup")
    public RateLimitResponse createOAuth2User(String email, AdditionalInfoRequest additionalInfoRequest,
            HttpServletRequest request, HttpServletResponse response) {

        HttpHeaders headers = new HttpHeaders();

        User user = User.builder()
                .blogId(additionalInfoRequest.getBlogId())
                .username(additionalInfoRequest.getUsername())
                .email(email)
                .build();

        User responseUser = userRepository.save(user); // 영속성 컨텍스트에 등록됨에 따라 user의 pk인 id값이 결정됨

        // 아래는 OAuth2 신규 사용자 토큰 발급 로직
        String rememberMe = redisTemplateString.opsForValue().get("RM:" + email);
        boolean isRememberMe = Boolean.parseBoolean(rememberMe);

        log.debug("OAuth2 User isRememberMe: {}", isRememberMe);

        // 리프레시 토큰 발급 및 Redis에 저장
        String refreshToken;
        if (isRememberMe) {
            refreshToken = tokenProvider.generateToken(user, TokenManagementService.REMEMBER_ME_REFRESH_TOKEN_DURATION);
            redisTemplateString.opsForValue().set(TokenManagementService.RT_PREFIX + email, refreshToken,
                    TokenManagementService.REMEMBER_ME_REFRESH_TOKEN_TTL, TimeUnit.SECONDS);
        } else {
            refreshToken = tokenProvider.generateToken(user, TokenManagementService.REFRESH_TOKEN_DURATION);
            redisTemplateString.opsForValue().set(TokenManagementService.RT_PREFIX + email, refreshToken,
                    TokenManagementService.REFRESH_TOKEN_TTL, TimeUnit.SECONDS);

        }

        // Redis에 저장된 rememberMe 정보 삭제
        redisTemplateString.delete("RM:" + email);

        // 생성된 리프레시 토큰을 클라이언트측 쿠키에 저장 -> 클라이언트에서 액세스 토큰이 만료되면 재발급 요청하기 위함
        tokenManagementService.addRefreshTokenToCookie(request, response, refreshToken, isRememberMe);

        // Access Token 생성
        String accessToken = tokenProvider.generateToken(user, TokenManagementService.ACCESS_TOKEN_DURATION);

        // 응답 헤더에 액세스 토큰 추가
        headers.set("Authorization", "Bearer " + accessToken);

        SignUpUserResponse signUpUserResponse = new SignUpUserResponse(responseUser.getId(), responseUser.getBlogId(),
                responseUser.getUsername(),
                responseUser.getEmail());

        SignUpResponseWithHeaders signUpResponseWithHeaders = new SignUpResponseWithHeaders(signUpUserResponse,
                headers);

        return new RateLimitResponse(true, "OAuth2 신규 사용자 등록에 성공하였습니다.", HttpStatus.CREATED.value(),
                signUpResponseWithHeaders);

    }

    @Override
    @Transactional(readOnly = true)
    public User findUserById(Long userId) {
        return userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException(
                "User not found"));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> findUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    @Transactional(readOnly = true)
    public UserProfileResponse findUserByBlogId(String blogId) {
        String cacheKey = "userProfile:" + blogId;

        // Try to get user from cache first
        UserProfileResponse cachedUser = userProfileRedisTemplate.opsForValue().get(cacheKey);
        if (cachedUser != null) {
            return cachedUser;
        }

        // If not in cache, get from database
        Optional<User> optionalUser = userRepository.findByBlogId(blogId);
        if (optionalUser.isEmpty()) {
            throw new ResourceNotFoundException(blogId + "를 가지고 있는 사용자를 찾지 못하였습니다.");
        }

        User user = optionalUser.get();

        UserProfileResponse userProfileResponseDTO = new UserProfileResponse(user.getBlogId(), user.getUsername());

        // blogId는 사용자가 프론트측에서 계정 정보를 변경하지 않는 한 유지된다.
        // 따라서 그때 캐시 무효화를 할 수 있지만, 만약 사용자가 탈퇴를 하거나, 또 다른 의도치 않은 상황을 대비해 30일로 설정
        userProfileRedisTemplate.opsForValue().set(cacheKey, userProfileResponseDTO, 30, TimeUnit.DAYS);

        return userProfileResponseDTO;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isExistsBlogId(String blogId) {

        String cacheKey = "user:" + blogId;

        Boolean exists = redisTemplateBoolean.opsForValue().get(cacheKey);
        if (exists != null) {
            return exists;
        }

        boolean userExists = userRepository.existsByBlogId(blogId);

        if (userExists) {
            // 캐시에 저장. 사용자는 회원탈퇴 하는 경우 아니면 계속 존재하기 때문에, 만료시간을 설정하지 않음. 즉 무한대.
            // redisTemplate.opsForValue().set(cacheKey, userExists, CACHE_TTL,
            // TimeUnit.SECONDS);
            redisTemplateBoolean.opsForValue().set(cacheKey, true);
        }

        return false;
    }

    @Override
    @DuplicateCheck(type = "BlogId")
    @Transactional(readOnly = true)
    public DuplicateCheckResponse isDuplicateBlogId(String blogId) {

        String cacheKey = "userBlogId:" + blogId;
        return checkDuplicate(cacheKey, () -> userRepository.existsByBlogId(blogId), "이미 존재하는 " +
                "BlogId 입니다. 다른 BlogId를 사용해 주세요.", "사용 가능한 BlogId 입니다.");
    }

    @Override
    @DuplicateCheck(type = "Email")
    @Transactional(readOnly = true)
    public DuplicateCheckResponse isDuplicateEmail(String email) {
        String cacheKey = "userEmail:" + email;
        return checkDuplicate(cacheKey, () -> userRepository.existsByEmail(email), "이미 존재하는 이메일 " +
                "입니다. 다른 이메일을 사용해 주세요.", "사용 가능한 이메일 입니다.");

    }

    @Override
    @DuplicateCheck(type = "Username")
    @Transactional(readOnly = true)
    public DuplicateCheckResponse isDuplicateUsername(String username) {
        String cacheKey = "username:" + username;
        return checkDuplicate(cacheKey, () -> userRepository.existsByUsername(username), "이미 존재하는" +
                " 사용자명 입니다. 다른 사용자명을 사용해 주세요.", "사용 가능한 사용자명 입니다.");
    }

    private DuplicateCheckResponse checkDuplicate(String cacheKey, Supplier<Boolean> dbCheck, String existMessage,
            String notExistMessage) {
        // boolean 기본 타입은 null값을 가질 수 없기 때문에 null 비교 하려면 래퍼 클래스 사용필요.
        Boolean exists = redisTemplateBoolean.opsForValue().get(cacheKey);
        if (exists != null) {
            // 캐시 조회 성공
            return new DuplicateCheckResponse(true, existMessage, false);
        }

        boolean isExists = dbCheck.get();
        if (isExists) {
            // DB 조회 성공
            // 캐시에 저장. 일단 무한대. 나중에 사용자 계정 변경 및 계정 탈퇴 시 캐시 무효화할 예정
            // 즉 한번 중복확인 체크하면 사용자가 계정 변경 시 사용자명(닉네임), 블로그아이디를 재설정 or 계정 탈퇴 하는거 아닌 이상 항상 같음
            // redisTemplate.opsForValue().set(cacheKey, userExists, CACHE_TTL,
            // TimeUnit.SECONDS);
            redisTemplateBoolean.opsForValue().set(cacheKey, true);
            return new DuplicateCheckResponse(true, existMessage, false);
        }

        return new DuplicateCheckResponse(false, notExistMessage, false);
    }

}
