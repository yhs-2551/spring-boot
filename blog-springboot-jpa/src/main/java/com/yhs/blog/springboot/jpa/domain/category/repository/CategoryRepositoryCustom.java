package com.yhs.blog.springboot.jpa.domain.category.repository;

import java.util.List;

import com.yhs.blog.springboot.jpa.domain.category.dto.response.CategoryWithChildrenResponse;

public interface CategoryRepositoryCustom {

    List<CategoryWithChildrenResponse> findAllWithChildrenAndPostsByUserId(Long userId);

}
