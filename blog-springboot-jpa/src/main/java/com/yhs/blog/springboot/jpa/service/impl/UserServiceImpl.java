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
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    @Override
    public Long createUser(AddUserRequest addUserRequest) {

        try {
            String encodedPassword =
                    bCryptPasswordEncoder.encode(addUserRequest.getPassword());

            User user = User.builder()
                    .username(addUserRequest.getUsername())
                    .email(addUserRequest.getEmail())
                    .password(encodedPassword)
                    .role(User.UserRole.ADMIN)
                    .build();

            return userRepository.save(user).getId();

        } catch (Exception ex) {
            throw new UserCreationException("사용자 생성 중 오류가 발생했습니다: " + ex.getMessage());
        }
    }
}
