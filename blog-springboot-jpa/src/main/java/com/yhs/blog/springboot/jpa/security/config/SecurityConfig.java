package com.yhs.blog.springboot.jpa.security.config;

import com.yhs.blog.springboot.jpa.domain.oauth2.filter.RememberMeAuthenticationFilter;
import com.yhs.blog.springboot.jpa.domain.oauth2.handler.OAuth2SuccessHandler;
import com.yhs.blog.springboot.jpa.domain.oauth2.repository.OAuth2AuthorizationRequestBasedOnCookieRepository;
import com.yhs.blog.springboot.jpa.domain.oauth2.service.OAuth2UserLoadService;
import com.yhs.blog.springboot.jpa.domain.token.jwt.filter.TokenAuthenticationFilter;
import com.yhs.blog.springboot.jpa.domain.token.jwt.provider.AuthenticationProvider;
import com.yhs.blog.springboot.jpa.domain.token.jwt.provider.TokenProvider;
import com.yhs.blog.springboot.jpa.domain.token.jwt.validation.TokenValidator;
import com.yhs.blog.springboot.jpa.domain.user.service.UserService; 
import com.yhs.blog.springboot.jpa.security.service.CustomUserDetailsService;
import com.yhs.blog.springboot.jpa.web.cookie.TokenCookieManager;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestRedirectFilter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

        private final OAuth2UserLoadService oAuth2UserCustomService;
        private final TokenProvider tokenProvider;
        private final TokenValidator tokenValidator;
        private final AuthenticationProvider authenticationProvider;
        private final TokenCookieManager TokenCookieManager;
        private final UserService userService;
        private final CustomUserDetailsService userDetailService;
        private final RedisTemplate<String, String> redisTemplate;

        // 스프링 시큐리티 기능 비활성화. 일반적으로 정적 리소스(이미지, html 파일)
        @Bean
        public WebSecurityCustomizer configure() {
                return (web) -> web.ignoring().requestMatchers("/static/**");
        }

        // cors 설정 빈
        @Bean
        public CorsConfigurationSource corsConfigurationSource() {
                CorsConfiguration configuration = new CorsConfiguration();
                configuration.setAllowedOrigins(List.of("http://localhost:3000"));
                configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH"));
                configuration.setAllowedHeaders(List.of("*"));

                // 클라이언트가 서버로의 요청에 인증 정보를 포함할 수 있도록 허용하는 설정. 쿠키, Authorization 헤더, TLS 인증서 등
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
        public AuthenticationManager authenticationManager(HttpSecurity http,
                        BCryptPasswordEncoder bCryptPasswordEncoder,
                        UserDetailsService userDetailsService) {
                DaoAuthenticationProvider daoAuthenticationProvider = new DaoAuthenticationProvider();
                daoAuthenticationProvider.setUserDetailsService(userDetailService);
                daoAuthenticationProvider.setPasswordEncoder(bCryptPasswordEncoder);
                return new ProviderManager(daoAuthenticationProvider);
        }

        // JSON 문서: http://localhost:8000/v3/api-docs
        // 애플리케이션의 모든 API 엔드포인트에 대한 상세한 OpenAPI 명세를 JSON 형식으로 확인할 수 있다.
        //
        // Swagger UI: http://localhost:8000/swagger-ui.html
        // 브라우저에서 API 문서를 시각적으로 탐색하고 테스트할 수 있다.

        private static final String[] SWAGGER_WHITELIST = {
                        "/swagger-ui.html",
                        "/swagger-ui/**",
                        "/v3/api-docs",
                        "/v3/api-docs/**",
                        "/swagger-resources",
                        "/swagger-resources/**"
        };

        @Bean
        public TokenAuthenticationFilter tokenAuthenticationFilter() {
                return new TokenAuthenticationFilter(tokenValidator, authenticationProvider);
        }

        @Bean
        public OAuth2AuthorizationRequestBasedOnCookieRepository oAuth2AuthorizationRequestBasedOnCookieRepository() {
                return new OAuth2AuthorizationRequestBasedOnCookieRepository();
        }

        @Bean
        public OAuth2SuccessHandler oAuth2SuccessHandler() {
                return new OAuth2SuccessHandler(tokenProvider, TokenCookieManager,
                                oAuth2AuthorizationRequestBasedOnCookieRepository(), userService, redisTemplate);
        }
 

        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
                http
                                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                                .csrf(csrf -> csrf.disable())
                                // h2-console iframe 사용을 위한 설정
                                .headers(headers -> headers.frameOptions(frameOptions -> frameOptions.disable()))
                                .sessionManagement((session) -> session
                                                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // 세션 사용하지 않음
                                .httpBasic(httpBasic -> httpBasic.disable()) // jwt방식에서 http basic 방식 비활성화. 기본적으로 켜져 있음
                                .addFilterBefore(tokenAuthenticationFilter(),
                                                UsernamePasswordAuthenticationFilter.class)
                                // /resource/**
                                .authorizeHttpRequests((authorize) -> authorize
                                                // SWAGGER 설정 부분
                                                // 인증 없이 접근 가능한 경로 설정
                                                .requestMatchers(SWAGGER_WHITELIST).permitAll()
                                                // GET 요청 permitAll
                                                .requestMatchers(HttpMethod.GET, "/api/token/initial-token").permitAll()
                                                .requestMatchers(HttpMethod.GET, "/api/token/new-token").permitAll()
                                                .requestMatchers(HttpMethod.GET, "/api/*/posts").permitAll()
                                                .requestMatchers(HttpMethod.GET, "/api/*/posts/page/*").permitAll()
                                                .requestMatchers(HttpMethod.GET, "/api/posts").permitAll()
                                                .requestMatchers(HttpMethod.GET, "/api/*/posts/*").permitAll()
                                                .requestMatchers(HttpMethod.GET, "/api/*/categories").permitAll()
                                                .requestMatchers(HttpMethod.GET, "/api/*/categories/*/posts")
                                                .permitAll()
                                                .requestMatchers(HttpMethod.GET, "/api/*/categories/*/posts/page/*")
                                                .permitAll()
                                                .requestMatchers(HttpMethod.GET, "/api/users/*/profile").permitAll()
                                                .requestMatchers(HttpMethod.GET, "/api/check/**").permitAll()
                                                // POST 요청 permitAll
                                                .requestMatchers(HttpMethod.POST, "/api/users/signup").permitAll()
                                                .requestMatchers(HttpMethod.POST, "/api/users/login").permitAll()
                                                .requestMatchers(HttpMethod.POST, "/api/users/logout").permitAll()
                                                .requestMatchers(HttpMethod.POST, "/api/users/verify-email").permitAll()
                                                .requestMatchers(HttpMethod.POST, "/api/oauth2/users").permitAll()
                                                .requestMatchers(HttpMethod.POST, "/api/admin/batch/cleanup")
                                                .permitAll()
                                                // 나머지는 인증 필요
                                                .anyRequest().authenticated()
                                // 그 외의 모든 요청은 USER 또는 ADMIN 권한을 가진 사용자만 접근 가능. 임시로 주석. 나중에 적용
                                // .anyRequest().hasAnyAuthority("USER", "ADMIN")

                                )
                                .addFilterBefore(new RememberMeAuthenticationFilter(redisTemplate),
                                                OAuth2AuthorizationRequestRedirectFilter.class)
                                // Authorization 요청과 관련된 상태 저장
                                .oauth2Login(oauth2 -> oauth2
                                                .authorizationEndpoint((
                                                                authorizationEndpointConfig) -> authorizationEndpointConfig
                                                                                .authorizationRequestRepository(
                                                                                                oAuth2AuthorizationRequestBasedOnCookieRepository()))
                                                .userInfoEndpoint(
                                                                (userInfoEndpointConfig) -> userInfoEndpointConfig
                                                                                .userService(oAuth2UserCustomService))
                                                .redirectionEndpoint(
                                                                redirectEndpointConfig -> redirectEndpointConfig
                                                                                .baseUri("/login/oauth2/code/*"))
                                                // 인증 성공 시 실행할 핸들러
                                                .successHandler(oAuth2SuccessHandler())

                                )
                                // .formLogin(formLogin ->
                                // formLogin.loginProcessingUrl("/user/login").successHandler(formLoginSuccessHandler()))
                                // /api/로 시작하는 url인 경우 401 상태 코드 즉, 권한이 없다는 상태 코드를 반환하도록 예외 처리.
                                .exceptionHandling(exceptionHandling -> exceptionHandling
                                                .defaultAuthenticationEntryPointFor(
                                                                new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED),
                                                                new AntPathRequestMatcher("/api/**")));

                return http.build();

                // 시큐리티를 사용한 세션 로그아웃 처리 방식
                // .logout((logout) -> logout
                // .logoutUrl("/logout")
                // .permitAll() // 로그아웃 엔드포인트를 모두에게 허용
                // .addLogoutHandler(new CookieClearingLogoutHandler("JSESSIONID")) // 쿠키 정리
                // // 핸들러 추가 (세션 방식의 경우 브라우저의 쿠키에 세션 ID값이 담겨있기 때문)
                // .addLogoutHandler(new HeaderWriterLogoutHandler(new
                // ClearSiteDataHeaderWriter(COOKIES))) // Clear-Site-Data 헤더 추가 (쿠키만 지우기),
                // COOKIE이외에 Cache도 지울 수 있다
                // .logoutSuccessHandler(new HttpStatusReturningLogoutSuccessHandler()) // 로그아웃
                // 성공 시 HTTP 상태 코드만 반환
                // )

        }

}
