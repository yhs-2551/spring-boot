package com.yhs.blog.springboot.jpa.domain.oauth2.controller;

import com.yhs.blog.springboot.jpa.common.response.ApiResponse;
import com.yhs.blog.springboot.jpa.common.response.ErrorResponse;
import com.yhs.blog.springboot.jpa.common.response.SuccessResponse;
import com.yhs.blog.springboot.jpa.domain.oauth2.dto.request.AdditionalInfoRequest;
import com.yhs.blog.springboot.jpa.domain.user.dto.response.RateLimitResponse;
import com.yhs.blog.springboot.jpa.domain.user.dto.response.SignUpResponseWithHeaders;
import com.yhs.blog.springboot.jpa.domain.user.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class OAuth2ApiController {

    private final UserService userService;
    private final RedisTemplate<String, String> redisTemplate;


    @PostMapping("/oauth2/users")
    public ResponseEntity<ApiResponse> oAuth2UserSignup(@Valid @RequestBody AdditionalInfoRequest additionalInfoRequest,
                                                        Authentication authentication, HttpServletRequest request, HttpServletResponse response) {


        String email = redisTemplate.opsForValue().get( "TEMP_OAUTH2_USER_EMAIL:" + additionalInfoRequest.getTempOAuth2UserUniqueId());
        redisTemplate.delete("TEMP_OAUTH2_USER_EMAIL:" + additionalInfoRequest.getTempOAuth2UserUniqueId());

        log.debug("OAuth2 User Signup Email: {}", email);

        try {
            RateLimitResponse rateLimitResponse = userService.createOAuth2User(email, additionalInfoRequest, request, response);
            SignUpResponseWithHeaders signUpResponseWithHeaders = (SignUpResponseWithHeaders) rateLimitResponse.getData();

            if (rateLimitResponse.isSuccess()) {
                return ResponseEntity.status(HttpStatus.CREATED).headers(signUpResponseWithHeaders.headers())
                        .body(new SuccessResponse<>(signUpResponseWithHeaders.signUpUserResponse(), rateLimitResponse.getMessage()));
            } else {
                return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(new ErrorResponse(rateLimitResponse.getMessage(),
                        rateLimitResponse.getStatusCode()));
            }

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorResponse("OAuth2 신규 사용자 등록에 " +
                    "실패하였습니다.",
                    HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }


    }
}
