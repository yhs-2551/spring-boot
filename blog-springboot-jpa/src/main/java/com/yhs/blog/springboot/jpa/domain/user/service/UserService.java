package com.yhs.blog.springboot.jpa.domain.user.service;

import com.yhs.blog.springboot.jpa.domain.oauth2.dto.request.AdditionalInfoRequest;
import com.yhs.blog.springboot.jpa.domain.user.dto.request.SignUpUserRequest;
import com.yhs.blog.springboot.jpa.domain.user.dto.response.DuplicateCheckResponse;
import com.yhs.blog.springboot.jpa.domain.user.dto.response.RateLimitResponse;
import com.yhs.blog.springboot.jpa.domain.user.dto.response.SignUpUserResponse;
import com.yhs.blog.springboot.jpa.domain.user.entity.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.Optional;

public interface UserService {
    SignUpUserResponse createUser(SignUpUserRequest signUpUserRequest);

    RateLimitResponse createOAuth2User(String email, AdditionalInfoRequest additionalInfoRequest,
                                       HttpServletRequest request, HttpServletResponse response);

    User findUserById(Long userId);

    Optional<User> findUserByEmail(String email);

//    void invalidateUserCache(String userIdentifier);

    boolean isExistsBlogId(String blogId);

    DuplicateCheckResponse isDuplicateBlogId(String blogId);

    DuplicateCheckResponse isDuplicateEmail(String email);

    DuplicateCheckResponse isDuplicateUsername(String username);
}
