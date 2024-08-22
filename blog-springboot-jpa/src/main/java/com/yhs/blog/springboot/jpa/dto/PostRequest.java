package com.yhs.blog.springboot.jpa.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDateTime;


@NoArgsConstructor
@Getter
public class PostRequest {
    private Long id;             // 게시글 ID

    private Long userId;          // 작성자 ID

    private String userName;        // 작성자명

    @NotEmpty(message = "제목을 입력하세요.")
    @Size(max = 255, message = "제목은 총 255글자 까지 허용 됩니다.")
    private String title;         // 게시글 제목

    @NotEmpty(message = "내용을 입력하세요.")
    private String content;       // 게시글 내용

    private Long categoryId;      // 카테고리 ID

    private LocalDateTime createdAt; // 생성 일시

    private LocalDateTime updatedAt; // 수정 일시

    private int views;            // 조회수

    private int commentCount;     // 댓글 수

    private int replyCount;       // 대댓글 수

    @NotNull(message = "공개 상태를 선택하세요.")
    @Pattern(regexp = "PUBLIC|PRIVATE", message = "'PUBLIC' 또는 'PRIVATE' 둘 중 하나의 상태로 선택하세요.")
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
