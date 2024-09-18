package com.yhs.blog.springboot.jpa.service.impl;

import com.yhs.blog.springboot.jpa.dto.AddUserRequest;
import com.yhs.blog.springboot.jpa.entity.User;
import com.yhs.blog.springboot.jpa.exception.UserCreationException;
import com.yhs.blog.springboot.jpa.repository.UserRepository;
import com.yhs.blog.springboot.jpa.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;


    @Override
    public Long createUser(AddUserRequest addUserRequest) {

        try {
            BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
            User user = User.builder()
                    .username(addUserRequest.getUsername())
                    .email(addUserRequest.getEmail())
                    .password(encoder.encode(addUserRequest.getPassword()))
//                    .role(User.UserRole.ADMIN) 일단 기본값인 user로 사용
                    .build();

            return userRepository.save(user).getId();

        } catch (Exception ex) {
            throw new UserCreationException("사용자 생성 중 오류가 발생했습니다: " + ex.getMessage());
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
}
