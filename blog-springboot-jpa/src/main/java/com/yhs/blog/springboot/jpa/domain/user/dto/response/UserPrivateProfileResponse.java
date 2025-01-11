package com.yhs.blog.springboot.jpa.domain.user.dto.response;

public record UserPrivateProfileResponse(String email, String blogId, String blogName, String username, String profileImageUrl) {

}
