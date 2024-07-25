package com.yhs.board.springboot.jpa.dto;

import com.yhs.board.springboot.jpa.entity.BoardEntity;
import com.yhs.board.springboot.jpa.entity.BoardFileEntity;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@ToString
public class BoardDTO {
    private Long id;
    private String boardWriter;
    private String boardPass;
    private String boardTitle;
    private String boardContents;
    private int boardHits;
    private LocalDateTime boardCreatedTime;
    private LocalDateTime boardUpdatedTime;

//    아래는 단일 파일 첨부
//    private MultipartFile boardFile;
//    private String originalFileName;
//    private String storedFileName;

    //    아래는 다중 파일 첨부
    private List<MultipartFile> boardFile;
    private List<String> originalFileName;
    private List<String> storedFileName;
    private int fileAttached;

    public BoardDTO() {}

    public BoardDTO(Long id, String boardWriter, String boardTitle, int boardHits, LocalDateTime boardCreatedTime) {
        this.id = id;
        this.boardWriter = boardWriter;
        this.boardTitle = boardTitle;
        this.boardHits = boardHits;
        this.boardCreatedTime = boardCreatedTime;
    }


//    아래는 단일 파일 첨부 방식
//    public static BoardDTO toBoardDTO(BoardEntity boardEntity) {
//        BoardDTO boardDTO = new BoardDTO();
//        boardDTO.setId(boardEntity.getId());
//        boardDTO.setBoardWriter(boardEntity.getBoardWriter());
//        boardDTO.setBoardTitle(boardEntity.getBoardTitle());
//        boardDTO.setBoardPass(boardEntity.getBoardPass());
//        boardDTO.setBoardContents(boardEntity.getBoardContents());
//        boardDTO.setBoardHits(boardEntity.getBoardHits());
//        boardDTO.setBoardCreatedTime(boardEntity.getCreatedTime());
//        boardDTO.setBoardUpdatedTime(boardEntity.getUpdatedTime());
//        if(boardEntity.getFileAttached() == 0) {
//            boardDTO.setFileAttached(0);;
//        } else {
//            boardDTO.setFileAttached(boardEntity.getFileAttached());
//
//            boardDTO.setOriginalFileName(boardEntity.getBoardFileEntityList().get(0).getOriginalFileName());
//            boardDTO.setStoredFileName(boardEntity.getBoardFileEntityList().get(0).getStoredFileName());
//        }
//        return boardDTO;
//    }
//

    // 아래는 다중 파일 첨부
    public static BoardDTO toBoardDTO(BoardEntity boardEntity) {
        BoardDTO boardDTO = new BoardDTO();
        boardDTO.setId(boardEntity.getId());
        boardDTO.setBoardWriter(boardEntity.getBoardWriter());
        boardDTO.setBoardTitle(boardEntity.getBoardTitle());
        boardDTO.setBoardPass(boardEntity.getBoardPass());
        boardDTO.setBoardContents(boardEntity.getBoardContents());
        boardDTO.setBoardHits(boardEntity.getBoardHits());
        boardDTO.setBoardCreatedTime(boardEntity.getCreatedTime());
        boardDTO.setBoardUpdatedTime(boardEntity.getUpdatedTime());
        if(boardEntity.getFileAttached() == 0) {
            boardDTO.setFileAttached(0);;
        } else {
            List<String> originalFileNameList = new ArrayList<>();
            List<String> storedFileNameList = new ArrayList<>();
            boardDTO.setFileAttached(boardEntity.getFileAttached());

            for(BoardFileEntity boardFileEntity: boardEntity.getBoardFileEntityList()) {
                originalFileNameList.add(boardFileEntity.getOriginalFileName());
                storedFileNameList.add(boardFileEntity.getStoredFileName());
            }
            boardDTO.setOriginalFileName(originalFileNameList);
            boardDTO.setStoredFileName(storedFileNameList);
        }
        return boardDTO;
    }
}
