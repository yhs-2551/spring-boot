package com.yhs.blog.springboot.jpa.domain.featured_image.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.yhs.blog.springboot.jpa.domain.featured_image.entity.FeaturedImage;

public interface FeaturedImageRepository extends JpaRepository<FeaturedImage, Long> {

    @Modifying
    @Query("DELETE FROM FeaturedImage fi WHERE fi.fileUrl = :fileUrl")
    void deleteByFileUrl(@Param("fileUrl") String fileUrl);

    @Modifying 
    @Query("DELETE FROM FeaturedImage fi WHERE fi.postId = :postId")
    void deleteFeaturedImageByPostId(@Param("postId") Long postId);

    Optional<FeaturedImage> findByFileUrl(String fileUrl);

}
