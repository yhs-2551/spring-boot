package com.yhs.blog.springboot.jpa.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class FileRequest {
    private String fileName;
    private String fileType;
    private String fileUrl;
    private Long fileSize;
    private Integer width; // 기본형 int로 지정하면 없는값일 경우 0으로 저장된다. 즉 null로 저장하고 싶어서 Integer로 지정.
    private Integer height;
}
