package com.yhs.blog.springboot.jpa.domain.post.controller;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.SearchResponse;

import com.yhs.blog.springboot.jpa.domain.category.entity.Category;
import com.yhs.blog.springboot.jpa.domain.category.repository.CategoryRepository;
import com.yhs.blog.springboot.jpa.domain.post.config.TestElasticsearchContainerConfig;
import com.yhs.blog.springboot.jpa.domain.post.dto.response.PostResponse;
import com.yhs.blog.springboot.jpa.domain.post.entity.Post;
import com.yhs.blog.springboot.jpa.domain.post.entity.enums.CommentsEnabled;
import com.yhs.blog.springboot.jpa.domain.post.entity.enums.PostStatus;
import com.yhs.blog.springboot.jpa.domain.post.event.elasticsearch.PostCreatedEvent;
import com.yhs.blog.springboot.jpa.domain.post.event.elasticsearch.PostDeletedEvent;
import com.yhs.blog.springboot.jpa.domain.post.repository.PostRepository;
import com.yhs.blog.springboot.jpa.domain.post.repository.search.SearchType;
import com.yhs.blog.springboot.jpa.domain.post.repository.search.document.PostDocument;
import com.yhs.blog.springboot.jpa.domain.post.service.PostService;
import com.yhs.blog.springboot.jpa.domain.user.entity.User;
import com.yhs.blog.springboot.jpa.domain.user.factory.TestUserFactory;
import com.yhs.blog.springboot.jpa.domain.user.repository.UserRepository;
import lombok.extern.log4j.Log4j2;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionTemplate;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@Log4j2
// After All을 Public으로 실행하기 위해. 각각의 테스트 메서드는 독립적으로 실행되어서 전체 테스트에서 Public으로 After
// All이 되려면 이 어노테이션 필요
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Import(TestElasticsearchContainerConfig.class)
public class PostSearchIntegrationTest {

        @Autowired
        private PostService postService;

        @Autowired
        private UserRepository userRepository;

        @Autowired
        private PostRepository postRepository;

        @Autowired
        private CategoryRepository categoryRepository;

        @Autowired
        private ElasticsearchClient elasticsearchClient;

        @Autowired
        private PlatformTransactionManager transactionManager;

        @Autowired
        private ApplicationEventPublisher eventPublisher;

        private User testUser;

        @BeforeAll
        public void setUpTestData() throws InterruptedException, IOException {

                // TransactionTemplate.execute() 가 트랜잭션 제공
                // 따라서 save 메서드와 이벤트 발생 모두 같은 트랜잭션 내에서 실행되기 때문에 이벤트 발동 가능
                new TransactionTemplate(transactionManager).execute(status -> {
                        // 테스트 데이터 생성
                        User user = TestUserFactory.createTestUser();
                        testUser = userRepository.save(user);
                        Category savedCategory = categoryRepository
                                        .save(Category.builder().id("test-category-id").name("테스트 카테고리").orderIndex(1L)
                                                        .user(testUser).build());

                        List<Post> postsArr = new ArrayList<>();
                        for (int i = 1; i <= 35; i++) {
                                postsArr.add(Post.builder()
                                                .category(savedCategory)
                                                .title(i <= 15 ? "검색용 게시글 " + i : "일반 게시글 " + i)
                                                .content("내용 " + i)
                                                .user(testUser)
                                                .commentsEnabled(CommentsEnabled.ALLOW)
                                                .postStatus(PostStatus.PUBLIC)
                                                .build());
                        }

                        for (Post post : postsArr) {

                                Post savedPost = postRepository.save(post);
                                TransactionSynchronizationManager.registerSynchronization(
                                                new TransactionSynchronization() {

                                                        @Override
                                                        public void afterCommit() {
                                                                eventPublisher.publishEvent(
                                                                                new PostCreatedEvent(savedPost));
                                                        }

                                                });
                        }

                        return null;
                });
                // ES 인덱싱 완료 대기
                Thread.sleep(2000);
                // // 인덱스 리프레시
                elasticsearchClient.indices().refresh(r -> r.index("posts"));
        }

