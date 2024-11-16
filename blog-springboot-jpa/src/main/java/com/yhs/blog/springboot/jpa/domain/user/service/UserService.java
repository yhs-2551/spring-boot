package com.yhs.blog.springboot.jpa.domain.user.service;

import com.yhs.blog.springboot.jpa.security.dto.request.SignUpUserRequest;
import com.yhs.blog.springboot.jpa.domain.user.entity.User;

public interface UserService{
    Long createUser(SignUpUserRequest signUpUserRequest);
    User findUserById(Long userId);
    User findUserByEmail(String email);
    boolean existsByUserIdentifier(String userIdentifier);
    void invalidateUserCache(String userIdentifier);
}
