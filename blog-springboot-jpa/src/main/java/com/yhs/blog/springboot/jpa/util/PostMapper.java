package com.yhs.blog.springboot.jpa.util;

import com.yhs.blog.springboot.jpa.dto.PostDTO;
import com.yhs.blog.springboot.jpa.entity.Category;
import com.yhs.blog.springboot.jpa.entity.Post;
import com.yhs.blog.springboot.jpa.entity.User;

public class PostMapper {

    public static Post toEntity(User user, Category category, String title, String content, String postStatus) {
        return Post.builder()
                .user(user) // user 객체 사용
                .title(title)
                .content(content)
                .category(category) // category 객체 사용
                .postStatus(Post.PostStatus.valueOf(postStatus)) // 문자열을 PostStatus 열거형으로 변환
                .build();
    }

    public static PostDTO toDTO(Post post) {
        return PostDTO.builder()
                .id(post.getId())
                .userId(post.getUser() != null ? post.getUser().getId() : null) // user 객체 사용
                .userName(post.getUser() != null ? post.getUser().getUsername() : null)
                .title(post.getTitle())
                .content(post.getContent())
                .categoryId(post.getCategory() != null ? post.getCategory().getId() : null) // category 객체 사용
                .createdAt(post.getCreatedAt()) // 문자열을 PostStatus 열거형으로 변환
                .updatedAt(post.getUpdatedAt())
                .postStatus(post.getPostStatus().name())
                .build();
    }



}
