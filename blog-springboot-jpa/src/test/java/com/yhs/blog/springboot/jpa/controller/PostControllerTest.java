package com.yhs.blog.springboot.jpa.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yhs.blog.springboot.jpa.dto.PostRequest;
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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.util.List;
import java.util.Optional;

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

    @Transactional
    @DisplayName("addPost: 블로그 글 작성 성공 테스트")
    @Test
    public void addPost() throws Exception {

//        given
        final String url = "/api/posts";
        final String title = "테스트 타이틀";
        final String content = "테스트 컨텐츠";
        final PostRequest postRequest = PostRequest.builder().title(title).content(content).postStatus("PRIVATE").build();

//        PostDTO 객체를 JSON 으로 직렬화
        final String requestBody = objectMapper.writeValueAsString(postRequest);

//        when
        ResultActions result = mockMvc.perform(MockMvcRequestBuilders.post(url).contentType(MediaType.APPLICATION_JSON_VALUE).content(requestBody));

//        then
        result.andExpect(MockMvcResultMatchers.status().isCreated());

        List<Post> posts = postRepository.findAll();

        assertThat(posts.size()).isEqualTo(1);
        assertThat(posts.getFirst().getTitle()).isEqualTo(title);
        assertThat(posts.getFirst().getContent()).isEqualTo(content);

    }

    @Transactional
    @DisplayName("findAllPosts: 블로그의 모든 글 목록 조회 테스트")
    @Test
    public void findAllPosts() throws Exception {
//        given

        final String url = "/api/posts";
        final String title = "테스트 타이틀";
        final String content = "테스트 컨텐츠";
        final String postStatus = "PUBLIC";

        postRepository.save(Post.builder().title(title).content(content).postStatus(Post.PostStatus.valueOf(postStatus)).build());

//        when
        final ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders.get(url).accept(MediaType.APPLICATION_JSON));

        resultActions.andExpect(MockMvcResultMatchers.status().isOk()).andExpect(jsonPath("$[0].title").value(title))
                .andExpect(jsonPath("$[0].content").value(content));

    }

    @Transactional
    @DisplayName("findPost: 게시글 조회 테스트")
    @Test
    public void findPost() throws Exception {
        final String url = "/api/posts/{id}";
        final String title = "게시글 조회 테스트 타이틀 값";
        final String content = "게시글 조회 테스트 컨텐츠 값";
        final String postStatus = "PRIVATE";

        Post savePost = postRepository.save(Post.builder()
                .title(title)
                .content(content)
                .postStatus(Post.PostStatus.valueOf(postStatus))
                .build());

        final ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders.get(url, savePost.getId()));

        resultActions.andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(jsonPath("$.content").value(content))
                .andExpect(jsonPath("$.title").value(title));
    }

    @Transactional
    @DisplayName("deletePost: 블로그 글 삭제 테스트")
    @Test
    public void deletePost() throws Exception {

//        given
        final String url = "/api/posts/{id}";
        final String title = "테스트용 제목";
        final String content = "테스트용 내용";
        final String postStatus = "PRIVATE";

        Post savedPost = postRepository.save(Post.builder()
                .title(title)
                .content(content)
                .postStatus(Post.PostStatus.valueOf(postStatus))
                .build());

//        when
        mockMvc.perform(MockMvcRequestBuilders.delete(url, savedPost.getId())).andExpect(MockMvcResultMatchers.status().isOk());
//        then

        Optional<Post> deletedPost = postRepository.findById(savedPost.getId());
        assertThat(deletedPost).isEmpty();
    }
}


