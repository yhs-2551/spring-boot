package com.yhs.blog.springboot.jpa.dto;

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
    private int childrenCount;
    private int postCount;
}
