package com.yhs.blog.springboot.jpa.domain.post.dto.response;

import java.util.List;

import com.querydsl.core.annotations.QueryProjection;

import lombok.Getter;

@Getter
public class PostUserPageResponseWithPostTotal {
    private final List<PostUserPageResponse> content;
    private final long total;

    @QueryProjection
    public PostUserPageResponseWithPostTotal(List<PostUserPageResponse> content, long total) {
        this.content = content;
        this.total = total;
    }

}
