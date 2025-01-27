package com.yhs.blog.springboot.jpa.domain.user.service.impl;

import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import com.yhs.blog.springboot.jpa.aop.log.Loggable;
import com.yhs.blog.springboot.jpa.common.constant.cache.CacheConstants;
import com.yhs.blog.springboot.jpa.common.constant.code.ErrorCode;
import com.yhs.blog.springboot.jpa.domain.file.service.infrastructure.s3.S3Service;
import com.yhs.blog.springboot.jpa.domain.user.dto.request.UserSettingsRequest;
import com.yhs.blog.springboot.jpa.domain.user.dto.response.UserPrivateProfileResponse;
import com.yhs.blog.springboot.jpa.domain.user.dto.response.UserPublicProfileResponse;
import com.yhs.blog.springboot.jpa.domain.user.entity.User;
import com.yhs.blog.springboot.jpa.domain.user.repository.UserRepository;
import com.yhs.blog.springboot.jpa.domain.user.service.UserProfileService;
import com.yhs.blog.springboot.jpa.exception.custom.BusinessException;
import com.yhs.blog.springboot.jpa.exception.custom.SystemException;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import software.amazon.awssdk.services.s3.model.S3Exception;

@Log4j2
@RequiredArgsConstructor
@Service
public class UserProfileServiceImpl implements UserProfileService {

    private final UserRepository userRepository;
    private final S3Service s3Service;

    private final RedisTemplate<String, UserPublicProfileResponse> userPublicProfileRedisTemplate; 
    private final RedisTemplate<String, Boolean> redisTemplateBoolean;


    // getUserPublicProfile 은 캐시에서 가져오기 때문에 User 엔티티 자체가 필요한 경우 아니면 여러곳에서 사용 가능
    @Loggable
    @Override
    @Transactional(readOnly = true)
    public UserPublicProfileResponse getUserPublicProfile(String blogId) {

        log.info("[UserProfileServiceImpl] getUserPublicProfile 메서드 시작");

        String cacheKey = "userPublicProfile:" + blogId;

        //UserPublicProfileResponse에서 id값은 JsonIgnore 했기 때문에 id값은 null값으로 저장됨. 
        UserPublicProfileResponse cachedUser = userPublicProfileRedisTemplate.opsForValue().get(cacheKey);
        if (cachedUser != null) {
            log.info("[UserProfileServiceImpl] getUserPublicProfile 메서드 Redis 캐시에 존재하는 경우 분기 시작");

            return cachedUser;
        }

        log.info("[UserProfileServiceImpl] getUserPublicProfile 메서드 Redis 캐시에 존재하지 않는 경우 분기 시작");

        Optional<User> optionalUser = userRepository.findByBlogId(blogId);
        if (optionalUser.isEmpty()) {
            throw new BusinessException(
                    ErrorCode.USER_NOT_FOUND,
                    blogId + "를 가지고 있는 사용자를 찾지 못하였습니다.",
                    "UserProfileServiceImpl",
                    "getUserPublicProfile");
        }

        User user = optionalUser.get();

        UserPublicProfileResponse userPublicProfileResponseDTO = new UserPublicProfileResponse(
                user.getBlogId(),
                user.getBlogName(),
                user.getUsername(), user.getProfileImageUrl());

        // blogId는 사용자가 프론트측에서 계정 정보를 변경하지 않는 한 유지된다.
        // 따라서 그때 캐시 무효화를 할 수 있지만, 만약 사용자가 탈퇴를 하거나, 또 다른 의도치 않은 상황을 대비해 만료 시간 명시적으로 설정
        userPublicProfileRedisTemplate.opsForValue().set(cacheKey, userPublicProfileResponseDTO,
                CacheConstants.PROFILE_CACHE_HOURS,
                TimeUnit.HOURS);

        return userPublicProfileResponseDTO;
    }

    
    @Loggable
    @Transactional(readOnly = true)
    @Override
    public UserPrivateProfileResponse getUserPrivateProfile(String blogId) { // email 민감한 정보는 개인정보 보호를 위해 캐시 삭제

        log.info("[UserProfileServiceImpl] findUserByTokenAndByBlogId 메서드 시작");

        Optional<User> optionalUser = userRepository.findByBlogId(blogId);
        if (optionalUser.isEmpty()) {
            throw new BusinessException(
                    ErrorCode.USER_NOT_FOUND,
                    blogId + "를 가지고 있는 사용자를 찾지 못하였습니다.",
                    "UserProfileServiceImpl",
                    "findUserByTokenAndByBlogId");
        }

        User findUser = optionalUser.get();

        UserPrivateProfileResponse userPrivateProfileResponseDTO = new UserPrivateProfileResponse(findUser.getEmail(),
                findUser.getBlogId(), findUser.getBlogName(),
                findUser.getUsername(), findUser.getProfileImageUrl());

        return userPrivateProfileResponseDTO;
    }

    @Loggable
    @Override
    @Transactional
    public void updateUserSettings(String blogId, UserSettingsRequest userSettingsRequest) throws IOException {

        log.info("[UserProfileServiceImpl] updateUserSettings 메서드 시작");

        User user = userRepository.findByBlogId(blogId).orElseThrow(() -> new BusinessException(
                ErrorCode.USER_NOT_FOUND,
                blogId + "를 가지고 있는 사용자를 찾지 못하였습니다.",
                "UserProfileServiceImpl",
                "updateUserSettings"));

        String oldUsername = user.getUsername();

        try {
            if (userSettingsRequest.profileImage() != null && !userSettingsRequest.profileImage().isEmpty()) {

                log.info("[UserProfileServiceImpl] updateUserSettings 메서드 - 프로필 이미지가 null이 아닌 경우 분기 시작");

                String awsS3FileUrl = s3Service.uploadProfileImage(userSettingsRequest.profileImage(), blogId);

                user.profileUpdate(userSettingsRequest.username(), userSettingsRequest.blogName(),
                        awsS3FileUrl);
            } else {

                log.info("[UserProfileServiceImpl] updateUserSettings 메서드 - 프로필 이미지가 null인 경우 분기 시작");

                s3Service.deleteProfileImage(blogId);

                user.profileUpdate(userSettingsRequest.username(), userSettingsRequest.blogName(),
                        "https://iceamericano-blog-storage.s3.ap-northeast-2.amazonaws.com/default/default-avatar-profile.webp");
            }

            // 더티체킹으로 인한 업데이트. 따라서 save 메서드 불필요
            // userRepository.save(user);

            TransactionSynchronizationManager.registerSynchronization(
                    new TransactionSynchronization() {
                        @Override
                        public void afterCommit() {
                            // 사용자 정보 변경 시 캐시 무효화. 트랜잭션이 성공적으로 커밋되어야만 redis 캐시 무효화
                            userPublicProfileRedisTemplate.delete("userPublicProfile:" + blogId); 
                            redisTemplateBoolean.delete("isDuplicateUsername:" + oldUsername);
                        }

                    });
        } catch (S3Exception e) {
            throw new SystemException(
                    ErrorCode.USER_PROFILE_UPDATE_ERROR,
                    "프로필 업데이트 중 오류가 발생하였습니다.",
                    "UserProfileServiceImpl",
                    "updateUserSettings", e);
        }

    }

}
