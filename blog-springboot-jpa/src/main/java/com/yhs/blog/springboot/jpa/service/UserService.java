package com.yhs.blog.springboot.jpa.service;

import com.yhs.blog.springboot.jpa.dto.AddUserRequest;
import com.yhs.blog.springboot.jpa.entity.User;
import jakarta.servlet.http.HttpServletRequest;

public interface UserService{
    Long createUser(AddUserRequest addUserRequest);
    User findUserById(Long userId);
    User findUserByEmail(String email);
    boolean existsByUserIdentifier(String userIdentifier);
    void invalidateUserCache(String userIdentifier);
}
