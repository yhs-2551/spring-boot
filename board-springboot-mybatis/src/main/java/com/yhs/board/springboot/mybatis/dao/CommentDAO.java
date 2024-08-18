package com.yhs.board.springboot.mybatis.dao;

import com.yhs.board.springboot.mybatis.dto.BoardDTO;
import com.yhs.board.springboot.mybatis.dto.CommentDTO;
import lombok.RequiredArgsConstructor;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class CommentDAO {
    private final SqlSessionTemplate sql;


    public int insertComment(CommentDTO commentDTO) {
           int result = sql.insert("Board.saveComment", commentDTO);
           return result;
    }

    public List<CommentDTO> getCommentsByBoardId(Long boardId) {
        return sql.selectList("Board.getCommentsByBoardId", boardId);
    }
}

