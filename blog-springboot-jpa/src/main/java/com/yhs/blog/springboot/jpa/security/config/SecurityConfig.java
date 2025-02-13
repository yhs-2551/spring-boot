package com.yhs.blog.springboot.jpa.security.config;

import com.yhs.blog.springboot.jpa.domain.auth.token.filter.TokenAuthenticationFilter;
import com.yhs.blog.springboot.jpa.domain.auth.token.provider.AuthenticationProvider;
import com.yhs.blog.springboot.jpa.domain.auth.token.provider.TokenProvider;
import com.yhs.blog.springboot.jpa.domain.auth.token.validation.TokenValidator;
import com.yhs.blog.springboot.jpa.domain.oauth2.filter.RememberMeAuthenticationFilter;
import com.yhs.blog.springboot.jpa.domain.oauth2.handler.OAuth2SuccessHandler;
import com.yhs.blog.springboot.jpa.domain.oauth2.repository.OAuth2AuthorizationRequestBasedOnCookieRepository;
import com.yhs.blog.springboot.jpa.domain.oauth2.service.OAuth2UserLoadService;
import com.yhs.blog.springboot.jpa.domain.user.service.UserFindService;
import com.yhs.blog.springboot.jpa.security.service.CustomUserDetailsService;
import com.yhs.blog.springboot.jpa.web.cookie.TokenCookieManager;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

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
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestRedirectFilter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true) // PreAuthorize, PostAuthorize관련 어노테이션 활성화
@RequiredArgsConstructor
@Log4j2
public class SecurityConfig {

        private final OAuth2UserLoadService oAuth2UserLoadService;
        private final TokenProvider tokenProvider;
        private final TokenValidator tokenValidator;
        private final AuthenticationProvider authenticationProvider;
        private final TokenCookieManager TokenCookieManager;
        private final UserFindService userFindService;
        private final CustomUserDetailsService userDetailService;
        private final RedisTemplate<String, String> redisTemplate;

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

        // actuator: 자세한 엔드포인트는 all endpoint로 검색 아래는 자주 쓰이는 것들
        // http://localhost:8000/actuator/health
        // http://localhost:8000/actuator/metrics
        // http://localhost:8000/actuator/prometheus
        // http://localhost:8000/actuator/loggers: 로그 레벨 확인/변경
        // http://localhost:8000/actuator/env : 환경 설정 정보
        // http://localhost:8000/actuator/metrics/jvm.memory.used : JVM 메모리 사용량
        // http://localhost:8000/actuator/metrics/http.server.requests : HTTP 요청 통계
        // curl http://localhost:8000/actuator/metrics/http.server.requests 과 같이 curl로
        // get 요청 해도 편함

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
                                oAuth2AuthorizationRequestBasedOnCookieRepository(), userFindService, redisTemplate);
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
                                                // 정적 리소스
                                                .requestMatchers("/static/**").permitAll()
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
                                                .requestMatchers(HttpMethod.GET, "/api/*/posts/*/edit").permitAll()
                                                .requestMatchers(HttpMethod.GET, "/api/*/categories").permitAll()
                                                .requestMatchers(HttpMethod.GET, "/api/*/categories/*/posts")
                                                .permitAll()
                                                .requestMatchers(HttpMethod.GET, "/api/*/categories/*/posts/page/*")
                                                .permitAll()
                                                .requestMatchers(HttpMethod.GET, "/api/users/*/profile").permitAll()
                                                .requestMatchers(HttpMethod.GET, "/api/check/**").permitAll()
                                                .requestMatchers(HttpMethod.GET, "/actuator/**").permitAll() // actuator
                                                                                                             // 활성화
                                                // POST 요청 permitAll
                                                .requestMatchers(HttpMethod.POST, "/api/users/signup").permitAll()
                                                .requestMatchers(HttpMethod.POST, "/api/auth/login").permitAll()
                                                .requestMatchers(HttpMethod.POST, "/api/auth/logout").permitAll()
                                                .requestMatchers(HttpMethod.POST, "/api/users/verify-code").permitAll()
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
                                                                                .userService(oAuth2UserLoadService))
                                                .redirectionEndpoint(
                                                                redirectEndpointConfig -> redirectEndpointConfig
                                                                                .baseUri("/login/oauth2/code/*"))
                                                // 인증 성공 시 실행할 핸들러
                                                .successHandler(oAuth2SuccessHandler())

                                );
                // 아래 AuthenticationEntryPoint를 이용해서 필터에서 발생하는 예외를 중앙화 하여 분기 처리해서 예외 메시지 작성하려
                // 했으나, 기능은 작동하는데 예외 메시지 분기 처리가 작동하지 않아서 그냥 token filter에서 직접 처리
                // /api/로 시작하는 url인 경우 + Security Filter Chain에서 발생하는 인증/인가 예외 처리. 401 상태 코드 즉,
                // 권한이 없다는 상태 코드를 반환하도록 예외 처리.
                // .exceptionHandling(exceptionHandling -> exceptionHandling
                // .defaultAuthenticationEntryPointFor(
                // new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED),
                // new AntPathRequestMatcher("/api/**")));
                return http.build();

        }

}
