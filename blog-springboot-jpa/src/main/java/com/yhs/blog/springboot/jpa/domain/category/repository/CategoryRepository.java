package com.yhs.blog.springboot.jpa.domain.category.repository;

import com.yhs.blog.springboot.jpa.domain.category.entity.Category;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, String>, CategoryRepositoryCustom {
        Optional<Category> findByNameAndUserId(String name, Long userId);

        @Query("DELETE FROM Category c WHERE c.id IN :uuids")
        @Modifying
        void deleteAllByCategoryId(@Param("uuids") List<String> uuids);

}
