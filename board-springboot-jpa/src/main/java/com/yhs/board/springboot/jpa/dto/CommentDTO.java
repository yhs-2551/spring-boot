package com.yhs.board.springboot.jpa.dto;

import com.yhs.board.springboot.jpa.entity.BoardEntity;
import com.yhs.board.springboot.jpa.entity.CommentEntity;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
public class CommentDTO {
    private Long id;
    private String CommentWriter;
    private String commentContents;
    private Long boardId;
    private LocalDateTime commentCreatedTime;

    public static CommentDTO toCommentDTO(CommentEntity commentEntity, Long boardId) {
        CommentDTO commentDTO = new CommentDTO();
        commentDTO.setId(commentEntity.getId());
        commentDTO.setCommentWriter(commentEntity.getCommentWriter());
        commentDTO.setCommentContents(commentEntity.getCommentContents());
        commentDTO.setCommentCreatedTime(commentEntity.getCreatedTime());
//      commentDTO.setBoardId(commentEntity.getBoardEntity().getId()); 이 구조를 사용하려면 Service 메서드에 @Transactional을 사용해야 한다.
//        이유는 getBoardEntity()로 가져올때 BoardEntity의 필드가 Lazy 로딩으로 설정되어 있다면 실제로 데이터베이스에서 해당 엔티티를 로드하기 때문이다.
//        즉, Lazy로 가져올 경우 Transactional처리가 필요하다
        commentDTO.setBoardId(boardId);
        return commentDTO;
    }
}
