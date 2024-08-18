package com.example.fungid.config;

import com.example.fungid.configuration.SecurityConfig;
import com.example.fungid.service.JwtService;
import com.example.fungid.service.UserService;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.IOException;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@Import(SecurityConfig.class)
public class TokenValidationFilterTest {
    private static final String testUri = "/testUri";

    @Mock
    private UserService userService;

    @Mock
    private JwtService jwtService;

    private SecurityConfig.TokenValidationFilter tokenAuthenticationFilter;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        SecurityConfig securityConfig = new SecurityConfig(jwtService, userService);
        tokenAuthenticationFilter = securityConfig.new TokenValidationFilter();
    }

    @Test
    @Tag("Unit_Testing")
    public void test_doFilterInternal_missingToken() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI(testUri);
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain filterChain = new MockFilterChain();

        tokenAuthenticationFilter.doFilterInternal(request, response, filterChain);

        assertThat(response.getStatus()).isEqualTo(HttpStatus.UNAUTHORIZED.value());
        assertThat(response.getContentAsString()).isEqualTo("Request blocked due to attempting to access a secured endpoint with no credentials or invalid credentials.");
    }

    @Test
    @Tag("Unit_Testing")
    public void test_doFilterInternal_expiredToken() throws ServletException, IOException {
        // Arrange
        MockHttpServletRequest request = new MockHttpServletRequest();
        String token = "expiredToken";
        request.addHeader("Authorization", "Bearer " + token);
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain filterChain = new MockFilterChain();

        Mockito.when(jwtService.extractUsername(token)).thenThrow(new ExpiredJwtException(null, null, "Login credentials provided have already expired."));

        // Act
        tokenAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        assertThat(response.getStatus()).isEqualTo(HttpStatus.UNAUTHORIZED.value());
        assertThat(response.getContentAsString()).isEqualTo("Login credentials provided have already expired.");
    }

    @Test
    @Tag("Unit_Testing")
    public void test_doFilterInternal_malformedToken() throws ServletException, IOException {
        // Arrange
        MockHttpServletRequest request = new MockHttpServletRequest();
        String token = "malformedToken";
        request.addHeader("Authorization", "Bearer " + token);
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain filterChain = new MockFilterChain();

        Mockito.when(jwtService.extractUsername(token)).thenThrow(new MalformedJwtException("The login token sent is malformed and cannot be used to verify your identity."));

        // Act
        tokenAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(response.getContentAsString()).isEqualTo("The login token sent is malformed and cannot be used to verify your identity.");
    }

    @Test
    @Tag("Unit_Testing")
    public void test_doFilterInternal_signatureException() throws ServletException, IOException {
        // Arrange
        MockHttpServletRequest request = new MockHttpServletRequest();
        String token = "signatureExceptionToken";
        request.addHeader("Authorization", "Bearer " + token);
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain filterChain = new MockFilterChain();

        Mockito.when(jwtService.extractUsername(token)).thenThrow(new SignatureException("JWT signature does not match locally computed signature. JWT validity cannot be asserted and should not be trusted."));

        // Act
        tokenAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(response.getContentAsString()).isEqualTo("JWT signature does not match locally computed signature. JWT validity cannot be asserted and should not be trusted.");
    }
}
