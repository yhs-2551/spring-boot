package com.yhs.board2.springboot.mybatis.mapper;

import com.yhs.board2.springboot.mybatis.domain.BoardDTO;
import com.yhs.board2.springboot.mybatis.domain.Criteria;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@Log4j2
@SpringBootTest
class BoardMapperTest {

    @Autowired
    private BoardMapper boardMapper;


    @Test
    public void testInsert() {
        BoardDTO boardDTO = new BoardDTO();
        boardDTO.setTitle("제목");
        boardDTO.setContent("내용");
        boardDTO.setWriter("작성자");
        boardMapper.insert(boardDTO);
        log.info("insert >>>> " + boardDTO);

    }

    @Test
    public void testInsertSelecKey() {
        BoardDTO boardDTO = new BoardDTO();
        boardDTO.setTitle("제목2");
        boardDTO.setContent("내용2");
        boardDTO.setWriter("작성자2");
        boardMapper.insertSelectKey(boardDTO);

        log.info("insertSelectKey >>> " + boardDTO);
    }

    @Test
    public void testRead() {
        BoardDTO boardDTO = boardMapper.read(5L);
        log.info("Read 결과 >>> " + boardDTO);
    }

    @Test
    public void testDelete() {
        log.info("DELETE 결과 >>> " + boardMapper.delete(3L));
    }

    @Test
    public void testUpdate() {
        BoardDTO boardDTO = new BoardDTO();
        boardDTO.setBno(2L);
        boardDTO.setTitle("수정 제목");
        boardDTO.setContent("수정 내용");
        boardDTO.setWriter("수정 작성자");

        int count = boardMapper.update(boardDTO);
        log.info("업데이트 결과 >> " + count);
    }

    @Test
    public void testPaging() {
        Criteria cri = new Criteria();
        cri.setAmount(11);
        cri.setPageNum(3);
        List<BoardDTO> list = boardMapper.getListWithPaging(cri);
        list.forEach(board -> log.info("테스트 페이징 보드값 >>> " + board));
    }

    @Test
    public void testSearch() {
        Criteria cri = new Criteria();
        cri.setKeyword("새로");
        cri.setType("TC");

        List<BoardDTO> list = boardMapper.getListWithPaging(cri);
        list.forEach(log::info);

        int count = boardMapper.getTotalCount(cri);
        log.info("count >>> " + count);
    }
}