package com.yhs.blog.springboot.jpa.dto;


import com.yhs.blog.springboot.jpa.entity.Post;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
public class PostUpdateResponse {

    private String title;         // 게시글 제목

    private String content;       // 게시글 내용

    private String postStatus;    // 게시글 상태 (PUBLIC, PRIVATE)

    private String categoryName; // 카테고리 아이디 값


    public PostUpdateResponse(Post post) {
        this.title = post.getTitle();
        this.content = post.getContent();
        this.postStatus = post.getPostStatus().name();
        this.categoryName = post.getCategory() != null ? post.getCategory().getName() : null;
    }
}
