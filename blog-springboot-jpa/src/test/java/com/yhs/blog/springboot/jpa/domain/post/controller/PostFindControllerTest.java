package com.yhs.blog.springboot.jpa.domain.post.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import com.yhs.blog.springboot.jpa.domain.post.entity.Post;
import com.yhs.blog.springboot.jpa.domain.post.entity.enums.CommentsEnabled;
import com.yhs.blog.springboot.jpa.domain.post.entity.enums.PostStatus;
import com.yhs.blog.springboot.jpa.domain.post.repository.PostRepository;
import com.yhs.blog.springboot.jpa.domain.user.entity.User;
import com.yhs.blog.springboot.jpa.domain.user.factory.TestUserFactory;
import com.yhs.blog.springboot.jpa.domain.user.repository.UserRepository;
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
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.transaction.annotation.Transactional;

//@SpringBootTest + MockMVC를 사용한 통합 테스트
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
class PostFindControllerTest {

       // @Autowired
       // private WebApplicationContext context;

       // 서블릿 컨테이너 모킹으로 인한 HTTP 요청 흉내, DispatcherServlet을 모방
       // 만약 @AutoConfigureMockMvc를 사용하지 않고 수동 설정을 하는 경우 @Autowired 불필요
       @Autowired
       private MockMvc mockMvc;

       @Autowired
       PostRepository postRepository;

       @Autowired
       private UserRepository userRepository;

       private static User savedUser;
       private static final String TEST_BLOG_ID = "testBlogId";

       @BeforeEach
       public void setUp() {
              // this.mockMvc = MockMvcBuilders
              // .webAppContextSetup(context)
              // .build();
              User testUser = TestUserFactory.createTestUser();
              savedUser = userRepository.save(testUser);

       }

       @DisplayName("특정 사용자의 모든 글 목록 조회 테스트")
       @Test
       public void 특정_사용자의_모든글_조회_테스트() throws Exception {
              // given

              final String title = "테스트 타이틀";
              final String content = "테스트 컨텐츠";

              postRepository.save(
                            Post.builder().user(savedUser).title(title).content(content).postStatus(PostStatus.PUBLIC)
                                          .commentsEnabled(CommentsEnabled.ALLOW).build());

              // when
              // ResultActions 대신 MvcResult도 있는데 ResultActions방식이 가독성 좋고, 체이닝 방식도 있어서 사용 
              ResultActions resultActions = mockMvc
                            .perform(get("/api/{blogId}/posts", TEST_BLOG_ID)
                                          .accept(MediaType.APPLICATION_JSON));

              resultActions.andExpect(MockMvcResultMatchers.status().isOk())
                            .andExpect(jsonPath("$.message").value("게시글 응답에 성공하였습니다."));

       }

       @DisplayName("특정 사용자의 특정 게시글 조회 테스트")
       @Test
       public void 특정_사용자의_특정_게시글_조회_테스트() throws Exception {
              final String title = "게시글 조회 테스트 타이틀 값";
              final String content = "게시글 조회 테스트 컨텐츠 값";

              Post savePost = postRepository.save(Post.builder()
                            .user(savedUser)
                            .title(title)
                            .content(content)
                            .postStatus(PostStatus.PUBLIC).commentsEnabled(CommentsEnabled.ALLOW)
                            .build());

              ResultActions resultActions = mockMvc.perform(
                            get("/api/{blogId}/posts/{id}", TEST_BLOG_ID, savePost.getId()));

              resultActions.andExpect(MockMvcResultMatchers.status().isOk())
                            .andExpect(jsonPath("$.content").value(content))
                            .andExpect(jsonPath("$.title").value(title));
       }

}
