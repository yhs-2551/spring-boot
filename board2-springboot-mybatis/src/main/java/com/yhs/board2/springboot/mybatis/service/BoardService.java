package com.yhs.board2.springboot.mybatis.service;

import com.yhs.board2.springboot.mybatis.domain.BoardDTO;
import com.yhs.board2.springboot.mybatis.domain.Criteria;

import java.util.List;

public interface BoardService {

    public void register(BoardDTO board);

    public BoardDTO get(Long bno);

//    public List<BoardDTO> getList();

    public List<BoardDTO> getList(Criteria cri);

    public boolean modify(BoardDTO board);

    public boolean remove(Long bno);

    public int getTotal(Criteria cri);

}
