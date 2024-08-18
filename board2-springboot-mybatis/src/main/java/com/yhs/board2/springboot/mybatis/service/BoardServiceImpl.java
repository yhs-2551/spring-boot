package com.yhs.board2.springboot.mybatis.service;

import com.yhs.board2.springboot.mybatis.domain.BoardDTO;
import com.yhs.board2.springboot.mybatis.domain.Criteria;
import com.yhs.board2.springboot.mybatis.mapper.BoardMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.util.List;

@Log4j2
@Service
@RequiredArgsConstructor
public class BoardServiceImpl implements BoardService {
    private final BoardMapper mapper;

    @Override
    public void register(BoardDTO boardDTO) {
        mapper.insertSelectKey(boardDTO);
    }

    @Override
    public BoardDTO get(Long bno) {
        return mapper.read(bno);
    }

    @Override
    public List<BoardDTO> getList(Criteria cri) {
        return mapper.getListWithPaging(cri);
    }

    @Override
    public int getTotal(Criteria cri) {
        return mapper.getTotalCount(cri);
    }
//
//    @Override
//    public List<BoardDTO> getList() {
//        return mapper.getList();
//    }

    @Override
    public boolean modify(BoardDTO board) {
        return mapper.update(board) == 1;
    }

    @Override
    public boolean remove(Long bno) {
        return mapper.delete(bno) == 1;
    }

}
