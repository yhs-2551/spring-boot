package com.yhs.board.springboot.mybatis.controller;

import com.yhs.board.springboot.mybatis.dto.CommentDTO;
import com.yhs.board.springboot.mybatis.service.CommentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Log4j2
@Controller
@RequiredArgsConstructor
@RequestMapping("/comment")
public class CommentController {

    private final CommentService commentService;

    // RESPONSE ENTITY 써야함 자세한건 JPA 코드 참고.
    @PostMapping("/save")
    @ResponseBody
    public ResponseEntity<?> addComment(CommentDTO commentDTO) {
        boolean isSuccess = commentService.insertComment(commentDTO);
        if (isSuccess) {
            List<CommentDTO> commentDTOList = commentService.getCommentsByBoardId(commentDTO.getBoardId());
            log.info("CommentDTO LIST >>>> {}", commentDTOList);
            return new ResponseEntity<>(commentDTOList, HttpStatus.OK);
        } else {
            // 삽입에 실패했을 경우
            return new ResponseEntity<>("Failed to add comment", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/{boardId}")
    public List<CommentDTO> getComments(@PathVariable Long boardId) {
        return commentService.getCommentsByBoardId(boardId);
    }
}
