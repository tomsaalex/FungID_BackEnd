package com.example.fungid.controller.user_controller;

import com.example.fungid.configuration.SecurityConfig;
import com.example.fungid.controller.UserController;
import com.example.fungid.dto.UserDTO;
import com.example.fungid.exceptions.login.InvalidCredentialsException;
import com.example.fungid.exceptions.register.*;
import com.example.fungid.service.JwtService;
import com.example.fungid.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.nio.charset.StandardCharsets;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@Import(SecurityConfig.class)
class UserControllerUnitTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private JwtService jwtService;

    @Test
    void test_registerUser_registerSuccessful() throws Exception {
        UserDTO userDTO = new UserDTO();
        userDTO.setUsername("valid_username");
        userDTO.setPassword("valid_password");
        userDTO.setEmail("valid_email");

        UserDTO expectedUserDTO = new UserDTO();
        expectedUserDTO.setUsername("valid_username");
        expectedUserDTO.setPassword("valid_password");
        expectedUserDTO.setEmail("valid_email");
        expectedUserDTO.setId(1L);

        Mockito.when(userService.saveUser(userDTO)).thenReturn(expectedUserDTO);
        Mockito.when(jwtService.generateToken(userDTO.getUsername())).thenReturn("valid_token");

        String reqBody = new ObjectMapper().writeValueAsString(userDTO);

        this.mockMvc.perform(MockMvcRequestBuilders.post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .accept(MediaType.APPLICATION_JSON_VALUE)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .content(reqBody)
                        .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.token").value("valid_token"));

        Mockito.verify(userService, Mockito.times(1)).saveUser(userDTO);
        Mockito.verify(jwtService, Mockito.times(1)).generateToken(userDTO.getUsername());
    }

    @Test
    void test_registerUser_invalidUsername() throws Exception {
        // Arrange
        UserDTO emptyUsernameUserDTO = new UserDTO();
        emptyUsernameUserDTO.setUsername("");
        emptyUsernameUserDTO.setPassword("valid_password");
        emptyUsernameUserDTO.setEmail("valid_email");

        UserDTO blankUsernameUserDTO = new UserDTO();
        blankUsernameUserDTO.setUsername("  ");
        blankUsernameUserDTO.setPassword("valid_password");
        blankUsernameUserDTO.setEmail("valid_email");

        UserDTO nullUsernameUserDTO = new UserDTO();
        nullUsernameUserDTO.setUsername(null);
        nullUsernameUserDTO.setPassword("valid_password");
        nullUsernameUserDTO.setEmail("valid_email");


        Mockito.when(userService.saveUser(emptyUsernameUserDTO)).thenThrow(new UncompletedFieldsException("Username, Password and Email are all necessary fields for registration."));
        Mockito.when(userService.saveUser(blankUsernameUserDTO)).thenThrow(new UncompletedFieldsException("Username, Password and Email are all necessary fields for registration."));
        Mockito.when(userService.saveUser(nullUsernameUserDTO)).thenThrow(new UncompletedFieldsException("Username, Password and Email are all necessary fields for registration."));

        // Act
        String emptyUsernameReqBody = new ObjectMapper().writeValueAsString(emptyUsernameUserDTO);
        String blankUsernameReqBody = new ObjectMapper().writeValueAsString(blankUsernameUserDTO);
        String nullUsernameReqBody = new ObjectMapper().writeValueAsString(nullUsernameUserDTO);

        this.mockMvc.perform(MockMvcRequestBuilders.post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .accept(MediaType.APPLICATION_JSON_VALUE)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .content(emptyUsernameReqBody)
                        .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(content().string("Username, Password and Email are all necessary fields for registration."));

        this.mockMvc.perform(MockMvcRequestBuilders.post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .accept(MediaType.APPLICATION_JSON_VALUE)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .content(blankUsernameReqBody)
                        .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(content().string("Username, Password and Email are all necessary fields for registration."));

        this.mockMvc.perform(MockMvcRequestBuilders.post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .accept(MediaType.APPLICATION_JSON_VALUE)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .content(nullUsernameReqBody)
                        .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(content().string("Username, Password and Email are all necessary fields for registration."));

        Mockito.verify(userService, Mockito.times(1)).saveUser(emptyUsernameUserDTO);
        Mockito.verify(jwtService, Mockito.times(0)).generateToken(emptyUsernameUserDTO.getUsername());

        Mockito.verify(userService, Mockito.times(1)).saveUser(blankUsernameUserDTO);
        Mockito.verify(jwtService, Mockito.times(0)).generateToken(blankUsernameUserDTO.getUsername());

        Mockito.verify(userService, Mockito.times(1)).saveUser(nullUsernameUserDTO);
        Mockito.verify(jwtService, Mockito.times(0)).generateToken(nullUsernameUserDTO.getUsername());
    }

    @Test
    void test_registerUser_invalidPassword() throws Exception {
        // Arrange
        UserDTO emptyPasswordUserDTO = new UserDTO();
        emptyPasswordUserDTO.setUsername("valid_username");
        emptyPasswordUserDTO.setPassword("");
        emptyPasswordUserDTO.setEmail("valid_email");

        UserDTO blankPasswordUserDTO = new UserDTO();
        blankPasswordUserDTO.setUsername("valid_username");
        blankPasswordUserDTO.setPassword("  ");
        blankPasswordUserDTO.setEmail("valid_email");

        UserDTO nullPasswordUserDTO = new UserDTO();
        nullPasswordUserDTO.setUsername("valid_username");
        nullPasswordUserDTO.setPassword(null);
        nullPasswordUserDTO.setEmail("valid_email");


        Mockito.when(userService.saveUser(emptyPasswordUserDTO)).thenThrow(new UncompletedFieldsException("Username, Password and Email are all necessary fields for registration."));
        Mockito.when(userService.saveUser(blankPasswordUserDTO)).thenThrow(new UncompletedFieldsException("Username, Password and Email are all necessary fields for registration."));
        Mockito.when(userService.saveUser(nullPasswordUserDTO)).thenThrow(new UncompletedFieldsException("Username, Password and Email are all necessary fields for registration."));

        // Act
        String emptyPasswordReqBody = new ObjectMapper().writeValueAsString(emptyPasswordUserDTO);
        String blankPasswordReqBody = new ObjectMapper().writeValueAsString(blankPasswordUserDTO);
        String nullPasswordReqBody = new ObjectMapper().writeValueAsString(nullPasswordUserDTO);

        this.mockMvc.perform(MockMvcRequestBuilders.post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .accept(MediaType.APPLICATION_JSON_VALUE)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .content(emptyPasswordReqBody)
                        .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(content().string("Username, Password and Email are all necessary fields for registration."));

        this.mockMvc.perform(MockMvcRequestBuilders.post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .accept(MediaType.APPLICATION_JSON_VALUE)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .content(blankPasswordReqBody)
                        .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(content().string("Username, Password and Email are all necessary fields for registration."));

        this.mockMvc.perform(MockMvcRequestBuilders.post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .accept(MediaType.APPLICATION_JSON_VALUE)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .content(nullPasswordReqBody)
                        .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(content().string("Username, Password and Email are all necessary fields for registration."));

        Mockito.verify(userService, Mockito.times(1)).saveUser(emptyPasswordUserDTO);
        Mockito.verify(jwtService, Mockito.times(0)).generateToken(emptyPasswordUserDTO.getUsername());

        Mockito.verify(userService, Mockito.times(1)).saveUser(blankPasswordUserDTO);
        Mockito.verify(jwtService, Mockito.times(0)).generateToken(blankPasswordUserDTO.getUsername());

        Mockito.verify(userService, Mockito.times(1)).saveUser(nullPasswordUserDTO);
        Mockito.verify(jwtService, Mockito.times(0)).generateToken(nullPasswordUserDTO.getUsername());
    }

    @Test
    void test_registerUser_invalidEmail() throws Exception {
        // Arrange
        UserDTO emptyEmailUserDTO = new UserDTO();
        emptyEmailUserDTO.setUsername("valid_username");
        emptyEmailUserDTO.setPassword("valid_password");
        emptyEmailUserDTO.setEmail("");

        UserDTO blankEmailUserDTO = new UserDTO();
        blankEmailUserDTO.setUsername("valid_username");
        blankEmailUserDTO.setPassword("valid_password");
        blankEmailUserDTO.setEmail("  ");

        UserDTO nullEmailUserDTO = new UserDTO();
        nullEmailUserDTO.setUsername("valid_username");
        nullEmailUserDTO.setPassword("valid_password");
        nullEmailUserDTO.setEmail(null);


        Mockito.when(userService.saveUser(emptyEmailUserDTO)).thenThrow(new UncompletedFieldsException("Username, Password and Email are all necessary fields for registration."));
        Mockito.when(userService.saveUser(blankEmailUserDTO)).thenThrow(new UncompletedFieldsException("Username, Password and Email are all necessary fields for registration."));
        Mockito.when(userService.saveUser(nullEmailUserDTO)).thenThrow(new UncompletedFieldsException("Username, Password and Email are all necessary fields for registration."));

        // Act
        String emptyEmailReqBody = new ObjectMapper().writeValueAsString(emptyEmailUserDTO);
        String blankEmailReqBody = new ObjectMapper().writeValueAsString(blankEmailUserDTO);
        String nullEmailReqBody = new ObjectMapper().writeValueAsString(nullEmailUserDTO);

        this.mockMvc.perform(MockMvcRequestBuilders.post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .accept(MediaType.APPLICATION_JSON_VALUE)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .content(emptyEmailReqBody)
                        .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(content().string("Username, Password and Email are all necessary fields for registration."));

        this.mockMvc.perform(MockMvcRequestBuilders.post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .accept(MediaType.APPLICATION_JSON_VALUE)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .content(blankEmailReqBody)
                        .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(content().string("Username, Password and Email are all necessary fields for registration."));

        this.mockMvc.perform(MockMvcRequestBuilders.post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .accept(MediaType.APPLICATION_JSON_VALUE)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .content(nullEmailReqBody)
                        .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(content().string("Username, Password and Email are all necessary fields for registration."));

        Mockito.verify(userService, Mockito.times(1)).saveUser(emptyEmailUserDTO);
        Mockito.verify(jwtService, Mockito.times(0)).generateToken(emptyEmailUserDTO.getUsername());

        Mockito.verify(userService, Mockito.times(1)).saveUser(blankEmailUserDTO);
        Mockito.verify(jwtService, Mockito.times(0)).generateToken(blankEmailUserDTO.getUsername());

        Mockito.verify(userService, Mockito.times(1)).saveUser(nullEmailUserDTO);
        Mockito.verify(jwtService, Mockito.times(0)).generateToken(nullEmailUserDTO.getUsername());
    }

    @Test
    void test_registerUser_usernameLengthExceeded() throws Exception {
        // Arrange
        UserDTO longUsernameUserDTO = new UserDTO();
        longUsernameUserDTO.setUsername("a".repeat(51));
        longUsernameUserDTO.setPassword("valid_password");
        longUsernameUserDTO.setEmail("valid_email");

        Mockito.when(userService.saveUser(longUsernameUserDTO)).thenThrow(new UsernameLengthExceededException("Username must not exceed 50 characters."));

        // Act
        String longUsernameReqBody = new ObjectMapper().writeValueAsString(longUsernameUserDTO);

        this.mockMvc.perform(MockMvcRequestBuilders.post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .accept(MediaType.APPLICATION_JSON_VALUE)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .content(longUsernameReqBody)
                        .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(content().string("Username must not exceed 50 characters."));

        Mockito.verify(userService, Mockito.times(1)).saveUser(longUsernameUserDTO);
        Mockito.verify(jwtService, Mockito.times(0)).generateToken(longUsernameUserDTO.getUsername());
    }

    @Test
    void test_registerUser_passwordLengthExceeded() throws Exception {
        // Arrange
        UserDTO longPasswordUserDTO = new UserDTO();
        longPasswordUserDTO.setUsername("valid_username");
        longPasswordUserDTO.setPassword("a".repeat(51));
        longPasswordUserDTO.setEmail("valid_email");

        Mockito.when(userService.saveUser(longPasswordUserDTO)).thenThrow(new PasswordLengthExceededException("Password must not exceed 50 characters."));

        // Act
        String longPasswordReqBody = new ObjectMapper().writeValueAsString(longPasswordUserDTO);

        this.mockMvc.perform(MockMvcRequestBuilders.post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .accept(MediaType.APPLICATION_JSON_VALUE)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .content(longPasswordReqBody)
                        .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(content().string("Password must not exceed 50 characters."));

        Mockito.verify(userService, Mockito.times(1)).saveUser(longPasswordUserDTO);
        Mockito.verify(jwtService, Mockito.times(0)).generateToken(longPasswordUserDTO.getUsername());
    }

    @Test
    void test_registerUser_emailLengthExceeded() throws Exception {
        // Arrange
        UserDTO longEmailUserDTO = new UserDTO();
        longEmailUserDTO.setUsername("valid_username");
        longEmailUserDTO.setPassword("valid_password");
        longEmailUserDTO.setEmail("a".repeat(51));


        Mockito.when(userService.saveUser(longEmailUserDTO)).thenThrow(new EmailLengthExceededException("Email must not exceed 50 characters."));

        // Act
        String longEmailReqBody = new ObjectMapper().writeValueAsString(longEmailUserDTO);

        this.mockMvc.perform(MockMvcRequestBuilders.post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .accept(MediaType.APPLICATION_JSON_VALUE)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .content(longEmailReqBody)
                        .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(content().string("Email must not exceed 50 characters."));

        Mockito.verify(userService, Mockito.times(1)).saveUser(longEmailUserDTO);
        Mockito.verify(jwtService, Mockito.times(0)).generateToken(longEmailUserDTO.getUsername());
    }

    @Test
    void test_registerUser_usernameTaken() throws Exception {
        // Arrange
        UserDTO takenUsernameUserDTO = new UserDTO();
        takenUsernameUserDTO.setUsername("taken_username");
        takenUsernameUserDTO.setPassword("valid_password");
        takenUsernameUserDTO.setEmail("valid_email");

        Mockito.when(userService.saveUser(takenUsernameUserDTO)).thenThrow(new UsernameTakenException("Username already taken."));

        // Act
        String takenUsernameReqBody = new ObjectMapper().writeValueAsString(takenUsernameUserDTO);

        this.mockMvc.perform(MockMvcRequestBuilders.post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .accept(MediaType.APPLICATION_JSON_VALUE)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .content(takenUsernameReqBody)
                        .with(csrf()))
                .andExpect(status().isConflict())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(content().string("Username already taken."));

        Mockito.verify(userService, Mockito.times(1)).saveUser(takenUsernameUserDTO);
    }

    @Test
    void test_registerUser_emailTaken() throws Exception {
        // Arrange
        UserDTO takenEmailUserDTO = new UserDTO();
        takenEmailUserDTO.setUsername("valid_username");
        takenEmailUserDTO.setPassword("valid_password");
        takenEmailUserDTO.setEmail("taken_email");

        Mockito.when(userService.saveUser(takenEmailUserDTO)).thenThrow(new EmailTakenException("Email already taken."));

        // Act
        String takenEmailReqBody = new ObjectMapper().writeValueAsString(takenEmailUserDTO);

        this.mockMvc.perform(MockMvcRequestBuilders.post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .accept(MediaType.APPLICATION_JSON_VALUE)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .content(takenEmailReqBody)
                        .with(csrf()))
                .andExpect(status().isConflict())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(content().string("Email already taken."));

        Mockito.verify(userService, Mockito.times(1)).saveUser(takenEmailUserDTO);
    }

    @Test
    void test_loginUser_loginSuccessful() throws Exception {
        UserDTO userDTO = new UserDTO();
        userDTO.setUsername("valid_username");
        userDTO.setPassword("valid_password");
        userDTO.setEmail("valid_email");

        UserDTO expectedUserDTO = new UserDTO();
        expectedUserDTO.setUsername("valid_username");
        expectedUserDTO.setPassword("valid_password");
        expectedUserDTO.setEmail("valid_email");
        expectedUserDTO.setId(1L);

        Mockito.when(userService.loadUserByCredentials(userDTO.getUsername(), userDTO.getPassword())).thenReturn(expectedUserDTO);
        Mockito.when(jwtService.generateToken(userDTO.getUsername())).thenReturn("valid_token");

        String reqBody = new ObjectMapper().writeValueAsString(userDTO);

        this.mockMvc.perform(MockMvcRequestBuilders.post("/api/users/login")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .accept(MediaType.APPLICATION_JSON_VALUE)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .content(reqBody)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.token").value("valid_token"));

        Mockito.verify(userService, Mockito.times(1)).loadUserByCredentials(userDTO.getUsername(), userDTO.getPassword());
        Mockito.verify(jwtService, Mockito.times(1)).generateToken(userDTO.getUsername());
    }

    @Test
    void test_loginUser_invalidUsername() throws Exception {
        // Arrange
        UserDTO emptyUsernameUserDTO = new UserDTO();
        emptyUsernameUserDTO.setUsername("");
        emptyUsernameUserDTO.setPassword("valid_password");

        UserDTO blankUsernameUserDTO = new UserDTO();
        blankUsernameUserDTO.setUsername("  ");
        blankUsernameUserDTO.setPassword("valid_password");

        UserDTO nullUsernameUserDTO = new UserDTO();
        nullUsernameUserDTO.setUsername(null);
        nullUsernameUserDTO.setPassword("valid_password");

        Mockito.when(userService.loadUserByCredentials(emptyUsernameUserDTO.getUsername(), emptyUsernameUserDTO.getPassword())).thenThrow(new UncompletedFieldsException("Username and Password are necessary fields for login."));
        Mockito.when(userService.loadUserByCredentials(blankUsernameUserDTO.getUsername(), blankUsernameUserDTO.getPassword())).thenThrow(new UncompletedFieldsException("Username and Password are necessary fields for login."));
        Mockito.when(userService.loadUserByCredentials(nullUsernameUserDTO.getUsername(), nullUsernameUserDTO.getPassword())).thenThrow(new UncompletedFieldsException("Username and Password are necessary fields for login."));

        // Act
        String emptyUsernameReqBody = new ObjectMapper().writeValueAsString(emptyUsernameUserDTO);
        String blankUsernameReqBody = new ObjectMapper().writeValueAsString(blankUsernameUserDTO);
        String nullUsernameReqBody = new ObjectMapper().writeValueAsString(nullUsernameUserDTO);

        this.mockMvc.perform(MockMvcRequestBuilders.post("/api/users/login")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .accept(MediaType.APPLICATION_JSON_VALUE)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .content(emptyUsernameReqBody)
                        .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(content().string("Username and Password are necessary fields for login."));

        this.mockMvc.perform(MockMvcRequestBuilders.post("/api/users/login")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .accept(MediaType.APPLICATION_JSON_VALUE)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .content(blankUsernameReqBody)
                        .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(content().string("Username and Password are necessary fields for login."));

        this.mockMvc.perform(MockMvcRequestBuilders.post("/api/users/login")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .accept(MediaType.APPLICATION_JSON_VALUE)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .content(nullUsernameReqBody)
                        .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(content().string("Username and Password are necessary fields for login."));

        Mockito.verify(userService, Mockito.times(1)).loadUserByCredentials(emptyUsernameUserDTO.getUsername(), emptyUsernameUserDTO.getPassword());
        Mockito.verify(jwtService, Mockito.times(0)).generateToken(emptyUsernameUserDTO.getUsername());

        Mockito.verify(userService, Mockito.times(1)).loadUserByCredentials(blankUsernameUserDTO.getUsername(), blankUsernameUserDTO.getPassword());
        Mockito.verify(jwtService, Mockito.times(0)).generateToken(blankUsernameUserDTO.getUsername());

        Mockito.verify(userService, Mockito.times(1)).loadUserByCredentials(nullUsernameUserDTO.getUsername(), nullUsernameUserDTO.getPassword());
        Mockito.verify(jwtService, Mockito.times(0)).generateToken(nullUsernameUserDTO.getUsername());
    }

    @Test
    void test_loginUser_invalidPassword() throws Exception {
        // Arrange
        UserDTO emptyPasswordUserDTO = new UserDTO();
        emptyPasswordUserDTO.setUsername("valid_username");
        emptyPasswordUserDTO.setPassword("");

        UserDTO blankPasswordUserDTO = new UserDTO();
        blankPasswordUserDTO.setUsername("valid_username");
        blankPasswordUserDTO.setPassword("  ");

        UserDTO nullPasswordUserDTO = new UserDTO();
        nullPasswordUserDTO.setUsername("valid_username");
        nullPasswordUserDTO.setPassword(null);

        Mockito.when(userService.loadUserByCredentials(emptyPasswordUserDTO.getUsername(), emptyPasswordUserDTO.getPassword())).thenThrow(new UncompletedFieldsException("Username and Password are necessary fields for login."));
        Mockito.when(userService.loadUserByCredentials(blankPasswordUserDTO.getUsername(), blankPasswordUserDTO.getPassword())).thenThrow(new UncompletedFieldsException("Username and Password are necessary fields for login."));
        Mockito.when(userService.loadUserByCredentials(nullPasswordUserDTO.getUsername(), nullPasswordUserDTO.getPassword())).thenThrow(new UncompletedFieldsException("Username and Password are necessary fields for login."));

        // Act
        String emptyPasswordReqBody = new ObjectMapper().writeValueAsString(emptyPasswordUserDTO);
        String blankPasswordReqBody = new ObjectMapper().writeValueAsString(blankPasswordUserDTO);
        String nullPasswordReqBody = new ObjectMapper().writeValueAsString(nullPasswordUserDTO);

        this.mockMvc.perform(MockMvcRequestBuilders.post("/api/users/login")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .accept(MediaType.APPLICATION_JSON_VALUE)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .content(emptyPasswordReqBody)
                        .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(content().string("Username and Password are necessary fields for login."));

        this.mockMvc.perform(MockMvcRequestBuilders.post("/api/users/login")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .accept(MediaType.APPLICATION_JSON_VALUE)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .content(blankPasswordReqBody)
                        .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(content().string("Username and Password are necessary fields for login."));

        this.mockMvc.perform(MockMvcRequestBuilders.post("/api/users/login")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .accept(MediaType.APPLICATION_JSON_VALUE)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .content(nullPasswordReqBody)
                        .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(content().string("Username and Password are necessary fields for login."));

        Mockito.verify(userService, Mockito.times(1)).loadUserByCredentials(emptyPasswordUserDTO.getUsername(), emptyPasswordUserDTO.getPassword());
        Mockito.verify(jwtService, Mockito.times(0)).generateToken(emptyPasswordUserDTO.getUsername());

        Mockito.verify(userService, Mockito.times(1)).loadUserByCredentials(blankPasswordUserDTO.getUsername(), blankPasswordUserDTO.getPassword());
        Mockito.verify(jwtService, Mockito.times(0)).generateToken(blankPasswordUserDTO.getUsername());

        Mockito.verify(userService, Mockito.times(1)).loadUserByCredentials(nullPasswordUserDTO.getUsername(), nullPasswordUserDTO.getPassword());
        Mockito.verify(jwtService, Mockito.times(0)).generateToken(nullPasswordUserDTO.getUsername());
    }

    @Test
    void test_loginUser_invalidCredentials() throws Exception {
        // Arrange
        UserDTO invalidCredentialsUserDTO = new UserDTO();
        invalidCredentialsUserDTO.setUsername("invalid_username");
        invalidCredentialsUserDTO.setPassword("invalid_password");

        Mockito.when(userService.loadUserByCredentials(invalidCredentialsUserDTO.getUsername(), invalidCredentialsUserDTO.getPassword())).thenThrow(new InvalidCredentialsException("No user found with the given credentials."));

        // Act
        String invalidCredentialsReqBody = new ObjectMapper().writeValueAsString(invalidCredentialsUserDTO);

        this.mockMvc.perform(MockMvcRequestBuilders.post("/api/users/login")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .accept(MediaType.APPLICATION_JSON_VALUE)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .content(invalidCredentialsReqBody)
                        .with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(content().string("No user found with the given credentials."));

        Mockito.verify(userService, Mockito.times(1)).loadUserByCredentials(invalidCredentialsUserDTO.getUsername(), invalidCredentialsUserDTO.getPassword());
        Mockito.verify(jwtService, Mockito.times(0)).generateToken(invalidCredentialsUserDTO.getUsername());
    }
}