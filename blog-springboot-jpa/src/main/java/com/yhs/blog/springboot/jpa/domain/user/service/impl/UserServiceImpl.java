package com.yhs.blog.springboot.jpa.domain.user.service.impl;

import com.yhs.blog.springboot.jpa.aop.duplicatecheck.DuplicateCheck;
import com.yhs.blog.springboot.jpa.domain.user.dto.response.DuplicateCheckResponse;
import com.yhs.blog.springboot.jpa.domain.user.dto.response.SignUpUserResponse;
import com.yhs.blog.springboot.jpa.domain.token.jwt.provider.TokenProvider;
import com.yhs.blog.springboot.jpa.domain.user.dto.request.SignUpUserRequest;
import com.yhs.blog.springboot.jpa.domain.user.entity.User;
import com.yhs.blog.springboot.jpa.exception.custom.UserCreationException;
import com.yhs.blog.springboot.jpa.domain.user.repository.UserRepository;
import com.yhs.blog.springboot.jpa.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.function.Supplier;

@Log4j2
@RequiredArgsConstructor
@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RedisTemplate<String, Boolean> redisTemplate;
    private final TokenProvider tokenProvider;

//    private static final long CACHE_TTL = 24 * 60 * 60; // 1일




    @Override
    public SignUpUserResponse createUser(SignUpUserRequest signUpUserRequest) {

        try {
            BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
            User user = User.builder()
                    .blogId(signUpUserRequest.getBlogId())
                    .username(signUpUserRequest.getUsername())
                    .email(signUpUserRequest.getEmail())
                    .password(encoder.encode(signUpUserRequest.getPassword()))
//                    .role(User.UserRole.ADMIN) 일단 기본값인 user로 사용
                    .build();

            User reponseUser = userRepository.save(user);
            return new SignUpUserResponse(reponseUser.getId(), reponseUser.getBlogId(), reponseUser.getUsername(), reponseUser.getEmail());


        } catch (Exception ex) {
            throw new UserCreationException("An error occurred while creating the user: " + ex.getMessage());
        }
    }

    @Override
    public User findUserById(Long userId) {
        return userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException(
                "User not found"));
    }

    @Override
    public User findUserByEmail(String email) {
        return userRepository.findByEmail(email).orElseThrow(() -> new IllegalArgumentException(
                "User not found"));
    }


    @Override
    @DuplicateCheck(type = "BlogId")
    public DuplicateCheckResponse existsByBlogId(String blogId) {

        String cacheKey = "userBlogId:" + blogId;
        return checkDuplicate(cacheKey, () -> userRepository.existsByBlogId(blogId), "이미 존재하는 " +
                "BlogId 입니다. 다른 BlogId를 사용해 주세요.", "사용 가능한 BlogId 입니다.");
    }

    @Override
    @DuplicateCheck(type = "Email")
    public DuplicateCheckResponse existsByEmail(String email) {
        String cacheKey = "userEmail:" + email;
        return checkDuplicate(cacheKey, () -> userRepository.existsByEmail(email), "이미 존재하는 이메일 " +
                "입니다. 다른 이메일을 사용해 주세요.", "사용 가능한 이메일 입니다.");

    }

    @Override
    @DuplicateCheck(type = "UserName")
    public DuplicateCheckResponse existsByUserName(String userName) {
        String cacheKey = "userName:" + userName;
        return checkDuplicate(cacheKey, () -> userRepository.existsByUserName(userName), "이미 존재하는" +
                " 사용자명 입니다. 다른 사용자명을 사용해 주세요.", "사용 가능한 사용자명 입니다.");
    }


    private DuplicateCheckResponse checkDuplicate(String cacheKey, Supplier<Boolean> dbCheck, String existMessage, String notExistMessage) {
        // boolean 기본 타입은 null값을 가질 수 없기 때문에 null 비교 하려면 래퍼 클래스 사용필요.
        Boolean exists = redisTemplate.opsForValue().get(cacheKey);
        if (exists != null) {
            // 캐시 조회 성공
            return new DuplicateCheckResponse(true, existMessage, false);
        }

        boolean isExists = dbCheck.get();
        if (isExists) {
            // DB 조회 성공
            // 캐시에 저장. 일단 무한대. 나중에 사용자 계정 변경 및 계정 탈퇴 시 캐시 무효화할 예정
            // 즉 한번 중복확인 체크하면 사용자가 계정 변경 시 사용자명(닉네임), 블로그아이디를 재설정 or 계정 탈퇴 하는거 아닌 이상 항상 같음
            // redisTemplate.opsForValue().set(cacheKey, userExists, CACHE_TTL, TimeUnit.SECONDS);
            redisTemplate.opsForValue().set(cacheKey, true);
            return new DuplicateCheckResponse(true, existMessage, false);
        }

        return new DuplicateCheckResponse(false, notExistMessage, false);
    }

//    @Override
//    public boolean existsByUserIdentifier(String userIdentifier) {
//
//        String cacheKey = "user:" + userIdentifier;
//
//        Boolean exists = redisTemplate.opsForValue().get(cacheKey);
//        if (exists != null) {
//            return exists;
//        }
//
//        boolean userExists = userRepository.existsByUserIdentifier(userIdentifier);
//
//        if (userExists) {
//            // 캐시에 저장. 사용자는 회원탈퇴 하는 경우 아니면 계속 존재하기 때문에, 만료시간을 설정하지 않음. 즉 무한대.
////            redisTemplate.opsForValue().set(cacheKey, userExists, CACHE_TTL, TimeUnit.SECONDS);
//            redisTemplate.opsForValue().set(cacheKey, userExists);
//        }
//
//        return userExists;
//    }

    // 나중에 무효화할때 필요
//    public void invalidateUserCache(String userIdentifier) {
//        redisTemplate.delete("user:" + userIdentifier);
//    }


}
