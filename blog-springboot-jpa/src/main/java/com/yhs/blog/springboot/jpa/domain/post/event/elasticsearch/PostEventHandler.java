package com.yhs.blog.springboot.jpa.domain.post.event.elasticsearch;

import java.util.concurrent.CompletableFuture;

import org.springframework.retry.support.RetryTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.yhs.blog.springboot.jpa.domain.post.repository.search.PostSearchRepository;
import com.yhs.blog.springboot.jpa.domain.post.repository.search.document.PostDocument;
import com.yhs.blog.springboot.jpa.exception.custom.ElasticsearchCustomException;

import co.elastic.clients.elasticsearch._types.ElasticsearchException;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Component
@RequiredArgsConstructor
@Log4j2
public class PostEventHandler {

    private final PostSearchRepository postSearchRepository;
    private final RetryTemplate retryTemplate;

    // @EventListener 또는 @TransactionalEventListener 어노테이션이 붙은 메서드는 자동으로 이벤트 핸들러로 등록
    // 최대 3번 까지 시도 후 실패하면 예외 발생
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public CompletableFuture<Void> handlePostCreated(PostCreatedEvent event) {

        try {
            retryTemplate.execute(context -> {

                log.info("PostCreatedEvent 발생 - Post ID: {}, Thread: {}",
                        event.getPost().getId(),
                        Thread.currentThread().getName());

                postSearchRepository.save(PostDocument.from(event.getPost()));
                return null;
            });

            return CompletableFuture.completedFuture(null);
        } catch (Exception e) {
            throw new ElasticsearchCustomException("Posts 인덱스에 문서 생성 실패" + event.getPost().getId(), "ESS001", e);

        }

    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public CompletableFuture<Void> handlePostUpdated(PostUpdatedEvent event) {

        try {
            retryTemplate.execute(context -> {

                log.info("PostUpdatedEvent 발생 - Post ID: {}, Thread: {}",
                        event.getPost().getId(),
                        Thread.currentThread().getName());

                postSearchRepository.save(PostDocument.from(event.getPost()));
                return null;
            });

            return CompletableFuture.completedFuture(null);

        } catch (Exception e) {
            throw new ElasticsearchCustomException("Posts 인덱스 문서 수정 실패" + event.getPost().getId(), "ESS002", e);
        }

    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public CompletableFuture<Void> handlePostDeleted(PostDeletedEvent event) {

        try {
            retryTemplate.execute(context -> {

                log.info("PostDeletedEvent 발생 - Post ID: {}, Thread: {}",
                        event.getPost().getId(),
                        Thread.currentThread().getName());

                postSearchRepository.deleteById(String.valueOf(event.getPost().getId()));
                return null;
            });

            return CompletableFuture.completedFuture(null);

        } catch (Exception e) {
            throw new ElasticsearchCustomException("Posts 인덱스 문서 삭제 실패" + event.getPost().getId(), "ESS003", e);
        }

    }
}
