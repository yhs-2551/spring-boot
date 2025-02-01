package com.yhs.blog.springboot.jpa.domain.user.service.impl;
import com.yhs.blog.springboot.jpa.aop.log.Loggable;
import com.yhs.blog.springboot.jpa.common.constant.code.ErrorCode; 
import com.yhs.blog.springboot.jpa.domain.user.entity.User;
import com.yhs.blog.springboot.jpa.domain.user.repository.UserRepository;
import com.yhs.blog.springboot.jpa.domain.user.service.UserFindService;
import com.yhs.blog.springboot.jpa.exception.custom.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;

@Log4j2
@RequiredArgsConstructor
@Service
public class UserFindServiceImpl implements UserFindService {

    private final UserRepository userRepository;

    @Loggable
    @Override
    @Transactional(readOnly = true)
    public User findUserById(Long userId) {

        log.info("[UserFindServiceImpl] findUserById 메서드 시작");

        return userRepository.findById(userId).orElseThrow(() -> new BusinessException(
                ErrorCode.USER_NOT_FOUND,
                userId + "를 가지고 있는 사용자를 찾지 못하였습니다.",
                "UserFindServiceImpl",
                "findUserById"));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> findUserByEmail(String email) { // OAuth2성공 핸들러에서 신규 사용자 및 기존 사용자를 구분해야해서 찾지 못하더라도 예외를 발생시키면 안됨 

        log.info("[UserFindServiceImpl] findUserByEmail 메서드 시작");

        return userRepository.findByEmail(email);
    }

    // 글생성시 필요, redis를 사용할까 했지만 redis에 json 변환시 여러 문제, redis에 저장되면 영속성 detached 상태로
    // 변경되는 문제 등 때문에 사용x
    @Override
    @Transactional(readOnly = true)
    public User findUserByBlogId(String blogId) {

        log.info("[UserFindServiceImpl] findUserByBlogId 메서드 시작");

        Optional<User> optionalUser = userRepository.findByBlogId(blogId);
        if (optionalUser.isEmpty()) {
            throw new BusinessException(
                    ErrorCode.USER_NOT_FOUND,
                    blogId + "를 가지고 있는 사용자를 찾지 못하였습니다.",
                    "UserFindServiceImpl",
                    "findUserByBlogId");
        }

        return optionalUser.get();

    }

}
