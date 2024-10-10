package com.yhs.board2.springboot.mybatis.domain;

import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class MemberDTO {
    private String userid;
    private String userpw;
    private String userName;
    private boolean enabled;

    private Date regDate;
    private Date updateDate;
    private List<MemberAuthDTO> authDTOList;

}
