package com.yhs.blog.springboot.jpa.domain.post.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.yhs.blog.springboot.jpa.domain.auth.token.provider.user.BlogUser;
import com.yhs.blog.springboot.jpa.domain.post.dto.response.PostAdminAndUserBaseResponse;
import com.yhs.blog.springboot.jpa.domain.post.dto.response.PostIndexAndIndexSearchResponse;
import com.yhs.blog.springboot.jpa.domain.post.dto.response.PostResponseForDetailPage;
import com.yhs.blog.springboot.jpa.domain.post.dto.response.PostResponseForEditPage;
import com.yhs.blog.springboot.jpa.domain.post.repository.search.SearchType;

public interface PostFindService {

    Page<? extends PostAdminAndUserBaseResponse> getAllPostsSpecificUser(String blogId, String keyword, SearchType searchType,
            String categoryName, Pageable pageable, BlogUser blogUser);

    Page<PostIndexAndIndexSearchResponse> getAllPostsAllUser(String keyword, SearchType searchType, Pageable pageable,
            BlogUser blogUser);

    PostResponseForDetailPage getPostByPostIdForDetailPage(Long postId);

    PostResponseForEditPage getPostByPostIdForEditPage(Long postId);

}
