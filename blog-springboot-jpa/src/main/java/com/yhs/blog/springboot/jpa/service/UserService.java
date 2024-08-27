package com.yhs.blog.springboot.jpa.service;

import com.yhs.blog.springboot.jpa.dto.AddUserRequest;

public interface UserService{
    Long createUser(AddUserRequest addUserRequest);
}
