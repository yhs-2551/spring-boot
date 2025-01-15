package com.yhs.blog.springboot.jpa.domain.oauth2.service;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

import com.yhs.blog.springboot.jpa.domain.oauth2.dto.request.AdditionalInfoRequest;
import com.yhs.blog.springboot.jpa.domain.oauth2.dto.request.OAuth2SignUpResponse;
import com.yhs.blog.springboot.jpa.exception.custom.UnauthorizedException;
import com.yhs.blog.springboot.jpa.domain.user.dto.response.RateLimitResponse;
import com.yhs.blog.springboot.jpa.domain.user.service.UserService;
import com.yhs.blog.springboot.jpa.aop.ratelimit.RateLimit;

@Service
@RequiredArgsConstructor
public class OAuth2SignUpService {
    private final OAuth2TempDataService oAuth2TempDataService;
    private final UserService userService;

    @RateLimit(key = "OAuth2Signup")
    public RateLimitResponse<OAuth2SignUpResponse> processOAuth2SignUp(AdditionalInfoRequest additionalInfoRequest) {

        String email = oAuth2TempDataService.getAOAuth2UserEmail(
                additionalInfoRequest.getTempOAuth2UserUniqueId());

        if (email == null) {
            throw new UnauthorizedException("유효하지 않은 OAuth2 회원가입 요청입니다.");
        }

        return userService.createOAuth2User(email, additionalInfoRequest, null, null);

    }

}
