package com.yhs.blog.springboot.jpa.security.service;

import com.yhs.blog.springboot.jpa.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class CustomUserDetailsService implements UserDetailsService {
    private final UserRepository userRepository;


    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
//
//        User user =
//                userRepository.findByEmail(email).orElseThrow(() -> new UsernameNotFoundException(
//                        "User not found with email: " + email));
//
//        return new org.springframework.security.core.userdetails.User(user.getEmail(),
//                user.getPassword(), user.getAuthorities());

        return userRepository.findByEmail(email).orElseThrow(() -> new UsernameNotFoundException(
                "User not found with email: " + email));
    }
}
