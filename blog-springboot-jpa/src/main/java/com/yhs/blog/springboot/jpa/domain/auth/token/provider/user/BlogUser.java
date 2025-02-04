package com.yhs.blog.springboot.jpa.domain.auth.token.provider.user;

import java.util.Collection;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import lombok.Getter;

@Getter
public class BlogUser extends User {

    private final String BlogIdFromToken;
    private final String usernameFromToken;
    private final Long userIdFromToken;

    // super(blogId, null, authorities);로 부모를 호출하고, userId 필드를 추가하여 확장
    public BlogUser(String BlogIdFromToken, String usernameFromToken, Long userIdFromToken, Collection<? extends GrantedAuthority> authorities) {
        super(BlogIdFromToken, null, authorities); // 컨트롤러에서 authentication.name의 값이 super(BlogIdFromToken)값이 반영.
        this.BlogIdFromToken = BlogIdFromToken;
        this.usernameFromToken = usernameFromToken;
        this.userIdFromToken = userIdFromToken;
    }

}
