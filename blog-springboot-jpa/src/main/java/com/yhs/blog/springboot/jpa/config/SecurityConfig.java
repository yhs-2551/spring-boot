package com.yhs.blog.springboot.jpa.config;

import com.yhs.blog.springboot.jpa.service.impl.UserDetailServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.logout.CookieClearingLogoutHandler;
import org.springframework.security.web.authentication.logout.HeaderWriterLogoutHandler;
import org.springframework.security.web.authentication.logout.HttpStatusReturningLogoutSuccessHandler;
import org.springframework.security.web.header.writers.ClearSiteDataHeaderWriter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

import static org.springframework.security.web.header.writers.ClearSiteDataHeaderWriter.Directive.COOKIES;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final UserDetailServiceImpl userDetailService;

    // 스프링 시큐리티 기능 비활성화. 일반적으로 정적 리소스(이미지, html 파일)
    @Bean
    public WebSecurityCustomizer configure() {
        return (web) -> web.ignoring().requestMatchers("/static/**");
    }


    //    cors 설정 빈
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("http://localhost:3000"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http, BCryptPasswordEncoder bCryptPasswordEncoder, UserDetailsService userDetailsService) {
        DaoAuthenticationProvider daoAuthenticationProvider = new DaoAuthenticationProvider();
        daoAuthenticationProvider.setUserDetailsService(userDetailService);
        daoAuthenticationProvider.setPasswordEncoder(bCryptPasswordEncoder);
        return new ProviderManager(daoAuthenticationProvider);
    }


    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
//                /resource/**
                .authorizeHttpRequests((authorize) -> authorize
                                // 인증 없이 접근 가능한 경로 설정
                                .requestMatchers(HttpMethod.GET, "/api/posts", "/api/posts/{id}").permitAll()
                                .requestMatchers(HttpMethod.POST, "/api/posts").permitAll()
                                .requestMatchers(HttpMethod.PATCH, "/api/posts/{id}").permitAll()
                                .requestMatchers(HttpMethod.DELETE, "/api/posts/{id}").permitAll()
                                .requestMatchers(HttpMethod.POST, "/user").permitAll()
                                .requestMatchers(HttpMethod.POST, "/login").permitAll()
//                                .requestMatchers(HttpMethod.POST, "/logout").permitAll()
//                                .requestMatchers(HttpMethod.POST, "/api/posts").hasAnyAuthority(
//                                        "USER", "ADMIN")
//                                .anyRequest().authenticated()

//                         그 외의 모든 요청은 USER 또는 ADMIN 권한을 가진 사용자만 접근 가능
                                .anyRequest().hasAnyAuthority("USER", "ADMIN")

                )
                .logout((logout) -> logout 
                        .logoutUrl("/logout")
                        .permitAll() // 로그아웃 엔드포인트를 모두에게 허용
                        .addLogoutHandler(new CookieClearingLogoutHandler("JSESSIONID")) // 쿠키 정리 핸들러 추가
                        .addLogoutHandler(new HeaderWriterLogoutHandler(new ClearSiteDataHeaderWriter(COOKIES))) // Clear-Site-Data 헤더 추가 (쿠키만 지우기), COOKIE이외에 Cache도 지울 수 있다
                        .logoutSuccessHandler(new HttpStatusReturningLogoutSuccessHandler()) // 로그아웃 성공 시 HTTP 상태 코드만 반환
                )

//                .logout(logout -> logout.logoutSuccessUrl("/login").invalidateHttpSession(true)
//                ) invalidateHttpSession(true)는 jwt방식을 사용할 것이기에 필요x, logout.logoutSuccessUrl
//                ("/login")는 jsp 및 타임리프를 사용할 경우 사용

                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable());

        return http.build();
    }


}
