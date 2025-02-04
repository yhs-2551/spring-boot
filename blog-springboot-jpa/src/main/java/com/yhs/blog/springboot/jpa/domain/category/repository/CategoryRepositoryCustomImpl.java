package com.yhs.blog.springboot.jpa.domain.category.repository;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Repository;

import com.querydsl.core.group.GroupBy;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.yhs.blog.springboot.jpa.common.constant.code.ErrorCode;
import com.yhs.blog.springboot.jpa.domain.category.dto.response.CategoryChildResponse;
import com.yhs.blog.springboot.jpa.domain.category.dto.response.CategoryWithChildrenResponse;
import com.yhs.blog.springboot.jpa.domain.category.entity.QCategory;
import com.yhs.blog.springboot.jpa.domain.post.entity.QPost;
import com.yhs.blog.springboot.jpa.exception.custom.SystemException;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@RequiredArgsConstructor
@Repository // 구현체 repository 어노테이션 추가 필요
@Log4j2
public class CategoryRepositoryCustomImpl implements CategoryRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    // 사용자의 모든 카테고리를 가져오며, 카테고리의 자식 카테고리와 포스트의 총 갯수도 함께 가져옴
    @Override
    public List<CategoryWithChildrenResponse> findAllWithChildrenAndPostsByUserId(Long userId) {

        log.info("[CategoryRepositoryCustomImpl] findAllWithChildrenAndPostsByUserId 메서드 - 카테고리 목록 조회 메서드 실행");

        try {

            QCategory parent = QCategory.category;
            QCategory child = new QCategory("child"); // alias 사용. 즉 인자로 아무거나 들어가도 된다.
            QPost parentPost = QPost.post;
            QPost childPost = new QPost("childPost");

            // parentPost.count() 집계 함수와 같이 사용하기 때문에 Group By를 통해 그룹화
            return queryFactory
                    .select(Projections.constructor(CategoryWithChildrenResponse.class,
                            parent.id,
                            parent.name,
                            Expressions.constant(null),
                            GroupBy.list(Projections.constructor(CategoryChildResponse.class,
                                    child.id,
                                    child.name,
                                    parent.id,
                                    Expressions.constant(new ArrayList<>()),
                                    childPost.count().coalesce(0L).intValue())), // coalesce()사용으로 값이 없을 경우 0으로 반환
                            parentPost.count().coalesce(0L).intValue()))
                    .from(parent)
                    .leftJoin(child).on(child.parentId.eq(parent.id))
                    .leftJoin(parentPost).on(parentPost.categoryId.eq(parent.id))
                    .leftJoin(childPost).on(childPost.categoryId.eq(child.id))
                    .where(parent.parentId.isNull(), parent.userId.eq(userId))
                    .groupBy(parent.id, parent.name)
                    .fetch();
        } catch (Exception e) {
            throw new SystemException(ErrorCode.QUERY_DSL_CATEGORIES_ERROR, "카테고리 목록 조회 중 오류가 발생 하였습니다.",
                    "CategoryRepositoryCustomImpl", "findAllWithChildrenAndPostsByUserId", e);
        }

    }

}
