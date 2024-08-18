package com.yhs.board.springboot.mybatis.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Getter
@Setter
@ToString
public class BoardDTO {
    private Long id;
    private String boardWriter;
    private  String boardPassword;
    private String boardTitle;
    private String boardContents;
    private int boardHits;
    private String createdAt;
    private int fileAttached;

    //아래는 단일 파일 첨부 방식
//    private MultipartFile boardFile;

//    아래는 다중 파일 첨부 방식
    private List<MultipartFile> boardFileList;

}
