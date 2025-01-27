package com.yhs.blog.springboot.jpa.domain.post.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.yhs.blog.springboot.jpa.aop.log.Loggable;
import com.yhs.blog.springboot.jpa.common.constant.code.ErrorCode;
import com.yhs.blog.springboot.jpa.domain.category.entity.QCategory;
import com.yhs.blog.springboot.jpa.domain.post.dto.response.PostResponse;
import com.yhs.blog.springboot.jpa.domain.post.entity.QFeaturedImage;
import com.yhs.blog.springboot.jpa.domain.post.entity.QPost; 
import com.yhs.blog.springboot.jpa.domain.post.repository.search.SearchType; 
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

        @Loggable
        @Override
        public Page<PostResponse> findPostsAllUser(String keyword, SearchType searchType, Pageable pageable) {

                try {
                        log.info("[PostRepositoryImpl] findPostsAllUser() 메서드 시작");
                        if (StringUtils.hasText(keyword)) {

                                log.info("[PostRepositoryImpl] findPostsAllUser() 메서드 검색어가 있을때 분기 진행");

                                // 아래는 일반 DB 조회
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
                                                .innerJoin(post.user, user)
                                                .leftJoin(post.category, category)
                                                .leftJoin(post.featuredImage, featuredImage)
                                                .where(searchByType(keyword, searchType))
                                                .offset(pageable.getOffset())
                                                .limit(pageable.getPageSize())
                                                .fetch();

                                long total = queryFactory
                                                .select(post.count())
                                                .from(post)
                                                .where(searchByType(keyword, searchType))
                                                .fetchOne();

                                return new PageImpl<>(posts, pageable, total);

                        }

                        log.info("[PostRepositoryImpl] findPostsAllUser() 메서드 검색어가 없을때 분기 진행");

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
                try {
                        log.info("[PostRepositoryImpl] findPostsByUserId() 메서드 시작");

                        if (StringUtils.hasText(keyword)) {

                                log.info("[PostRepositoryImpl] findPostsByUserId() 메서드 검색어가 있을때 분기 진행");

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
                                                .innerJoin(post.user, user)
                                                .leftJoin(post.category, category)
                                                .leftJoin(post.featuredImage, featuredImage)
                                                .where(searchByType(keyword, searchType), userIdEq(userId))
                                                .offset(pageable.getOffset())
                                                .limit(pageable.getPageSize())
                                                .fetch();

                                long total = queryFactory
                                                .select(post.count())
                                                .from(post)
                                                .where(searchByType(keyword, searchType), userIdEq(userId))
                                                .fetchOne();

                                return new PageImpl<>(posts, pageable, total);

                        }

                        log.info("[PostRepositoryImpl] findPostsByUserId() 메서드 검색어가 없을때 분기 진행");

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
                try {
                        log.info("[PostRepositoryImpl] findPostsByUserIdAndCategoryId() 메서드 시작");

                        // 검색어가 있는 경우 Elasticsearch 사용
                        if (StringUtils.hasText(keyword)) {

                                log.info("[PostRepositoryImpl] findPostsByUserIdAndCategoryId() 메서드 검색어가 있을때 분기 진행");

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
                                                .innerJoin(post.user, user)
                                                .leftJoin(post.category, category)
                                                .leftJoin(post.featuredImage, featuredImage)
                                                .where(searchByType(keyword, searchType), userIdEq(userId),
                                                                categoryIdEq(categoryId))
                                                .offset(pageable.getOffset())
                                                .limit(pageable.getPageSize())
                                                .fetch();

                                long total = queryFactory
                                                .select(post.count())
                                                .from(post)
                                                .where(searchByType(keyword, searchType), userIdEq(userId),
                                                                categoryIdEq(categoryId))
                                                .fetchOne();

                                return new PageImpl<>(posts, pageable, total);
                        }

                        log.info("[PostRepositoryImpl] findPostsByUserIdAndCategoryId() 메서드 검색어가 없을때 분기 진행");

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

        // BooleanExpression은 where절에서 사용되는 조건식. and(), or() 체이닝 가능, null 처리 가능
        private BooleanExpression searchByType(String keyword, SearchType searchType) {
                QPost post = QPost.post;

                return switch (searchType) {
                        case TITLE -> post.title.contains(keyword);
                        case CONTENT -> post.content.contains(keyword);
                        case ALL -> post.title.contains(keyword).or(post.content.contains(keyword));
                        default -> null;
                };
        }

        private BooleanExpression userIdEq(Long userId) {
                return userId != null ? QPost.post.user.id.eq(userId) : null;
        }

        private BooleanExpression categoryIdEq(String categoryId) {
                return StringUtils.hasText(categoryId) ? QPost.post.category.id.eq(categoryId) : null;
        }

        private Page<PostResponse> executeQueryDSLQuery(BooleanBuilder builder, Pageable pageable) {

                log.info("[PostRepositoryImpl] executeQueryDSLQuery() 메서드 시작");

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

                return new PageImpl<>(
                                posts,
                                pageable,
                                total);
        }

}
