package com.yhs.blog.springboot.jpa.domain.category.controller;

import com.yhs.blog.springboot.jpa.domain.category.dto.request.CategoryRequestPayLoad;
import com.yhs.blog.springboot.jpa.domain.category.dto.response.CategoryResponse;
import com.yhs.blog.springboot.jpa.aop.performance.MeasurePerformance;
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
@RequestMapping("/api/{blogId}/categories")
public class CategoryController {

        private final CategoryService categoryService;

        @MeasurePerformance
        @PostMapping
        @PreAuthorize("#userBlogId == authentication.name")
        public ResponseEntity<ApiResponse> createCategory(@RequestBody CategoryRequestPayLoad categoryRequestPayLoad,
                        @P("userBlogId") @PathVariable("blogId") String blogId) {

                categoryService.createCategory(categoryRequestPayLoad, blogId);

                return ResponseEntity.status(HttpStatus.CREATED)
                                .body(new SuccessResponse<>(
                                                "카테고리 생성에 성공하였습니다."));
        }

        // 특정 사용자의 모든 카테고리 조회. 모든 사용자가 볼 수 있어야 해서 preauthorize 제거
        @GetMapping
        public ResponseEntity<ApiResponse> getAllCategoriesWithChildrenByUserId(@PathVariable("blogId") String blogId) {

                List<CategoryResponse> categories = categoryService.getAllCategoriesWithChildrenByUserId(blogId);

                return ResponseEntity.status(HttpStatus.OK)
                                .body(new SuccessResponse<List<CategoryResponse>>(categories, "카테고리 조회에 성공하였습니다."));
        }

}
