package com.yhs.blog.springboot.jpa.domain.user.service;

import com.yhs.blog.springboot.jpa.domain.user.dto.response.DuplicateCheckResponse;

public interface UserCheckService {

    boolean isExistsBlogId(String blogId);

    DuplicateCheckResponse isDuplicateBlogId(String blogId);

    DuplicateCheckResponse isDuplicateEmail(String email);

    DuplicateCheckResponse isDuplicateUsername(String username);

}
