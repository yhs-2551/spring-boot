package com.yhs.blog.springboot.jpa.domain.post.controller;

import static org.junit.Assert.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.transaction.annotation.Transactional;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yhs.blog.springboot.jpa.domain.auth.token.provider.TokenProvider;
import com.yhs.blog.springboot.jpa.domain.post.dto.request.PostRequest;
import com.yhs.blog.springboot.jpa.domain.post.dto.request.PostUpdateRequest;
import com.yhs.blog.springboot.jpa.domain.post.entity.Post;
import com.yhs.blog.springboot.jpa.domain.post.entity.enums.CommentsEnabled;
import com.yhs.blog.springboot.jpa.domain.post.entity.enums.PostStatus;
import com.yhs.blog.springboot.jpa.domain.post.repository.PostRepository;
import com.yhs.blog.springboot.jpa.domain.user.entity.User;
import com.yhs.blog.springboot.jpa.domain.user.factory.TestUserFactory;
import com.yhs.blog.springboot.jpa.domain.user.repository.UserRepository;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
public class PostOperationControllerTest {

        @Autowired
        private ObjectMapper objectMapper;

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        PostRepository postRepository;

        @Autowired
        private UserRepository userRepository;

        @Autowired
        private TokenProvider tokenProvider;

        private static User savedUser;
        private static String accessToken;
        private static final String TEST_BLOG_ID = "testBlogId";

        @BeforeEach
        public void setUp() {

                User testUser = TestUserFactory.createTestUser();
                savedUser = userRepository.save(testUser);

                // 액세스 토큰 생성
                accessToken = tokenProvider.generateToken(savedUser, Duration.ofHours(1));

                // Security Context 설정

                // SecurityContext context = SecurityContextHolder.createEmptyContext();

                // context.setAuthentication(authenticationProvider.getAuthentication(accessToken));

                // SecurityContextHolder.setContext(context);

        }

        @DisplayName("특정 사용자의 블로그 글 작성 성공 테스트")
        @Test
        public void 특정_사용자의_글작성_테스트() throws Exception {

                // given
                final String title = "테스트 타이틀";
                final String content = "테스트 컨텐츠";
                final String postStatus = "PRIVATE";
                final String commentsEnabled = "ALLOW";
                final PostRequest postRequest = new PostRequest();
                postRequest.setTitle(title);
                postRequest.setContent(content);
                postRequest.setPostStatus(postStatus);
                postRequest.setCommentsEnabled(commentsEnabled);
                // PostDTO 객체를 JSON 으로 직렬화
                final String requestBody = objectMapper.writeValueAsString(postRequest);

                // when
                ResultActions result = mockMvc.perform(post("/api/{blogId}/posts", TEST_BLOG_ID)
                                .header("Authorization", "Bearer " + accessToken)
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .content(requestBody));

                // then
                result.andExpect(MockMvcResultMatchers.status().isCreated());

                List<Post> posts = postRepository.findAll();

                assertThat(posts.size()).isEqualTo(1);
                assertThat(posts.get(0).getTitle()).isEqualTo(title);
                assertThat(posts.get(0).getContent()).isEqualTo(content);

        }

        @Transactional
        @DisplayName("특정 사용자의 게시글 삭제 테스트")
        @Test
        public void 특정_사용자의_게시글_삭제_테스트() throws Exception {

                // given
                final String title = "테스트용 제목";
                final String content = "테스트용 내용";

                Post savedPost = postRepository.save(Post.builder()
                                .userId(savedUser.getId())
                                .title(title)
                                .content(content)
                                .postStatus(PostStatus.PUBLIC).commentsEnabled(CommentsEnabled.ALLOW)
                                .build());

                // when
                mockMvc.perform(MockMvcRequestBuilders
                                .delete("/api/{blogId}/posts/{postId}", TEST_BLOG_ID, savedPost.getId())
                                .header("Authorization", "Bearer " + accessToken));

                // then

                Optional<Post> deletedPost = postRepository.findById(savedPost.getId());
                assertThat(deletedPost).isEmpty();
        }

        @Transactional
        @DisplayName("특정 사용자의 게시글 수정 테스트")
        @Test
        public void 특정_사용자의_게시글_수정_테스트() throws Exception {

                // given

                final String title = "글 제목";
                final String content = "글 내용";

                Post savedPost = postRepository.save(Post.builder().userId(savedUser.getId()).title(title)
                                .content(content)
                                .postStatus(PostStatus.PUBLIC).commentsEnabled(CommentsEnabled.ALLOW).build());

                final String newTitle = "글 수정 테스트 제목";
                final String newContent = "글 수정 테스트 내용";
                final String newPostStatus = "PUBLIC";
                final String newCommentsEnabled = "ALLOW";

                PostUpdateRequest postUpdateRequest = new PostUpdateRequest(null, newTitle, newContent, null, null,
                                null,
                                null, newPostStatus, newCommentsEnabled, null);

                // when
                mockMvc.perform(MockMvcRequestBuilders
                                .patch("/api/{blogId}/posts/{postId}", TEST_BLOG_ID, savedPost.getId())
                                .header("Authorization", "Bearer " + accessToken)
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .content(objectMapper.writeValueAsString(postUpdateRequest)));

                // then

                Post post = postRepository.findById(savedPost.getId()).get();

                assertThat(post.getTitle()).isEqualTo(newTitle);
                assertThat(post.getContent()).isEqualTo(newContent);

        }
}
