package com.yhs.blog.springboot.jpa.service;

import com.yhs.blog.springboot.jpa.dto.PostRequest;
import com.yhs.blog.springboot.jpa.entity.Post;
import com.yhs.blog.springboot.jpa.repository.PostRepository;
import com.yhs.blog.springboot.jpa.service.impl.PostServiceImpl;
import com.yhs.blog.springboot.jpa.util.PostMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PostServiceTest {

    @Mock
    private PostRepository postRepository;

    @InjectMocks
    private PostServiceImpl postService;

    @Test
    public void testCreatePost() {

        // Given
        PostRequest postRequest = PostRequest.builder().title("Test Title").content("Test Content").postStatus("PUBLIC").build();

        Post post = PostMapper.toEntity(null, null, "Test Title", "Test Content", "PUBLIC");

        // Mockito가 postRepository.save() 메서드가 호출될 때(postService.createPost()를 실행하면 save()메서드가 호출됨.),
        // 인자로 어떤 Post 객체가 전달되더라도 즉 상관없이 post 객체를 반환하도록 설정
        when(postRepository.save(any(Post.class))).thenReturn(post);

        // when
        Post createdPost = postService.createPost(postRequest);

        // then
        assertThat(createdPost).isNotNull();
        assertThat(createdPost.getTitle()).isEqualTo("Test Title");
        assertThat(createdPost.getContent()).isEqualTo("Test Content");
        assertThat(createdPost.getPostStatus()).isEqualTo(createdPost.getPostStatus());
    }

    @Test
    public void testGetAllPosts() {
//        given
        Post post1 = PostMapper.toEntity(null, null, "Title 1", "Content 1", "PUBLIC");
        Post post2 = PostMapper.toEntity(null, null, "Title 2", "Content 2", "PUBLIC");

        when(postRepository.findAll()).thenReturn(Arrays.asList(post1, post2));

        // When
        List<Post> posts = postService.getList();

        // Then
        assertThat(posts).isNotNull();
        assertThat(posts).hasSize(2);
        assertThat(posts.get(0).getTitle()).isEqualTo("Title 1");
        assertThat(posts.get(1).getTitle()).isEqualTo("Title 2");
    }

    @Test
    public void testGetPost() {
        Long id = 1L;
        Post post = PostMapper.toEntity(null, null, "Test Title", "Test Content", "PUBLIC");

        when(postRepository.findById(id)).thenReturn(Optional.of(post));

        // When
        Post foundPost = postService.getPost(id);

        // Then
        assertThat(foundPost).isNotNull();
        assertThat(foundPost.getTitle()).isEqualTo("Test Title");
        assertThat(foundPost.getContent()).isEqualTo("Test Content");
    }

    @Test
    public void testDeletePost() {
        // Given
        Long postId = 1L;
        Post post = Post.builder()
                .title("Test Title")
                .content("Test Content")
                .postStatus(Post.PostStatus.PUBLIC)
                .build();

        when(postRepository.findById(postId)).thenReturn(Optional.of(post));

//        When
        postService.deletePost(postId);

//        Then
        verify(postRepository).deleteById(postId);

//        when
        when(postRepository.findById(postId)).thenReturn(Optional.empty());

//        then
        assertThat(postRepository.findById(postId)).isEmpty();
    }
}