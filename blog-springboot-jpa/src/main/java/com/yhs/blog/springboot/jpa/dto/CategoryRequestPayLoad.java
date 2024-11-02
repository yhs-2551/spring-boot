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
public class CategoryRequestPayLoad {
    List<CategoryRequest> categories;
    List<String> categoryToDelete;
}
