package com.example.fungid.service.user_service;

import com.example.fungid.domain.User;
import com.example.fungid.dto.UserDTO;
import com.example.fungid.exceptions.login.InvalidCredentialsException;
import com.example.fungid.exceptions.register.EmailTakenException;
import com.example.fungid.exceptions.register.UsernameTakenException;
import com.example.fungid.repository.UserRepository;
import com.example.fungid.service.UserService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.*;

/*@SpringBootTest(*//*classes = {
        UserService.class,
        UserRepository.class
}*//*webEnvironment = SpringBootTest.WebEnvironment.NONE)*/
@DataJpaTest
@Import(UserService.class)
@TestPropertySource("classpath:application-test.properties")
class UserServiceIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    User existingUser;

    @BeforeEach
    void setUp() {
        existingUser = new User();
        existingUser.setUsername("user1");
        existingUser.setEmail("user1@gmail.com");
        existingUser.setPassword("password1");

        existingUser = userRepository.save(existingUser);
    }

    @AfterEach
    void tearDown() {
        userRepository.deleteAll();
    }

    @Test
    void test_saveUser_saveSuccessful() {
        // Arrange
        UserDTO userDTO = new UserDTO();
        userDTO.setUsername("user2");
        userDTO.setEmail("user2@gmail.com");
        userDTO.setPassword("password2");

        // Act
        UserDTO savedUser = userService.saveUser(userDTO);

        // Assert
        assertNotNull(savedUser);
        assertNotNull(savedUser.getId());
        assertEquals(userDTO.getUsername(), savedUser.getUsername());
        assertEquals(userDTO.getEmail(), savedUser.getEmail());
        assertEquals(userRepository.findAll().size(), 2);
    }

    @Test
    void test_saveUser_usernameTaken() {
        // Arrange
        UserDTO userDTO = new UserDTO();
        userDTO.setUsername(existingUser.getUsername());
        userDTO.setEmail("random_email");
        userDTO.setPassword("random_password");

        // Act & Assert
        assertThrows(UsernameTakenException.class, () -> userService.saveUser(userDTO));
        assertEquals(userRepository.findAll().size(), 1);
    }

    @Test
    void test_saveUser_emailTaken() {
        // Arrange
        UserDTO userDTO = new UserDTO();
        userDTO.setUsername("random_username");
        userDTO.setEmail(existingUser.getEmail());
        userDTO.setPassword("random_password");

        // Act & Assert
        assertThrows(EmailTakenException.class, () -> userService.saveUser(userDTO));
        assertEquals(userRepository.findAll().size(), 1);
    }

    @Test
    void test_loadUserByCredentials_loadedSuccessfully() {
        // Arrange
        UserDTO userDTO = new UserDTO();
        userDTO.setUsername(existingUser.getUsername());
        userDTO.setPassword(existingUser.getPassword());

        // Act
        UserDTO loadedUser = userService.loadUserByCredentials(userDTO.getUsername(), userDTO.getPassword());

        // Assert
        assertNotNull(loadedUser);
        assertEquals(existingUser.getId(), loadedUser.getId());
        assertEquals(existingUser.getUsername(), loadedUser.getUsername());
        assertEquals(existingUser.getEmail(), loadedUser.getEmail());
    }

    @Test
    void test_loadUserByCredentials_invalidCredentials() {
        // Arrange
        String invalidUsername = "invalid_username";
        String invalidPassword = "invalid_password";

        // Act & Assert
        assertThrows(InvalidCredentialsException.class, () -> userService.loadUserByCredentials(invalidUsername, existingUser.getPassword()));
        assertThrows(InvalidCredentialsException.class, () -> userService.loadUserByCredentials(existingUser.getUsername(), invalidPassword));
        assertThrows(InvalidCredentialsException.class, () -> userService.loadUserByCredentials(invalidUsername, invalidPassword));
    }

    @Test
    void test_getUser_userFound() {
        // Arrange
        Long existingUserId = existingUser.getId();

        // Act
        User foundUser = assertDoesNotThrow(() -> userService.getUser(existingUserId));

        // Assert
        assertNotNull(foundUser);
        assertEquals(existingUser.getId(), foundUser.getId());
        assertEquals(existingUser.getUsername(), foundUser.getUsername());
        assertEquals(existingUser.getEmail(), foundUser.getEmail());
    }

    @Test
    void test_getUser_userNotFound() {
        // Arrange
        Long invalidUserId = 100L;

        // Act & Assert
        assertThrows(InvalidCredentialsException.class, () -> userService.getUser(invalidUserId));
    }

    @Test
    void test_getUserByUsername_userFound() {
        // Arrange
        String existingUsername = existingUser.getUsername();

        // Act
        User foundUser = assertDoesNotThrow(() -> userService.getUserByUsername(existingUsername));

        // Assert
        assertNotNull(foundUser);
        assertEquals(existingUser.getId(), foundUser.getId());
        assertEquals(existingUser.getUsername(), foundUser.getUsername());
        assertEquals(existingUser.getEmail(), foundUser.getEmail());
    }

    @Test
    void test_getUserByUsername_userNotFound() {
        // Arrange
        String invalidUsername = "invalid_username";

        // Act & Assert
        assertThrows(InvalidCredentialsException.class, () -> userService.getUserByUsername(invalidUsername));
    }
}