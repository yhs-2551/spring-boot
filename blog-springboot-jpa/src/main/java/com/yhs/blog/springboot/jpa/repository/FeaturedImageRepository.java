package com.yhs.blog.springboot.jpa.repository;

import com.yhs.blog.springboot.jpa.entity.FeaturedImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FeaturedImageRepository extends JpaRepository<FeaturedImage, Long> {
}
