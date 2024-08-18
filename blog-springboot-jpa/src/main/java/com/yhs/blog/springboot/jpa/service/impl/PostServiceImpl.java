package com.yhs.blog.springboot.jpa.service.impl;

import com.yhs.blog.springboot.jpa.dto.PostDTO;
import com.yhs.blog.springboot.jpa.entity.Category;
import com.yhs.blog.springboot.jpa.entity.Post;
import com.yhs.blog.springboot.jpa.entity.User;
import com.yhs.blog.springboot.jpa.repository.CategoryRepository;
import com.yhs.blog.springboot.jpa.repository.PostRepository;
import com.yhs.blog.springboot.jpa.repository.UserRepository;
import com.yhs.blog.springboot.jpa.service.PostService;
import com.yhs.blog.springboot.jpa.util.PostMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Log4j2
public class PostServiceImpl implements PostService {

    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final PostRepository postRepository;

    @Override
    public Post createPost(PostDTO postDTO) {



        try {
            User user = postDTO.getUserId() != null ? userRepository.findById(postDTO.getUserId()).orElse(null) : null;
            Category category = postDTO.getCategoryId() != null ? categoryRepository.findById(postDTO.getCategoryId()).orElse(null) : null;
            Post post = PostMapper.toEntity(user, category, postDTO.getTitle(), postDTO.getContent(), postDTO.getPostStatus());


            return postRepository.save(post);

//            throw new DataAccessException("Simulated database exception") {};

        } catch (DataAccessException ex) {
            throw new RuntimeException("A server error occurred while creating the post.", ex);
        }

    }
}
