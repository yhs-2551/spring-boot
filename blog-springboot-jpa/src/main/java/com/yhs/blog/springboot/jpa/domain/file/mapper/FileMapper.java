package com.yhs.blog.springboot.jpa.domain.file.mapper;

import com.yhs.blog.springboot.jpa.domain.file.dto.request.FileRequest;
import com.yhs.blog.springboot.jpa.domain.file.entity.File;
import com.yhs.blog.springboot.jpa.domain.post.entity.Post;
import com.yhs.blog.springboot.jpa.domain.user.entity.User;

public class FileMapper {
    public static File create(FileRequest fileRequest, Post post, User user,
                              String updatedFileUrl) {
        return File.builder()
                .fileName(fileRequest.getFileName())
                .filetType(fileRequest.getFileType())
                .fileUrl(updatedFileUrl)
                .fileSize(fileRequest.getFileSize())
                .width(fileRequest.getFileType().startsWith("image/")
                        ? (fileRequest.getWidth() != null ? fileRequest.getWidth() : null)
                        : null)
                .height(fileRequest.getFileType().startsWith("image/")
                        ? (fileRequest.getHeight() != null ? fileRequest.getHeight() : null)
                        : null)
                .post(post)
                .user(user)
                .build();
    }
}
