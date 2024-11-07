package com.yhs.blog.springboot.jpa.util;

import com.yhs.blog.springboot.jpa.dto.CategoryResponse;
import com.yhs.blog.springboot.jpa.entity.Category;

import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

public class CategoryMapper {

    public static CategoryResponse toDTO(Category category, Map<String, CategoryResponse> cache) {
        if (category == null) {
            return null;
        }

        // 이미 DTO로 변환된 엔티티는 다시 변환하지 않음. 캐시 사용
        if (cache.containsKey(category.getId())) {
            return cache.get(category.getId());
        }

        CategoryResponse dto = new CategoryResponse(
                category.getId(),
                category.getName(),
                category.getParent() != null ? category.getParent().getId() : null,
                category.getChildren() != null && (!category.getChildren().isEmpty()) ?
                        category.getChildren().stream()
                                .map(child -> toDTO(child, cache))
                                .collect(Collectors.toList()) : Collections.emptyList()
        );

        cache.put(category.getId(), dto);
        return dto;
    }

}