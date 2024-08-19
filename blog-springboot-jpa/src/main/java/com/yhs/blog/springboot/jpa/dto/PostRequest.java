package com.yhs.blog.springboot.jpa.dto;

import lombok.*;

import java.time.LocalDateTime;


@NoArgsConstructor
@Getter
public class PostRequest {
    private Long id;             // 게시글 ID

    private Long userId;          // 작성자 ID

    private String userName;        // 작성자명

    private String title;         // 게시글 제목

    private String content;       // 게시글 내용

    private Long categoryId;      // 카테고리 ID

    private LocalDateTime createdAt; // 생성 일시

    private LocalDateTime updatedAt; // 수정 일시

    private int views;            // 조회수

    private int commentCount;     // 댓글 수

    private int replyCount;       // 대댓글 수

    private String postStatus;    // 게시글 상태 (PUBLIC, PRIVATE)


    @Builder
    public PostRequest(Long id, Long userId, String userName, String title, String content, Long categoryId, LocalDateTime createdAt, LocalDateTime updatedAt, String postStatus) {
        this.userId = userId;
        this.userName = userName;
        this.title = title;
        this.content = content;
        this.categoryId = categoryId;
        this.postStatus = postStatus;
    }

}
