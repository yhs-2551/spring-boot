package com.yhs.blog.springboot.jpa.domain.file.controller;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.parameters.P;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import com.yhs.blog.springboot.jpa.domain.file.service.s3.S3Service;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.S3Object;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api")
@Log4j2
public class FileController {

    private final S3Service s3Service;
    private final RestTemplate restTemplate;
    private final S3Client s3Client;

    @Value("${aws.s3.bucketName}")
    private String bucketName;

    // Swagger api에서 파일을 선택하기 위해서 consumes = "multipart/form-data"를 지정해 주어야 함.
    @PreAuthorize("#userBlogId == authentication.name")
    @PostMapping(value = "/{blogId}/temp/files/upload", consumes = "multipart/form-data", produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "featured", required = false) String featured,
            @P("userBlogId") @PathVariable("blogId") String blogId,
            HttpServletRequest request) {

        try {

            if (Objects.requireNonNull(file.getContentType()).startsWith("image/")
                    && file.getSize() > 5 * 1024 * 1024) { // 5MB
                // limit for image files
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Image file size exceeds the limit of 5MB");
            } else if (!file.getContentType().startsWith("image/") && file.getSize() > 10 * 1024 * 1024) {
                // 10MB limit for other files
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("File size exceeds the limit of 10MB");
            }

            String fileUrl = s3Service.tempUploadFile(file, featured, blogId);

            return ResponseEntity.ok(fileUrl);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to upload file");
        }

    }

    // 글작성 및 수정시에 이미지 외부에서 복사하고, 붙여넣을때 프론트에서 처리하면 CORS 문제가 발생해서 여기서 프록시로 처리
    @PreAuthorize("#userBlogId == authentication.name")
    @GetMapping("/{blogId}/proxy-image")
    public ResponseEntity<byte[]> proxyImage(@P("userBlogId") @PathVariable("blogId") String blogId,
            @RequestParam(name = "url") String url) {
        try {

            if (url.contains("iceamericano-blog-storage.s3")) {
                // S3 클라이언트를 통한 접근
                GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                        .bucket(bucketName)
                        .key(getKeyFromUrl(url))
                        .build();

                log.debug("추출된 S3 key: {}", getKeyFromUrl(url));

                ResponseBytes<GetObjectResponse> objectBytes = s3Client.getObjectAsBytes(getObjectRequest);

                String contentType = objectBytes.response().contentType();

                log.info("S3 이미지 응답 - ContentType: {}", contentType);

                return ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(contentType))
                        .body(objectBytes.asByteArray());
            }

            // 외부 이미지 요청
            ResponseEntity<byte[]> response = restTemplate.getForEntity(url, byte[].class);

            log.info("외부 서버 응답 - 상태코드: {}, ContentType: {}, ContentLength: {}",
                    response.getStatusCode(),
                    response.getHeaders().getContentType(),
                    response.getHeaders().getContentLength());

            // 헤더 설정.외부 이미지 서버에서 받은 이미지의 타입(png, jpeg 등)을 그대로 유지
            HttpHeaders headers = new HttpHeaders();

            headers.setContentType(response.getHeaders().getContentType());

            return new ResponseEntity<>(
                    response.getBody(),
                    headers,
                    HttpStatus.OK);
        } catch (Exception e) {
            log.error("이미지 프록시 처리 실패 - URL: {}, 에러: {}", url, e.getMessage(), e);
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    private String getKeyFromUrl(String url) {
        // amazonaws.com/ 이후의 모든 문자열을 key로 사용
        String prefix = "amazonaws.com/";
        int startIndex = url.indexOf(prefix) + prefix.length();
        String key = url.substring(startIndex);

        key = URLDecoder.decode(key, StandardCharsets.UTF_8); // 한글 파일명을 위한 디코딩. 예를들어 파일명이 스크린샷이면 스크린샷으로 디코딩해야함.

        return key;
    }

}
