package com.yhs.blog.springboot.jpa.domain.post.dto.response;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.ConstructorExpression;
import javax.annotation.processing.Generated;

/**
 * com.yhs.blog.springboot.jpa.domain.post.dto.response.QPostUserPageResponse is a Querydsl Projection type for PostUserPageResponse
 */
@Generated("com.querydsl.codegen.DefaultProjectionSerializer")
public class QPostUserPageResponse extends ConstructorExpression<PostUserPageResponse> {

    private static final long serialVersionUID = 1514558472L;

    public QPostUserPageResponse(com.querydsl.core.types.Expression<Long> id, com.querydsl.core.types.Expression<String> title, com.querydsl.core.types.Expression<String> content, com.querydsl.core.types.Expression<com.yhs.blog.springboot.jpa.domain.post.entity.enums.PostStatus> postStatus, com.querydsl.core.types.Expression<String> username, com.querydsl.core.types.Expression<String> categoryName, com.querydsl.core.types.Expression<String> featuredImageUrl, com.querydsl.core.types.Expression<java.time.LocalDateTime> createdAt) {
        super(PostUserPageResponse.class, new Class<?>[]{long.class, String.class, String.class, com.yhs.blog.springboot.jpa.domain.post.entity.enums.PostStatus.class, String.class, String.class, String.class, java.time.LocalDateTime.class}, id, title, content, postStatus, username, categoryName, featuredImageUrl, createdAt);
    }

}

