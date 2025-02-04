package com.yhs.blog.springboot.jpa.domain.category.dto.response;

import java.util.List;

public record CategoryWithChildrenResponse(
        String categoryUuid,
        String name,
        String categoryUuidParent, // 부모는 항상 null
        List<CategoryChildResponse> children, // 없으면 빈 배열
        int postCount // 없으면 0
) {

}
