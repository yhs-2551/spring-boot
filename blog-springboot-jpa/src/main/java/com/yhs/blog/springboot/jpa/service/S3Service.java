package com.yhs.blog.springboot.jpa.service;

import com.yhs.blog.springboot.jpa.dto.PostRequest;
import com.yhs.blog.springboot.jpa.dto.PostUpdateRequest;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface S3Service {

    public String tempUploadFile(MultipartFile file, String folder) throws IOException;
//    public void tempDeleteFile(String fileUrl);
//    void moveTempFilesToFinal(String tempFileUrl, String finalFolder) throws IOException;
    @Async
    void processCreatePostS3TempOperation(PostRequest postRequest);
    @Async
    void processUpdatePostS3TempOperation(PostUpdateRequest postUpdateRequest);

}
