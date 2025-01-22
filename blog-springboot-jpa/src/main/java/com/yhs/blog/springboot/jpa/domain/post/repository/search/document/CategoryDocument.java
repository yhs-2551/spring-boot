package com.yhs.blog.springboot.jpa.domain.post.repository.search.document;

import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import com.yhs.blog.springboot.jpa.domain.category.entity.Category;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Getter
@NoArgsConstructor // 역직렬화시 필요 내부적으로 Reflection 통해 private 필드 초기화. jpa와는 다르게 Setter가 필요 없음.
@AllArgsConstructor // builder와 함께 사용
@Builder
@Log4j2
public class CategoryDocument {

    @Field(type = FieldType.Keyword)
    private String id;

    @Field(type = FieldType.Keyword)
    private String name;

    public static CategoryDocument from(Category category) {

        return CategoryDocument.builder().id(category.getId()).name(category.getName()).build();
    }
}
