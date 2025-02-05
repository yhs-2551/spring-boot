package com.yhs.blog.springboot.jpa.domain.user.service;

import java.io.IOException;

import com.yhs.blog.springboot.jpa.domain.auth.token.provider.user.BlogUser;
import com.yhs.blog.springboot.jpa.domain.user.dto.request.UserSettingsRequest;
import com.yhs.blog.springboot.jpa.domain.user.dto.response.UserPrivateProfileResponse;
import com.yhs.blog.springboot.jpa.domain.user.dto.response.UserPublicProfileResponse;

public interface UserProfileService {

    UserPublicProfileResponse getUserPublicProfile(String blogId);

    UserPrivateProfileResponse getUserPrivateProfile(BlogUser blogUser);

    void updateUserSettings(String blogId, UserSettingsRequest userSettingsRequest) throws IOException;

}
