package com.yhs.blog.springboot.jpa.domain.user.service.impl;
 
import com.yhs.blog.springboot.jpa.domain.user.entity.User; 
import com.yhs.blog.springboot.jpa.domain.user.repository.UserRepository;
import com.yhs.blog.springboot.jpa.domain.user.dto.request.SignUpUserRequest;
import org.junit.jupiter.api.DisplayName; 
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
 

import static org.assertj.core.api.Assertions.*; 
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserRegistrationServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    User userMock;

    @Mock
    private RedisTemplate<String, Boolean> redisTemplate;

    @Mock
    private ValueOperations<String, Boolean> valueOperations;

    @Mock
    BCryptPasswordEncoder bCryptPasswordEncoder;

    @InjectMocks
    private UserRegistrationServiceImpl userRegistrationServiceImpl;

    @Test
    @DisplayName("회원가입시 사용자를 생성")
    void createUserSuccess() {
        // given
        SignUpUserRequest request = new SignUpUserRequest(
                "testBlogId",
                "testUser",
                "test@example.com",
                "Password123*",
                "Password123*");

        // when
        userRegistrationServiceImpl.createUser(request);

        // then
        verify(bCryptPasswordEncoder).encode(request.getPassword());

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture()); // save메서드의 인자로 넘어가는 객체를 캡쳐함
        User capturedUser = userCaptor.getValue();
        assertThat(capturedUser.getBlogId()).isEqualTo(request.getBlogId());
        assertThat(capturedUser.getUsername()).isEqualTo(request.getUsername());
        assertThat(capturedUser.getEmail()).isEqualTo(request.getEmail());

    }

}



