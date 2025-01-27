package com.yhs.blog.springboot.jpa.domain.user.service;
import com.yhs.blog.springboot.jpa.domain.user.entity.User;
import java.util.Optional;

public interface UserFindService {

    User findUserById(Long userId);

    Optional<User> findUserByEmail(String email);

    User findUserByBlogId(String blogId);

}
