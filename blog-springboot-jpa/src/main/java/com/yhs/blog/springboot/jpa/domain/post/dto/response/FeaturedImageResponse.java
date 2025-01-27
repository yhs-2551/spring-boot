package com.yhs.blog.springboot.jpa.domain.post.dto.response;

import com.yhs.blog.springboot.jpa.domain.post.entity.FeaturedImage;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Getter
@NoArgsConstructor
@Log4j2
public class FeaturedImageResponse {
    private String fileName;
    private String fileType;
    private String fileUrl;
    private Long fileSize;

    public static FeaturedImageResponse from(FeaturedImage featuredImage) {

        FeaturedImageResponse response = new FeaturedImageResponse();
        response.fileName = featuredImage.getFileName();
        response.fileType = featuredImage.getFileType();
        response.fileUrl = featuredImage.getFileUrl();
        response.fileSize = featuredImage.getFileSize();
        return response;
    }

}
