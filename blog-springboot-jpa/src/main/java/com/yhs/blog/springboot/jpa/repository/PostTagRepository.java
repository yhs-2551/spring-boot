package com.yhs.blog.springboot.jpa.repository;

import com.yhs.blog.springboot.jpa.entity.PostTag;
import com.yhs.blog.springboot.jpa.entity.PostTagId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PostTagRepository extends JpaRepository<PostTag, PostTagId> {
}
