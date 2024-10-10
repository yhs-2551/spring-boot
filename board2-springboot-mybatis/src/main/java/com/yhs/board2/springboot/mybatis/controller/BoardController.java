package com.yhs.board2.springboot.mybatis.controller;

import com.yhs.board2.springboot.mybatis.domain.BoardAttachDTO;
import com.yhs.board2.springboot.mybatis.domain.BoardDTO;
import com.yhs.board2.springboot.mybatis.domain.Criteria;
import com.yhs.board2.springboot.mybatis.domain.PageDTO;
import com.yhs.board2.springboot.mybatis.service.BoardService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

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

    private void deleteFiles(List<BoardAttachDTO> attachDTOList) {
        if (attachDTOList == null || attachDTOList.size() == 0) {
            return;
        }

        attachDTOList.forEach(attach -> {
            try {
                Path file =
                        Paths.get("C:\\upload\\" + attach.getUploadPath() + "\\" + attach.getUuid() + "_" + attach.getFileName());

                Files.deleteIfExists(file);

                if (Files.probeContentType(file).startsWith("image")) {
                    Path thumNail =
                            Paths.get("C:\\upload\\" + attach.getUploadPath() + "\\s_" + attach.getUuid() + "_" + attach.getFileName());
                    Files.delete(thumNail);
                }
            } catch (Exception e) {
                log.error("delete file error" + e.getMessage());
            }
        });
    }

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

    // MediaType.APPLICATION_JSON_VALUE 기본적으로 UTF-8 인코딩을 사용함.
    @GetMapping(value = "/getAttachList", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<List<BoardAttachDTO>> getAttachList(Long bno) {
        return new ResponseEntity<>(boardService.getAttachList(bno), HttpStatus.OK);
    }

    // 로그인한 사용자만 사용 가능
    @GetMapping("/register")
    @PreAuthorize("isAuthenticated()")
    public String register() {
        return "register";
    }

    // 로그인한 사용자만 사용 가능
    @PostMapping("/register")
    @PreAuthorize("isAuthenticated()")
    public String register(BoardDTO boardDTO, RedirectAttributes rttr) {

        if (boardDTO.getAttachDTOList() != null) {
            boardDTO.getAttachDTOList().forEach(attach -> log.info(attach));
        }

        boardService.register(boardDTO);
        rttr.addFlashAttribute("result", boardDTO.getBno());
        return "redirect:/board/list";
    }

    @PreAuthorize("principal.username == #boardDTO.writer")
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

    @PreAuthorize("principal.username == #writer")
    @PostMapping("/remove")
    public String remove(@RequestParam("bno") Long bno, @ModelAttribute("cri") Criteria cri,
                         RedirectAttributes rttr, String writer) {

        List<BoardAttachDTO> attachDTOList = boardService.getAttachList(bno);

        if (boardService.remove(bno)) {
            deleteFiles(attachDTOList);
            rttr.addFlashAttribute("result", "success");
        }
//        rttr.addAttribute("type", cri.getType());
//        rttr.addAttribute("keyword", cri.getKeyword());
//        rttr.addAttribute("pageNum", cri.getPageNum());
//        rttr.addAttribute("amount", cri.getAmount());
        return "redirect:/board/list" + cri.getListLink();
    }
}
