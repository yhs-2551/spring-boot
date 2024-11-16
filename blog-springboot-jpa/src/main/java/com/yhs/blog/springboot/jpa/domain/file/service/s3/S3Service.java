package com.yhs.blog.springboot.jpa.domain.file.service.s3;

import com.yhs.blog.springboot.jpa.domain.post.dto.request.PostRequest;
import com.yhs.blog.springboot.jpa.domain.post.dto.request.PostUpdateRequest;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface S3Service {

    String tempUploadFile(MultipartFile file, String folder, String userIdentifier) throws IOException;
//    public void tempDeleteFile(String fileUrl);
//    void moveTempFilesToFinal(String tempFileUrl, String finalFolder) throws IOException;
    @Async
    void processCreatePostS3TempOperation(PostRequest postRequest, String userIdentifier);
    @Async
    void processUpdatePostS3TempOperation(PostUpdateRequest postUpdateRequest,
                                          String userIdentifier);

}
