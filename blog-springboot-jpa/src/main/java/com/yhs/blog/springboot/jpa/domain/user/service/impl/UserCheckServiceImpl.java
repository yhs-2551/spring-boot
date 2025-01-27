package com.yhs.blog.springboot.jpa.domain.user.service.impl;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.yhs.blog.springboot.jpa.aop.duplicatecheck.DuplicateCheck;
import com.yhs.blog.springboot.jpa.aop.log.Loggable;
import com.yhs.blog.springboot.jpa.common.constant.cache.CacheConstants;
import com.yhs.blog.springboot.jpa.domain.user.dto.response.DuplicateCheckResponse;
import com.yhs.blog.springboot.jpa.domain.user.repository.UserRepository;
import com.yhs.blog.springboot.jpa.domain.user.service.UserCheckService;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Log4j2
@RequiredArgsConstructor
@Service
public class UserCheckServiceImpl implements UserCheckService {

    private final UserRepository userRepository;
    private final RedisTemplate<String, Boolean> redisTemplate;

    @Override
    @Transactional(readOnly = true)
    public boolean isExistsBlogId(String blogId) {

        log.info("[UserCheckServiceImpl] isExistsBlogId 메서드 시작");

        String cacheKey = "isExists:" + blogId;

        Boolean exists = redisTemplate.opsForValue().get(cacheKey);
        if (exists != null) {

            log.info("[UserCheckServiceImpl] isExistsBlogId 메서드 - 캐시에 존재하는 경우 분기 시작");

            return exists;
        }

        log.info("[UserCheckServiceImpl] isExistsBlogId 메서드 - 캐시에 존재하지 않는 경우 분기 시작");

        boolean userExists = userRepository.existsByBlogId(blogId);

        if (userExists) {

            log.info("[UserCheckServiceImpl] isExistsBlogId 메서드 - DB에 사용자가 존재하는 경우 분기 시작");

            // 캐시에 저장.
            redisTemplate.opsForValue().set(cacheKey, true, CacheConstants.IS_EXISTS_BLOG_ID_CACHE_HOURS, TimeUnit.HOURS);
            return userExists;
        }

        log.info("[UserCheckServiceImpl] isExistsBlogId 메서드 - DB에 사용자가 존재하지 않는 경우 분기 시작");

        return userExists;
    }

    // 아래는 회원가입 시 중복확인 관련
    @Override
    @Loggable
    @DuplicateCheck(type = "BlogId")
    @Transactional(readOnly = true)
    public DuplicateCheckResponse isDuplicateBlogId(String blogId) {

        log.info("[UserCheckServiceImpl] isDuplicateBlogId 메서드 시작");

        String cacheKey = "isDuplicateBlogId:" + blogId;
        return checkDuplicate(cacheKey, () -> userRepository.existsByBlogId(blogId), "이미 존재하는 " +
                "BlogId 입니다. 다른 BlogId를 사용해 주세요.", "사용 가능한 BlogId 입니다.");
    }

    @Override
    @Loggable
    @DuplicateCheck(type = "Email")
    @Transactional(readOnly = true)
    public DuplicateCheckResponse isDuplicateEmail(String email) {

        log.info("[UserCheckServiceImpl] isDuplicateEmail 메서드 시작");

        String cacheKey = "isDuplicateEmail:" + email;
        return checkDuplicate(cacheKey, () -> userRepository.existsByEmail(email), "이미 존재하는 이메일 " +
                "입니다. 다른 이메일을 사용해 주세요.", "사용 가능한 이메일 입니다.");

    }

    @Override
    @Loggable
    @DuplicateCheck(type = "Username")
    @Transactional(readOnly = true)
    public DuplicateCheckResponse isDuplicateUsername(String username) {

        log.info("[UserCheckServiceImpl] isDuplicateUsername 메서드 시작");

        String cacheKey = "isDuplicateUsername:" + username;
        return checkDuplicate(cacheKey, () -> userRepository.existsByUsername(username), "이미 존재하는" +
                " 사용자명 입니다. 다른 사용자명을 사용해 주세요.", "사용 가능한 사용자명 입니다.");
    }

    private DuplicateCheckResponse checkDuplicate(String cacheKey, Supplier<Boolean> dbCheck, String existMessage,
            String notExistMessage) {

        log.info("[UserCheckServiceImpl] checkDuplicate 메서드 시작");

        // boolean 기본 타입은 null값을 가질 수 없기 때문에 null 비교 하려면 래퍼 클래스 사용필요.
        Boolean exists = redisTemplate.opsForValue().get(cacheKey);
        if (exists != null) {

            log.info("[UserCheckServiceImpl] checkDuplicate 메서드 - 캐시에 존재하는 경우 분기 시작");

            // 캐시 조회 성공
            return new DuplicateCheckResponse(true, existMessage);
        }

        log.info("[UserCheckServiceImpl] checkDuplicate 메서드 - 캐시에 존재하지 않는 경우 분기 시작");

        boolean isExists = dbCheck.get();
        if (isExists) {

            log.info("[UserCheckServiceImpl] checkDuplicate 메서드 - DB에 존재하는 경우 분기 시작");

            // DB 조회 성공
            // 캐시에 저장. 일단 무한대. 사용자 계정 변경 및 계정 탈퇴 시 무효화 필요요
            redisTemplate.opsForValue().set(cacheKey, true, CacheConstants.DUPLICATE_CHECK_CACHE_HOURS, TimeUnit.HOURS);
            return new DuplicateCheckResponse(true, existMessage);
        }

        log.info("[UserCheckServiceImpl] checkDuplicate 메서드 - DB에 존재하지 않는 경우 분기 시작");

        return new DuplicateCheckResponse(false, notExistMessage);
    }

}
