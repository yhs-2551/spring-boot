package com.yhs.blog.springboot.jpa.domain.post.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.group.GroupBy;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.yhs.blog.springboot.jpa.aop.log.Loggable;
import com.yhs.blog.springboot.jpa.common.constant.code.ErrorCode;
import com.yhs.blog.springboot.jpa.domain.category.entity.QCategory;
import com.yhs.blog.springboot.jpa.domain.file.dto.response.FileResponse;
import com.yhs.blog.springboot.jpa.domain.file.entity.QFile;
import com.yhs.blog.springboot.jpa.domain.post.dto.response.FeaturedImageResponse;
import com.yhs.blog.springboot.jpa.domain.post.dto.response.PostIndexAndIndexSearchResponseWithPostTotal;
import com.yhs.blog.springboot.jpa.domain.post.dto.response.PostIndexAndIndexSearchResponse;
import com.yhs.blog.springboot.jpa.domain.post.dto.response.PostResponseForDetailPage;
import com.yhs.blog.springboot.jpa.domain.post.dto.response.PostResponseForEditPage;
import com.yhs.blog.springboot.jpa.domain.post.dto.response.PostUserPageResponse;
import com.yhs.blog.springboot.jpa.domain.post.dto.response.PostUserPageResponseWithPostTotal;
import com.yhs.blog.springboot.jpa.domain.post.dto.response.QPostIndexAndIndexSearchResponseWithPostTotal;
import com.yhs.blog.springboot.jpa.domain.post.dto.response.QPostResponseForDetailPage;
import com.yhs.blog.springboot.jpa.domain.post.dto.response.QPostResponseForEditPage;
import com.yhs.blog.springboot.jpa.domain.post.dto.response.QPostUserPageResponseWithPostTotal;
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
public class PostRepositoryCustomImpl implements PostRepositoryCustom {

        // 아래 쿼리들 여러 쿼리는 postStatus가 포함되어 있는지 없는지에 따라 다름. 나중에 postStatus상태에 따라 분기 처리 필요

        private final JPAQueryFactory queryFactory;

        @Loggable
        @Override
        public Page<PostIndexAndIndexSearchResponse> findPostsAllUser(String keyword, SearchType searchType,
                        Pageable pageable) { // postStatus
                // 필요x

                try {
                        log.info("[PostRepositoryCustomImpl] findPostsAllUser() 메서드 시작");
                        if (StringUtils.hasText(keyword)) {

                                log.info("[PostRepositoryCustomImpl] findPostsAllUser() 메서드 검색어가 있을때 분기 진행");

                                // 아래는 일반 DB 조회
                                QPost post = QPost.post;
                                QUser user = QUser.user;
                                QCategory category = QCategory.category;
                                QFeaturedImage featuredImage = QFeaturedImage.featuredImage;

                                List<PostIndexAndIndexSearchResponseWithPostTotal> results = queryFactory
                                                .from(post)
                                                .join(user).on(user.id.eq(post.userId))
                                                .leftJoin(category).on(category.id.eq(post.categoryId))
                                                .leftJoin(featuredImage).on(featuredImage.id.eq(post.featuredImageId))
                                                .where(searchByType(keyword, searchType))
                                                .orderBy(post.createdAt.desc(), post.id.desc())
                                                .offset(pageable.getOffset())
                                                .limit(pageable.getPageSize())
                                                .transform(GroupBy.groupBy(Expressions.constant(1)).list(
                                                                new QPostIndexAndIndexSearchResponseWithPostTotal(
                                                                                GroupBy.list(
                                                                                                Projections.constructor(
                                                                                                                PostIndexAndIndexSearchResponse.class,
                                                                                                                post.id,
                                                                                                                post.title,
                                                                                                                post.content,
                                                                                                                user.username,
                                                                                                                user.blogId,
                                                                                                                category.name,
                                                                                                                featuredImage.fileUrl,
                                                                                                                post.createdAt)),
                                                                                JPAExpressions
                                                                                                .select(post.count())
                                                                                                .from(post)
                                                                                                .where(searchByType(
                                                                                                                keyword,
                                                                                                                searchType))))); // .get()으로
                                                                                                                                 // 가져오면
                                                                                                                                 // list안에
                                                                                                                                 // 원소를
                                                                                                                                 // 바로
                                                                                                                                 // 추출

                                if (results.isEmpty()) {

                                        log.info("[PostRepositoryCustomImpl] findPostsAllUser() - DB 조회 결과가 없을때 분기 진행");

                                        return new PageImpl<>(List.of(), pageable, 0);
                                }

                                log.info("[PostRepositoryCustomImpl] findPostsAllUser() - DB 조회 결과가 있을때 분기 진행");

                                PostIndexAndIndexSearchResponseWithPostTotal result = results.get(0);
                                return new PageImpl<>(result.getContent(), pageable, result.getTotal());
                        }

                        log.info("[PostRepositoryCustomImpl] findPostsAllUser() 메서드 검색어가 없을때 분기 진행");

                        // 검색어가 없는 경우 기존 QueryDSL 사용
                        return commonQueryDSLQueryNotWithPostStatus(new BooleanBuilder(), pageable);

                } catch (Exception e) {

                        throw new SystemException(ErrorCode.QUERY_DSL_POSTS_ERROR, "게시글 목록 조회 중 오류가 발생 하였습니다.",
                                        "PostRepositoryCustomImpl", "findPostsAllUser", e);
                }
        }

