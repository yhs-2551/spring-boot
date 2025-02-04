package com.yhs.blog.springboot.jpa.domain.category.repository;

import com.yhs.blog.springboot.jpa.domain.category.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, String>, CategoryRepositoryCustom {
        Optional<Category> findByNameAndUserId(String name, Long userId);
}
