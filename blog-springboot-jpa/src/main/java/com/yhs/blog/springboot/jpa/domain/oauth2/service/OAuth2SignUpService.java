package com.yhs.blog.springboot.jpa.domain.oauth2.service;

import com.yhs.blog.springboot.jpa.common.response.RateLimitResponse;
import com.yhs.blog.springboot.jpa.domain.oauth2.dto.request.AdditionalInfoRequest;
import com.yhs.blog.springboot.jpa.domain.oauth2.dto.response.OAuth2SignUpResponse;

public interface OAuth2SignUpService {

    RateLimitResponse<OAuth2SignUpResponse> processOAuth2SignUp(AdditionalInfoRequest additionalInfoRequest);

}