        @Loggable
        @Override
        public Page<PostUserPageResponse> findPostsByUserId(Long userId, String keyword, SearchType searchType,
                        Pageable pageable) {
                try {
                        log.info("[PostRepositoryCustomImpl] findPostsByUserId() 메서드 시작");

                        if (StringUtils.hasText(keyword)) {

                                log.info("[PostRepositoryCustomImpl] findPostsByUserId() 메서드 검색어가 있을때 분기 진행");
                                QPost post = QPost.post;
                                QUser user = QUser.user;
                                QCategory category = QCategory.category;
                                QFeaturedImage featuredImage = QFeaturedImage.featuredImage;

                                List<PostUserPageResponseWithPostTotal> results = queryFactory
                                                .from(post)
                                                .join(user).on(user.id.eq(post.userId))
                                                .leftJoin(category).on(category.id.eq(post.categoryId))
                                                .leftJoin(featuredImage).on(featuredImage.id.eq(post.featuredImageId))
                                                .where(searchByType(keyword, searchType), userIdEq(userId))

                                                .orderBy(post.createdAt.desc(), post.id.desc())
                                                .offset(pageable.getOffset())
                                                .limit(pageable.getPageSize())
                                                .transform(GroupBy.groupBy(Expressions.constant(1)).list( // Expressions.constant(1)은
                                                                                                          // 하나로 그룹화
                                                                new QPostUserPageResponseWithPostTotal(
                                                                                GroupBy.list(
                                                                                                Projections.constructor(
                                                                                                                PostUserPageResponse.class,
                                                                                                                post.id,
                                                                                                                post.title,
                                                                                                                post.content,
                                                                                                                post.postStatus,
                                                                                                                user.username,
                                                                                                                category.name,
                                                                                                                featuredImage.fileUrl,
                                                                                                                post.createdAt)),
                                                                                JPAExpressions
                                                                                                .select(post.count())
                                                                                                .from(post)
                                                                                                .where(searchByType(
                                                                                                                keyword,
                                                                                                                searchType),
                                                                                                                userIdEq(userId)))));

                                if (results.isEmpty()) {

                                        log.info("[PostRepositoryCustomImpl] findPostsByUserId - DB 조회 결과가 없을때 분기 진행");

                                        return new PageImpl<>(List.of(), pageable, 0);
                                }

                                log.info("[PostRepositoryCustomImpl] findPostsByUserId() - DB 조회 결과가 있을때 분기 진행");

                                PostUserPageResponseWithPostTotal result = results.get(0);
                                return new PageImpl<>(result.getContent(), pageable, result.getTotal());

                        }

                        log.info("[PostRepositoryCustomImpl] findPostsByUserId() - 메서드 검색어가 없을때 분기 진행");

                        // 검색어가 없는 경우 기존 QueryDSL 사용

                        QPost post = QPost.post;
                        BooleanBuilder builder = new BooleanBuilder();
                        builder.and(post.userId.eq(userId));
                        return commonQueryDSLQueryWithPostStatus(builder, pageable);

                } catch (Exception e) {

                        throw new SystemException(ErrorCode.QUERY_DSL_POSTS_ERROR, "게시글 목록 조회 중 오류가 발생 하였습니다.",
                                        "PostRepositoryCustomImpl", "findPostsByUserId", e);
                }

        }

