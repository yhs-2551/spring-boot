package com.yhs.blog.springboot.jpa.domain.post.dto.response;

import java.util.List;

import com.querydsl.core.annotations.QueryProjection;

import lombok.Getter;

@Getter
public class PostIndexAndIndexSearchResponseWithPostTotal {
    private final List<PostIndexAndIndexSearchResponse> content;
    private final long total;

    @QueryProjection
    public PostIndexAndIndexSearchResponseWithPostTotal(List<PostIndexAndIndexSearchResponse> content, long total) {
        this.content = content;
        this.total = total;
    }

}
