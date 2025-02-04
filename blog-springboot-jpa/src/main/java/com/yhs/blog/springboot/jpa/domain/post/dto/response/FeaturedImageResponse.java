package com.yhs.blog.springboot.jpa.domain.post.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Getter
@NoArgsConstructor
@Log4j2
@AllArgsConstructor
public class FeaturedImageResponse {
    private String fileName;
    private String fileUrl;
    private String fileType;
    private Long fileSize;

}
