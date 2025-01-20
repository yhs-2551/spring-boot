package com.yhs.blog.springboot.jpa.domain.file.service.infrastructure.s3.impl;

import com.yhs.blog.springboot.jpa.aop.log.Loggable;
import com.yhs.blog.springboot.jpa.common.constant.code.ErrorCode;
import com.yhs.blog.springboot.jpa.domain.file.dto.request.FileRequest;
import com.yhs.blog.springboot.jpa.domain.file.service.infrastructure.s3.S3Service;
import com.yhs.blog.springboot.jpa.domain.post.dto.request.PostRequest;
import com.yhs.blog.springboot.jpa.domain.post.dto.request.PostUpdateRequest;
import com.yhs.blog.springboot.jpa.exception.custom.S3OperationException;

import com.yhs.blog.springboot.jpa.exception.custom.SystemException;
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
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Log4j2
@Service
@RequiredArgsConstructor
public class S3ServiceImpl implements S3Service {
    private final S3Client s3Client;

    @Value("${aws.s3.bucketName}")
    private String bucketName;

    @Override
    @Loggable
    public String tempUploadFile(MultipartFile file, String folder, String blogId) throws IOException {

        // 대표 이미지 구분하기 위함
        if (folder == null || folder.isEmpty()) {
            folder = Objects.requireNonNull(file.getContentType()).startsWith("image/") ? blogId + "/temp/images/"
                    : blogId + "/temp/files/";
        } else if (folder.equals("featured")) {
            folder = blogId + "/temp/" + folder + "/";
        }
        String fileName = folder + UUID.randomUUID() + "-" + file.getOriginalFilename();

        try {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(fileName)
                    .contentType(file.getContentType())
                    .build();

            s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
            return s3Client.utilities().getUrl(builder -> builder.bucket(bucketName).key(fileName)).toExternalForm();
        } catch (S3Exception e) {
            throw new SystemException(
                    ErrorCode.S3_UPLOAD_ERROR,
                    "파일 업로드에 실패하였습니다.",
                    "S3ServiceImpl",
                    "tempUploadFile", e);
        }

    }

    @Override
    public void deleteProfileImage(String blogId) {
        String folder = blogId + "/final/profile/";

        // 폴더 내 모든 객체 삭제. ListObjects는 버킷 내 객체들을 나열한다.
        ListObjectsV2Request listRequest = ListObjectsV2Request.builder()
                .bucket(bucketName)
                .prefix(folder)
                .build();

        ListObjectsV2Response listResponse = s3Client.listObjectsV2(listRequest);
        listResponse.contents().forEach(obj -> {
            s3Client.deleteObject(DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(obj.key())
                    .build());
        });
    }

    @Override
    public String uploadProfileImage(MultipartFile file, String blogId) throws IOException {

        deleteProfileImage(blogId);

        String folder = blogId + "/final/profile/";

        String fileName = folder + UUID.randomUUID() + "-" + file.getOriginalFilename();

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(fileName)
                .contentType(file.getContentType())
                .build();

        s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
        return s3Client.utilities().getUrl(builder -> builder.bucket(bucketName).key(fileName)).toExternalForm();

    }

    // 실패 원인이 일시적이지 않기 때문에 재시도 불필요 하다고 판단
    @Async
    @Override
    public CompletableFuture<Void> processCreatePostS3TempOperation(PostRequest postRequest, String blogId) {

        try {
            // String userFolder = getUserFolder();

            // AWS S3 final 폴더에 최종 업로드 및 temp 폴더에 저장되어 있는 불필요한 이미지 및 파일 삭제 처리 로직
            List<String> tempfileUrls = postRequest.getFiles().stream().map(FileRequest::getFileUrl).toList();

            processTempFilesToFinal(tempfileUrls, blogId);

            if (postRequest.getFeaturedImage() != null) {
                moveTempFilesToFinal(postRequest.getFeaturedImage().getFileUrl(),
                        blogId + "/final/featured/", blogId);
            }

            for (String tempFileUrl : postRequest.getDeleteTempImageUrls()) {
                tempDeleteFile(tempFileUrl, blogId);
            }

            return CompletableFuture.completedFuture(null);
        } catch (Exception ex) {
            log.error("processCreatePostS3TempOperation 에러 발생", ex);
             // 언체크드 예외로 변경했기 때문에 throws를 던지지 안아도 됨. 비동기 설정에서 예외 처리 
            throw new S3OperationException("processCreatePostS3TempOperation 에러 발생", ex);

        }

    }

