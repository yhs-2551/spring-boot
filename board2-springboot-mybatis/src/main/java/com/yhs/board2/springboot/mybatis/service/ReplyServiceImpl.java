package com.yhs.board2.springboot.mybatis.service;

import com.yhs.board2.springboot.mybatis.domain.Criteria;
import com.yhs.board2.springboot.mybatis.domain.ReplyDTO;
import com.yhs.board2.springboot.mybatis.domain.ReplyPageDTO;
import com.yhs.board2.springboot.mybatis.mapper.ReplyMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
@Log4j2
@RequiredArgsConstructor
public class ReplyServiceImpl implements ReplyService{

    private final ReplyMapper replyMapper;

    @Override
    public int register(ReplyDTO replyDTO) {
        return replyMapper.insert(replyDTO);
    }

    @Override
    public ReplyDTO get(Long rno) {
        return replyMapper.read(rno);
    }

    @Override
    public int modify(ReplyDTO replyDTO) {
        return replyMapper.update(replyDTO);
    }

    @Override
    public int remove(Long rno) {
        return replyMapper.delete(rno);
    }

    @Override
    public List<ReplyDTO> getList(Criteria cri, Long bno) {
       return replyMapper.getListWithPaging(cri, bno);
    }

    @Override
    public ReplyPageDTO getListPage(Criteria cri, Long bno) {
        return new ReplyPageDTO(replyMapper.getCountByBno(bno), replyMapper.getListWithPaging(cri, bno));
    }

}
