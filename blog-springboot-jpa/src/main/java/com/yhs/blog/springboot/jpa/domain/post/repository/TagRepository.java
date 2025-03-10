package com.yhs.blog.springboot.jpa.domain.post.repository;

import com.yhs.blog.springboot.jpa.domain.post.entity.Tag;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param; 

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface TagRepository extends JpaRepository<Tag, Long> {

    // 다른 트랜잭션이 데이터를 읽거나 수정할 수 없도록 완전히 잠금을 거는 비관적 잠금.
    // 포스트 생성 및 수정시 여러 사용자가 동시에 접근할 경우 동일한 태그가 중복으로 생성되는 것을 방지하기 위해 사용
    // Tag 엔티티에 name 필드에 unique 제약조건이 걸려있지만, 비관적 잠금이 없다면 여러 사용자가 동시에 접근할 경우 같은 태그를
    // 생성할 수도 있음.
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Tag> findByName(String tagName);

    @Query("""
                SELECT t FROM Tag t
                WHERE t.name IN :tagNames
                AND NOT EXISTS (
                    SELECT 1 FROM PostTag pt
                    WHERE pt.tagId = t.id
                    AND (pt.postId <> :postId)
                )
            """)
    List<Tag> findUnusedTagsByTagNames(@Param("tagNames") List<String> tagNames, @Param("postId") Long postId);

    @Query("""
                SELECT t FROM Tag t
                WHERE NOT EXISTS (
                    SELECT 1 FROM PostTag pt
                    WHERE pt.tagId = t.id
                    AND pt.postId != :postId
                )
            """)
    List<Tag> findUnusedTagsByPostId(@Param("postId") Long postId); // 결과가 없을시 빈 리스트 반환

    @Modifying
    @Query("DELETE FROM Tag t WHERE t.name IN :tagNames")
    void deleteByTagNames(@Param("tagNames") List<String> tagNames);

    @Query("DELETE FROM Tag t WHERE t IN :tags")
    @Modifying
    void deleteAllTags(@Param("tags") Collection<Tag> tags);

    // Select다음에 1은 존재 유무만 판단, AND NOT EXIST뒤에 있는 서브쿼리가 결과를 찾지 못하면(false 반환) AND NOT
    // EXIST가 true. 쉽게 찾지 못하면 AND NOT EXIST는 FALSE의 반대인 TRUE
    // @Modifying
    // @Query("""
    // DELETE FROM Tag t
    // WHERE t.id IN :tagIds
    // AND NOT EXISTS (
    // SELECT 1 FROM PostTag pt WHERE pt.tag.id = t.id
    // AND (pt.post.id != :postId OR pt.user.id != :userId)
    // )""")
    // void deleteUnusedTags(@Param("tagIds") List<Long> tagIds, @Param("postId")
    // Long postId, @Param("userId") Long userId);

}
