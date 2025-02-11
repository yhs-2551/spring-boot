package com.yhs.blog.springboot.jpa.domain.post.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@NoArgsConstructor
@Setter // 테스트할때 인스턴스화 하기 위해서 사용
public class FeaturedImageRequest {
    private String fileName;
    private String fileType;
    private String fileUrl;
    private Long fileSize;
}
