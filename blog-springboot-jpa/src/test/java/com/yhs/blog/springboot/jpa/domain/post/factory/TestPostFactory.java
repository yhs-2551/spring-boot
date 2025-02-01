package com.yhs.blog.springboot.jpa.domain.post.factory;

import org.springframework.boot.test.context.TestComponent;
import org.springframework.test.util.ReflectionTestUtils;

import com.yhs.blog.springboot.jpa.domain.post.entity.Post;
import com.yhs.blog.springboot.jpa.domain.post.entity.enums.CommentsEnabled;
import com.yhs.blog.springboot.jpa.domain.post.entity.enums.PostStatus;
import com.yhs.blog.springboot.jpa.domain.user.entity.User;

@TestComponent
public class TestPostFactory {

    // 테스트용 기본 게시글 생성

    public static Post createTestPost(User user) {
        Post post = Post.builder()
                .user(user)
                .category(null)
                .title("testTitle")
                .content("testContent")
                .postStatus(PostStatus.PUBLIC)
                .commentsEnabled(CommentsEnabled.ALLOW)
                .featuredImage(null)
                .build();

        return post;

    }

    public static Post createTestPostWithId(User user) {
        Post post = Post.builder()
                .user(user)
                .category(null)
                .title("testTitle")
                .content("testContent")
                .postStatus(PostStatus.PUBLIC)
                .commentsEnabled(CommentsEnabled.ALLOW)
                .featuredImage(null)
                .build();

        ReflectionTestUtils.setField(post, "id", 1L);

        return post;

    }

}
