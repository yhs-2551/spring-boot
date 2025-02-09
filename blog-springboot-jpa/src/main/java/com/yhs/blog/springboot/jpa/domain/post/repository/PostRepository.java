package com.yhs.blog.springboot.jpa.domain.post.repository;

import com.yhs.blog.springboot.jpa.domain.post.entity.Post;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PostRepository extends JpaRepository<Post, Long>, PostRepositoryCustom {

    @Query("SELECT p.featuredImageId FROM Post p WHERE p.id = :postId")
    Long findFeaturedImageIdByPostId(@Param("postId") Long postId);

}
