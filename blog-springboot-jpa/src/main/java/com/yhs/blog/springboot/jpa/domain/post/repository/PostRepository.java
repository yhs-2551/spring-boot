package com.yhs.blog.springboot.jpa.domain.post.repository;

 
import com.yhs.blog.springboot.jpa.domain.post.entity.Post;
 
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostRepository extends JpaRepository<Post, Long>, PostRepositoryCustom {
    
    List<Post> findByUserId(Long userId);

}
