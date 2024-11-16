package com.yhs.blog.springboot.jpa.domain.post.mapper;

import com.yhs.blog.springboot.jpa.domain.category.entity.Category;
import com.yhs.blog.springboot.jpa.domain.post.entity.FeaturedImage;
import com.yhs.blog.springboot.jpa.domain.post.entity.Post;
import com.yhs.blog.springboot.jpa.domain.user.entity.User;

public class PostMapper {

    public static Post toEntity(User user, Category category, String title, String content,
                                 String postStatus, String commentsEnabled,
                                FeaturedImage featuredImage) {
        return Post.builder()
                .user(user) // user 객체 사용
                .category(category) // category 객체 사용
                .title(title)
                .content(content)
                .postStatus(Post.PostStatus.valueOf(postStatus.toUpperCase())) // 문자열을 PostStatus 열거형으로 변환
                .commentsEnabled(Post.CommentsEnabled.valueOf(commentsEnabled.toUpperCase())) // 문자열을
                // CommentsEnabled 열거형으로 변환
                .featuredImage(featuredImage)
                .build();

    }
//
//    public static PostRequest toDTO(Post post) {
//        return PostRequest.builder()
//                .id(post.getId())
//                .userId(post.getUser() != null ? post.getUser().getId() : null) // user 객체 사용
//                .userName(post.getUser() != null ? post.getUser().getUsername() : null)
//                .title(post.getTitle())
//                .content(post.getContent())
//                .categoryId(post.getCategory() != null ? post.getCategory().getId() : null) // category 객체 사용
//                .createdAt(post.getCreatedAt()) // 문자열을 PostStatus 열거형으로 변환
//                .updatedAt(post.getUpdatedAt())
//                .postStatus(post.getPostStatus().name())
//                .build();
//    }



}
