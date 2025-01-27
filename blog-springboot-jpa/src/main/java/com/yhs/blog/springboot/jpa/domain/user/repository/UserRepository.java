package com.yhs.blog.springboot.jpa.domain.user.repository;

import com.yhs.blog.springboot.jpa.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    Optional<User> findByBlogId(String blogId);
    
    boolean existsByBlogId(String blogId);
    boolean existsByEmail(String email);
    boolean existsByUsername(String username);
}
