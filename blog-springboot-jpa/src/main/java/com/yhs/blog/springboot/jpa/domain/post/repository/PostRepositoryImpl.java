package com.yhs.blog.springboot.jpa.domain.post.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.yhs.blog.springboot.jpa.aop.log.Loggable;
import com.yhs.blog.springboot.jpa.common.constant.code.ErrorCode;
import com.yhs.blog.springboot.jpa.domain.category.entity.QCategory;
import com.yhs.blog.springboot.jpa.domain.file.dto.response.FileResponse;
import com.yhs.blog.springboot.jpa.domain.file.entity.QFile;
import com.yhs.blog.springboot.jpa.domain.post.dto.response.FeaturedImageResponse;
import com.yhs.blog.springboot.jpa.domain.post.dto.response.PostResponse;
import com.yhs.blog.springboot.jpa.domain.featured_image.entity.QFeaturedImage;
import com.yhs.blog.springboot.jpa.domain.post.entity.QPost;
import com.yhs.blog.springboot.jpa.domain.post.entity.QPostTag;
import com.yhs.blog.springboot.jpa.domain.post.entity.QTag;
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

        // 아래 쿼리들 여러 쿼리는 postStatus가 포함되어 있는지 없는지에 따라 다름. 나중에 postStatus상태에 따라 분기 처리 필요

        private final JPAQueryFactory queryFactory;

        @Loggable
        @Override
        public Page<PostResponse> findPostsAllUser(String keyword, SearchType searchType, Pageable pageable) { // postStatus
                                                                                                               // 필요x

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
                                                                Projections.constructor(FeaturedImageResponse.class,
                                                                                featuredImage.fileName,
                                                                                featuredImage.fileUrl,
                                                                                featuredImage.fileType,
                                                                                featuredImage.fileSize)))
                                                .from(post)
                                                .join(user).on(user.id.eq(post.userId)) // INNER JOIN
                                                .leftJoin(category).on(category.id.eq(post.categoryId)) // LEFT JOIN
                                                // LEFT JOIN
                                                .leftJoin(featuredImage).on(featuredImage.id.eq(post.featuredImageId))
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
                        return executeQueryDSLQueryForIndexPageOrUserPageNotOWner(new BooleanBuilder(), pageable);

                } catch (Exception e) {

                        throw new SystemException(ErrorCode.QUERY_DSL_POSTS_ERROR, "게시글 목록 조회 중 오류가 발생 하였습니다.",
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
                                                                post.postStatus,
                                                                user.username,
                                                                category.name,
                                                                Projections.constructor(FeaturedImageResponse.class,
                                                                                featuredImage.fileName,
                                                                                featuredImage.fileUrl,
                                                                                featuredImage.fileType,
                                                                                featuredImage.fileSize)))
                                                .from(post)
                                                .join(user).on(user.id.eq(post.userId)) // 이쪽 user.id는 join하기 위한 user의
                                                                                        // id
                                                .leftJoin(category).on(category.id.eq(post.categoryId))
                                                .leftJoin(featuredImage).on(featuredImage.id.eq(post.featuredImageId))
                                                .where(searchByType(keyword, searchType), userIdEq(userId)) // 이쪽
                                                                                                            // userId는
                                                                                                            // 실제
                                                                                                            // where절에서
                                                                                                            // 해당 사용자에
                                                                                                            // 대한 게시글만
                                                                                                            // 가져오기 위한
                                                                                                            // userId
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
                        builder.and(post.userId.eq(userId));
                        return executeQueryDSLQueryForUserPageAndOwner(builder, pageable);

                } catch (Exception e) {

                        throw new SystemException(ErrorCode.QUERY_DSL_POSTS_ERROR, "게시글 목록 조회 중 오류가 발생 하였습니다.",
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
                                                                post.postStatus,
                                                                user.username,
                                                                category.name,
                                                                Projections.constructor(FeaturedImageResponse.class,
                                                                                featuredImage.fileName,
                                                                                featuredImage.fileUrl,
                                                                                featuredImage.fileType,
                                                                                featuredImage.fileSize)))
                                                .from(post)
                                                .join(user).on(user.id.eq(post.userId))
                                                .leftJoin(category).on(category.id.eq(post.categoryId))
                                                .leftJoin(featuredImage).on(featuredImage.id.eq(post.featuredImageId))
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
                        builder.and(post.userId.eq(userId))
                                        .and(post.categoryId.eq(categoryId));
                        return executeQueryDSLQueryForUserPageAndOwner(builder, pageable);
                } catch (Exception e) {
                        throw new SystemException(ErrorCode.QUERY_DSL_POSTS_ERROR, "게시글 목록 조회 중 오류가 발생 하였습니다.",
                                        "PostRepositoryImpl", "findPostsByUserIdAndCategoryId", e);
                }

        }

        @Override
        public PostResponse findByIdNotWithFeaturedImage(Long postId) {

                log.info("[PostRepositoryImpl] findByIdNotWithFeaturedImage() 메서드 시작");

                QPost post = QPost.post;
                QUser user = QUser.user;
                QCategory category = QCategory.category;
                QPostTag postTag = QPostTag.postTag;
                QTag tag = QTag.tag;
                QFile file = QFile.file;

                try {

                        return queryFactory
                                        .select(Projections.constructor(PostResponse.class,
                                                        post.title,
                                                        post.content, // 가져올때 temp -> final로 안바꼈을 수 있는데 확인 필요
                                                        // JPAExpressions에 의해 가져온 결과를 배열로 반환, null이면 자동 null처리
                                                        JPAExpressions
                                                                        .select(tag.name)
                                                                        .from(postTag)
                                                                        .join(tag).on(postTag.tagId.eq(tag.id))
                                                                        .where(postTag.postId.eq(post.id)),
                                                        JPAExpressions
                                                                        .select(Projections.list(
                                                                                        Projections.constructor(
                                                                                                        FileResponse.class,
                                                                                                        file.fileUrl,
                                                                                                        file.width,
                                                                                                        file.height)))
                                                                        .from(file)
                                                                        // 여기 post.id는 아래 전체 where절에서 가져온post를 의미함. 즉 외부
                                                                        // 쿼리의
                                                                        // post 엔티티 참조
                                                                        .where(file.postId.eq(post.id)),
                                                        post.postStatus,
                                                        user.username,
                                                        category.name,
                                                        post.createdAt))
                                        .from(post) // 여기서 정의된 post를 서브쿼리에서 참조
                                        .join(user).on(user.id.eq(post.userId))
                                        .leftJoin(category).on(category.id.eq(post.categoryId))
                                        .where(post.id.eq(postId))
                                        .fetchOne();
                } catch (Exception e) {
                        throw new SystemException(ErrorCode.QUERY_DSL_POSTS_ERROR, "게시글 목록 조회 중 오류가 발생 하였습니다.",
                                        "PostRepositoryImpl", "findByIdNotWithFeaturedImage", e);
                }

        }

        @Override
        public PostResponse findByIdWithFeaturedImage(Long postId) {

                log.info("[PostRepositoryImpl] findByIdWithFeaturedImage() 메서드 시작");

                QPost post = QPost.post;
                QUser user = QUser.user;
                QCategory category = QCategory.category;
                QPostTag postTag = QPostTag.postTag;
                QTag tag = QTag.tag;
                QFile file = QFile.file;
                QFeaturedImage featuredImage = QFeaturedImage.featuredImage;
                try {

                        return queryFactory
                                        .select(Projections.constructor(PostResponse.class,
                                                        post.title,
                                                        post.content, // 가져올때 temp -> final로 안바꼈을 수 있는데 확인 필요
                                                        // JPAExpressions에 의해 가져온 결과를 배열로 반환, null이면 자동 null처리
                                                        JPAExpressions
                                                                        .select(tag.name)
                                                                        .from(postTag)
                                                                        .join(tag).on(postTag.tagId.eq(tag.id))
                                                                        .where(postTag.postId.eq(post.id)),
                                                        JPAExpressions
                                                                        .select(Projections.list(
                                                                                        Projections.constructor(
                                                                                                        FileResponse.class,
                                                                                                        file.fileName,
                                                                                                        file.fileType,
                                                                                                        file.fileUrl,
                                                                                                        file.fileSize,
                                                                                                        file.width,
                                                                                                        file.height),
                                                                                        Projections.constructor(
                                                                                                        FeaturedImageResponse.class,
                                                                                                        featuredImage.fileName,
                                                                                                        featuredImage.fileUrl,
                                                                                                        featuredImage.fileType,
                                                                                                        featuredImage.fileSize)))
                                                                        .from(file)
                                                                        // 여기 post.id는 아래 전체 where절에서 가져온post를 의미함. 즉 외부
                                                                        // 쿼리의
                                                                        // post 엔티티 참조
                                                                        .where(file.postId.eq(post.id))

                                                        ,
                                                        post.postStatus,
                                                        user.username,
                                                        category.name,
                                                        post.createdAt))
                                        .from(post) // 여기서 정의된 post를 서브쿼리에서 참조
                                        .join(user).on(user.id.eq(post.userId))
                                        .leftJoin(category).on(category.id.eq(post.categoryId))
                                        .where(post.id.eq(postId))
                                        .fetchOne();
                } catch (Exception e) {
                        throw new SystemException(ErrorCode.QUERY_DSL_POSTS_ERROR, "게시글 목록 조회 중 오류가 발생 하였습니다.",
                                        "PostRepositoryImpl", "findByIdWithFeaturedImage", e);
                }

        }

        private Page<PostResponse> executeQueryDSLQueryForIndexPageOrUserPageNotOWner(BooleanBuilder builder,
                        Pageable pageable) {

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
                                                Projections.constructor(FeaturedImageResponse.class,
                                                                featuredImage.fileName,
                                                                featuredImage.fileUrl,
                                                                featuredImage.fileType,
                                                                featuredImage.fileSize)))
                                .from(post)
                                .join(user).on(user.id.eq(post.userId)) // userId가 있는 게시물만 조회 따라서, leftjoin이 아닌
                                                                        // innerJoin
                                .leftJoin(category).on(category.id.eq(post.categoryId)) // 카테고리는 optional 즉, 카테고리가 없는
                                                                                        // 게시물도 조회
                                .leftJoin(featuredImage).on(featuredImage.id.eq(post.featuredImageId)) // 이미지도 optional
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

        private Page<PostResponse> executeQueryDSLQueryForUserPageAndOwner(BooleanBuilder builder, Pageable pageable) {

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
                                                post.postStatus,
                                                user.username,
                                                category.name,
                                                Projections.constructor(FeaturedImageResponse.class,
                                                                featuredImage.fileName,
                                                                featuredImage.fileUrl,
                                                                featuredImage.fileType,
                                                                featuredImage.fileSize)))
                                .from(post)
                                .join(user).on(user.id.eq(post.userId))
                                .leftJoin(category).on(category.id.eq(post.categoryId))
                                .leftJoin(featuredImage).on(featuredImage.id.eq(post.featuredImageId))
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
                return userId != null ? QPost.post.userId.eq(userId) : null;
        }

        private BooleanExpression categoryIdEq(String categoryId) {
                return StringUtils.hasText(categoryId) ? QPost.post.categoryId.eq(categoryId) : null;
        }

}
