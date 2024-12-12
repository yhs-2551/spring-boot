package com.yhs.blog.springboot.jpa.domain.post.repository;

import com.yhs.blog.springboot.jpa.domain.post.entity.Post;
import com.yhs.blog.springboot.jpa.domain.user.entity.User;
import com.yhs.blog.springboot.jpa.domain.user.factory.TestUserFactory;
import com.yhs.blog.springboot.jpa.domain.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest // @transactional 포함
// Replace.NONE: 설정된 실제 DB 사용, ANY: 내장 메모리 DB 사용(기본값)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test") // application-test.yml/properties 설정 사용
class PostRepositoryTest {

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private UserRepository userRepository;


    private User user;

    @BeforeEach
    public void setUp() {
        user = TestUserFactory.createTestUser();
        userRepository.save(user);

    }

    @Nested
    @DisplayName("Post Repository 테스트")
    class JPACRDTest {
        @Test
        @DisplayName("로그인된 특정 사용자가 게시글을 작성한다.")
        public void testSave() {

//        given
            Post post =
                    Post.builder().title("Test Title").content("Test Content").postStatus(Post.PostStatus.PUBLIC).user(user).commentsEnabled(Post.CommentsEnabled.ALLOW).build();

//        when
            Post savePost = postRepository.save(post);
            Optional<Post> foundPost = postRepository.findById(savePost.getId());

//        then
            assertThat(foundPost).isPresent();
            assertThat(foundPost.get().getTitle()).isEqualTo("Test Title");
            assertThat(foundPost.get().getContent()).isEqualTo("Test Content");
            assertThat(foundPost.get().getUser().getId()).isEqualTo(user.getId());
        }


        @Test
        @DisplayName("특정 사용자의 게시글 전체를 조회한다.")
        public void testFindAll() {

            // given
            List<User> users = TestUserFactory.createMultipleTestUsers(2);
            userRepository.saveAll(users);

            Post post1 =
                    Post.builder().title("First Post").content("Content for the first post").postStatus(Post.PostStatus.PUBLIC).user(users.getFirst()).commentsEnabled(Post.CommentsEnabled.ALLOW).build();
            Post post2 =
                    Post.builder().title("Second Post").content("Content for the second post").postStatus(Post.PostStatus.PUBLIC).user(users.getFirst()).commentsEnabled(Post.CommentsEnabled.ALLOW).build();
            Post post3 =
                    Post.builder().title("Third Post").content("Content for the third post").postStatus(Post.PostStatus.PUBLIC).user(users.getLast()).commentsEnabled(Post.CommentsEnabled.ALLOW).build();

            postRepository.save(post1);
            postRepository.save(post2);
            postRepository.save(post3);

            // when
            List<Post> postsWithFirstUser = postRepository.findByUserId(users.getFirst().getId());
            List<Post> postsWithSecondUser = postRepository.findByUserId(users.getLast().getId());

            // then
            assertThat(postsWithFirstUser).hasSize(2);
            assertThat(postsWithFirstUser.get(0).getTitle()).isEqualTo("First Post");
            assertThat(postsWithFirstUser.get(0).getContent()).isEqualTo("Content for the first post");
            assertThat(postsWithFirstUser.get(1).getTitle()).isEqualTo("Second Post");
            assertThat(postsWithFirstUser.get(1).getContent()).isEqualTo("Content for the second post");

            assertThat(postsWithSecondUser).hasSize(1);
            assertThat(postsWithSecondUser.getFirst().getTitle()).isEqualTo("Third Post");
            assertThat(postsWithSecondUser.getFirst().getContent()).isEqualTo("Content for the third post");

        }


        @Test
        @DisplayName("post id로 특정 사용자의 게시글을 조회한다.")
        public void testFindById() {

//        given

            Post post =
                    Post.builder().title("First Post").content("Content for the first post").postStatus(Post.PostStatus.PUBLIC).user(user).commentsEnabled(Post.CommentsEnabled.ALLOW).build();

            Post savedPost = postRepository.save(post);
//        when
            Optional<Post> responsePost =
                    postRepository.findById(savedPost.getId());

//        then

            if (responsePost.isPresent()) {
                assertThat(responsePost.get().getId()).isEqualTo(savedPost.getId());
                assertThat(responsePost.get().getTitle()).isEqualTo("First Post");
                assertThat(responsePost.get().getContent()).isEqualTo("Content for the first post");
            }

        }

        @Test
        @DisplayName("post id로 특정 사용자의 게시글을 삭제한다.")
        public void testDeletePost() {

//        given
            Post post = Post.builder().title("First Post").content("Content for the first post").postStatus(Post.PostStatus.PUBLIC).user(user).commentsEnabled(Post.CommentsEnabled.ALLOW).build();

            Long savedPostId = postRepository.save(post).getId();

//        when
            postRepository.deleteById(savedPostId);
            Optional<Post> deletedPost = postRepository.findById(savedPostId);

//        then
            assertThat(deletedPost).isEmpty();

        }
    }
}