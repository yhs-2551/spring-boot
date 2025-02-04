package com.yhs.blog.springboot.jpa.domain.category.dto.response;

import java.util.List;

public record CategoryChildResponse(
        String categoryUuid,
        String name,
        String categoryUuidParent, // 부모 id
        List<Object> children, // 항상 빈 배열
        int postCount // 없으면 0
) {

}
