package com.cuutruyen.config;

import com.cuutruyen.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.Collections;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // Admin Endpoints
                .requestMatchers(HttpMethod.GET, "/api/admin/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.GET, "/api/manga/admin").hasRole("ADMIN")
                .requestMatchers(HttpMethod.GET, "/api/comment/all").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/comment/*/admin").hasRole("ADMIN")
                
                // Public Endpoints
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/api/ai/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/manga/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/chapter/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/comment/**").permitAll()
                
                // Uploader/Translator/Admin/Editor Endpoints
                .requestMatchers(HttpMethod.POST, "/api/manga/**").hasAnyRole("ADMIN", "UPLOADER", "TRANSLATOR", "EDITOR")
                .requestMatchers(HttpMethod.PUT, "/api/manga/**").hasAnyRole("ADMIN", "UPLOADER", "TRANSLATOR", "EDITOR")
                .requestMatchers(HttpMethod.POST, "/api/chapter/**").hasAnyRole("ADMIN", "UPLOADER", "TRANSLATOR", "EDITOR")
                
                // Group Endpoints
                .requestMatchers(HttpMethod.GET, "/api/groups/my-group").authenticated()
                .requestMatchers(HttpMethod.GET, "/api/groups/**").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/groups/request").authenticated()
                .requestMatchers(HttpMethod.PUT, "/api/groups/{id}/accept", "/api/groups/{id}/reject").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/groups/**").hasRole("ADMIN")
                
                // User Endpoints
                .requestMatchers(HttpMethod.PUT, "/api/users/*/role").hasRole("ADMIN")
                
                // Report Endpoints
                .requestMatchers(HttpMethod.POST, "/api/reports").authenticated()
                .requestMatchers(HttpMethod.GET, "/api/reports/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/reports/**").hasRole("ADMIN")

                // Menu API
                .requestMatchers(HttpMethod.GET, "/api/menus/tree").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/menus/**").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/menus/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/menus/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/menus/**").hasRole("ADMIN")
                
                // Root path and static resources (Frontend)
                .requestMatchers("/", "/index.html", "/favicon.ico").permitAll()
                .requestMatchers("/pages/**", "/assets/**").permitAll()
                // Swagger UI
                .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                // All other requests require authentication
                .requestMatchers("/error", "/uploads/**").permitAll()
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        // Allow local dev
        configuration.setAllowedOriginPatterns(Arrays.asList(
            "http://localhost:*",
            "http://127.0.0.1:*",
            "null"
        ));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "Accept", "X-Requested-With", "Remember-Me"));
        configuration.setExposedHeaders(Collections.singletonList("Authorization"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
