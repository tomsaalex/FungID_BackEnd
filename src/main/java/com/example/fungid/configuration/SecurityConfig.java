package com.example.fungid.configuration;

import com.example.fungid.domain.User;
import com.example.fungid.service.JwtService;
import com.example.fungid.service.UserService;
import com.example.fungid.utils.EndpointData;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.annotation.Nonnull;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Configuration
public class SecurityConfig {
    private final List<EndpointData> openEndpoints = new ArrayList<>();
    private final JwtService jwtService;
    private final UserService userService;

    public SecurityConfig(JwtService jwtService, UserService userService) {
        this.jwtService = jwtService;
        this.userService = userService;
    }

    @PostConstruct
    public void registerOpenEndpoints() {
        openEndpoints.add(new EndpointData("/api/users", HttpMethod.GET));
        openEndpoints.add(new EndpointData("/api/users/login", HttpMethod.POST));
        openEndpoints.add(new EndpointData("/api/users/register", HttpMethod.POST));
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(AbstractHttpConfigurer::disable)
                .csrf(AbstractHttpConfigurer::disable)
                .addFilterBefore(new TokenValidationFilter(), UsernamePasswordAuthenticationFilter.class)
                .authorizeHttpRequests((authz) -> authz
                        .requestMatchers("/api/**").permitAll()
                );

        return http.build();
    }

    public class TokenValidationFilter extends OncePerRequestFilter {
        @Override
        public void doFilterInternal(HttpServletRequest request, @Nonnull HttpServletResponse response, @Nonnull FilterChain filterChain) throws ServletException, IOException {
            String authHeader = request.getHeader("Authorization");
            String token;
            String username = null;

            try {
                if (authHeader != null && authHeader.startsWith("Bearer")) {
                    token = authHeader.substring(7);
                    username = jwtService.extractUsername(token);
                }
            } catch (ExpiredJwtException ex) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json");
                response.getWriter().write("Login credentials provided have already expired.");
                response.getWriter().flush();
                response.getWriter().close();
                return;
            } catch (MalformedJwtException ex) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.setContentType("application/json");
                response.getWriter().write("The login token sent is malformed and cannot be used to verify your identity.");
                response.getWriter().flush();
                response.getWriter().close();
                return;
            } catch (SignatureException ex) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.setContentType("application/json");
                response.getWriter().write("JWT signature does not match locally computed signature. JWT validity cannot be asserted and should not be trusted.");
                response.getWriter().flush();
                response.getWriter().close();
                return;
            }

            User user;
            try {
                user = userService.getUserByUsername(username);
            } catch (Exception ex) {
                user = null;
            }

            if (user != null) {
                request.setAttribute("userId", user.getId());
                filterChain.doFilter(request, response);
            } else if (openEndpoints.stream().anyMatch(endpoint -> endpoint.matches(request))) {
                filterChain.doFilter(request, response);
            } else {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json");
                response.getWriter().write("Request blocked due to attempting to access a secured endpoint with no credentials or invalid credentials.");
                response.getWriter().flush();
                response.getWriter().close();
            }
        }
    }
}
