package com.yhs.board2.springboot.mybatis.service;

import com.yhs.board2.springboot.mybatis.domain.Criteria;
import com.yhs.board2.springboot.mybatis.domain.ReplyDTO;
import com.yhs.board2.springboot.mybatis.domain.ReplyPageDTO;

import java.util.List;


public interface ReplyService {
    public int register(ReplyDTO replyDTO);

    public ReplyDTO get(Long rno);

    public int modify(ReplyDTO replyDTO);

    public int remove(Long rno);

    public List<ReplyDTO> getList(Criteria cri, Long bno);

    public ReplyPageDTO getListPage(Criteria cri, Long bno);
}
