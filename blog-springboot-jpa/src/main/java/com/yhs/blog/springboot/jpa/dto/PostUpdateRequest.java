package com.yhs.blog.springboot.jpa.dto;

import com.yhs.blog.springboot.jpa.entity.Post;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@NoArgsConstructor
@Getter
@AllArgsConstructor
public class PostUpdateRequest {
    private String title;         // 게시글 제목

    private String content;       // 게시글 내용

    private String postStatus;    // 게시글 상태 (PUBLIC, PRIVATE)

    private Long categoryId; // 카테고리 아이디 값


}
