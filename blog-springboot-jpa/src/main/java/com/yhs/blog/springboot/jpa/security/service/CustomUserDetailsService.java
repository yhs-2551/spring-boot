package com.yhs.blog.springboot.jpa.security.service;

import com.yhs.blog.springboot.jpa.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
@Log4j2
public class CustomUserDetailsService implements UserDetailsService {
    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) {
        log.info("[CustomUserDetailsService] loadUserByUsername() 메서드 시작");

        log.info("[CustomUserDetailsService] loadUserByUsername() email: {}", email);
        //
        // User user =
        // userRepository.findByEmail(email).orElseThrow(() -> new
        // UsernameNotFoundException(
        // "User not found with email: " + email));
        //
        // return new
        // org.springframework.security.core.userdetails.User(user.getEmail(),
        // user.getPassword(), user.getAuthorities());

        // BadCredentialsException Spring Security에서 제공하는 예외도 있음
        return userRepository.findByEmail(email).orElseThrow(() -> new UsernameNotFoundException(
                email + "에 해당하는 사용자를 찾을 수 없습니다."));
    }
}
