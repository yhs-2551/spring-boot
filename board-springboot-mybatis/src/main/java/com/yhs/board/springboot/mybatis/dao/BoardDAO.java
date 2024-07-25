package com.yhs.board.springboot.mybatis.dao;

import com.yhs.board.springboot.mybatis.dto.BoardDTO;
import com.yhs.board.springboot.mybatis.dto.BoardFileDTO;
import lombok.RequiredArgsConstructor;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class BoardDAO {

    private final SqlSessionTemplate sql;

    public BoardDTO save(BoardDTO boardDTO) {
        sql.insert("Board.save", boardDTO);
        return boardDTO;
    }

    public List<BoardDTO> findAll() {
        return sql.selectList("Board.findAll");
    }

    public void updateHits(Long id) {
        sql.update("Board.updateHits", id);
    }

    public BoardDTO findById(Long id) {
        return sql.selectOne("Board.findById", id);
    }

    public void update(BoardDTO boardDTO) {
        sql.update("Board.update", boardDTO);
    }

    public void delete(Long id) {
        sql.delete("Board.delete", id);
    }

    public void saveFile(BoardFileDTO boardFileDTO) {
        sql.insert("Board.saveFile", boardFileDTO);
    }

//
//    아래는 단일 파일 첨부 방식
//    public BoardFileDTO findFile(Long id) {
//        return sql.selectOne("Board.findFile", id);
//    }

//    아래는 다중 파일 첨부 방식. 단일 파일이든 다중 파일이든 같은 쿼리를 사용한다.
    public List<BoardFileDTO> findFileList(Long id) {
        return sql.selectList("Board.findFile", id);
    }
}
