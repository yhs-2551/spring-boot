package com.yhs.blog.springboot.jpa.domain.post.dto.response;

import java.util.List;
import java.util.Objects;

import org.springframework.lang.Nullable;

import com.querydsl.core.annotations.QueryProjection;
import com.yhs.blog.springboot.jpa.domain.file.dto.response.FileResponse;
import com.yhs.blog.springboot.jpa.domain.post.entity.enums.PostStatus;

import lombok.Getter;

@Getter
public class PostResponseForEditPage {

    private final String title;

    private final String content;

    private final String postStatus;

    @Nullable
    private final String categoryName;

    @Nullable
    private final List<String> tags;

    @Nullable
    private final List<FileResponse> files;

    @Nullable
    private final FeaturedImageResponse featuredImage; // 여기선 대표 이미지 모든 정보 필요

    @QueryProjection
    public PostResponseForEditPage(String title, String content, List<String> tags,
            List<FileResponse> files,
            FeaturedImageResponse featuredImage,
            PostStatus postStatus, String categoryName) {

        this.title = title;

        this.content = content;
        this.tags = (tags != null && tags.stream().anyMatch(Objects::nonNull)) ? tags : null;
        this.files = (files != null && files.stream().anyMatch(file -> file.getFileUrl() != null)) ? files
                : null;
        this.featuredImage = featuredImage.getFileUrl() == null ? null : featuredImage;
        this.postStatus = postStatus.name();
        this.categoryName = categoryName == null ? null : categoryName;
    }
}
