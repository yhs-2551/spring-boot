package com.yhs.board.springboot.mybatis.service;

import com.yhs.board.springboot.mybatis.dao.BoardDAO;
import com.yhs.board.springboot.mybatis.dao.CommentDAO;
import com.yhs.board.springboot.mybatis.dto.BoardDTO;
import com.yhs.board.springboot.mybatis.dto.CommentDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final BoardDAO boardDAO;
    private final CommentDAO commentDAO;


    public boolean insertComment(CommentDTO commentDTO) {
         int result = commentDAO.insertComment(commentDTO);
         return result > 0;
    }

    public List<CommentDTO> getCommentsByBoardId(Long boardId) {
        return commentDAO.getCommentsByBoardId(boardId);
    }


//    public List<CommentDTO> getCommentsByBoardId(Long boardId) {
//
//    }
}
