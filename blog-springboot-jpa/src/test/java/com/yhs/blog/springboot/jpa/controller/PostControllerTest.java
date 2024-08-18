package com.yhs.blog.springboot.jpa.controller;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yhs.blog.springboot.jpa.dto.PostDTO;
import com.yhs.blog.springboot.jpa.entity.Post;
import com.yhs.blog.springboot.jpa.repository.PostRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureMockMvc
class PostControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    private WebApplicationContext context;

    @Autowired
    PostRepository postRepository;

    @BeforeEach
    public void mockMvcSetUp() {
        this.mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .build();

        postRepository.deleteAll();
    }

    @DisplayName("addPost: 블로그 글 작성 성공 테스트")
    @Test
    public void addPost() throws Exception {

//        given
        final String url = "/api/posts";
        final String title = "테스트 타이틀";
        final String content = "테스트 컨텐츠";
        final PostDTO postDTO = PostDTO.builder().title(title).content(content).postStatus("PRIVATE").build();

//        PostDTO 객체를 JSON 으로 직렬화
        final String requestBody = objectMapper.writeValueAsString(postDTO);

//        when
        ResultActions result = mockMvc.perform(MockMvcRequestBuilders.post(url).contentType(MediaType.APPLICATION_JSON_VALUE).content(requestBody));

//        then
        result.andExpect(MockMvcResultMatchers.status().isCreated());

        List<Post> posts = postRepository.findAll();

        assertThat(posts.size()).isEqualTo(1);
        assertThat(posts.getFirst().getTitle()).isEqualTo(title);
        assertThat(posts.getFirst().getContent()).isEqualTo(content);

    }
}


