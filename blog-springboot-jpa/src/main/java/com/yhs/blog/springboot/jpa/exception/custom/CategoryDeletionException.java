package com.yhs.blog.springboot.jpa.exception.custom;

public class CategoryDeletionException extends RuntimeException {
    public CategoryDeletionException(String message) {
        super(message);
    }
}