package com.yhs.blog.springboot.jpa.domain.post.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.yhs.blog.springboot.jpa.domain.post.dto.request.PostRequest;
import com.yhs.blog.springboot.jpa.domain.post.service.PostService;

@SpringBootTest
public class PostControllerTest {

    @Autowired
    private PostService postService;

    @Test
    @DisplayName("ES 테스트용 1000개 데이터 생성")
    void createBulkPostsForES() {
        // given
        String blogId = "aalv__"; // 실제 존재하는 사용자의 blogId

        for (int i = 1; i <= 900; i++) {
            PostRequest postRequest = PostRequest.builder()
                    .title("테스트 제목 " + i)
                    .content("테스트 내용 " + i)
                    .categoryName("프로그래밍") // 실제 존재하는 카테고리명
                    .postStatus("PUBLIC")
                    .commentsEnabled("ALLOW")
                    .build();

            // when
            postService.createNewPost(postRequest, blogId);
        }
    }

}
