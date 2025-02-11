package com.yhs.blog.springboot.jpa.domain.post.repository;

import com.yhs.blog.springboot.jpa.config.TestQueryDslConfig;
import com.yhs.blog.springboot.jpa.domain.post.entity.Post; 
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
    @DisplayName("PostRepository의 대표 이미지 ID 조회 메서드 테스트")
    public void findFeaturedImageIdByPostId_메서드_테스트() {
        // given
        List<User> users = TestUserFactory.createMultipleTestUsers(2);
        userRepository.saveAll(users);

        List<Post> posts = new ArrayList<>();
        Long featuredImageId1 = 1L;
        Long featuredImageId2 = 2L;

        Post post1 = TestPostFactory.createTestPost(users.get(0).getId());
        post1.setFeaturedImageId(featuredImageId1);
        posts.add(post1);

        Post post2 = TestPostFactory.createTestPost(users.get(1).getId());
        post2.setFeaturedImageId(featuredImageId2);
        posts.add(post2);

        postRepository.saveAll(posts);

        // when
        Long foundFeaturedImageId1 = postRepository.findFeaturedImageIdByPostId(posts.get(0).getId());
        Long foundFeaturedImageId2 = postRepository.findFeaturedImageIdByPostId(posts.get(1).getId());

        // then
        assertThat(foundFeaturedImageId1).isEqualTo(featuredImageId1);
        assertThat(foundFeaturedImageId2).isEqualTo(featuredImageId2);
    }
}