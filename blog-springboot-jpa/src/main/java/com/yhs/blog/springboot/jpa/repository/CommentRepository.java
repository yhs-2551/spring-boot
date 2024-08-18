package com.yhs.blog.springboot.jpa.repository;

import com.yhs.blog.springboot.jpa.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
}
