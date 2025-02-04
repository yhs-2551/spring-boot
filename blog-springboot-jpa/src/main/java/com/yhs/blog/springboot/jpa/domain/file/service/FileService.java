package com.yhs.blog.springboot.jpa.domain.file.service;

import java.io.IOException;
import java.util.Set;

import org.springframework.http.MediaType;
import org.springframework.web.multipart.MultipartFile;

import com.yhs.blog.springboot.jpa.domain.file.entity.File;

public interface FileService {

    String uploadTempFile(MultipartFile file, String folder, String blogId) throws IOException;

    byte[] getProxyImage(String url);

    MediaType getContentType(String url);

    void saveFiles(Set<File> files);

}
