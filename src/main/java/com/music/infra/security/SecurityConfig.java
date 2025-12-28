package com.music.infra.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import static org.springframework.security.web.util.matcher.AntPathRequestMatcher.antMatcher;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(jsr250Enabled = true)
public class SecurityConfig {

        public static final String ADMIN = "ADMIN";
        private final SecurityFilter securityFilter;

        public SecurityConfig(SecurityFilter securityFilter) {
                this.securityFilter = securityFilter;
        }

        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
                httpSecurity
                                .cors(withDefaults -> {
                                })
                                .csrf(AbstractHttpConfigurer::disable)
                                .sessionManagement(session -> session
                                                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                                .authorizeHttpRequests(auth -> auth
                                                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                                                .requestMatchers(
                                                                antMatcher("/v1/music/auth/register"),
                                                                antMatcher("/v1/music/auth/login"),
                                                                antMatcher("/v1/music/auth/forgotPassword"),
                                                                antMatcher("/v1/music/auth/resetPassword"))
                                                .permitAll()

                                                .requestMatchers(
                                                                antMatcher("/v1/music/user/**"),
                                                                antMatcher("/v1/music/musics/post/**"),
                                                                antMatcher("/v1/music/musics/put/**"),
                                                                antMatcher("/v1/music/musics/delete/**"),
                                                                antMatcher("/v1/music/block_music/post/**"),
                                                                antMatcher("/v1/music/block_music/put/**"),
                                                                antMatcher("/v1/music/block_music/delete/**"),
                                                                antMatcher("/v1/music/repertoire/post/**"),
                                                                antMatcher("/v1/music/repertoire/put/**"),
                                                                antMatcher("/v1/music/repertoire/delete/**"),
                                                                antMatcher("/v1/music/event/post/**"),
                                                                antMatcher("/v1/music/event/get/**"),
                                                                antMatcher("/v1/music/event/put/**"),
                                                                antMatcher("/v1/music/event/delete/**"))
                                                .hasRole(ADMIN)

                                                .anyRequest().authenticated())
                                .addFilterBefore(securityFilter, UsernamePasswordAuthenticationFilter.class)
                                .headers(headers -> headers.frameOptions().disable());

                return httpSecurity.build();
        }
}
