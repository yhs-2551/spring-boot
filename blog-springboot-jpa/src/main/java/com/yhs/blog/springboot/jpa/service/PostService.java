package com.yhs.blog.springboot.jpa.service;

import com.yhs.blog.springboot.jpa.dto.PostDTO;
import com.yhs.blog.springboot.jpa.entity.Post;

public interface PostService {
    Post createPost(PostDTO postDTO);
}
