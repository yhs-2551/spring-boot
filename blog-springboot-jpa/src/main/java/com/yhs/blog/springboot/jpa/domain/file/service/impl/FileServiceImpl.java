package com.yhs.blog.springboot.jpa.domain.file.service.impl;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Set;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

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

    private String getKeyFromUrl(String url) {

        log.info("[FileServiceImpl] getKeyFromUrl() 메서드 시작 - url: {}", url);

        String prefix = "amazonaws.com/";
        int startIndex = url.indexOf(prefix) + prefix.length();
        String key = url.substring(startIndex);

        return URLDecoder.decode(key, StandardCharsets.UTF_8); // 한글 파일명을 위한 디코딩. 예를들어 파일명이 스크린샷이면 스크린샷으로 디코딩해야함.
    }

    @Override
    public void saveFiles(Set<File> files) {
        fileRepository.saveAll(files);
    }

}
