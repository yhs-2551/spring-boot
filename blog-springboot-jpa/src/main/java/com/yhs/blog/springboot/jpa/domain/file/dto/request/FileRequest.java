package com.yhs.blog.springboot.jpa.domain.file.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@NoArgsConstructor
@Setter // 테스트 코드 작성시 필요
public class FileRequest {
    private String fileName;
    private String fileType;
    private String fileUrl;
    private Long fileSize;
    private Integer width; // 기본형 int로 지정하면 없는값일 경우 0으로 저장된다. 즉 null로 저장하고 싶어서 Integer로 지정.
    private Integer height;
}
