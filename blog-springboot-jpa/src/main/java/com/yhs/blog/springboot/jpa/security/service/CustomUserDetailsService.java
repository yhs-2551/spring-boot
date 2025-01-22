package com.yhs.blog.springboot.jpa.security.service;

import com.yhs.blog.springboot.jpa.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
@Log4j2
public class CustomUserDetailsService implements UserDetailsService {
    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) {
        log.info("[CustomUserDetailsService] loadUserByUsername() 메서드 시작");
        //
        // User user =
        // userRepository.findByEmail(email).orElseThrow(() -> new
        // UsernameNotFoundException(
        // "User not found with email: " + email));
        //
        // return new
        // org.springframework.security.core.userdetails.User(user.getEmail(),
        // user.getPassword(), user.getAuthorities());

        // Srping Security의 BadCredentialsException 유지. 커스텀으로 변경하지 않음
        // UserNameNotFoundException Spring Security에서 제공하는 예외도 있음
        return userRepository.findByEmail(email).orElseThrow(() -> new BadCredentialsException(
                "이메일 또는 비밀번호가 잘못되었습니다."));
    }
}
