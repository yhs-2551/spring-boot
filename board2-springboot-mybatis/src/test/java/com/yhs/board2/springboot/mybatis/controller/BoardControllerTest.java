package com.yhs.board2.springboot.mybatis.controller;

import com.yhs.board2.springboot.mybatis.domain.BoardDTO;
import com.yhs.board2.springboot.mybatis.service.BoardService;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@Log4j2
@SpringBootTest
@ExtendWith(SpringExtension.class)
@AutoConfigureMockMvc
public class BoardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private BoardService boardService;

//    @Test
//    public void testList() throws Exception {
//        log.info(mockMvc.perform(get("/board/list")).andReturn().getModelAndView().getModelMap());
//    }

    @Test
    public void testListPaging() throws Exception {
        log.info("컨트롤러 페이징 데이터 테스트 결과 >>> " + mockMvc.perform(
                MockMvcRequestBuilders.get("/board/list").param("pageNum", "2").param("amount", "10")
        ).andReturn().getModelAndView().getModelMap());
    }

    @Test
    public void testRegister() throws Exception {
        String resultPage = mockMvc.perform(MockMvcRequestBuilders.post("/board/register")
                .param("title", "포스트 요청 테스트 제목")
                .param("content", "포스트 요청 테스트 내용")
                .param("writer", "포스트 요청 테스트 작성자")).andReturn().getModelAndView().getViewName();

        log.info("포스트 요청 테스트 결과 >>>> " + resultPage);
    }

    @Test
    public void testGet() throws Exception {
        log.info(mockMvc.perform(MockMvcRequestBuilders
                        .get("/board/get")
                        .param("bno", "2"))
                .andReturn().getModelAndView().getModelMap());
    }

    @Test
    public void testModify() throws Exception {
        String resultPage = mockMvc.perform(MockMvcRequestBuilders.post("/board/modify")
                .param("bno", "1")
                .param("title", "컨트롤러 수정 테스트 제목")
                .param("content", "컨트롤러 수정 테스트 내용")
                .param("writer", "컨트롤러 수정 테스트 작성자")).andReturn().getModelAndView().getViewName();

        log.info("컨트롤러 수정 테스트 결과 >>> " + resultPage);
    }

    @Test
    public void testRemove() throws Exception {
        String resultPage = mockMvc.perform(MockMvcRequestBuilders.post("/board/remove").param("bno", "9"))
                .andReturn().getModelAndView().getViewName();

        log.info("컨트롤러 삭제 테스트 결과 >>>> " + resultPage);
    }
}
