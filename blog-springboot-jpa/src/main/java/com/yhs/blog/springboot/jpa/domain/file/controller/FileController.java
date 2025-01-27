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

import com.yhs.blog.springboot.jpa.common.response.ErrorResponse;
import com.yhs.blog.springboot.jpa.common.response.SuccessResponse;
import com.yhs.blog.springboot.jpa.domain.file.service.FileService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Tag(name = "파일 업로드, 외부 파일 정보 조회", description = "AWS S3에 파일 업로드 및 외부 이미지 정보를 얻어오기 위한 API")
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/{blogId}")
@Log4j2
public class FileController {

    private final FileService fileService;

    @Value("${aws.s3.bucketName}")
    private String bucketName;

    // Swagger api에서 파일을 선택하기 위해서 consumes = "multipart/form-data"를 지정해 주어야 함.
    @Operation(summary = "AWS S3에 파일 업로드 요청 처리", description = "AWS S3에 파일을 업로드하고 파일 URL 반환")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "파일 URL 응답 성공", content = @Content(schema = @Schema(implementation = SuccessResponse.class))),
            @ApiResponse(responseCode = "403", description = "권한 없음", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),

    })
    @Parameters({
            @Parameter(name = "blogId", description = "사용자 블로그 아이디", required = true),
            @Parameter(name = "featured", description = "대표 이미지 여부", required = false)
    })
    @PreAuthorize("#userBlogId == authentication.name")
    @PostMapping(value = "/temp/files/upload", consumes = "multipart/form-data", produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "featured", required = false) String featured,
            @P("userBlogId") @PathVariable("blogId") String blogId) {

        log.info("[FileController] uploadFile() 요청 - file: {}, featured: {}, blogId: {}", file, featured, blogId);

        try {

            if (Objects.requireNonNull(file.getContentType()).startsWith("image/")
                    && file.getSize() > 5 * 1024 * 1024) { // 5MB

                log.warn("[FileController] uploadFile() image fileSize 초과 분기 응답");

                // limit for image files
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("이미지 파일 크기는 5MB를 초과할 수 없습니다.");
            } else if (!file.getContentType().startsWith("image/") && file.getSize() > 10 * 1024 * 1024) {

                log.warn("[FileController] uploadFile() 일반 파일 fileSize 초과 분기 응답");

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
    @Operation(summary = "외부 이미지 정보를 얻어오기 위한 Proxy 요청 처리", description = "프론트측에서는 CORS 오류가 발생하기 때문에 백엔드측에서 외부 이미지 정보를 얻어서 프론트측에 응답")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "외부 이미지 정보 응답 성공", content = @Content(schema = @Schema(implementation = SuccessResponse.class))),
            @ApiResponse(responseCode = "403", description = "권한 없음", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),

    })
    @Parameters({
            @Parameter(name = "blogId", description = "사용자 블로그 아이디", required = true),
            @Parameter(name = "url", description = "이미지 정보를 얻어올 외부 이미지 url", required = true)
    })
    @PreAuthorize("#userBlogId == authentication.name")
    @GetMapping("/proxy-image")
    public ResponseEntity<byte[]> proxyImage(@P("userBlogId") @PathVariable("blogId") String blogId,
            @RequestParam(name = "url") String url) {

        log.info("[FileController] proxyImage() 요청 - URL: {}", url);

        try {
            return ResponseEntity.ok()
                    .contentType(fileService.getContentType(url))
                    .body(fileService.getProxyImage(url));
        } catch (Exception e) {
            log.error("이미지 프록시 처리 실패 - URL: {}, 에러: {}", url, e.getMessage(), e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
