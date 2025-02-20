package com.yhs.blog.springboot.jpa.domain.post.dto.response;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.ConstructorExpression;
import javax.annotation.processing.Generated;

/**
 * com.yhs.blog.springboot.jpa.domain.post.dto.response.QPostIndexAndIndexSearchResponse is a Querydsl Projection type for PostIndexAndIndexSearchResponse
 */
@Generated("com.querydsl.codegen.DefaultProjectionSerializer")
public class QPostIndexAndIndexSearchResponse extends ConstructorExpression<PostIndexAndIndexSearchResponse> {

    private static final long serialVersionUID = -1258731319L;

    public QPostIndexAndIndexSearchResponse(com.querydsl.core.types.Expression<Long> id, com.querydsl.core.types.Expression<String> title, com.querydsl.core.types.Expression<String> content, com.querydsl.core.types.Expression<String> username, com.querydsl.core.types.Expression<String> blogId, com.querydsl.core.types.Expression<String> categoryName, com.querydsl.core.types.Expression<String> featuredImageUrl, com.querydsl.core.types.Expression<java.time.LocalDateTime> createdAt) {
        super(PostIndexAndIndexSearchResponse.class, new Class<?>[]{long.class, String.class, String.class, String.class, String.class, String.class, String.class, java.time.LocalDateTime.class}, id, title, content, username, blogId, categoryName, featuredImageUrl, createdAt);
    }

}

