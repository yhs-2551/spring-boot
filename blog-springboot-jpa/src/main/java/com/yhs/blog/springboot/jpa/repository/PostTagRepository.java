package com.yhs.blog.springboot.jpa.repository;

import com.yhs.blog.springboot.jpa.entity.PostTag;
import com.yhs.blog.springboot.jpa.entity.PostTagId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostTagRepository extends JpaRepository<PostTag, PostTagId> {


        @Query("SELECT DISTINCT pt.tag.id FROM PostTag pt WHERE pt.post.id = :postId")
        List<Long> findTagIdsByPostId(@Param("postId") Long postId);

}
