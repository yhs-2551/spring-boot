package com.yhs.blog.springboot.jpa.domain.file.dto.response;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.ConstructorExpression;
import javax.annotation.processing.Generated;

/**
 * com.yhs.blog.springboot.jpa.domain.file.dto.response.QFileResponse is a Querydsl Projection type for FileResponse
 */
@Generated("com.querydsl.codegen.DefaultProjectionSerializer")
public class QFileResponse extends ConstructorExpression<FileResponse> {

    private static final long serialVersionUID = -573102938L;

    public QFileResponse(com.querydsl.core.types.Expression<String> fileName, com.querydsl.core.types.Expression<String> fileType, com.querydsl.core.types.Expression<String> fileUrl, com.querydsl.core.types.Expression<Long> fileSize, com.querydsl.core.types.Expression<Integer> width, com.querydsl.core.types.Expression<Integer> height) {
        super(FileResponse.class, new Class<?>[]{String.class, String.class, String.class, long.class, int.class, int.class}, fileName, fileType, fileUrl, fileSize, width, height);
    }

    public QFileResponse(com.querydsl.core.types.Expression<String> fileUrl, com.querydsl.core.types.Expression<Integer> width, com.querydsl.core.types.Expression<Integer> height) {
        super(FileResponse.class, new Class<?>[]{String.class, int.class, int.class}, fileUrl, width, height);
    }

}

