package com.yhs.blog.springboot.jpa.service;

import com.yhs.blog.springboot.jpa.dto.PostDTO;
import com.yhs.blog.springboot.jpa.entity.Post;
import com.yhs.blog.springboot.jpa.repository.PostRepository;
import com.yhs.blog.springboot.jpa.service.impl.PostServiceImpl;
import com.yhs.blog.springboot.jpa.util.PostMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.assertj.core.api.Assertions.assertThat;



import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class PostServiceTest {

    @Mock
    private PostRepository postRepository;

    @InjectMocks
    private PostServiceImpl postService;

    @Test
    public void testCreatePost() {

        // Given
        PostDTO postDTO = PostDTO.builder().title("Test Title").content("Test Content").postStatus("PUBLIC").build();

        Post post = PostMapper.toEntity(null, null, "Test Title", "Test Content", "PUBLIC");

        // Mockito가 postRepository.save() 메서드가 호출될 때(postService.createPost()를 실행하면 save()메서드가 호출됨.),
        // 인자로 어떤 Post 객체가 전달되더라도 즉 상관없이 post 객체를 반환하도록 설정
        when(postRepository.save(any(Post.class))).thenReturn(post);

        // when
        Post createdPost = postService.createPost(postDTO);

        // then
        assertThat(createdPost).isNotNull();
        assertThat(createdPost.getTitle()).isEqualTo("Test Title");
        assertThat(createdPost.getContent()).isEqualTo("Test Content");
        assertThat(createdPost.getPostStatus()).isEqualTo(createdPost.getPostStatus());
    }
}