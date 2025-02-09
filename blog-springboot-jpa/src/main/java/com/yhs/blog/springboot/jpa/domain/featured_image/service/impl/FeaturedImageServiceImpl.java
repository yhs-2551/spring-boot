package com.yhs.blog.springboot.jpa.domain.featured_image.service.impl;

import java.util.Optional;

import org.springframework.stereotype.Service;

import com.yhs.blog.springboot.jpa.domain.featured_image.entity.FeaturedImage;
import com.yhs.blog.springboot.jpa.domain.featured_image.repository.FeaturedImageRepository;
import com.yhs.blog.springboot.jpa.domain.featured_image.service.FeaturedImageService;
import com.yhs.blog.springboot.jpa.domain.post.dto.request.FeaturedImageRequest;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Service
@RequiredArgsConstructor
@Log4j2
public class FeaturedImageServiceImpl implements FeaturedImageService {

    private final FeaturedImageRepository featuredImageRepository;

    @Override
    public Long processFeaturedImageForCreatePostRequest(FeaturedImageRequest featuredImageRequest) {

        log.info("[FeaturedImageServiceImpl] processFeaturedImageForCreatePostRequest 메서드 시작");

        Long featuredImageId;

        if (featuredImageRequest != null) {

            log.info(
                    "[FeaturedImageServiceImpl] processFeaturedImageForCreatePostRequest - featuredImageRequest != null 분기 시작");

            featuredImageId = processFeaturedImage(featuredImageRequest).getId();
        } else {

            log.info(
                    "[FeaturedImageServiceImpl] processFeaturedImageForCreatePostRequest - featuredImageRequest == null 분기 시작");

            featuredImageId = null;
        }

        return featuredImageId;

    }

    @Override
    public Long processFeaturedImageForUpdatePostRequest(FeaturedImageRequest featuredImageRequest) {

        log.info("[FeaturedImageServiceImpl] processFeaturedImageForUpdatePostRequest 메서드 시작");
        Long featuredImageId;

        if (featuredImageRequest != null) {

            log.info(
                    "[FeaturedImageServiceImpl] processFeaturedImageForUpdatePostRequest - featuredImageRequest != null 분기 진행");

            Optional<FeaturedImage> featuredImage = featuredImageRepository
                    .findByFileUrl(featuredImageRequest.getFileUrl());

            if (featuredImage.isEmpty()) {

                log.info(
                        "[FeaturedImageServiceImpl] processFeaturedImageForUpdatePostRequest - 대표이미지 db에 없을때 분기 진행");

                featuredImageId = processFeaturedImage(featuredImageRequest).getId();
            } else {

                log.info(
                        "[FeaturedImageServiceImpl] processFeaturedImageForUpdatePostRequest - 대표이미지 db에 있을때 분기 진행");

                featuredImageId = featuredImage.get().getId();
            }
        } else {

            log.info(
                    "[FeaturedImageServiceImpl] processFeaturedImageForUpdatePostRequest - featuredImageRequest == null 분기 진행");

            featuredImageId = null;
        }

        return featuredImageId;

    }

    @Override
    public void processDeleteFeaturedImageForUpdatePostRequest(String featuredImageFileUrl) {

        log.info("[FeaturedImageServiceImpl] processDeleteFeaturedImageForUpdatePostRequest 시작");

        featuredImageRepository.deleteByFileUrl(featuredImageFileUrl);
    }

    @Override
    public void processDeleteFeaturedImageForDeletePostRequest(Long featuredImageId) {
        
        log.info("[FeaturedImageServiceImpl] processDeleteFeaturedImageForDeletePostRequest 시작");

        // 단일 삭제 시 조회 이후 삭제하기 때문에 불필요한 조회 쿼리 발생. 따라서 추가 조회 없는 벌크 삭제 사용 
        featuredImageRepository.deleteByIdInBatch(featuredImageId);
    }

    private FeaturedImage processFeaturedImage(FeaturedImageRequest featuredImageRequest) {

        log.info("[FeaturedImageServiceImpl] processFeaturedImage 메서드 시작");

        // 최종적으로 post를 저장시에 aws 파일 저장 위치를 temp -> final로 변경하기 때문에 final로 변경하는 로직 추가. 따라서
        // db에 final 경로로 저장한다
        String updatedFileUrl = featuredImageRequest.getFileUrl().replace("/temp/", "/final/");
        FeaturedImage featuredImage = FeaturedImage.builder()
                .fileName(featuredImageRequest.getFileName())
                .fileUrl(updatedFileUrl)
                .fileType(featuredImageRequest.getFileType())
                .fileSize(featuredImageRequest.getFileSize())
                .build();
        featuredImageRepository.save(featuredImage);
        return featuredImage;

    }

}
