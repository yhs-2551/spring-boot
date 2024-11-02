package com.yhs.blog.springboot.jpa.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@NoArgsConstructor
@Getter
@Setter
@ToString
public class CategoryRequest {
    private String name;
    private String categoryUuid;
    private String categoryUuidParent;
    private List<CategoryRequest> children;
}
