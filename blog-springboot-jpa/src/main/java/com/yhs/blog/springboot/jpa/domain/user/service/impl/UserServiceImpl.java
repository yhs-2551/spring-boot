package com.yhs.blog.springboot.jpa.domain.user.service.impl;

import com.yhs.blog.springboot.jpa.security.dto.response.SignUpUserResponse;
import com.yhs.blog.springboot.jpa.security.jwt.provider.TokenProvider;
import com.yhs.blog.springboot.jpa.security.dto.request.SignUpUserRequest;
import com.yhs.blog.springboot.jpa.domain.user.entity.User;
import com.yhs.blog.springboot.jpa.exception.custom.UserCreationException;
import com.yhs.blog.springboot.jpa.domain.user.repository.UserRepository;
import com.yhs.blog.springboot.jpa.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

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
        String userEmail = signUpUserRequest.getEmail();

        String userIdentifier;

        if (userEmail.contains("@")) {
            userIdentifier = userEmail.substring(0, userEmail.indexOf('@'));
        } else {
            throw new IllegalArgumentException("Invalid email address: " + userEmail);
        }



        try {
            BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
            User user = User.builder()
                    .username(signUpUserRequest.getUsername())
                    .userIdentifier(userIdentifier)
                    .email(signUpUserRequest.getEmail())
                    .password(encoder.encode(signUpUserRequest.getPassword()))
//                    .role(User.UserRole.ADMIN) 일단 기본값인 user로 사용
                    .build();

             User reponseUser = userRepository.save(user);
             return new SignUpUserResponse(reponseUser.getId(), reponseUser.getUsername(),
                     reponseUser.getUserIdentifier(), reponseUser.getEmail());


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
    public boolean existsByUserIdentifier(String userIdentifier) {

        String cacheKey = "user:" + userIdentifier;

        Boolean exists = redisTemplate.opsForValue().get(cacheKey);
        if (exists != null) {
            return exists;
        }

        boolean userExists = userRepository.existsByUserIdentifier(userIdentifier);

        if (userExists) {
            // 캐시에 저장. 사용자는 회원탈퇴 하는 경우 아니면 계속 존재하기 때문에, 만료시간을 설정하지 않음. 즉 무한대.
//            redisTemplate.opsForValue().set(cacheKey, userExists, CACHE_TTL, TimeUnit.SECONDS);
            redisTemplate.opsForValue().set(cacheKey, userExists);
        }

        return userExists;
    }

    // 나중에 무효화할때 필요
    public void invalidateUserCache(String userIdentifier) {
        redisTemplate.delete("user:" + userIdentifier);
    }

}
