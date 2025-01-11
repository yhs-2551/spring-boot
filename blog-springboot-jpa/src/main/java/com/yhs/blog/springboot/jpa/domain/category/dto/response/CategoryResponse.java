package com.yhs.blog.springboot.jpa.domain.category.dto.response;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class CategoryResponse {
//    private Long id;
    private String categoryUuid;
    private String name;
    private String categoryUuidParent;
    private List<CategoryResponse> children;
    private int postCount;
}
