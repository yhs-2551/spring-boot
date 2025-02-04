package com.yhs.blog.springboot.jpa.domain.post.repository;

import com.yhs.blog.springboot.jpa.domain.post.entity.FeaturedImage;
import org.springframework.data.jpa.repository.JpaRepository; 
 
public interface FeaturedImageRepository extends JpaRepository<FeaturedImage, Long> {
}
