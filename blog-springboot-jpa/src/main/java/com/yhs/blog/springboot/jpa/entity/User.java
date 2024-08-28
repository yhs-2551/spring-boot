package com.yhs.blog.springboot.jpa.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Set;

@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@ToString
@Entity
@Table(name = "Users")
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String username;

    @Column(nullable = false, length = 255)
    private String password;

    @Column(nullable = false, length = 100, unique = true)
    private String email;


    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Post> posts;

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Comment> comments;

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Like> likes;

    @Enumerated(EnumType.STRING)
    @Column(length = 10, nullable = false)
    private UserRole role = UserRole.USER;

    
    // 권한 반환 SimpleGrantedAuthority는 GrantedAuthority의 구현체
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(role.name()));
    }

    //계정 만료 여부. true는 만료 되지 않음
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    // 계정 잠금 여부. true는 잠금 되지 않음
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    // 패스워드 만료 여부 반환 true는 만료되지 않았음.
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    // 계정 사용 가능 여부 반환 true -> 사용 가능 
    @Override
    public boolean isEnabled() {
        return true;
    }

    @Builder
    public User(String email, String password, String username, UserRole role, String auth) {
        this.email = email;
        this.password = password;
        this.username = username;
        this.role = role;
    }

    public enum UserRole {
        USER, ADMIN
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public String getPassword() {
        return password;
    }

}
