package com.yhs.board2.springboot.mybatis.controller;

import com.yhs.board2.springboot.mybatis.domain.BoardDTO;
import com.yhs.board2.springboot.mybatis.domain.Criteria;
import com.yhs.board2.springboot.mybatis.domain.PageDTO;
import com.yhs.board2.springboot.mybatis.service.BoardService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/board")
@RequiredArgsConstructor
@Log4j2
public class BoardController {
    private final BoardService boardService;

//    @GetMapping("/list")
//    public String list(Model model) {
//        model.addAttribute("list", boardService.getList());
//        return "list";
//    }

    @GetMapping("/list")
    public String list(Criteria cri, Model model) {

        model.addAttribute("list", boardService.getList(cri));
//        model.addAttribute("pageMaker", new PageDTO(cri, 123));

        int total = boardService.getTotal(cri);
        model.addAttribute("pageMaker", new PageDTO(cri, total));

        return "list";
    }


    @GetMapping({"/get", "/modify"})
    public String get(@RequestParam("bno") Long bno, Model model, @ModelAttribute("cri") Criteria cri, HttpServletRequest httpServletRequest) {
        model.addAttribute("board", boardService.get(bno));

        String requestURI = httpServletRequest.getRequestURI();

        if (requestURI.contains("/get")) {
            return "get";
        } else {
            return "modify";
        }
    }

    @GetMapping("/register")
    public String register() {
        return "register";
    }

    @PostMapping("/register")
    public String register(BoardDTO boardDTO, RedirectAttributes rttr) {
        boardService.register(boardDTO);
        rttr.addFlashAttribute("result", boardDTO.getBno());
        return "redirect:/board/list";
    }

    @PostMapping("/modify")
    public String modify(BoardDTO boardDTO, @ModelAttribute("cri") Criteria cri, RedirectAttributes rttr) {
        if (boardService.modify(boardDTO)) {
            rttr.addFlashAttribute("result", "success");
        }

        rttr.addAttribute("type", cri.getType());
        rttr.addAttribute("keyword", cri.getKeyword());
        rttr.addAttribute("pageNum", cri.getPageNum());
        rttr.addAttribute("amount", cri.getAmount());
        return "redirect:/board/list";
    }

    @PostMapping("/remove")
    public String remove(@RequestParam("bno") Long bno, @ModelAttribute("cri") Criteria cri, RedirectAttributes rttr) {
        if (boardService.remove(bno)) {
            rttr.addFlashAttribute("result", "success");
        }
        rttr.addAttribute("type", cri.getType());
        rttr.addAttribute("keyword", cri.getKeyword());
        rttr.addAttribute("pageNum", cri.getPageNum());
        rttr.addAttribute("amount", cri.getAmount());
        return "redirect:/board/list";
    }
}
