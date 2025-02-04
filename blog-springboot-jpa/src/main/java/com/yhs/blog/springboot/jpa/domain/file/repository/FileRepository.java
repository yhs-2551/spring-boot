package com.yhs.blog.springboot.jpa.domain.file.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.yhs.blog.springboot.jpa.domain.file.entity.File;

public interface FileRepository extends JpaRepository<File, Long> {

}
