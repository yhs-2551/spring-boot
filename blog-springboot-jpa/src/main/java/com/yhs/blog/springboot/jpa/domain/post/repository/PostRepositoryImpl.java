package com.yhs.blog.springboot.jpa.domain.post.repository;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.NoSuchIndexException;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import com.querydsl.core.BooleanBuilder;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.yhs.blog.springboot.jpa.domain.post.dto.response.PostResponse;
import com.yhs.blog.springboot.jpa.domain.post.entity.Post;
import com.yhs.blog.springboot.jpa.domain.post.entity.QPost;
import com.yhs.blog.springboot.jpa.domain.post.repository.search.PostSearchRepository;
import com.yhs.blog.springboot.jpa.domain.post.repository.search.SearchType;
import com.yhs.blog.springboot.jpa.domain.post.repository.search.document.PostDocument;
import com.yhs.blog.springboot.jpa.exception.custom.ElasticsearchCustomException;
import com.yhs.blog.springboot.jpa.exception.custom.QueryDslCustomException;

import co.elastic.clients.elasticsearch._types.ElasticsearchException;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@RequiredArgsConstructor
@Repository // 구현체 repository 어노테이션 추가 필요
@Log4j2
public class PostRepositoryImpl implements PostRepositoryCustom {

        private final JPAQueryFactory queryFactory;
        private final PostSearchRepository searchRepository;

        @Override
        public Page<PostResponse> findPostsAllUser(String keyword, SearchType searchType, Pageable pageable) {
                if (StringUtils.hasText(keyword)) {

                        try {
                                Page<PostDocument> searchResult = switch (searchType) {
                                        case TITLE -> searchRepository.searchByTitleForAllUser(keyword,
                                                        pageable);
                                        case CONTENT ->
                                                searchRepository.searchByContentForAllUser(keyword,
                                                                pageable);
                                        case ALL ->
                                                searchRepository.searchByAllForAllUser(keyword, pageable);
                                };

                                return searchResult.map(PostResponse::fromDocument);
                        } catch (ElasticsearchException e) {
                                throw new ElasticsearchCustomException("검색 처리 중 오류가 발생했습니다.", "ES500", e);
                        } catch (Exception e) {
                                throw new ElasticsearchCustomException("검색을 처리할 수 없습니다.", "ES520", e);
                        }

                }

                try {
                        // 검색어가 없는 경우 기존 QueryDSL 사용
                        return executeQueryDSLQuery(new BooleanBuilder(), pageable);

                } catch (Exception e) {

                        throw new QueryDslCustomException("일반 QueryDSL query 오류가 발생했습니다.", "QD500", e);
                }
        }

        @Override
        public Page<PostResponse> findPostsByUserId(Long userId, String keyword, SearchType searchType,
                        Pageable pageable) {

                if (StringUtils.hasText(keyword)) {

                        try {
                                Page<PostDocument> searchResult = switch (searchType) {
                                        case TITLE -> searchRepository.searchByTitleForSpecificUser(keyword, String.valueOf(userId),
                                                        pageable);
                                        case CONTENT ->
                                                searchRepository.searchByContentForSpecificUser(keyword, String.valueOf(userId),
                                                                pageable);
                                        case ALL ->
                                                searchRepository.searchByAllForSpecificUser(keyword, String.valueOf(userId), pageable);
                                };

                                return searchResult.map(PostResponse::fromDocument);
                        } catch (ElasticsearchException e) {
                                throw new ElasticsearchCustomException("검색 처리 중 오류가 발생했습니다.", "ES500", e);
                        } catch (Exception e) {
                                throw new ElasticsearchCustomException("검색을 처리할 수 없습니다.", "ES520", e);
                        }

                }

                try {
                        // 검색어가 없는 경우 기존 QueryDSL 사용

                        QPost post = QPost.post;
                        BooleanBuilder builder = new BooleanBuilder();
                        builder.and(post.user.id.eq(userId));
                        return executeQueryDSLQuery(builder, pageable);

                } catch (Exception e) {

                        throw new QueryDslCustomException("일반 QueryDSL query 오류가 발생했습니다.", "QD500", e);
                }

        }

        @Override
        public Page<PostResponse> findPostsByUserIdAndCategoryId(Long userId, String categoryUuid, String keyword,
                        SearchType searchType, Pageable pageable) {

                // 검색어가 있는 경우 Elasticsearch 사용
                if (StringUtils.hasText(keyword)) {
                        try {
                                Page<PostDocument> searchResult = switch (searchType) {
                                        case TITLE ->
                                                searchRepository.searchByTitleAndCategoryForSpecificUser(keyword,
                                                                String.valueOf(userId),
                                                                categoryUuid, pageable);
                                        case CONTENT ->
                                                searchRepository.searchByContentAndCategoryForSpecificUser(keyword,
                                                                String.valueOf(userId),
                                                                categoryUuid, pageable);
                                        case ALL ->
                                                searchRepository.searchByAllAndCategoryForSpecificUser(keyword, String.valueOf(userId),
                                                                categoryUuid, pageable);
                                };
                                return searchResult.map(PostResponse::fromDocument);

                        } catch (ElasticsearchException e) {
                                throw new ElasticsearchCustomException("검색 처리 중 오류가 발생했습니다.", "ES500", e);
                        } catch (Exception e) {
                                throw new ElasticsearchCustomException("검색을 처리할 수 없습니다.", "ES520", e);
                        }
                }

                try {
                        // 검색어가 없는 경우 기존 QueryDSL 사용
                        QPost post = QPost.post;
                        BooleanBuilder builder = new BooleanBuilder();
                        builder.and(post.user.id.eq(userId))
                                        .and(post.category.id.eq(categoryUuid));
                        return executeQueryDSLQuery(builder, pageable);
                } catch (Exception e) {
                        throw new QueryDslCustomException("카테고리 QueryDSL query 오류가 발생했습니다.", "QD500", e);
                }

        }

        private Page<PostResponse> executeQueryDSLQuery(BooleanBuilder builder, Pageable pageable) {
                QPost post = QPost.post;

                log.debug("post >>>> {}", post);

                List<Post> posts = queryFactory
                                .selectFrom(post)
                                .where(builder.hasValue() ? builder : null)
                                .orderBy(post.createdAt.desc(), post.id.desc()) // 생성 날짜가 같은 경우 id로 추가 정렬
                                .offset(pageable.getOffset())
                                .limit(pageable.getPageSize())
                                .fetch();

                log.debug("posts 입니당>>>> {}", posts);

                long total = queryFactory
                                .select(post.count())
                                .from(post)
                                .where(builder.hasValue() ? builder : null)
                                .fetchOne();

                return new PageImpl<>(
                                posts.stream().map(PostResponse::from).collect(Collectors.toList()),
                                pageable,
                                total);
        }

}
