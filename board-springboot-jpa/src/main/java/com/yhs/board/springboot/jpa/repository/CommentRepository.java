package com.yhs.board.springboot.jpa.repository;

import com.yhs.board.springboot.jpa.entity.BoardEntity;
import com.yhs.board.springboot.jpa.entity.CommentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<CommentEntity, Long> {
    List<CommentEntity> findAllByBoardEntityOrderByIdDesc(BoardEntity boardEntity);

}
