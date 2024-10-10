package com.yhs.board2.springboot.mybatis.mapper;

import com.yhs.board2.springboot.mybatis.domain.BoardAttachDTO;

import java.util.List;

public interface BoardAttachMapper {
    public void insert(BoardAttachDTO boardAttachDTO);

    public void delete(String uuid);

    public List<BoardAttachDTO> findByBno(Long bno);

    public void deleteAll(Long bno);
}
