package com.yhs.blog.springboot.jpa.domain.user.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class DuplicateCheckResponse {
    private boolean isExist;
    private String message;
    private boolean isLimited;
}
