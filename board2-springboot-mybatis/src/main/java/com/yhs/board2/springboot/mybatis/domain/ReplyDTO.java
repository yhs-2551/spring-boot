package com.yhs.board2.springboot.mybatis.domain;

import lombok.*;

import java.util.Date;

@Getter
@Setter
@ToString
public class ReplyDTO {
    private Long rno;
    private Long bno;

    private String reply;
    private String replyer;
    private Date replyDate;
    private Date updateDate;

}
