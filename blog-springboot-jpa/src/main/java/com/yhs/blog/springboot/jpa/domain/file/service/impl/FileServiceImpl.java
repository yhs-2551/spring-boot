package com.yhs.blog.springboot.jpa.domain.file.service.impl;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import com.yhs.blog.springboot.jpa.domain.featured_image.service.FeaturedImageService;
import com.yhs.blog.springboot.jpa.domain.file.dto.request.FilePropertiesForUpdateRequest;
import com.yhs.blog.springboot.jpa.domain.file.dto.request.FileRequest;
import com.yhs.blog.springboot.jpa.domain.file.entity.File;
import com.yhs.blog.springboot.jpa.domain.file.repository.FileRepository;
import com.yhs.blog.springboot.jpa.domain.file.service.FileService;
import com.yhs.blog.springboot.jpa.domain.file.service.infrastructure.s3.S3Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;

@Service
@RequiredArgsConstructor
@Log4j2
public class FileServiceImpl implements FileService {
    private final S3Service s3Service;
    private final S3Client s3Client;
    private final RestTemplate restTemplate;
    private final FileRepository fileRepository;
    private final FeaturedImageService featuredImageService;

    @Value("${aws.s3.bucketName}")
    private String bucketName;

    @Override
    public String uploadTempFile(MultipartFile file, String folder, String blogId) throws IOException {

        log.info("[FileServiceImpl] uploadTempFile() 메서드 시작 - file: {}, folder: {}, blogId: {}", file, folder, blogId);

        return s3Service.tempUploadFile(file, folder, blogId);
    }

    @Override
    public byte[] getProxyImage(String url) {

        log.info("[FileServiceImpl] getProxyImage() 메서드 시작 - url: {}", url);

        if (url.contains("iceamericano-blog-storage.s3")) {

            log.info("[FileServiceImpl] getProxyImage() - url에 iceamericano-blog-storage.s3 포함 분기 시작");

            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(getKeyFromUrl(url))
                    .build();

            ResponseBytes<GetObjectResponse> objectBytes = s3Client.getObjectAsBytes(getObjectRequest);

            return objectBytes.asByteArray();
        }

        // 외부 이미지 요청
        ResponseEntity<byte[]> response = restTemplate.getForEntity(url, byte[].class);

        return response.getBody();
    }

    @Override
    public MediaType getContentType(String url) {

        log.info("[FileServiceImpl] getContentType() 메서드 시작 - url: {}", url);

        if (url.contains("iceamericano-blog-storage.s3")) {

            log.info("[FileServiceImpl] getContentType() - url에 iceamericano-blog-storage.s3 포함 분기 시작");

            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(getKeyFromUrl(url))
                    .build();

            ResponseBytes<GetObjectResponse> objectBytes = s3Client.getObjectAsBytes(getObjectRequest);

            return MediaType.parseMediaType(objectBytes.response().contentType());
        }

        ResponseEntity<byte[]> response = restTemplate.getForEntity(url, byte[].class);

        return response.getHeaders().getContentType(); // 헤더 설정.외부 이미지 서버에서 받은 이미지의 타입(png, jpeg 등)을 그대로 유지
    }

    @Override
    public void processCreateFilesForCreatePostRequest(List<FileRequest> fileRequests, Long postId) {

        log.info("[PostOperationServiceImpl] processCreateFilesForCreatePostRequest 메서드 시작");

        if (fileRequests != null && !fileRequests.isEmpty()) {

            log.info("[PostOperationServiceImpl] processCreateFilesForCreatePostRequest !fileRequests.isEmpty() 분기 시작");

            Set<File> files = new HashSet<>();

            for (FileRequest fileRequest : fileRequests) {

                // 최종적으로 post를 저장시에 aws 파일 저장 위치를 temp -> final로 변경하기 때문에 final로 변경하는 로직 추가.
                // 따라서 db에 final 경로로 저장한다.
                String updatedFileUrl = fileRequest.getFileUrl().replace("/temp/", "/final/");

                Integer width;
                Integer height;

                if (fileRequest.getFileType().startsWith("image/") && fileRequest.getWidth() != null) {
                    width = fileRequest.getWidth();
                } else {
                    width = null;
                }

                if (fileRequest.getFileType().startsWith("image/") && fileRequest.getHeight() != null) {
                    height = fileRequest.getHeight();
                } else {
                    height = null;
                }

                File file = File.builder().fileName(fileRequest.getFileName()).fileType(fileRequest.getFileType())
                        .fileUrl(updatedFileUrl).fileSize(fileRequest.getFileSize()).width(width).height(height)
                        .postId(postId).build();

                files.add(file);
            }

            fileRepository.saveAll(files);
        }
    }

