package com.yhs.blog.springboot.jpa.domain.user.factory;

import com.yhs.blog.springboot.jpa.domain.user.entity.User;
import org.springframework.boot.test.context.TestComponent;

import java.util.ArrayList;
import java.util.List;

@TestComponent
public class TestUserFactory {

    // 테스트용 기본 사용자 생성
    public static User createTestUser() {
        return User.builder()
                .email("test@example.com")
                .username("testUser")
                .userIdentifier("test")
                .password("123")
                .build();
    }

    // 커스텀 사용자 생성
    public static User createTestUser(String email, String username, String userIdentifier) {
        return User.builder()
                .email(email)
                .username(username)
                .userIdentifier(userIdentifier)
                .password("123")
                .build();
    }

    // 여러 테스트 사용자 생성
    public static List<User> createMultipleTestUsers(int count) {
        List<User> users = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            users.add(User.builder()
                    .email("test" + i + "@example.com")
                    .username("testUser" + i)
                    .userIdentifier("test" + i)
                    .password("123")
                    .build());
        }
        return users;
    }

}