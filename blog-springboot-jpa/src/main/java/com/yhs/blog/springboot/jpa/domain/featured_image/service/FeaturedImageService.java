package com.yhs.blog.springboot.jpa.domain.featured_image.service;

import com.yhs.blog.springboot.jpa.domain.post.dto.request.FeaturedImageRequest;

public interface FeaturedImageService {

    Long processFeaturedImageForCreatePostRequest(FeaturedImageRequest featuredImageRequest);

    Long processFeaturedImageForUpdatePostRequest(FeaturedImageRequest featuredImageRequest);

    void processDeleteFeaturedImageForUpdatePostRequest(String featuredImageFileUrl);

    void processDeleteFeaturedImageForDeletePostRequest(Long postId);

}
