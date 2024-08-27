package com.yhs.blog.springboot.jpa.service.impl;

import com.yhs.blog.springboot.jpa.dto.AddUserRequest;
import com.yhs.blog.springboot.jpa.entity.User;
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
        String encodedPassword =
                bCryptPasswordEncoder.encode(addUserRequest.getPassword());

        User user = User.builder()
                .email(addUserRequest.getEmail())
                .password(encodedPassword)
                .username(addUserRequest.getUsername())
                .build();

        return userRepository.save(user).getId();
    }
}
