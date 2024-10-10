package com.yhs.board2.springboot.mybatis.domain;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Date;
import java.util.List;

@ToString
@Getter
@Setter
public class BoardDTO {
    private Long bno;
    private String title;
    private String content;
    private String writer;
    private Date regdate;
    private Date updateDate;

    private int replyCnt;

    private List<BoardAttachDTO> attachDTOList;
}
