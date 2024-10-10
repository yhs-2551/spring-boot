package com.yhs.board2.springboot.mybatis.domain;

import lombok.Data;

@Data
public class BoardAttachDTO {
    private String uuid;
    private String uploadPath;
    private String fileName;
    private boolean fileType;
    private Long bno;
}