        @Test
        @Transactional
        public void 검색없이_첫페이지_조회() {

                // given
                PageRequest pageRequest = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdAt"));

                // when
                Page<PostResponse> result = postService.getPosts(testUser.getId(), null, null, null, pageRequest);

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
                Page<PostResponse> result = postService.getPosts(testUser.getId(), null,
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
                Page<PostResponse> result = postService.getPosts(testUser.getId(),
                                searchKeyword, searchType, null,
                                pageRequest);

                // then
                assertThat(result.getContent()).isNotEmpty();
                assertThat(result.getTotalElements()).isGreaterThan(0);
                assertThat(result.getContent()).anyMatch(post -> post.getTitle().contains(searchKeyword) ||
                                post.getContent().contains(searchKeyword));
                // Elasticsearch 검색 확인
                SearchResponse<PostDocument> searchResponse = elasticsearchClient.search(s -> s
                                .index("posts")
                                .query(q -> q
                                                .bool(b -> b
                                                                .must(m -> m // userId 필터링 추가
                                                                                .term(t -> t
                                                                                                .field("userId")
                                                                                                .value(String.valueOf(
                                                                                                                testUser.getId()))))
                                                                .should(sh -> sh
                                                                                .match(m -> m
                                                                                                .field("title.ngram")
                                                                                                .query(searchKeyword)))
                                                                .should(sh -> sh
                                                                                .match(m -> m
                                                                                                .field("content.ngram")
                                                                                                .query(searchKeyword)))
                                                                .minimumShouldMatch("1"))),
                                PostDocument.class);

                assertThat(searchResponse.hits().total().value())
                                .isEqualTo(result.getTotalElements());

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
                Page<PostResponse> result = postService.getPosts(testUser.getId(),
                                searchKeyword, searchType, null,
                                pageRequest);

                // then
                assertThat(result.getContent()).isNotEmpty();
                assertThat(result.getContent())
                                .allMatch(post -> post.getTitle().contains(searchKeyword));

                // Elasticsearch 검색 확인
                SearchResponse<PostDocument> searchResponse = elasticsearchClient.search(s -> s
                                .index("posts")
                                .query(q -> q
                                                .bool(b -> b
                                                                .must(m -> m
                                                                                .term(t -> t
                                                                                                .field("userId")
                                                                                                .value(String.valueOf(
                                                                                                                testUser.getId()))))
                                                                .must(m -> m
                                                                                .match(mt -> mt
                                                                                                .field("title.ngram")
                                                                                                .query(searchKeyword))))),
                                PostDocument.class);

                assertThat(searchResponse.hits().total().value())
                                .isEqualTo(result.getTotalElements());

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
                Page<PostResponse> result = postService.getPosts(testUser.getId(),
                                searchKeyword, searchType, null,
                                pageRequest);

                // then
                assertThat(result.getContent()).isNotEmpty();
                assertThat(result.getContent())
                                .allMatch(post -> post.getContent().contains(searchKeyword));

                // Elasticsearch 검색 확인
                SearchResponse<PostDocument> searchResponse = elasticsearchClient.search(s -> s
                                .index("posts")
                                .query(q -> q
                                                .bool(b -> b
                                                                .must(m -> m
                                                                                .term(t -> t
                                                                                                .field("userId")
                                                                                                .value(String.valueOf(
                                                                                                                testUser.getId()))))
                                                                .must(m -> m
                                                                                .match(mt -> mt
                                                                                                .field("content.ngram")
                                                                                                .query(searchKeyword))))),
                                PostDocument.class);

                assertThat(searchResponse.hits().total().value())
                                .isEqualTo(result.getTotalElements());
        }

        @Test
        @Transactional
        public void ngram_한글자_검색어_처리_확인() throws IOException {

                // given
                PageRequest pageRequest = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC,
                                "createdAt"));
                String searchKeyword = "내"; // 한 글자 검색어
                SearchType searchType = SearchType.ALL;

                // when
                Page<PostResponse> result = postService.getPosts(testUser.getId(),
                                searchKeyword, searchType, null,
                                pageRequest);

                // then
                assertThat(result.getContent()).isNotEmpty();
                assertThat(result.getContent())
                                .anyMatch(post -> post.getTitle().contains("검색용"));

                // Elasticsearch 직접 검색으로 확인
                SearchResponse<PostDocument> searchResponse = elasticsearchClient.search(s -> s
                                .index("posts")
                                .query(q -> q
                                                .bool(b -> b
                                                                .must(m -> m
                                                                                .term(t -> t
                                                                                                .field("userId")
                                                                                                .value(String.valueOf(
                                                                                                                testUser.getId()))))
                                                                .should(sh -> sh
                                                                                .match(m -> m
                                                                                                .field("title.ngram")
                                                                                                .query(searchKeyword)))
                                                                .should(sh -> sh
                                                                                .match(m -> m
                                                                                                .field("content.ngram")
                                                                                                .query(searchKeyword)))
                                                                .minimumShouldMatch("1"))),
                                PostDocument.class);

