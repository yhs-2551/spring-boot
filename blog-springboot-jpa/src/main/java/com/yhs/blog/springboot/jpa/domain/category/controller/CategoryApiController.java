package com.yhs.blog.springboot.jpa.domain.category.controller;


import com.yhs.blog.springboot.jpa.security.jwt.provider.TokenProvider;
import com.yhs.blog.springboot.jpa.domain.category.dto.request.CategoryRequestPayLoad;
import com.yhs.blog.springboot.jpa.domain.category.dto.response.CategoryResponse;
import com.yhs.blog.springboot.jpa.common.response.ApiResponse;
import com.yhs.blog.springboot.jpa.common.response.SuccessResponse;
import com.yhs.blog.springboot.jpa.domain.category.service.CategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.parameters.P;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@Log4j2
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/{userIdentifier}/categories")
public class CategoryApiController {

    private final CategoryService categoryService;
    private final TokenProvider tokenProvider;

    @PostMapping
    @PreAuthorize("#userBlogId == authentication.name")
    public ResponseEntity<ApiResponse> createCategory(@RequestBody CategoryRequestPayLoad categoryRequestPayLoad,
                                                      @P("userBlogId") @PathVariable("userIdentifier") String userIdentifier) {

        List<CategoryResponse> categoryResponse =
                categoryService.createCategory(categoryRequestPayLoad);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new SuccessResponse<List<CategoryResponse>>(categoryResponse, "Success " +
                        "create new category."));
    }

    // 특정 사용자의 모든 카테고리 조회
    @GetMapping
    @PreAuthorize("#userBlogId == authentication.name")
    public ResponseEntity<ApiResponse> getAllCategoriesWithChildrenByUserId(@P("userBlogId") @PathVariable("userIdentifier") String userIdentifier) {

        List<CategoryResponse> categories = categoryService.getAllCategoriesWithChildrenByUserId();

        return ResponseEntity.status(HttpStatus.OK)
                .body(new SuccessResponse<List<CategoryResponse>>(categories, "Success " +
                        "get all categories."));
    }


}
