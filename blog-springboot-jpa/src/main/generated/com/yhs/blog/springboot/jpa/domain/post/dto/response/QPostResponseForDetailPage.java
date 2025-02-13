package com.yhs.blog.springboot.jpa.domain.post.dto.response;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.ConstructorExpression;
import javax.annotation.processing.Generated;

/**
 * com.yhs.blog.springboot.jpa.domain.post.dto.response.QPostResponseForDetailPage is a Querydsl Projection type for PostResponseForDetailPage
 */
@Generated("com.querydsl.codegen.DefaultProjectionSerializer")
public class QPostResponseForDetailPage extends ConstructorExpression<PostResponseForDetailPage> {

    private static final long serialVersionUID = 1637794011L;

    public QPostResponseForDetailPage(com.querydsl.core.types.Expression<String> title, com.querydsl.core.types.Expression<String> content, com.querydsl.core.types.Expression<? extends java.util.List<String>> tags, com.querydsl.core.types.Expression<? extends java.util.List<com.yhs.blog.springboot.jpa.domain.file.dto.response.FileResponse>> files, com.querydsl.core.types.Expression<com.yhs.blog.springboot.jpa.domain.post.entity.enums.PostStatus> postStatus, com.querydsl.core.types.Expression<String> username, com.querydsl.core.types.Expression<String> categoryName, com.querydsl.core.types.Expression<java.time.LocalDateTime> createdAt) {
        super(PostResponseForDetailPage.class, new Class<?>[]{String.class, String.class, java.util.List.class, java.util.List.class, com.yhs.blog.springboot.jpa.domain.post.entity.enums.PostStatus.class, String.class, String.class, java.time.LocalDateTime.class}, title, content, tags, files, postStatus, username, categoryName, createdAt);
    }

}

