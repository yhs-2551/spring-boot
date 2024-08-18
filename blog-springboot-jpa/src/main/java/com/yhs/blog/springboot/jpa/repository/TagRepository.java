package com.yhs.blog.springboot.jpa.repository;

import com.yhs.blog.springboot.jpa.entity.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TagRepository extends JpaRepository<Tag, Long> {
}
