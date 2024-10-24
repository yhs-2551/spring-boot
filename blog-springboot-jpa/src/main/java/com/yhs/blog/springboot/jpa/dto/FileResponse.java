package com.yhs.blog.springboot.jpa.dto;

import com.yhs.blog.springboot.jpa.entity.File;
import lombok.*;

import java.util.Optional;

@NoArgsConstructor
@Getter
public class FileResponse {
    private String fileName;
    private String fileType;
    private String fileUrl;
    private Long fileSize;
    //일반 파일일 경우 width, height는 null
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