        @Loggable
        @Override
        public Page<PostUserPageResponse> findPostsByUserIdAndCategoryId(Long userId, String categoryId, String keyword,
                        SearchType searchType, Pageable pageable) {

                try {
                        log.info("[PostRepositoryCustomImpl] findPostsByUserIdAndCategoryId() 메서드 시작");

                        if (StringUtils.hasText(keyword)) {

                                log.info("[PostRepositoryCustomImpl] findPostsByUserIdAndCategoryId() 메서드 검색어가 있을때 분기 진행");

                                QPost post = QPost.post;
                                QUser user = QUser.user;
                                QCategory category = QCategory.category;
                                QFeaturedImage featuredImage = QFeaturedImage.featuredImage;

                                List<PostUserPageResponseWithPostTotal> results = queryFactory
                                                .from(post)
                                                .join(user).on(user.id.eq(post.userId))
                                                .leftJoin(category).on(category.id.eq(post.categoryId))
                                                .leftJoin(featuredImage).on(featuredImage.id.eq(post.featuredImageId))
                                                .where(searchByType(keyword, searchType), userIdEq(userId),
                                                                categoryIdEq(categoryId))
                                                .orderBy(post.createdAt.desc(), post.id.desc())
                                                .offset(pageable.getOffset())
                                                .limit(pageable.getPageSize())
                                                .transform(GroupBy.groupBy(Expressions.constant(1)).list( // Expressions.constant(1)은
                                                                                                          // 하나로 그룹화
                                                                new QPostUserPageResponseWithPostTotal(
                                                                                GroupBy.list(
                                                                                                Projections.constructor(
                                                                                                                PostUserPageResponse.class,
                                                                                                                post.id,
                                                                                                                post.title,
                                                                                                                post.content,
                                                                                                                post.postStatus,
                                                                                                                user.username,
                                                                                                                category.name,
                                                                                                                featuredImage.fileUrl,
                                                                                                                post.createdAt)),
                                                                                JPAExpressions
                                                                                                .select(post.count())
                                                                                                .from(post)
                                                                                                .where(searchByType(
                                                                                                                keyword,
                                                                                                                searchType),
                                                                                                                userIdEq(userId),
                                                                                                                categoryIdEq(categoryId)))));

                                if (results.isEmpty()) {

                                        log.info("[PostRepositoryCustomImpl] findPostsByUserIdAndCategoryId - DB 조회 결과가 없을때 분기 진행");

                                        return new PageImpl<>(List.of(), pageable, 0);
                                }

                                log.info("[PostRepositoryCustomImpl] findPostsByUserIdAndCategoryId() - DB 조회 결과가 있을때 분기 진행");

                                PostUserPageResponseWithPostTotal result = results.get(0);
                                return new PageImpl<>(result.getContent(), pageable, result.getTotal());

                        }

                        log.info("[PostRepositoryCustomImpl] findPostsByUserIdAndCategoryId() 메서드 검색어가 없을때 분기 진행");

                        // 검색어가 없는 경우 기존 QueryDSL 사용
                        QPost post = QPost.post;
                        BooleanBuilder builder = new BooleanBuilder();
                        builder.and(post.userId.eq(userId))
                                        .and(post.categoryId.eq(categoryId));
                        return commonQueryDSLQueryWithPostStatus(builder, pageable);
                } catch (Exception e) {
                        throw new SystemException(ErrorCode.QUERY_DSL_POSTS_ERROR, "게시글 목록 조회 중 오류가 발생 하였습니다.",
                                        "PostRepositoryCustomImpl", "findPostsByUserIdAndCategoryId", e);
                }

        }

