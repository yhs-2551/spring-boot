package com.yhs.blog.springboot.jpa.domain.user.service;

import com.yhs.blog.springboot.jpa.common.response.RateLimitResponse;
import com.yhs.blog.springboot.jpa.domain.oauth2.dto.request.AdditionalInfoRequest;
import com.yhs.blog.springboot.jpa.domain.oauth2.dto.request.OAuth2SignUpResponse;
import com.yhs.blog.springboot.jpa.domain.user.dto.request.SignUpUserRequest;

public interface UserRegistrationService {

    void createUser(SignUpUserRequest signUpUserRequest);

    RateLimitResponse<OAuth2SignUpResponse> createOAuth2User(String email, AdditionalInfoRequest additionalInfoRequest);

}
