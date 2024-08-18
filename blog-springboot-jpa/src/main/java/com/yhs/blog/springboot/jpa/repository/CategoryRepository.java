package com.yhs.blog.springboot.jpa.repository;

import com.yhs.blog.springboot.jpa.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
}
