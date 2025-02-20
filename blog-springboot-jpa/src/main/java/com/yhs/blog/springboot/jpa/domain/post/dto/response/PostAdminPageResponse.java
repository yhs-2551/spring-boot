package com.yhs.blog.springboot.jpa.domain.post.dto.response;

import java.time.LocalDateTime;

import com.querydsl.core.annotations.QueryProjection;
import com.yhs.blog.springboot.jpa.domain.post.entity.enums.PostStatus;

import lombok.Getter;

@Getter
public final class PostAdminPageResponse extends PostAdminAndUserBaseResponse {

    private final String postStatus;

    @QueryProjection
    public PostAdminPageResponse(Long id, String title, String content, PostStatus postStatus, String username,
            String blogId,
            String categoryName,
            String featuredImageUrl, LocalDateTime createdAt) {
        super(id, title, content, username, blogId,
                categoryName == null ? null : categoryName,
                featuredImageUrl == null ? null : featuredImageUrl,
                createdAt);
        this.postStatus = postStatus.name();
    }

}
