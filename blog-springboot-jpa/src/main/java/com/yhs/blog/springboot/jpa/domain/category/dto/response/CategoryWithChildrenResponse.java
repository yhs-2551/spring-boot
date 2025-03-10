package com.yhs.blog.springboot.jpa.domain.category.dto.response;

import java.util.List;

// 나중에 @QueryProjection 사용 고려. @QueryProjection이 더 나을 듯
public record CategoryWithChildrenResponse(
                String categoryUuid,
                String name,
                String categoryUuidParent, // 부모는 항상 null
                List<CategoryChildResponse> children, // 없으면 빈 배열
                Long postCount // 없으면 0
) {
        public CategoryWithChildrenResponse withChildren(List<CategoryChildResponse> newChildren) {
                return new CategoryWithChildrenResponse(
                                this.categoryUuid,
                                this.name,
                                this.categoryUuidParent,
                                newChildren,
                                this.postCount);
        }
}
