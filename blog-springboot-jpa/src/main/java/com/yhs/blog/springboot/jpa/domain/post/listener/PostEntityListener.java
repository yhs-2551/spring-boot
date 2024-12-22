package com.yhs.blog.springboot.jpa.domain.post.listener;

import org.springframework.stereotype.Component;

import com.yhs.blog.springboot.jpa.domain.post.entity.Post;
import com.yhs.blog.springboot.jpa.domain.post.repository.search.PostSearchRepository;
import com.yhs.blog.springboot.jpa.domain.post.repository.search.document.PostDocument;

import jakarta.persistence.PostPersist;
import jakarta.persistence.PostRemove;
import jakarta.persistence.PostUpdate;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@RequiredArgsConstructor
@Component
@Log4j2
public class PostEntityListener {

    private final PostSearchRepository postSearchRepository;

    @PostPersist
    @PostUpdate
    public void syncToElasticsearch(Post post) {
        try {
            log.info("syncToElasticsearch 실행 - Post ID: {}", post.getId());
            PostDocument document = PostDocument.from(post);
            log.info("Created PostDocument with ID: {}", document.getId());
            postSearchRepository.save(document);
        } catch (Exception e) {
            log.error("Error in syncToElasticsearch: ", e);
        }
    }

    @PostRemove
    public void removeFromElasticsearch(Post post) {
        try {
            String documentId = String.valueOf(post.getId());
            log.info("Removing document with ID: {}", documentId);
            postSearchRepository.deleteById(documentId);
        } catch (Exception e) {
            log.error("Error in removeFromElasticsearch: ", e);
        }
    }
}
