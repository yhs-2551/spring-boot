// package com.yhs.blog.springboot.jpa.security.service;

// import org.junit.jupiter.api.Test;
// import org.junit.jupiter.api.DisplayName;
// import org.junit.jupiter.api.extension.ExtendWith;
// import org.mockito.InjectMocks;
// import org.mockito.Mock;
// import org.mockito.junit.jupiter.MockitoExtension;
// import static org.assertj.core.api.Assertions.assertThat;
// import static org.junit.jupiter.api.Assertions.assertThrows;
// import static org.mockito.Mockito.when;
// import java.util.Optional;
// import org.springframework.security.core.userdetails.UserDetails;
// import org.springframework.security.core.userdetails.UsernameNotFoundException;

// import com.yhs.blog.springboot.jpa.domain.user.entity.User; 
// import com.yhs.blog.springboot.jpa.domain.user.repository.UserRepository;
// import com.yhs.blog.springboot.jpa.domain.user.type.UserRole;
 

// @ExtendWith(MockitoExtension.class)
// class CustomUserDetailsServiceTest {

//     @InjectMocks
//     private CustomUserDetailsService customUserDetailsService;

//     @Mock
//     private UserRepository userRepository;

//     @Test
//     @DisplayName("이메일로 사용자를 찾아 UserDetails 반환 성공")
//     void 사용자명으로_사용자_조회_성공() {
//         // given
//         String email = "";
//         User user = User.builder()
//                 .email(email)
//                 .password("password123")
//                 .role(UserRole.USER)
//                 .blogId("test-blog")
//                 .username("testuser")
//                 .build();
        
//         when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

//         // when
//         UserDetails userDetails = customUserDetailsService.loadUserByUsername(email);

//         // then
//         assertThat(userDetails).isNotNull();
//         assertThat(userDetails.getUsername()).isEqualTo("testuser");
//         assertThat(userDetails.getPassword()).isEqualTo("password123");
//         assertThat(userDetails.getAuthorities()).isNotEmpty();
//     }

//     @Test
//     @DisplayName("존재하지 않는 이메일로 조회시 예외 발생")
//     void 사용자명으로_사용자_조회_실패() {
//         // given
//         String email = "nonexistent@example.com";
//         when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

//         // when & then
//         assertThrows(UsernameNotFoundException.class, () -> 
//             customUserDetailsService.loadUserByUsername(email),
//             "User not found with email: " + email
//         );
//     }
// }