    @Override
    public void processUpdateFilesForUpdatePostRequest(List<FileRequest> fileRequests, Long postId,
            List<String> deleteFileUrls) {

        log.info("[FileServiceImpl] processUpdateFilesForUpdateRequest 메서드 시작");

        if (deleteFileUrls != null
                && !deleteFileUrls.isEmpty()) {

            log.info("[FileServiceImpl] processUpdateFilesForUpdateRequest - 삭제할 파일(이미지)이 있을때 분기 시작");

            processDeletedImages(deleteFileUrls);
        }

        if (fileRequests != null && !fileRequests.isEmpty()) {

            log.info("[FileServiceImpl] processUpdateFilesForUpdateRequest 저장 할 파일이 있을 때 분기 시작");

            Map<String, FilePropertiesForUpdateRequest> existingFileProperties = fileRepository
                    .findFilePropertiesByPostId(postId).stream()
                    .collect(Collectors.toMap(
                            FilePropertiesForUpdateRequest::fileUrl,
                            FilePropertiesForUpdateRequest -> FilePropertiesForUpdateRequest));

            Set<File> newFiles = new HashSet<>();

            for (FileRequest fileRequest : fileRequests) {

                FilePropertiesForUpdateRequest existingFile = existingFileProperties.get(fileRequest.getFileUrl());

                if (existingFile != null) {

                    if (fileRequest.getFileType().startsWith("image/") &&
                            (isWidthChanged(fileRequest, existingFile) ||
                                    isHeightChanged(fileRequest, existingFile))) {

                        log.info("[FileServiceImpl] 저장할 파일이 DB에 이미 존재하면서, 새롭게 이미지 사이즈 변경해야 할 때 분기 진행");

                        // width, height 업데이트할 파일 배치 작업으로 일괄 처리하려면 복잡해서 일단 하나씩 처리. 이렇게만해도 조회 -> 업데이트에서 조회 쿼리 생략 가능 
                        fileRepository.updateImageProperties(
                                existingFile.fileUrl(), // Record의 fileUrl() 메서드 사용
                                fileRequest.getWidth(),
                                fileRequest.getHeight());
                    }

                    log.info("[FileServiceImpl] 저장할 파일이 DB에 이미 존재하면서 이미지 사이즈 변경이 필요 없을때 continue 진행");

                    continue;
                }

                // 최종적으로 post를 저장시에 aws 파일 저장 위치를 temp -> final로 변경하기 때문에 final로 변경하는 로직 추가.
                // 따라서 db에 final 경로로 저장한다.
                String updatedFileUrl = fileRequest.getFileUrl().replace("/temp/", "/final/");

                Integer width;
                Integer height;

                log.info("fileRequest.getwdith()>>>", fileRequest.getWidth());
                log.info("fileRequest.getHeight()>>>", fileRequest.getHeight());

                if (fileRequest.getFileType().startsWith("image/") && fileRequest.getWidth() != null) {
                    width = fileRequest.getWidth();
                } else {
                    width = null;
                }

                if (fileRequest.getFileType().startsWith("image/") && fileRequest.getHeight() != null) {
                    height = fileRequest.getHeight();
                } else {
                    height = null;
                }

                File file = File.builder().fileName(fileRequest.getFileName()).fileType(fileRequest.getFileType())
                        .fileUrl(updatedFileUrl).fileSize(fileRequest.getFileSize()).width(width).height(height)
                        .postId(postId).build();

                newFiles.add(file);
            }

            fileRepository.saveAll(newFiles);
        }
    }

    // 리턴값이 필요해서 조회 꼭 필요, 만약 리턴값이 필요 없었다면 바로 벌크로 삭제
    @Override
    public List<String> processDeleteFilesForDeletePostRequest(Long postId) {
        log.info("[FileServiceImpl] processDeleteFilesForDeletePostRequest() 메서드 시작");

        List<File> files = fileRepository.findAllByPostId(postId);

        if (files == null || files.isEmpty()) {
            return null;
        }

        List<String> fileUrls = files.stream().map(File::getFileUrl).collect(Collectors.toList());

        fileRepository.deleteAll(files);

        return fileUrls;
    }

    private String getKeyFromUrl(String url) {

        log.info("[FileServiceImpl] getKeyFromUrl() 메서드 시작 - url: {}", url);

        String prefix = "amazonaws.com/";
        int startIndex = url.indexOf(prefix) + prefix.length();
        String key = url.substring(startIndex);

        return URLDecoder.decode(key, StandardCharsets.UTF_8); // 한글 파일명을 위한 디코딩. 예를들어 파일명이 스크린샷이면 스크린샷으로 디코딩해야함.
    }

    private void processDeletedImages(List<String> deletedUrls) {
        log.info("[FileServiceImpl] processDeletedImages 메서드 시작");

        List<String> toBeDeletedFileUrl = new ArrayList<>();

        // temp는 무시하고 final만 db에서 삭제될 수 있도록 처리
        for (String url : deletedUrls) {
            if (url.contains("/final/featured/")) {

                log.info("url>>>>>>>>>>>>>>>> {}", url);

                log.info("[FileServiceImpl] processDeletedImages - 대표 이미지 삭제 분기 진행");

                // 대표 이미지 처리
                // JPQL로 직접 작성해야함. find류, exists류, count류만 필드명에 매핑시켜서 자동 생성 - find류도 기본 제공 외
                // 메서드명은 repository에 정의되어 있어야함
                featuredImageService.processDeleteFeaturedImageForUpdatePostRequest(url);
            } else if (url.contains("/final/images/") || url.contains("/final/files/")) {
                // 이미지 및 파일 처리
                log.info("[FileServiceImpl] processDeletedImages - 이미지 및 파일 삭제 분기 진행");
                toBeDeletedFileUrl.add(url);
            }

        }

        log.info("여기 추가 진행");

        if (toBeDeletedFileUrl.isEmpty() || toBeDeletedFileUrl == null) {
            return;
        }

        fileRepository.deleteByFileUrlInBatch(toBeDeletedFileUrl);
    }

    private boolean isWidthChanged(FileRequest request, FilePropertiesForUpdateRequest existing) {
        return request.getWidth() != null && !request.getWidth().equals(existing.width());
    }

    private boolean isHeightChanged(FileRequest request, FilePropertiesForUpdateRequest existing) {
        return request.getHeight() != null && !request.getHeight().equals(existing.height());
    }

}
