package com.yhs.blog.springboot.jpa.domain.user.dto.response;

// 직렬화/역직렬화시 record에선 기본생성자 불필요(canonical정식 생성자를 통해 직렬화/역직렬화를 진행), 일반 클래스에서만 기본 생성자 필요 
// 정식 생성자: 클래스의 모든 final 필드를 매개변수로 받아 초기화하는 생성자. 

public record LoginResultToken(String refreshToken, String accessToken) {
}
