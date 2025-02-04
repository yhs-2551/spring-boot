package com.yhs.blog.springboot.jpa.domain.category.service;

import com.yhs.blog.springboot.jpa.domain.category.dto.request.CategoryRequestPayLoad; 
import com.yhs.blog.springboot.jpa.domain.category.dto.response.CategoryWithChildrenResponse;
import com.yhs.blog.springboot.jpa.domain.category.entity.Category;

import java.util.List;

public interface CategoryService {
    void createCategory(CategoryRequestPayLoad categoryRequestPayLoad, String blogId);

    List<CategoryWithChildrenResponse> getAllCategoriesWithChildrenByUserId(String blogId);

    Category findCategoryByNameAndUserId(String categoryName, Long userId);

}
