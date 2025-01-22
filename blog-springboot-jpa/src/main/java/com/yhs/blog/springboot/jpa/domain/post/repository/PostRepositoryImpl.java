package com.yhs.blog.springboot.jpa.domain.post.repository;

import co.elastic.clients.elasticsearch._types.ElasticsearchException;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.yhs.blog.springboot.jpa.aop.log.Loggable;
import com.yhs.blog.springboot.jpa.common.constant.code.ErrorCode;
import com.yhs.blog.springboot.jpa.domain.category.entity.QCategory;
import com.yhs.blog.springboot.jpa.domain.post.dto.response.PostResponse;
import com.yhs.blog.springboot.jpa.domain.post.entity.QFeaturedImage;
import com.yhs.blog.springboot.jpa.domain.post.entity.QPost;
import com.yhs.blog.springboot.jpa.domain.post.repository.search.PostSearchRepository;
import com.yhs.blog.springboot.jpa.domain.post.repository.search.SearchType;
import com.yhs.blog.springboot.jpa.domain.post.repository.search.document.PostDocument;
import com.yhs.blog.springboot.jpa.domain.user.entity.QUser;
import com.yhs.blog.springboot.jpa.exception.custom.SystemException;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.List;

@RequiredArgsConstructor
@Repository // 구현체 repository 어노테이션 추가 필요
@Log4j2
public class PostRepositoryImpl implements PostRepositoryCustom {

        private final JPAQueryFactory queryFactory;
        private final PostSearchRepository searchRepository;

        @Loggable
        @Override
        public Page<PostResponse> findPostsAllUser(String keyword, SearchType searchType, Pageable pageable) {
                if (StringUtils.hasText(keyword)) {

//                        아래는 일반 DB 조회
//                        QPost post = QPost.post;
//                        QUser user = QUser.user;
//                        QCategory category = QCategory.category;
//                        QFeaturedImage featuredImage = QFeaturedImage.featuredImage;
//
//                        List<PostResponse> posts = queryFactory
//                                .select(Projections.constructor(PostResponse.class,
//                                        post.id,
//                                        post.title,
//                                        post.content,
//                                        user.username,
//                                        category.name,
//                                        featuredImage))
//                                .from(post)
//                                .innerJoin(post.user, user) // Join with user entity
//                                .leftJoin(post.category, category) // Left join with category entity
//                                .leftJoin(post.featuredImage, featuredImage) // Left join with featuredImage entity
//                                .where(post.title.contains(keyword))
//                                .offset(pageable.getOffset())
//                                .limit(pageable.getPageSize())
//                                .fetch();
//
//                        long total = queryFactory
//                                .select(post.count())
//                                .from(post)
//                                .where(post.title.contains(keyword))
//                                .fetchOne();
//
//                        return new PageImpl<>(posts, pageable, total);

                        try {
                                Page<PostDocument> searchResult = switch (searchType) {
                                        case TITLE -> searchRepository.searchByTitleForAllUser(keyword,
                                                pageable);
                                        case CONTENT -> searchRepository.searchByContentForAllUser(keyword,
                                                pageable);
                                        case ALL -> searchRepository.searchByAllForAllUser(keyword, pageable);
                                };

                                return searchResult.map(PostResponse::fromDocument);
                        } catch (ElasticsearchException e) {
                                throw new SystemException(ErrorCode.ELASTIC_SEARCH_SPECIFIC_ERROR,
                                        "검색 처리 중 오류가 발생 하였습니다.", "PostRepositoryImpl", "findPostsAllUser", e);
                        } catch (Exception e) {
                                throw new SystemException(ErrorCode.ELASTIC_SEARCH_GENERAL_ERROR,
                                        "검색 처리 중 오류가 발생 하였습니다.", "PostRepositoryImpl", "findPostsAllUser", e);
                        }

                }

                try {
                        // 검색어가 없는 경우 기존 QueryDSL 사용
                        return executeQueryDSLQuery(new BooleanBuilder(), pageable);

                } catch (Exception e) {

                        throw new SystemException(ErrorCode.QUERY_DSL_ERROR, "게시글 목록 조회 중 오류가 발생 하였습니다.",
                                        "PostRepositoryImpl", "findPostsAllUser", e);
                }
        }

