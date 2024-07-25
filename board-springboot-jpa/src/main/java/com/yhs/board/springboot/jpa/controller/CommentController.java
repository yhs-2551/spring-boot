package com.yhs.board.springboot.jpa.controller;

import com.yhs.board.springboot.jpa.dto.CommentDTO;
import com.yhs.board.springboot.jpa.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/comment")
public class CommentController {
    private final CommentService commentService;

    @PostMapping("/save")
    @ResponseBody
    public ResponseEntity<?> save(CommentDTO commentDTO) {
        Long saveResult = commentService.save(commentDTO);
        if (saveResult != null) {
         List<CommentDTO> commentDTOList = commentService.findAll(commentDTO.getBoardId());
         return new ResponseEntity<>(commentDTOList, HttpStatus.OK);
        }
        return new ResponseEntity<>("댓글 작성 실패", HttpStatus.INTERNAL_SERVER_ERROR);
    }



}
