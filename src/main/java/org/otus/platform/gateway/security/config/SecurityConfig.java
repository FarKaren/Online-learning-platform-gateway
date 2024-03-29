package org.otus.platform.gateway.security.config;

import org.otus.platform.gateway.client.UserClient;
import org.otus.platform.gateway.security.filter.JwtTokenFilter;
import org.otus.platform.gateway.security.jwt.JwtService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(
            final AuthenticationConfiguration configuration
    ) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    public AuthenticationEntryPoint authenticationEntryPoint() {
        return new UnauthorizedErrorAuthenticationEntryPoint();
    }


    @Bean
    public SecurityFilterChain filterChain(
            final HttpSecurity httpSecurity,
            final JwtService jwtService,
            final UserClient userClient
            ) throws Exception {
        httpSecurity
                .csrf(AbstractHttpConfigurer::disable)
                .cors(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .sessionManagement(sessionManagement ->
                        sessionManagement
                                .sessionCreationPolicy(
                                        SessionCreationPolicy.STATELESS
                                )
                )
                .exceptionHandling(exceptionHandling ->
                        exceptionHandling.authenticationEntryPoint(new UnauthorizedErrorAuthenticationEntryPoint()))
                .authorizeHttpRequests(configurer ->
                        configurer
                                .requestMatchers("/api/v1/register/**", "/api/v1/login/**")
                                .permitAll()
                                .requestMatchers("/swagger-ui/**")
                                .permitAll()
                                .requestMatchers("/v3/api-docs/**")
                                .permitAll()
                                .anyRequest().authenticated())
                .anonymous(AbstractHttpConfigurer::disable)
                .addFilterBefore(new JwtTokenFilter(userClient, jwtService),
                        UsernamePasswordAuthenticationFilter.class);

        return httpSecurity.build();
    }
}
