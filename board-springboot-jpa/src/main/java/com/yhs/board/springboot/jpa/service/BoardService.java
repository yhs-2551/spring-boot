package com.yhs.board.springboot.jpa.service;

import com.yhs.board.springboot.jpa.dto.BoardDTO;
import com.yhs.board.springboot.jpa.entity.BoardEntity;
import com.yhs.board.springboot.jpa.entity.BoardFileEntity;
import com.yhs.board.springboot.jpa.repository.BoardFileRepository;
import com.yhs.board.springboot.jpa.repository.BoardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BoardService {
    private final BoardRepository boardRepository;
    private final BoardFileRepository boardFileRepository;


//    아래는 단일 파일 첨부 방식
//    @Transactional
//    public void save(BoardDTO boardDTO) throws IOException {
//
//        if (boardDTO.getBoardFile().isEmpty()) {
//            boardRepository.save(BoardEntity.toSaveEntity(boardDTO));
//        } else {
//            MultipartFile boardFile = boardDTO.getBoardFile();
//            String originalFileName = boardFile.getOriginalFilename();
//            String storedFileName = System.currentTimeMillis() + "_" + originalFileName;
//            String savePath = "E:/유현수/_유현수/B/Java/springboot/board-springboot-jpa/file/" + storedFileName;
//            boardFile.transferTo(new File(savePath));
//            BoardEntity boardEntity = BoardEntity.toSaveFileEntity(boardDTO);
//            BoardEntity savedBoardEntity = boardRepository.save(boardEntity);
//            Long saveId = savedBoardEntity.getId(); //Entity를 DB에 저장하고 DB에 저장되어 있는 Entity의 컬럼 중 Id값을 가져옴.
//            BoardEntity fetchedBoardEntity = boardRepository.findById(saveId).get();
//
//            BoardFileEntity boardFileEntity = BoardFileEntity.toBoardFileEntity(fetchedBoardEntity, originalFileName, storedFileName);
//            boardFileRepository.save(boardFileEntity);
//
//
//        }
//    }

    // 다중 파일 첨부 방식
    @Transactional
    public void save(BoardDTO boardDTO) throws IOException {

        if (boardDTO.getBoardFile().isEmpty()) {
            boardRepository.save(BoardEntity.toSaveEntity(boardDTO));
        } else {
            BoardEntity boardEntity = BoardEntity.toSaveFileAttachedEntity(boardDTO);
            BoardEntity savedBoardEntity = boardRepository.save(boardEntity);
            Long saveId = savedBoardEntity.getId(); //Entity를 DB에 저장하고 DB에 저장되어 있는 Entity의 컬럼 중 Id값을 가져옴.
            BoardEntity fetchedBoardEntity = boardRepository.findById(saveId).get();

            for (MultipartFile boardFile : boardDTO.getBoardFile()) {

                String originalFileName = boardFile.getOriginalFilename();
                String storedFileName = System.currentTimeMillis() + "_" + originalFileName;
                String savePath = "E:/유현수/_유현수/B/Java/springboot/board-springboot-jpa/file/" + storedFileName;
                boardFile.transferTo(new File(savePath));


                BoardFileEntity boardFileEntity = BoardFileEntity.toBoardFileEntity(fetchedBoardEntity, originalFileName, storedFileName);
                boardFileRepository.save(boardFileEntity);
            }

        }
    }

    //  아래는 select문 관련 이지만 Transactional을 붙인 이유는 toBoardDTO 메서드 내용에서 부모 엔티티에서 지연 로딩을 사용하는 자식 엔티티에 접근하고 있기 때문에 Transactional 사용
    @Transactional
    public List<BoardDTO> findAll() {
        List<BoardEntity> boardEntityList = boardRepository.findAll();
        List<BoardDTO> boardDTOList = new ArrayList<>();
        for (BoardEntity boardEntity : boardEntityList) {
            boardDTOList.add(BoardDTO.toBoardDTO(boardEntity));
        }
        return boardDTOList;
    }

    @Transactional
    public void updateHits(Long id) {
        boardRepository.updateHits(id);
    }


    //  아래는 select문 관련 이지만 Transactional을 붙인 이유는 toBoardDTO 메서드 내에서 부모 엔티티에서 지연 로딩을 사용하는 자식 엔티티에 접근하고 있기 때문에 Transactional 사용
    @Transactional
    public BoardDTO findById(Long id) {
        Optional<BoardEntity> optionalBoardEntity = boardRepository.findById(id);
        return optionalBoardEntity.map(BoardDTO::toBoardDTO).orElse(null);
    }

    @Transactional
    public BoardDTO update(BoardDTO boardDTO) {
        BoardEntity boardEntity = BoardEntity.toUpdateEntity(boardDTO);
        boardRepository.save(boardEntity);
        return this.findById(boardDTO.getId());
    }

    @Transactional
    public void delete(Long id) {
        boardRepository.deleteById(id);
    }

    public Page<BoardDTO> paging(Pageable pageable) {
        int page = pageable.getPageNumber() - 1;
        int pageLimit = 3;

        Page<BoardEntity> boardEntities =
                boardRepository.findAll(PageRequest.of(page, pageLimit, Sort.by(Sort.Direction.DESC, "id")));

        Page<BoardDTO> boardDTOS = boardEntities.map(entity -> new BoardDTO(entity.getId(), entity.getBoardWriter(), entity.getBoardTitle(), entity.getBoardHits(), entity.getCreatedTime()));
        return boardDTOS;
    }

}
