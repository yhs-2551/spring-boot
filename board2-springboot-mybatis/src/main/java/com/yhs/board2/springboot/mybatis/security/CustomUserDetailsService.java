package com.yhs.board2.springboot.mybatis.security;


import com.yhs.board2.springboot.mybatis.domain.CustomUser;
import com.yhs.board2.springboot.mybatis.domain.MemberDTO;
import com.yhs.board2.springboot.mybatis.mapper.MemberMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

@RequiredArgsConstructor
@Log4j2
public class CustomUserDetailsService implements UserDetailsService {
    private final MemberMapper memberMapper;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        log.warn("Load User By UserName Check: " + username);
        MemberDTO memberDTO = memberMapper.read(username);

        return  memberDTO == null ? null : new CustomUser(memberDTO);

    }
}
