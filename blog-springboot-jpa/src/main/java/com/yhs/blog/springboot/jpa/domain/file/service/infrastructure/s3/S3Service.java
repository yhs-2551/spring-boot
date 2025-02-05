package com.yhs.blog.springboot.jpa.domain.file.service.infrastructure.s3;

import com.yhs.blog.springboot.jpa.domain.post.dto.request.PostRequest;
import com.yhs.blog.springboot.jpa.domain.post.dto.request.PostUpdateRequest;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface S3Service {

    String tempUploadFile(MultipartFile file, String folder, String blogId) throws IOException;

    String uploadProfileImage(MultipartFile file, String blogId) throws IOException;

    void deleteProfileImage(String blogId) throws IOException;

    // public void tempDeleteFile(String fileUrl);
    // void moveTempFilesToFinal(String tempFileUrl, String finalFolder) throws
    // IOException;
    @Async
    CompletableFuture<Void> processCreatePostS3TempOperation(PostRequest postRequest, String blogId);

    @Async
    CompletableFuture<Void> processUpdatePostS3TempOperation(PostUpdateRequest postUpdateRequest,
            String blogId);

    @Async
    CompletableFuture<Void> processDeletePostS3Operation(List<String> toBeDeletedFileUrls,
            String blogId);

}
