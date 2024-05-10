package com.example.fungid.configuration;

import com.example.fungid.utils.EndpointData;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.config.annotation.InterceptorRegistration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Configuration
public class SecurityConfig {
    private final List<EndpointData> openEndpoints = new ArrayList<>();

    @PostConstruct
    public void registerOpenEndpoints() {
        openEndpoints.add(new EndpointData("/api/users", HttpMethod.GET));
        openEndpoints.add(new EndpointData("/api/users/login", HttpMethod.POST));
        openEndpoints.add(new EndpointData("/api/users/register", HttpMethod.POST));
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        InterceptorRegistry registry = new InterceptorRegistry();
        registry.addInterceptor(new TokenValidationInterceptor());

        http
                .cors(AbstractHttpConfigurer::disable)
                .csrf(AbstractHttpConfigurer::disable)
                .addFilterBefore(new TokenValidationFilter(), UsernamePasswordAuthenticationFilter.class)
                .authorizeHttpRequests((authz) -> authz
                        .requestMatchers("/api/**").permitAll()
                );

        return http.build();
    }

    @Autowired
    private TokenValidationInterceptor tokenValidationInterceptor;

    private class TokenValidationFilter extends OncePerRequestFilter {
        @Override
        protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) {
            try {
                if (tokenValidationInterceptor.preHandle(request, response, null)
                    ||    openEndpoints.stream().anyMatch(endpoint -> endpoint.matches(request))) {
                    filterChain.doFilter(request, response);
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
}
