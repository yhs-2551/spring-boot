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
    void deleteByFileUrl(@Param("fileUrl") String fileUrl); // 일괄 처리로 인해 추가 조회없이 바로 삭제

    @Modifying
    @Query("DELETE FROM FeaturedImage f WHERE f.id = :featuredImageId")
    void deleteByIdInBatch(@Param("featuredImageId") Long featuredImageId);

    Optional<FeaturedImage> findByFileUrl(String fileUrl);

}
