package com.yhs.blog.springboot.jpa.domain.auth.service;

import io.jsonwebtoken.ExpiredJwtException;

public interface LogoutProcessService {

    void logoutUser(String token);

    void logoutUserByExpiredToken(ExpiredJwtException e);

}
