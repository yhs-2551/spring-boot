package com.yhs.blog.springboot.jpa.domain.post.dto.response;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

import org.springframework.lang.Nullable;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.querydsl.core.annotations.QueryProjection;
import com.yhs.blog.springboot.jpa.domain.file.dto.response.FileResponse;
import com.yhs.blog.springboot.jpa.domain.post.entity.enums.PostStatus;

import lombok.Getter;

@Getter
public class PostResponseForDetailPage { // 프론트에 DTO응답 전달되는 데이터는 생성자 기준이 아닌 클래스 필드 기준이라 파일 나눠야함..

    private final String username;

    private final String title;

    private final String content;

    private final String postStatus;

    @Nullable
    private final String categoryName;

    @Nullable
    private final List<String> tags;

    @Nullable
    private final List<FileResponse> files;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Seoul")
    private final LocalDateTime createdAt; // 생성 일시, LocalDateTime은 Spring Boot에서 자동으로 ISO-8601 형식으로 변환. Redis같은거만 설정해주면
                                           // 됨

    @QueryProjection
    public PostResponseForDetailPage(String title, String content, List<String> tags,
            List<FileResponse> files,
            PostStatus postStatus, String username, String categoryName, LocalDateTime createdAt) {
        this.title = title;
        this.content = content;
        // 리스트에 하나의 문자열이라도 있으면 tags 리턴
        this.tags = (tags != null && tags.stream().anyMatch(Objects::nonNull)) ? tags : null;
        // 파일에 하나의 url이라도 있으면 files 리턴
        this.files = (files != null && files.stream().anyMatch(file -> file.getFileUrl() != null)) ? files
                : null;
        this.categoryName = categoryName == null ? null : categoryName;
        this.postStatus = postStatus.name();
        this.username = username;
        this.createdAt = createdAt;
    }

}
