package com.yhs.board2.springboot.mybatis.mapper;

import com.yhs.board2.springboot.mybatis.domain.BoardDTO;
import com.yhs.board2.springboot.mybatis.domain.Criteria;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;


public interface BoardMapper {

//    @Select("select * from tbl_board where bno > 0")
//    public List<BoardDTO> getList();

    public BoardDTO get(Long bno);

    public List<BoardDTO> getListWithPaging(Criteria cri);

    public void insert(BoardDTO board);

    public void insertSelectKey(BoardDTO board);

    public  BoardDTO read(Long bno);

    public int update(BoardDTO board);

    public int delete(long bno);

    public int getTotalCount(Criteria cri);
}
