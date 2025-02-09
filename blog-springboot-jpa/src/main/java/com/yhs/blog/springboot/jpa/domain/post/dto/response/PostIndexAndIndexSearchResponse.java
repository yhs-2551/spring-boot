package com.yhs.blog.springboot.jpa.domain.post.dto.response;

import java.time.LocalDateTime;

import lombok.Getter;

@Getter
public class PostIndexAndIndexSearchResponse {
    private final Long id; // 게시글 식별자. 수정 페이지 및 상세페이지에는 필요x(프론트측 params에서 뽑아 쓰기 때문)
    private final String title;
    private final String content;
    private final String username;
    private final String blogId;
    private final String categoryName;
    private final String featuredImageUrl;
    // 생성 일시, LocalDateTime은 Spring Boot에서 자동으로 ISO-8601 형식으로 변환. Redis같은거만 설정해주면 됨
    private final LocalDateTime createdAt;

    public PostIndexAndIndexSearchResponse(Long id, String title, String content, String username, String blogId, String categoryName,
            String featuredImageUrl, LocalDateTime createdAt) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.username = username;
        this.blogId = blogId;
        this.categoryName = categoryName == null ? null : categoryName;
        this.featuredImageUrl = featuredImageUrl == null ? null : featuredImageUrl;
        this.createdAt = createdAt;
    }

}
