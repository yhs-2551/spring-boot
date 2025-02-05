package com.yhs.blog.springboot.jpa.domain.file.service;

import java.io.IOException;
import java.util.List;
import org.springframework.http.MediaType;
import org.springframework.web.multipart.MultipartFile;
import com.yhs.blog.springboot.jpa.domain.file.dto.request.FileRequest;

public interface FileService {

    String uploadTempFile(MultipartFile file, String folder, String blogId) throws IOException;

    byte[] getProxyImage(String url);

    MediaType getContentType(String url);

    void processCreateFilesForCreatePostRequest(List<FileRequest> fileRequests, Long postId);

    void processUpdateFilesForUpdatePostRequest(List<FileRequest> fileRequests, Long postId,
            List<String> deleteFileUrls);

    List<String> processDeleteFilesForDeletePostRequest(Long postId);

}
