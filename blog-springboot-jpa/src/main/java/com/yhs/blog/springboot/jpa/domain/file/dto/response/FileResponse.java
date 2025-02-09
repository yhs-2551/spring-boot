package com.yhs.blog.springboot.jpa.domain.file.dto.response;

import com.querydsl.core.annotations.QueryProjection;
import com.yhs.blog.springboot.jpa.domain.file.entity.File;
import lombok.*;

import org.springframework.lang.Nullable;

@NoArgsConstructor
@Getter
public class FileResponse {
    private String fileName;
    private String fileType;
    private String fileUrl;
    private Long fileSize;
    // 일반 파일일 경우 width, height은 null, primitive type즉 int는 null을 허용하지 않음
    @Nullable
    private Integer width;
    @Nullable
    private Integer height;

    // 수정 페이지 응답용
    @QueryProjection
    public FileResponse(String fileName, String fileType, String fileUrl, Long fileSize, Integer width,
            Integer height) {
        this.fileName = fileName;
        this.fileType = fileType;
        this.fileUrl = fileUrl;
        this.fileSize = fileSize;
        this.width = width;
        this.height = height;
    }

    // 상세 페이지 응답용
    @QueryProjection
    public FileResponse(String fileUrl, Integer width, Integer height) {
        this.fileUrl = fileUrl;
        this.width = width;
        this.height = height;
    }

    // public FileResponse(File file) {
    // this.fileName = file.getFileName();
    // this.fileType = file.getFiletType();
    // this.fileUrl = file.getFileUrl();
    // this.fileSize = file.getFileSize();
    // this.width = file.getWidth();
    // this.height = file.getHeight();
    // }
}
