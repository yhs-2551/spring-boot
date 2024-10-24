package com.yhs.blog.springboot.jpa.repository;

import com.yhs.blog.springboot.jpa.entity.File;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FileRepository extends JpaRepository<File, Long> {
}
