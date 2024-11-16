package com.yhs.blog.springboot.jpa.repository;

import com.yhs.blog.springboot.jpa.domain.post.entity.Post;
import com.yhs.blog.springboot.jpa.domain.post.repository.PostRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class PostRepositoryTest {

    @Autowired
    private PostRepository postRepository;


    @BeforeEach
    public void deleteAll() {
        postRepository.deleteAll();
    }


    @Test
    @Transactional
    public void testSave() {
//        given
        Post post = Post.builder().title("Test Title").content("Test Content").postStatus(Post.PostStatus.PUBLIC).build();

//        when
        Post savePost = postRepository.save(post);
        Optional<Post> foundPost = postRepository.findById(savePost.getId());

//        then
        assertThat(foundPost).isPresent();
        assertThat(foundPost.get().getTitle()).isEqualTo("Test Title");
    }


    @Test
    @Transactional
    public void testFindAll() {
        // given
        Post post1 = Post.builder().title("First Post").content("Content for the first post").postStatus(Post.PostStatus.PUBLIC).build();
        Post post2 = Post.builder().title("Second Post").content("Content for the second post").postStatus(Post.PostStatus.PUBLIC).build();

        postRepository.save(post1);
        postRepository.save(post2);

        // when
        List<Post> posts = postRepository.findAll();

        // then
        assertThat(posts).hasSize(2);
        assertThat(posts.get(0).getTitle()).isEqualTo("First Post");
        assertThat(posts.get(0).getContent()).isEqualTo("Content for the first post");
        assertThat(posts.get(1).getTitle()).isEqualTo("Second Post");
        assertThat(posts.get(1).getContent()).isEqualTo("Content for the second post");
    }


    @Test
    @Transactional
    public void testFindById() {

//        given
        Post requestPost = Post.builder().title("First Post").content("Content for the first post").postStatus(Post.PostStatus.PUBLIC).build();
        Post responsePost = postRepository.save(requestPost);

//        when
        postRepository.findById(responsePost.getId());

//        then
        assertThat(responsePost.getTitle()).isEqualTo("First Post");
        assertThat(responsePost.getContent()).isEqualTo("Content for the first post");
    }

    @Test
    @Transactional
    public void testDeletePost() {

//        given
        Post requestPost = Post.builder().title("First Post").content("Content for the first post").postStatus(Post.PostStatus.PUBLIC).build();
        Post responsePost = postRepository.save(requestPost);

//        when
        postRepository.deleteById(responsePost.getId());
        Optional<Post> post = postRepository.findById(responsePost.getId());

//        then
        assertThat(post).isEmpty();

    }
}