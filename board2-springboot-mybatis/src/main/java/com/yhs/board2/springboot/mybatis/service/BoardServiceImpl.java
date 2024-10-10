package com.yhs.board2.springboot.mybatis.service;

import com.yhs.board2.springboot.mybatis.domain.BoardAttachDTO;
import com.yhs.board2.springboot.mybatis.domain.BoardDTO;
import com.yhs.board2.springboot.mybatis.domain.Criteria;
import com.yhs.board2.springboot.mybatis.mapper.BoardAttachMapper;
import com.yhs.board2.springboot.mybatis.mapper.BoardMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Log4j2
@Service
@RequiredArgsConstructor
public class BoardServiceImpl implements BoardService {
    private final BoardMapper mapper;
    private final BoardAttachMapper boardAttachMapper;


    // 먼저 게시글을 등록하고, 이후에 tbl_attach 테이블에 첨부된 파일을 저장한다. 따라서 transactional이 필요.
    @Override
    @Transactional
    public void register(BoardDTO boardDTO) {
        mapper.insertSelectKey(boardDTO);
        if (boardDTO.getAttachDTOList() == null || boardDTO.getAttachDTOList().size() <= 0) {
            return;
        }

        boardDTO.getAttachDTOList().forEach(attach -> {
            attach.setBno(boardDTO.getBno());
            boardAttachMapper.insert(attach);
        });
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


    // 아래 방식은 사용자가 게시글을 수정할 때 첨부파일 중 일부를 수정한다고 가정. 첨부 파일 먼저 전부 DB에서 삭제 후, 사용자가 수정 시 등록한 첨부파일만
    // tbl_attach 테이블에 저장한다.
    @Override
    @Transactional
    public boolean modify(BoardDTO board) {
        boardAttachMapper.deleteAll(board.getBno());

        boolean modifyResult = mapper.update(board) == 1;
        if (modifyResult && board.getAttachDTOList().size() > 0) {
            board.getAttachDTOList().forEach(attach -> {
                attach.setBno(board.getBno());
                boardAttachMapper.insert(attach);
            });
        }

        return modifyResult;
    }

    @Override
    @Transactional
    public boolean remove(Long bno) {
        boardAttachMapper.deleteAll(bno);
        return mapper.delete(bno) == 1;
    }

    @Override
    public List<BoardAttachDTO> getAttachList(Long bno) {
        return boardAttachMapper.findByBno(bno);
    }

}