        @Loggable
        @Override
        public Page<PostResponse> findPostsByUserId(Long userId, String keyword, SearchType searchType,
                        Pageable pageable) {

                if (StringUtils.hasText(keyword)) {

                        try {
                                Page<PostDocument> searchResult = switch (searchType) {
                                        case TITLE -> searchRepository.searchByTitleForSpecificUser(keyword,
                                                        String.valueOf(userId),
                                                        pageable);
                                        case CONTENT ->
                                                searchRepository.searchByContentForSpecificUser(keyword,
                                                                String.valueOf(userId),
                                                                pageable);
                                        case ALL ->
                                                searchRepository.searchByAllForSpecificUser(keyword,
                                                                String.valueOf(userId), pageable);
                                };

                                return searchResult.map(PostResponse::fromDocument);
                        } catch (ElasticsearchException e) {
                                throw new SystemException(ErrorCode.ELASTIC_SEARCH_SPECIFIC_ERROR,
                                                "검색 처리 중 오류가 발생 하였습니다.", "PostRepositoryImpl", "findPostsByUserId", e);
                        } catch (Exception e) {
                                throw new SystemException(ErrorCode.ELASTIC_SEARCH_GENERAL_ERROR,
                                                "검색 처리 중 오류가 발생 하였습니다.", "PostRepositoryImpl", "findPostsByUserId", e);
                        }

                }

                try {
                        // 검색어가 없는 경우 기존 QueryDSL 사용

                        QPost post = QPost.post;
                        BooleanBuilder builder = new BooleanBuilder();
                        builder.and(post.user.id.eq(userId));
                        return executeQueryDSLQuery(builder, pageable);

                } catch (Exception e) {

                        throw new SystemException(ErrorCode.QUERY_DSL_ERROR, "게시글 목록 조회 중 오류가 발생 하였습니다.",
                                        "PostRepositoryImpl", "findPostsByUserId", e);
                }

        }

        @Loggable
        @Override
        public Page<PostResponse> findPostsByUserIdAndCategoryId(Long userId, String categoryId, String keyword,
                        SearchType searchType, Pageable pageable) {

                // 검색어가 있는 경우 Elasticsearch 사용
                if (StringUtils.hasText(keyword)) {

                        try {
                                Page<PostDocument> searchResult = switch (searchType) {
                                        case TITLE ->
                                                searchRepository.searchByTitleAndCategoryForSpecificUser(keyword,
                                                                String.valueOf(userId),
                                                                categoryId, pageable);
                                        case CONTENT ->
                                                searchRepository.searchByContentAndCategoryForSpecificUser(keyword,
                                                                String.valueOf(userId),
                                                                categoryId, pageable);
                                        case ALL ->
                                                searchRepository.searchByAllAndCategoryForSpecificUser(keyword,
                                                                String.valueOf(userId),
                                                                categoryId, pageable);
                                };

                                return searchResult.map(PostResponse::fromDocument);

                        } catch (ElasticsearchException e) {
                                throw new SystemException(ErrorCode.ELASTIC_SEARCH_SPECIFIC_ERROR,
                                                "검색 처리 중 오류가 발생 하였습니다.", "PostRepositoryImpl",
                                                "findPostsByUserIdAndCategoryId", e);
                        } catch (Exception e) {
                                throw new SystemException(ErrorCode.ELASTIC_SEARCH_GENERAL_ERROR,
                                                "검색 처리 중 오류가 발생 하였습니다.", "PostRepositoryImpl",
                                                "findPostsByUserIdAndCategoryId", e);
                        }
                }

                try {
                        // 검색어가 없는 경우 기존 QueryDSL 사용
                        QPost post = QPost.post;
                        BooleanBuilder builder = new BooleanBuilder();
                        builder.and(post.user.id.eq(userId))
                                        .and(post.category.id.eq(categoryId));
                        return executeQueryDSLQuery(builder, pageable);
                } catch (Exception e) {
                        throw new SystemException(ErrorCode.QUERY_DSL_ERROR, "게시글 목록 조회 중 오류가 발생 하였습니다.",
                                        "PostRepositoryImpl", "findPostsByUserIdAndCategoryId", e);
                }

        }

        private Page<PostResponse> executeQueryDSLQuery(BooleanBuilder builder, Pageable pageable) {
                QPost post = QPost.post;
                QUser user = QUser.user;
                QCategory category = QCategory.category;
                QFeaturedImage featuredImage = QFeaturedImage.featuredImage;

                List<PostResponse> posts = queryFactory
                                .select(Projections.constructor(PostResponse.class,
                                                post.id,
                                                post.title,
                                                post.content,
                                                user.username,
                                                category.name,
                                                featuredImage))
                                .from(post)
                                .innerJoin(post.user, user) // userId가 있는 게시물만 조회 따라서, leftjoin이 아닌 innerJoin
                                .leftJoin(post.category, category) // 카테고리는 optional 즉, 카테고리가 없는 게시물도 조회
                                .leftJoin(post.featuredImage, featuredImage) // 이미지도 optional
                                .where(builder.hasValue() ? builder : null)
                                .orderBy(post.createdAt.desc(), post.id.desc()) // 생성 날짜가 같은 경우 id로 추가 정렬
                                .offset(pageable.getOffset())
                                .limit(pageable.getPageSize())
                                .fetch();

                long total = queryFactory
                                .select(post.count())
                                .from(post)
                                .where(builder.hasValue() ? builder : null)
                                .fetchOne();

                log.debug("total >>>>>> {}", total);

                return new PageImpl<>(
                                posts,
                                pageable,
                                total);
        }

}
