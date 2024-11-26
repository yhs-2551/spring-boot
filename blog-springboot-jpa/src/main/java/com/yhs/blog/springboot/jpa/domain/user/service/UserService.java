package com.yhs.blog.springboot.jpa.domain.user.service;

import com.yhs.blog.springboot.jpa.domain.user.dto.request.SignUpUserRequest;
import com.yhs.blog.springboot.jpa.domain.user.dto.response.DuplicateCheckResponse;
import com.yhs.blog.springboot.jpa.domain.user.entity.User;
import com.yhs.blog.springboot.jpa.domain.user.dto.response.SignUpUserResponse;

public interface UserService {
    SignUpUserResponse createUser(SignUpUserRequest signUpUserRequest);

    User findUserById(Long userId);

    User findUserByEmail(String email);

    //    boolean existsByUserIdentifier(String userIdentifier);
//    void invalidateUserCache(String userIdentifier);
    DuplicateCheckResponse existsByBlogId(String blogId);

    DuplicateCheckResponse existsByEmail(String email);

    DuplicateCheckResponse existsByUserName(String userName);
}
