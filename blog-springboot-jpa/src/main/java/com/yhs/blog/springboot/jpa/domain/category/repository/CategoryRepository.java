package com.yhs.blog.springboot.jpa.domain.category.repository;

import com.yhs.blog.springboot.jpa.domain.category.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, String> {

    // 부모 카테고리가 NULL 즉, 최상위 카테고리를 가져오면서 자식도 함께 가져오는데, LEFT JOIN으로 인해 자식이 없는 부모도 가져올
    // 수 있다.
    // 또한 해당 사용자의 카테고리를 가져오며, cateogry 엔티티에 orderIndex를 따로 부여해 프론트에서 요청하는 순서 및
    // insert로 삽입되는 순서
    // 그대로 조회딜 수 있도록 한다.
    // c.parent IS NULL 은 최상위 카테고리만 먼저 조회하기 위함. 즉 최상위 카테고리만 조회하면서 자식 카테고리도 같이 가져옴
    @Query("SELECT c FROM Category c LEFT JOIN FETCH c.children ch WHERE c.parent IS NULL AND c.user.id = :userId ORDER BY c.orderIndex ASC, ch.orderIndex ASC")
    List<Category> findAllWithChildrenByUserId(@Param("userId") Long userId);

    @Query("SELECT c FROM Category c LEFT JOIN FETCH c.parent WHERE c.id = :id")
    Optional<Category> findByIdWithParent(@Param("id") String id);

    Optional<Category> findByNameAndUserId(String name, Long userId);

}
