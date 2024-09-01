package com.yhs.blog.springboot.jpa.controller;

import com.yhs.blog.springboot.jpa.dto.CreateAccessTokenRequest;
import com.yhs.blog.springboot.jpa.dto.CreateAccessTokenResponse;
import com.yhs.blog.springboot.jpa.service.TokenService;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class TokenApiController {
    private final TokenService tokenService;

    @PostMapping("/token")
    public ResponseEntity<CreateAccessTokenResponse> createNewAccessToken(@RequestBody CreateAccessTokenRequest accessTokenRequest) {
        String newAccessToken =
                tokenService.createNewAccessToken(accessTokenRequest.getRefreshToken());

        return ResponseEntity.status(HttpStatus.CREATED).body(new CreateAccessTokenResponse(newAccessToken));
    }
}
