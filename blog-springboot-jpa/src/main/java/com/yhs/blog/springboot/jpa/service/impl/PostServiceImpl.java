package com.yhs.blog.springboot.jpa.service.impl;

import com.yhs.blog.springboot.jpa.dto.PostRequest;
import com.yhs.blog.springboot.jpa.dto.PostUpdateRequest;
import com.yhs.blog.springboot.jpa.dto.PostUpdateResponse;
import com.yhs.blog.springboot.jpa.entity.Category;
import com.yhs.blog.springboot.jpa.entity.Post;
import com.yhs.blog.springboot.jpa.entity.User;
import com.yhs.blog.springboot.jpa.exception.ResourceNotFoundException;
import com.yhs.blog.springboot.jpa.repository.CategoryRepository;
import com.yhs.blog.springboot.jpa.repository.PostRepository;
import com.yhs.blog.springboot.jpa.repository.UserRepository;
import com.yhs.blog.springboot.jpa.service.PostService;
import com.yhs.blog.springboot.jpa.util.PostMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.dao.DataAccessException;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Log4j2
public class PostServiceImpl implements PostService {

    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final PostRepository postRepository;

    @Override
    public Post createPost(PostRequest postRequest) {

        try {
            User user = postRequest.getUserId() != null ? userRepository.findById(postRequest.getUserId()).orElse(null) : null;
            Category category = postRequest.getCategoryId() != null ? categoryRepository.findById(postRequest.getCategoryId()).orElse(null) : null;
            Post post = PostMapper.toEntity(user, category, postRequest.getTitle(), postRequest.getContent(), postRequest.getPostStatus());


            return postRepository.save(post);

//            throw new DataAccessException("Simulated database exception") {};

        } catch (DataAccessException ex) {
            throw new RuntimeException("A server error occurred while creating the post.", ex);
        }

    }

    @Override
    public List<Post> getList() {
        return postRepository.findAll();
    }

    @Override
    public Post getPost(Long id) {
        return postRepository.findById(id).orElseThrow(() -> new RuntimeException("Post not found with id  " + id));
    }

    @Override
    public void deletePost(Long id) {
        postRepository.deleteById(id);
    }

    @Transactional
    @Override
    public Post updatePost(Long id, PostUpdateRequest postUpdateRequest) {

        Post post = postRepository.findById(id).orElseThrow(() -> new RuntimeException("Post not found with id  " + id));
        Category category = postUpdateRequest.getCategoryId() != null ? categoryRepository.findById(postUpdateRequest.getCategoryId()).orElse(null) : null;

        post.update(postUpdateRequest.getTitle(), postUpdateRequest.getContent(), Post.PostStatus.valueOf(postUpdateRequest.getPostStatus().toUpperCase()), category);
        return postRepository.save(post);
    }

}
