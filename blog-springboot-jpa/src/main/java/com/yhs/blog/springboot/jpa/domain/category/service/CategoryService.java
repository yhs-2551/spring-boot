package com.yhs.blog.springboot.jpa.domain.category.service;
import com.yhs.blog.springboot.jpa.domain.category.dto.request.CategoryRequestPayLoad;
import com.yhs.blog.springboot.jpa.domain.category.dto.response.CategoryResponse;

import java.util.List;

public interface CategoryService {
    List<CategoryResponse> createCategory(CategoryRequestPayLoad categoryRequestPayLoad);
    List<CategoryResponse> getAllCategoriesWithChildrenByUserId(Long userId);

}
