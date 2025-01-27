package com.yhs.blog.springboot.jpa.domain.file.service;

import java.io.IOException;

import org.springframework.http.MediaType;
import org.springframework.web.multipart.MultipartFile;

public interface FileService {

    String uploadTempFile(MultipartFile file, String folder, String blogId) throws IOException;

    byte[] getProxyImage(String url);

    MediaType getContentType(String url);

}