        // 상세 페이지용
        @Override
        public PostResponseForDetailPage findByIdNotWithFeaturedImage(Long postId) {

                log.info("[PostRepositoryCustomImpl] findByIdNotWithFeaturedImage() 메서드 시작");
                QPost post = QPost.post;
                QUser user = QUser.user;
                QCategory category = QCategory.category;
                QPostTag postTag = QPostTag.postTag;
                QTag tag = QTag.tag;
                QFile file = QFile.file;

                try {
                        return queryFactory
                                        .from(post)
                                        .join(user).on(user.id.eq(post.userId))
                                        .leftJoin(category).on(category.id.eq(post.categoryId))
                                        .leftJoin(postTag).on(postTag.postId.eq(post.id))
                                        .leftJoin(tag).on(postTag.tagId.eq(tag.id))
                                        .leftJoin(file).on(file.postId.eq(post.id))
                                        .where(post.id.eq(postId))
                                        .transform(GroupBy.groupBy(post.id).list(
                                                        new QPostResponseForDetailPage(
                                                                        post.title,
                                                                        post.content,
                                                                        GroupBy.list(tag.name),
                                                                        GroupBy.list(Projections.constructor(
                                                                                        FileResponse.class,
                                                                                        file.fileUrl,
                                                                                        file.width,
                                                                                        file.height)),
                                                                        post.postStatus,
                                                                        user.username,
                                                                        category.name,
                                                                        post.createdAt)))
                                        .get(0);

                } catch (Exception e) {
                        throw new SystemException(ErrorCode.QUERY_DSL_POSTS_ERROR, "게시글 목록 조회 중 오류가 발생 하였습니다.",
                                        "PostRepositoryCustomImpl", "findByIdNotWithFeaturedImage", e);
                }

        }

        // 수정 페이지 게시글 정보용
        @Override
        public PostResponseForEditPage findByIdWithFeaturedImage(Long postId) {

                log.info("[PostRepositoryCustomImpl] findByIdWithFeaturedImage() 메서드 시작");
                QPost post = QPost.post;
                QCategory category = QCategory.category;
                QPostTag postTag = QPostTag.postTag;
                QTag tag = QTag.tag;
                QFile file = QFile.file;
                QFeaturedImage featuredImage = QFeaturedImage.featuredImage;

                try {

                        return queryFactory
                                        .from(post)
                                        .leftJoin(category).on(category.id.eq(post.categoryId))
                                        .leftJoin(featuredImage).on(featuredImage.id.eq(post.featuredImageId))
                                        .leftJoin(postTag).on(postTag.postId.eq(post.id))
                                        .leftJoin(tag).on(postTag.tagId.eq(tag.id))
                                        .leftJoin(file).on(file.postId.eq(post.id))
                                        .where(post.id.eq(postId))
                                        .transform(GroupBy.groupBy(post.id).list(
                                                        new QPostResponseForEditPage(
                                                                        post.title,
                                                                        post.content,
                                                                        GroupBy.list(tag.name),
                                                                        GroupBy.list(Projections.constructor(
                                                                                        FileResponse.class,
                                                                                        file.fileName,
                                                                                        file.fileType,
                                                                                        file.fileUrl,
                                                                                        file.fileSize,
                                                                                        file.width,
                                                                                        file.height)),
                                                                        Projections.constructor(
                                                                                        FeaturedImageResponse.class,
                                                                                        featuredImage.fileName,
                                                                                        featuredImage.fileUrl,
                                                                                        featuredImage.fileType,
                                                                                        featuredImage.fileSize),
                                                                        post.postStatus,
                                                                        category.name)))
                                        .get(0);
                } catch (Exception e) {
                        throw new SystemException(ErrorCode.QUERY_DSL_POSTS_ERROR, "게시글 목록 조회 중 오류가 발생 하였습니다.",
                                        "PostRepositoryCustomImpl", "findByIdWithFeaturedImage", e);
                }

        }

