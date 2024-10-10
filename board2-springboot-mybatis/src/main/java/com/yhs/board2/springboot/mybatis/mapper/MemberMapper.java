package com.yhs.board2.springboot.mybatis.mapper;

import com.yhs.board2.springboot.mybatis.domain.MemberDTO;

public interface MemberMapper {
    public MemberDTO read(String userid);
}
