package com.yhs.board2.springboot.mybatis.mapper;

import com.yhs.board2.springboot.mybatis.domain.MemberDTO;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@Log4j2
@SpringBootTest
public class MemberMapperTests {

    @Autowired
    private MemberMapper mapper;

    @Test
    public void testRead() {
        MemberDTO memberDTO = mapper.read("admin90");

        memberDTO.getAuthDTOList().forEach(authDto -> log.info(authDto));
    }
}
