package com.yhs.blog.springboot.jpa.domain.user.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.yhs.blog.springboot.jpa.common.entity.BaseEntity;
import com.yhs.blog.springboot.jpa.domain.user.type.UserRole;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@ToString
@Entity
@Table(name = "users")
public class User extends BaseEntity implements UserDetails {

    private static final String DEFAULT_PROFILE_IMAGE_URL = "https://iceamericano-blog-storage.s3.ap-northeast-2.amazonaws.com/default/default-avatar-profile.webp";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50, unique = true)
    private String username;

    // OAUTH2 사용자의 경우 비밀번호를 저장할 필요가 없기 때문에 nullable true 설정
    // redis에 json형식으로 저장할때 보안상 redis에 저장되면 안되는 정보이기 때문에 제외.
    // 현재 USER entity를 redis를 사용해서 저장하진 않지만 일단 보류
    @Column(nullable = true, length = 255)
    @JsonIgnore
    private String password;

    @Column(nullable = false, length = 100, unique = true)
    @JsonIgnore // 민감한 정보 제외
    private String email;

    @Column(name = "profile_image_url", nullable = true, length = 255)
    private String profileImageUrl = DEFAULT_PROFILE_IMAGE_URL;

    @Column(nullable = false, length = 50, unique = true)
    private String blogId;

    // 이것도 프론트 측이랑 백엔드 측에서 중복확인 처리 나중에 해야할 듯
    @Column(nullable = false, length = 32, unique = true)
    private String blogName;

    @PrePersist
    public void prePersist() {
        this.blogName = this.username + "의 DevLog";
    }

    // 필드의 기본값 (UserRole.USER)은 객체가 기본 생성자를 통해 생성될 때 적용된다. 반면, 빌더 패턴을 사용하거나 파라미터화된
    // 생성자를 사용하는
    // 경우, 해당 기본값은 적용되지 않는다.
    @Enumerated(EnumType.STRING)
    @Column(length = 10, nullable = false)
    private UserRole role = UserRole.USER;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // 권한 반환 SimpleGrantedAuthority는 GrantedAuthority의 구현체
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(role.name()));
    }

    // 계정 만료 여부. true는 만료 되지 않음
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

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Builder
    public User(String email, String password, String username, String blogId, UserRole role) {
        this.email = email;
        this.password = password;
        this.username = username;
        this.blogId = blogId;
        this.role = role != null ? role : UserRole.USER;
    }

    public User profileUpdate(String username, String blogName, String profileImageUrl) {
        this.username = username;
        this.blogName = blogName;
        this.profileImageUrl = profileImageUrl;
        return this;
    }

    public User update(String username) {
        this.username = username;
        return this;
    }

}
