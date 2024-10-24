package com.yhs.blog.springboot.jpa.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class FeaturedImageRequest {
    private String fileName;
    private String fileType;
    private String fileUrl;
    private Long fileSize;
}
