package com.yhs.blog.springboot.jpa.domain.file.controller;

import java.util.Objects;

import org.springframework.beans.factory.annotation.Value;

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

import org.springframework.web.multipart.MultipartFile;

import com.yhs.blog.springboot.jpa.domain.file.service.FileService;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api")
@Log4j2
public class FileController {

    private final FileService fileService;

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
                        .body("이미지 파일 크기는 5MB를 초과할 수 없습니다.");
            } else if (!file.getContentType().startsWith("image/") && file.getSize() > 10 * 1024 * 1024) {
                // 10MB limit for other files
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("일반 파일 크기는 10MB를 초과할 수 없습니다.");
            }

            String fileUrl = fileService.uploadTempFile(file, featured, blogId);

            return ResponseEntity.ok(fileUrl);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("파일 업로드에 실패하였습니다.");
        }

    }

    // 글작성 및 수정시에 이미지 외부에서 복사하고, 붙여넣을때 프론트에서 처리하면 CORS 문제가 발생해서 여기서 프록시로 처리
    @PreAuthorize("#userBlogId == authentication.name")
    @GetMapping("/{blogId}/proxy-image")
    public ResponseEntity<byte[]> proxyImage(@P("userBlogId") @PathVariable("blogId") String blogId,
            @RequestParam(name = "url") String url) {
        try {
            return ResponseEntity.ok()
                    .contentType(fileService.getContentType(url))
                    .body(fileService.getProxyImage(url));
        } catch (Exception e) {
            log.error("이미지 프록시 처리 실패 - URL: {}, 에러: {}", url, e.getMessage(), e);
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

}
