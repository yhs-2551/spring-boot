package com.yhs.blog.springboot.jpa.domain.post.dto.response;

import java.time.LocalDateTime;

import com.querydsl.core.annotations.QueryProjection;

import lombok.Getter;

@Getter
public final class PostUserPageResponse extends PostAdminAndUserBaseResponse {

    @QueryProjection
    public PostUserPageResponse(Long id, String title, String content, String username, String blogId,
            String categoryName,
            String featuredImageUrl, LocalDateTime createdAt) {
        super(id, title, content, username, blogId,
                categoryName == null ? null : categoryName,
                featuredImageUrl == null ? null : featuredImageUrl,
                createdAt);
    }

}
