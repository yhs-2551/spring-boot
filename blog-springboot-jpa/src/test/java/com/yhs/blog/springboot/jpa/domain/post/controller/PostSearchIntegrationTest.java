package com.yhs.blog.springboot.jpa.domain.post.controller;

import com.yhs.blog.springboot.jpa.domain.category.entity.Category;
import com.yhs.blog.springboot.jpa.domain.category.repository.CategoryRepository;
import com.yhs.blog.springboot.jpa.domain.post.dto.response.PostUserPageResponse;
import com.yhs.blog.springboot.jpa.domain.post.entity.Post;
import com.yhs.blog.springboot.jpa.domain.post.entity.enums.CommentsEnabled;
import com.yhs.blog.springboot.jpa.domain.post.entity.enums.PostStatus;
import com.yhs.blog.springboot.jpa.domain.post.repository.PostRepository;
import com.yhs.blog.springboot.jpa.domain.post.repository.search.SearchType;
import com.yhs.blog.springboot.jpa.domain.post.service.PostFindService;
import com.yhs.blog.springboot.jpa.domain.user.entity.User;
import com.yhs.blog.springboot.jpa.domain.user.factory.TestUserFactory;
import com.yhs.blog.springboot.jpa.domain.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
// After All을 Public으로 실행하기 위해. 각각의 테스트 메서드는 독립적으로 실행되어서 전체 테스트에서 Public으로 After
// All이 되려면 이 어노테이션 필요
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class PostSearchIntegrationTest {

        @Autowired
        private PostFindService postFindService;

        @Autowired
        private UserRepository userRepository;

        @Autowired
        private PostRepository postRepository;

        @Autowired
        private CategoryRepository categoryRepository;

        private User testUser;

        @BeforeAll
        public void setUpTestData() {

                // TransactionTemplate.execute() 가 트랜잭션 제공
                // 따라서 save 메서드와 이벤트 발생 모두 같은 트랜잭션 내에서 실행되기 때문에 이벤트 발동 가능

                // 테스트 데이터 생성
                User user = TestUserFactory.createTestUser();
                testUser = userRepository.save(user);
                Category savedCategory = categoryRepository
                                .save(Category.builder().name("테스트 카테고리").orderIndex(1L)
                                                .userId(testUser.getId()).build());

                List<Post> postsArr = new ArrayList<>();
                for (int i = 1; i <= 35; i++) {
                        postsArr.add(Post.builder()
                                        .categoryId(savedCategory.getId())
                                        .title(i <= 15 ? "검색용 게시글 " + i : "일반 게시글 " + i)
                                        .content("내용 " + i)
                                        .userId(testUser.getId())
                                        .commentsEnabled(CommentsEnabled.ALLOW)
                                        .postStatus(PostStatus.PUBLIC)
                                        .build());
                }

                postRepository.saveAll(postsArr);

        }

        @Test
        @Transactional
        public void 검색없이_첫페이지_조회() {

                // given
                PageRequest pageRequest = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdAt"));

                // when
                Page<PostUserPageResponse> result = postFindService.getAllPostsSpecificUser(testUser.getBlogId(), null,
                                null, null,
                                pageRequest);

                // then
                assertThat(result.getContent()).hasSize(10);
                assertThat(result.getNumber()).isEqualTo(0);
                assertThat(result.getTotalElements()).isEqualTo(35);
        }

        @Test
        @Transactional
        public void 검색없이_두번째페이지_조회() {
                // given
                PageRequest pageRequest = PageRequest.of(1, 10, Sort.by(Sort.Direction.DESC,
                                "createdAt"));

                // when
                Page<PostUserPageResponse> result = postFindService.getAllPostsSpecificUser(testUser.getBlogId(), null,
                                null, null, pageRequest);

                // then
                assertThat(result.getContent()).hasSize(10);
                assertThat(result.getNumber()).isEqualTo(1);
                assertThat(result.getTotalPages()).isEqualTo(4); // 35개 데이터는 4페이지로 나뉨
        }

        @Test
        @Transactional
        public void 통합검색_제목또는내용으로_검색() throws IOException {

                // given
                PageRequest pageRequest = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC,
                                "createdAt"));
                String searchKeyword = "검";
                SearchType searchType = SearchType.ALL;

                // when
                Page<PostUserPageResponse> result = postFindService.getAllPostsSpecificUser(testUser.getBlogId(),
                                searchKeyword, searchType, null,
                                pageRequest);

                // then
                assertThat(result.getContent()).isNotEmpty();
                assertThat(result.getTotalElements()).isGreaterThan(0);
                assertThat(result.getContent()).anyMatch(post -> post.getTitle().contains(searchKeyword) ||
                                post.getContent().contains(searchKeyword));

        }

        @Test
        @Transactional
        public void 제목으로만_검색() throws IOException {

                // given
                PageRequest pageRequest = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC,
                                "createdAt"));
                String searchKeyword = "게시글";
                SearchType searchType = SearchType.TITLE;

                // when
                Page<PostUserPageResponse> result = postFindService.getAllPostsSpecificUser(testUser.getBlogId(),
                                searchKeyword, searchType, null,
                                pageRequest);

                // then
                assertThat(result.getContent()).isNotEmpty();
                assertThat(result.getContent())
                                .allMatch(post -> post.getTitle().contains(searchKeyword));

        }

        @Test
        @Transactional
        public void 내용으로만_검색() throws IOException {
                // given
                PageRequest pageRequest = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC,
                                "createdAt"));
                String searchKeyword = "내용";
                SearchType searchType = SearchType.CONTENT;

                // when
                Page<PostUserPageResponse> result = postFindService.getAllPostsSpecificUser(testUser.getBlogId(),
                                searchKeyword, searchType, null,
                                pageRequest);

                // then
                assertThat(result.getContent()).isNotEmpty();
                assertThat(result.getContent())
                                .allMatch(post -> post.getContent().contains(searchKeyword));

        }

        @Test
        @Transactional
        public void 카테고리_네번째페이지_조회() throws IOException {
                // given
                PageRequest pageRequest = PageRequest.of(3, 10, Sort.by(Sort.Direction.DESC, "createdAt"));
                String categoryName = "테스트 카테고리";

                // when
                Page<PostUserPageResponse> result = postFindService.getAllPostsSpecificUser(testUser.getBlogId(), null,
                                null,
                                categoryName,
                                pageRequest);

                // then
                assertThat(result.getContent()).isNotEmpty();
                assertThat(result.getNumber()).isEqualTo(3); // 현재 페이지 번호가 1인지 확인
                assertThat(result.getSize()).isEqualTo(10); // 페이지 크기가 10인지 확인
                assertThat(result.getContent()).hasSize(5); // 마지막 페이지는 5개의 항목만 있어야 함
                // 모든 게시글이 해당 카테고리에 속하는지 확인
                assertThat(result.getContent())
                                .allMatch(post -> post.getCategoryName().equals(categoryName));

        }

        @Test
        @Transactional
        public void 카테고리내_검색_두번째페이지() throws IOException {
                // given
                PageRequest pageRequest = PageRequest.of(1, 10, Sort.by(Sort.Direction.DESC, "createdAt"));
                String categoryName = "테스트 카테고리";
                String searchKeyword = "색";
                SearchType searchType = SearchType.ALL;

                // when
                Page<PostUserPageResponse> result = postFindService.getAllPostsSpecificUser(testUser.getBlogId(),
                                searchKeyword,
                                searchType,
                                categoryName, pageRequest);

                // then
                assertThat(result.getContent()).isNotEmpty(); // 검색 결과가 비어있지 않은지 확인
                assertThat(result.getNumber()).isEqualTo(1); // 현재 페이지 번호가 1인지 확인
                assertThat(result.getSize()).isEqualTo(10); // 페이지 크기가 10인지 확인
                // 모든 게시글이 해당 카테고리에 속하는지 확인
                assertThat(result.getContent())
                                .allMatch(post -> post.getCategoryName().equals(categoryName));

                // 검색어가 제목이나 내용에 포함되는지 확인
                assertThat(result.getContent())
                                .anyMatch(post -> post.getTitle().contains(searchKeyword)
                                                || post.getContent().contains(searchKeyword));

        }

}
