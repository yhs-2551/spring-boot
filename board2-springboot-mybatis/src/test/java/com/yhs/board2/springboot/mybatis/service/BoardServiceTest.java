package com.yhs.board2.springboot.mybatis.service;

import com.yhs.board2.springboot.mybatis.domain.BoardDTO;
import com.yhs.board2.springboot.mybatis.domain.Criteria;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Log4j2
public class BoardServiceTest {

    @Autowired
    private BoardService boardService;

    @Test
    public void testExist() {
        log.info("서비스 존재 유무 >>> " + boardService);
        assertNotNull(boardService);
    }

    @Test
    public void testRegister() {
        BoardDTO boardDTO = new BoardDTO();
        boardDTO.setTitle("새로운 제목");
        boardDTO.setContent("새로운 내용");
        boardDTO.setWriter("새로운 작성자");
        boardService.register(boardDTO);


        //insertSelectKey를 통해 새롭게 생성될 bno의 값을 가져왔기 때문에 bno의 값이 조회가 가능한 것
        log.info("생성된 게시물의 번호 >>> {}", boardDTO.getBno());

    }

    @Test
    public void testGetList() {
//        boardService.getList().forEach(boardDTO -> log.info("getList >>>> " + boardDTO));
        boardService.getList(new Criteria(2, 10)).forEach(board -> log.info("보드 리스트 값 >>>" + board));
    }

    @Test
    public void testGet() {
        log.info(boardService.get(1L));
    }

    @Test
    public void testUpdate() {
        BoardDTO boardDTO = boardService.get(1L);

        if (!(boardDTO == null)) {
            boardDTO.setTitle("제목 수정");
            log.info("수정 결과 >>>> {}", boardService.modify(boardDTO));
        }
    }

    @Test
    public void testDelete() {
        log.info("삭제 결과 >>> ${}", boardService.remove(3L));
    }
}