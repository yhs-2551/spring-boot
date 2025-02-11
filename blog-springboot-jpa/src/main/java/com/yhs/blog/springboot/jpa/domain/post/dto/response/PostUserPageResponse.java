package com.yhs.blog.springboot.jpa.domain.post.dto.response;

import java.time.LocalDateTime;

import com.querydsl.core.annotations.QueryProjection;
import com.yhs.blog.springboot.jpa.domain.post.entity.enums.PostStatus;

import lombok.Getter;

@Getter
public class PostUserPageResponse {
    private final Long id;
    private final String title;
    private final String content;
    private final String postStatus;
    private final String username;
    private final String categoryName;
    private final String featuredImageUrl;
    private final LocalDateTime createdAt;

    @QueryProjection
    public PostUserPageResponse(Long id, String title, String content, PostStatus postStatus, String username,
            String categoryName,
            String featuredImageUrl, LocalDateTime createdAt) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.postStatus = postStatus.name();
        this.username = username;
        this.categoryName = categoryName == null ? null : categoryName;
        this.featuredImageUrl = featuredImageUrl == null ? null : featuredImageUrl;
        this.createdAt = createdAt;
    }

}
