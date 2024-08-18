package com.example.fungid.controller.user_controller;

import com.example.fungid.FungidApplication;
import com.example.fungid.dto.AuthDTO;
import com.example.fungid.dto.UserDTO;
import com.example.fungid.test_config.TestFileSystemConfig;
import com.example.fungid.test_config.TestRestTemplateConfig;
import jakarta.annotation.PostConstruct;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.jdbc.Sql;

import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = FungidApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration(classes = {TestFileSystemConfig.class, TestRestTemplateConfig.class})
@Import({TestFileSystemConfig.class, TestRestTemplateConfig.class})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@ActiveProfiles("test")
class UserControllerIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    private String baseUrl;

    @PostConstruct
    void createBaseUrl() {
        baseUrl = "http://localhost:" + port + "/api/users";
    }

    @BeforeEach
    void setUp() {
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    @Tag("Integration_Testing")
    void test_registerUser_registerSuccessful() {
        // Arrange

        UserDTO userDTO = new UserDTO();
        userDTO.setUsername("testUser");
        userDTO.setPassword("testPassword");
        userDTO.setEmail("test_email");

        // Act
        ResponseEntity<AuthDTO> response = restTemplate.postForEntity(baseUrl + "/register", userDTO, AuthDTO.class);

        // Assert
        assertTrue(response.getStatusCode().isSameCodeAs(HttpStatus.CREATED));
        assertNotNull(Objects.requireNonNull(response.getBody()).id());
        assertNotNull(response.getBody().token());
        assertFalse(response.getBody().token().isEmpty());
    }

    @Test
    @Tag("Integration_Testing")
    void test_registerUser_invalidUsername() {
        // Arrange

        UserDTO emptyUsernameUserDTO = new UserDTO();
        emptyUsernameUserDTO.setUsername("");
        emptyUsernameUserDTO.setPassword("testPassword");
        emptyUsernameUserDTO.setEmail("test_email");

        UserDTO blankUsernameUserDTO = new UserDTO();
        blankUsernameUserDTO.setUsername(" ");
        blankUsernameUserDTO.setPassword("testPassword");
        blankUsernameUserDTO.setEmail("test_email");

        UserDTO nullUsernameUserDTO = new UserDTO();
        nullUsernameUserDTO.setUsername(null);
        nullUsernameUserDTO.setPassword("testPassword");
        nullUsernameUserDTO.setEmail("test_email");

        // Act
        ResponseEntity<String> emptyUsernameResponse = restTemplate.postForEntity(baseUrl + "/register", emptyUsernameUserDTO, String.class);
        ResponseEntity<String> blankUsernameResponse = restTemplate.postForEntity(baseUrl + "/register", blankUsernameUserDTO, String.class);
        ResponseEntity<String> nullUsernameResponse = restTemplate.postForEntity(baseUrl + "/register", nullUsernameUserDTO, String.class);

        // Assert
        assertTrue(emptyUsernameResponse.getStatusCode().isSameCodeAs(HttpStatus.BAD_REQUEST));
        assertEquals("Username, Password and Email are all necessary fields for registration.", emptyUsernameResponse.getBody());

        assertTrue(blankUsernameResponse.getStatusCode().isSameCodeAs(HttpStatus.BAD_REQUEST));
        assertEquals("Username, Password and Email are all necessary fields for registration.", blankUsernameResponse.getBody());

        assertTrue(nullUsernameResponse.getStatusCode().isSameCodeAs(HttpStatus.BAD_REQUEST));
        assertEquals("Username, Password and Email are all necessary fields for registration.", nullUsernameResponse.getBody());
    }

    @Test
    @Tag("Integration_Testing")
    void test_registerUser_invalidPassword() {
        // Arrange

        UserDTO emptyPasswordUserDTO = new UserDTO();
        emptyPasswordUserDTO.setUsername("testUsername");
        emptyPasswordUserDTO.setPassword("");
        emptyPasswordUserDTO.setEmail("test_email");

        UserDTO blankPasswordUserDTO = new UserDTO();
        blankPasswordUserDTO.setUsername("testUsername");
        blankPasswordUserDTO.setPassword(" ");
        blankPasswordUserDTO.setEmail("test_email");

        UserDTO nullPasswordUserDTO = new UserDTO();
        nullPasswordUserDTO.setUsername("testUsername");
        nullPasswordUserDTO.setPassword(null);
        nullPasswordUserDTO.setEmail("test_email");

        // Act
        ResponseEntity<String> emptyPasswordResponse = restTemplate.postForEntity(baseUrl + "/register", emptyPasswordUserDTO, String.class);
        ResponseEntity<String> blankPasswordResponse = restTemplate.postForEntity(baseUrl + "/register", blankPasswordUserDTO, String.class);
        ResponseEntity<String> nullPasswordResponse = restTemplate.postForEntity(baseUrl + "/register", nullPasswordUserDTO, String.class);

        // Assert
        assertTrue(emptyPasswordResponse.getStatusCode().isSameCodeAs(HttpStatus.BAD_REQUEST));
        assertEquals("Username, Password and Email are all necessary fields for registration.", emptyPasswordResponse.getBody());

        assertTrue(blankPasswordResponse.getStatusCode().isSameCodeAs(HttpStatus.BAD_REQUEST));
        assertEquals("Username, Password and Email are all necessary fields for registration.", blankPasswordResponse.getBody());

        assertTrue(nullPasswordResponse.getStatusCode().isSameCodeAs(HttpStatus.BAD_REQUEST));
        assertEquals("Username, Password and Email are all necessary fields for registration.", nullPasswordResponse.getBody());
    }

    @Test
    @Tag("Integration_Testing")
    void test_registerUser_invalidEmail() {
        // Arrange

        UserDTO emptyEmailUserDTO = new UserDTO();
        emptyEmailUserDTO.setUsername("testUsername");
        emptyEmailUserDTO.setPassword("testPassword");
        emptyEmailUserDTO.setEmail("");

        UserDTO blankEmailUserDTO = new UserDTO();
        blankEmailUserDTO.setUsername("testUsername");
        blankEmailUserDTO.setPassword("testPassword");
        blankEmailUserDTO.setEmail("  ");

        UserDTO nullEmailUserDTO = new UserDTO();
        nullEmailUserDTO.setUsername("testUsername");
        nullEmailUserDTO.setPassword("testPassword");
        nullEmailUserDTO.setEmail(null);

        // Act
        ResponseEntity<String> emptyEmailResponse = restTemplate.postForEntity(baseUrl + "/register", emptyEmailUserDTO, String.class);
        ResponseEntity<String> blankEmailResponse = restTemplate.postForEntity(baseUrl + "/register", blankEmailUserDTO, String.class);
        ResponseEntity<String> nullEmailResponse = restTemplate.postForEntity(baseUrl + "/register", nullEmailUserDTO, String.class);

        // Assert
        assertTrue(emptyEmailResponse.getStatusCode().isSameCodeAs(HttpStatus.BAD_REQUEST));
        assertEquals("Username, Password and Email are all necessary fields for registration.", emptyEmailResponse.getBody());

        assertTrue(blankEmailResponse.getStatusCode().isSameCodeAs(HttpStatus.BAD_REQUEST));
        assertEquals("Username, Password and Email are all necessary fields for registration.", blankEmailResponse.getBody());

        assertTrue(nullEmailResponse.getStatusCode().isSameCodeAs(HttpStatus.BAD_REQUEST));
        assertEquals("Username, Password and Email are all necessary fields for registration.", nullEmailResponse.getBody());
    }

    @Test
    @Tag("Integration_Testing")
    void test_registerUser_usernameLengthExceeded() {
        // Arrange

        UserDTO usernameLengthExceededUserDTO = new UserDTO();
        usernameLengthExceededUserDTO.setUsername("a".repeat(51));
        usernameLengthExceededUserDTO.setPassword("testPassword");
        usernameLengthExceededUserDTO.setEmail("test_email");

        // Act
        ResponseEntity<String> response = restTemplate.postForEntity(baseUrl + "/register", usernameLengthExceededUserDTO, String.class);

        // Assert
        assertTrue(response.getStatusCode().isSameCodeAs(HttpStatus.BAD_REQUEST));
        assertEquals("Username must not exceed 50 characters.", response.getBody());
    }

    @Test
    @Tag("Integration_Testing")
    void test_registerUser_passwordLengthExceeded() {
        // Arrange

        UserDTO usernameLengthExceededUserDTO = new UserDTO();
        usernameLengthExceededUserDTO.setUsername("testUsername");
        usernameLengthExceededUserDTO.setPassword("a".repeat(51));
        usernameLengthExceededUserDTO.setEmail("test_email");

        // Act
        ResponseEntity<String> response = restTemplate.postForEntity(baseUrl + "/register", usernameLengthExceededUserDTO, String.class);

        // Assert
        assertTrue(response.getStatusCode().isSameCodeAs(HttpStatus.BAD_REQUEST));
        assertEquals("Password must not exceed 50 characters.", response.getBody());
    }

    @Test
    @Tag("Integration_Testing")
    void test_registerUser_emailLengthExceeded() {
        // Arrange

        UserDTO usernameLengthExceededUserDTO = new UserDTO();
        usernameLengthExceededUserDTO.setUsername("testUsername");
        usernameLengthExceededUserDTO.setPassword("testPassword");
        usernameLengthExceededUserDTO.setEmail("a".repeat(51));

        // Act
        ResponseEntity<String> response = restTemplate.postForEntity(baseUrl + "/register", usernameLengthExceededUserDTO, String.class);

        // Assert
        assertTrue(response.getStatusCode().isSameCodeAs(HttpStatus.BAD_REQUEST));
        assertEquals("Email must not exceed 50 characters.", response.getBody());
    }

    @Test
    @Tag("Integration_Testing")
    @Sql({"/user-controller-test.sql"})
    void test_registerUser_usernameTaken() {
        // Arrange

        UserDTO userDTO = new UserDTO();
        userDTO.setUsername("John Doe");
        userDTO.setPassword("testPassword");
        userDTO.setEmail("test_email");

        // Act
        ResponseEntity<String> response = restTemplate.postForEntity(baseUrl + "/register", userDTO, String.class);

        // Assert
        assertTrue(response.getStatusCode().isSameCodeAs(HttpStatus.CONFLICT));
        assertEquals("Username already taken.", response.getBody());
    }

    @Test
    @Tag("Integration_Testing")
    @Sql({"/user-controller-test.sql"})
    void test_registerUser_emailTaken() {
        // Arrange

        UserDTO userDTO = new UserDTO();
        userDTO.setUsername("testUser");
        userDTO.setPassword("testPassword");
        userDTO.setEmail("specific_email");

        // Act
        ResponseEntity<String> response = restTemplate.postForEntity(baseUrl + "/register", userDTO, String.class);

        // Assert
        assertTrue(response.getStatusCode().isSameCodeAs(HttpStatus.CONFLICT));
        assertEquals("Email already taken.", response.getBody());
    }

    @Test
    @Tag("Integration_Testing")
    @Sql({"/user-controller-test.sql"})
    void test_loginUser_loginSuccessful() {
        // Arrange
        UserDTO userDTO = new UserDTO();
        userDTO.setUsername("John Doe");
        userDTO.setPassword("specific_password");

        // Act
        ResponseEntity<AuthDTO> response = restTemplate.postForEntity(baseUrl + "/login", userDTO, AuthDTO.class);

        // Assert
        assertTrue(response.getStatusCode().isSameCodeAs(HttpStatus.OK));
        assertNotNull(Objects.requireNonNull(response.getBody()).id());
        assertNotNull(response.getBody().token());
        assertFalse(response.getBody().token().isEmpty());
    }

    @Test
    @Tag("Integration_Testing")
    void test_loginUser_invalidUsername() {
        // Arrange

        UserDTO emptyUsernameUserDTO = new UserDTO();
        emptyUsernameUserDTO.setUsername("");
        emptyUsernameUserDTO.setPassword("testPassword");

        UserDTO blankUsernameUserDTO = new UserDTO();
        blankUsernameUserDTO.setUsername(" ");
        blankUsernameUserDTO.setPassword("testPassword");

        UserDTO nullUsernameUserDTO = new UserDTO();
        nullUsernameUserDTO.setUsername(null);
        nullUsernameUserDTO.setPassword("testPassword");

        // Act
        ResponseEntity<String> emptyUsernameResponse = restTemplate.postForEntity(baseUrl + "/login", emptyUsernameUserDTO, String.class);
        ResponseEntity<String> blankUsernameResponse = restTemplate.postForEntity(baseUrl + "/login", blankUsernameUserDTO, String.class);
        ResponseEntity<String> nullUsernameResponse = restTemplate.postForEntity(baseUrl + "/login", nullUsernameUserDTO, String.class);

        // Assert
        assertTrue(emptyUsernameResponse.getStatusCode().isSameCodeAs(HttpStatus.BAD_REQUEST));
        assertEquals("Username and Password are necessary fields for login.", emptyUsernameResponse.getBody());

        assertTrue(blankUsernameResponse.getStatusCode().isSameCodeAs(HttpStatus.BAD_REQUEST));
        assertEquals("Username and Password are necessary fields for login.", blankUsernameResponse.getBody());

        assertTrue(nullUsernameResponse.getStatusCode().isSameCodeAs(HttpStatus.BAD_REQUEST));
        assertEquals("Username and Password are necessary fields for login.", nullUsernameResponse.getBody());
    }

    @Test
    @Tag("Integration_Testing")
    void test_loginUser_invalidPassword() {
        // Arrange

        UserDTO emptyPasswordUserDTO = new UserDTO();
        emptyPasswordUserDTO.setUsername("testUsername");
        emptyPasswordUserDTO.setPassword("");

        UserDTO blankPasswordUserDTO = new UserDTO();
        blankPasswordUserDTO.setUsername("testUsername");
        blankPasswordUserDTO.setPassword(" ");

        UserDTO nullPasswordUserDTO = new UserDTO();
        nullPasswordUserDTO.setUsername("testUsername");
        nullPasswordUserDTO.setPassword(null);

        // Act
        ResponseEntity<String> emptyPasswordResponse = restTemplate.postForEntity(baseUrl + "/login", emptyPasswordUserDTO, String.class);
        ResponseEntity<String> blankPasswordResponse = restTemplate.postForEntity(baseUrl + "/login", blankPasswordUserDTO, String.class);
        ResponseEntity<String> nullPasswordResponse = restTemplate.postForEntity(baseUrl + "/login", nullPasswordUserDTO, String.class);

        // Assert
        assertTrue(emptyPasswordResponse.getStatusCode().isSameCodeAs(HttpStatus.BAD_REQUEST));
        assertEquals("Username and Password are necessary fields for login.", emptyPasswordResponse.getBody());

        assertTrue(blankPasswordResponse.getStatusCode().isSameCodeAs(HttpStatus.BAD_REQUEST));
        assertEquals("Username and Password are necessary fields for login.", blankPasswordResponse.getBody());

        assertTrue(nullPasswordResponse.getStatusCode().isSameCodeAs(HttpStatus.BAD_REQUEST));
        assertEquals("Username and Password are necessary fields for login.", nullPasswordResponse.getBody());
    }

    @Test
    @Tag("Integration_Testing")
    @Sql({"/user-controller-test.sql"})
    void test_loginUser_invalidCredentials() {
        // Arrange
        UserDTO wrongUserDTO = new UserDTO();
        wrongUserDTO.setUsername("John Don't");
        wrongUserDTO.setPassword("random_password");

        UserDTO rightUserWrongPasswordDTO = new UserDTO();
        rightUserWrongPasswordDTO.setUsername("John Doe");
        rightUserWrongPasswordDTO.setPassword("random_password");

        // Act
        ResponseEntity<String> response = restTemplate.postForEntity(baseUrl + "/login", wrongUserDTO, String.class);
        ResponseEntity<String> response2 = restTemplate.postForEntity(baseUrl + "/login", rightUserWrongPasswordDTO, String.class);

        // Assert
        assertTrue(response.getStatusCode().isSameCodeAs(HttpStatus.NOT_FOUND));
        assertEquals("No user found with the given credentials.", response.getBody());

        assertTrue(response2.getStatusCode().isSameCodeAs(HttpStatus.NOT_FOUND));
        assertEquals("No user found with the given credentials.", response2.getBody());
    }

    @Test
    @Tag("Integration_Testing")
    void loginUser() {
    }
}