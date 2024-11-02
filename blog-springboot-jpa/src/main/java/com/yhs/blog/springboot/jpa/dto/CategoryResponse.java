package com.yhs.blog.springboot.jpa.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CategoryResponse {
//    private Long id;
    private String categoryUuid;
    private String name;
    private String categoryUuidParent;
    private List<CategoryResponse> children;
}
