package com.yhs.blog.springboot.jpa.domain.post.dto.response;

import java.time.LocalDateTime;

import lombok.Getter;

@Getter
public sealed abstract class PostAdminAndUserBaseResponse permits PostAdminPageResponse, PostUserPageResponse {
    protected final Long id;
    protected final String title;
    protected final String content;
    protected final String username;
    protected final String blogId;
    protected final String categoryName;
    protected final String featuredImageUrl;
    protected final LocalDateTime createdAt;

    protected PostAdminAndUserBaseResponse(
            Long id, String title, String content, String username,
            String blogId, String categoryName, String featuredImageUrl,
            LocalDateTime createdAt) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.username = username;
        this.blogId = blogId;
        this.categoryName = categoryName;
        this.featuredImageUrl = featuredImageUrl;
        this.createdAt = createdAt;
    }
}
