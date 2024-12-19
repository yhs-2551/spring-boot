package com.yhs.blog.springboot.jpa.domain.post.mapper;

import com.yhs.blog.springboot.jpa.domain.category.entity.Category;
import com.yhs.blog.springboot.jpa.domain.post.entity.FeaturedImage;
import com.yhs.blog.springboot.jpa.domain.post.entity.Post;
import com.yhs.blog.springboot.jpa.domain.post.entity.enums.CommentsEnabled;
import com.yhs.blog.springboot.jpa.domain.post.entity.enums.PostStatus;
import com.yhs.blog.springboot.jpa.domain.user.entity.User;

public class PostMapper {

    public static Post create(User user, Category category, String title, String content,
                                 String postStatus, String commentsEnabled,
                                FeaturedImage featuredImage) {
        return Post.builder()
                .user(user) // user 객체 사용
                .category(category) // category 객체 사용
                .title(title)
                .content(content)
                .postStatus(PostStatus.valueOf(postStatus.toUpperCase())) // 문자열을 PostStatus 열거형으로 변환
                .commentsEnabled(CommentsEnabled.valueOf(commentsEnabled.toUpperCase())) // 문자열을
                // CommentsEnabled 열거형으로 변환
                .featuredImage(featuredImage)
                .build();

    }

}
