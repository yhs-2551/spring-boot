package com.yhs.blog.springboot.jpa.domain.post.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class FeaturedImageResponse {
    private final String fileName;
    private final String fileUrl;
    private final String fileType;
    private final Long fileSize;

}
