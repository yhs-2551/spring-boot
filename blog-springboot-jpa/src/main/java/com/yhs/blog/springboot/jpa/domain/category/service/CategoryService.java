package com.yhs.blog.springboot.jpa.domain.category.service;

import com.yhs.blog.springboot.jpa.domain.category.dto.request.CategoryRequestPayLoad;
import com.yhs.blog.springboot.jpa.domain.category.dto.response.CategoryResponse;
import com.yhs.blog.springboot.jpa.domain.category.entity.Category;

import java.util.List;

public interface CategoryService {
    List<CategoryResponse> createCategory(CategoryRequestPayLoad categoryRequestPayLoad);

    List<CategoryResponse> getAllCategoriesWithChildrenByUserId(String blogId);

    Category findCategoryByNameAndUserId(String categoryName, Long userId);

}
