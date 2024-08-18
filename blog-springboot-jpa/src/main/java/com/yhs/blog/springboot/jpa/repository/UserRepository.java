package com.yhs.blog.springboot.jpa.repository;

import com.yhs.blog.springboot.jpa.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
}