        // 인덱스 및 인덱스 검색 페이지용
        private Page<PostIndexAndIndexSearchResponse> commonQueryDSLQueryNotWithPostStatus(BooleanBuilder builder,
                        Pageable pageable) {

                log.info("[PostRepositoryCustomImpl] commonQueryDSLQueryNotWithPostStatus() 메서드 시작");

                QPost post = QPost.post;
                QUser user = QUser.user;
                QCategory category = QCategory.category;
                QFeaturedImage featuredImage = QFeaturedImage.featuredImage;

                List<PostIndexAndIndexSearchResponseWithPostTotal> results = queryFactory
                                .from(post)
                                .join(user).on(user.id.eq(post.userId))
                                .leftJoin(category).on(category.id.eq(post.categoryId))
                                .leftJoin(featuredImage).on(featuredImage.id.eq(post.featuredImageId))
                                .where(builder)
                                .orderBy(post.createdAt.desc(), post.id.desc()) // 생성 날짜가 같은 경우 id로 추가 정렬
                                .offset(pageable.getOffset())
                                .limit(pageable.getPageSize())
                                .transform(GroupBy.groupBy(Expressions.constant(1)).list(
                                                new QPostIndexAndIndexSearchResponseWithPostTotal(
                                                                GroupBy.list(
                                                                                Projections.constructor(
                                                                                                PostIndexAndIndexSearchResponse.class,
                                                                                                post.id,
                                                                                                post.title,
                                                                                                post.content,
                                                                                                user.username,
                                                                                                user.blogId,
                                                                                                category.name,
                                                                                                featuredImage.fileUrl,
                                                                                                post.createdAt)),
                                                                JPAExpressions
                                                                                .select(post.count())
                                                                                .from(post)
                                                                                .where(builder))));
                if (results.isEmpty()) {

                        log.info("[PostRepositoryCustomImpl] commonQueryDSLQueryNotWithPostStatus - DB 조회 결과가 없을때 분기 진행");
                        return new PageImpl<>(List.of(), pageable, 0);
                }

                log.info("[PostRepositoryCustomImpl] commonQueryDSLQueryNotWithPostStatus() - DB 조회 결과가 있을때 분기 진행");
                PostIndexAndIndexSearchResponseWithPostTotal result = results.get(0);
                return new PageImpl<>(result.getContent(), pageable, result.getTotal());
        }

        // 사용자 페이지용
        private Page<PostUserPageResponse> commonQueryDSLQueryWithPostStatus(BooleanBuilder builder,
                        Pageable pageable) {

                log.info("[PostRepositoryCustomImpl] commonQueryDSLQueryWithPostStatus() 메서드 시작");

                QPost post = QPost.post;
                QUser user = QUser.user;
                QCategory category = QCategory.category;
                QFeaturedImage featuredImage = QFeaturedImage.featuredImage;

                List<PostUserPageResponseWithPostTotal> results = queryFactory
                                .from(post)
                                .join(user).on(user.id.eq(post.userId))
                                .leftJoin(category).on(category.id.eq(post.categoryId))
                                .leftJoin(featuredImage).on(featuredImage.id.eq(post.featuredImageId))
                                .where(builder)
                                .orderBy(post.createdAt.desc(), post.id.desc())
                                .offset(pageable.getOffset())
                                .limit(pageable.getPageSize())
                                .transform(GroupBy.groupBy(Expressions.constant(1)).list(
                                                new QPostUserPageResponseWithPostTotal(
                                                                GroupBy.list(
                                                                                Projections.constructor(
                                                                                                PostUserPageResponse.class,
                                                                                                post.id,
                                                                                                post.title,
                                                                                                post.content,
                                                                                                post.postStatus,
                                                                                                user.username,
                                                                                                category.name,
                                                                                                featuredImage.fileUrl,
                                                                                                post.createdAt)),
                                                                JPAExpressions
                                                                                .select(post.count())
                                                                                .from(post)
                                                                                .where(builder))));
                if (results.isEmpty()) {

                        log.info("[PostRepositoryCustomImpl] commonQueryDSLQueryWithPostStatus - DB 조회 결과가 없을때 분기 진행");
                        return new PageImpl<>(List.of(), pageable, 0);
                }

                log.info("[PostRepositoryCustomImpl] commonQueryDSLQueryWithPostStatus() - DB 조회 결과가 있을때 분기 진행");
                PostUserPageResponseWithPostTotal result = results.get(0);
                return new PageImpl<>(result.getContent(), pageable, result.getTotal());
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
