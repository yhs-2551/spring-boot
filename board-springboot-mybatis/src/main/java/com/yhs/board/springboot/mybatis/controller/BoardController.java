package com.yhs.board.springboot.mybatis.controller;

import com.yhs.board.springboot.mybatis.dto.BoardDTO;
import com.yhs.board.springboot.mybatis.dto.BoardFileDTO;
import com.yhs.board.springboot.mybatis.dto.CommentDTO;
import com.yhs.board.springboot.mybatis.service.BoardService;
import com.yhs.board.springboot.mybatis.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.io.IOException;
import java.util.List;


@Controller
@RequiredArgsConstructor
public class BoardController {
    private final BoardService boardService;
    private final CommentService commentService;

    private static final Logger logger = LoggerFactory.getLogger(BoardController.class);

    @GetMapping("/save")
    public String save() {
        return "save";
    }

    @PostMapping("/save")
    public String save(BoardDTO boardDTO) {
        try {
            boardService.save(boardDTO);
        } catch (IOException e) {
            logger.error("An error occurred", e);
        }
        return "redirect:/list";
    }

    @GetMapping("/list")
    public String findAll(Model model) {
        List<BoardDTO> boardDTOList = boardService.findAll();
        model.addAttribute("boardList", boardDTOList);
        return "list";
    }


//    아래는 단일 파일 첨부 방식

//    @GetMapping("/{id}")
//    public String findById(@PathVariable("id") Long id, Model model) {
//        boardService.updateHits(id);
//        BoardDTO boardDTO = boardService.findById(id);
//        model.addAttribute("board", boardDTO);
//        if (boardDTO.getFileAttached() == 1) {
//            BoardFileDTO boardFileDTO = boardService.findFile(id);
//            model.addAttribute("boardFile", boardFileDTO);
//        }
//        return "detail";
//    }

//    아래는 다중 파일 첨부 방식

    @GetMapping("/{id}")
    public String findById(@PathVariable("id") Long id, Model model) {
        boardService.updateHits(id);
        BoardDTO boardDTO = boardService.findById(id);

        List<CommentDTO> commentDTOList = commentService.getCommentsByBoardId(id);

        model.addAttribute("board", boardDTO);
        model.addAttribute("comments", commentDTOList);
        if (boardDTO.getFileAttached() == 1) {
            List<BoardFileDTO> boardFileDTOList = boardService.findFileList(id);
            model.addAttribute("boardFileList", boardFileDTOList);
        }
        return "detail";
    }

    @GetMapping("/update/{id}")
    public String update(@PathVariable("id") Long id, Model model) {
        BoardDTO boardDTO = boardService.findById(id);
        model.addAttribute("board", boardDTO);
        return "update";
    }

    @PostMapping("/update/{id}")
    public String update(BoardDTO boardDTO, Model model) {
        boardService.update(boardDTO);
        BoardDTO dto = boardService.findById(boardDTO.getId());
        model.addAttribute("board", dto);
        return "detail";

    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable("id") Long id) {
        boardService.delete(id);
        return "redirect:/list";
    }

}
