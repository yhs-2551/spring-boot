package com.yhs.board2.springboot.mybatis.controller;

import com.yhs.board2.springboot.mybatis.domain.Criteria;
import com.yhs.board2.springboot.mybatis.domain.ReplyDTO;
import com.yhs.board2.springboot.mybatis.domain.ReplyPageDTO;
import com.yhs.board2.springboot.mybatis.service.ReplyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.coyote.Response;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequiredArgsConstructor
@Log4j2
@RequestMapping("/replies/")
public class ReplyController {

    private final ReplyService replyService;

    @PostMapping("/new")
    public ResponseEntity<String> create(@RequestBody ReplyDTO replyDTO) {
        int insertCount = replyService.register(replyDTO);
        log.info("Reply Insert Success Count >>>> " + insertCount);
        return insertCount == 1 ? new ResponseEntity<>("success", HttpStatus.OK) : new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }

//    아래는 댓글 페이징 처리 이전에 댓글 데이터 가져오는 방법
//    @GetMapping("/pages/{bno}/{page}")
//    public ResponseEntity<List<ReplyDTO>> getList(@PathVariable("page")int page, @PathVariable("bno")Long bno) {
//        Criteria cri = new Criteria(page, 10);
//
//        return new ResponseEntity<>(replyService.getList(cri, bno), HttpStatus.OK);
//    }

    @GetMapping("/pages/{bno}/{page}")
    public ResponseEntity<ReplyPageDTO> getList(@PathVariable("page")int page, @PathVariable("bno")Long bno) {
        Criteria cri = new Criteria(page, 10);

        return new ResponseEntity<>(replyService.getListPage(cri, bno), HttpStatus.OK);
    }


    @GetMapping("/{rno}")
    public ResponseEntity<ReplyDTO> get(@PathVariable("rno") Long rno) {
        return new ResponseEntity<>(replyService.get(rno), HttpStatus.OK);
    }

    @DeleteMapping("/{rno}")
    public ResponseEntity<String> remove(@PathVariable("rno") Long rno) {
        return replyService.remove(rno) == 1 ? new ResponseEntity<>("sucess", HttpStatus.OK) : new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @PatchMapping("/{rno}")
    public ResponseEntity<String> modify(@RequestBody ReplyDTO replyDTO, @PathVariable("rno") Long rno) {
        replyDTO.setRno(rno);
        return replyService.modify(replyDTO) == 1 ? new ResponseEntity<>("success", HttpStatus.OK)
                : new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }
}

