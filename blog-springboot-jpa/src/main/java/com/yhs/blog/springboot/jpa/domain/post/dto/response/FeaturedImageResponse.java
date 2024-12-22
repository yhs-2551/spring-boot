package com.yhs.blog.springboot.jpa.domain.post.dto.response;

import com.yhs.blog.springboot.jpa.domain.post.entity.FeaturedImage;
import com.yhs.blog.springboot.jpa.domain.post.repository.search.document.FeaturedImageDocument;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
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

    public static FeaturedImageResponse from(FeaturedImageDocument featuredImageDocument) {
        FeaturedImageResponse response = new FeaturedImageResponse();
        response.fileName = featuredImageDocument.getFileName();
        response.fileType = featuredImageDocument.getFileType();
        response.fileUrl = featuredImageDocument.getFileUrl();
        response.fileSize = featuredImageDocument.getFileSize();
        return response;
    }

}
