package com.yhs.blog.springboot.jpa.service;

import com.yhs.blog.springboot.jpa.dto.AddUserRequest;
import com.yhs.blog.springboot.jpa.entity.User;

public interface UserService{
    Long createUser(AddUserRequest addUserRequest);
    User findUserById(Long userId);
    User findUserByEmail(String email);
}
