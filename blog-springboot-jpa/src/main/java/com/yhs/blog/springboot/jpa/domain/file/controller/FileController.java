package com.yhs.blog.springboot.jpa.domain.file.controller;

import java.util.Objects;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.parameters.P;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.yhs.blog.springboot.jpa.domain.file.service.s3.S3Service;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api")
@Log4j2
public class FileController {

    private final S3Service s3Service;

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
}
