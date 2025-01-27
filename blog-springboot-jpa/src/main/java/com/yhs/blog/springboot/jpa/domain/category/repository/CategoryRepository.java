package com.yhs.blog.springboot.jpa.domain.category.repository;

import com.yhs.blog.springboot.jpa.domain.category.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, String> {

    // 사용자의 모든 카테고리를 가져오며, 카테고리의 자식 카테고리와 포스트도 함께 가져옴 
    @Query("""
            SELECT DISTINCT c FROM Category c
            LEFT JOIN FETCH c.children ch
            LEFT JOIN FETCH c.posts p
            LEFT JOIN FETCH ch.posts chp
            WHERE c.parent IS NULL AND c.user.id = :userId
            ORDER BY c.orderIndex ASC, ch.orderIndex ASC
            """)
    List<Category> findAllWithChildrenAndPostsByUserId(@Param("userId") Long userId);

    // 단일 카테고리의 자식 및 포스트 까지 한번에 가져옴 
    @Query("""
            SELECT DISTINCT c FROM Category c
            LEFT JOIN FETCH c.children ch
            LEFT JOIN FETCH c.posts p
            LEFT JOIN FETCH ch.posts chp
            WHERE c.id = :categoryId
            """)
    Optional<Category> findByIdWithChildrenAndPosts(@Param("categoryId") String categoryId);

    Optional<Category> findByNameAndUserId(String name, Long userId);

}
