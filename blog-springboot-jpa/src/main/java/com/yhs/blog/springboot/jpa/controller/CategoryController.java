package com.yhs.blog.springboot.jpa.controller;


import com.yhs.blog.springboot.jpa.config.jwt.TokenProvider;
import com.yhs.blog.springboot.jpa.dto.*;
import com.yhs.blog.springboot.jpa.service.CategoryService;
import com.yhs.blog.springboot.jpa.util.TokenUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@Log4j2
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/{userIdentifier}/categories")
public class CategoryController {

    private final CategoryService categoryService;
    private final TokenProvider tokenProvider;

    @PostMapping
    public ResponseEntity<ApiResponse> createCategory(@RequestBody CategoryRequestPayLoad categoryRequestPayLoad,
                                                      @PathVariable("userIdentifier") String userIdentifier,
                                                      HttpServletRequest request) {

        String userIdentifierFromAccessToken =
                TokenUtil.extractUserIdentifierFromRequestToken(request,
                        tokenProvider);

        if (!userIdentifier.equals(userIdentifierFromAccessToken)) {

            log.warn("Unauthorized access attempt by user: {}", userIdentifierFromAccessToken);

            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ErrorResponse("You are not authorized to create category.", 403));
        }

        List<CategoryResponse> categoryResponse =
                categoryService.createCategory(categoryRequestPayLoad, userIdentifier);

        log.info("categoryResponse >>>>>>> " + categoryResponse);


        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new SuccessResponse<List<CategoryResponse>>(categoryResponse, "Success " +
                        "create new category."));

    }

    // 특정 사용자의 모든 카테고리 조회
    @GetMapping
    public ResponseEntity<ApiResponse> getAllCategoriesWithChildrenByUserId(@PathVariable(
            "userIdentifier") String userIdentifier, HttpServletRequest request) {

        String userIdentifierFromAccessToken =
                TokenUtil.extractUserIdentifierFromRequestToken(request,
                        tokenProvider);

        if (!userIdentifier.equals(userIdentifierFromAccessToken)) {

            log.warn("Unauthorized access attempt by user: {}", userIdentifierFromAccessToken);

            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ErrorResponse("You are not authorized to get categories.", 403));
        }

        List<CategoryResponse> categories = categoryService.getAllCategoriesWithChildrenByUserId(userIdentifier);

        return ResponseEntity.status(HttpStatus.OK)
                .body(new SuccessResponse<List<CategoryResponse>>(categories, "Success " +
                        "get all categories."));
    }


}
