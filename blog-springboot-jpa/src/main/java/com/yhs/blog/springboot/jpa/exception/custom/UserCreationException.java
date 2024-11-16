package com.yhs.blog.springboot.jpa.exception.custom;

public class UserCreationException  extends RuntimeException {
    public UserCreationException(String message) {
        super(message);
    }
}
