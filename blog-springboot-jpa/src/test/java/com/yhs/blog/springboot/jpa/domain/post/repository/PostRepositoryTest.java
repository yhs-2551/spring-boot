package com.yhs.blog.springboot.jpa.domain.post.repository;

import com.yhs.blog.springboot.jpa.config.TestQueryDslConfig;
import com.yhs.blog.springboot.jpa.domain.post.entity.Post;
import com.yhs.blog.springboot.jpa.domain.post.entity.enums.CommentsEnabled;
import com.yhs.blog.springboot.jpa.domain.post.entity.enums.PostStatus;
import com.yhs.blog.springboot.jpa.domain.post.factory.TestPostFactory;
import com.yhs.blog.springboot.jpa.domain.user.entity.User;
import com.yhs.blog.springboot.jpa.domain.user.factory.TestUserFactory;
import com.yhs.blog.springboot.jpa.domain.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest // @transactional 포함
@ActiveProfiles("test") // application-test.yml/properties 설정 사용
@Import(TestQueryDslConfig.class)
class PostRepositoryTest {

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("PostRepository의 커스텀 findByIdWithUser 메서드 테스트")
    public void findByIdWithUser_메서드_테스트() {

        // given
        List<User> users = TestUserFactory.createMultipleTestUsers(2);
        userRepository.saveAll(users);

        List<Post> posts = new ArrayList<>();

        for (int i = 0; i < 2; i++) {
            Post post = TestPostFactory.createTestPost(users.get(i));
            posts.add(post);
        }

        postRepository.saveAll(posts);

        // when
        Optional<Post> postsWithFirstUser = postRepository.findByIdWithUser(posts.get(0).getId());
        Optional<Post> postsWithSecondUser = postRepository.findByIdWithUser(posts.get(1).getId());

        // then
        assertThat(postsWithFirstUser).isNotEmpty();
        assertThat(postsWithFirstUser.get().getUser().getUsername()).isEqualTo("testUser0");

        assertThat(postsWithSecondUser).isNotEmpty();
        assertThat(postsWithSecondUser.get().getUser().getUsername()).isEqualTo("testUser1");

    }

}