                assertThat(searchResponse.hits().total().value()).isGreaterThan(0);
        }

        @Test
        @Transactional
        public void 카테고리_네번째페이지_조회() throws IOException {
                // given
                PageRequest pageRequest = PageRequest.of(3, 10, Sort.by(Sort.Direction.DESC, "createdAt"));
                String categoryId = "test-category-id"; // 테스트용 카테고리 ID
                String categoryName = "테스트 카테고리";

                // when
                Page<PostResponse> result = postService.getPosts(testUser.getId(), null, null, categoryId, pageRequest);

                // then
                assertThat(result.getContent()).isNotEmpty();
                assertThat(result.getNumber()).isEqualTo(3); // 현재 페이지 번호가 1인지 확인
                assertThat(result.getSize()).isEqualTo(10); // 페이지 크기가 10인지 확인
                assertThat(result.getContent()).hasSize(5); // 마지막 페이지는 5개의 항목만 있어야 함
                // 모든 게시글이 해당 카테고리에 속하는지 확인
                assertThat(result.getContent())
                                .allMatch(post -> post.getCategoryName().equals(categoryName));

                // Elasticsearch 검색 확인
                SearchResponse<PostDocument> searchResponse = elasticsearchClient.search(s -> s
                                .index("posts")
                                .query(q -> q
                                                .bool(b -> b
                                                                .must(m -> m
                                                                                .term(t -> t
                                                                                                .field("userId")
                                                                                                .value(String.valueOf(
                                                                                                                testUser.getId()))))
                                                                .must(m -> m
                                                                                .term(t -> t
                                                                                                .field("categoryId")
                                                                                                .value(categoryId))))),
                                PostDocument.class);

                assertThat(searchResponse.hits().total().value())
                                .isEqualTo(result.getTotalElements());
        }

        @Test
        @Transactional
        public void 카테고리내_검색_두번째페이지() throws IOException {
                // given
                PageRequest pageRequest = PageRequest.of(1, 10, Sort.by(Sort.Direction.DESC, "createdAt"));
                String categoryId = "test-category-id";
                String categoryName = "테스트 카테고리";
                String searchKeyword = "색";
                SearchType searchType = SearchType.ALL;

                // when
                Page<PostResponse> result = postService.getPosts(testUser.getId(), searchKeyword, searchType,
                                categoryId, pageRequest);

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

                // Elasticsearch 검색 확인
                SearchResponse<PostDocument> searchResponse = elasticsearchClient.search(s -> s
                                .index("posts")
                                .query(q -> q
                                                .bool(b -> b
                                                                .must(m -> m
                                                                                .term(t -> t
                                                                                                .field("userId")
                                                                                                .value(String.valueOf(
                                                                                                                testUser.getId()))))
                                                                .must(m -> m
                                                                                .term(t -> t
                                                                                                .field("categoryId")
                                                                                                .value(categoryId)))
                                                                .should(sh -> sh
                                                                                .match(m -> m
                                                                                                .field("title.ngram")
                                                                                                .query(searchKeyword)))
                                                                .should(sh -> sh
                                                                                .match(m -> m
                                                                                                .field("content.ngram")
                                                                                                .query(searchKeyword)))
                                                                .minimumShouldMatch("1"))),
                                PostDocument.class);

                assertThat(searchResponse.hits().total().value())
                                .isEqualTo(result.getTotalElements());
        }

        @AfterAll
        public void cleanUp() throws InterruptedException, IOException {

                // TransactionTemplate.execute() 가 트랜잭션 제공
                // 따라서 delete 메서드와 이벤트 발생 모두 같은 트랜잭션 내에서 실행되기 때문에 이벤트 발동 가능
                new TransactionTemplate(transactionManager).execute(status -> {
                        List<Post> posts = postRepository.findAll();

                        for (Post post : posts) {

                                postRepository.delete(post);
                                TransactionSynchronizationManager.registerSynchronization(
                                                new TransactionSynchronization() {

                                                        @Override
                                                        public void afterCommit() {
                                                                eventPublisher.publishEvent(new PostDeletedEvent(post));
                                                        }

                                                });
                        }

                        categoryRepository.deleteAll();
                        userRepository.deleteAll();
                        return null;
                });

                // ES 인덱싱 완료 대기
                Thread.sleep(2000);
                SearchResponse<PostDocument> response = elasticsearchClient.search(s -> s
                                .index("posts"),
                                PostDocument.class);
                log.info("삭제 후 남은 문서 수: {}", response.hits().total().value());

        }

}
