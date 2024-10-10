package com.yhs.board2.springboot.mybatis.domain;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;
import java.util.stream.Collectors;

public class CustomUser extends User {
    private static final long serialVersionUID = 1L;

    private MemberDTO memberDTO;

    public CustomUser(String username, String password,
                      Collection<? extends GrantedAuthority> authorities) {
        super(username, password, authorities);
    }

    public CustomUser(MemberDTO memberDTO) {

        super(memberDTO.getUserid(), memberDTO.getUserpw(),
                memberDTO.getAuthDTOList().stream().map(auth -> new SimpleGrantedAuthority(auth.getAuth())).collect(Collectors.toList()));

        this.memberDTO = memberDTO;
    }

}
