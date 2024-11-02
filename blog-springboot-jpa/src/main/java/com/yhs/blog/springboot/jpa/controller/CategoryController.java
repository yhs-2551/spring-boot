package com.yhs.blog.springboot.jpa.controller;


import com.yhs.blog.springboot.jpa.dto.*;
import com.yhs.blog.springboot.jpa.service.CategoryService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/categories")
public class CategoryController {

    private final CategoryService categoryService;

    @PostMapping
    public ResponseEntity<List<CategoryResponse>> createCategory(@RequestBody CategoryRequestPayLoad categoryRequestPayLoad) {
        List<CategoryResponse> categoryResponse = categoryService.createCategory(categoryRequestPayLoad);
        return new ResponseEntity<>(categoryResponse, HttpStatus.CREATED);
    }

    // 특정 사용자의 모든 카테고리 조회
    @GetMapping
    public ResponseEntity<List<CategoryResponse>> getAllCategoriesWithChildrenByUserId() {
        List<CategoryResponse> categories = categoryService.getAllCategoriesWithChildrenByUserId();
        return ResponseEntity.ok(categories);
    }


}
