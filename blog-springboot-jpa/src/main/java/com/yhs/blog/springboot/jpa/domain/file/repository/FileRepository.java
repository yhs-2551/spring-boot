package com.yhs.blog.springboot.jpa.domain.file.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.yhs.blog.springboot.jpa.domain.file.entity.File;

public interface FileRepository extends JpaRepository<File, Long> {

    @Modifying
    @Query("DELETE FROM File f WHERE f.fileUrl = :fileUrl")
    void deleteByFileUrl(@Param("fileUrl") String fileUrl);

    @Modifying
    @Query("DELETE FROM File f WHERE f.postId = :postId")
    void deleteFilesByPostId(@Param("postId") Long postId);

    // 한번에 postId 게시글에 관련된 모든 파일 삭제
    @Modifying
    @Query("DELETE FROM File f WHERE f.postId = :postId")
    void deleteByPostId(@Param("postId") Long postId);

    // 업데이트에서 전체 fileUrl 목록 불러올 때 필요, 삭제할 때 url을 조회하고, s3에서 삭제될 수 있도록 함
    @Query("SELECT f.fileUrl FROM File f WHERE f.postId = :postId")
    List<String> findFileUrlsByPostId(@Param("postId") Long postId);

}
