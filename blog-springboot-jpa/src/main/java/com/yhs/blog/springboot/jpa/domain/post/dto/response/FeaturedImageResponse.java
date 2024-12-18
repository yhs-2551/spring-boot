package com.yhs.blog.springboot.jpa.domain.post.dto.response;


import com.yhs.blog.springboot.jpa.domain.post.entity.FeaturedImage;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class FeaturedImageResponse {
    private String fileName;
    private String fileType;
    private String fileUrl;
    private Long fileSize;

    public FeaturedImageResponse(FeaturedImage featuredImage) {
        this.fileName = featuredImage.getFileName();
        this.fileType = featuredImage.getFileType();
        this.fileUrl = featuredImage.getFileUrl();
        this.fileSize = featuredImage.getFileSize();
    }
}
