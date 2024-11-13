package com.yhs.blog.springboot.jpa.service;
import com.yhs.blog.springboot.jpa.dto.CategoryRequest;
import com.yhs.blog.springboot.jpa.dto.CategoryRequestPayLoad;
import com.yhs.blog.springboot.jpa.dto.CategoryResponse;

import java.util.List;

public interface CategoryService {
    List<CategoryResponse> createCategory(CategoryRequestPayLoad categoryRequestPayLoad,
                                          String userIdentifier);
    List<CategoryResponse> getAllCategoriesWithChildrenByUserId(String userIdentifier);

}
