package com.yhs.blog.springboot.jpa.dto.response;

import java.util.List;

import org.springframework.data.domain.Page;

import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class PageResponse<T> {
    private final int currentPage;
    private final int totalPages;
    private final long totalElements;
    private final boolean first;
    private final boolean last;
    private final boolean hasNext; 
    private final int size;
    private final List<T> content;

    public PageResponse(Page<T> page) {
        this.currentPage = page.getNumber() + 1; // 0-based to 1-based
        this.totalPages = page.getTotalPages();
        this.totalElements = page.getTotalElements();
        this.first = page.isFirst();
        this.last = page.isLast();
        this.hasNext = page.hasNext();
        this.size = page.getSize();
        this.content = page.getContent();
    }
}
