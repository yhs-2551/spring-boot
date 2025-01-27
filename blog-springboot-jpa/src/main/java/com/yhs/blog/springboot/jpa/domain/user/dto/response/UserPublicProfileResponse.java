package com.yhs.blog.springboot.jpa.domain.user.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnore;

// 직렬화/역직렬화시 record에선 기본생성자 불필요(canonical 생성자를 통해 직렬화/역직렬화를 진행), 일반 클래스에서만 기본 생성자 필요 
public record UserPublicProfileResponse(@JsonIgnore Long id, String blogId, String blogName, String username,
        String profileImageUrl) {

}
