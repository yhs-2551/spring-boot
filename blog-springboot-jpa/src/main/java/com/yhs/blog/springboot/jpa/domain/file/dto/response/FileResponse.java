package com.yhs.blog.springboot.jpa.domain.file.dto.response;

import com.yhs.blog.springboot.jpa.domain.file.entity.File;
import lombok.*;

import java.util.Optional;

@NoArgsConstructor
@Getter
public class FileResponse {
    private String fileName;
    private String fileType;
    private String fileUrl;
    private Long fileSize;
    //일반 파일일 경우 width, height은 null, primitive type즉 int는 null을 허용하지 않음
    private Optional<Integer> width;
    private Optional<Integer> height;

    public FileResponse(File file) {
        this.fileName = file.getFileName();
        this.fileType = file.getFiletType();
        this.fileUrl = file.getFileUrl();
        this.fileSize = file.getFileSize();
        this.width = Optional.ofNullable(file.getWidth());
        this.height = Optional.ofNullable(file.getHeight());
    }
}
