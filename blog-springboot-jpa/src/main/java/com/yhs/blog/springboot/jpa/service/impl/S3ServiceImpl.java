package com.yhs.blog.springboot.jpa.service.impl;

import com.yhs.blog.springboot.jpa.dto.FileRequest;
import com.yhs.blog.springboot.jpa.dto.PostRequest;
import com.yhs.blog.springboot.jpa.dto.PostResponse;
import com.yhs.blog.springboot.jpa.dto.PostUpdateRequest;
import com.yhs.blog.springboot.jpa.service.S3Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CopyObjectRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Log4j2
@Service
@RequiredArgsConstructor
public class S3ServiceImpl implements S3Service {
    private final S3Client s3Client;

    @Value("${aws.s3.bucketName}")
    private String buketName;

    public String tempUploadFile(MultipartFile file, String folder) throws IOException {

        // 대표 이미지 구분하기 위함
        if (folder == null || folder.isEmpty()) {
            folder = Objects.requireNonNull(file.getContentType()).startsWith("image/") ?
                    "temp/images/" : "temp/files/";
        } else if (folder.equals("featured")) {
            folder = "temp/" + folder + "/";
        }
        String fileName = folder + UUID.randomUUID() + "-" + file.getOriginalFilename();
        try {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(buketName)
                    .key(fileName)
                    .contentType(file.getContentType())
                    .build();

            s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
            return s3Client.utilities().getUrl(builder -> builder.bucket(buketName).key(fileName)).toExternalForm();
        } catch (S3Exception e) {
            throw new IOException("Failed to upload file to S3", e);
        }


    }

    @Async
    @Override
    public void processCreatePostS3TempOperation(PostRequest postRequest) {
        try {

            // AWS S3 final 폴더에 최종 업로드 및 temp 폴더에 저장되어 있는 불필요한 이미지 및 파일 삭제 처리 로직

            List<String> tempfileUrls =
                    postRequest.getFiles().stream().map(FileRequest::getFileUrl).toList();

            processTempFilesToFinal(tempfileUrls);

            if (postRequest.getFeaturedImage() != null) {
                moveTempFilesToFinal(postRequest.getFeaturedImage().getFileUrl(),
                        "final/featured/");
            }

            log.info("getDeleteTempImageUrls: ", postRequest.getDeleteTempImageUrls());

            for (String tempFileUrl : postRequest.getDeleteTempImageUrls()) {
                tempDeleteFile(tempFileUrl);
            }

        } catch (IOException e) {
            log.error("Failed to process S3 operations", e);
        }
    }

    @Override
    @Async
    public void processUpdatePostS3TempOperation(PostUpdateRequest postUpdateRequest) {

        try {

            // AWS S3 final 폴더에 최종 업로드 및 temp 폴더에 저장되어 있는 불필요한 이미지 및 파일 삭제 처리 로직

            // 수정 요청을 했을때 수정 페이지에 기존에 존재하던(글 작성 시 발행된) final 경로 파일들은 제외하고 에디터에 새롭게 등록되는, 즉 파일 경로에
            // temp가 포함되어있는 파일들만 가져옴
            List<String> tempfileUrls = postUpdateRequest.getFiles().stream()
                    .map(FileRequest::getFileUrl)
                    .filter(url -> !url.contains("final/"))
                    .toList();

            processTempFilesToFinal(tempfileUrls);

            if (postUpdateRequest.getFeaturedImage() != null) {
                moveTempFilesToFinal(postUpdateRequest.getFeaturedImage().getFileUrl(),
                        "final/featured/");
            }

            log.info("getDeleteTempImageUrls: ", postUpdateRequest.getDeleteTempImageUrls());

            for (String tempFileUrl : postUpdateRequest.getDeleteTempImageUrls()) {
                tempDeleteFile(tempFileUrl);
            }

        } catch (IOException e) {
            log.error("Failed to process S3 operations", e);
        }
    }

    private void processTempFilesToFinal(List<String> tempfileUrls) throws IOException {
        for (String tempFileUrl : tempfileUrls) {
            String finalFolder;
            if (tempFileUrl.contains("/images/")) {
                finalFolder = "final/images/";
            } else {
                finalFolder = "final/files/";
            }
            moveTempFilesToFinal(tempFileUrl, finalFolder);
        }
    }


    private void moveTempFilesToFinal(String tempFileUrl, String finalFolder) throws IOException {
        String fileName = tempFileUrl.substring(tempFileUrl.lastIndexOf("/") + 1);

        log.info("fileName: " + fileName);

        fileName = URLDecoder.decode(fileName, StandardCharsets.UTF_8);
        String tempFolder;

        if (tempFileUrl.contains("/images/")) {
            tempFolder = "temp/images/";
        } else if (tempFileUrl.contains("/files/")) {
            tempFolder = "temp/files/";
        } else {
            tempFolder = "temp/featured/";
        }

        String tempFullPath = tempFolder + fileName;
        String finalFullPath = finalFolder + fileName;


        log.info("tempFullPath: " + tempFullPath);
        log.info("finalFullPath: " + finalFullPath);


        log.info("buketName: " + buketName);

        try {
            CopyObjectRequest copyObjectRequest = CopyObjectRequest.builder()
                    .sourceBucket(buketName)
                    .sourceKey(tempFullPath)
                    .destinationBucket(buketName)
                    .destinationKey(finalFullPath)
                    .build();

            s3Client.copyObject(copyObjectRequest);

            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                    .bucket(buketName)
                    .key(tempFullPath)
                    .build();

            s3Client.deleteObject(deleteObjectRequest);

        } catch (S3Exception e) {
            throw new IOException("Failed to move file from temp to final folder", e);
        }

    }

    private void tempDeleteFile(String fileUrl) {
        log.info("fileUrl: " + fileUrl);
        String fileName = fileUrl.substring(fileUrl.lastIndexOf("/") + 1);
        fileName = URLDecoder.decode(fileName, StandardCharsets.UTF_8);
        log.info("fileName: " + fileName);

        String folder;
        if (fileUrl.contains("temp/featured/")) {
            folder = "temp/featured/";
        } else if (fileUrl.contains("temp/images/")) {
            folder = "temp/images/";
        } else if (fileUrl.contains("temp/files/")) {
            folder = "temp/files/";
        } else if (fileUrl.contains("final/featured/")) {
            folder = "final/featured/";
        } else if (fileUrl.contains("final/images/")) {
            folder = "final/images/";
        } else {
            folder = "final/files/";
        }
        String fullPath = folder + fileName;

        try {
            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                    .bucket(buketName)
                    .key(fullPath)
                    .build();
            s3Client.deleteObject(deleteObjectRequest);
        } catch (S3Exception e) {
            throw new RuntimeException("Failed to delete file from S3", e);
        }
    }


}
