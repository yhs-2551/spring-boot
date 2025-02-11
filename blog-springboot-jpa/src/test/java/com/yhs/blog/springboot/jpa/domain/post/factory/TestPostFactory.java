package com.yhs.blog.springboot.jpa.domain.post.factory;

import org.springframework.boot.test.context.TestComponent;
import org.springframework.test.util.ReflectionTestUtils;

import com.yhs.blog.springboot.jpa.domain.post.entity.Post;
import com.yhs.blog.springboot.jpa.domain.post.entity.enums.CommentsEnabled;
import com.yhs.blog.springboot.jpa.domain.post.entity.enums.PostStatus; 

@TestComponent
public class TestPostFactory {

    // 테스트용 기본 게시글 생성

    public static Post createTestPost(Long userid) {
        Post post = Post.builder()
                .userId(userid)
                .categoryId(null)
                .title("testTitle")
                .content("testContent")
                .postStatus(PostStatus.PUBLIC)
                .commentsEnabled(CommentsEnabled.ALLOW)
                .featuredImageId(null)
                .build();

        return post;

    }

    public static Post createTestPostWithId(Long userid) {
        Post post = Post.builder()
                .userId(userid)
                .categoryId(null)
                .title("testTitle")
                .content("testContent")
                .postStatus(PostStatus.PUBLIC)
                .commentsEnabled(CommentsEnabled.ALLOW)
                .featuredImageId(null)
                .build();

        ReflectionTestUtils.setField(post, "id", 1L);

        return post;

    }

}
