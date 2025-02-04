package com.yhs.blog.springboot.jpa.domain.post.repository;

import com.yhs.blog.springboot.jpa.domain.post.entity.Post;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param; 
import java.util.Optional;

public interface PostRepository extends JpaRepository<Post, Long>, PostRepositoryCustom {

    @Query("""
            SELECT p FROM Post p
            JOIN FETCH p.user u
            LEFT JOIN FETCH p.category c
            WHERE p.id = :postId
            """)
    Optional<Post> findByIdWithUserAndCategory(@Param("postId") Long postId);

    // 단일 게시글 이기 때문에 DISTINCT 불필요(카테시안 곱 미발생), user는 항상 존재하기 때문에 inner join, 나머지
    // left join
    @Query("""
            SELECT p FROM Post p
            JOIN FETCH p.user u
            LEFT JOIN FETCH p.category c
            LEFT JOIN FETCH p.featuredImage f
            WHERE p.id = :postId
            """)
    Optional<Post> findByIdWithUserAndCategoryAndFeaturedImage(@Param("postId") Long postId); // 하나의 게시글에 카테고리, 대표이미지,
                                                                                              // 사용자는 각각 1개만 존재하기 때문에
                                                                                              // 카테시안 곱 가능성 없음

    @Query("SELECT p FROM Post p JOIN FETCH p.user WHERE p.id = :postId")
    Optional<Post> findByIdWithUser(@Param("postId") Long postId);

}
