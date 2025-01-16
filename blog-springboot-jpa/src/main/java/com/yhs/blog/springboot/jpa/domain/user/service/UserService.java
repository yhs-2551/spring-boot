package com.yhs.blog.springboot.jpa.domain.user.service;

import com.yhs.blog.springboot.jpa.domain.oauth2.dto.request.AdditionalInfoRequest;
import com.yhs.blog.springboot.jpa.domain.oauth2.dto.request.OAuth2SignUpResponse;
import com.yhs.blog.springboot.jpa.domain.user.dto.request.SignUpUserRequest;
import com.yhs.blog.springboot.jpa.domain.user.dto.request.UserSettingsRequest;
import com.yhs.blog.springboot.jpa.domain.user.dto.response.DuplicateCheckResponse;
import com.yhs.blog.springboot.jpa.domain.user.dto.response.LoginResultToken;
import com.yhs.blog.springboot.jpa.domain.user.dto.response.RateLimitResponse;
import com.yhs.blog.springboot.jpa.domain.user.dto.response.SignUpUserResponse;
import com.yhs.blog.springboot.jpa.domain.user.dto.response.UserPrivateProfileResponse;
import com.yhs.blog.springboot.jpa.domain.user.dto.response.UserPublicProfileResponse;
import com.yhs.blog.springboot.jpa.domain.user.entity.User;
import com.yhs.blog.springboot.jpa.domain.user.dto.request.LoginRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Optional;

public interface UserService {
    SignUpUserResponse createUser(SignUpUserRequest signUpUserRequest);

    RateLimitResponse<OAuth2SignUpResponse> createOAuth2User(String email, AdditionalInfoRequest additionalInfoRequest,
            HttpServletRequest request, HttpServletResponse response);

    LoginResultToken getTokenForLoginUser(User user, LoginRequest loginRequest);

    User findUserById(Long userId);

    Optional<User> findUserByEmail(String email);

    // void invalidateUserCache(String userIdentifier);

    UserPublicProfileResponse findUserByBlogId(String blogId);

    UserPrivateProfileResponse findUserByTokenAndByBlogId(String blogId);

    void updateUserSettings(String blogId, UserSettingsRequest userSettingsRequest) throws IOException;

    boolean isExistsBlogId(String blogId);

    DuplicateCheckResponse isDuplicateBlogId(String blogId);

    DuplicateCheckResponse isDuplicateEmail(String email);

    DuplicateCheckResponse isDuplicateUsername(String username);

}
