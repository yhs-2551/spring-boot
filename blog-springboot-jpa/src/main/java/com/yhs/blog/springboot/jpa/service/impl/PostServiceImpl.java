package com.yhs.blog.springboot.jpa.service.impl;

import com.yhs.blog.springboot.jpa.config.jwt.TokenProvider;
import com.yhs.blog.springboot.jpa.dto.PostRequest;
import com.yhs.blog.springboot.jpa.dto.PostResponse;
import com.yhs.blog.springboot.jpa.dto.PostUpdateRequest;
import com.yhs.blog.springboot.jpa.entity.Category;
import com.yhs.blog.springboot.jpa.entity.Post;
import com.yhs.blog.springboot.jpa.entity.User;
import com.yhs.blog.springboot.jpa.repository.CategoryRepository;
import com.yhs.blog.springboot.jpa.repository.PostRepository;
import com.yhs.blog.springboot.jpa.repository.UserRepository;
import com.yhs.blog.springboot.jpa.service.PostService;
import com.yhs.blog.springboot.jpa.service.UserService;
import com.yhs.blog.springboot.jpa.util.PostMapper;
import com.yhs.blog.springboot.jpa.util.TokenUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
@Log4j2
public class PostServiceImpl implements PostService {

    private final UserRepository userRepository;
    private final UserService userService;
    private final CategoryRepository categoryRepository;
    private final PostRepository postRepository;
    private final TokenProvider tokenProvider;


    @Transactional
    @Override
    public PostResponse createPost(PostRequest postRequest, HttpServletRequest request) {

        try {

//            User user = extractUserFromPrincipal(principal);

            Long userId = TokenUtil.extractUserIdFromRequestToken(request, tokenProvider);
            User user = userService.findUserById(userId);

            Category category = postRequest.getCategoryId() != null ? categoryRepository.findById(postRequest.getCategoryId()).orElse(null) : null;

            // 포스트 생성 시 user를 넘겨주면 외래키 연관관계 설정으로 인해 posts테이블에 user_id 값이 자동으로 들어간다.
            Post post = PostMapper.toEntity(user, category, postRequest.getTitle(), postRequest.getContent(), postRequest.getPostStatus());
            postRepository.save(post);

            return new PostResponse(post);

//            throw new DataAccessException("Simulated database exception") {};

        } catch (DataAccessException ex) {
            throw new RuntimeException("A server error occurred while creating the post.", ex);
        }

    }

    @Override
    @Transactional(readOnly = true)
    public List<PostResponse> getPostList() {
        List<Post> posts = postRepository.findAll();
        return posts.stream().map(post -> new PostResponse(post)).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public PostResponse getPost(Long id) {
        Post post = postRepository.findById(id).orElseThrow(() -> new RuntimeException("Post not found with id  " + id));
        return new PostResponse(post);
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

//    private User extractUserFromPrincipal(Principal principal) {
//        if (principal instanceof UsernamePasswordAuthenticationToken) {
//            //getName에서 리턴하는 값은 USerDetails에서 loadByuserName메서드에서 이메일 값으로 찾아왔기 때문에 email값을 가져오게 된다.
//            String userEmail =  ((UsernamePasswordAuthenticationToken) principal).getName();
//            return userRepository.findByEmail(userEmail).orElseThrow(() -> new UsernameNotFoundException("User not found with userEmail" + userEmail));
//        } else if (principal instanceof OAuth2AuthenticationToken) {
//            OAuth2User oAuth2User = ((OAuth2AuthenticationToken) principal).getPrincipal();
//            String userEmail = (String) oAuth2User.getAttributes().get("email");
//            return userRepository.findByEmail(userEmail).orElseThrow(() -> new UsernameNotFoundException("User not found with userEmail" + userEmail));
//        } else {
//            throw new IllegalArgumentException("Unsupported authentication type");
//        }
//    }



}
