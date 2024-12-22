package com.yhs.blog.springboot.jpa.domain.post.event.elasticsearch;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionTemplate;
import com.yhs.blog.springboot.jpa.domain.post.entity.Post;
import com.yhs.blog.springboot.jpa.domain.post.entity.enums.CommentsEnabled;
import com.yhs.blog.springboot.jpa.domain.post.entity.enums.PostStatus;
import com.yhs.blog.springboot.jpa.domain.post.repository.PostRepository;
import com.yhs.blog.springboot.jpa.domain.post.repository.search.PostSearchRepository;
import com.yhs.blog.springboot.jpa.domain.user.entity.User;
import com.yhs.blog.springboot.jpa.domain.user.factory.TestUserFactory;
import com.yhs.blog.springboot.jpa.domain.user.repository.UserRepository;
import com.yhs.blog.springboot.jpa.exception.custom.ElasticsearchCustomException;
import lombok.extern.log4j.Log4j2;
import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@Log4j2
public class PostEventHandlerESAsyncErrorTest {

    @MockitoBean
    private PostSearchRepository postSearchRepository;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PostEventHandler postEventHandler;

    @Test
    @DisplayName("ES 저장 3회 재시도 실패 후 예외 발생 검증")
    void handlePostCreated_WhenExceedsMaxRetries_ShouldThrowException() throws Exception {
        // given
        AtomicInteger attempts = new AtomicInteger();
        AtomicReference<Throwable> caughtException = new AtomicReference<>();

        // mock 설정을 트랜잭션 밖으로
        doAnswer(invocation -> {
            int count = attempts.incrementAndGet();
            log.info("ES 저장 시도 {} 회차 실패", count);
            throw new RuntimeException("ES 저장 실패");
        }).when(postSearchRepository).save(any());

        CompletableFuture<Void> future = new CompletableFuture<>();

        // 트랜잭션 실행
        new TransactionTemplate(transactionManager).execute(status -> {

            User user = TestUserFactory.createTestUser();
            userRepository.save(user);

            Post post = Post.builder()
                    .title("테스트 제목")
                    .content("테스트 내용")
                    .user(user)
                    .commentsEnabled(CommentsEnabled.ALLOW)
                    .postStatus(PostStatus.PUBLIC)
                    .build();

            postRepository.save(post);

            TransactionSynchronizationManager.registerSynchronization(
                    new TransactionSynchronization() {
                        @Override
                        public void afterCommit() {
                            try {
                                // eventPublisher.publishEvent(new PostCreatedEvent(post));
                                future.complete(postEventHandler.handlePostCreated(new PostCreatedEvent(post)).get());
                            } catch (Exception e) {
                                // caughtException.set(e);
                                future.completeExceptionally(e);

                            }
                        }
                    });

            return null;
        });

        // then
        assertThatThrownBy(() -> future.get(10, TimeUnit.SECONDS))
                .isInstanceOf(ExecutionException.class)
                .extracting(Throwable::getCause)
                .extracting(Throwable::getCause) // cause를 한번 더 추출
                .satisfies(cause -> {
                    assertThat(cause)
                            .isInstanceOf(ElasticsearchCustomException.class)
                            .hasMessage("Posts 인덱스에 문서 생성 실패1")
                            .hasFieldOrPropertyWithValue("errorCode", "E001");
                });

        verify(postSearchRepository, times(3)).save(any());
        assertThat(attempts.get()).isEqualTo(3);
    }

}