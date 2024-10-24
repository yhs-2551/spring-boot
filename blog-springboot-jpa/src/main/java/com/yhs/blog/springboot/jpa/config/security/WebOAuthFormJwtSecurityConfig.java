package com.yhs.blog.springboot.jpa.config.security;

import com.yhs.blog.springboot.jpa.config.jwt.TokenAuthenticationFilter;
import com.yhs.blog.springboot.jpa.config.jwt.TokenManagementService;
import com.yhs.blog.springboot.jpa.config.jwt.TokenProvider;
import com.yhs.blog.springboot.jpa.config.oauth.OAuth2AuthorizationRequestBasedOnCookieRepository;
import com.yhs.blog.springboot.jpa.config.oauth.OAuth2SuccessHandler;
import com.yhs.blog.springboot.jpa.config.oauth.OAuth2UserCustomService;
import com.yhs.blog.springboot.jpa.repository.RefreshTokenRepository;
import com.yhs.blog.springboot.jpa.service.impl.TokenServiceImpl;
import com.yhs.blog.springboot.jpa.service.impl.UserDetailServiceImpl;
import com.yhs.blog.springboot.jpa.service.impl.UserServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;

import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.CookieClearingLogoutHandler;
import org.springframework.security.web.authentication.logout.HttpStatusReturningLogoutSuccessHandler;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class WebOAuthFormJwtSecurityConfig {

    private final OAuth2UserCustomService oAuth2UserCustomService;
    private final TokenProvider tokenProvider;
    private final TokenManagementService tokenManagementService;
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserServiceImpl userService;
    private final TokenServiceImpl tokenService;
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

//        클라이언트가 서버로의 요청에 인증 정보를 포함할 수 있도록 허용하는 설정. 쿠키, Authorization 헤더, TLS 인증서 등
        configuration.setAllowCredentials(true);

        // 클라이언트가 접근할 수 있도록 노출할 헤더 설정. 이렇게 해야 클라이언트에서 Authorization 헤더에 접근 가능하다.
        configuration.setExposedHeaders(List.of("Authorization"));

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


//    JSON 문서: http://localhost:8080/v3/api-docs
//    애플리케이션의 모든 API 엔드포인트에 대한 상세한 OpenAPI 명세를 JSON 형식으로 확인할 수 있다.
//
//    Swagger UI: http://localhost:8080/swagger-ui.html
//    브라우저에서 API 문서를 시각적으로 탐색하고 테스트할 수 있다.

    private static final String[] SWAGGER_WHITELIST = {
            "/swagger-ui/**",
            "v3/api-docs/**",
            "swagger-resources/**",
            "/swagger-resources"
    };

    @Bean
    public TokenAuthenticationFilter tokenAuthenticationFilter() {
        return new TokenAuthenticationFilter(tokenProvider);
    }

    @Bean
    public OAuth2AuthorizationRequestBasedOnCookieRepository oAuth2AuthorizationRequestBasedOnCookieRepository() {
        return new OAuth2AuthorizationRequestBasedOnCookieRepository();
    }

    @Bean
    public OAuth2SuccessHandler oAuth2SuccessHandler() {
        return new OAuth2SuccessHandler(tokenProvider, tokenManagementService,
                oAuth2AuthorizationRequestBasedOnCookieRepository(), userService, tokenService);
    }


    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .sessionManagement((session) -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // 세션 사용하지 않음
                .httpBasic(httpBasic -> httpBasic.disable()) // jwt방식에서 http basic 방식 비활성화. 기본적으로
                // 켜져 있음
                .addFilterBefore(tokenAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class)
//                /resource/**
                .authorizeHttpRequests((authorize) -> authorize
                                //SWAGGER 설정 부분
                                // 인증 없이 접근 가능한 경로 설정
                                .requestMatchers(SWAGGER_WHITELIST).permitAll()
//                                .requestMatchers(HttpMethod.POST, "/api/token").permitAll()
//                                .requestMatchers(HttpMethod.POST, "/user", "/login").permitAll()
//                                .requestMatchers(HttpMethod.GET, "/api/posts", "/api/posts/{id}").permitAll()
//                                //그 외 /api/** 모든 경로는 인증 필요
//                                .requestMatchers("/api/**").authenticated()
                                // 나머지 경로 임시로 모두 접근 가능
                                .anyRequest().permitAll()
                        // 그 외의 모든 요청은 USER 또는 ADMIN 권한을 가진 사용자만 접근 가능. 임시로 주석. 나중에 적용
//                                .anyRequest().hasAnyAuthority("USER", "ADMIN")


                )
                // Authorization 요청과 관련된 상태 저장
                .oauth2Login(oauth2 -> oauth2
                        .authorizationEndpoint((authorizationEndpointConfig) -> authorizationEndpointConfig.authorizationRequestRepository(oAuth2AuthorizationRequestBasedOnCookieRepository())
                        )
                        .userInfoEndpoint((userInfoEndpointConfig) -> userInfoEndpointConfig.userService(oAuth2UserCustomService))
                        //인증 성공 시 실행할 핸들러
                        .redirectionEndpoint(redirectEndpointConfig -> redirectEndpointConfig.baseUri("/login/oauth2/code/*"))
                        .successHandler(oAuth2SuccessHandler())

                )
//                .formLogin(formLogin -> formLogin.loginProcessingUrl("/user/login").successHandler(formLoginSuccessHandler()))
                // /api/로 시작하는 url인 경우 401 상태 코드 즉, 권한이 없다는 상태 코드를 반환하도록 예외 처리.
                .exceptionHandling(exceptionHandling -> exceptionHandling.defaultAuthenticationEntryPointFor(
                                new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED),
                                new AntPathRequestMatcher("/api/**")
                        )
                );

        return http.build();

        // 시큐리티를 사용한 세션 로그아웃 처리 방식
//                .logout((logout) -> logout
//                        .logoutUrl("/logout")
//                        .permitAll() // 로그아웃 엔드포인트를 모두에게 허용
//                        .addLogoutHandler(new CookieClearingLogoutHandler("JSESSIONID")) // 쿠키 정리
//                        // 핸들러 추가 (세션 방식의 경우 브라우저의 쿠키에 세션 ID값이 담겨있기 때문)
//                        .addLogoutHandler(new HeaderWriterLogoutHandler(new ClearSiteDataHeaderWriter(COOKIES))) // Clear-Site-Data 헤더 추가 (쿠키만 지우기), COOKIE이외에 Cache도 지울 수 있다
//                        .logoutSuccessHandler(new HttpStatusReturningLogoutSuccessHandler()) // 로그아웃 성공 시 HTTP 상태 코드만 반환
//                )

    }


}
