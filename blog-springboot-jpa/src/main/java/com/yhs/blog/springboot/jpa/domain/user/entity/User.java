package com.yhs.blog.springboot.jpa.domain.user.entity;

import com.yhs.blog.springboot.jpa.common.entity.BaseEntity;
import com.yhs.blog.springboot.jpa.domain.category.entity.Category;
import com.yhs.blog.springboot.jpa.domain.file.entity.File;
import com.yhs.blog.springboot.jpa.domain.post.entity.Comment;
import com.yhs.blog.springboot.jpa.domain.post.entity.Like;
import com.yhs.blog.springboot.jpa.domain.post.entity.Post;
import com.yhs.blog.springboot.jpa.domain.post.entity.PostTag;
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
import java.util.Set;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@ToString
@Entity
@Table(name = "Users")
public class User extends BaseEntity implements UserDetails {

    private static final String DEFAULT_PROFILE_IMAGE_URL = "https://iceamericano-blog-storage.s3.ap-northeast-2.amazonaws.com/default/default-avatar-profile.webp";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50, unique = true)
    private String username;

    @Column(nullable = false, length = 50, unique = true)
    private String blogId;

    @Column(nullable = false, length = 32, unique = true)
    private String blogName;

    @PrePersist
    public void prePersist() {
        this.blogName = this.username + "의 DevLog";
    }

    // OAUTH2 사용자의 경우 비밀번호를 저장할 필요가 없기 때문에 nullable true 설정
    @Column(nullable = true, length = 255)
    private String password;

    @Column(nullable = false, length = 100, unique = true)
    private String email;

    @Column(name = "profile_image_url", nullable = true, length = 255)
    private String profileImageUrl = DEFAULT_PROFILE_IMAGE_URL;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Post> posts;

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Category> categories;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<File> files;

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval =
            true)
    private Set<PostTag> postTags;


    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Comment> comments;

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Like> likes;

    //필드의 기본값 (UserRole.USER)은 객체가 기본 생성자를 통해 생성될 때 적용된다. 반면, 빌더 패턴을 사용하거나 파라미터화된 생성자를 사용하는
    // 경우, 해당 기본값은 적용되지 않는다.
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
