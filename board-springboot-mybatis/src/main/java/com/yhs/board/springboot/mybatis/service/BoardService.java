package com.yhs.board.springboot.mybatis.service;

import com.yhs.board.springboot.mybatis.dao.BoardDAO;
import com.yhs.board.springboot.mybatis.dto.BoardDTO;
import com.yhs.board.springboot.mybatis.dto.BoardFileDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BoardService {

    private final BoardDAO boardDAO;

//
//    아래는 단일 파일 첨부 방식
//    public void save(BoardDTO boardDTO) throws IOException {
//
//        if (boardDTO.getBoardFile().isEmpty()) {
//            boardDTO.setFileAttached(0);
//            boardDAO.save(boardDTO);
//        } else {
//
//            boardDTO.setFileAttached(1);
//            BoardDTO saveBoard = boardDAO.save(boardDTO);
//            MultipartFile boardFile = boardDTO.getBoardFile();
//            String originalFileName = boardFile.getOriginalFilename();
//            String storedFileName = System.currentTimeMillis() + "-" + originalFileName;
//            BoardFileDTO boardFileDTO = new BoardFileDTO();
//            boardFileDTO.setOriginalFileName(originalFileName);
//            boardFileDTO.setStoredFileName(storedFileName);
//            boardFileDTO.setBoardId(saveBoard.getId());
//
//            String savePath = "E:/유현수/_유현수/B/Java/springboot/board-springboot-mybatis/file/" + storedFileName;
//            boardFile.transferTo(new File(savePath));
//            boardDAO.saveFile(boardFileDTO);
//        }
//    }

    public void save(BoardDTO boardDTO) throws IOException {

        if (boardDTO.getBoardFileList().get(0).isEmpty()) {
            boardDTO.setFileAttached(0);
            boardDAO.save(boardDTO);
        } else {

            boardDTO.setFileAttached(1);
            BoardDTO saveBoard = boardDAO.save(boardDTO);

            for (MultipartFile boardFile : boardDTO.getBoardFileList()) {

                String originalFileName = boardFile.getOriginalFilename();
                String storedFileName = System.currentTimeMillis() + "-" + originalFileName;
                BoardFileDTO boardFileDTO = new BoardFileDTO();
                boardFileDTO.setOriginalFileName(originalFileName);
                boardFileDTO.setStoredFileName(storedFileName);
                boardFileDTO.setBoardId(saveBoard.getId());

                String savePath = "E:/유현수/_유현수/B/Java/springboot/board-springboot-mybatis/file/" + storedFileName;
                boardFile.transferTo(new File(savePath));
                boardDAO.saveFile(boardFileDTO);
            }
        }
    }

    public List<BoardDTO> findAll() {
        return boardDAO.findAll();
    }

    public void updateHits(Long id) {
        boardDAO.updateHits(id);
    }

    public BoardDTO findById(Long id) {
        return boardDAO.findById(id);
    }

    public void update(BoardDTO boardDTO) {
        boardDAO.update(boardDTO);
    }

    public void delete(Long id) {
        boardDAO.delete(id);
    }

//     아래는 단일 파일 첨부 방식
//    public BoardFileDTO findFile(Long id) {
//        return boardDAO.findFile(id);
//    }

    // 아래는 다중 파일 첨부 방식
    public List<BoardFileDTO> findFileList(Long id) {
        return boardDAO.findFileList(id);
    }
}
