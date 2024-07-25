package com.yhs.board.springboot.jpa.repository;


import com.yhs.board.springboot.jpa.entity.BoardFileEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BoardFileRepository extends JpaRepository<BoardFileEntity, Long> {
}
