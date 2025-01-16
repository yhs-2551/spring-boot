package com.yhs.blog.springboot.jpa.domain.post.repository.search.document;

import java.time.LocalDateTime; 
import java.util.Optional;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.InnerField;
import org.springframework.data.elasticsearch.annotations.MultiField;
import com.yhs.blog.springboot.jpa.domain.category.entity.Category;
import com.yhs.blog.springboot.jpa.domain.post.entity.Post;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Document(indexName = "posts")
@Getter // 필수
@Setter // Elasticsearch에서 값을 업데이트하기 위해
@NoArgsConstructor // 필수
@AllArgsConstructor // 필수는 아니지만 권장
@Builder
@JsonIgnoreProperties(ignoreUnknown = true) // elasticSearch의 _class 같은 필드를 무시하도록 설정
public class PostDocument {

        @Id
        @Field(type = FieldType.Keyword)
        private String id; // Long에서 String으로 변경

        // 메인 필드 설정으로 정확한 키워드 일치 및 InnerField 설정으로검색시 ngram 필드 추가 사용
        @MultiField(mainField = @Field(type = FieldType.Keyword), otherFields = {
                        @InnerField(suffix = "ngram", // title.ngram으로 접근
                                        type = FieldType.Text, analyzer = "my_analyzer")
        })
        private String title;

        @MultiField(mainField = @Field(type = FieldType.Keyword), otherFields = {
                        @InnerField(suffix = "ngram", // content.ngram으로 접근
                                        type = FieldType.Text, analyzer = "my_analyzer")
        })
        private String content;

        @Field(type = FieldType.Keyword)
        private String categoryId; // 카테고리 검색용

        @Field(type = FieldType.Nested)
        private CategoryDocument category; // 프론트측 응답에 필요

        @Field(type = FieldType.Keyword)
        private String userId; // userId 검색용

        @Field(type = FieldType.Keyword)
        private String username; // 프론트측 응답에 필요함

        @Field(type = FieldType.Keyword)
        private String blogId; // 프론트측 응답에 필요함

        @Field(type = FieldType.Nested)
        private FeaturedImageDocument featuredImage; // 프론트측 응답에 필요함

        @Field(type = FieldType.Date, format = DateFormat.date_hour_minute_second, pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime createdAt;

        public static PostDocument from(Post post) {
                return PostDocument.builder()
                                .id(String.valueOf(post.getId())) // Long을 String으로 변환
                                .title(post.getTitle())
                                .content(post.getContent())
                                .categoryId(Optional.ofNullable(post.getCategory()).map(Category::getId).orElse(null))
                                .category(Optional.ofNullable(post.getCategory()).map(CategoryDocument::from)
                                                .orElse(null))
                                .username(post.getUser().getUsername())
                                .blogId(post.getUser().getBlogId())
                                .userId(String.valueOf(post.getUser().getId()))
                                .featuredImage(Optional.ofNullable(post.getFeaturedImage())
                                                .map(FeaturedImageDocument::from).orElse(null))
                                .createdAt(post.getCreatedAt())
                                .build();
        }

}
