// src/main/java/hello/wsd/config/SecurityConfig.java
package com.looky.security.config;

import com.looky.security.handler.CustomAccessDeniedHandler;
import com.looky.security.handler.CustomAuthenticationEntryPoint;
import com.looky.security.handler.OAuth2LoginSuccessHandler;
import com.looky.security.service.CustomOAuth2UserService;
import com.looky.security.jwt.JwtFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

        private final JwtFilter jwtFilter;
        private final CustomOAuth2UserService customOAuth2UserService;
        private final OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler;
        private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;
        private final CustomAccessDeniedHandler customAccessDeniedHandler;

        @Bean
        public PasswordEncoder passwordEncoder() {
                return new BCryptPasswordEncoder();
        }

        @Bean
        public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
                return configuration.getAuthenticationManager();
        }

        @Bean
        public CorsConfigurationSource corsConfigurationSource() {
                CorsConfiguration configuration = new CorsConfiguration();
                configuration.setAllowedOrigins(List.of("http://localhost:5173")); // 프론트 주소
                configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
                configuration.setAllowedHeaders(List.of("*"));
                configuration.setAllowCredentials(true);
                configuration.setExposedHeaders(List.of("Authorization", "Set-Cookie"));
                configuration.setMaxAge(3600L);

                UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
                source.registerCorsConfiguration("/**", configuration);
                return source;
        }

        @Bean
        public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

                // CORS 설정
                http
                        .cors(cors -> cors.configurationSource(corsConfigurationSource()));
                
                // csrf disable
                http
                        .csrf((auth) -> auth.disable());

                // From 로그인 방식 disable
                http
                        .formLogin((auth) -> auth.disable());

                // http basic 인증 방식 disable
                http
                        .httpBasic((auth) -> auth.disable());

                // 세션 설정
                http
                        .sessionManagement((session) -> session
                                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS));

                // 경로별 인가 작업
                http
                        .authorizeHttpRequests((auth) -> auth
                                .requestMatchers("/api/auth/**", "/reissue", "/docs", "/swagger-ui/**", "/v3/api-docs/**", "/health")
                                .permitAll()
                                .requestMatchers(HttpMethod.GET, "/api/stores/**", "/api/items/**", "/api/universities/*/organizations")
                                .permitAll()
                                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                                .anyRequest().authenticated());

                http.oauth2Login(oauth2 -> oauth2
                        .userInfoEndpoint(userInfo -> userInfo
                                        .userService(customOAuth2UserService) // 유저 정보 로드 서비스
                        )
                        .successHandler(oAuth2LoginSuccessHandler) // 로그인 성공 시 핸들러
                );

                // 예외 처리 핸들러 등록
                http.exceptionHandling(exception -> exception
                        .authenticationEntryPoint(customAuthenticationEntryPoint)
                        .accessDeniedHandler(customAccessDeniedHandler));

                http.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

                return http.build();
        }
}
