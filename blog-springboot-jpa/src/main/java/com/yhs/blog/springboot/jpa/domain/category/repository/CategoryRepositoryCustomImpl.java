package com.yhs.blog.springboot.jpa.domain.category.repository;

import java.util.ArrayList; 
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;  
import org.springframework.stereotype.Repository; 
import com.querydsl.core.Tuple; 
import com.querydsl.jpa.JPAExpressions; 
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
    // 아래 querydsl의 groupy list를 사용하는 방식도 있는데, 오류가 자꾸 나서 JPAExpression + Tuple사용. 한번의 쿼리안에 서브쿼리(부모, 자식 총 게시글 개수)까지 포함하며 필요한 컬럼(필드)만 가져옴. 즉 최적화 완료
    // 또한 아래 tuple 방식 일단 사용하는데, 나중에 @QueryProjection 방식 사용 고려. 아래 코드 적용한 후에 알게 되었음
    @Override
    public List<CategoryWithChildrenResponse> findAllWithChildrenAndPostsByUserId(Long userId) {

        log.info("[CategoryRepositoryCustomImpl] findAllWithChildrenAndPostsByUserId 메서드 - 카테고리 목록 조회 메서드 실행");
        QCategory parent = QCategory.category;
        QCategory child = new QCategory("child"); // alias는 아무거나 사용 가능
        QPost post = QPost.post;

        try {
            // 부모 카테고리와 자식 카테고리 한번에 조회
            // 튜플은 배열과 유사하다고 보면 된다. 순서가 있고 불변하다. 
            // 아래 List<Tuple>은 List배열안에 쿼리 결과가 Tuple타입으로 여러개 들어가 있는 형태. 
            // Tuple은 쿼리 결과의 행을 가지고 있으며, 해당 행에 각 열에 대한 데이터도 담고 있다. 쉽게 (parentId: 값, parentName: 값, ...)
            List<Tuple> results = queryFactory 
                    .select(
                            parent.id,
                            parent.name,
                            child.id,
                            child.name,
                            // 부모 카테고리의 게시글 수
                            JPAExpressions
                                    .select(post.count())
                                    .from(post)
                                    .where(post.categoryId.eq(parent.id)),
                            // 자식 카테고리의 게시글 수
                            JPAExpressions
                                    .select(post.count())
                                    .from(post)
                                    .where(post.categoryId.eq(child.id)))
                    .from(parent)
                    .leftJoin(child).on(child.parentId.eq(parent.id))
                    .where(parent.parentId.isNull(),
                            parent.userId.eq(userId))
                    .orderBy(parent.orderIndex.asc(), child.orderIndex.asc())
                    .fetch();

            // 결과를 DTO로 변환
            Map<String, CategoryWithChildrenResponse> parentMap = new LinkedHashMap<>(); // 링크드 해쉬맵을 통해 순서 보장. 순서 유지를 위해 추가 메모리 공간이 필요하긴 하지만 순서 보장

            for (Tuple row : results) {
                String parentId = row.get(parent.id);

                // 부모 카테고리 처리
                if (!parentMap.containsKey(parentId)) {
                    parentMap.put(parentId, new CategoryWithChildrenResponse(
                            parentId,
                            row.get(parent.name),
                            null, // 부모는 항상 null
                            new ArrayList<>(), // 자식은 일단 빈 배열로 처리하고 아래에서 추가 처리
                            row.get(4, Long.class) // 부모의 게시글 수. 튜플의 인덱스는 배열처럼 0부터 시작
                    ));
                }

                // 자식 카테고리가 있는 경우 추가
                String childId = row.get(child.id);
                if (childId != null) {
                    CategoryChildResponse childResponse = new CategoryChildResponse(
                            childId,
                            row.get(child.name),
                            parentId,
                            Collections.emptyList(), // 항상 빈 배열
                            row.get(5, Long.class) // 자식의 게시글 수
                    );
                    parentMap.get(parentId).children().add(childResponse);
                }
            }

            return new ArrayList<>(parentMap.values()); // values()를 통해 Collection<CategoryWithChildrenResponse> 즉 collection타입의 원소들을 반환하며, 순서 보장을 위해 ArrayList의 원소들로 반환 

        } catch (Exception e) {
            throw new SystemException(ErrorCode.QUERY_DSL_CATEGORIES_ERROR, "카테고리 목록 조회 중 오류가 발생 하였습니다.",
                    "CategoryRepositoryCustomImpl", "findAllWithChildrenAndPostsByUserId", e);
        }

    }

}

// coalesce()사용으로 값이 없을 경우 0으로 반환