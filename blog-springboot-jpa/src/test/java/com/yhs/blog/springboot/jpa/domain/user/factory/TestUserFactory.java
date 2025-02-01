package com.yhs.blog.springboot.jpa.domain.user.factory;

import com.yhs.blog.springboot.jpa.domain.user.entity.User;
import org.springframework.boot.test.context.TestComponent;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.List;

@TestComponent
public class TestUserFactory {

    // 테스트용 기본 사용자 생성
    public static User createTestUser() {
        return User.builder()
                .blogId("testBlogId")
                .username("testUser")
                .email("test@example.com")
                .password("Password123*")
                .build();
    }

    // 테스트용 기본 사용자 생성
    public static User createTestUserWithId() {
        User user = User.builder()
                .blogId("testBlogId")
                .username("testUser")
                .email("test@example.com")
                .password("Password123*")
                .build();

        ReflectionTestUtils.setField(user, "id", 1L);
        return user;
    }

    // 커스텀 사용자 생성
    public static User createTestUser(String email, String username, String blogId) {
        return User.builder()
                .blogId(blogId)
                .email(email)
                .username(username)
                .password("Password123*")
                .build();
    }

    // 여러 테스트 사용자 생성
    public static List<User> createMultipleTestUsers(int count) {
        List<User> users = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            users.add(User.builder()
                    .blogId("testBlogId" + i)
                    .email("test" + i + "@example.com")
                    .username("testUser" + i)
                    .password("Password123*")
                    .build());
        }
        return users;
    }

}
