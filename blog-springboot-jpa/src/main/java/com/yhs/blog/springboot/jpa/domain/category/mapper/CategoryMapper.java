package com.yhs.blog.springboot.jpa.domain.category.mapper;

import com.yhs.blog.springboot.jpa.domain.category.dto.response.CategoryResponse;
import com.yhs.blog.springboot.jpa.domain.category.entity.Category;

import java.util.Collections; 
import java.util.stream.Collectors;

public class CategoryMapper {
 


    public static CategoryResponse of(Category category) {

        return new CategoryResponse(
                category.getId(),
                category.getName(),
                null,
                category.getChildren() != null && !category.getChildren().isEmpty() ?
                        category.getChildren().stream()
                                .map(subCategory -> new CategoryResponse(
                                        subCategory.getId(),
                                        subCategory.getName(),
                                        category.getId(),
                                        Collections.emptyList(), // 2단계 자식은 자식이 존재하지 않으니 빈배열로
                                        subCategory.getPosts() != null && !subCategory.getPosts().isEmpty() ?
                                                subCategory.getPosts().size() : 0
                                ))
                                .collect(Collectors.toList()) : Collections.emptyList(), // 최상위에자식이 없으면 빈배열로 반환
                category.getPosts() != null && !category.getPosts().isEmpty() ?
                        category.getPosts().size() : 0

        );
    }


}