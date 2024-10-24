//package com.yhs.blog.springboot.jpa.service;
//
//import com.yhs.blog.springboot.jpa.dto.PostRequest;
//import com.yhs.blog.springboot.jpa.dto.PostResponse;
//import com.yhs.blog.springboot.jpa.entity.Post;
//import com.yhs.blog.springboot.jpa.entity.User;
//import com.yhs.blog.springboot.jpa.repository.CategoryRepository;
//import com.yhs.blog.springboot.jpa.repository.PostRepository;
//import com.yhs.blog.springboot.jpa.service.impl.PostServiceImpl;
//import com.yhs.blog.springboot.jpa.service.impl.UserServiceImpl;
//import com.yhs.blog.springboot.jpa.util.PostMapper;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.Mockito;
//import org.mockito.junit.jupiter.MockitoExtension;
//import org.springframework.security.core.Authentication;
//import org.springframework.security.oauth2.core.user.OAuth2User;
//
//import java.security.Principal;
//import java.util.Arrays;
//import java.util.List;
//import java.util.Map;
//import java.util.Optional;
//
//import static org.mockito.ArgumentMatchers.any;
//import static org.assertj.core.api.Assertions.assertThat;
//import static org.mockito.Mockito.*;
//
//@ExtendWith(MockitoExtension.class)
//class PostServiceTest {
//
//    @InjectMocks
//    private PostServiceImpl postService;
//
//    @Mock
//    private PostRepository postRepository;
//
//    @Mock
//    private UserServiceImpl userService;
//
//    @Mock
//    private CategoryRepository categoryRepository;
//
//    @Mock
//    private Principal principal;
//
//    @Mock
//    private Authentication authentication;
//
//    @Mock
//    private OAuth2User oAuth2User;
//
//    @Test
//    @DisplayName("OAuth2 User 포스트 작성 테스트")
//    public void testCreatePostWithOAuth2Login() {
//
//        // Given
//        PostRequest postRequest = PostRequest.builder().title("Test Title").content("Test Content").postStatus("PUBLIC").build();
//
//        User user = User.builder().email("testuser@test.com").password("password").username("testuser").build();
//
//        Post post = PostMapper.toEntity(user, null, "Test Title", "Test Content", "PUBLIC");
//
//        when(principal instanceof Authentication).thenReturn(true);
//        when(((Authentication) principal).getPrincipal()).thenReturn(oAuth2User);
//        when(oAuth2User.getAttributes()).thenReturn(Map.of("email", "testuser@test.com"));
//
//        when(userService.findUserByEmail("testuser@test.com")).thenReturn(user);
//
//        // Mockito가 postRepository.save() 메서드가 호출될 때(postService.createPost()를 실행하면 save()메서드가 호출됨.),
//        // 인자로 어떤 Post 객체가 전달되더라도 즉 상관없이 post 객체를 반환하도록 설정
//        when(postRepository.save(any(Post.class))).thenReturn(post);
//
//        // when
//        PostResponse createdPost = postService.createPost(postRequest, principal);
//
//        // then
//        assertThat(createdPost).isNotNull();
//        assertThat(createdPost.getTitle()).isEqualTo("Test Title");
//        assertThat(createdPost.getContent()).isEqualTo("Test Content");
//        assertThat(createdPost.getPostStatus()).isEqualTo("PUBLIC");
//    }
//
//
//    @Test
//    @DisplayName("폼 로그인 User 포스트 작성 테스트")
//    public void testCreatePostWithFormLogin() {
//
//        // Given
//        PostRequest postRequest = PostRequest.builder().title("Test Title").content("Test Content").postStatus("PUBLIC").build();
//
//        User user = User.builder().email("testuser@test.com").password("password").username("testuser").build();
//
//        Post post = PostMapper.toEntity(user, null, "Test Title", "Test Content", "PUBLIC");
//
//        authentication = Mockito.mock(Authentication.class);
//
//        when(principal).thenReturn(authentication);
//        when(authentication.getPrincipal()).thenReturn(user);
//        when(authentication.getName()).thenReturn("testuser@test.com"); // Authentication에서 사용자 이름 반환
//
//        when(userService.findUserByEmail("testuser@test.com")).thenReturn(user);
//
//        // Mockito가 postRepository.save() 메서드가 호출될 때(postService.createPost()를 실행하면 save()메서드가 호출됨.),
//        // 인자로 어떤 Post 객체가 전달되더라도 즉 상관없이 post 객체를 반환하도록 설정
//        when(postRepository.save(any(Post.class))).thenReturn(post);
//
//        // when
//        PostResponse createdPost = postService.createPost(postRequest, principal);
//
//        // then
//        assertThat(createdPost).isNotNull();
//        assertThat(createdPost.getTitle()).isEqualTo("Test Title");
//        assertThat(createdPost.getContent()).isEqualTo("Test Content");
//        assertThat(createdPost.getPostStatus()).isEqualTo("PUBLIC");
//    }
//
//    @Test
//    public void testGetAllPosts() {
////        given
//        Post post1 = PostMapper.toEntity(null, null, "Title 1", "Content 1", "PUBLIC");
//        Post post2 = PostMapper.toEntity(null, null, "Title 2", "Content 2", "PUBLIC");
//
//        when(postRepository.findAll()).thenReturn(Arrays.asList(post1, post2));
//
//        // When
//        List<Post> posts = postService.getList();
//
//        // Then
//        assertThat(posts).isNotNull();
//        assertThat(posts).hasSize(2);
//        assertThat(posts.get(0).getTitle()).isEqualTo("Title 1");
//        assertThat(posts.get(1).getTitle()).isEqualTo("Title 2");
//    }
//
//    @Test
//    public void testGetPost() {
//        Long id = 1L;
//        Post post = PostMapper.toEntity(null, null, "Test Title", "Test Content", "PUBLIC");
//
//        when(postRepository.findById(id)).thenReturn(Optional.of(post));
//
//        // When
//        Post foundPost = postService.getPost(id);
//
//        // Then
//        assertThat(foundPost).isNotNull();
//        assertThat(foundPost.getTitle()).isEqualTo("Test Title");
//        assertThat(foundPost.getContent()).isEqualTo("Test Content");
//    }
//
//    @Test
//    public void testDeletePost() {
//        // Given
//        Long postId = 1L;
//        Post post = Post.builder()
//                .title("Test Title")
//                .content("Test Content")
//                .postStatus(Post.PostStatus.PUBLIC)
//                .build();
//
//        when(postRepository.findById(postId)).thenReturn(Optional.of(post));
//
////        When
//        postService.deletePost(postId);
//
////        Then
//        verify(postRepository).deleteById(postId);
//
////        when
//        when(postRepository.findById(postId)).thenReturn(Optional.empty());
//
////        then
//        assertThat(postRepository.findById(postId)).isEmpty();
//    }
//}