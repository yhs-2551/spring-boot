package com.yhs.blog.springboot.jpa.domain.post.repository;

import com.yhs.blog.springboot.jpa.domain.post.entity.PostTag;
import com.yhs.blog.springboot.jpa.domain.post.entity.PostTagId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostTagRepository extends JpaRepository<PostTag, PostTagId> {

        // tag.id값의 경우 중복될 가능성이 없기 때문에 distinct 필요x
        @Query("SELECT pt.tag.id FROM PostTag pt WHERE pt.post.id = :postId")
        List<Long> findTagIdsByPostId(@Param("postId") Long postId);

}
