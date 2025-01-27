package com.yhs.blog.springboot.jpa.domain.auth.service;

import com.yhs.blog.springboot.jpa.domain.auth.dto.request.LoginRequest;
import com.yhs.blog.springboot.jpa.domain.auth.dto.response.LoginResultToken; 

public interface LoginProcessService{
    LoginResultToken loginUser(LoginRequest loginRequest);

}
