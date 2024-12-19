package com.yhs.blog.springboot.jpa.domain.post.repository;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import com.querydsl.core.BooleanBuilder;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.yhs.blog.springboot.jpa.domain.post.dto.response.PostResponse;
import com.yhs.blog.springboot.jpa.domain.post.entity.Post;
import com.yhs.blog.springboot.jpa.domain.post.entity.QPost;
import com.yhs.blog.springboot.jpa.domain.post.repository.search.PostDocument;
import com.yhs.blog.springboot.jpa.domain.post.repository.search.PostSearchRepository;
import com.yhs.blog.springboot.jpa.domain.post.repository.search.SearchType;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@RequiredArgsConstructor
@Repository // 구현체 repository 어노테이션 추가 필요
@Log4j2
public class PostRepositoryImpl implements PostRepositoryCustom {

        private final JPAQueryFactory queryFactory;
        private final PostSearchRepository searchRepository;

        @Override
        public Page<PostResponse> findPosts(Long userId, String keyword, SearchType searchType, Pageable pageable) {

                if (StringUtils.hasText(keyword)) {
                        Page<PostDocument> searchResult = switch (searchType) {
                                case TITLE -> searchRepository.searchByTitle(keyword, String.valueOf(userId), pageable);
                                case CONTENT ->
                                        searchRepository.searchByContent(keyword, String.valueOf(userId), pageable);
                                case ALL -> searchRepository.searchByAll(keyword, String.valueOf(userId), pageable);
                        };
                        return searchResult.map(PostResponse::fromDocument);
                }

                QPost post = QPost.post;

                log.info("post: {}", post);

                BooleanBuilder builder = new BooleanBuilder();
                builder.and(post.user.id.eq(userId));

                return executeQueryDSLQuery(builder, pageable);

        }

        @Override
        public Page<PostResponse> findPostsByCategory(Long userId, String categoryUuid, String keyword,
                        SearchType searchType, Pageable pageable) {

                // 검색어가 있는 경우 Elasticsearch 사용
                if (StringUtils.hasText(keyword)) {
                        Page<PostDocument> searchResult = switch (searchType) {
                                case TITLE ->
                                        searchRepository.searchByTitleAndCategory(keyword, String.valueOf(userId),
                                                        categoryUuid, pageable);
                                case CONTENT ->
                                        searchRepository.searchByContentAndCategory(keyword, String.valueOf(userId),
                                                        categoryUuid, pageable);
                                case ALL -> searchRepository.searchByAllAndCategory(keyword, String.valueOf(userId),
                                                categoryUuid, pageable);
                        };
                        return searchResult.map(PostResponse::fromDocument);
                }

                // 검색어가 없는 경우 기존 QueryDSL 사용

                QPost post = QPost.post;

                BooleanBuilder builder = new BooleanBuilder();
                builder.and(post.user.id.eq(userId))
                                .and(post.category.id.eq(categoryUuid));

                return executeQueryDSLQuery(builder, pageable);
        }

        private Page<PostResponse> executeQueryDSLQuery(BooleanBuilder builder, Pageable pageable) {
                QPost post = QPost.post;

                List<Post> posts = queryFactory
                                .selectFrom(post)
                                .where(builder)
                                .orderBy(post.createdAt.desc())
                                .offset(pageable.getOffset())
                                .limit(pageable.getPageSize())
                                .fetch();

                long total = queryFactory
                                .select(post.count())
                                .from(post)
                                .where(builder)
                                .fetchOne();

                return new PageImpl<>(
                                posts.stream().map(PostResponse::from).collect(Collectors.toList()),
                                pageable,
                                total);
        }

}
