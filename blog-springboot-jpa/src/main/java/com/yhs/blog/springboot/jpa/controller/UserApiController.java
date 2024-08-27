package com.yhs.blog.springboot.jpa.controller;


import com.yhs.blog.springboot.jpa.dto.AddUserRequest;
import com.yhs.blog.springboot.jpa.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;

import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;

import org.springframework.stereotype.Controller;

import org.springframework.web.bind.annotation.PostMapping;

@Controller
@RequiredArgsConstructor
public class UserApiController {

    private final UserService userService;

    @PostMapping("/user")
    public Long signup(AddUserRequest addUserRequest) {
        return userService.createUser(addUserRequest);
    }

    SecurityContextLogoutHandler logoutHandler =
            new SecurityContextLogoutHandler();

    @PostMapping("/logout")
    public void logout(Authentication authentication, HttpServletRequest httpServletRequest,
                       HttpServletResponse httpServletResponse) {
        this.logoutHandler.logout(httpServletRequest, httpServletResponse, authentication);
    }
}
