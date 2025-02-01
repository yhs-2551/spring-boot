package com.yhs.blog.springboot.jpa.domain.user.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.yhs.blog.springboot.jpa.common.constant.code.ErrorCode;
import com.yhs.blog.springboot.jpa.domain.user.entity.User;
import com.yhs.blog.springboot.jpa.domain.user.factory.TestUserFactory;
import com.yhs.blog.springboot.jpa.domain.user.repository.UserRepository;
import com.yhs.blog.springboot.jpa.exception.custom.BusinessException;

@ExtendWith(MockitoExtension.class)
public class UserFindServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserFindServiceImpl userFindService;

    @Test
    @DisplayName("사용자 ID로 사용자 조회 성공")
    void id_값으로_사용자_조회시_성공() {
        // given
        Long userId = 1L;

        User user = TestUserFactory.createTestUserWithId();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // when
        User result = userFindService.findUserById(userId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(userId);
        verify(userRepository).findById(userId);
    }

    @Test
    @DisplayName("사용자 ID로 사용자 조회 실패")
    void id_값으로_사용자_조회시_실패() {
        // given
        Long userId = 1L;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // when & then
        BusinessException exception = assertThrows(BusinessException.class,
                () -> userFindService.findUserById(userId));

        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.USER_NOT_FOUND);
        verify(userRepository).findById(userId);
    }

    @Test
    @DisplayName("사용자 Email로 사용자 조회 성공")
    void 이메일_값으로_사용자_조회시_성공() {
        // given
        String email = "test@example.com";
        User user = TestUserFactory.createTestUserWithId();

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        // when
        Optional<User> result = userFindService.findUserByEmail(email);

        // then
        assertThat(result).isPresent();
        assertThat(result.get().getEmail()).isEqualTo(email);
        verify(userRepository).findByEmail(email);
    }

    @Test
    @DisplayName("사용자 Email로 사용자 조회 실패")
    void 이메일_값으로_사용자_조회시_실패() {
        // given
        String email = "nonexistent@email.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        // when
        Optional<User> result = userFindService.findUserByEmail(email);

        // then
        assertThat(result).isEmpty();
        verify(userRepository).findByEmail(email);
    }

    @Test
    @DisplayName("사용자 블로그ID로 사용자 조회 성공")
    void 블로그ID_값으로_사용자_조회시_성공() {
        // given
        String blogId = "testBlogId";
        User user = TestUserFactory.createTestUserWithId();

        when(userRepository.findByBlogId(blogId)).thenReturn(Optional.of(user));

        // when
        User result = userFindService.findUserByBlogId(blogId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getBlogId()).isEqualTo(blogId);
        verify(userRepository).findByBlogId(blogId);
    }

    @Test
    @DisplayName("사용자 블로그ID로 사용자 조회 실패")
    void 블로그ID_값으로_사용자_조회시_실패() {
        // given
        String blogId = "nonexistentBlog";
        when(userRepository.findByBlogId(blogId)).thenReturn(Optional.empty());

        // when & then
        BusinessException exception = assertThrows(BusinessException.class,
                () -> userFindService.findUserByBlogId(blogId));

        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.USER_NOT_FOUND);
        verify(userRepository).findByBlogId(blogId);
    }
}