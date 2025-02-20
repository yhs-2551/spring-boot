package com.yhs.blog.springboot.jpa.domain.post.dto.response;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.ConstructorExpression;
import javax.annotation.processing.Generated;

/**
 * com.yhs.blog.springboot.jpa.domain.post.dto.response.QPostResponseForEditPage is a Querydsl Projection type for PostResponseForEditPage
 */
@Generated("com.querydsl.codegen.DefaultProjectionSerializer")
public class QPostResponseForEditPage extends ConstructorExpression<PostResponseForEditPage> {

    private static final long serialVersionUID = -1644466988L;

    public QPostResponseForEditPage(com.querydsl.core.types.Expression<String> title, com.querydsl.core.types.Expression<String> content, com.querydsl.core.types.Expression<? extends java.util.List<String>> tags, com.querydsl.core.types.Expression<? extends java.util.List<com.yhs.blog.springboot.jpa.domain.file.dto.response.FileResponse>> files, com.querydsl.core.types.Expression<? extends FeaturedImageResponse> featuredImage, com.querydsl.core.types.Expression<com.yhs.blog.springboot.jpa.domain.post.entity.enums.PostStatus> postStatus, com.querydsl.core.types.Expression<String> categoryName) {
        super(PostResponseForEditPage.class, new Class<?>[]{String.class, String.class, java.util.List.class, java.util.List.class, FeaturedImageResponse.class, com.yhs.blog.springboot.jpa.domain.post.entity.enums.PostStatus.class, String.class}, title, content, tags, files, featuredImage, postStatus, categoryName);
    }

}

