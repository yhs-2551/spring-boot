package com.yhs.blog.springboot.jpa.domain.post.dto.request;

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
