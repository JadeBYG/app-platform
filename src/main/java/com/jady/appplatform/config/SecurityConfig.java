package com.jady.appplatform.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // 先关闭 CSRF，便于你后续调试 REST API（后面接 JWT 时会再系统处理）
                .csrf(csrf -> csrf.disable())

                // 授权规则：放行 Swagger / OpenAPI；其他请求需要认证
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/swagger-ui.html",
                                "/api/health"
                        ).permitAll()
                        .anyRequest().authenticated()
                )

                // 保留默认登录页（你看到的那个），用于保护其他路径
                .formLogin(Customizer.withDefaults())

                // 允许 logout（默认 /logout）
                .logout(Customizer.withDefaults());

        return http.build();
    }
}