    @Override
    @Async
    public CompletableFuture<Void> processUpdatePostS3TempOperation(PostUpdateRequest postUpdateRequest,
            String blogId) {

        try {

            // String userFolder = getUserFolder();

            // AWS S3 final 폴더에 최종 업로드 및 temp 폴더에 저장되어 있는 불필요한 이미지 및 파일 삭제 처리 로직

            // 수정 요청을 했을때 수정 페이지에 기존에 존재하던(글 작성 시 발행된) final 경로 파일들은 제외하고 에디터에 새롭게 등록되는, 즉
            // 파일 경로에
            // temp가 포함되어있는 파일들만 가져옴
            List<String> tempfileUrls = postUpdateRequest.getFiles().stream()
                    .map(FileRequest::getFileUrl)
                    .filter(url -> !url.contains("final/"))
                    .toList();

            processTempFilesToFinal(tempfileUrls, blogId);

            if (postUpdateRequest.getFeaturedImage() != null) {
                moveTempFilesToFinal(postUpdateRequest.getFeaturedImage().getFileUrl(),
                        blogId + "/final/featured/", blogId);
            }

            for (String tempFileUrl : postUpdateRequest.getDeleteTempImageUrls()) {
                tempDeleteFile(tempFileUrl, blogId);
            }

            return CompletableFuture.completedFuture(null);

        } catch (Exception ex) {
            log.error("processUpdatePostS3TempOperation 에러 발생", ex);
            throw new S3OperationException("processUpdatePostS3TempOperation 에러 발생", ex);

        }

    }

    private void processTempFilesToFinal(List<String> tempfileUrls, String blogId) {

        for (String tempFileUrl : tempfileUrls) {
            String finalFolder;
            if (tempFileUrl.contains("/images/")) {
                finalFolder = blogId + "/final/images/";
            } else {
                finalFolder = blogId + "/final/files/";
            }
            moveTempFilesToFinal(tempFileUrl, finalFolder, blogId);
        }
    }

    private void moveTempFilesToFinal(String tempFileUrl, String finalFolder, String blogId) {

        String fileName = tempFileUrl.substring(tempFileUrl.lastIndexOf("/") + 1);

        fileName = URLDecoder.decode(fileName, StandardCharsets.UTF_8);
        String tempFolder;

        if (tempFileUrl.contains("/images/")) {
            tempFolder = blogId + "/temp/images/";
        } else if (tempFileUrl.contains("/files/")) {
            tempFolder = blogId + "/temp/files/";
        } else {
            tempFolder = blogId + "/temp/featured/";
        }

        String tempFullPath = tempFolder + fileName;
        String finalFullPath = finalFolder + fileName;

        CopyObjectRequest copyObjectRequest = CopyObjectRequest.builder()
                .sourceBucket(bucketName)
                .sourceKey(tempFullPath)
                .destinationBucket(bucketName)
                .destinationKey(finalFullPath)
                .build();

        s3Client.copyObject(copyObjectRequest);

        DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                .bucket(bucketName)
                .key(tempFullPath)
                .build();

        s3Client.deleteObject(deleteObjectRequest);

    }

    private void tempDeleteFile(String fileUrl, String blogId) {

        // String userFolder = getUserFolder();

        String fileName = fileUrl.substring(fileUrl.lastIndexOf("/") + 1);
        fileName = URLDecoder.decode(fileName, StandardCharsets.UTF_8);

        String folder;
        if (fileUrl.contains("temp/featured/")) {
            folder = blogId + "/temp/featured/";
        } else if (fileUrl.contains("temp/images/")) {
            folder = blogId + "/temp/images/";
        } else if (fileUrl.contains("temp/files/")) {
            folder = blogId + "/temp/files/";
        } else if (fileUrl.contains("final/featured/")) {
            folder = blogId + "/final/featured/";
        } else if (fileUrl.contains("final/images/")) {
            folder = blogId + "/final/images/";
        } else {
            folder = blogId + "/final/files/";
        }
        String fullPath = folder + fileName;

        DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                .bucket(bucketName)
                .key(fullPath)
                .build();
        s3Client.deleteObject(deleteObjectRequest);

    }

}
